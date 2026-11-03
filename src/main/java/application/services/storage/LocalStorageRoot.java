package application.services.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Raíz de almacenamiento local: la ÚNICA instancia con el {@code Path} absoluto resuelto
 * (design Decision 4 — SR5, independiente del CWD).
 *
 * <p>Se calcula UNA sola vez en el constructor ({@code toAbsolutePath().normalize()}) y lo
 * comparten {@code LocalFileStorageService} (escritura) y {@code LocalMediaConfig}
 * (servido por HTTP): upload y serve nunca pueden divergir.
 *
 * <p>La propiedad por defecto {@code ${user.home}/.exerciceapi/media} resuelve al mismo
 * path absoluto en cada arranque (IDE, mvn spring-boot:run, JAR empaquetado desde cualquier
 * CWD) y vive FUERA del repo — el seed local de ~131 MB nunca ensucia git ni entra al JAR.
 */
@Component
@Profile("dev")
public class LocalStorageRoot {

    private final Path root;

    public LocalStorageRoot(@Value("${storage.local.root:${user.home}/.exerciceapi/media}") String root) {
        this.root = Path.of(root).toAbsolutePath().normalize();
    }

    public Path path() {
        return root;
    }
}
