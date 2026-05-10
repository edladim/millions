package edu.ntnu.idi.idatt.millions.controller;

import edu.ntnu.idi.idatt.millions.model.Exchange;
import edu.ntnu.idi.idatt.millions.model.Player;
import edu.ntnu.idi.idatt.millions.view.MainView;

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

    initializeViews(exchange, player);
  }

  /**
   * <p>Advances the exchange by one week and snapshots the player's net worth.</p>
   * <p>{@code exchange.advance()} notifies all {@link edu.ntnu.idi.idatt.millions.observer.ExchangeObserver}s,
   * and {@code player.updateHistoricalNetWorth()} notifies all
   * {@link edu.ntnu.idi.idatt.millions.observer.PlayerObserver}s — so all views
   * refresh automatically without a manual push.</p>
   */
  public void advanceWeek() {
    exchange.advance();
    player.updateHistoricalNetWorth();
    tradingController.updateChart();
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
    view.getTradingView().onExchangeUpdated(exchange);
    view.getDashboardView().onPlayerUpdated(player);
    view.getPortfolioView().onPlayerUpdated(player);
  }
}
