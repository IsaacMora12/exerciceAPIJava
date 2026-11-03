package application.services.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Decision 11: fixed contentType map (validators reject null contentType) and
 * unknown extension = data-level IllegalArgumentException (J1 matrix row 7),
 * NEVER an infrastructure failure.
 */
class ClasspathMultipartFileTest {

    @Test
    void knownExtensionsMapToFixedContentTypes() {
        Map<String, String> expected = Map.of(
                "photo.jpg", "image/jpeg",
                "photo.jpeg", "image/jpeg",
                "photo.JPG", "image/jpeg",
                "photo.png", "image/png",
                "photo.webp", "image/webp",
                "anim.gif", "image/gif",
                "clip.mp4", "video/mp4",
                "clip.webm", "video/webm",
                "clip.mov", "video/quicktime");
        expected.forEach((name, contentType) ->
                assertEquals(contentType, ClasspathMultipartFile.contentTypeFor(name),
                        "extension of " + name));
    }

    @Test
    void unknownExtensionIsDataLevelIllegalArgument() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ClasspathMultipartFile.contentTypeFor("payload.bmp"));
        assertTrue(ex.getMessage().contains("Unsupported media extension"));

        assertThrows(IllegalArgumentException.class,
                () -> ClasspathMultipartFile.contentTypeFor("noextension"));
        assertThrows(IllegalArgumentException.class,
                () -> ClasspathMultipartFile.contentTypeFor(""));
        assertThrows(IllegalArgumentException.class,
                () -> ClasspathMultipartFile.contentTypeFor(null));
    }

    @Test
    void constructorRejectsMissingFilenameAndNullBytes() {
        assertThrows(IllegalArgumentException.class, () -> new ClasspathMultipartFile(null, "image/jpeg", new byte[1]));
        assertThrows(IllegalArgumentException.class, () -> new ClasspathMultipartFile("  ", "image/jpeg", new byte[1]));
        assertThrows(IllegalArgumentException.class, () -> new ClasspathMultipartFile("a.jpg", "image/jpeg", null));
    }

    @Test
    void exposesBytesSizeAndContentTypeLikeAMultipartFile() {
        byte[] payload = {(byte) 0xFF, (byte) 0xD8, 0x01};
        ClasspathMultipartFile file = new ClasspathMultipartFile("a.jpg", "image/jpeg", payload);

        assertEquals("a.jpg", file.getName());
        assertEquals("a.jpg", file.getOriginalFilename());
        assertEquals("image/jpeg", file.getContentType());
        assertEquals(3, file.getSize());
        assertFalse(file.isEmpty());
        assertArrayEquals(payload, file.getBytes());
        try (InputStream in = file.getInputStream()) {
            assertArrayEquals(payload, in.readAllBytes());
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void emptyBytesAreReportedEmptyAndTransferToWritesFile(@TempDir Path tempDir) throws Exception {
        ClasspathMultipartFile empty = new ClasspathMultipartFile("empty.png", "image/png", new byte[0]);
        assertTrue(empty.isEmpty());

        ClasspathMultipartFile full = new ClasspathMultipartFile("a.png", "image/png", new byte[]{1, 2, 3});
        File dest = tempDir.resolve("out.png").toFile();
        full.transferTo(dest);
        assertArrayEquals(new byte[]{1, 2, 3}, Files.readAllBytes(dest.toPath()));
    }

    @Test
    void allKnownExtensionsAreCoveredByTheContract() {
        List<String> imageExts = List.of("jpg", "jpeg", "png", "webp");
        List<String> videoExts = List.of("gif", "mp4", "webm", "mov");
        for (String ext : imageExts) {
            assertTrue(ClasspathMultipartFile.contentTypeFor("x." + ext).startsWith("image/"), ext);
        }
        for (String ext : videoExts) {
            String ct = ClasspathMultipartFile.contentTypeFor("x." + ext);
            assertFalse(ct.isBlank(), ext);
        }
    }
}
