package edu.ntnu.idi.idatt.millions.model.transaction;
import edu.ntnu.idi.idatt.millions.model.portfolio.Share;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Performs financial calculations for purchase transactions.
 *
 * <p>This calculator determines the financial values associated with
 * purchasing shares, including:</p>
 *
 * <ul>
 *   <li>Gross purchase value</li>
 *   <li>Broker commission</li>
 *   <li>Tax (none for purchases)</li>
 *   <li>Total cost of the transaction</li>
 * </ul>
 *
 * <p>The calculations follow these rules:</p>
 *
 * <ul>
 *   <li>Gross = purchasePrice × quantity</li>
 *   <li>Commission = 0.5% of gross</li>
 *   <li>Tax = 0</li>
 *   <li>Total = gross + commission</li>
 * </ul>
 */
public final class PurchaseCalculator implements TransactionCalculator {

  private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.005");

  private final BigDecimal purchasePrice;
  private final BigDecimal quantity;

  /**
   * Creates a calculator based on a {@link Share}.
   *
   * <p>The share provides the purchase price and quantity used
   * for all calculations.</p>
   *
   * @param share the share being purchased
   * @throws NullPointerException if {@code share} is null
   */
  public PurchaseCalculator(Share share) {
    Objects.requireNonNull(share, "Share cannot be null");

    this.purchasePrice = share.getPurchasePrice();
    this.quantity = share.getQuantity();
  }

  /**
   * Calculates the gross value of the purchase.
   *
   * <p>The gross value represents the total price of the shares
   * before any fees are applied.</p>
   *
   * @return purchase price multiplied by quantity
   */
  @Override
  public BigDecimal calculateGross() {
    return purchasePrice.multiply(quantity);
  }

  /**
   * Calculates the broker commission charged for the purchase.
   *
   * <p>The commission is 0.5% of the gross transaction value.</p>
   *
   * @return the commission fee
   */
  @Override
  public BigDecimal calculateCommission() {
    return calculateGross().multiply(COMMISSION_RATE);
  }

  /**
   * Calculates the tax for the purchase transaction.
   *
   * <p>No tax is applied to purchase transactions.</p>
   *
   * @return zero
   */
  @Override
  public BigDecimal calculateTax() {
    return BigDecimal.ZERO;
  }

  /**
   * Calculates the total cost of the purchase transaction.
   *
   * <p>The total represents the amount paid by the investor,
   * including commission.</p>
   *
   * @return gross value plus commission
   */
  @Override
  public BigDecimal calculateTotal() {
    return calculateGross()
        .add(calculateCommission())
        .add(calculateTax());
  }
}