package edu.ntni.idi.idatt.millions.model.transaction;

import edu.ntni.idi.idatt.millions.model.Share;
import edu.ntni.idi.idatt.millions.model.Stock;
import edu.ntni.idi.idatt.millions.model.transaction.SaleCalculator;
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
    share = new Share(stock, BigDecimal.valueOf(10), BigDecimal.valueOf(100));
    saleCalculator = new SaleCalculator(share);
  }

  @Test
  void testCalculateGross() {
    BigDecimal expected = new BigDecimal("2000");
    assertEquals(expected, saleCalculator.calculateGross());
  }

  @Test
  void testCalculateCommission() {
    BigDecimal expected = new BigDecimal("20.00");
    assertEquals(expected, saleCalculator.calculateComission());
  }

  @Test
  void testCalculatetax() {
    BigDecimal expected = new BigDecimal("300.0");
    assertEquals(expected, saleCalculator.calculateTax());
  }

  @Test
  void testCalclateTotal() {
    BigDecimal expected = new BigDecimal("1680.00");
    assertEquals(expected, saleCalculator.calculateTotal());
  }

}
