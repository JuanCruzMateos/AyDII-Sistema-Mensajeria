package org.grupouno.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class ConfigService {
    private static final Logger logger = Logger.getLogger(ConfigService.class.getName());
    private static final Properties properties = new Properties();
    private static boolean propertiesLoaded = false;

    private static void loadProperties() {
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                logger.warning("config.properties not found");
            } else {
                properties.load(input);
            }
        } catch (IOException ex) {
            logger.warning("Error loading config.properties: " + ex.getMessage());
        }
    }

    public static String getConfig(String key) {
        if (!propertiesLoaded) {
            loadProperties();
            propertiesLoaded = true;
        }
        return properties.getProperty(key);
    }
}