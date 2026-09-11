package domain.port.storage.in;

import org.springframework.web.multipart.MultipartFile;

public interface StorageUploadUseCase {
    String upload(MultipartFile file, String directory);
}
