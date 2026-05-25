package edu.ntnu.idi.idatt.millions.observer;

import edu.ntnu.idi.idatt.millions.model.player.ReadOnlyPlayer;

/**
 * Observer interface for receiving notifications when a player changes state.
 *
 * <p>Observers register themselves on a {@link edu.ntnu.idi.idatt.millions.model.player.Player}
 * and are notified whenever the player's money balance changes
 * (i.e. after a purchase or sale).</p>
 *
 * <p>The callback receives a {@link ReadOnlyPlayer} to prevent observers from
 * mutating the player's state.</p>
 *
 * <p>Typical implementors are GUI views that display the player's cash balance,
 * net worth, return rate, or status.</p>
 *
 * @see ReadOnlyPlayer
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
   * @param player a read-only view of the player whose state changed, never null
   */
  void onPlayerUpdated(ReadOnlyPlayer player);
}
