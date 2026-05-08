package edu.ntnu.idi.idatt.millions.observer;

import edu.ntnu.idi.idatt.millions.model.ReadOnlyPortfolio;

/**
 * Observer interface for receiving notifications when a portfolio changes state.
 *
 * <p>Observers register themselves on a {@link edu.ntnu.idi.idatt.millions.model.Portfolio}
 * and are notified whenever shares are added to or removed from it.</p>
 *
 * <p>The callback receives a {@link ReadOnlyPortfolio} to prevent observers from
 * mutating portfolio contents.</p>
 *
 * <p>Typical implementors are GUI views that display portfolio holdings or
 * portfolio-derived metrics such as total value and gain/loss.</p>
 *
 * @see ReadOnlyPortfolio
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
   * @param portfolio a read-only view of the portfolio that changed, never null
   */
  void onPortfolioUpdated(ReadOnlyPortfolio portfolio);
}
