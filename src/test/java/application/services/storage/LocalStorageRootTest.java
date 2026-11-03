package application.services.storage;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SR5 / design Decision 4: one resolved absolute normalized Path, stable for the
 * same input, computed once in the constructor.
 */
class LocalStorageRootTest {

    @Test
    void relativeInputIsResolvedToAbsolutePath() {
        Path path = new LocalStorageRoot("some/relative/media").path();

        assertTrue(path.isAbsolute(), "root must be absolute, got " + path);
        assertTrue(path.toString().endsWith("some/relative/media"), path.toString());
    }

    @Test
    void pathIsNormalizedAndIdempotent() {
        LocalStorageRoot root = new LocalStorageRoot("some/../relative/./media");

        assertTrue(root.path().toString().endsWith("relative/media"), root.path().toString());
        assertTrue(root.path().toString().contains("some/../") == false);
        assertEquals(root.path(), root.path(), "path() must be stable across calls");
    }

    @Test
    void sameInputProducesEqualPaths() {
        assertEquals(new LocalStorageRoot("/tmp/a/b").path(), new LocalStorageRoot("/tmp/a/b").path());
    }

    @Test
    void absoluteInputStaysAbsolute() {
        assertEquals(Path.of("/tmp/exercice-media").toAbsolutePath().normalize(),
                new LocalStorageRoot("/tmp/exercice-media").path());
    }
}
