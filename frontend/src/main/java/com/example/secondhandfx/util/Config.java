package com.example.secondhandfx.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Config {

    private static final String DEFAULT_API_BASE_URL = "https://secondhand-6kfg.onrender.com";

    private static final String API_BASE_URL = loadApiBaseUrl();

    private Config() {
    }

    public static String getApiBaseUrl() {
        return API_BASE_URL;
    }

    private static String loadApiBaseUrl() {
        Path externalConfig = Path.of("config", "config.properties");

        if (Files.exists(externalConfig)) {
            try (InputStream in = Files.newInputStream(externalConfig)) {
                Properties props = new Properties();
                props.load(in);
                String url = props.getProperty("API_BASE_URL");
                if (url != null && !url.isBlank()) {
                    return url.trim();
                }
            } catch (IOException e) {
                System.err.println("Could not read config/config.properties, falling back to default: " + e.getMessage());
            }
        }

        return DEFAULT_API_BASE_URL;
    }
}
