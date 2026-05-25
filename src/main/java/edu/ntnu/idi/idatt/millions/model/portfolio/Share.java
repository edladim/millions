package edu.ntnu.idi.idatt.millions.model.portfolio;

import edu.ntnu.idi.idatt.millions.model.market.ReadOnlyStock;
import edu.ntnu.idi.idatt.millions.model.market.Stock;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents ownership of a given quantity of a specific {@link Stock}.
 *
 * <p>A {@code Share} contains information about:
 *
 * <ul>
 *   <li>The stock that was purchased
 *   <li>The quantity purchased
 *   <li>The purchase price per unit at the time of purchase
 * </ul>
 *
 * <p>This class is immutable and guarantees that:
 *
 * <ul>
 *   <li>Stock is never null
 *   <li>Quantity is positive
 *   <li>Purchase price is non-negative
 * </ul>
 */
public final class Share {

  private final Stock stock;
  private final BigDecimal quantity;
  private final BigDecimal purchasePrice;

  /**
   * Constructs a new {@code Share}.
   *
   * @param stock the stock being owned, cannot be null
   * @param quantity the amount of stock purchased, must be positive
   * @param purchasePrice the price per stock at purchase time, cannot be null or negative
   * @throws NullPointerException if {@code stock} or {@code purchasePrice} is null
   * @throws IllegalArgumentException if {@code quantity} is zero or negative, or if {@code
   *     purchasePrice} is negative
   */
  public Share(Stock stock, BigDecimal quantity, BigDecimal purchasePrice) {
    this.stock = Objects.requireNonNull(stock, "Stock cannot be null");
    this.quantity = validateQuantity(quantity);
    this.purchasePrice = validateNonNegative(purchasePrice, "Purchase price");
  }

  /**
   * Returns the stock associated with this share.
   *
   * @return the stock as a read-only view, never null
   */
  public ReadOnlyStock getStock() {
    return stock;
  }

  /**
   * Returns the quantity of stock owned.
   *
   * @return the quantity, always positive
   */
  public BigDecimal getQuantity() {
    return quantity;
  }

  /**
   * Returns the purchase price per stock.
   *
   * @return the purchase price, never negative
   */
  public BigDecimal getPurchasePrice() {
    return purchasePrice;
  }

  /**
   * Calculates the total amount originally invested.
   *
   * @return quantity multiplied by purchase price
   */
  public BigDecimal getTotalInvestment() {
    return purchasePrice.multiply(quantity);
  }

  /**
   * Calculates the current market value of this share.
   *
   * <p>The current price is retrieved from the associated {@link Stock}.
   *
   * @return current stock price multiplied by quantity
   */
  public BigDecimal getCurrentValue() {
    return stock.getSalesPrice().multiply(quantity);
  }

  /**
   * Calculates the unrealized gain or loss.
   *
   * @return current value minus total investment
   */
  public BigDecimal getGainOrLoss() {
    return getCurrentValue().subtract(getTotalInvestment());
  }

  /**
   * Indicates whether some other object is equal to this share. Two shares are considered equal if
   * they refer to the same stock and have the same quantity and purchase price regardless of time
   * of purchase, owner etc.
   *
   * @param o the object to compare with
   * @return true if equal
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Share other)) return false;
    return stock.equals(other.stock)
        && quantity.compareTo(other.quantity) == 0
        && purchasePrice.compareTo(other.purchasePrice) == 0;
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}.
   *
   * @return hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(stock, quantity.stripTrailingZeros(), purchasePrice.stripTrailingZeros());
  }

  /** Validates that quantity is non-null and strictly positive. */
  private static BigDecimal validateQuantity(BigDecimal quantity) {
    Objects.requireNonNull(quantity, "Quantity cannot be null");
    if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Quantity must be positive");
    }
    return quantity;
  }

  /** Validates that a monetary value is non-null and not negative. */
  private static BigDecimal validateNonNegative(BigDecimal value, String fieldName) {
    Objects.requireNonNull(value, fieldName + " cannot be null");
    if (value.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException(fieldName + " cannot be negative");
    }
    return value;
  }
}
