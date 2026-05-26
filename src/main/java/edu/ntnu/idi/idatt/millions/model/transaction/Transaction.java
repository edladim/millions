package edu.ntnu.idi.idatt.millions.model.transaction;

import edu.ntnu.idi.idatt.millions.model.player.Player;
import edu.ntnu.idi.idatt.millions.model.portfolio.Share;
import java.util.Objects;

/**
 * Represents a financial transaction involving a {@link Share}.
 *
 * <p>A transaction represents either the purchase or sale of shares performed in a specific week.
 * Each transaction uses a {@link TransactionCalculator} to compute financial values such as gross
 * value, commission, tax and total value.
 *
 * <p>A transaction can only be committed once. After a transaction has been committed, further
 * attempts to commit it again should result in an exception.
 *
 * <p>This class serves as a base class for concrete transaction types such as {@link Purchase} and
 * {@link Sale}.
 */
public abstract class Transaction {

  private final Share share;
  private final int week;
  private final TransactionCalculator calculator;

  /** Indicates whether the transaction has already been committed. */
  protected boolean committed;

  /**
   * Creates a new transaction.
   *
   * @param share the share involved in the transaction
   * @param week the week in which the transaction occurs
   * @param calculator the calculator used for financial calculations
   * @throws NullPointerException if share or calculator is null
   * @throws IllegalArgumentException if week is not positive
   */
  protected Transaction(Share share, int week, TransactionCalculator calculator) {

    this.share = Objects.requireNonNull(share, "Share cannot be null");
    this.calculator = Objects.requireNonNull(calculator, "Calculator cannot be null");

    if (week <= 0) {
      throw new IllegalArgumentException("Week must be positive");
    }

    this.week = week;
    this.committed = false;
  }

  /**
   * Returns the share involved in this transaction.
   *
   * @return the share
   */
  public Share getShare() {
    return share;
  }

  /**
   * Returns the week in which the transaction occurs.
   *
   * @return the week number
   */
  public int getWeek() {
    return week;
  }

  /**
   * Returns the calculator used for this transaction.
   *
   * @return the transaction calculator
   */
  public TransactionCalculator getCalculator() {
    return calculator;
  }

  /**
   * Indicates whether the transaction has already been committed.
   *
   * @return true if the transaction has been committed
   */
  public boolean isCommitted() {
    return committed;
  }

  /**
   * Commits the transaction.
   *
   * <p>The implementation is provided by subclasses such as {@link Purchase} and {@link Sale}.
   *
   * @param player the player performing the transaction
   * @throws NullPointerException if player is null
   * @throws IllegalStateException if the transaction has already been committed
   */
  public abstract void commit(Player player);
}
