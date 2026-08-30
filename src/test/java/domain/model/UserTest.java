package domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUserWithValidData() {
        String name = "Juan";
        String email = "juan123@emailTest.com";
        String password = "test12353";

        User user = User.create(name, email, password);

        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertTrue(user.getIsActive());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            User.create("juan", "no-Email", "12345678");
        });
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            User.create("juan", "juan123@emailtest.com", "");
        });
    }
}
