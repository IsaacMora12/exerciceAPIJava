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
}
