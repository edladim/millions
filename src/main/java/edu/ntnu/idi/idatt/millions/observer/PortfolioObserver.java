package edu.ntnu.idi.idatt.millions.observer;

import edu.ntnu.idi.idatt.millions.model.Portfolio;

/**
 * Observer interface for receiving notifications when a {@link Portfolio} changes state.
 *
 * <p>Observers register themselves on a {@link Portfolio} and are notified whenever
 * shares are added to or removed from it.</p>
 *
 * <p>Typical implementors are GUI views that display portfolio holdings or
 * portfolio-derived metrics such as total value and gain/loss.</p>
 *
 * @see Portfolio
 * @see ExchangeObserver
 * @see PlayerObserver
 */
public interface PortfolioObserver {

  /**
   * Called when the portfolio's contents have changed.
   *
   * <p>Implementations should refresh any displayed share listings or
   * portfolio metrics using the data available on {@code portfolio}.</p>
   *
   * @param portfolio the portfolio that changed, never null
   */
  void onPortfolioUpdated(Portfolio portfolio);
}
