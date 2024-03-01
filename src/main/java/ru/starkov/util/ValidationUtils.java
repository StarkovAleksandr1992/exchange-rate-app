package ru.starkov.util;

import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * The ValidationUtils class provides utility methods for validating data. It includes methods to
 * check if a map is null or empty, if a string is null or blank, and if two sets of strings are
 * equal.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidationUtils {

  public static <K, V> boolean isNullOrEmpty(Map<K, V> map) {
    return map == null || map.isEmpty();
  }

  public static boolean isNullOrBlank(String value) {
    return value == null || value.isBlank();
  }

  public static boolean isClientFormMatchesServerSideForm(Set<String> clientSideForm,
      Set<String> serverSideForm) {
    return clientSideForm.equals(serverSideForm);
  }
}
