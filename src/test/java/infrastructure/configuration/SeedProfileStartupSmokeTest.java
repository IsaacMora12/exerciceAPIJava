package infrastructure.configuration;

import com.example.exerciceapi.ExerciceApiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.SpringApplication;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * SR6 smoke test (carry-over risk from Phase 4): proves the ProfileWiringValidator is
 * actually REGISTERED via META-INF/spring.factories and fires BEFORE bean creation.
 *
 * <p>Command-line profile activation replaces the application.properties default, so
 * {@code --spring.profiles.active=seed} yields the active set ["seed"] alone — exactly
 * the broken combination. The run must abort with OUR diagnostic and must NEVER reach
 * the refresh phase (no raw NoSuchBeanDefinitionException / BeanCreationException).
 *
 * <p>Guard rails in case the validator is ever lost: fixtures are empty and the web
 * server is disabled, so a mis-ordered startup cannot seed data nor bind a port.
 */
class SeedProfileStartupSmokeTest {

    @Test
    void seedAloneAbortsStartupWithActionableDiagnosticBeforeBeanCreation() {
        Throwable thrown = catchThrowable(() -> SpringApplication.run(
                ExerciceApiApplication.class,
                "--spring.profiles.active=seed",
                "--spring.main.web-application-type=none",
                "--seed.exercise-file=seedtest/empty.json",
                "--seed.muscle-file=seedtest/empty.json"));

        assertThat(thrown).as("seed alone must fail startup").isNotNull();

        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        StringBuilder all = new StringBuilder();
        for (Throwable t = thrown; t != null && seen.add(t); t = t.getCause()) {
            all.append(t).append('\n');
        }

        assertThat(all.toString())
                .as("actionable diagnostic must surface in the exception chain")
                .contains("Profile 'seed' requires an active storage profile")
                .contains("--spring.profiles.active=dev,seed")
                .contains("--spring.profiles.active=garage,seed");

        for (Throwable t = thrown; t != null; ) {
            assertThat(t)
                    .as("the validator must fire BEFORE bean creation — a raw missing-bean "
                            + "error would mean spring.factories registration or ordering broke")
                    .isNotInstanceOf(NoSuchBeanDefinitionException.class)
                    .isNotInstanceOf(BeanCreationException.class);
            Throwable cause = t.getCause();
            if (cause == t) {
                break;
            }
            t = cause;
        }
    }
}
