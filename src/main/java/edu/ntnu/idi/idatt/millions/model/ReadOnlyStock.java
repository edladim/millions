package edu.ntnu.idi.idatt.millions.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * Read-only view of a {@link Stock} intended for use by observer classes
 * such as views.
 * </p>
 *
 * <p>
 * Exposing only getter methods ensures that observers can display stock data
 * without being able to mutate the price history. The full mutable
 * {@link Stock} is only accessible to controllers and the {@link Exchange}.
 * </p>
 */
public interface ReadOnlyStock {

  /**
   * Returns the ticker symbol of this stock.
   *
   * @return the ticker symbol, never null or blank
   */
  String getSymbol();

  /**
   * Returns the company name of this stock.
   *
   * @return the company name, never null or blank
   */
  String getCompany();

  /**
   * Returns the most recent sale price of this stock.
   *
   * @return the latest sale price, never null
   */
  BigDecimal getSalesPrice();

  /**
   * Returns an unmodifiable view of the full price history for this stock,
   * ordered from oldest to most recent.
   *
   * @return an unmodifiable list of prices, never null or empty
   */
  List<BigDecimal> getHistoricalPrices();

  /**
   * Returns the highest recorded sale price in the price history.
   *
   * @return the highest price ever recorded, never null
   */
  BigDecimal getHighestPrice();

  /**
   * Returns the lowest recorded sale price in the price history.
   *
   * @return the lowest price ever recorded, never null
   */
  BigDecimal getLowestPrice();

  /**
   * Returns the change between the two most recent sale prices.
   *
   * @return the difference between the last and second-to-last price,
   *         or {@link BigDecimal#ZERO} if fewer than two prices exist
   */
  BigDecimal getLatestPriceChange();
}
