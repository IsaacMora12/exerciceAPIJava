package domain.model;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class User {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private Long id;
    private String name;
    private String email;
    private String password;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor privado — TODOS los constructores pasan por acá
    private User(Long id, String name, String email, String password,
                 Boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        validateName(name);
        validateEmail(email);
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Fábrica 1: Crear usuario nuevo
    public static User create(String name, String email, String password) {
        return new User(null, name, email, password, true, LocalDateTime.now(), LocalDateTime.now());
    }

    // Fábrica 2: Reconstruir usuario desde la BD
    public static User reconstruct(Long id, String name, String email, String password,
                                   Boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (id == null) {
            throw new IllegalArgumentException("The id must not be empty for an existing user");
        }
        return new User(id, name, email, password, isActive, createdAt, updatedAt);
    }

    // Métodos de negocio
    public void activate() {
        this.isActive = true;
        updateTimestamp();
    }

    public void deactivate() {
        this.isActive = false;
        updateTimestamp();
    }

    public void updateEmail(String email) {
        validateEmail(email);
        this.email = email;
        updateTimestamp();
    }

    public void updateName(String name) {
        validateName(name);
        this.name = name;
        updateTimestamp();
    }

    // Almacena el password hasheado SIN validar (el service valida antes)
    public void setPassword(String hashedPassword) {
        this.password = hashedPassword;
        updateTimestamp();
    }

    // Valida la fuerza del password plano — el service llama esto ANTES de hashear
    public static void validatePasswordStrength(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("The password must not be empty");
        }
        if (rawPassword.length() < 8) {
            throw new IllegalArgumentException("The password must be at least 8 characters");
        }
    }

    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    // Validaciones del dominio
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("The name must not be empty");
        }
    }

    private void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("This email address is invalid");
        }
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Boolean getIsActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
