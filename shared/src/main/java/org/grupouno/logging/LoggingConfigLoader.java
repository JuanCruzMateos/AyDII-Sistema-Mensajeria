package org.grupouno.logging;


import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

public class LoggingConfigLoader {
    public static void loadConfig() {
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("logging.properties")) {
            if (stream == null) {
                System.err.println("LoggingConfigLoader: logging.properties not found in classpath.");
            } else {
                LogManager.getLogManager().readConfiguration(stream);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading logging.properties", e);
        }
    }
}
