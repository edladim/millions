package edu.ntnu.idi.idatt.millions.controller;

import edu.ntnu.idi.idatt.millions.model.market.Exchange;
import edu.ntnu.idi.idatt.millions.model.player.Player;
import edu.ntnu.idi.idatt.millions.view.pages.DashboardView;
import java.util.function.Consumer;

/**
 * <p>Controller for the dashboard page.</p>
 * <p>Owns the wiring between the {@link DashboardView} and the underlying
 * models: registers the view as an observer on the exchange, player, and
 * portfolio, fires an initial push so the view populates on startup, and
 * connects stock-row clicks to a navigation handler supplied by the parent
 * controller.</p>
 */
public class DashboardController {

  /**
   * <p>Creates a dashboard controller and wires all observers and callbacks.</p>
   *
   * @param view the dashboard view to control
   * @param exchange the exchange model to observe
   * @param player the active player to observe
   * @param onStockClicked  handler invoked with a stock symbol when the user
   *                        clicks a mover row (typically navigates to the
   *                        trading page focused on that stock)
   */
  public DashboardController(DashboardView view, Exchange exchange, Player player, Consumer<String> onStockClicked) {
    exchange.addObserver(view);
    player.addObserver(view);
    player.getPortfolio().addObserver(view);

    view.setOnStockClicked(onStockClicked);

    view.onExchangeUpdated(exchange);
    view.onPlayerUpdated(player);
  }
}
