package application.services.storage;

import domain.port.storage.FileStorageService;
import domain.port.storage.StorageInfrastructureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

/**
 * Implementación de FileStorageService usando Garage (S3-compatible).
 * Activa con el perfil "garage" o "prod".
 *
 * <p>Frontera J1: la validación ({@code IllegalArgumentException}) se ejecuta ANTES del
 * try y NUNCA se envuelve (data-level, el seeder la cuenta por registro). Todo lo que
 * escape de {@code upload/uploadAt/delete/getUrl} después de validar — endpoint
 * inalcanzable, credenciales rechazadas, bucket inexistente — es infraestructura →
 * {@link StorageInfrastructureException} (fail-fast).
 */
@Service
@Profile({"garage", "prod"})
public class GarageFileStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(GarageFileStorageService.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;
    private final String urlMode;
    private final long presignExpirySeconds;
    /** Host que ven los clientes en las URLs — NO el endpoint operativo del S3Client. */
    private final String publicEndpoint;

    public GarageFileStorageService(S3Client s3Client,
                                    S3Presigner s3Presigner,
                                    @Value("${garage.bucket}") String bucket,
                                    // Fallback = default de application.properties: presigned
                                    // (Garage v2.x no acepta anónimo en el endpoint S3).
                                    @Value("${garage.url-mode:presigned}") String urlMode,
                                    @Value("${garage.presign-expiry-seconds:900}") long presignExpirySeconds,
                                    @Value("${garage.public-endpoint}") String publicEndpoint) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
        this.urlMode = urlMode;
        this.presignExpirySeconds = presignExpirySeconds;
        this.publicEndpoint = publicEndpoint;
    }

    @Override
    public String upload(MultipartFile file, String directory) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String originalFilename = sanitizeFilename(file.getOriginalFilename());
        String key = directory + "/" + UUID.randomUUID() + "-" + originalFilename;
        return uploadAt(file, key);
    }

    /**
     * Subida con clave determinista (J2: el seeder usa {@code images/<basename>}).
     */
    @Override
    public String uploadAt(MultipartFile file, String key) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String safeKey = requireSafeKey(key);

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(safeKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("File uploaded to Garage: bucket={}, key={}", bucket, safeKey);
            return safeKey;

        } catch (StorageInfrastructureException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new StorageInfrastructureException("Failed to upload file to Garage: " + safeKey, e);
        }
    }

    @Override
    public void delete(String fileKey) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileKey)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("File deleted from Garage: bucket={}, key={}", bucket, fileKey);
        } catch (StorageInfrastructureException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new StorageInfrastructureException(
                    "Failed to delete file from Garage: bucket=" + bucket + ", key=" + fileKey, e);
        }
    }

    @Override
    public String getUrl(String fileKey) {
        // D1(b): antes retornaba "bucket/key" (no resolvible). Ahora:
        // presigned → URL firmada HMAC local (sin red). public → URL path-style
        // directa, que Garage v2.x RECHAZA sin firma (403 "does not support
        // anonymous access yet"): solo sirve contra S3/MinIO con anónimo habilitado.
        if ("presigned".equalsIgnoreCase(urlMode)) {
            try {
                GetObjectRequest getRequest = GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(fileKey)
                        .build();
                GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofSeconds(presignExpirySeconds))
                        .getObjectRequest(getRequest)
                        .build();
                return s3Presigner.presignGetObject(presignRequest).url().toString();
            } catch (StorageInfrastructureException | IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                throw new StorageInfrastructureException(
                        "Failed to presign URL from Garage: bucket=" + bucket + ", key=" + fileKey, e);
            }
        }
        // public → URL path-style sobre el endpoint PÚBLICO (la baja el cliente).
        String base = publicEndpoint.endsWith("/")
                ? publicEndpoint.substring(0, publicEndpoint.length() - 1)
                : publicEndpoint;
        return base + "/" + bucket + "/" + fileKey;
    }

    /**
     * Limpia el nombre del archivo para prevenir path traversal.
     * Solo permite el nombre base, sin directorios.
     */
    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Filename must not be empty");
        }
        // Toma solo el nombre base (elimina path traversal como ../../etc/passwd)
        return java.nio.file.Path.of(filename).getFileName().toString();
    }

    /**
     * Clave determinista válida: no vacía, relativa, sin {@code ..} (G7: sin espacios
     * ni comas — el seeder ya normaliza el basename; esto es la última defensa).
     * Data-level → {@code IllegalArgumentException}, nunca envuelta.
     */
    private String requireSafeKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key must not be empty");
        }
        if (key.startsWith("/") || key.contains("..") || key.contains("\\")) {
            throw new IllegalArgumentException("Key must be a relative path without traversals: " + key);
        }
        return key;
    }
}
