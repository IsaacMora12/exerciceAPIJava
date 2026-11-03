# Sistema de guardado de imágenes/videos, conexión con Garage y seed

> Documento generado a partir del código real del repo. Cada afirmación corresponde a un archivo verificado.

---

## MÓDULO 0 — La regla de oro que explica TODO

**La DB nunca guarda URLs. Guarda CLAVES.**

```
images = ["images/0003-1ZFqTDN.jpg"]     ← esto vive en PostgreSQL
```

La URL (`http://localhost:3900/...?X-Amz-Signature=...`) **no existe persistida en ningún lado**. Se fabrica fresca en cada respuesta HTTP.

¿Por qué? Porque la URL de un objeto depende del entorno (en Docker `localhost:3900`, en prod otro host) y además expira (15 min). Si la guardaras en la DB, cambiar de entorno = URLs muertas en 1318 filas.

Separar **clave (dato)** de **URL (presentación)** es el diseño entero del sistema.

---

## MÓDULO 1 — El dominio define el contrato (puertos)

```
src/main/java/domain/port/storage/
├── FileStorageService.java        ← EL contrato central
├── ImageStorageService.java       ← wrapper con validación de imagen
├── VideoStorageService.java       ← wrapper con validación de video
├── StorageInfrastructureException ← señal "se rompió la infra"
└── in/StorageUploadUseCase.java   ← caso de uso de entrada
```

`FileStorageService` es una interfaz con 4 métodos:

```java
String upload(MultipartFile file, String directory);  // clave ALEATORIA (API)
String uploadAt(MultipartFile file, String key);      // clave DETERMINISTA (seed)
void    delete(String fileKey);
String  getUrl(String fileKey);                       // clave → URL
```

**Acá está la bisagra del sistema**: dos formas de subir.

| Método | Quién lo usa | Clave generada | Ejemplo |
|---|---|---|---|
| `upload()` | API (multipart) | `UUID-random` | `images/8f3a-...-foto.jpg` |
| `uploadAt()` | Seed | la que vos pasás | `images/0003-1ZFqTDN.jpg` |

El seed necesita clave **determinista**: si el JSON dice `0003-1ZFqTDN.jpg`, el objeto tiene que caer EXACTAMENTE ahí. Así la clave es estable entre corridas y la idempotencia funciona. La API no necesita eso: cada subida es única, UUID y listo.

**Decoradores de validación** (`ImageStorageService` / `VideoStorageService`):

```java
public String uploadAs(MultipartFile file, String key) {
    imageFileValidator.validate(file);   // 1) valida
    return delegate.uploadAt(file, key); // 2) delega al adapter
}
```

Las reglas viven en `domain/validation/storage/`:

- **Imagen**: `image/jpeg`, `image/png`, `image/webp`, tope **5 MB**
- **Video**: `image/gif`, `video/mp4`, `video/webm`, `video/quicktime`, tope **10 MB**

**Frontera J1** (la distinción más importante del código): validación fallida = `IllegalArgumentException` = **data-level** (el registro falla, el loop sigue). Infraestructura caída (Garage apagado, credenciales mal) = `StorageInfrastructureException` = **fail-fast** (aborta TODO el proceso). Eso está repetido en cada adapter.

---

## MÓDULO 2 — Los dos adapters (la divergencia del profile)

Spring selecciona con `@Profile`:

```java
@Profile("dev")                  → LocalFileStorageService   (disco local)
@Profile({"garage", "prod"})     → GarageFileStorageService  (S3/Garage)
```

**`LocalFileStorageService`** (solo dev): escribe en `~/.exerciceapi/media/images/...` y `getUrl()` devuelve `"/media/images/0003-....jpg"` — la app después sirve ese directorio en `/media/**` (permitAll en `SecurityConfig`). Cero red, cero firma.

**`GarageFileStorageService`**: habla con el SDK de AWS S3. `uploadAt()` construye un `PutObjectRequest` con bucket + key + contentType + contentLength y manda el stream. `getUrl()` firma la URL con `S3Presigner`.

**El bug del profile**: el seed corre con `garage,seed` (sube los objetos a Garage) y la app puede correr con `dev` (busca los archivos en el disco local, que está vacío). Dos perfiles, dos backends, la misma clave. La clave sirve para los dos, el archivo solo existe en uno → `/media/...` rotos y 500.

---

## MÓDULO 3 — La conexión con Garage (`GarageConfig`)

```java
@Configuration
@Profile({"garage", "prod"})
public class GarageConfig {
    private static final Region GARAGE_REGION = Region.of("garage");
    @Bean S3Client     s3Client(...)      // operativo
    @Bean S3Presigner  s3Presigner(...)   // firma URLs
}
```

### Tres verdades técnicas (cada una es un bug ya corregido)

**1. Dos hosts, dos beans.**

- `garage.endpoint` → `http://garage:3900` (DNS interno de compose) → para el `S3Client` que sube/borra **desde el contenedor**.
- `garage.public-endpoint` → `http://localhost:3900` → para el presigner, porque **la URL firmada la consume el browser del cliente**, y el browser no resuelve `garage` (nombre de servicio interno). Firmar contra el host interno = URLs que nadie puede abrir.

**2. La región es `"garage"`**, no `us-east-1`. Garage valida el scope SigV4 y devuelve `400 AuthorizationHeaderMalformed` si no coincide con `s3_region` de `garage.toml`.

**3. Tres desactivadores de compatibilidad** (S3 puro vs S3-compatible):

```java
.forcePathStyle(true)                                    // /bucket/key, no virtual-host
.chunkedEncodingEnabled(false)                           // sin aws-chunked streaming
.requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)  // sin CRC32 trailer
```

Garage v2.3.0 rechaza el `aws-chunked` que el SDK manda por default con `400 Invalid payload signature`. Mismo workaround que Cloudflare R2.

El presigner **debe** espejar `pathStyleAccessEnabled(true)` — si el cliente firma virtual-host y el objeto se pide path-style, Garage responde `403 SignatureDoesNotMatch`.

### Cómo llegan las credenciales

```
garage.toml
  → garage key create exercise-app-key
    → key info --show-secret
      → lo imprime garage-bootstrap.sh
        → deploy.sh lo sincroniza en .env
          → compose lo inyecta como GARAGE_ACCESS_KEY / GARAGE_SECRET_KEY
            → Spring los lee como garage.access-key / garage.secret-key
```

---

## MÓDULO 4 — `garage-bootstrap.sh` (prepara el cluster)

Corre **antes** de la app, vía `docker exec <garage> /garage ...`. Es idempotente (8 pasos, cada uno chequea si ya está):

```
1. Espera al daemon RPC     → loop: garage layout show (hasta 120s)
2. garage node id           → hexid del nodo
3. garage layout assign     → capacidad 10G, zona dc1
4. garage layout apply      → --version (actual + 1)
5. garage key create        → exercise-app-key (si no existe)
6. garage bucket create     → exercise-app (si no existe)
7. garage bucket allow      → --key X --read --write bucket
8. garage bucket website --allow   → acceso anónimo vía website endpoint (3902)
9. imprime credenciales     → GARAGE_ACCESS_KEY / GARAGE_SECRET_KEY
```

**Detalle que importa**: la imagen `dxflrs/garage:v2.3.0` es **distroless** — no hay `sh`, no hay `garage` en `$PATH`. Por eso SIEMPRE la ruta absoluta `/garage` (mismo criterio en el healthcheck del compose).

---

## MÓDULO 5 — `DataInitializer` (el seed, paso a paso)

Se activa solo con `@Profile("seed")`. Es un `CommandLineRunner`.

### 5.1 — Ingesta de JSON

```java
readSeedJson("static/data/exercise.json")  →  List<Map<String,Object>>
```

Dos archivos embebidos en el classpath del JAR:

| Archivo | Contenido |
|---|---|
| `static/data/exercise.json` | **1324 filas, 1318 nombres únicos** (6 duplicados) |
| `static/data/muscle.json` | 40 músculos |
| `static/data/images/` | **1324 archivos** (~137 MB) |
| `static/data/videos/` | **1324 GIFs** |

### 5.2 — Loop de músculos

```java
for (músculo del JSON) {
    findByName(name)              // ← IDEMPOTENCIA: ¿ya existe?
        ├─ sí → nameToId.put(), alreadyExisted++, CONTINÚA (no sube media)
        └─ no → uploadAssets(images, "images")  →  save(Muscle.create(...))
}
```

El mapa `nameToId` es crítico: los ejercicios referencian músculos **por nombre** ("abs", "biceps") y lo traducen a ID con este mapa.

### 5.3 — Loop de ejercicios

```java
for (ejercicio) {
    seen.add(name)            // dedup DENTRO del archivo (los 6 duplicados)
    findByName(name)          // dedup vs corridas anteriores
    resolveMuscleId(...)      // nombre → ID (exacto + fuzzy contains)
    uploadAssets(images, "images") + uploadAssets(videos, "videos")
    save(Exercise.create(...))
}
```

### 5.4 — `uploadAssets` → `uploadSeedAsset` (el corazón del cargado)

```java
assetUrl = "https://example.com/images/0003-1ZFqTDN.jpg"
basename = basenameOf(assetUrl)                 // "0003-1ZFqTDN.jpg"
requireConverterSafe(basename)                  // sin coma/[ ]/espacios
ClassPathResource("static/data/images/0003-1ZFqTDN.jpg")  // ¿existe?
    ├─ no existe → return null  → assetsMissing++  (el registro igual se crea)
    └─ sí → bytes → ClasspathMultipartFile(basename, contentType, bytes)
             key = "images/0003-1ZFqTDN.jpg"
             imageStorageService.uploadAs(file, key)
                 ├─ validate()  → tipo + 5MB
                 └─ uploadAt()  → PutObjectRequest → Garage
```

**La URL `https://example.com/...` del JSON es un placeholder descartado**: solo se extrae el basename. Lo que persiste es la clave determinista `images/0003-1ZFqTDN.jpg`.

`ClasspathMultipartFile` es el puente `Resource del classpath → MultipartFile`, para que el seed corra por **la misma cadena de validación** que una subida por API. Si mañana subís un `.exe` de 900 MB, cae igual por los mismos validadores — el seed no tiene atajos.

### 5.5 — Matriz de fallos J1 (por qué no revienta todo con la primera clave mala)

| Excepción | Clase | Qué hace |
|---|---|---|
| `IllegalArgumentException` | data-level | `failed++`, el loop **sigue** |
| asset no encontrado | D2 | `assetsMissing++`, el registro **se crea igual** con lista vacía |
| `StorageInfrastructureException` | infra | **fail-fast**: logea contadores y aborta TODO |

Un GIF que no existe no mata 1318 ejercicios. Garage apagado sí te aborta — no vas a tener 1300 registros a medias.

### 5.6 — Por qué NO tiene `@Transactional` de clase

Documentado a propósito en el javadoc: cada `save()` corre en su propia transacción. Si un `save` falla, solo se revierte ese. Con transacción de clase, un fallo deja la transacción en estado `rollback-only` y **todos** los saves siguientes fallan con "current transaction is aborted".

### 5.7 — Salida

```java
log("🎉 Seed process completed! Muscles: {}, Exercises: {}", ...);
if (exitAfterRun) System.exit(0);   // --seed.exit-after-run=true
```

`System.exit(0)` solo en el one-shot de Docker. Sin esto, el contexto servlet (`--server.port=0`, necesario para el wiring de Spring Security) deja threads de Tomcat vivos y **el JVM nunca termina** (el bug del contenedor seed colgado). En `dev,seed` local nunca se activa. Los fallos salen con exit 1 porque la excepción escapa de `main()`.

---

## MÓDULO 6 — `deploy.sh` (el orquestador)

```
1. Preflight       → docker, compose, .env existe/completo
                     JWT_SECRET: base64 válido ≥32 bytes O crudo ≥32 chars
                     anti-interpolación: `$` en .env rompe compose → chequea
                     "is not set" en stderr y muere con instrucción
2. compose build   → 1 imagen única: exerciceapi-app:latest (app y seed la comparten)
3. up infra        → postgres + garage (con healthchecks)
4. garage-bootstrap → bucket + keys (idempotente)
5. sync keys       → SIEMPRE sobrescribe GARAGE_* en .env si difiere del bootstrap
                     (el bootstrap es la fuente de verdad; logs solo 4 chars)
6. up full         → app + seed (seed espera healthchecks)
7. wait del seed   → loop cada 15s, timeout 900s
                     rompe en: exit / dead / log "Seed process completed"
                     (los logs tienen la última palabra en el timeout)
                     resolución: exit 0 → OK | log+exit ambiguo → OK+warning | resto → error
```

**Regla de oro del preflight**: validar igual que el runtime. Si `JwtService` hace fallback a bytes crudos solo cuando el decode falla, el preflight no puede aceptar base64-decodificado-corto — deploy pasaría y el login revienta igual después.

---

## MÓDULO 7 — El camino de LECTURA (clave → URL → `<img>`)

```
GET /api/exercise/3
        ↓
ViewExerciseController
        ↓
exercise.getImages()  →  ["images/0003-1ZFqTDN.jpg"]    (clave cruda de la DB)
        ↓
MediaUrlResolver.toUrls(...)
        ├─ null/blank        → passthrough
        ├─ contiene "://"    → passthrough (legacy, URLs ya armadas)
        └─ lo demás          → fileStorageService.getUrl(key)
                                      ↓
                          GarageFileStorageService.getUrl()
                                      ↓
                    S3Presigner.presignGetObject(...)   ← HMAC LOCAL, sin red
                                      ↓
        "http://localhost:3900/exercise-app/images/0003-1ZFqTDN.jpg
         ?X-Amz-Algorithm=...&X-Amz-Expires=5400&X-Amz-Signature=..."
```

- **900 s = 15 min** de validez (`garage.presign-expiry-seconds`)
- El firmado es **HMAC local**: no hay llamada a Garage por cada respuesta, es matemática pura sobre la clave
- El browser baja el objeto **directo de Garage**, sin pasar por tu API — tu Spring solo firma

---

## Diagrama completo

```
[ESCRITURA - seed]
static/data/exercise.json  ─┐
static/data/images/*.jpg   ─┼→ DataInitializer → basename → ClasspathMultipartFile
static/data/videos/*.gif   ─┘         │
                                      ↓
                        ImageStorageService.uploadAs()  → validate (tipo/5MB)
                                      ↓
                        FileStorageService.uploadAt()   → clave determinista
                                      ↓
                        GarageFileStorageService → S3Client.putObject()
                                      ↓
                        http://garage:3900  bucket=exercise-app  key=images/0003-...jpg
                                      ↓
                              GARAGE (2636 objetos, 137 MB)
                                      ↓
                        DB guarda SOLO:  images = ["images/0003-...jpg"]

[LECTURA - API]
GET /api/exercise/3 → DB lee clave → MediaUrlResolver → S3Presigner (HMAC local)
                                          ↓
                            URL firmada 15 min  →  <img src="...">
                                          ↓
                            browser baja DIRECTO de Garage
```

---

## Los tres errores de integración, en una línea cada uno

| Error | Causa | Fix |
|---|---|---|
| `AuthorizationHeaderMalformed` | Región SigV4 distinta a `garage.toml` | `Region.of("garage")` |
| `Invalid payload signature` | SDK manda `aws-chunked` streaming | `chunkedEncodingEnabled(false)` + `WHEN_REQUIRED` |
| URLs que el browser no abre | Firmaba contra el endpoint operativo (red interna) | split `endpoint` / `public-endpoint` |
| `/media/...` rotos en la API | App con profile `dev`, seed con `garage` | `SPRING_PROFILES_ACTIVE=garage` en `.env` |
