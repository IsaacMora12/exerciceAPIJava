package application.services.storage;

import domain.port.storage.StorageInfrastructureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Local adapter contract (D1(b) + J1 matrix rows 6/8/10):
 * uploadAt uses the EXACT caller key (J2), traversal keys are data-level
 * IllegalArgumentException, I/O failures are StorageInfrastructureException,
 * getUrl returns the served /media/ path.
 */
class LocalFileStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalFileStorageService service;
    private MockMultipartFile file;

    @BeforeEach
    void setUp() {
        service = new LocalFileStorageService(new LocalStorageRoot(tempDir.toString()));
        file = new MockMultipartFile("file", "5201-KOpzGBL.jpg", "image/jpeg", new byte[]{1, 2, 3, 4});
    }

    @Test
    void uploadAtPersistsUnderTheExactDeterministicKey() throws Exception {
        String key = service.uploadAt(file, "images/5201-KOpzGBL.jpg");

        assertEquals("images/5201-KOpzGBL.jpg", key);
        Path written = tempDir.resolve("images/5201-KOpzGBL.jpg");
        assertTrue(Files.exists(written));
        assertArrayEquals(new byte[]{1, 2, 3, 4}, Files.readAllBytes(written));
    }

    @Test
    void uploadAtOverwritesInPlaceInsteadOfCreatingNewObjects() throws Exception {
        service.uploadAt(file, "images/same.jpg");
        MockMultipartFile replacement =
                new MockMultipartFile("file", "same.jpg", "image/jpeg", new byte[]{9, 9});
        service.uploadAt(replacement, "images/same.jpg");

        assertEquals(1, Files.list(tempDir.resolve("images")).count());
        assertArrayEquals(new byte[]{9, 9}, Files.readAllBytes(tempDir.resolve("images/same.jpg")));
    }

    @Test
    void uploadKeepsUuidKeysForTheApiPath() throws Exception {
        String key = service.upload(file, "images");

        assertTrue(key.startsWith("images/"), key);
        assertTrue(key.endsWith("5201-KOpzGBL.jpg"), key);
        assertTrue(Files.exists(tempDir.resolve(key)));
    }

    @Test
    void traversalKeysAreDataLevelRejections() {
        assertThrows(IllegalArgumentException.class, () -> service.uploadAt(file, "../evil.jpg"));
        assertThrows(IllegalArgumentException.class, () -> service.uploadAt(file, "images/../../evil.jpg"));
        assertThrows(IllegalArgumentException.class, () -> service.uploadAt(file, "/absolute/evil.jpg"));
        assertThrows(IllegalArgumentException.class, () -> service.uploadAt(file, "images\\evil.jpg"));
        assertThrows(IllegalArgumentException.class, () -> service.uploadAt(file, " "));
        assertThrows(IllegalArgumentException.class, () -> service.uploadAt(file, null));
    }

    @Test
    void emptyFileIsDataLevelRejectionNotInfra() {
        MockMultipartFile empty = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> service.uploadAt(empty, "images/empty.jpg"));
    }

    @Test
    void ioFailureIsWrappedAsStorageInfrastructureException(@TempDir Path fileInsteadOfDir) throws Exception {
        Path blocked = fileInsteadOfDir.resolve("root-is-a-file");
        Files.write(blocked, new byte[]{0});
        LocalFileStorageService broken = new LocalFileStorageService(new LocalStorageRoot(blocked.toString()));

        StorageInfrastructureException ex = assertThrows(StorageInfrastructureException.class,
                () -> broken.uploadAt(file, "images/x.jpg"));
        assertTrue(ex.getCause() != null, "infra failures must keep their cause");
    }

    @Test
    void getUrlReturnsApplicationServedMediaPath() {
        assertEquals("/media/images/5201-KOpzGBL.jpg", service.getUrl("images/5201-KOpzGBL.jpg"));
        assertEquals("/media/videos/5201-KOpzGBL.gif", service.getUrl("videos/5201-KOpzGBL.gif"));
    }
}
