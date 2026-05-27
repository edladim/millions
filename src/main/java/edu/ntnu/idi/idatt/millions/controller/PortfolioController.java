package edu.ntnu.idi.idatt.millions.controller;

import edu.ntnu.idi.idatt.millions.model.market.Exchange;
import edu.ntnu.idi.idatt.millions.model.player.Player;
import edu.ntnu.idi.idatt.millions.view.dialogs.TransactionDialog;
import edu.ntnu.idi.idatt.millions.view.pages.PortfolioView;
import java.math.BigDecimal;

/**
 * Controller for the portfolio page.
 *
 * <p>Owns the wiring between the {@link PortfolioView} and the underlying models: registers the
 * view as a player and portfolio observer, fires an initial push so the holdings populate on
 * startup, and dispatches sell orders via {@link TransactionExecutor}.
 */
public class PortfolioController {

  private final Exchange exchange;
  private final Player player;

  /**
   * Creates a portfolio controller, registers observers, fires the initial view-population call,
   * and wires the sell callback.
   *
   * @param view the portfolio view to control.
   * @param exchange the exchange model.
   * @param player the active player.
   */
  public PortfolioController(PortfolioView view, Exchange exchange, Player player) {
    this.exchange = exchange;
    this.player = player;

    player.addObserver(view);
    player.getPortfolio().addObserver(view);

    view.setOnSell(this::handleSell);

    view.onPlayerUpdated(player);
  }

  /**
   * Executes a sell order for the given quantity of a stock via {@link TransactionExecutor},
   * drawing across all underlying share lots in FIFO order, and shows a confirmation dialog with
   * the proceeds breakdown.
   *
   * @param symbol the stock symbol to sell
   * @param quantity the total quantity to sell across all lots
   */
  private void handleSell(String symbol, BigDecimal quantity) {
    TransactionExecutor.execute(
        "Sale failed",
        () -> exchange.sell(symbol, quantity, player),
        tx -> TransactionDialog.showSaleConfirmation(tx, player.getMoney()));
  }
}
