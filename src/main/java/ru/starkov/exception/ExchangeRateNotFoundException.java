package ru.starkov.exception;

/**
 * An exception indicating that exchange rate was not found. This exception is thrown when
 * attempting to retrieve or manipulate an exchange rate that not exists in the system.
 */
public class ExchangeRateNotFoundException extends RuntimeException {

  public ExchangeRateNotFoundException(String message) {
    super(message);
  }
}
