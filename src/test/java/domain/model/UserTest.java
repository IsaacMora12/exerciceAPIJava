package domain.model;

import domain.model.user.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    // ========== CREATE ==========
    @Test
    void shouldCreateUserWithValidData() {
        User user = User.create("Juan", "juan@email.com", "password123");

        assertEquals("Juan", user.getName());
        assertEquals("juan@email.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertTrue(user.getIsActive());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertNull(user.getId(), "ID should be null before persisting");
    }

    // ========== NAME VALIDATIONS ==========
    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                User.create(null, "juan@email.com", "password123"));
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                User.create("", "juan@email.com", "password123"));
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                User.create("   ", "juan@email.com", "password123"));
    }

    // ========== EMAIL VALIDATIONS ==========
    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                User.create("Juan", null, "password123"));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsInvalid() {
        assertThrows(IllegalArgumentException.class, () ->
                User.create("Juan", "no-email", "password123"));
    }

    @Test
    void shouldThrowExceptionWhenEmailHasNoDomain() {
        assertThrows(IllegalArgumentException.class, () ->
                User.create("Juan", "juan@", "password123"));
    }

    @Test
    void shouldThrowExceptionWhenEmailHasNoAt() {
        assertThrows(IllegalArgumentException.class, () ->
                User.create("Juan", "juanemail.com", "password123"));
    }

    // ========== PASSWORD VALIDATIONS ==========

    @Test
    void shouldThrowExceptionWhenPasswordIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                User.validatePasswordStrength(null));
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                User.validatePasswordStrength(""));
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsTooShort() {
        assertThrows(IllegalArgumentException.class, () ->
                User.validatePasswordStrength("short"));
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsExactly7Chars() {
        assertThrows(IllegalArgumentException.class, () ->
                User.validatePasswordStrength("1234567"));
    }

    @Test
    void shouldAcceptPasswordWithExactly8Chars() {
        assertDoesNotThrow(() -> User.validatePasswordStrength("12345678"));
    }

    // ========== BUSINESS METHODS ==========
    @Test
    void shouldDeactivateUser() {
        User user = User.create("Juan", "juan@email.com", "password123");
        assertTrue(user.getIsActive());

        user.deactivate();
        assertFalse(user.getIsActive());
    }

    @Test
    void shouldActivateUser() {
        User user = User.create("Juan", "juan@email.com", "password123");
        user.deactivate();
        assertFalse(user.getIsActive());

        user.activate();
        assertTrue(user.getIsActive());
    }

    @Test
    void shouldUpdateName() {
        User user = User.create("Juan", "juan@email.com", "password123");
        user.updateName("Pedro");
        assertEquals("Pedro", user.getName());
    }

    @Test
    void shouldUpdateEmail() {
        User user = User.create("Juan", "juan@email.com", "password123");
        user.updateEmail("pedro@email.com");
        assertEquals("pedro@email.com", user.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNameToInvalid() {
        User user = User.create("Juan", "juan@email.com", "password123");
        assertThrows(IllegalArgumentException.class, () -> user.updateName(""));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingEmailToInvalid() {
        User user = User.create("Juan", "juan@email.com", "password123");
        assertThrows(IllegalArgumentException.class, () -> user.updateEmail("invalid"));
    }

    // ========== RECONSTRUCT ==========
    @Test
    void shouldReconstructUserFromDatabase() {
        User user = User.reconstruct(1L, "Juan", "juan@email.com", "hashed1123",
                infrastructure.adapter.out.persistence.user.UserEntity.Rol.USER,
                true, java.time.LocalDateTime.now(), java.time.LocalDateTime.now());

        assertEquals(1L, user.getId());
        assertEquals("Juan", user.getName());
        assertTrue(user.getIsActive());
    }

    @Test
    void shouldThrowExceptionWhenReconstructWithNullId() {
        assertThrows(IllegalArgumentException.class, () ->
                User.reconstruct(null, "Juan", "juan@email.com", "hashed",
                        infrastructure.adapter.out.persistence.user.UserEntity.Rol.USER,
                        true, java.time.LocalDateTime.now(), java.time.LocalDateTime.now()));
    }
}
