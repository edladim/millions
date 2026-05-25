package edu.ntnu.idi.idatt.millions.controller;

import edu.ntnu.idi.idatt.millions.model.market.Exchange;
import edu.ntnu.idi.idatt.millions.model.player.Player;
import edu.ntnu.idi.idatt.millions.model.player.ScoreCalculator;
import edu.ntnu.idi.idatt.millions.model.portfolio.Share;
import edu.ntnu.idi.idatt.millions.persistence.leaderboard.LeaderboardStore;
import edu.ntnu.idi.idatt.millions.view.MainView;
import edu.ntnu.idi.idatt.millions.view.pages.Page;
import java.util.ArrayList;
import java.util.OptionalInt;

/**
 * Top-level controller for an active game.
 *
 * <p>Wires the sidebar to the underlying models, instantiates one sub-controller per page (each
 * owning its own observer registrations and view callbacks), handles week advancement, and triggers
 * the end-game sequence when the simulation runs out.
 */
public class GameController {

  /** Number of weeks before the game ends and the end-game overlay is shown. */
  private static final int END_OF_GAME_WEEK = 520;

  private final MainView view;
  private final Player player;
  private final Exchange exchange;
  private final TradingController tradingController;
  private boolean gameOver = false;

  /**
   * Creates the game controller, registers global observers, and sets up the dashboard, trading,
   * and portfolio sub-controllers.
   *
   * @param view the main view to drive.
   * @param player the active player model.
   * @param exchange the active exchange model.
   */
  public GameController(MainView view, Player player, Exchange exchange) {
    this.view = view;
    this.player = player;
    this.exchange = exchange;

    exchange.addObserver(view.getSidebar());
    view.getSidebar().onExchangeUpdated(exchange);

    tradingController = new TradingController(view.getTradingView(), exchange, player);
    new PortfolioController(view.getPortfolioView(), exchange, player);
    new DashboardController(
        view.getDashboardView(),
        exchange,
        player,
        symbol -> {
          view.navigateTo(Page.TRADING);
          tradingController.focusStock(symbol);
        });

    view.setOnAdvanceWeek(this::advanceWeek);
    view.setOnRetire(this::retire);
  }

  /**
   * Liquidates the player's entire portfolio and ends the game immediately.
   *
   * <p>Iterates over a snapshot of the share lots so the underlying list can be safely mutated by
   * each sell. Individual sell failures are swallowed since the player is retiring — the final
   * score reflects whatever was successfully sold plus the remaining cash.
   */
  public void retire() {
    if (gameOver) {
      return;
    }
    for (Share share : new ArrayList<>(player.getPortfolio().getShares())) {
      try {
        exchange.sell(share, player);
      } catch (IllegalStateException | IllegalArgumentException ignore) {
        // Expected: a lot cannot be sold right now.
        // Skip it and let the rest of the liquidation proceed.
      }
    }
    gameOver = true;
    endGame();
  }

  /**
   * Advances the exchange by one week and snapshots the player's net worth.
   *
   * <p>{@code exchange.advance()} notifies all {@link
   * edu.ntnu.idi.idatt.millions.observer.ExchangeObserver}s, and {@code
   * player.updateHistoricalNetWorth()} notifies all {@link
   * edu.ntnu.idi.idatt.millions.observer.PlayerObserver}s — so all views refresh automatically
   * without a manual push.
   *
   * <p>When the exchange reaches {@value #END_OF_GAME_WEEK}, the game ends and the end-game overlay
   * is shown.
   */
  public void advanceWeek() {
    if (gameOver) {
      return;
    }
    exchange.advance();
    player.updateHistoricalNetWorth();

    if (exchange.getWeek() >= END_OF_GAME_WEEK) {
      gameOver = true;
      endGame();
    }
  }

  /** Computes the final score and asks the view to render the end-game overlay. */
  private void endGame() {
    long score = ScoreCalculator.compute(player.getReturnRate(), exchange.getWeek());
    OptionalInt rank = LeaderboardStore.submit(player.getName(), score);
    view.showEndGame(
        player.getNetWorth(),
        player.getProfit(),
        player.getReturnRate(),
        exchange.getWeek(),
        score,
        rank);
  }
}
