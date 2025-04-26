package org.grupouno.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
//    private static Properties properties;
//
//    static {
//        properties = new Properties();
//        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(filePath.trim())) {
//            if (input != null) {
//                properties.load(input);
//            }
//        } catch (Exception e) {
//            System.err.println("Failed to load config.properties: " + e.getMessage());
//        }
//    }
//
//    public static String get(String key) {
//        // Priority: System Property > Environment Variable > config.properties > null
//        return System.getProperty(key, System.getenv().getOrDefault(key, properties.getProperty(key)));
//    }

    public static void main(String[] args) {
        Properties properties = new Properties();
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            properties.load(input);
            String myValue = properties.getProperty("some.key");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
