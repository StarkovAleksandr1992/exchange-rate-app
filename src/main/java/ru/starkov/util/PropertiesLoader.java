package ru.starkov.util;

import java.io.IOException;
import java.util.Properties;

public class PropertiesLoader {

    private final static Properties PROPERTIES = new Properties();

    static {
        loadProperties();
    }

    public static String get(String key) {
        return PROPERTIES.getProperty(key);
    }

    private static void loadProperties() {
        try (var propertiesStream = PropertiesLoader.class.getClassLoader().getResourceAsStream("application.properties")) {
            PROPERTIES.load(propertiesStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private PropertiesLoader() {
    }
}
