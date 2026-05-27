package edu.ntnu.idi.idatt.millions.model.transaction;

import edu.ntnu.idi.idatt.millions.model.portfolio.Share;

/**
 * Factory that creates {@link Purchase} transactions.
 *
 * <p>This is a concrete implementation of {@link TransactionFactory} for the buy side of a trade.
 * It instantiates a {@link Purchase} and inherits the shared {@link
 * TransactionFactory#createAndCommit} behaviour from the base class.
 *
 * @see TransactionFactory
 * @see Purchase
 */
public final class PurchaseFactory extends TransactionFactory {

  /**
   * Creates a new {@link Purchase} transaction for the given share and week.
   *
   * @param share the share to purchase, cannot be null
   * @param week the trading week, must be positive
   * @return a new, uncommitted {@link Purchase}
   * @throws NullPointerException if {@code share} is null
   * @throws IllegalArgumentException if {@code week} is not positive
   */
  @Override
  public Transaction create(Share share, int week) {
    return new Purchase(share, week);
  }
}
