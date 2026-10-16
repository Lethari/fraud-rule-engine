package com.fraud.engine.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.fraud.engine")
public class FraudEngineApp {
    public static void main(String[] args) {
        SpringApplication.run(FraudEngineApp.class, args);
    }
}
