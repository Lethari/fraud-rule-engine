package com.fraud.engine.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.fraud.engine")
@EnableJpaRepositories(basePackages = "com.fraud.engine.repository")
@EntityScan(basePackages = "com.fraud.engine.model")
public class FraudEngineApp {
    public static void main(String[] args) {
        SpringApplication.run(FraudEngineApp.class, args);
    }
}