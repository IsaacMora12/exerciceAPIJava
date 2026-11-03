package com.example.exerciceapi.config;

import domain.port.storage.FileStorageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.multipart.MultipartFile;

@Configuration
public class TestStorageConfig {

    @Bean
    @Primary
    public FileStorageService fileStorageService() {
        return new FileStorageService() {
            @Override
            public String upload(MultipartFile file, String directory) {
                return "test://" + directory + "/" + file.getOriginalFilename();
            }

            /**
             * Doble de {@code uploadAt} (clave determinista J2).
             */
            @Override
            public String uploadAt(MultipartFile file, String key) {
                return "test://" + key;
            }

            @Override
            public void delete(String fileKey) {
                // no-op for tests
            }

            @Override
            public String getUrl(String fileKey) {
                return fileKey;
            }
        };
    }
}
