package edu.ntnu.idi.idatt.millions.observer;

import edu.ntnu.idi.idatt.millions.model.Exchange;

/**
 * Observer interface for receiving notifications when an {@link Exchange} changes state.
 *
 * <p>Observers register themselves on an {@link Exchange} and are notified whenever
 * the exchange advances to the next trading week (i.e. stock prices are updated).</p>
 *
 * <p>Typical implementors are GUI views that display stock prices or the current week,
 * such as a trading view or a sidebar showing the week counter.</p>
 *
 * @see Exchange
 * @see PortfolioObserver
 * @see PlayerObserver
 */
public interface ExchangeObserver {

  /**
   * Called when the exchange has advanced to a new trading week.
   *
   * <p>Implementations should refresh any displayed stock prices or
   * week-dependent information using the data available on {@code exchange}.</p>
   *
   * @param exchange the exchange that changed, never null
   */
  void onExchangeUpdated(Exchange exchange);
}
