package edu.ntnu.idi.idatt.millions.controller;

import edu.ntnu.idi.idatt.millions.model.Exchange;
import edu.ntnu.idi.idatt.millions.model.Player;
import edu.ntnu.idi.idatt.millions.model.ReadOnlyStock;
import edu.ntnu.idi.idatt.millions.model.transaction.Transaction;
import edu.ntnu.idi.idatt.millions.view.TransactionDialog;
import edu.ntnu.idi.idatt.millions.view.ViewFormatter;
import edu.ntnu.idi.idatt.millions.view.pages.TradingView;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * <p>Controller for the trading page.</p>
 * <p>Handles stock search, live cost preview, and buy order execution.</p>
 */
public class TradingController {

  private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.005");

  private final TradingView view;
  private final Exchange exchange;
  private final Player player;

  private String selectedSymbol = null;

  /**
   * <p>Creates a trading controller and wires all view callbacks.</p>
   *
   * @param view     the trading view to control.
   * @param exchange the exchange model.
   * @param player   the active player.
   */
  public TradingController(TradingView view, Exchange exchange, Player player) {
    this.view     = view;
    this.exchange = exchange;
    this.player   = player;

    view.setOnRefresh(() -> filterStocks(view.getSearchField().getText()));

    view.setOnSelectStock(symbol -> {
      selectedSymbol = symbol;
      ReadOnlyStock stock = exchange.getStock(symbol);
      view.getStockChart().setStockInfo(stock.getSymbol(), stock.getCompany());
      view.getStockChart().setData(stock.getHistoricalPrices());
      updateCostPreview();
    });

    view.setOnQuantityChanged(this::updateCostPreview);

    view.setOnBuy(qty -> handleBuy(selectedSymbol, qty));

    view.getSearchField().textProperty().addListener(
        (_, _, text) -> filterStocks(text)
    );
  }

  /**
   * <p>Recalculates and pushes the cost preview to the view based on the
   * currently selected stock and spinner quantity.</p>
   */
  private void updateCostPreview() {
    if (selectedSymbol == null) return;

    int qty;
    try {
      qty = Integer.parseInt(view.getQuantityInput());
    } catch (NumberFormatException e) {
      // spinner is mid-edit (empty or partial input)
      return;
    }

    ReadOnlyStock stock = exchange.getStock(selectedSymbol);
    BigDecimal gross      = stock.getSalesPrice().multiply(BigDecimal.valueOf(qty));
    BigDecimal commission = gross.multiply(COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);
    BigDecimal total      = gross.add(commission);

    view.setCostPreview(
        ViewFormatter.price(gross),
        ViewFormatter.price(commission),
        ViewFormatter.price(total)
    );
  }

  /**
   * <p>Executes a buy order and shows a confirmation dialog with the full
   * cost breakdown.</p>
   *
   * @param symbol      the stock symbol to buy.
   * @param quantityStr the quantity as entered by the user.
   */
  private void handleBuy(String symbol, String quantityStr) {
    try {
      BigDecimal quantity = new BigDecimal(quantityStr);
      Transaction tx = exchange.buy(symbol, quantity, player);
      TransactionDialog.showPurchaseConfirmation(tx, player.getMoney());
    } catch (IllegalStateException e) {
      TransactionDialog.showError("Purchase failed", e.getMessage());
    } catch (Exception e) {
      TransactionDialog.showError("Unexpected error",
          "Could not complete purchase: " + e.getMessage());
    }
  }

  /**
   * <p>Filters the stock list by the given search text.</p>
   * <p>Shows all stocks when the text is blank, otherwise delegates to
   * {@link Exchange#findStocks(String)}.</p>
   *
   * @param text the search term typed by the user.
   */
  private void filterStocks(String text) {
    view.clearStocks();
    List<? extends ReadOnlyStock> results = text.isBlank()
        ? exchange.getStocks()
        : exchange.findStocks(text);

    for (ReadOnlyStock stock : results) {
      view.addStockRow(stock);
    }
  }

  public void updateChart() {
    if (selectedSymbol == null) return;
    try {
      ReadOnlyStock stock = exchange.getStock(selectedSymbol);
      view.getStockChart().setStockInfo(stock.getSymbol(), stock.getCompany());
      view.getStockChart().setData(stock.getHistoricalPrices());
    } catch (Exception _) {

    }
  }

}