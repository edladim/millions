package edu.ntnu.idi.idatt.millions.persistence;

/**
 * Thrown when reading or writing persistent data (stocks, leaderboard, ...) fails.
 *
 * <p>This is a checked exception, forcing callers to handle persistence errors explicitly rather
 * than letting them propagate silently as unchecked exceptions.
 */
public class PersistenceException extends Exception {

  /**
   * Constructs a new exception with the given detail message.
   *
   * @param message a description of what went wrong
   */
  public PersistenceException(String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the given detail message and cause.
   *
   * @param message a description of what went wrong
   * @param cause the underlying exception that triggered this one
   */
  public PersistenceException(String message, Throwable cause) {
    super(message, cause);
  }
}
