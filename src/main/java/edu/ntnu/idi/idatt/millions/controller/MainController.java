package edu.ntnu.idi.idatt.millions.controller;

import edu.ntnu.idi.idatt.millions.model.Exchange;
import edu.ntnu.idi.idatt.millions.model.Player;
import edu.ntnu.idi.idatt.millions.model.Share;
import edu.ntnu.idi.idatt.millions.view.MainView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

/**
 * <p>Top-level controller that wires the game models to all views.</p>
 * <p>Registers observers, creates sub-controllers for each page, and
 * handles week advancement.</p>
 */
public class MainController {

  private final MainView view;
  private final Player player;
  private final Exchange exchange;
  private final TradingController tradingController;
  private boolean gameOver = false;
  private double millionsScore;

  /**
   * <p>Creates a main controller, registers all observers, and sets up
   * page controllers for trading and portfolio.</p>
   *
   * @param view main view to update.
   * @param player active player model.
   * @param exchange active exchange model.
   */
  public MainController(MainView view,
                        Player player,
                        Exchange exchange) {
    this.view = view;
    this.player = player;
    this.exchange = exchange;

    registerObservers();

    tradingController = new TradingController(view.getTradingView(), exchange, player);
                        new PortfolioController(view.getPortfolioView(), exchange, player);

    view.setOnAdvanceWeek(this::advanceWeek);

    view.setOnSellAllEndGame(this::sellAllEndGame);

    initializeViews(exchange, player);
  }

  /**
   * <p>Advances the exchange by one week and snapshots the player's net worth.</p>
   * <p>{@code exchange.advance()} notifies all {@link edu.ntnu.idi.idatt.millions.observer.ExchangeObserver}s,
   * and {@code player.updateHistoricalNetWorth()} notifies all
   * {@link edu.ntnu.idi.idatt.millions.observer.PlayerObserver}s — so all views
   * refresh automatically without a manual push.</p>
   * <p>When the exchange reaches week 500, the game ends and the end-game overlay is shown.</p>
   */
  public void advanceWeek() {
    if (gameOver) return;
    exchange.advance();
    player.updateHistoricalNetWorth();
    tradingController.updateChart();

    if (exchange.getWeek() >= 520) {
      gameOver = true;
      endGame();
    }
  }

  public void sellAllEndGame() {
    for (Share share : new ArrayList<>(player.getPortfolio().getShares())) {
      exchange.sell(share, player);
    }
    endGame();
  }

  /**
   * <p>Registers all views as observers on the appropriate models.</p>
   */
  private void registerObservers() {
    exchange.addObserver(view.getSidebar());
    exchange.addObserver(view.getDashboardView());
    exchange.addObserver(view.getTradingView());

    player.addObserver(view.getDashboardView());
    player.addObserver(view.getPortfolioView());

    player.getPortfolio().addObserver(view.getDashboardView());
    player.getPortfolio().addObserver(view.getPortfolioView());
  }


  /**
   * <p>Fires an initial notification to all views so they populate on startup.</p>
   *
   * @param exchange the exchange to push to exchange observers.
   * @param player   the player to push to player observers.
   */
  private void initializeViews(Exchange exchange, Player player) {
    view.getSidebar().onExchangeUpdated(exchange);
    view.getDashboardView().onExchangeUpdated(exchange);
    tradingController.selectDefault();
    view.getTradingView().onExchangeUpdated(exchange);
    view.getDashboardView().onPlayerUpdated(player);
    view.getPortfolioView().onPlayerUpdated(player);
  }

  /**
   * <p>Ends the game and displays the end-game overlay.</p>
   *
   * <p>Builds a summary of the player's final stats and delegates the
   * overlay display to the main view.</p>
   */
  private void endGame() {
    double ratio = player.getReturnRate().add(BigDecimal.ONE).doubleValue();
    millionsScore = 1000 * Math.log10(ratio);

    BigDecimal netWorthRounded = player.getNetWorth().setScale(0, RoundingMode.HALF_UP);
    BigDecimal profitRounded = player.getProfit().setScale(2, RoundingMode.HALF_UP);
    BigDecimal returnRateRounded = player.getReturnRate().setScale(4, RoundingMode.HALF_UP);
    long millionsScoreRounded = Math.round(millionsScore);

    millionsScore = 1000 * Math.log10(ratio);
    String statsText = "Net Worth: " + netWorthRounded + "\n"
            + "Profit: " + profitRounded + "\n"
            + "Return Rate: " + returnRateRounded + "\n"
            + "Weeks played: " + exchange.getWeek() + "\n"
            + "Millions Score: " + millionsScoreRounded;

    view.showEndGame(statsText);
  }
}
