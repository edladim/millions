package edu.ntnu.idi.idatt.millions.model.market;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

  /**
   * Returns the latest price change as a percentage of the previous price.
   *
   * <p>Computed as {@code change / (currentPrice − change) × 100}. Returns
   * {@link BigDecimal#ZERO} when no previous price exists so callers can sort
   * and render uniformly without null checks.</p>
   *
   * @return the latest percentage change, scaled to four decimal places
   */
  default BigDecimal getLatestPercentChange() {
    BigDecimal change = getLatestPriceChange();
    BigDecimal previous = getSalesPrice().subtract(change);
    if (previous.signum() == 0) return BigDecimal.ZERO;
    return change.divide(previous, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
  }
}
