package edu.ntnu.idi.idatt.millions.model.portfolio;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * Read-only view of a {@link Portfolio} intended for use by observer classes
 * such as views.
 * </p>
 *
 * <p>
 * Exposing only getter methods ensures that observers can display portfolio
 * data without being able to add or remove shares. The full mutable
 * {@link Portfolio} is only accessible to controllers.
 * </p>
 */
public interface ReadOnlyPortfolio {

  /**
   * Returns an unmodifiable list of all shares in the portfolio.
   *
   * @return the shares, never null
   */
  List<Share> getShares();

  /**
   * Returns one {@link Holding} per unique stock symbol, aggregating all share
   * lots that reference the same stock.
   *
   * @return the aggregated holdings, never null
   */
  List<Holding> getHoldings();

  /**
   * Returns the current total market value of the portfolio.
   *
   * @return the total market value
   */
  BigDecimal getTotalValue();

  /**
   * Returns the total amount originally invested in the portfolio.
   *
   * @return the total invested capital
   */
  BigDecimal getTotalInvestment();

  /**
   * Returns the total unrealized gain or loss of the portfolio.
   *
   * @return the total gain or loss
   */
  BigDecimal getTotalGainOrLoss();

  /**
   * Returns the net worth of the portfolio after simulated sale deductions.
   *
   * @return the net worth
   */
  BigDecimal getNetWorth();

  /**
   * Returns the number of shares currently in the portfolio.
   *
   * @return the share count
   */
  int size();

  /**
   * <p>Returns the number of distinct stock symbols held in the portfolio.
   * Two {@link Share} lots referencing the same stock count as one.</p>
   *
   * @return the number of unique stocks owned
   */
  long getDistinctStockCount();
}
