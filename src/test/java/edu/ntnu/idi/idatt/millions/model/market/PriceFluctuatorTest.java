package edu.ntnu.idi.idatt.millions.model.market;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class PriceFluctuatorTest {
  @Test
  void beginWeek_defaultImplementation_doesNothing() {
    PriceFluctuator fluctuator = new PriceFluctuator() {
      @Override
      public BigDecimal nextPrice(String symbol, BigDecimal currentPrice, Random random) {
        return currentPrice;
      }
    };

    assertDoesNotThrow(() -> fluctuator.beginWeek(new Random(1)));
  }
}
