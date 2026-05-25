package edu.ntnu.idi.idatt.millions.model.player;

import java.math.BigDecimal;

/**
 * <p>Utility for computing the player's "Millions Score" — a single number
 * that rewards both <em>how fast</em> the player grew their capital and
 * <em>how long</em> they survived in the market.</p>
 *
 * <p>The score is the sum of two components:</p>
 * <ul>
 *   <li><b>Performance</b> — based on the annualised return rate, so a player
 *       who doubles their capital in one year scores far higher than one who
 *       doubles over ten years.</li>
 *   <li><b>Survival</b> — a logarithmic bonus for weeks played, scaled by how
 *       much of the starting capital the player preserved. A long career that
 *       weathered downturns still earns a meaningful score even if the
 *       annualised rate is modest, but losing most of the capital reduces the
 *       bonus proportionally — surviving with empty pockets isn't really
 *       surviving.</li>
 * </ul>
 *
 * <p>A floor of {@value #MIN_WEEKS_FOR_ANNUALISATION} weeks is applied when
 * annualising so a single lucky week cannot inflate the rate to an absurd
 * value. Players who lose everything ({@code returnRate <= -100%}) score 0.</p>
 *
 * <p>This class cannot be instantiated; use the static method directly.</p>
 */
public final class ScoreCalculator {

  /** Weeks per year used when annualising the return rate. */
  private static final double WEEKS_PER_YEAR = 52.0;

  /** Minimum effective week count used when annualising, to prevent very
   *  short games from inflating the rate. */
  private static final int MIN_WEEKS_FOR_ANNUALISATION = 26;

  /** Multiplier on the performance component (annualised return). */
  private static final int PERFORMANCE_WEIGHT = 1000;

  /** Multiplier on the survival component (weeks played). */
  private static final int SURVIVAL_WEIGHT = 50;

  private ScoreCalculator() {}

  /**
   * <p>Computes the Millions Score from the player's total return rate and
   * the number of weeks they played.</p>
   *
   * <p>Formula:</p>
   * <pre>
   *   effectiveWeeks = max(MIN_WEEKS_FOR_ANNUALISATION, weeks)
   *   annualReturn = (1 + returnRate)^(52 / effectiveWeeks) − 1
   *   preservationFactor = min(1, max(0, 1 + returnRate))
   *   performance = 1000 · log10(1 + max(0, annualReturn))
   *   survival = 50 · log10(1 + weeks) · preservationFactor
   *   score = max(0, round(performance + survival))
   * </pre>
   *
   * @param returnRate the player's total return rate as a decimal
   *                   (e.g. {@code 0.20} for a 20% gain)
   * @param weeks      the number of weeks the player has played
   * @return the rounded, non-negative score
   */
  public static long compute(BigDecimal returnRate, int weeks) {
    double r = returnRate.doubleValue();

    // A wipe-out cannot be annualised: score is 0.
    if (r <= -1.0) return 0L;

    double effectiveWeeks = Math.max(MIN_WEEKS_FOR_ANNUALISATION, weeks);
    double annualReturn = Math.pow(1 + r, WEEKS_PER_YEAR / effectiveWeeks) - 1;

    // Fraction of starting capital still in the player's hands, capped at 1
    // so gains do not double-count (those are rewarded via performance).
    double preservationFactor = Math.min(1.0, Math.max(0.0, 1 + r));

    double performance = PERFORMANCE_WEIGHT * Math.log10(1 + Math.max(0, annualReturn));
    double survival    = SURVIVAL_WEIGHT    * Math.log10(1 + weeks) * preservationFactor;

    return Math.max(0L, Math.round(performance + survival));
  }
}
