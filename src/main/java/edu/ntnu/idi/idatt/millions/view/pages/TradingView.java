package edu.ntnu.idi.idatt.millions.view.pages;

import edu.ntnu.idi.idatt.millions.model.ReadOnlyExchange;
import edu.ntnu.idi.idatt.millions.model.ReadOnlyStock;
import edu.ntnu.idi.idatt.millions.observer.ExchangeObserver;
import edu.ntnu.idi.idatt.millions.view.components.StockChartComponent;
import edu.ntnu.idi.idatt.millions.view.ViewWidgets;
import edu.ntnu.idi.idatt.millions.view.pages.trading.BuyPanel;
import edu.ntnu.idi.idatt.millions.view.pages.trading.StockListPanel;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * <p>
 * Trading page view that assembles the stock list, chart preview, and
 * buy/sell panel into a {@link BorderPane} layout.
 * </p>
 *
 * <p>
 * Layout: the center column holds a {@link StockChartComponent} above a
 * {@link StockListPanel}; the right column holds a {@link BuyPanel}.
 * {@code TradingView} acts as the orchestrator — it wires the internal
 * connection between the panels (stock selection drives both the buy panel
 * and the chart) and exposes a flat public API that the controller calls
 * without needing to know about the sub-panels.
 * </p>
 */
public class TradingView extends BorderPane implements ExchangeObserver {

  /** Action mode of the trading panel. */
  public enum Mode { BUY, SELL }

  private final StockListPanel stockListPanel;
  private final BuyPanel buyPanel;
  private final StockChartComponent stockChart;

  private Consumer<String> onSelectStock;
  private Runnable onRefresh;

  /**
   * <p>Constructs the trading view, builds its layout, and wires the
   * internal connection between the stock list and the buy panel.</p>
   */
  public TradingView() {
    getStyleClass().add("dashboard-view");

    stockChart = new StockChartComponent("–", "No stock selected");
    stockChart.setChartHeight(250);
    stockChart.setPrefHeight(300);

    stockListPanel = new StockListPanel();
    buyPanel = new BuyPanel();

    stockListPanel.setOnStockActivated(stock -> {
      buyPanel.updateStockInfo(stock);
      stockChart.setStockInfo(stock.getSymbol(), stock.getCompany());
    });
    stockListPanel.setOnStockSelected(symbol -> {
      if (onSelectStock != null) onSelectStock.accept(symbol);
    });

    this.setCenter(buildCenterPanel());
    this.setRight(buildBuyPanelWrapper());
  }

  private VBox buildCenterPanel() {
    VBox center = new VBox(5);
    center.setPadding(new Insets(24));
    center.setMaxWidth(Double.MAX_VALUE);
    VBox.setVgrow(center, Priority.ALWAYS);
    VBox.setVgrow(stockListPanel, Priority.ALWAYS);
    center.getChildren().addAll(stockChart, ViewWidgets.divider(), stockListPanel);
    return center;
  }

  private VBox buildBuyPanelWrapper() {
    VBox wrapper = new VBox(buyPanel);
    wrapper.getStyleClass().add("buy-panel-wrapper");
    wrapper.setPadding(new Insets(24, 24, 24, 0));
    return wrapper;
  }

  // ── ExchangeObserver ──────────────────────────────────────────────────────

  @Override
  public void onExchangeUpdated(ReadOnlyExchange exchange) {
    if (onRefresh != null) onRefresh.run();
  }

  // ── Public API (delegates to sub-panels) ──────────────────────────────────

  /**
   * <p>Replaces the stock list and re-applies the pending highlight.</p>
   *
   * @param stocks the stocks to display
   */
  public void setStocks(List<? extends ReadOnlyStock> stocks) {
    stockListPanel.setStocks(stocks);
  }

  /**
   * <p>Programmatically selects the given stock without spuriously firing
   * the controller's {@code onSelectStock} callback from the listener.</p>
   *
   * @param stock the stock to select
   */
  public void selectStock(ReadOnlyStock stock) {
    stockListPanel.selectStock(stock);
  }

  /**
   * <p>Marks the given symbol as highlighted without firing the controller
   * callback. Remembered if the stock is not yet in the items list.</p>
   *
   * @param symbol the symbol to highlight, or {@code null} to clear
   */
  public void setHighlightedStock(String symbol) {
    stockListPanel.setHighlightedStock(symbol);
  }

  /**
   * <p>Returns the search field used for stock filtering.</p>
   *
   * @return the search text field
   */
  public TextField getSearchField() {
    return stockListPanel.getSearchField();
  }

  /**
   * <p>Returns the chart component so the controller can push price data.</p>
   *
   * @return the stock chart component
   */
  public StockChartComponent getStockChart() {
    return stockChart;
  }

  /** @see BuyPanel#setMode(Mode) */
  public void setMode(Mode mode) { buyPanel.setMode(mode); }

  /** @see BuyPanel#getMode() */
  public Mode getMode() { return buyPanel.getMode(); }

  /** @see BuyPanel#getInputValue() */
  public BigDecimal getInputValue() { return buyPanel.getInputValue(); }

  /** @see BuyPanel#isAmountMode() */
  public boolean isAmountMode() { return buyPanel.isAmountMode(); }

  /** @see BuyPanel#setCostPreview(String, String, String) */
  public void setCostPreview(String gross, String commission, String total) {
    buyPanel.setCostPreview(gross, commission, total);
  }

  /** @see BuyPanel#setDerivedLabel(String) */
  public void setDerivedLabel(String text) { buyPanel.setDerivedLabel(text); }

  /** @see BuyPanel#setActionEnabled(boolean) */
  public void setActionEnabled(boolean enabled) { buyPanel.setActionEnabled(enabled); }

  /** @see BuyPanel#clearInput() */
  public void clearInput() { buyPanel.clearInput(); }

  /** @see BuyPanel#setCurrentPrice(BigDecimal) */
  public void setCurrentPrice(BigDecimal price) { buyPanel.setCurrentPrice(price); }

  /** @see BuyPanel#setInputAmount(BigDecimal, boolean) */
  public void setInputAmount(BigDecimal value, boolean asAmount) {
    buyPanel.setInputAmount(value, asAmount);
  }

  /** @see BuyPanel#setOwnedQuantity(BigDecimal) */
  public void setOwnedQuantity(BigDecimal qty) { buyPanel.setOwnedQuantity(qty); }

  /** @see BuyPanel#setCashBalance(BigDecimal) */
  public void setCashBalance(BigDecimal cash) { buyPanel.setCashBalance(cash); }

  // ── Callback registration ─────────────────────────────────────────────────

  /**
   * <p>Registers a handler that runs when the user confirms a trade.</p>
   *
   * @param handler the action handler
   */
  public void setOnAction(Consumer<Mode> handler) { buyPanel.setOnAction(handler); }

  /**
   * <p>Registers a handler that runs when the input field changes.</p>
   *
   * @param handler the handler to run on change
   */
  public void setOnInputChanged(Runnable handler) { buyPanel.setOnInputChanged(handler); }

  /**
   * <p>Registers a handler that runs when the user toggles Buy/Sell mode.</p>
   *
   * @param handler the handler accepting the new mode
   */
  public void setOnModeChanged(Consumer<Mode> handler) { buyPanel.setOnModeChanged(handler); }

  /**
   * <p>Registers a handler that runs when a percentage button is clicked.</p>
   *
   * @param handler the consumer that receives the selected percentage as a decimal
   */
  public void setOnPercentSelected(Consumer<BigDecimal> handler) {
    buyPanel.setOnPercentSelected(handler);
  }

  /**
   * <p>Registers a handler that runs when a stock is selected by the user,
   * receiving its symbol.</p>
   *
   * @param handler the handler accepting the selected symbol
   */
  public void setOnSelectStock(Consumer<String> handler) {
    this.onSelectStock = handler;
  }

  /**
   * <p>Registers a handler that runs when the exchange updates, so the
   * controller can rebuild the stock list.</p>
   *
   * @param handler the handler to run on exchange update
   */
  public void setOnRefresh(Runnable handler) {
    this.onRefresh = handler;
  }
}
