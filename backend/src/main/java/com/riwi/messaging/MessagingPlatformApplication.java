package com.riwi.messaging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@SpringBootApplication
public class MessagingPlatformApplication {

    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(MessagingPlatformApplication.class, args);
    }

    private static void loadDotEnv() {
        try {
            Path envPath = Path.of(".env");
            if (!Files.exists(envPath)) {
                envPath = Path.of("../.env");
            }
            if (Files.exists(envPath)) {
                List<String> lines = Files.readAllLines(envPath);
                for (String line : lines) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
                        int eqIdx = line.indexOf('=');
                        String key = line.substring(0, eqIdx).trim();
                        String value = line.substring(eqIdx + 1).trim();
                        if (System.getProperty(key) == null && System.getenv(key) == null) {
                            System.setProperty(key, value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not load .env file: " + e.getMessage());
        }
    }
}
