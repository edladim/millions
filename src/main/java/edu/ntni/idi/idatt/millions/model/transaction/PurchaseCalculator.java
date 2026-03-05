package edu.ntni.idi.idatt.millions.model.transaction;
import edu.ntni.idi.idatt.millions.model.Share;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Performs financial calculations for purchase transactions.
 */
public class PurchaseCalculator implements TransactionCalculator {

  private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.005");

  private final BigDecimal purchasePrice;
  private final BigDecimal quantity;

  /**
   * Creates a calculator based on a share.
   *
   * @param share the share involved in the transaction
   * @throws NullPointerException if share is null
   */
  public PurchaseCalculator(Share share) {
    Objects.requireNonNull(share, "Share cannot be null");

    this.purchasePrice = share.getPurchasePrice();
    this.quantity = share.getQuantity();
  }

  @Override
  public BigDecimal calculateGross() {
    return purchasePrice.multiply(quantity);
  }

  @Override
  public BigDecimal calculateCommission() {
    return calculateGross().multiply(COMMISSION_RATE);
  }

  @Override
  public BigDecimal calculateTax() {
    return BigDecimal.ZERO;
  }

  @Override
  public BigDecimal calculateTotal() {
    return calculateGross()
        .add(calculateCommission())
        .add(calculateTax());
  }
}
