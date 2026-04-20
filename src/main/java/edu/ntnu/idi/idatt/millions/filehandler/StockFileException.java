package edu.ntnu.idi.idatt.millions.filehandler;

/**
 * Thrown when reading or writing stock data from/to a file fails.
 *
 * <p>This is a checked exception, forcing callers to handle file errors
 * explicitly rather than letting them propagate silently as unchecked exceptions.</p>
 */
public class StockFileException extends Exception {

  /**
   * Constructs a new exception with the given detail message.
   *
   * @param message a description of what went wrong
   */
  public StockFileException(String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the given detail message and cause.
   *
   * @param message a description of what went wrong
   * @param cause   the underlying exception that triggered this one
   */
  public StockFileException(String message, Throwable cause) {
    super(message, cause);
  }
}
