package ru.starkov.exception;

/**
 * An exception indicating that a currency was not found. This exception is thrown when attempting
 * to retrieve or manipulate a currency that does not exist in the system.
 */
public class CurrencyNotFoundException extends RuntimeException {

  public CurrencyNotFoundException(String message) {
    super(message);
  }
}
