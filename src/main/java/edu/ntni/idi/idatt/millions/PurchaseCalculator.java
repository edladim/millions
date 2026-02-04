package edu.ntni.idi.idatt.millions;

import java.math.BigDecimal;

public class PurchaseCalculator implements TransactionCalculator{
  private BigDecimal purchasePrice;
  private BigDecimal quantity;

  public PurchaseCalculator(BigDecimal purchasePrice, BigDecimal quantity) {
    this.purchasePrice = purchasePrice;
    this.quantity = quantity;
  }

  @Override
  public BigDecimal calculateGross() {
    return purchasePrice.multiply(quantity);
  }

  @Override
  public BigDecimal calculateComission() {
    return purchasePrice;
  }

  @Override
  public BigDecimal calculateTax() {
    return purchasePrice;
  }

  @Override
  public BigDecimal calculateTotal() {
    return purchasePrice;
  }
}
