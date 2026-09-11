package com.example.exerciceapi.application.services.users;

import application.services.users.CreateUserService;
import application.services.users.UpdateUserService;
import domain.model.user.User;
import domain.port.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UpdateUserServiceTest {

    @Autowired private UpdateUserService updateUserService;
    @Autowired private CreateUserService createUserService;
    @Autowired private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = createUserService.createUser("Original", "original@test.com", "password123", null);
    }

    // ========== SUCCESS CASES ==========
    @Test
    void shouldUpdateUserName() {
        User updated = updateUserService.updateUser(user.getId(), "New Name", null, null, null, null);

        assertEquals("New Name", updated.getName());
    }

    @Test
    void shouldUpdateUserEmail() {
        User updated = updateUserService.updateUser(user.getId(), null, "new@email.com", null, null, null);

        assertEquals("new@email.com", updated.getEmail());
    }

    @Test
    void shouldUpdateUserPassword() {
        User updated = updateUserService.updateUser(user.getId(), null, null, "newPassword123", null, null);

        assertNotEquals("newPassword123", updated.getPassword(), "Password should be hashed");
        assertTrue(updated.getPassword().length() > "newPassword123".length());
    }

    @Test
    void shouldActivateUser() {
        User deactivated = updateUserService.updateUser(user.getId(), null, null, null, false, null);
        assertFalse(deactivated.getIsActive());

        User activated = updateUserService.updateUser(user.getId(), null, null, null, true, null);
        assertTrue(activated.getIsActive());
    }

    @Test
    void shouldDeactivateUser() {
        User updated = updateUserService.updateUser(user.getId(), null, null, null, false, null);

        assertFalse(updated.getIsActive());
    }

    @Test
    void shouldUpdateMultipleFields() {
        User updated = updateUserService.updateUser(user.getId(), "New Name", "new@email.com", "newPass123", false, null);

        assertEquals("New Name", updated.getName());
        assertEquals("new@email.com", updated.getEmail());
        assertNotEquals("newPass123", updated.getPassword());
        assertFalse(updated.getIsActive());
    }

    @Test
    void shouldNotUpdateNullFields() {
        User updated = updateUserService.updateUser(user.getId(), null, null, null, null, null);

        assertEquals("Original", updated.getName());
        assertEquals("original@test.com", updated.getEmail());
    }

    // ========== VALIDATIONS ==========
    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                updateUserService.updateUser(999999L, "New Name", null, null, null, null));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsInvalid() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                updateUserService.updateUser(user.getId(), null, "invalid-email", null, null, null));

        assertEquals("This email address is invalid", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsTooShort() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                updateUserService.updateUser(user.getId(), null, null, "short", null, null));

        assertEquals("The password must be at least 8 characters", exception.getMessage());
    }

    // ========== TIMESTAMP ==========
    @Test
    void shouldUpdateTimestampOnModification() throws InterruptedException {
        Thread.sleep(10);
        User updated = updateUserService.updateUser(user.getId(), "New Name", null, null, null, null);

        assertTrue(updated.getUpdatedAt().isAfter(user.getCreatedAt()));
    }
}
