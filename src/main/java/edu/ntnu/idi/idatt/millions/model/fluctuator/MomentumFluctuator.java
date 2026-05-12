package edu.ntnu.idi.idatt.millions.model.fluctuator;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * <p>A stateful price model combining momentum, per-stock volatility,
 * fat-tail events, and a market-wide sentiment shock.</p>
 *
 * <h2>Per-stock momentum</h2>
 * <p>Each stock maintains a hidden <em>momentum</em> value that carries over
 * between weeks. Momentum fades toward zero each week (mean-reversion via
 * {@code DECAY}) but is continuously perturbed by random shocks, producing
 * trend phases of roughly 3–6 weeks that are visible in the price history.</p>
 *
 * <h2>Per-stock volatility</h2>
 * <p>Each stock is assigned a random volatility multiplier on first contact,
 * in the range {@code [VOL_MIN, VOL_MAX]}. High-volatility stocks swing more
 * per week; low-volatility stocks move slowly and steadily.</p>
 *
 * <h2>Fat-tail events (per stock)</h2>
 * <p>With probability {@code FAT_TAIL_PROB} each week, a stock receives an
 * additional large shock (±{@code FAT_TAIL_AMP}, scaled by volatility),
 * modelling sudden company-specific news that can break or reverse a trend.</p>
 *
 * <h2>Market-wide sentiment</h2>
 * <p>Once per week, a market shock is sampled and applied to every stock's
 * momentum update. A small baseline sentiment (±{@code MARKET_BASE_AMP})
 * creates mild weekly correlation between all stocks. With probability
 * {@code MARKET_EVENT_PROB}, a large market event (±{@code MARKET_EVENT_AMP})
 * is added on top, pushing all stocks into a sustained bull or bear phase for
 * several weeks before momentum decays back to zero.</p>
 *
 * <h2>Momentum cap</h2>
 * <p>After each momentum update, the value is clamped to
 * {@code [−MOMENTUM_CAP, +MOMENTUM_CAP]} to prevent compounding extreme shocks
 * (especially fat-tail and market events) from producing unrealistic weekly
 * price swings.</p>
 *
 * <h2>Formula (per stock per week)</h2>
 * <pre>
 *   shock      = stockShock(vol) [+ fatTailShock(vol) with prob FAT_TAIL_PROB]
 *                + marketShock (same for all stocks this week)
 *   momentum'  = clamp(momentum × DECAY + shock, −CAP, +CAP)
 *   change     = momentum' + noise(vol)
 *   newPrice   = currentPrice × (1 + change)  [floor: $1.00]
 * </pre>
 */
public final class MomentumFluctuator implements PriceFluctuator {

  // Momentum
  private static final double DECAY = 0.7;
  private static final double MOMENTUM_CAP = 0.15;

  // Per-stock base shock and noise (scaled by per-stock volatility)
  private static final double BASE_SHOCK = 0.03;
  private static final double BASE_NOISE = 0.01;

  // Per-stock fat-tail events
  private static final double FAT_TAIL_PROB = 0.02;
  private static final double FAT_TAIL_AMP = 0.15;

  // Market-wide sentiment (computed once per week, shared by all stocks)
  private static final double MARKET_BASE_AMP = 0.02;
  private static final double MARKET_EVENT_PROB = 0.03;
  private static final double MARKET_EVENT_AMP = 0.15;

  // Per-stock volatility multiplier range
  private static final double VOL_MIN = 0.5;
  private static final double VOL_MAX = 2.0;

  private static final BigDecimal FLOOR = BigDecimal.ONE;

  private final Map<String, Double> momentum   = new HashMap<>();
  private final Map<String, Double> volatility = new HashMap<>();

  private double marketShock = 0.0;

  /**
   * {@inheritDoc}
   *
   * <p>Samples a market-wide sentiment shock for the coming week.
   * A small baseline keeps mild correlation between all stocks every week.
   * With {@code MARKET_EVENT_PROB} probability an additional large shock is
   * added, creating a sustained bull or bear phase across the whole market.</p>
   */
  @Override
  public void beginWeek(Random random) {
    marketShock = (random.nextDouble() - 0.5) * 2 * MARKET_BASE_AMP;
    if (random.nextDouble() < MARKET_EVENT_PROB) {
      marketShock += (random.nextDouble() - 0.5) * 2 * MARKET_EVENT_AMP;
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>The symbol is used to retrieve the per-stock
   * momentum and volatility state.</p>
   */
  @Override
  public BigDecimal nextPrice(String symbol, BigDecimal currentPrice, Random random) {
    double vol = volatility.computeIfAbsent(
        symbol, k -> VOL_MIN + random.nextDouble() * (VOL_MAX - VOL_MIN));

    double m = momentum.getOrDefault(symbol, 0.0);

    double shock = (random.nextDouble() - 0.5) * 2 * BASE_SHOCK * vol;
    if (random.nextDouble() < FAT_TAIL_PROB) {
      shock += (random.nextDouble() - 0.5) * 2 * FAT_TAIL_AMP * vol;
    }
    shock += marketShock;

    m = Math.clamp(m * DECAY + shock, -MOMENTUM_CAP, MOMENTUM_CAP);
    momentum.put(symbol, m);

    double noise  = (random.nextDouble() - 0.5) * 2 * BASE_NOISE * vol;
    double change = m + noise;

    BigDecimal newPrice = currentPrice.multiply(BigDecimal.valueOf(1 + change));
    return newPrice.compareTo(FLOOR) < 0 ? FLOOR : newPrice;
  }
}
