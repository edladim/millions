package edu.ntnu.idi.idatt.millions.model.market;

import java.math.BigDecimal;
import java.util.Random;

/**
 * Strategy interface for computing the next sales price of a stock.
 *
 * <p>Implementations define the price-movement model used by the exchange when advancing to the
 * next trading week. A new price is computed for each stock individually, allowing stateful models
 * to track per-symbol history.
 *
 * <p>The exchange calls {@link #beginWeek(Random)} exactly once before iterating over stocks,
 * giving implementations a chance to compute week-level state such as a market-wide sentiment
 * shock.
 */
public interface PriceFluctuator {

  /**
   * Called by the exchange once at the start of each week, before any {@link #nextPrice} calls for
   * that week.
   *
   * <p>Stateful implementations override this to generate week-level values, like a market-wide
   * shock, that should be the same for all stocks within one advance cycle. The default
   * implementation is a no-op.
   *
   * @param random shared {@link Random} instance provided by the exchange
   */
  default void beginWeek(Random random) {}

  /**
   * Computes the next sales price for a single stock.
   *
   * @param symbol the stock's ticker symbol, used by stateful models to look up per-stock state
   * @param currentPrice the stock's current sales price
   * @param random shared {@link Random} instance provided by the exchange
   * @return the new sales price; must be positive
   */
  BigDecimal nextPrice(String symbol, BigDecimal currentPrice, Random random);
}
