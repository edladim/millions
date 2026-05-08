package edu.ntnu.idi.idatt.millions.controller;

import edu.ntnu.idi.idatt.millions.model.Exchange;
import edu.ntnu.idi.idatt.millions.model.Player;
import edu.ntnu.idi.idatt.millions.model.Stock;
import edu.ntnu.idi.idatt.millions.model.transaction.Transaction;
import edu.ntnu.idi.idatt.millions.view.ViewFormatter;
import edu.ntnu.idi.idatt.millions.view.pages.TradingView;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import javafx.scene.control.Alert;

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
      updateCostPreview();
    });

    view.setOnQuantityChanged(this::updateCostPreview);

    view.setOnBuy(this::handleBuy);

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

    try {
      Stock stock = exchange.getStock(selectedSymbol);
      int qty = Integer.parseInt(view.getQuantityInput());

      BigDecimal gross      = stock.getSalesPrice().multiply(BigDecimal.valueOf(qty));
      BigDecimal commission = gross.multiply(COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);
      BigDecimal total      = gross.add(commission);

      view.setCostPreview(
          ViewFormatter.price(gross),
          ViewFormatter.price(commission),
          ViewFormatter.price(total)
      );
    } catch (Exception ignored) {
      // symbol not yet resolved or spinner in transient state
    }
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

      BigDecimal gross      = tx.getCalculator().calculateGross();
      BigDecimal commission = tx.getCalculator().calculateCommission();
      BigDecimal total      = tx.getCalculator().calculateTotal();

      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("Purchase Confirmed");
      alert.setHeaderText("Bought " + quantityStr + " × " + symbol);
      alert.setContentText(
          "Cost:        " + ViewFormatter.price(gross)      + "\n"
        + "Commission:  " + ViewFormatter.price(commission) + "\n"
        + "Total paid:  " + ViewFormatter.price(total)      + "\n"
        + "Cash left:   " + ViewFormatter.price(player.getMoney())
      );
      alert.showAndWait();

    } catch (IllegalStateException e) {
      showError("Insufficient funds", e.getMessage());
    } catch (Exception e) {
      showError("Purchase failed", e.getMessage());
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
    List<Stock> results = text.isBlank()
        ? exchange.getStocks()
        : exchange.findStocks(text);

    for (Stock stock : results) {
      BigDecimal change    = stock.getLatestPriceChange();
      boolean isPositive   = change.compareTo(BigDecimal.ZERO) >= 0;
      view.addStockRow(new edu.ntnu.idi.idatt.millions.view.StockRowData(
          stock.getSymbol(),
          stock.getCompany(),
          ViewFormatter.price(stock.getSalesPrice()),
          ViewFormatter.signedAmount(change),
          ViewFormatter.price(stock.getHighestPrice()),
          ViewFormatter.price(stock.getLowestPrice()),
          isPositive
      ));
    }
  }

  /**
   * <p>Displays an error alert with the given title and message.</p>
   *
   * @param title   the dialog title.
   * @param message the error message to display.
   */
  private void showError(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}