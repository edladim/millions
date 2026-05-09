package edu.ntnu.idi.idatt.millions.controller;

import edu.ntnu.idi.idatt.millions.model.Exchange;
import edu.ntnu.idi.idatt.millions.model.Player;
import edu.ntnu.idi.idatt.millions.model.Share;
import edu.ntnu.idi.idatt.millions.model.transaction.Transaction;
import edu.ntnu.idi.idatt.millions.view.TransactionDialog;
import edu.ntnu.idi.idatt.millions.view.pages.PortfolioView;

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
    this.player   = player;

    view.setOnSell(this::handleSell);
  }

  /**
   * <p>Executes a sell order for the given share and shows a confirmation dialog
   * with the full proceeds breakdown.</p>
   *
   * @param share the share to sell.
   */
  private void handleSell(Share share) {
    try {
      Transaction tx = exchange.sell(share, player);
      TransactionDialog.showSaleConfirmation(tx, player.getMoney());
    } catch (Exception e) {
      TransactionDialog.showError("Sale failed", e.getMessage());
    }
  }
}
