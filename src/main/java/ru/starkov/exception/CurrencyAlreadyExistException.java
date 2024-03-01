package ru.starkov.exception;

/**
 * An exception indicating that a currency already exists. This exception is thrown when attempting
 * to add a currency that already exists in the system.
 */
public class CurrencyAlreadyExistException extends RuntimeException {

  public CurrencyAlreadyExistException(String message) {
    super(message);
  }
}
