package infrastructure.configuration;

import application.services.storage.LocalStorageRoot;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sirve el storage local por HTTP en {@code /media/**} (D1(b) read path).
 *
 * <p>Usa el MISMO objeto {@link LocalStorageRoot} que el adapter de escritura
 * inyecta: upload y serve nunca pueden divergir (design Decision 4 — SR5).
 * La propiedad NUNCA se re-resuelve aquí.
 *
 * <p>Solo activo en {@code dev}: en {@code garage} las URLs las firma/sirve
 * el object store y ningún handler se registra (el carve-out de
 * {@code SecurityConfig} queda inofensivo → 404).
 */
@Configuration
@Profile("dev")
public class LocalMediaConfig implements WebMvcConfigurer {

    private final LocalStorageRoot localStorageRoot;

    public LocalMediaConfig(LocalStorageRoot localStorageRoot) {
        this.localStorageRoot = localStorageRoot;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Trailing slash obligatorio: sin él Spring no resuelve el file: location.
        registry.addResourceHandler("/media/**")
                .addResourceLocations("file:" + localStorageRoot.path() + "/");
    }
}
