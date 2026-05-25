package edu.ntnu.idi.idatt.millions.model.transaction;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CostPreviewCalculator}.
 *
 * <p>Verifies that the gross, commission, and total are computed correctly for both buy and sell
 * orders, including the following properties:
 *
 * <ul>
 *   <li>Gross = price × quantity
 *   <li>Commission = gross × {@link CostPreviewCalculator#COMMISSION_RATE} (0.5 %)
 *   <li>Buy total = gross + commission
 *   <li>Sell total = gross − commission
 * </ul>
 */
class CostPreviewCalculatorTest {

  /** Verifies that the gross is the product of price and quantity. */
  @Test
  void constructor_buy_grossIsProductOfPriceAndQuantity() {
    CostPreviewCalculator calc =
        new CostPreviewCalculator(new BigDecimal("100"), new BigDecimal("10"), true);
    assertEquals(
        0, new BigDecimal("1000").compareTo(calc.getGross()), "Expected gross = 100 × 10 = 1000");
  }

  /** Verifies that the commission is 0.5 % of the gross for a buy order. */
  @Test
  void constructor_buy_commissionIsHalfPercentOfGross() {
    CostPreviewCalculator calc =
        new CostPreviewCalculator(new BigDecimal("100"), new BigDecimal("10"), true);
    assertEquals(
        0,
        new BigDecimal("5").compareTo(calc.getCommission()),
        "Expected commission = 1000 × 0.005 = 5");
  }

  /** Verifies that the total for a buy order equals gross + commission. */
  @Test
  void constructor_buy_totalIsGrossPlusCommission() {
    CostPreviewCalculator calc =
        new CostPreviewCalculator(new BigDecimal("100"), new BigDecimal("10"), true);
    assertEquals(
        0,
        new BigDecimal("1005").compareTo(calc.getTotal()),
        "Expected buy total = 1000 + 5 = 1005");
  }

  /** Verifies that the total for a sell order equals gross − commission. */
  @Test
  void constructor_sell_totalIsGrossMinusCommission() {
    CostPreviewCalculator calc =
        new CostPreviewCalculator(new BigDecimal("100"), new BigDecimal("10"), false);
    assertEquals(
        0,
        new BigDecimal("995").compareTo(calc.getTotal()),
        "Expected sell total = 1000 − 5 = 995");
  }

  /** Verifies that the commission is the same regardless of order direction. */
  @Test
  void constructor_sell_commissionEqualsCommissionForBuy() {
    CostPreviewCalculator buy =
        new CostPreviewCalculator(new BigDecimal("200"), new BigDecimal("4"), true);
    CostPreviewCalculator sell =
        new CostPreviewCalculator(new BigDecimal("200"), new BigDecimal("4"), false);
    assertEquals(
        0,
        buy.getCommission().compareTo(sell.getCommission()),
        "Commission should be the same for buy and sell at the same gross");
  }

  /** Verifies that a single-share trade computes correct values. */
  @Test
  void constructor_singleShare_buy_correctBreakdown() {
    // price=50, qty=1 → gross=50, commission=0.25, total=50.25
    CostPreviewCalculator calc =
        new CostPreviewCalculator(new BigDecimal("50"), new BigDecimal("1"), true);
    assertEquals(0, new BigDecimal("50").compareTo(calc.getGross()));
    assertEquals(0, new BigDecimal("0.25").compareTo(calc.getCommission()));
    assertEquals(0, new BigDecimal("50.25").compareTo(calc.getTotal()));
  }

  /** Verifies that the commission rate constant is exactly 0.005. */
  @Test
  void commissionRate_isHalfPercent() {
    assertEquals(0, new BigDecimal("0.005").compareTo(CostPreviewCalculator.COMMISSION_RATE));
  }
}
