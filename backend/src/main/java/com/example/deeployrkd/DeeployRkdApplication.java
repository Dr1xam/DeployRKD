package com.example.deeployrkd;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;

import java.io.File;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class
})
public class DeeployRkdApplication {

    public static void main(String[] args) {
        loadDotenv();
        SpringApplication.run(DeeployRkdApplication.class, args);
    }

    private static void loadDotenv() {
        try {
            // Check current directory, backend directory, and parent directory for .env
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            dotenv.entries().forEach(entry -> {
                if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                    System.setProperty(entry.getKey(), entry.getValue());
                }
            });

            // Also check parent directory if ran from backend folder
            File parentEnv = new File("../.env");
            if (parentEnv.exists()) {
                Dotenv parentDotenv = Dotenv.configure()
                        .directory("../")
                        .ignoreIfMissing()
                        .load();
                parentDotenv.entries().forEach(entry -> {
                    if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                        System.setProperty(entry.getKey(), entry.getValue());
                    }
                });
            }
        } catch (Exception ignored) {
        }
    }
}
