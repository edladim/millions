package edu.ntni.idi.idatt.millions.calculator;

import edu.ntni.idi.idatt.millions.Share;
import edu.ntni.idi.idatt.millions.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class testSalesCalculator {

  private SaleCalculator saleCalculator;
  private Share share;

  @BeforeEach
  void setUp() {
    Stock stock = new Stock("AAPL", "Apple Inc.", BigDecimal.valueOf(200));
    share = new Share(stock, BigDecimal.valueOf(100), BigDecimal.valueOf(10));
    saleCalculator = new SaleCalculator(share);
  }

  @Test
  void testCalculateGross() {
    BigDecimal expected = BigDecimal.valueOf(2000);
    assertEquals(expected, saleCalculator.calculateGross());
  }

  @Test
  void testCalculateCommission() {
    BigDecimal expected = BigDecimal.valueOf(20);
    assertEquals(expected, saleCalculator.calculateComission());
  }


}
