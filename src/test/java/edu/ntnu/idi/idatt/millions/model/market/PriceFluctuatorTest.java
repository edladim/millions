package edu.ntnu.idi.idatt.millions.model.market;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.math.BigDecimal;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class PriceFluctuatorTest {
  @Test
  void beginWeek_defaultImplementation_doesNothing() {
    PriceFluctuator fluctuator =
        new PriceFluctuator() {
          @Override
          public BigDecimal nextPrice(String symbol, BigDecimal currentPrice, Random random) {
            return currentPrice;
          }
        };

    assertDoesNotThrow(() -> fluctuator.beginWeek(new Random(1)));
  }
}
