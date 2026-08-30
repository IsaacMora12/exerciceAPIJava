package com.example.exerciceapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.exerciceapi", "application", "domain", "infrastructure"})
@EnableJpaRepositories(basePackages = {"infrastructure.adapter.out.persistence"})
@EntityScan(basePackages = {"infrastructure.adapter.out.persistence"})
public class ExerciceApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExerciceApiApplication.class, args);
    }

}
