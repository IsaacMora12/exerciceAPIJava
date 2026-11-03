package domain.port.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {


    String upload(MultipartFile file, String directory);

    /**
     * Subida con clave determinista (J2): el objeto queda EXACTAMENTE en {@code key},
     * sin UUID. El seeder la usa con {@code images/<basename>} para que la URL
     * almacenada sea estable entre corridas; las subidas por API siguen usando
     * {@link #upload} (claves UUID).
     *
     * <p>Frontera J1: {@code IllegalArgumentException} (validación, clave inválida) =
     * data-level; cualquier otra falla del adapter = {@code StorageInfrastructureException}.
     */
    String uploadAt(MultipartFile file, String key);

    void delete(String fileKey);

    String getUrl(String fileKey);
}
