package edu.ntni.idi.idatt.millions.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Share}.
 *
 * <p>The tests verify that {@code Share}:</p>
 * <ul>
 *   <li>validates constructor and method arguments</li>
 *   <li>calculates investment, current value and gain/loss correctly</li>
 *   <li>implements value-based equality (scale-insensitive)</li>
 * </ul>
 *
 * <p>Both valid cases and exceptional cases are tested to ensure full branch coverage.</p>
 */
class ShareTest {

  private Stock stock;

  /**
   * Creates a reusable stock instance with a known current market price
   * to simplify value calculations in tests.
   */
  @BeforeEach
  void setup() {
    stock = new Stock("AAPL", "Apple", new BigDecimal("200"));
  }

  /**
   * Verifies that a valid construction stores and exposes fields correctly.
   */
  @Test
  void constructor_validInput_createsShare() {
    Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("150"));

    assertEquals(stock, share.getStock());
    assertEquals(new BigDecimal("10"), share.getQuantity());
    assertEquals(new BigDecimal("150"), share.getPurchasePrice());
  }

  /**
   * Ensures the associated stock reference is mandatory.
   */
  @Test
  void constructor_nullStock_throwsException() {
    assertThrows(NullPointerException.class,
        () -> new Share(null, BigDecimal.ONE, BigDecimal.ONE));
  }

  /**
   * Ensures quantity is mandatory.
   */
  @Test
  void constructor_nullQuantity_throwsException() {
    assertThrows(NullPointerException.class,
        () -> new Share(stock, null, BigDecimal.ONE));
  }

  /**
   * Ensures quantity must be strictly positive.
   */
  @Test
  void constructor_zeroQuantity_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> new Share(stock, BigDecimal.ZERO, BigDecimal.ONE));
  }

  /**
   * Ensures negative quantity is rejected.
   */
  @Test
  void constructor_negativeQuantity_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> new Share(stock, new BigDecimal("-1"), BigDecimal.ONE));
  }

  /**
   * Ensures purchase price is mandatory.
   */
  @Test
  void constructor_nullPrice_throwsException() {
    assertThrows(NullPointerException.class,
        () -> new Share(stock, BigDecimal.ONE, null));
  }

  /**
   * Ensures purchase price cannot be negative.
   */
  @Test
  void constructor_negativePrice_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> new Share(stock, BigDecimal.ONE, new BigDecimal("-1")));
  }

  /**
   * Verifies total investment equals purchase price multiplied by quantity.
   */
  @Test
  void getTotalInvestment_returnsCorrectValue() {
    Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("150"));

    assertEquals(new BigDecimal("1500"), share.getTotalInvestment());
  }

  /**
   * Verifies current value equals current stock price multiplied by quantity.
   */
  @Test
  void getCurrentValue_returnsCorrectValue() {
    Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("150"));

    assertEquals(new BigDecimal("2000"), share.getCurrentValue());
  }

  /**
   * Verifies gain/loss equals current value minus original investment.
   */
  @Test
  void getGainOrLoss_returnsCorrectValue() {
    Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("150"));

    assertEquals(new BigDecimal("500"), share.getGainOrLoss());
  }

  /**
   * Verifies value equality is numeric (scale-insensitive) rather than based on
   * {@link BigDecimal#equals(Object)} (which is scale-sensitive).
   *
   * <p>This guards against cases such as 10.0 and 10.00 being considered different
   * when they represent the same numeric value.</p>
   */
  @Test
  void equals_sameValuesDifferentScale_returnsTrue() {
    Share s1 = new Share(stock,
        new BigDecimal("10.0"),
        new BigDecimal("150.00"));

    Share s2 = new Share(stock,
        new BigDecimal("10.00"),
        new BigDecimal("150.0"));

    assertEquals(s1, s2);
    assertEquals(s1.hashCode(), s2.hashCode());
  }

  /**
   * Ensures two shares with different underlying stocks are not equal,
   * even if the numeric fields match.
   */
  @Test
  void equals_differentStock_returnsFalse() {
    Stock other = new Stock("TSLA", "Tesla", BigDecimal.TEN);

    Share s1 = new Share(stock, BigDecimal.ONE, BigDecimal.ONE);
    Share s2 = new Share(other, BigDecimal.ONE, BigDecimal.ONE);

    assertNotEquals(s1, s2);
  }

  /**
   * Ensures equals safely handles null.
   */
  @Test
  void equals_null_returnsFalse() {
    Share share = new Share(stock, BigDecimal.ONE, BigDecimal.ONE);
    assertNotEquals(null, share);
  }

  /**
   * Ensures equals returns false for unrelated types.
   */
  @Test
  void equals_differentType_returnsFalse() {
    Share share = new Share(stock, BigDecimal.ONE, BigDecimal.ONE);
    assertNotEquals("not share", share);
  }
}