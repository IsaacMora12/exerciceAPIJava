package domain.port.storage;

import domain.validation.storage.ImageFileValidator;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ImageStorageService {
    private final FileStorageService delegate;
    private final ImageFileValidator imageFileValidator;

    public ImageStorageService(FileStorageService delegate, ImageFileValidator imageFileValidator) {
        this.delegate = delegate;
        this.imageFileValidator = imageFileValidator;
    }

    public String upload(MultipartFile file)
    {
        imageFileValidator.validate(file);
        return delegate.upload(file, "images");
    }

    /**
     * Subida con clave determinista (J2): la MISMA validación que por API, pero el
     * objeto queda exactamente en {@code key}. La usa el seeder con
     * {@code images/<basename>}.
     */
    public String uploadAs(MultipartFile file, String key)
    {
        imageFileValidator.validate(file);
        return delegate.uploadAt(file, key);
    }
}
