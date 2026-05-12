package edu.ntnu.idi.idatt.millions.model;

import edu.ntnu.idi.idatt.millions.model.transaction.Transaction;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * Read-only view of a {@link Player} intended for use by observer classes
 * such as views.
 * </p>
 *
 * <p>
 * Exposing only getter methods ensures that observers can display player data
 * without being able to mutate the player's state. The full mutable
 * {@link Player} is only accessible to controllers.
 * </p>
 */
public interface ReadOnlyPlayer {

  /**
   * Returns the player's name.
   *
   * @return the player name
   */
  String getName();

  /**
   * Returns the player's current cash balance.
   *
   * @return the current balance
   */
  BigDecimal getMoney();

  /**
   * Returns the player's total net worth (cash pluss portfolio net worth).
   *
   * @return the total net worth
   */
  BigDecimal getNetWorth();

  List<BigDecimal> getHistoricalNetWorth();

  /**
   * Returns the player's profit relative to starting capital.
   *
   * @return the profit, may be negative
   */
  BigDecimal getProfit();

  /**
   * Returns the player's growth rate as a decimal (e.g. {@code 0.20} for 20%).
   *
   * @return the return rate
   */
  BigDecimal getReturnRate();

  /**
   * Returns the player's current progression status.
   *
   * @return the player status
   */
  PlayerStatus getStatus();

  /**
   * Returns a read-only view of the player's portfolio.
   *
   * @return the read-only portfolio
   */
  ReadOnlyPortfolio getPortfolio();

  /**
   * Returns all transactions in the player's archive, in insertion order.
   *
   * @return an unmodifiable list of all transactions
   */
  List<Transaction> getTransactions();
}
