package ru.starkov.exception;

/**
 * An exception indicating a database-related error. This exception is thrown when an error occurs
 * during database operations.
 */
public class DatabaseException extends RuntimeException {

  public DatabaseException(String message, Throwable cause) {
    super(message, cause);
  }
}
