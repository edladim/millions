package edu.ntnu.idi.idatt.millions.model.market;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ReadOnlyStockTest {
  @Test
  void getLatestPercentChange_whenPreviousIsZero_returnsZero() {
    ReadOnlyStock stock = new Stock("ZERO", "Zero Inc.", BigDecimal.ZERO);
    assertEquals(BigDecimal.ZERO, stock.getLatestPercentChange());
  }

  @Test
  void getLatestPercentChange_whenPreviousNonZero_returnsPercent() {
    Stock stock = new Stock("TEST", "Test Inc.", new BigDecimal("100"));
    stock.addNewSalesPrice(new BigDecimal("110")); // change = 10, previous = 100
    assertEquals(new BigDecimal("10.0000"), stock.getLatestPercentChange());
  }
}
