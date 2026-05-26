package edu.ntnu.idi.idatt.millions.model.transaction;

import edu.ntnu.idi.idatt.millions.model.portfolio.Share;

/**
 * Read-only view of a committed {@link Transaction} intended for use by observer classes such as
 * views.
 *
 * <p>Exposes only the getter methods that views need to display transaction data. The mutating
 * {@link Transaction#commit} method is deliberately absent so view code cannot trigger a second
 * commit.
 *
 * <p>The full mutable {@link Transaction} is only accessible to model and controller code.
 *
 * @see Transaction
 * @see edu.ntnu.idi.idatt.millions.model.player.ReadOnlyPlayer#getTransactions()
 */
public interface ReadOnlyTransaction {

  /**
   * Returns the share involved in this transaction.
   *
   * @return the share, never null
   */
  Share getShare();

  /**
   * Returns the calculator used to derive financial values (gross, commission, tax, total).
   *
   * @return the transaction calculator, never null
   */
  TransactionCalculator getCalculator();

  /**
   * Returns the week in which this transaction was executed.
   *
   * @return the week number, always positive
   */
  int getWeek();

  /**
   * Returns {@code true} if this transaction is a purchase, {@code false} if it is a sale.
   *
   * @return {@code true} for a buy, {@code false} for a sell
   */
  boolean isBuy();
}
