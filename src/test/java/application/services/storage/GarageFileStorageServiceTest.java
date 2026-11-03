package application.services.storage;

import domain.port.storage.StorageInfrastructureException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Garage adapter contract (design Decision 1 + 5, J1 matrix rows 1-5, SR4):
 * everything escaping uploadAt after validation is wrapped into
 * StorageInfrastructureException (fail-fast); getUrl returns a path-style public
 * URL or an offline-presigned URL with the configured expiry. Both are built on
 * garage.PUBLIC-endpoint (the host the client resolves) — never on the
 * operational garage.endpoint the S3Client talks to.
 */
class GarageFileStorageServiceTest {

    /** Host que resuelve el cliente; espeja el default de garage.public-endpoint. */
    private static final String PUBLIC_ENDPOINT = "http://localhost:3900";
    private static final String BUCKET = "exercise-app";

    private final S3Client s3 = mock(S3Client.class);

    private GarageFileStorageService service(String urlMode, long expiry) {
        return new GarageFileStorageService(s3, presigner(), BUCKET, urlMode, expiry, PUBLIC_ENDPOINT);
    }

    /** Real presigner, offline HMAC signing — no network involved. */
    private S3Presigner presigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(PUBLIC_ENDPOINT))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test-access", "test-secret")))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                // Espeja GarageConfig.GARAGE_REGION: la región debe ser la de s3_region.
                .region(Region.of("garage"))
                .build();
    }

    private final MockMultipartFile file =
            new MockMultipartFile("file", "5201-KOpzGBL.jpg", "image/jpeg", new byte[]{1, 2, 3});

    @Test
    void uploadAtForwardsTheExactDeterministicKeyToS3() {
        String key = service("public", 900).uploadAt(file, "images/5201-KOpzGBL.jpg");

        assertEquals("images/5201-KOpzGBL.jpg", key);
        verify(s3).putObject(
                org.mockito.ArgumentMatchers.argThat((PutObjectRequest r) ->
                        BUCKET.equals(r.bucket()) && "images/5201-KOpzGBL.jpg".equals(r.key())),
                any(RequestBody.class));
    }

    @Test
    void connectionFailureIsWrappedAsInfrastructure() {
        doThrow(SdkClientException.create("Connection refused"))
                .when(s3).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        StorageInfrastructureException ex = assertThrows(StorageInfrastructureException.class,
                () -> service("public", 900).uploadAt(file, "images/x.jpg"));
        assertTrue(ex.getCause() instanceof SdkClientException, "original SDK cause must be kept");
    }

    @Test
    void accessDenied403IsWrappedAsInfrastructure() {
        doThrow(S3Exception.builder().statusCode(403).message("AccessDenied").build())
                .when(s3).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        StorageInfrastructureException ex = assertThrows(StorageInfrastructureException.class,
                () -> service("public", 900).uploadAt(file, "images/x.jpg"));
        assertTrue(ex.getCause() instanceof S3Exception);
    }

    @Test
    void missingBucketIsWrappedAsInfrastructure() {
        doThrow(NoSuchBucketException.builder().message("NoSuchBucket").build())
                .when(s3).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        assertThrows(StorageInfrastructureException.class,
                () -> service("public", 900).uploadAt(file, "images/x.jpg"));
    }

    @Test
    void serviceSide503IsWrappedAsInfrastructure() {
        doThrow(S3Exception.builder().statusCode(503).message("SlowDown").build())
                .when(s3).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        assertThrows(StorageInfrastructureException.class,
                () -> service("public", 900).uploadAt(file, "images/x.jpg"));
    }

    @Test
    void unsafeKeyStaysDataLevelIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> service("public", 900).uploadAt(file, "a/../b.jpg"));
        assertThrows(IllegalArgumentException.class, () -> service("public", 900).uploadAt(file, null));
    }

    @Test
    void emptyFileStaysDataLevelIllegalArgument() {
        MockMultipartFile empty = new MockMultipartFile("file", "e.jpg", "image/jpeg", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> service("public", 900).uploadAt(empty, "images/e.jpg"));
    }

    @Test
    void getUrlPublicModeReturnsPathStyleAbsoluteUrl() {
        String url = service("public", 900).getUrl("images/5201-KOpzGBL.jpg");

        assertEquals(PUBLIC_ENDPOINT + "/" + BUCKET + "/images/5201-KOpzGBL.jpg", url);
        assertTrue(url.startsWith("http://"), url);
    }

    @Test
    void getUrlPublicModeStripsTrailingSlashFromEndpoint() {
        GarageFileStorageService trailing = new GarageFileStorageService(
                s3, presigner(), BUCKET, "public", 900, PUBLIC_ENDPOINT + "/");

        assertEquals(PUBLIC_ENDPOINT + "/" + BUCKET + "/images/a.jpg", trailing.getUrl("images/a.jpg"));
    }

    @Test
    void getUrlPresignedProducesSignedUrlWithConfiguredExpiry() {
        String url = service("presigned", 900).getUrl("images/5201-KOpzGBL.jpg");

        assertTrue(url.startsWith(PUBLIC_ENDPOINT + "/" + BUCKET + "/images/5201-KOpzGBL.jpg"), url);
        assertTrue(url.contains("X-Amz-Expires=900"), "SR4 documented default: 900s, got " + url);
        assertTrue(url.contains("X-Amz-Signature="), url);
        assertTrue(url.contains("X-Amz-Credential="), url);
        assertTrue(url.contains("X-Amz-SignedHeaders="), url);
        assertTrue(url.toLowerCase().contains("x-amz-algorithm="), url);
    }

    @Test
    void presignExpiryIsConfigurable() {
        String url = service("presigned", 60).getUrl("videos/5201-KOpzGBL.gif");

        assertTrue(url.contains("X-Amz-Expires=60"), url);
        assertFalse(url.contains("X-Amz-Expires=900"), url);
    }

    @Test
    void uploadKeepsUuidKeysForTheApiPath() {
        String key = service("public", 900).upload(file, "images");

        assertTrue(key.startsWith("images/"), key);
        String name = key.substring("images/".length());
        assertTrue(name.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}-5201-KOpzGBL\\.jpg"),
                "API path must keep UUID keys, got " + key);
    }
}
