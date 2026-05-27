package edu.ntnu.idi.idatt.millions.model.market;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MomentumFluctuatorTest {

  private MomentumFluctuator fluctuator;

  @BeforeEach
  void setUp() {
    fluctuator = new MomentumFluctuator();
  }

  private static final class ConstantRandom extends Random {
    private final double value;

    private ConstantRandom(double value) {
      this.value = value;
    }

    @Override
    public double nextDouble() {
      return value;
    }
  }

  @Test
  void beginWeek_handlesMarketEventAndNonEvent() {
    fluctuator.beginWeek(new ConstantRandom(0.0));
    BigDecimal priceAfterEvent =
        fluctuator.nextPrice("AAA", new BigDecimal("10"), new ConstantRandom(1.0));
    assertTrue(priceAfterEvent.compareTo(BigDecimal.ONE) > 0);

    fluctuator.beginWeek(new ConstantRandom(1.0));
    BigDecimal priceAfterNoEvent =
        fluctuator.nextPrice("BBB", new BigDecimal("10"), new ConstantRandom(1.0));
    assertTrue(priceAfterNoEvent.compareTo(BigDecimal.ONE) > 0);
  }

  @Test
  void nextPrice_returnsFloorWhenBelowOne() {
    fluctuator.beginWeek(new ConstantRandom(0.0));

    BigDecimal price =
        fluctuator.nextPrice("FLOOR", new BigDecimal("1.00"), new ConstantRandom(0.0));

    assertEquals(BigDecimal.ONE, price);
  }

  @Test
  void nextPrice_returnsRawPriceWhenAtOrAboveFloor() {
    fluctuator.beginWeek(new ConstantRandom(1.0));

    BigDecimal price =
        fluctuator.nextPrice("ABOVE", new BigDecimal("1.00"), new ConstantRandom(1.0));

    assertTrue(price.compareTo(BigDecimal.ONE) > 0);
  }

  @Test
  void nextPrice_reusesExistingVolatilityForSameSymbol() {
    fluctuator.beginWeek(new ConstantRandom(1.0));

    BigDecimal first = fluctuator.nextPrice("REUSE", new BigDecimal("10"), new ConstantRandom(1.0));
    assertTrue(first.compareTo(BigDecimal.ONE) > 0);

    BigDecimal second =
        fluctuator.nextPrice("REUSE", new BigDecimal("10"), new ConstantRandom(1.0));
    assertTrue(second.compareTo(BigDecimal.ONE) > 0);
  }
}
