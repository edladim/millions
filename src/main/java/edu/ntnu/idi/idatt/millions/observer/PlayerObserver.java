package edu.ntnu.idi.idatt.millions.observer;

import edu.ntnu.idi.idatt.millions.model.Player;

/**
 * Observer interface for receiving notifications when a {@link Player} changes state.
 *
 * <p>Observers register themselves on a {@link Player} and are notified whenever
 * the player's money balance changes (i.e. after a purchase or sale).</p>
 *
 * <p>Typical implementors are GUI views that display the player's cash balance,
 * net worth, return rate, or status.</p>
 *
 * @see Player
 * @see ExchangeObserver
 * @see PortfolioObserver
 */
public interface PlayerObserver {

  /**
   * Called when the player's state has changed.
   *
   * <p>Implementations should refresh any displayed balance, net worth,
   * or status information using the data available on {@code player}.</p>
   *
   * @param player the player whose state changed, never null
   */
  void onPlayerUpdated(Player player);
}
