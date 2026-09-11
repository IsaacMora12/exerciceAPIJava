package application.services.storage;

import domain.port.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

/**
 * Implementación de FileStorageService usando Garage (S3-compatible).
 * Activa con el perfil "garage" o "prod".
 */
@Service
@Profile({"garage", "prod"})
public class GarageFileStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(GarageFileStorageService.class);

    private final S3Client s3Client;
    private final String bucket;

    public GarageFileStorageService(S3Client s3Client,
                                    @Value("${garage.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    @Override
    public String upload(MultipartFile file, String directory) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String originalFilename = sanitizeFilename(file.getOriginalFilename());
        String key = directory + "/" + UUID.randomUUID() + "-" + originalFilename;

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("File uploaded to Garage: bucket={}, key={}", bucket, key);
            return key;

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Garage: " + key, e);
        }
    }

    @Override
    public void delete(String fileKey) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .build();

        s3Client.deleteObject(deleteRequest);
        log.info("File deleted from Garage: bucket={}, key={}", bucket, fileKey);
    }

    @Override
    public String getUrl(String fileKey) {
        // Para Garage con acceso público, retorna la URL directa.
        // Si necesitás presigned URLs, usar s3Client.utilities().presign()
        return bucket + "/" + fileKey;
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
}
