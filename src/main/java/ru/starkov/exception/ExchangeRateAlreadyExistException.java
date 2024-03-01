package ru.starkov.exception;

public class ExchangeRateAlreadyExistException extends RuntimeException {

  public ExchangeRateAlreadyExistException(String message) {
    super(message);
  }
}
