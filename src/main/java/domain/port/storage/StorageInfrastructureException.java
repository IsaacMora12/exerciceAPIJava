package domain.port.storage;

/**
 * Marker de falla de INFRAESTRUCTURA (endpoint inalcanzable, credenciales rechazadas,
 * bucket inexistente, fallo de servicio, I/O de disco local).
 *
 * <p>La lanzan ÚNICAMENTE los adapters ({@code LocalFileStorageService},
 * {@code GarageFileStorageService}) en la frontera de clasificación J1: todo lo que
 * escapa de {@code upload/uploadAt/delete} después de la validación es infraestructura
 * por construcción. El seeder NUNCA inspecciona tipos de excepciones del SDK (hexagonal:
 * la capa application no conoce AWS) — solo distingue esta marca de los errores
 * {@code IllegalArgumentException} de validación, que son data-level y se cuentan por registro.
 *
 * <p>Comportamiento: se propaga fuera de {@code CommandLineRunner.run()} → la aplicación
 * aborta con UN solo diagnóstico (fail-fast), sin tormenta de 1318 stacktraces.
 */
public class StorageInfrastructureException extends RuntimeException {

    public StorageInfrastructureException(String message) {
        super(message);
    }

    public StorageInfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }
}
