package edu.ntnu.idi.idatt.millions.controller;

import edu.ntnu.idi.idatt.millions.model.Exchange;
import edu.ntnu.idi.idatt.millions.model.Player;
import edu.ntnu.idi.idatt.millions.model.transaction.Transaction;
import edu.ntnu.idi.idatt.millions.view.TransactionDialog;
import edu.ntnu.idi.idatt.millions.view.pages.PortfolioView;
import java.math.BigDecimal;

/**
 * <p>Controller for the portfolio page.</p>
 * <p>Handles sell order execution and wires the sell callback on the view.</p>
 */
public class PortfolioController {

  private final Exchange exchange;
  private final Player player;

  /**
   * <p>Creates a portfolio controller and wires all view callbacks.</p>
   *
   * @param view     the portfolio view to control.
   * @param exchange the exchange model.
   * @param player   the active player.
   */
  public PortfolioController(PortfolioView view, Exchange exchange, Player player) {
    this.exchange = exchange;
    this.player = player;

    view.setOnSell(this::handleSell);
  }

  /**
   * <p>Executes a sell order for the full owned quantity of a stock, drawing
   * across all underlying share lots in FIFO order, and shows a confirmation
   * dialog with the proceeds breakdown.</p>
   *
   * @param symbol   the stock symbol to sell
   * @param quantity the total quantity to sell across all lots
   */
  private void handleSell(String symbol, BigDecimal quantity) {
    try {
      Transaction tx = exchange.sell(symbol, quantity, player);
      TransactionDialog.showSaleConfirmation(tx, player.getMoney());
    } catch (IllegalStateException | IllegalArgumentException e) {
      TransactionDialog.showError("Sale failed", e.getMessage());
    } catch (Exception e) {
      TransactionDialog.showError("Unexpected error",
          "Could not complete sale: " + e.getMessage());
    }
  }
}
