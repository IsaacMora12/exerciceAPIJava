package domain.port.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {


    String upload(MultipartFile file, String directory);

    void delete(String fileKey);

    String getUrl(String fileKey);
}
