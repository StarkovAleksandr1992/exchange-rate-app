package ru.starkov.util;

import java.io.IOException;
import java.util.Properties;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * The PropertiesLoader class is responsible for loading properties from the application.properties
 * file. It provides a method to retrieve a property value by its key.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PropertiesLoader {

  private static final Properties PROPERTIES = new Properties();

  static {
    loadProperties();
  }

  /**
   * Retrieves the value of the property associated with the specified key.
   *
   * @param key the key of the property
   * @return the value of the property, or null if the key is not found
   */
  public static String get(String key) {
    return PROPERTIES.getProperty(key);
  }

  private static void loadProperties() {
    try (var propertiesStream = PropertiesLoader.class.getClassLoader()
        .getResourceAsStream("application.properties")) {
      PROPERTIES.load(propertiesStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
