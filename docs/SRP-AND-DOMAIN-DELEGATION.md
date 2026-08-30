# SRP y Delegación al Dominio en UpdateUserService

## El Problema

Al implementar el endpoint `PUT /api/users/{id}`, surgió la pregunta:

> **¿Debería crear un servicio separado para cada campo (name, email, password) o uno solo que los maneje todos?**

## Análisis de Opciones

### Opción A: Servicios separados + Orquestador

```
UpdateUser (controller)
    ↓
UpdateUserService (orquestador)
    ↓
├── UpdateUserNameService
├── UpdateUserEmailService
└── UpdateUserPasswordService
```

**Problema:** 5 clases para 1 endpoint. Los services separados son trivialmente simples:

```java
// UpdateUserNameService - hace EXACTAMENTE esto:
User user = userRepository.findById(userId)...;
user.updateName(newName);  // ← una línea de lógica real
userRepository.save(user);
```

Crear una clase entera para envolver una línea es **over-engineering**, no SRP.

### Opción B: Un solo servicio que delega al dominio

```
UpdateUser (controller)
    ↓
UpdateUserService (coordina)
    ↓
User.updateName()      ← dominio
User.updateEmail()     ← dominio
User.updatePassword()  ← dominio
```

**Resultado:** 2 clases para 1 endpoint. La lógica de validación y comportamiento está en el dominio.

## Por Qué la Opción B es Correcta

### 1. SRP se cumple en el nivel correcto

| Clase | Responsabilidad | ¿Cuándo cambia? |
|-------|----------------|-----------------|
| `User.updateName()` | Validar y aplicar regla de negocio del nombre | Cambia la regla del nombre |
| `User.updateEmail()` | Validar y aplicar regla del email | Cambia la validación del email |
| `User.updatePassword()` | Validar y aplicar regla del password | Cambia la política de passwords |
| `UpdateUserService` | Coordinar el flujo de actualización | Cambia el flujo de actualización |

**El SRP vive en el dominio, no en los services.**

### 2. Los services separados son tubería, no lógica

```java
// Esto es tubería:
User user = userRepository.findById(id);
user.updateXxx(value);
userRepository.save(user);

// Se repite en CADA service, solo cambia updateXxx
```

Dividir tubería en 3 partes no es separación de responsabilidades, es **duplicación con extra pasos**.

### 3. El dominio ya tiene la separación

```java
// User.java - acá está el SRP real
public void updateName(String name) {
    validateName(name);
    this.name = name;
    updateAtMethod();
}

public void updateEmail(String email) {
    validateEmail(email);
    this.email = email;
    updateAtMethod();
}

public void updatePassword(String password) {
    validatePassword(password);
    this.password = password;
    updateAtMethod();
}
```

Cada método tiene su propia validación. Si mañana la validación del email cambia, solo modificás `updateEmail()`. Eso es SRP.

### 4. El service solo decide qué llamar

```java
// UpdateUserService.java
if (name != null) {
    user.updateName(name);      // delega al dominio
}
if (email != null) {
    user.updateEmail(email);    // delega al dominio
}
if (password != null) {
    user.updatePassword(hashed); // delega al dominio
}
```

**Su única responsabilidad:** determinar qué campos actualizar y coordinar el flujo. No valida, no aplica reglas, no transforma datos.

## Regla Práctica

> **Si la lógica de negocio está en el dominio, no necesitás dividir el service.**

Dividir el service solo tiene sentido cuando:
- Cada parte tiene **reglas de negocio diferentes** que justifiquen una clase separada
- Los use cases son **diferentes** (ej: un endpoint solo para nombre, otro solo para email)
- Hay ** dependencias diferentes** (ej: un servicio necesita un email sender, otro no)

## Referencia: Arquitectura Hexagonal

```
┌─────────────────────────────────────────┐
│  DOMINIO                                │
│  - Modelo: User (validación + reglas)   │
│  - Puertos: UpdateUserUseCase (interfaz)│
└─────────────────────────────────────────┘
         ↑ define QUÉ
         │
┌─────────────────────────────────────────┐
│  APLICACIÓN                             │
│  - UpdateUserService (coordina)         │
│  - NO contiene lógica de negocio        │
└─────────────────────────────────────────┘
         ↑ implementa CÓMO
         │
┌─────────────────────────────────────────┐
│  INFRAESTRUCTURA                        │
│  - UpdateUser (controller)              │
│  - UserRepositoryAdapter (persistencia) │
└─────────────────────────────────────────┘
```
