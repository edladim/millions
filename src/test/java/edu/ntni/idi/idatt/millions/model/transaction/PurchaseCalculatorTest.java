package edu.ntni.idi.idatt.millions.model.transaction;

import edu.ntni.idi.idatt.millions.model.Share;
import edu.ntni.idi.idatt.millions.model.Stock;
import edu.ntni.idi.idatt.millions.model.transaction.PurchaseCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PurchaseCalculatorTest {

  private PurchaseCalculator purchaseCalculator;
  private Share share;

  @BeforeEach
  void setup() {
    Stock stock = new Stock("AAPL", "Apple Inc.", BigDecimal.valueOf(150));
    share = new Share(stock, BigDecimal.valueOf(100), BigDecimal.valueOf(10));
    purchaseCalculator = new PurchaseCalculator(share);
  }

  @Test
  void testCalculateGross() {
    BigDecimal expected = BigDecimal.valueOf(1000);
    assertEquals(expected, purchaseCalculator.calculateGross());
  }

  @Test
  void testCalculateCommision() {
    BigDecimal expected = new  BigDecimal("5.000");
    assertEquals(expected, purchaseCalculator.calculateComission());
  }

  @Test
  void testCalculateTax() {
    assertEquals(BigDecimal.ZERO,  purchaseCalculator.calculateTax());
  }

  @Test
  void testCalculateTotal() {
    BigDecimal expected = new BigDecimal("1005.000");
    assertEquals(expected, purchaseCalculator.calculateTotal());
  }

}
