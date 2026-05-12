package edu.ntnu.idi.idatt.millions.model.transaction;

import edu.ntnu.idi.idatt.millions.model.Share;
import edu.ntnu.idi.idatt.millions.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SaleCalculator}.
 *
 * <p>The tests verify that the calculator correctly computes financial
 * values for share sale transactions including:</p>
 *
 * <ul>
 *   <li>Gross sale value</li>
 *   <li>Broker commission</li>
 *   <li>Tax on profit</li>
 *   <li>Total value received</li>
 * </ul>
 *
 * <p>The tests also verify correct behavior when the transaction results
 * in a loss (no tax applied) and when invalid input is provided.</p>
 */
class SaleCalculatorTest {

  private SaleCalculator calculator;
  private Share profitableShare;

  /**
   * Creates a share that will generate a profit when sold.
   */
  @BeforeEach
  void setUp() {

    Stock stock = new Stock("AAPL", "Apple Inc.", new BigDecimal("200"));
    profitableShare = new Share(stock, new BigDecimal("10"), new BigDecimal("100"));

    calculator = new SaleCalculator(profitableShare);
  }

  /**
   * Verifies that the gross sale value is calculated correctly.
   *
   * <p>Formula: salesPrice × quantity</p>
   */
  @Test
  void calculateGross_returnsCorrectValue() {

    BigDecimal expected = new BigDecimal("2000");

    BigDecimal result = calculator.calculateGross();

    assertEquals(expected, result);
  }

  /**
   * Verifies that the broker commission is calculated correctly.
   *
   * <p>Formula: 1% of gross</p>
   */
  @Test
  void calculateCommission_returnsCorrectValue() {

    BigDecimal expected = new BigDecimal("20.00");

    BigDecimal result = calculator.calculateCommission();

    assertEquals(expected, result);
  }

  /**
   * Verifies that tax is calculated correctly when the sale produces a profit.
   *
   * <p>Profit = gross − commission − purchase value</p>
   * <p>Tax = 30% of profit</p>
   */
  @Test
  void calculateTax_profitScenario_returnsCorrectTax() {

    BigDecimal expected = new BigDecimal("294.0");

    BigDecimal result = calculator.calculateTax();

    assertEquals(0, expected.compareTo(result));
  }

  /**
   * Verifies that no tax is applied when the transaction results in a loss.
   */
  @Test
  void calculateTax_lossScenario_returnsZeroTax() {

    Stock stock = new Stock("AAPL", "Apple Inc.", new BigDecimal("50"));
    Share lossShare = new Share(stock, new BigDecimal("10"), new BigDecimal("100"));

    SaleCalculator lossCalculator = new SaleCalculator(lossShare);

    BigDecimal expected = BigDecimal.ZERO;

    BigDecimal result = lossCalculator.calculateTax();

    assertEquals(expected, result);
  }

  /**
   * Verifies that the total received value is calculated correctly.
   *
   * <p>Formula: gross − commission − tax</p>
   */
  @Test
  void calculateTotal_returnsCorrectValue() {

    BigDecimal expected = new BigDecimal("1686");

    BigDecimal result = calculator.calculateTotal();

    assertEquals(0, expected.compareTo(result));
  }

  /**
   * Ensures the constructor rejects null input.
   */
  @Test
  void constructor_nullShare_throwsException() {

    assertThrows(NullPointerException.class,
        () -> new SaleCalculator(null));
  }
}