#!/usr/bin/env bash
#
# Bootstrap idempotente del cluster Garage local (single-node) para exerciceAPI.
#
# Uso:
#   ./scripts/garage-bootstrap.sh
#
# Variables opcionales:
#   GARAGE_BIN         path absoluto del binario dentro del container (default: /garage)
#   GARAGE_CONTAINER   container de Garage (auto-detect si no se pasa)
#   GARAGE_CAPACITY    capacidad de storage del nodo  (default: 10G)
#   GARAGE_ZONE        zona/datacenter del nodo       (default: dc1)
#
# La sintaxis de todos los comandos `garage` está verificada contra Garage
# v2.3.0 (dxflrs/garage:v2.3.0, el mismo image que usa docker-compose.yaml).
# El CLI corre SIEMPRE vía `docker exec` porque el container tiene
# /etc/garage.toml montado (garage.toml del repo). La imagen es DISTROLESS
# (sin shell ni binarios en $PATH): dentro del container se invoca SIEMPRE
# el binario por su path absoluto (${GARAGE_BIN}, default /garage).

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# Path absoluto del binario: la imagen dxflrs/garage:v2.3.0 es DISTROLESS —
# no tiene `sh` ni `garage` en $PATH (verificado: `docker exec <c> garage ...`
# falla con "executable file not found in $PATH"; `/garage ...` funciona).
GARAGE_BIN="${GARAGE_BIN:-/garage}"
GARAGE_CAPACITY="${GARAGE_CAPACITY:-10G}"
GARAGE_ZONE="${GARAGE_ZONE:-dc1}"
WAIT_ATTEMPTS="${GARAGE_WAIT_ATTEMPTS:-60}"
WAIT_SECONDS="${GARAGE_WAIT_SECONDS:-2}"
KEY_NAME="exercise-app-key"
BUCKET_NAME="exercise-app"

log() { echo "[garage-bootstrap] $*"; }

# ---------------------------------------------------------------------------
# Container + CLI
# ---------------------------------------------------------------------------

detect_container() {
    local id=""
    # 1) compose del repo (si compose v2 está disponible)
    id="$(docker compose -f "${REPO_ROOT}/docker-compose.yaml" ps -q garage 2>/dev/null || true)"
    if [[ -z "${id}" ]] && command -v docker-compose >/dev/null 2>&1; then
        id="$(docker-compose -f "${REPO_ROOT}/docker-compose.yaml" ps -q garage 2>/dev/null || true)"
    fi
    # 2) fallback: cualquier container que matchee el nombre
    if [[ -z "${id}" ]]; then
        id="$(docker ps -q --filter name=garage 2>/dev/null | head -n1 || true)"
    fi
    echo "${id}"
}

GARAGE_CONTAINER="$(detect_container)"
if [[ -z "${GARAGE_CONTAINER}" ]]; then
    echo "ERROR: no se encontró el container de Garage." >&2
    echo "       Levantalo con: docker compose up -d garage" >&2
    echo "       (o exportá GARAGE_CONTAINER=<id> y volvé a correr el script)" >&2
    exit 1
fi
log "Container de Garage: ${GARAGE_CONTAINER}"

# Todas las operaciones administrativas van contra el daemon dentro del container.
# OJO: SIEMPRE por path absoluto ("${GARAGE_BIN}") — la imagen es distroless,
# sin $PATH, y el binario pelado `garage` no se resuelve (mismo criterio que el
# healthcheck del compose, que usa /garage).
garage() {
    docker exec "${GARAGE_CONTAINER}" "${GARAGE_BIN}" "$@"
}

# ---------------------------------------------------------------------------
# 1) Esperar a que el daemon responda por RPC
#    (`garage layout show` es el primer comando que necesita el daemon vivo;
#     `garage node id` es local y no sirve como healthcheck)
# ---------------------------------------------------------------------------
log "Esperando a que Garage responda por RPC..."
ready=0
for ((attempt = 1; attempt <= WAIT_ATTEMPTS; attempt++)); do
    if garage layout show >/dev/null 2>&1; then
        ready=1
        break
    fi
    sleep "${WAIT_SECONDS}"
done
if [[ "${ready}" -ne 1 ]]; then
    echo "ERROR: Garage no respondió por RPC tras $((WAIT_ATTEMPTS * WAIT_SECONDS))s." >&2
    echo "       Revisá: docker logs ${GARAGE_CONTAINER}" >&2
    exit 1
fi
log "RPC de Garage OK."

# ---------------------------------------------------------------------------
# 2) Node id
#    `garage node id` imprime `<hexid>@<addr>` cuando hay rpc_public_addr,
#    así que nos quedamos con la parte antes de '@'.
# ---------------------------------------------------------------------------
NODE_ID="$(garage node id | head -n1 | cut -d'@' -f1)"
if [[ -z "${NODE_ID}" ]]; then
    echo "ERROR: no se pudo obtener el node id." >&2
    exit 1
fi
log "Node id: ${NODE_ID}"

# ---------------------------------------------------------------------------
# 3) Capacidad del nodo
#    En v2.3 NO existe `layout assign-capacity`: el comando es
#    `garage layout assign <node> -z <zone> -c <capacity>`.
#    Idempotente: si el rol ya está staged o vigente con la misma capacidad,
#    Garage lo descarta como cambio (no-op) y no hay nada que aplicar después.
# ---------------------------------------------------------------------------
log "Asignando capacidad ${GARAGE_CAPACITY} (zona ${GARAGE_ZONE})..."
garage layout assign "${NODE_ID}" -z "${GARAGE_ZONE}" -c "${GARAGE_CAPACITY}"

# ---------------------------------------------------------------------------
# 4) Apply del layout
#    En v2.3 `layout apply` exige --version = versión actual + 1 (NO es
#    `-c 1`). Si no hay cambios staged, no hay nada que aplicar: éxito.
# ---------------------------------------------------------------------------
layout_show="$(garage layout show)"
if grep -q "==== STAGED ROLE CHANGES ====" <<<"${layout_show}"; then
    current_version="$(sed -n 's/^Current cluster layout version: //p' <<<"${layout_show}" | head -n1)"
    if [[ -z "${current_version}" ]]; then
        echo "ERROR: no pude leer la versión actual del layout." >&2
        exit 1
    fi
    new_version="$((current_version + 1))"
    log "Aplicando layout: versión ${current_version} -> ${new_version}..."
    garage layout apply --version "${new_version}"
else
    log "Layout ya aplicado (sin cambios staged) — nada que aplicar."
fi

# ---------------------------------------------------------------------------
# 5) Access key
#    OJO: en v2.3 `key create` NO falla si el nombre ya existe: crea OTRA key
#    con el mismo nombre. Hay que chequear antes para que sea idempotente.
#    Sintaxis real: el nombre es POSICIONAL (no hay flag --name).
# ---------------------------------------------------------------------------
if garage key list | grep -qw "${KEY_NAME}"; then
    log "Key '${KEY_NAME}' ya existe — se reutiliza."
else
    log "Creando key '${KEY_NAME}'..."
    garage key create "${KEY_NAME}"
fi

# ---------------------------------------------------------------------------
# 6) Bucket
#    `bucket create` falla si el alias global ya existe: chequeamos antes.
# ---------------------------------------------------------------------------
if garage bucket list | grep -qw "${BUCKET_NAME}"; then
    log "Bucket '${BUCKET_NAME}' ya existe."
else
    log "Creando bucket '${BUCKET_NAME}'..."
    garage bucket create "${BUCKET_NAME}"
fi

# ---------------------------------------------------------------------------
# 7) Permisos de la key sobre el bucket
#    Sintaxis real v2.3 (PermBucketOpt): --key es OBLIGATORIO y el bucket va
#    POSICIONAL. NO es `bucket allow --read --write <key> --bucket <bucket>`
#    (esa era la sintaxis de Garage 0.x).
#    Idempotente: re-aplicar los permisos deja el bucket en el mismo estado.
# ---------------------------------------------------------------------------
log "Otorgando permisos read/write a '${KEY_NAME}' sobre '${BUCKET_NAME}'..."
garage bucket allow --key "${KEY_NAME}" --read --write "${BUCKET_NAME}"

# ---------------------------------------------------------------------------
# 8) Bucket público
#    En v2.3 NO existe `bucket set --allow public`. El comando real es
#    `bucket website --allow` (habilita acceso anónimo de lectura).
#    Nota: en Garage v2.3 el acceso anónimo funciona vía website endpoint
#    (s3_web, puerto 3902), NO vía el endpoint S3 (3900).
# ---------------------------------------------------------------------------
log "Haciendo público el bucket '${BUCKET_NAME}' (website access)..."
garage bucket website --allow "${BUCKET_NAME}"

# ---------------------------------------------------------------------------
# 9) Credenciales para .env
# ---------------------------------------------------------------------------
key_info="$(garage key info "${KEY_NAME}" --show-secret)"
access_key_id="$(sed -n 's/^Key ID:[[:space:]]*//p' <<<"${key_info}" | head -n1)"
secret_key="$(sed -n 's/^Secret key:[[:space:]]*//p' <<<"${key_info}" | head -n1)"

echo
log "==== CREDENCIALES (no las commitees) ===="
echo "GARAGE_ACCESS_KEY = ${access_key_id}"
echo "GARAGE_SECRET_KEY = ${secret_key}"
echo
log "Copiá lo de arriba a .env como GARAGE_ACCESS_KEY / GARAGE_SECRET_KEY."
log "Listo. Cluster Garage configurado para '${BUCKET_NAME}'."
