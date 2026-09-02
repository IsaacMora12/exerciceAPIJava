package com.example.exerciceapi.application.services.users;

import application.services.users.CreateUserService;
import domain.model.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CreateUserServiceTest {

    @Autowired
    private CreateUserService createUserService;

    @Test
    void shouldCreateUserSuccessfully() {
        // Arrange
        String name = "Juan";
        String email = "juan123@emailTest.com";
        String password = "test12353";

        // Act
        User user = createUserService.createUser(name, email, password);

        // Assert
        assertNotNull(user.getId());
        assertEquals(name, user.getName());
        assertNotEquals(password, user.getPassword(), "Password should be hashed");
        assertTrue(user.getIsActive());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void shouldThrowExceptionWhenEmailDuplicated() {
        // Arrange
        String name = "Juan";
        String email = "juanDuplicate@emailTest.com";
        String password = "test12353";

        // Act - First creation should work
        createUserService.createUser(name, email, password);

        // Act & Assert - Second creation with same email should fail
        assertThrows(IllegalArgumentException.class, () -> {
            createUserService.createUser(name, email, password);
        });
    }

    @Test
    void shouldThrowExceptionWhenEmailInvalid() {
        // Arrange
        String name = "Juan";
        String email = "invalid-email";
        String password = "test12353";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            createUserService.createUser(name, email, password);
        });
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsNull() {
        // Arrange
        String name = "Juan";
        String email = "juanNull@emailTest.com";
        String password = null;

        // Act & Assert
        assertThrows(Exception.class, () -> {
            createUserService.createUser(name, email, password);
        });
    }

    @Test
    void shouldThrowExceptionWhenNameEmpty() {
        // Arrange
        String name = "";
        String email = "juanEmpty@emailTest.com";
        String password = "test12353";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            createUserService.createUser(name, email, password);
        });
    }

    @Test
    void shouldSetUserActiveByDefault() {
        // Arrange
        String name = "Active User";
        String email = "active@emailTest.com";
        String password = "test12353";

        // Act
        User user = createUserService.createUser(name, email, password);

        // Assert
        assertTrue(user.getIsActive());
    }

    @Test
    void shouldHashPassword() {
        // Arrange
        String name = "Hash Test";
        String email = "hash@emailTest.com";
        String rawPassword = "myPassword123";

        // Act
        User user = createUserService.createUser(name, email, rawPassword);

        // Assert
        assertNotEquals(rawPassword, user.getPassword(), "Password should be hashed");
        assertNotNull(user.getPassword());
        assertTrue(user.getPassword().length() > rawPassword.length(), "Hashed password should be longer");
    }
}
