package edu.ntni.idi.idatt.millions.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a stock with a ticker symbol, company name,
 * and a historical record of sale prices.
 *
 * <p>A stock is uniquely identified by its {@link #symbol ticker symbol}.
 * Two stocks with the same symbol are considered equal regardless of
 * company name or price history.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Stock apple = new Stock("AAPL", "Apple Inc.", new BigDecimal("189.50"));
 * apple.addNewSalesPrice(new BigDecimal("192.30"));
 *
 * System.out.println(apple.getSalesPrice()); // 192.30
 * }</pre>
 */
public final class Stock {
  private final String symbol;
  private final String company;
  private final List<BigDecimal> prices = new ArrayList<>();

  /**
   * Constructs a new {@code Stock} with the given symbol, company name,
   * and initial sale price.
   *
   * @param symbol     the ticker symbol (e.g. {@code "AAPL"}), cannot be null or blank
   * @param company    the full company name (e.g. {@code "Apple Inc."}), cannot be null or blank
   * @param salesPrice the initial sale price cannot be null or negative
   * @throws NullPointerException     if any argument is null
   * @throws IllegalArgumentException if {@code symbol} or {@code company} is blank,
   *                                  or if {@code salesPrice} is negative
   */
  public Stock(String symbol, String company, BigDecimal salesPrice) {
    this.symbol = validateString(symbol, "Symbol");
    this.company = validateString(company, "Company");
    addNewSalesPrice(salesPrice);
  }

  /**
   * Returns the ticker symbol of this stock.
   *
   * @return the ticker symbol, never null or blank
   */
  public String getSymbol() {
    return symbol;
  }

  /**
   * Returns the company name of this stock.
   *
   * @return the company name, never null or blank
   */
  public String getCompany() {
    return company;
  }

  /**
   * Returns an unmodifiable view of the full price history for this stock,
   * ordered from oldest to most recent.
   *
   * @return an unmodifiable list of prices, never null or empty
   */
  public List<BigDecimal> getHistoricalPrices() {
    return List.copyOf(prices);
  }

  /**
   * Returns the most recent sale price of this stock.
   *
   * @return the latest sale price, never null
   */
  public BigDecimal getSalesPrice() {
    return prices.getLast();
  }

  /**
   * Adds a new sale price to the price history of this stock.
   *
   * <p>Prices are stored in insertion order. The added price becomes
   * the new value returned by {@link #getSalesPrice()}.</p>
   *
   * @param price the new sale price to record, cannot be null or negative
   * @throws NullPointerException     if {@code price} is null
   * @throws IllegalArgumentException if {@code price} is negative
   */
  public void addNewSalesPrice(BigDecimal price) {
    Objects.requireNonNull(price, "Price cannot be null");

    if (price.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Price cannot be negative");
    }

    prices.add(price);
  }

  /**
   * Returns the highest recorded sale price in the price history.
   *
   * @return the highest price ever recorded, never null
   */
  public BigDecimal getHighestPrice() {
    return prices.stream()
        .max(BigDecimal::compareTo)
        .orElseThrow();
  }

  /**
   * Returns the lowest recorded sale price in the price history.
   *
   * @return the lowest price ever recorded, never null
   */
  public BigDecimal getLowestPrice() {
    return prices.stream()
        .min(BigDecimal::compareTo)
        .orElseThrow();
  }

  /**
   * Returns the change between the two most recent sale prices.
   *
   * <p>The change is calculated as the latest price minus the second-to-last price.
   * A positive value indicates a price increase and a negative value indicates a decrease.</p>
   *
   * <p>If only one price has been recorded, this is interpreted as no change
   * and {@link BigDecimal#ZERO} is returned.</p>
   *
   * @return the difference between the last and second-to-last price,
   *         or {@code BigDecimal.ZERO} if fewer than two prices exist
   */
  public BigDecimal getLatestPriceChange() {
    if (prices.size() < 2) {
      return BigDecimal.ZERO;
    }
    return prices.getLast().subtract(prices.get(prices.size() - 2));
  }

  /**
   * Indicates whether some other object is equal to this stock.
   * Two stocks are considered equal if they share the same ticker symbol,
   * regardless of company name or price history.
   *
   * @param o the object to compare with
   * @return {@code true} if {@code o} is a {@code Stock} with the same symbol
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Stock other)) return false;
    return symbol.equals(other.symbol);
  }

  /**
   * Returns a hash code based solely on the ticker symbol,
   * consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(symbol);
  }

  @Override
  public String toString() {
    return symbol + " " + company + " " + getHistoricalPrices();
  }

  /**
   * Validates that a string field is non-null and non-blank.
   *
   * @param value     the value to validate
   * @param fieldName the name of the field, used in exception messages
   * @return the trimmed value
   * @throws NullPointerException     if {@code value} is null
   * @throws IllegalArgumentException if {@code value} is blank after trimming
   */
  private static String validateString(String value, String fieldName) {
    value = Objects.requireNonNull(value, fieldName + " cannot be null").trim();
    if (value.isEmpty()) {
      throw new IllegalArgumentException(fieldName + " cannot be empty");
    }
    return value;
  }
}