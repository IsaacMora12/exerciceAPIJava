package infrastructure.adapter.in.web;

import domain.port.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Resolution rule (design Decision 2 / spec media-delivery):
 * null/blank → passthrough, "://" → passthrough, anything else → storage.getUrl.
 */
class MediaUrlResolverTest {

    private static class RecordingStorage implements FileStorageService {
        final List<String> getUrlCalls = new java.util.ArrayList<>();

        @Override
        public String upload(MultipartFile file, String directory) {
            return directory + "/" + file.getOriginalFilename();
        }

        @Override
        public String uploadAt(MultipartFile file, String storageKey) {
            return storageKey;
        }

        @Override
        public void delete(String fileKey) {
        }

        @Override
        public String getUrl(String fileKey) {
            getUrlCalls.add(fileKey);
            return "/media/" + fileKey;
        }
    }

    private RecordingStorage storage;
    private MediaUrlResolver resolver;

    @BeforeEach
    void setUp() {
        storage = new RecordingStorage();
        resolver = new MediaUrlResolver(storage);
    }

    @Test
    void bareStorageKeyIsResolvedThroughTheActiveStorageBean() {
        assertEquals("/media/images/5201-KOpzGBL.jpg", resolver.toUrl("images/5201-KOpzGBL.jpg"));
        assertEquals(List.of("images/5201-KOpzGBL.jpg"), storage.getUrlCalls);
    }

    @Test
    void nullPassesThroughWithoutTouchingStorage() {
        assertNull(resolver.toUrl(null));
        assertTrue(storage.getUrlCalls.isEmpty());
    }

    @Test
    void blankPassesThroughWithoutTouchingStorage() {
        assertEquals("   ", resolver.toUrl("   "));
        assertTrue(storage.getUrlCalls.isEmpty());
    }

    @Test
    void absoluteLegacyUrlPassesThroughUnchanged() {
        assertEquals("https://example.com/chest.jpg", resolver.toUrl("https://example.com/chest.jpg"));
        assertTrue(storage.getUrlCalls.isEmpty());
    }

    @Test
    void nonHttpSchemesPassThroughUnchanged() {
        assertEquals("test://images/x.jpg", resolver.toUrl("test://images/x.jpg"));
        assertEquals("https://s3.local/bucket/k?X-Amz-Signature=abc",
                resolver.toUrl("https://s3.local/bucket/k?X-Amz-Signature=abc"));
        assertTrue(storage.getUrlCalls.isEmpty());
    }

    @Test
    void toUrlsNullListBecomesEmptyList() {
        assertEquals(List.of(), resolver.toUrls(null));
        assertTrue(storage.getUrlCalls.isEmpty());
    }

    @Test
    void toUrlsResolvesBareKeysAndKeepsAbsoluteUrlsVerbatim() {
        List<String> result = resolver.toUrls(
                List.of("images/a.jpg", "https://example.com/b.jpg", "test://c"));

        assertEquals(List.of("/media/images/a.jpg", "https://example.com/b.jpg", "test://c"), result);
        assertEquals(List.of("images/a.jpg"), storage.getUrlCalls);
    }
}
