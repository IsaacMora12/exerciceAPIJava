package domain.port.storage;

import domain.validation.storage.VideoFileValidator;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class VideoStorageService {
    private final FileStorageService delegate;
    private final VideoFileValidator videoFileValidator;

    public VideoStorageService(FileStorageService delegate, VideoFileValidator videoFileValidator) {
        this.delegate = delegate;
        this.videoFileValidator = videoFileValidator;
    }

    public String upload(MultipartFile file) {
        videoFileValidator.validate(file);
        return delegate.upload(file, "videos");
    }
}
