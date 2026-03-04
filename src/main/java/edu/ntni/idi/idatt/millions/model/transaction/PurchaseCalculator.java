package edu.ntni.idi.idatt.millions.model.transaction;

import edu.ntni.idi.idatt.millions.model.Share;

import java.math.BigDecimal;

public class PurchaseCalculator implements TransactionCalculator {
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
    BigDecimal commisionRate =  BigDecimal.valueOf(0.005);
    return calculateGross().multiply(commisionRate);
  }

  @Override
  public BigDecimal calculateTax() {
    return BigDecimal.ZERO;
  }

  @Override
  public BigDecimal calculateTotal() {
    return calculateGross().add(calculateComission()).add(calculateTax());
  }
}
