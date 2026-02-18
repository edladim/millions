package edu.ntni.idi.idatt.millions;

import java.math.BigDecimal;

public class PurchaseCalculator implements TransactionCalculator{
  private BigDecimal purchasePrice;
  private BigDecimal quantity;

  public PurchaseCalculator(Share share) {
    this.purchasePrice = share.getPurchasePrice();
    this.quantity = share.getQuantity();
  }

  @Override
  public BigDecimal calculateGross() {
    return purchasePrice.multiply(quantity);
  }

  @Override
  public BigDecimal calculateComission() {
    return calculateGross().multiply(BigDecimal.valueOf(0.005));
  }

  @Override
  public BigDecimal calculateTax() {
    return BigDecimal.ZERO;
  }

  @Override
  public BigDecimal calculateTotal() {
    return calculateComission().subtract(calculateComission()).subtract(calculateTax());
  }
}
