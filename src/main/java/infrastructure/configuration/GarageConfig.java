package infrastructure.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * Configuración de S3 para Garage (S3-compatible).
 * Solo se activa con los perfiles "garage" o "prod".
 *
 * <p>Dos hosts distintos, dos beans distintos:
 * <ul>
 *   <li>{@code garage.endpoint} → el S3Client OPERATIVO (sube/borra objetos) habla
 *       con Garage desde donde corra la app (en Docker: http://garage:3900, red de
 *       compose).</li>
 *   <li>{@code garage.public-endpoint} → el presigner firma URLs que baja el
 *       BROWSER, así que el host debe ser resoluble desde la máquina del cliente
 *       (en Docker: localhost:3900).</li>
 * </ul>
 */
@Configuration
@Profile({"garage", "prod"})
public class GarageConfig {

    /**
     * La región DEBE coincidir con s3_region de garage.toml ("garage"):
     * Garage valida el scope SigV4 y rechaza cualquier otro valor con
     * 400 AuthorizationHeaderMalformed.
     */
    private static final Region GARAGE_REGION = Region.of("garage");

    /**
     * S3Client operativo: único canal de escritura/lectura contra Garage.
     * endpoint = {@code garage.endpoint} (lado servidor).
     */
    @Bean
    public S3Client s3Client(
            @Value("${garage.endpoint}") String endpoint,
            @Value("${garage.access-key}") String accessKey,
            @Value("${garage.secret-key}") String secretKey) {

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(credentialsProvider(accessKey, secretKey))
                .forcePathStyle(true)  // Requerido para Garage/MinIO
                .region(GARAGE_REGION)
                // aws-chunked por defecto rompe contra Garage: el SDK firma los
                // chunks (x-amz-content-sha256: STREAMING-AWS4-HMAC-SHA256-PAYLOAD)
                // y Garage v2.3.0 responde 400 "Invalid payload signature"
                // (src/api/common/signature/streaming.rs,
                // StreamingPayloadError::InvalidSignature cuando
                // header.signature != expected_signature). Sin chunked encoding el
                // body viaja directo con x-amz-content-sha256 que Garage valida.
                // Mismo workaround que R2 y otros S3-compatible (chunkedEncodingEnabled(false)).
                .serviceConfiguration(S3Configuration.builder()
                        .chunkedEncodingEnabled(false)
                        .build())
                // WHEN_SUPPORTED (default) agrega checksums CRC32 que el SDK manda
                // como trailer de aws-chunked; WHEN_REQUIRED evita extra que Garage
                // valida con semántica propia.
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .build();
    }

    /**
     * Presigner para getUrl() en modo {@code garage.url-mode=presigned}.
     * El firmado es HMAC local — sin llamada de red por respuesta.
     *
     * <p>Usa {@code garage.public-endpoint}: la URL firmada la consume el cliente,
     * no el servidor. Firmar contra el endpoint operativo (p.ej.
     * http://garage:3900, red de compose) produciría URLs que el browser no puede
     * resolver.
     *
     * <p>DEBE espejar {@code forcePathStyle(true)} del S3Client con
     * {@code pathStyleAccessEnabled(true)}: si no, la firma presignada se calcula contra
     * el host virtual-style y Garage/MinIO la rechaza (403 SignatureDoesNotMatch).
     */
    @Bean
    public S3Presigner s3Presigner(
            @Value("${garage.public-endpoint}") String publicEndpoint,
            @Value("${garage.access-key}") String accessKey,
            @Value("${garage.secret-key}") String secretKey) {

        return S3Presigner.builder()
                .endpointOverride(URI.create(publicEndpoint))
                .credentialsProvider(credentialsProvider(accessKey, secretKey))
                .serviceConfiguration(pathStyleAccess())
                .region(GARAGE_REGION)
                .build();
    }

    private StaticCredentialsProvider credentialsProvider(String accessKey, String secretKey) {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
    }

    private S3Configuration pathStyleAccess() {
        return S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();
    }
}
