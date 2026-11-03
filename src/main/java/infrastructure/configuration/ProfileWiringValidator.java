package infrastructure.configuration;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

/**
 * SR6 / design Decision 7: diagnóstico de perfil ANTES del refresh.
 *
 * <p>{@code ImageStorageService}/{@code VideoStorageService} son {@code @Component}
 * incondicionales que exigen un {@code FileStorageService}: con el perfil {@code seed}
 * solo, el context refresh moriría con un {@code NoSuchBeanDefinitionException} crudo
 * antes de que ningún check a nivel de bean pudiera correr. Un initializer corre después
 * de preparar el environment (el {@code spring.profiles.active=dev} de
 * {@code application.properties} ya es visible) y antes de la creación de beans → este
 * mensaje reemplaza al error crudo.
 *
 * <p>Registrado declarativamente en {@code META-INF/spring.factories} (mecanismo
 * documentado; verificado contra el bytecode de Spring Boot 4.1.0: el constructor de
 * {@code SpringApplication} llama {@code getSpringFactoriesInstances(ApplicationContextInitializer.class)}
 * y {@code SpringFactoriesLoader.forDefaultResourceLocation} lee {@code META-INF/spring.factories}).
 *
 * <p>Riesgo carry-over conocido: necesita un smoke test (Phase 5) que ejecute la app con
 * {@code --spring.profiles.active=seed} y afirme el mensaje.
 */
public class ProfileWiringValidator implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    /** Perfiles que proveen un {@code FileStorageService} (dev → local, garage/prod → S3). */
    private static final String[] STORAGE_PROFILES = {"dev", "garage", "prod"};

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Environment environment = applicationContext.getEnvironment();

        boolean seedActive = false;
        boolean storageActive = false;
        for (String profile : environment.getActiveProfiles()) {
            if ("seed".equals(profile)) {
                seedActive = true;
            }
            for (String storageProfile : STORAGE_PROFILES) {
                if (storageProfile.equals(profile)) {
                    storageActive = true;
                }
            }
        }

        if (seedActive && !storageActive) {
            throw new IllegalStateException(
                    "Profile 'seed' requires an active storage profile. "
                            + "Use --spring.profiles.active=dev,seed (local storage) or "
                            + "--spring.profiles.active=garage,seed (S3/Garage). "
                            + "Note: setting spring.profiles.active on the command line REPLACES "
                            + "the default profile from application.properties — specify the full set.");
        }
    }
}
