#!/usr/bin/env bash
#
# Deploy idempotente de exerciceAPI.
#
# Flujo: preflight (.env) -> build de la imagen -> up de postgres/garage ->
#        bootstrap de Garage (bucket + keys) -> up de app + seed ->
#        espera de la resolución del seed one-shot.
#
# Variables opcionales:
#   SEED_TIMEOUT       segundos máximos de espera del seed (default: 900 = 15 min;
#                      la primera corrida sube ~2636 objetos a Garage)
#   SEED_POLL_SECONDS  cada cuántos segundos se imprime estado del seed (default: 15)

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${REPO_ROOT}"

SEED_TIMEOUT="${SEED_TIMEOUT:-900}"
SEED_POLL_SECONDS="${SEED_POLL_SECONDS:-15}"

log() { echo "[deploy] $*"; }
err() { echo "[deploy] ERROR: $*" >&2; }
die() { err "$*"; exit 1; }

# ---------------------------------------------------------------------------
# 1) Preflight: docker + compose + .env
# ---------------------------------------------------------------------------

command -v docker >/dev/null 2>&1 || die "docker no está en el PATH."

if docker compose version >/dev/null 2>&1; then
    compose() { docker compose "$@"; }
elif command -v docker-compose >/dev/null 2>&1; then
    compose() { docker-compose "$@"; }
else
    die "docker compose no está disponible (hace falta Compose v2)."
fi

[[ -f .env.example ]] || die "no existe .env.example en la raíz del repo."

if [[ ! -f .env ]]; then
    cp .env.example .env
    log ".env creado desde .env.example — COMPLETALO (passwords/secrets) y volvé a correr."
fi

# Formato del .env: 'CLAVE = valor' o 'CLAVE=valor' (tolera espacios).
env_value() {
    sed -n "s/^${1}[[:space:]]*=[[:space:]]*//p" .env | head -n1
}

for required in POSTGRES_PASSWORD; do
    value="$(env_value "${required}")"
    if [[ -z "${value}" || "${value}" == "***" ]]; then
        die ".env: '${required}' vacío o sin completar (placeholder '***') — editalo y volvé a correr."
    fi
done

# JWT_SECRET: mismo contrato que JwtService.getSigningKey() — si es base64 válido
# debe decodificar a ≥32 bytes; si NO es base64 (fallback a bytes crudos) el valor
# crudo debe tener ≥32 chars.
jwt_secret="$(env_value JWT_SECRET)"
if [[ -z "${jwt_secret}" || "${jwt_secret}" == "***" ]]; then
    die ".env: 'JWT_SECRET' vacío o sin completar (placeholder '***') — editalo y volvé a correr."
fi
if printf '%s' "${jwt_secret}" | base64 -d >/dev/null 2>&1; then
    decoded_len="$(printf '%s' "${jwt_secret}" | base64 -d 2>/dev/null | wc -c)"
    if (( decoded_len < 32 )); then
        die ".env: 'JWT_SECRET' es base64 válido pero decodifica a ${decoded_len} bytes (<32). Generá uno: openssl rand -base64 48"
    fi
elif (( ${#jwt_secret} < 32 )); then
    die ".env: 'JWT_SECRET' inválido: se requieren ≥32 bytes (base64 válido O texto crudo ≥32 chars). Generá uno: openssl rand -base64 48"
fi

# Preflight anti-interpolación: compose v2 interpola `$...` dentro de valores
# sin comillas en .env (típicamente JWT_SECRET con `$` en el valor) → warnings
# "variable is not set" y valor CORROMPIDO al container.
compose_stderr="$(mktemp -t exerciceapi-compose-stderr.XXXXXX)"
if ! compose config >/dev/null 2>"${compose_stderr}"; then
    err "docker compose config falló:"
    cat "${compose_stderr}" >&2
    rm -f "${compose_stderr}"
    exit 1
fi
if grep -q "is not set" "${compose_stderr}"; then
    unset_vars="$(grep -o "The \"[^\"]*\" variable is not set" "${compose_stderr}" | sort -u | sed 's/The "//;s/" variable is not set//' | tr '\n' ' ')"
    rm -f "${compose_stderr}"
    err ".env: hay valores con '\$' que compose interpola (típicamente JWT_SECRET)."
    err "Variables no seteadas detectadas: ${unset_vars}"
    err "Envolvé esos valores entre comillas simples ('...') y volvé a correr."
    exit 1
fi
rm -f "${compose_stderr}"

# ---------------------------------------------------------------------------
# 2) Build único de la imagen (el servicio seed reutiliza exerciceapi-app:latest)
# ---------------------------------------------------------------------------

log "Build de la imagen del servicio 'app'..."
compose build app

# ---------------------------------------------------------------------------
# 3) Infra base
# ---------------------------------------------------------------------------

log "Levantando postgres y garage..."
compose up -d postgres garage

# ---------------------------------------------------------------------------
# 4) Bootstrap de Garage: bucket 'exercise-app' + keys (idempotente)
# ---------------------------------------------------------------------------

BOOTSTRAP_LOG="$(mktemp -t exerciceapi-deploy-bootstrap.XXXXXX)"
trap 'rm -f "${BOOTSTRAP_LOG}"' EXIT

log "Corriendo scripts/garage-bootstrap.sh..."
if ! ./scripts/garage-bootstrap.sh 2>&1 | tee "${BOOTSTRAP_LOG}"; then
    die "garage-bootstrap.sh falló — revisá la salida de arriba."
fi

GARAGE_ACCESS_KEY_NEW="$(sed -n 's/^GARAGE_ACCESS_KEY = //p' "${BOOTSTRAP_LOG}" | tail -n1)"
GARAGE_SECRET_KEY_NEW="$(sed -n 's/^GARAGE_SECRET_KEY = //p' "${BOOTSTRAP_LOG}" | tail -n1)"
if [[ -z "${GARAGE_ACCESS_KEY_NEW}" || -z "${GARAGE_SECRET_KEY_NEW}" ]]; then
    die "no pude extraer GARAGE_ACCESS_KEY / GARAGE_SECRET_KEY del output del bootstrap."
fi

# ---------------------------------------------------------------------------
# 5) Sincronizar las claves de Garage en .env
#    La key que imprime garage-bootstrap.sh es la FUENTE DE VERDAD porque este
#    flujo SIEMPRE usa el bucket/key local de Garage (exercise-app-key). El .env
#    puede tener valores manuales viejos/junk: SIEMPRE comparo contra lo que
#    imprimió el bootstrap y actualizo si difiere (vacío, placeholder o distinto).
# ---------------------------------------------------------------------------

env_set() {
    local key="$1" value="$2"
    if grep -qE "^${key}[[:space:]]*=" .env; then
        sed -i "s|^${key}[[:space:]]*=.*|${key} = ${value}|" .env
    else
        printf '%s = %s\n' "${key}" "${value}" >> .env
    fi
}

# sync_env_key <NOMBRE> <valor-del-bootstrap>: si el valor actual del .env
# difiere, lo reemplaza. Los logs muestran SOLO 4 chars del valor VIEJO
# (jamás secretos completos, ni el nuevo ni el viejo).
sync_env_key() {
    local name="$1" expected="$2" current
    current="$(env_value "${name}")"
    if [[ "${current}" == "${expected}" ]]; then
        log "${name} OK (ya sincronizada)."
        return 0
    fi
    local old_masked="(vacia)"
    if [[ -n "${current}" ]]; then
        old_masked="${current:0:4}..."
    fi
    env_set "${name}" "${expected}"
    log "${name} sincronizada con la key del bootstrap (valor anterior: ${old_masked})."
}

sync_env_key GARAGE_ACCESS_KEY "${GARAGE_ACCESS_KEY_NEW}"
sync_env_key GARAGE_SECRET_KEY "${GARAGE_SECRET_KEY_NEW}"

# ---------------------------------------------------------------------------
# 6) Up completo: app + seed (seed espera los healthchecks de postgres/garage)
# ---------------------------------------------------------------------------

log "Levantando app y seed..."
compose up -d

# ---------------------------------------------------------------------------
# 7) Esperar la resolución del seed one-shot
#    Rompe en exited/dead, pero TAMBIÉN si los logs ya dicen "Seed process
#    completed" aunque el container siga "running" (exit code ambiguo: con
#    --server.port=0 y sin exit-after-run el JVM no se cerraba solo). En ese
#    caso lo detenemos nosotros y contamos como éxito.
# ---------------------------------------------------------------------------

SEED_DONE_MARKER="Seed process completed"

seed_completed_in_logs() {
    compose logs seed 2>/dev/null | grep -q "${SEED_DONE_MARKER}"
}

log "Esperando la resolución del seed (timeout: ${SEED_TIMEOUT}s)..."
elapsed=0
seed_id=""
while :; do
    # -a incluye containers detenidos: sin eso, `ps -q` devuelve vacío tras el exit del seed.
    seed_id="$(compose ps -aq seed 2>/dev/null | tail -n1 || true)"
    if [[ -n "${seed_id}" ]]; then
        state="$(docker inspect -f '{{.State.Status}}' "${seed_id}" 2>/dev/null || echo unknown)"
        if [[ "${state}" == "exited" || "${state}" == "dead" ]]; then
            break
        fi
        # Con 15s de poll, chequear los logs por iteración es barato.
        if seed_completed_in_logs; then
            log "Seed completó según sus logs (container '${state}') — lo detengo yo."
            compose stop seed >/dev/null 2>&1 || true
            break
        fi
        log "seed: ${state} (${elapsed}s)"
    else
        log "seed: container aún no creado (${elapsed}s)"
    fi
    if (( elapsed >= SEED_TIMEOUT )); then
        # Antes de decretar timeout, los logs tienen la última palabra.
        if seed_completed_in_logs; then
            log "Seed completó según sus logs pese al timeout — lo detengo yo."
            compose stop seed >/dev/null 2>&1 || true
            break
        fi
        err "el seed no terminó en ${SEED_TIMEOUT}s. Logs hasta ahora:"
        compose logs seed >&2 || true
        exit 1
    fi
    sleep "${SEED_POLL_SECONDS}"
    elapsed=$((elapsed + SEED_POLL_SECONDS))
done

seed_exit="$(docker inspect -f '{{.State.ExitCode}}' "${seed_id}" 2>/dev/null || echo unknown)"
seed_logs="$(compose logs seed 2>/dev/null || true)"
seed_ok_by_log=0
if grep -q "${SEED_DONE_MARKER}" <<<"${seed_logs}"; then
    seed_ok_by_log=1
fi

# Resolución en este orden: (1) exit 0 → OK; (2) exit ambiguo pero log de
# completado → OK con warning; (3) resto → error.
if [[ "${seed_exit}" == "0" ]]; then
    log "==== Seed OK (exit 0) ===="
elif [[ "${seed_ok_by_log}" -eq 1 ]]; then
    log "==== Seed OK (por logs — exit code ambiguo: ${seed_exit}) ===="
else
    err "el seed terminó con exit code ${seed_exit} y sin log de éxito. Logs completos:"
    compose logs seed >&2 || true
    exit 1
fi

log "Re-correrlo a mano: docker compose run --rm seed"
log "Últimos logs del seed:"
compose logs --tail=30 seed || true
exit 0
