package application.services.storage;

import domain.port.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Implementación de FileStorageService para desarrollo local.
 * Guarda archivos en src/main/resources/files/{directory}/
 * Activa con el perfil "dev".
 */
@Service
@Profile("dev")
public class LocalFileStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageService.class);
    private static final String BASE_DIR = "src/main/resources/files/";

    @Override
    public String upload(MultipartFile file, String directory) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String originalFilename = sanitizeFilename(file.getOriginalFilename());
        String key = directory + "/" + UUID.randomUUID() + "-" + originalFilename;
        Path fullPath = Path.of(BASE_DIR, key);

        try {
            Files.createDirectories(fullPath.getParent());
            Files.write(fullPath, file.getBytes());

            log.info("File saved locally: {}", fullPath);
            return key;

        } catch (IOException e) {
            throw new RuntimeException("Failed to save file locally: " + fullPath, e);
        }
    }

    @Override
    public void delete(String fileKey) {
        Path fullPath = Path.of(BASE_DIR, fileKey);
        try {
            Files.deleteIfExists(fullPath);
            log.info("File deleted locally: {}", fullPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + fullPath, e);
        }
    }

    @Override
    public String getUrl(String fileKey) {
        // En local, retorna la ruta relativa (no es una URL real)
        return BASE_DIR + fileKey;
    }

    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Filename must not be empty");
        }
        return Path.of(filename).getFileName().toString();
    }
}
