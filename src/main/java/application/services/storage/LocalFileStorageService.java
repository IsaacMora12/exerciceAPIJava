package application.services.storage;

import domain.port.storage.FileStorageService;
import domain.port.storage.StorageInfrastructureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Implementación de FileStorageService para desarrollo local.
 * Guarda archivos bajo {@link LocalStorageRoot} (resuelto absoluto UNA vez,
 * independiente del CWD) y los expone en {@code /media/**}.
 * Activa con el perfil "dev".
 *
 * <p>Frontera J1: la validación ({@code IllegalArgumentException}) se ejecuta ANTES del
 * try y NUNCA se envuelve (data-level, el seeder la cuenta por registro). Todo lo que
 * escape de {@code upload/uploadAt/delete} después de validar es I/O de disco →
 * {@link StorageInfrastructureException} (fail-fast).
 */
@Service
@Profile("dev")
public class LocalFileStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageService.class);

    private final LocalStorageRoot root;

    public LocalFileStorageService(LocalStorageRoot root) {
        this.root = root;
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
        Path fullPath = root.path().resolve(safeKey).normalize();
        if (!fullPath.startsWith(root.path())) {
            throw new IllegalArgumentException("Key escapes storage root: " + key);
        }

        try {
            Files.createDirectories(fullPath.getParent());
            Files.write(fullPath, file.getBytes());

            log.info("File saved locally: {}", fullPath);
            return safeKey;

        } catch (StorageInfrastructureException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new StorageInfrastructureException("Failed to save file locally: " + fullPath, e);
        }
    }

    @Override
    public void delete(String fileKey) {
        Path fullPath = root.path().resolve(fileKey).normalize();
        try {
            Files.deleteIfExists(fullPath);
            log.info("File deleted locally: {}", fullPath);
        } catch (StorageInfrastructureException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new StorageInfrastructureException("Failed to delete file: " + fullPath, e);
        }
    }

    @Override
    public String getUrl(String fileKey) {
        // D1(b): la app sirve el root en /media/** — el cliente nunca ve rutas de disco.
        return "/media/" + fileKey;
    }

    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Filename must not be empty");
        }
        return Path.of(filename).getFileName().toString();
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
