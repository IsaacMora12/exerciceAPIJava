package infrastructure.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * Configuración del S3Client para Garage (S3-compatible).
 * Solo se activa con los perfiles "garage" o "prod".
 */
@Configuration
@Profile({"garage", "prod"})
public class GarageConfig {

    @Bean
    public S3Client s3Client(
            @Value("${garage.endpoint}") String endpoint,
            @Value("${garage.access-key}") String accessKey,
            @Value("${garage.secret-key}") String secretKey) {

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .forcePathStyle(true)  // Requerido para Garage/MinIO
                .region(Region.US_EAST_1)  // Garage no valida región, pero el SDK la requiere
                .build();
    }
}
