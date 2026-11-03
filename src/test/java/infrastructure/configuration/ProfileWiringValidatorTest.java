package infrastructure.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SR6 / design Decision 7: seed WITHOUT a storage profile fails with an actionable
 * message naming the legal profile combinations; every legal combination passes.
 */
class ProfileWiringValidatorTest {

    private final ProfileWiringValidator validator = new ProfileWiringValidator();

    private void initialize(MockEnvironment environment) {
        org.springframework.context.support.GenericApplicationContext context =
                new org.springframework.context.support.GenericApplicationContext();
        context.setEnvironment(environment);
        validator.initialize(context);
    }

    @Test
    void seedAloneThrowsWithActionableDiagnostic() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("seed");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> initialize(env));

        assertTrue(ex.getMessage().contains("Profile 'seed' requires an active storage profile"),
                ex.getMessage());
        assertTrue(ex.getMessage().contains("--spring.profiles.active=dev,seed"), ex.getMessage());
        assertTrue(ex.getMessage().contains("--spring.profiles.active=garage,seed"), ex.getMessage());
        assertTrue(ex.getMessage().toLowerCase().contains("replaces"), ex.getMessage());
    }

    @Test
    void devSeedPasses() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev", "seed");
        assertDoesNotThrow(() -> initialize(env));
    }

    @Test
    void garageSeedPasses() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("garage", "seed");
        assertDoesNotThrow(() -> initialize(env));
    }

    @Test
    void prodSeedPasses() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod", "seed");
        assertDoesNotThrow(() -> initialize(env));
    }

    @Test
    void noSeedProfilesPassUntouched() {
        MockEnvironment defaultEnv = new MockEnvironment();
        assertDoesNotThrow(() -> initialize(defaultEnv));

        MockEnvironment devOnly = new MockEnvironment();
        devOnly.setActiveProfiles("dev");
        assertDoesNotThrow(() -> initialize(devOnly));

        MockEnvironment testOnly = new MockEnvironment();
        testOnly.setActiveProfiles("test");
        assertDoesNotThrow(() -> initialize(testOnly));
    }
}
