package infrastructure.adapter.in.web;

import domain.port.storage.FileStorageService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Resuelve claves de storage → URLs de cliente en la construcción de respuestas
 * (design Decision 2).
 *
 * <p>La DB guarda SIEMPRE claves ({@code images/<basename>}); la URL es una
 * preocupación de presentación dependiente del entorno, así que se resuelve
 * por respuesta (URLs presignadas frescas, sin cacheo en servidor).
 *
 * <p>Regla:
 * <ul>
 *   <li>{@code null} / blank → passthrough (sin contenido que resolver).</li>
 *   <li>contiene {@code "://"} → passthrough (filas legacy
 *       {@code https://example.com/...} sin reparar, URLs presignadas, valores
 *       {@code test://} del doble de tests).</li>
 *   <li>resto → {@link FileStorageService#getUrl} (cada adapter define su forma:
 *       local {@code /media/<key>}, garage pública/presignada).</li>
 * </ul>
 */
@Component
public class MediaUrlResolver {

    private final FileStorageService fileStorageService;

    public MediaUrlResolver(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public String toUrl(String storedValue) {
        if (storedValue == null || storedValue.isBlank()) {
            return storedValue;
        }
        if (storedValue.contains("://")) {
            return storedValue;
        }
        return fileStorageService.getUrl(storedValue);
    }

    public List<String> toUrls(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .map(this::toUrl)
                .toList();
    }
}
