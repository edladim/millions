package edu.ntni.idi.idatt.millions.calculator;

import edu.ntni.idi.idatt.millions.Share;
import edu.ntni.idi.idatt.millions.Stock;
import edu.ntni.idi.idatt.millions.calculator.PurchaseCalculator;
import edu.ntni.idi.idatt.millions.calculator.SaleCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

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
    BigDecimal expected = BigDecimal.valueOf(5.0);
    assertEquals(expected, purchaseCalculator.calculateComission());
  }

  @Test
  void testCalculateTax() {
    assertEquals(BigDecimal.ZERO,  purchaseCalculator.calculateTax());
  }

  @Test
  void testCalculateTotal() {
    BigDecimal expected = BigDecimal.valueOf(1005);
    assertEquals(expected, purchaseCalculator.calculateTotal());
  }

}
