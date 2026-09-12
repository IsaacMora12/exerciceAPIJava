package domain.validation.storage;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Component
public class VideoFileValidator {
    // Por ahora soportamos GIF (imagen animada) como "video"
    // En el futuro se agregan mp4, webm, mov
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/gif",
            "video/mp4",
            "video/webm",
            "video/quicktime"
    );
    private static final long MAX_SIZE = 10 * 1024 * 1024; // 50MB

    public void validate(MultipartFile file) {
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException(
                    "Invalid video type. Allowed: gif, mp4, webm, mov");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException(
                    "Video too large. Maximum size: 50MB");
        }
    }
}
