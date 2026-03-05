package edu.ntni.idi.idatt.millions.model.transaction;

import edu.ntni.idi.idatt.millions.model.Share;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Performs financial calculations for sale transactions.
 */
public class SaleCalculator implements TransactionCalculator {

  private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.01");
  private static final BigDecimal TAX_RATE = new BigDecimal("0.30");

  private final BigDecimal purchasePrice;
  private final BigDecimal salesPrice;
  private final BigDecimal quantity;

  /**
   * Creates a calculator based on a share.
   *
   * @param share the share involved in the transaction
   * @throws NullPointerException if share is null
   */
  public SaleCalculator(Share share) {
    Objects.requireNonNull(share, "Share cannot be null");

    this.purchasePrice = share.getPurchasePrice();
    this.salesPrice = share.getStock().getSalesPrice();
    this.quantity = share.getQuantity();
  }

  @Override
  public BigDecimal calculateGross() {
    return salesPrice.multiply(quantity);
  }

  @Override
  public BigDecimal calculateCommission() {
    return calculateGross().multiply(COMMISSION_RATE);
  }

  @Override
  public BigDecimal calculateTax() {
    BigDecimal profit = calculateGross()
        .subtract(calculateCommission())
        .subtract(purchasePrice.multiply(quantity));

    if (profit.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }

    return profit.multiply(TAX_RATE);
  }

  @Override
  public BigDecimal calculateTotal() {
    return calculateGross()
        .subtract(calculateCommission())
        .subtract(calculateTax());
  }
}