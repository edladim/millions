package edu.ntnu.idi.idatt.millions.model.transaction;

import edu.ntnu.idi.idatt.millions.model.Share;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Performs financial calculations for sale transactions.
 *
 * <p>This calculator determines the financial values associated with
 * selling shares, including:</p>
 *
 * <ul>
 *   <li>Gross sale value</li>
 *   <li>Broker commission</li>
 *   <li>Tax on profit</li>
 *   <li>Total amount received from the transaction</li>
 * </ul>
 *
 * <p>The calculations follow these rules:</p>
 *
 * <ul>
 *   <li>Gross = salesPrice × quantity</li>
 *   <li>Commission = 1% of gross</li>
 *   <li>Tax = 30% of profit (only if the transaction results in a profit)</li>
 *   <li>Total = gross − commission − tax</li>
 * </ul>
 *
 * <p>The profit used for tax calculation is determined as:</p>
 *
 * <ul>
 *   <li>Profit = gross − (purchasePrice × quantity)</li>
 * </ul>
 *
 * <p>If the calculated profit is zero or negative, no tax is applied.</p>
 */
public final class SaleCalculator implements TransactionCalculator {

  private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.01");
  private static final BigDecimal TAX_RATE = new BigDecimal("0.30");

  private final BigDecimal purchasePrice;
  private final BigDecimal salesPrice;
  private final BigDecimal quantity;

  /**
   * Creates a calculator based on a share.
   *
   * @param share the share being sold
   * @throws NullPointerException if {@code share} is null
   */
  public SaleCalculator(Share share) {
    Objects.requireNonNull(share, "Share cannot be null");

    this.purchasePrice = share.getPurchasePrice();
    this.salesPrice = share.getStock().getSalesPrice();
    this.quantity = share.getQuantity();
  }

  /**
   * Calculates the gross value of the sale.
   *
   * @return sales price multiplied by quantity
   */
  @Override
  public BigDecimal calculateGross() {
    return salesPrice.multiply(quantity);
  }

  /**
   * Calculates the broker commission for the sale.
   *
   * @return 1% of the gross value
   */
  @Override
  public BigDecimal calculateCommission() {
    return calculateGross().multiply(COMMISSION_RATE);
  }

  /**
   * Calculates the tax on profit from the sale.
   *
   * <p>Tax is only applied if the sale results in a profit.
   * Losses are not taxed.</p>
   *
   * @return the tax amount or zero if the sale produced no profit
   */
  @Override
  public BigDecimal calculateTax() {

    BigDecimal purchaseValue = purchasePrice.multiply(quantity);

    BigDecimal profit = calculateGross().subtract(purchaseValue);

    if (profit.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }

    return profit.multiply(TAX_RATE);
  }

  /**
   * Calculates the final total received after commission and tax.
   *
   * @return the final transaction value
   */
  @Override
  public BigDecimal calculateTotal() {
    return calculateGross()
        .subtract(calculateCommission())
        .subtract(calculateTax());
  }
}