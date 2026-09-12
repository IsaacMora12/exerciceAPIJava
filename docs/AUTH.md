# Sistema de Autenticación - exerciceAPI

## Descripción

Sistema de autenticación basado en JWT (JSON Web Token) con Spring Security.

## Arquitectura

```
Request HTTP
    │
    ▼
┌─────────────────────────────────────────────────────────┐
│                    SPRING SECURITY                       │
│                                                          │
│  ┌──────────────┐    ┌──────────────┐    ┌────────────┐ │
│  │  JwtFilter    │───▶│ CustomUser   │───▶│ Security   │ │
│  │  (intercepta  │    │DetailsService │    │ Context    │ │
│  │   requests)   │    │ (busca en BD) │    │ (guarda)   │ │
│  └──────────────┘    └──────────────┘    └────────────┘ │
│         │                                            │    │
│         │    ┌──────────────┐                      │    │
│         └───▶│  JwtService   │                      │    │
│              │  (genera/     │                      │    │
│              │   valida JWT) │                      │    │
│              └──────────────┘                      │    │
└─────────────────────────────────────────────────────────┘
                              │
                              ▼
                    Controller (request autenticado)
```

## Flujo de Autenticación

### 1. Login (POST /api/auth/login)

```
Cliente                          Servidor
  │                                │
  │  POST /api/auth/login          │
  │  { email, password }           │
  │  ─────────────────────────────▶│
  │                                │
  │                    ┌───────────┴───────────┐
  │                    │ AuthenticationManager  │
  │                    │ (valida credenciales)  │
  │                    └───────────┬───────────┘
  │                                │
  │                    ┌───────────┴───────────┐
  │                    │    JwtService          │
  │                    │  .generateToken()      │
  │                    │  (crea JWT con email   │
  │                    │   y rol del usuario)   │
  │                    └───────────┬───────────┘
  │                                │
  │  { token: "eyJhbG..." }        │
  │  ◀─────────────────────────────│
  │                                │
```

### 2. Request Autenticado

```
Cliente                          Servidor
  │                                │
  │  GET /api/users/1              │
  │  Authorization: Bearer eyJhbG..│
  │  ─────────────────────────────▶│
  │                                │
  │                    ┌───────────┴───────────┐
  │                    │      JwtFilter         │
  │                    │  1. Extrae token       │
  │                    │  2. Verifica firma     │
  │                    │  3. Extrae email       │
  │                    │  4. Busca en BD        │
  │                    │  5. Valida token       │
  │                    │  6. Autentica en Ctx   │
  │                    └───────────┬───────────┘
  │                                │
  │                    ┌───────────┴───────────┐
  │                    │   SecurityContext      │
  │                    │   usuario: juan@..     │
  │                    │   rol: ADMIN           │
  │                    └───────────┬───────────┘
  │                                │
  │  { id: 1, name: "Juan", ... }  │
  │  ◀─────────────────────────────│
  │                                │
```

## Endpoints

### POST /api/auth/login

Autentica un usuario y retorna un JWT.

**Request:**
```json
{
  "email": "juan@mail.com",
  "password": "miPassword123"
}
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1YW5AbWFpbC5jb20i..."
}
```

**Response (401):**
```json
{
  "message": "Invalid email or password",
  "status": 401
}
```

### Endpoints Protegidos

Cualquier endpoint que no sea `/api/auth/**` requiere el header:

```
Authorization: Bearer <token>
```

## Archivos

### Dominio

| Archivo | Descripción |
|---------|-------------|
| `domain/port/auth/in/LoginUseCase.java` | Interfaz del caso de uso de login |

### Aplicación

| Archivo | Descripción |
|---------|-------------|
| `application/services/auth/JwtService.java` | Servicio de generación y validación de JWT |
| `application/services/auth/LoginService.java` | Implementa LoginUseCase, usa AuthenticationManager |

### Infraestructura

| Archivo | Descripción |
|---------|-------------|
| `infrastructure/adapter/in/web/auth/AuthController.java` | Endpoint POST /api/auth/login |
| `infrastructure/adapter/in/web/auth/dto/LoginRequest.java` | DTO de entrada (email, password) |
| `infrastructure/adapter/in/web/auth/dto/LoginResponse.java` | DTO de salida (token) |
| `infrastructure/adapter/out/persistence/auth/JwtFilter.java` | Filtro que intercepta requests y valida JWT |
| `infrastructure/adapter/out/persistence/auth/CustomUserDetailsService.java` | Implementa UserDetailsService, busca usuarios en BD |
| `infrastructure/configuration/SecurityConfig.java` | Configuración de Spring Security |

## Configuración

En `application.properties`:

```properties
# JWT Secret (generado con: openssl rand -base64 32)
jwt.secret=PjucO8eWuGUPEPNQTqSTyoaTtYTj63kleurVnur0eMU=

# JWT Expiration en milisegundos (24 horas)
jwt.expiration=86400000
```

## Seguridad

### Passwords
- Nunca se guardan en texto plano
- Se hashean con BCrypt antes de persistir
- Se validan antes de hashear (mínimo 8 caracteres)

### Tokens
- Firma HMAC-SHA256 con secret key
- Expiración de 24 horas (configurable)
- El payload contiene: email (subject), fecha de creación, fecha de expiración

### Secret Key
- Nunca hardcodeada en el código fuente
- Almacenada en `application.properties`
- En producción: usar variables de entorno

## Errores Comunes

### 401 Invalid or expired token
El token es inválido, expiró o fue manipulado.

### 401 Invalid email or password
Las credenciales del login son incorrectas.

### 403 Forbidden
El usuario está autenticado pero no tiene permisos para acceder al recurso.
