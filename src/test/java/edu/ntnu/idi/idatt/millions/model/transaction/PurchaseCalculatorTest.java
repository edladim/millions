package edu.ntnu.idi.idatt.millions.model.transaction;

import static org.junit.jupiter.api.Assertions.*;

import edu.ntnu.idi.idatt.millions.model.market.Stock;
import edu.ntnu.idi.idatt.millions.model.portfolio.Share;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PurchaseCalculator}.
 *
 * <p>The tests verify that the calculator correctly computes financial values for purchase
 * transactions including:
 *
 * <ul>
 *   <li>Gross purchase value
 *   <li>Broker commission
 *   <li>Tax (should always be zero)
 *   <li>Total transaction cost
 * </ul>
 *
 * <p>The tests also verify constructor validation and ensure that all calculations follow the
 * financial rules defined for purchases.
 */
class PurchaseCalculatorTest {

  private PurchaseCalculator calculator;
  private Share share;

  /** Creates a sample share used for testing purchase calculations. */
  @BeforeEach
  void setUp() {

    Stock stock = new Stock("AAPL", "Apple Inc.", new BigDecimal("200"));
    share = new Share(stock, new BigDecimal("10"), new BigDecimal("100"));

    calculator = new PurchaseCalculator(share);
  }

  /**
   * Verifies that the gross purchase value is calculated correctly.
   *
   * <p>Formula: purchasePrice × quantity
   */
  @Test
  void calculateGross_returnsCorrectValue() {

    BigDecimal expected = new BigDecimal("1000");

    BigDecimal result = calculator.calculateGross();

    assertEquals(0, expected.compareTo(result));
  }

  /**
   * Verifies that the broker commission is calculated correctly.
   *
   * <p>Formula: 0.5% of gross value
   */
  @Test
  void calculateCommission_returnsCorrectValue() {

    BigDecimal expected = new BigDecimal("5.0");

    BigDecimal result = calculator.calculateCommission();

    assertEquals(0, expected.compareTo(result));
  }

  /** Verifies that purchase transactions never incur tax. */
  @Test
  void calculateTax_returnsZero() {

    BigDecimal result = calculator.calculateTax();

    assertEquals(BigDecimal.ZERO, result);
  }

  /**
   * Verifies that the total transaction cost is calculated correctly.
   *
   * <p>Formula: gross + commission
   */
  @Test
  void calculateTotal_returnsCorrectValue() {

    BigDecimal expected = new BigDecimal("1005.0");

    BigDecimal result = calculator.calculateTotal();

    assertEquals(0, expected.compareTo(result));
  }

  /** Ensures that the constructor rejects null input. */
  @Test
  void constructor_nullShare_throwsException() {

    assertThrows(NullPointerException.class, () -> new PurchaseCalculator(null));
  }
}
