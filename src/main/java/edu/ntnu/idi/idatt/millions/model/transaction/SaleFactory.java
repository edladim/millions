package edu.ntnu.idi.idatt.millions.model.transaction;

import edu.ntnu.idi.idatt.millions.model.Share;

/**
 * Factory that creates {@link Sale} transactions.
 *
 * <p>This is a concrete implementation of {@link TransactionFactory} for the
 * sell side of a trade. It instantiates a {@link Sale} and inherits the
 * shared {@link TransactionFactory#createAndCommit} behaviour from the base class.</p>
 *
 * @see TransactionFactory
 * @see Sale
 */
public final class SaleFactory extends TransactionFactory {

  /**
   * Creates a new {@link Sale} transaction for the given share and week.
   *
   * @param share the share to sell, cannot be null
   * @param week  the trading week, must be positive
   * @return a new, uncommitted {@link Sale}
   * @throws NullPointerException if {@code share} is null
   * @throws IllegalArgumentException if {@code week} is not positive
   */
  @Override
  public Transaction create(Share share, int week) {
    return new Sale(share, week);
  }
}
