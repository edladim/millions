package edu.ntni.idi.idatt.millions.calculator;

import edu.ntni.idi.idatt.millions.Share;

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
  public BigDecimal calculateComission() {
    BigDecimal rate = BigDecimal.valueOf(0.01);
    return calculateGross().multiply(rate);
  }

  @Override
  public BigDecimal calculateTax() {
    return calculateComission().subtract(BigDecimal.valueOf(0.3)).subtract(purchasePrice.multiply(quantity));
  }

  @Override
  public BigDecimal calculateTotal() {
    return calculateGross().subtract(calculateComission()).subtract(calculateTax());
  }
}
