package application.services.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/**
 * Puente {@code Resource de classpath → MultipartFile} para que el seeder pueda
 * alimentar la MISMA cadena de validación/almacenamiento que subida por API.
 *
 * <p>Los bytes viven en memoria (el asset más pesado del set de seed pesa ~233 KB y los
 * validadores techo-an en 5/10 MB — acotado por construcción).
 * {@code MockMultipartFile} es test-scoped, por eso existe esta clase en src/main.
 *
 * <p>El contentType NUNCA es null: los validadores rechazan null contentType
 * (design Decision 11), así que se deriva de la extensión con un mapa fijo.
 * Extensión desconocida → {@code IllegalArgumentException} (data-level, matriz J1 fila 7).
 */
public class ClasspathMultipartFile implements MultipartFile {

    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp",
            "gif", "image/gif",
            "mp4", "video/mp4",
            "webm", "video/webm",
            "mov", "video/quicktime"
    );

    private final String filename;
    private final String contentType;
    private final byte[] bytes;

    public ClasspathMultipartFile(String filename, String contentType, byte[] bytes) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Filename must not be empty");
        }
        if (bytes == null) {
            throw new IllegalArgumentException("Bytes must not be null");
        }
        this.filename = filename;
        this.contentType = contentType;
        this.bytes = bytes;
    }

    /**
     * Deriva el contentType a partir de la extensión del archivo (mapa fijo, sin red).
     *
     * @throws IllegalArgumentException si la extensión no está soportada (data-level)
     */
    public static String contentTypeFor(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Filename must not be empty");
        }
        String normalized = filename.toLowerCase(Locale.ROOT);
        int dot = normalized.lastIndexOf('.');
        String extension = dot >= 0 ? normalized.substring(dot + 1) : "";
        String contentType = CONTENT_TYPES.get(extension);
        if (contentType == null) {
            throw new IllegalArgumentException("Unsupported media extension: " + extension);
        }
        return contentType;
    }

    @Override
    public String getName() {
        return filename;
    }

    @Override
    public String getOriginalFilename() {
        return filename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return bytes.length == 0;
    }

    @Override
    public long getSize() {
        return bytes.length;
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        Files.write(dest.toPath(), bytes);
    }
}
