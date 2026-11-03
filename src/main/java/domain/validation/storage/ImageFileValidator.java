package domain.validation.storage;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Component
public class ImageFileValidator {
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5mb
    public void validate(MultipartFile file)
    {
        if (!ALLOWED_TYPES.contains(file.getContentType()))
        {
            throw new IllegalArgumentException("Invalid image Type");
        }
        if (file.getSize() > MAX_SIZE )
        {
            throw new IllegalArgumentException("Invalid image Size");
        }
    }

}
