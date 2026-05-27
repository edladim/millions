package edu.ntnu.idi.idatt.millions.model.player;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ScoreCalculator}.
 *
 * <p>Verifies the scoring formula including:
 *
 * <ul>
 *   <li>Wipe-out edge case ({@code returnRate <= -1.0} gives zero)
 *   <li>Non-negativity guarantee
 *   <li>Higher returns produce higher scores
 *   <li>More weeks produce higher survival bonuses
 *   <li>The minimum-weeks clamp prevents short-game inflation
 *   <li>Known exact values (hand-computed from the formula)
 * </ul>
 */
class ScoreCalculatorTest {

  /** Verifies that a complete wipe-out ({@code returnRate = -1.0}) scores zero. */
  @Test
  void compute_exactWipeOut_returnsZero() {
    assertEquals(0L, ScoreCalculator.compute(new BigDecimal("-1.0"), 52));
  }

  /** Verifies that a return below -100% also scores zero. */
  @Test
  void compute_belowWipeOut_returnsZero() {
    assertEquals(0L, ScoreCalculator.compute(new BigDecimal("-2.0"), 100));
  }

  /**
   * Verifies that a zero return with no weeks played scores zero. (No growth → zero performance; no
   * weeks → zero survival bonus.)
   */
  @Test
  void compute_zeroReturnZeroWeeks_returnsZero() {
    assertEquals(0L, ScoreCalculator.compute(BigDecimal.ZERO, 0));
  }

  /** Verifies that score is always non-negative. */
  @Test
  void compute_anyInput_isNonNegative() {
    long[] scores = {
      ScoreCalculator.compute(new BigDecimal("-0.99"), 1),
      ScoreCalculator.compute(new BigDecimal("-0.50"), 10),
      ScoreCalculator.compute(BigDecimal.ZERO, 0),
      ScoreCalculator.compute(new BigDecimal("0.10"), 26),
    };
    for (long score : scores) {
      assertTrue(score >= 0, "Expected non-negative score but got " + score);
    }
  }

  /** Verifies that a higher return rate produces a higher score, all else equal. */
  @Test
  void compute_higherReturn_givesHigherScore() {
    long lowScore = ScoreCalculator.compute(new BigDecimal("0.20"), 52);
    long highScore = ScoreCalculator.compute(new BigDecimal("1.00"), 52);
    assertTrue(highScore > lowScore, "Expected 100% return to score higher than 20% return");
  }

  /**
   * Verifies that more weeks produce a higher survival bonus when the return rate is zero.
   *
   * <p>With zero return the performance component is always zero, so the difference in score is
   * driven entirely by the survival bonus, which grows with weeks played.
   */
  @Test
  void compute_moreWeeks_givesHigherSurvivalBonus() {
    long shortGame = ScoreCalculator.compute(BigDecimal.ZERO, 26);
    long longGame = ScoreCalculator.compute(BigDecimal.ZERO, 104);
    assertTrue(
        longGame > shortGame,
        "Expected longer game to score higher than shorter game at the same zero return rate");
  }

  /**
   * Verifies that the minimum-weeks clamp prevents a short game from scoring <em>higher</em> than
   * the equivalent 26-week game.
   *
   * <p>The clamp applies only to the annualisation (performance component); the survival component
   * still uses the actual week count. Therefore a 1-week game must score lower than a 26-week game
   * because it has the same performance score but a smaller survival bonus.
   */
  @Test
  void compute_shortGameClampedToMinWeeks_doesNotInflateAbove26Weeks() {
    long oneWeek = ScoreCalculator.compute(new BigDecimal("1.00"), 1);
    long twentySixWeeks = ScoreCalculator.compute(new BigDecimal("1.00"), 26);
    assertTrue(
        oneWeek < twentySixWeeks,
        "1-week game should score lower than 26-week game due to smaller survival bonus");
  }

  /**
   * Verifies a known score for a zero-return, 52-week run.
   *
   * <p>Formula (hand-computed):
   *
   * <pre>
   *   effectiveWeeks = max(26, 52) = 52
   *   annualReturn   = (1 + 0)^(52/52) − 1 = 0
   *   performance    = 1000 · log10(1) = 0
   *   preservation   = min(1, max(0, 1)) = 1
   *   survival       = 50 · log10(53) · 1 ≈ 86.2
   *   score          = round(86.2) = 86
   * </pre>
   */
  @Test
  void compute_zeroReturnFiftyTwoWeeks_returnsExpectedScore() {
    assertEquals(86L, ScoreCalculator.compute(BigDecimal.ZERO, 52));
  }

  /**
   * Verifies a known score for a 100% return over 52 weeks.
   *
   * <p>Formula (hand-computed):
   *
   * <pre>
   *   effectiveWeeks = 52
   *   annualReturn   = 2^(52/52) − 1 = 1.0
   *   performance    = 1000 · log10(2) ≈ 301.0
   *   preservation   = 1
   *   survival       = 50 · log10(53) ≈ 86.2
   *   score          = round(387.2) = 387
   * </pre>
   */
  @Test
  void compute_hundredPercentReturnFiftyTwoWeeks_returnsExpectedScore() {
    assertEquals(387L, ScoreCalculator.compute(new BigDecimal("1.00"), 52));
  }

  /**
   * Verifies that a large loss (but not a full wipe-out) gives a lower score than break-even, due
   * to the reduced preservation factor.
   */
  @Test
  void compute_largeLoss_scoresLowerThanBreakEven() {
    long bigLoss = ScoreCalculator.compute(new BigDecimal("-0.80"), 52);
    long breakEven = ScoreCalculator.compute(BigDecimal.ZERO, 52);
    assertTrue(bigLoss < breakEven, "Expected large loss to score lower than break-even");
  }
}
