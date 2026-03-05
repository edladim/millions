package edu.ntni.idi.idatt.millions.model.transaction;

import edu.ntni.idi.idatt.millions.model.Share;

import java.math.BigDecimal;

public class SaleCalculator implements TransactionCalculator {
  private BigDecimal purchasePrice;
  private BigDecimal salesPrice;
  private BigDecimal quantity;

  public SaleCalculator(Share share) {
    this.purchasePrice = share.getPurchasePrice();
    this.quantity = share.getQuantity();
    this.salesPrice = share.getStock().getSalesPrice();
  }

  @Override
  public BigDecimal calculateGross() {
    return salesPrice.multiply(quantity);
  }

  @Override
  public BigDecimal calculateCommission() {
    BigDecimal rate = BigDecimal.valueOf(0.01);
    return calculateGross().multiply(rate);
  }

  @Override
  public BigDecimal calculateTax() {
    BigDecimal purchaseCost = purchasePrice.multiply(quantity);
    BigDecimal earning = calculateGross().subtract(purchaseCost);

    if (earning.compareTo(BigDecimal.ZERO) > 0) {
      return earning.multiply(BigDecimal.valueOf(0.3));
    }
    return BigDecimal.ZERO;
  }

  @Override
  public BigDecimal calculateTotal() {
    return calculateGross().subtract(calculateCommission()).subtract(calculateTax());
  }
}
