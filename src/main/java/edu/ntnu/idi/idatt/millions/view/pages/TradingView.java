package edu.ntnu.idi.idatt.millions.view.pages;

import edu.ntnu.idi.idatt.millions.model.market.ReadOnlyExchange;
import edu.ntnu.idi.idatt.millions.model.market.ReadOnlyStock;
import edu.ntnu.idi.idatt.millions.observer.ExchangeObserver;
import edu.ntnu.idi.idatt.millions.view.components.StockChartComponent;
import edu.ntnu.idi.idatt.millions.view.components.ViewWidgets;
import edu.ntnu.idi.idatt.millions.view.components.trading.BuyPanel;
import edu.ntnu.idi.idatt.millions.view.components.trading.StockListPanel;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Trading page view that assembles the stock list, chart preview, and buy/sell panel into a {@link
 * BorderPane} layout.
 *
 * <p>Layout: the center column holds a {@link StockChartComponent} above a {@link StockListPanel};
 * the right column holds a {@link BuyPanel}. {@code TradingView} acts as the orchestrator — it
 * wires the internal connection between the panels (stock selection drives both the buy panel and
 * the chart) and exposes a flat public API that the controller calls without needing to know about
 * the sub-panels.
 */
public class TradingView extends BorderPane implements ExchangeObserver {

  /** Action mode of the trading panel. */
  public enum Mode {
    BUY,
    SELL
  }

  /** Minimum width kept for the center panel before the buy-panel wrapper starts shrinking. */
  private static final double CENTER_MIN_WIDTH = 420;

  /** Natural preferred width of the buy-panel wrapper (BuyPanel pref 300 + right padding 24). */
  private static final double WRAPPER_PREF_WIDTH = 324;

  /** Hard floor for the buy-panel wrapper so it never collapses entirely. */
  private static final double WRAPPER_MIN_WIDTH = 200;

  private final StockListPanel stockListPanel;
  private final BuyPanel buyPanel;
  private final StockChartComponent stockChart;

  private Consumer<String> onSelectStock;
  private Runnable onRefresh;

  /**
   * Constructs the trading view, builds its layout, and wires the internal connection between the
   * stock list and the buy panel.
   */
  public TradingView() {
    getStyleClass().add("dashboard-view");

    stockChart = new StockChartComponent("–", "No stock selected");
    stockChart.setChartHeight(250);
    stockChart.setPrefHeight(300);

    stockListPanel = new StockListPanel();
    buyPanel = new BuyPanel();

    stockListPanel.setOnStockActivated(
        stock -> {
          buyPanel.updateStockInfo(stock);
          stockChart.setStockInfo(stock.getSymbol(), stock.getCompany());
        });
    stockListPanel.setOnStockSelected(
        symbol -> {
          if (onSelectStock != null) {
            onSelectStock.accept(symbol);
          }
        });

    buyPanel.setMinWidth(0);

    this.setCenter(buildCenterPanel());
    this.setRight(buildBuyPanelWrapper());
  }

  /**
   * Returns {@code 0} so the parent {@link javafx.scene.control.ScrollPane} (with {@code
   * fitToWidth=true}) can shrink this node freely to the viewport width. The default {@link
   * BorderPane} implementation would return {@code center.minWidth + right.minWidth}, freezing the
   * view at that combined minimum and causing the right panel to be clipped instead of shrinking.
   */
  @Override
  protected double computeMinWidth(double height) {
    return 0;
  }

  /**
   * Controls the exact pixel split between the center panel and the buy-panel wrapper in one layout
   * pass, bypassing the standard {@link BorderPane} algorithm which reads {@code prefWidth} one
   * frame too late on window resize.
   *
   * <p>The right wrapper holds at {@value #WRAPPER_PREF_WIDTH} px and only starts shrinking once
   * the center reaches {@value #CENTER_MIN_WIDTH} px. It floors at {@value #WRAPPER_MIN_WIDTH} px.
   */
  @Override
  protected void layoutChildren() {
    Insets ins = getInsets();
    double x = ins.getLeft();
    double y = ins.getTop();
    double w = getWidth() - ins.getLeft() - ins.getRight();
    double h = getHeight() - ins.getTop() - ins.getBottom();

    double rightW = Math.clamp(w - CENTER_MIN_WIDTH, WRAPPER_MIN_WIDTH, WRAPPER_PREF_WIDTH);
    double centerW = w - rightW;

    if (getCenter() != null) {
      layoutInArea(getCenter(), x, y, centerW, h, 0, null, HPos.LEFT, VPos.TOP);
    }
    if (getRight() != null) {
      layoutInArea(getRight(), x + centerW, y, rightW, h, 0, null, HPos.LEFT, VPos.TOP);
    }
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
    VBox buyPanelWrapper = new VBox(buyPanel);
    buyPanelWrapper.getStyleClass().add("buy-panel-wrapper");
    buyPanelWrapper.setPadding(new Insets(24, 24, 24, 0));
    buyPanelWrapper.setMinWidth(WRAPPER_MIN_WIDTH);
    return buyPanelWrapper;
  }

  // ExchangeObserver

  @Override
  public void onExchangeUpdated(ReadOnlyExchange exchange) {
    if (onRefresh != null) {
      onRefresh.run();
    }
  }

  // Public API — delegates to sub-panels

  /**
   * Replaces the stock list and re-applies the pending highlight.
   *
   * @param stocks the stocks to display
   */
  public void setStocks(List<? extends ReadOnlyStock> stocks) {
    stockListPanel.setStocks(stocks);
  }

  /**
   * Programmatically selects the given stock without spuriously firing the controller's {@code
   * onSelectStock} callback from the listener.
   *
   * @param stock the stock to select
   */
  public void selectStock(ReadOnlyStock stock) {
    stockListPanel.selectStock(stock);
  }

  /**
   * Marks the given symbol as highlighted without firing the controller callback. Remembered if the
   * stock is not yet in the items list.
   *
   * @param symbol the symbol to highlight, or {@code null} to clear
   */
  public void setHighlightedStock(String symbol) {
    stockListPanel.setHighlightedStock(symbol);
  }

  /**
   * Returns the search field used for stock filtering.
   *
   * @return the search text field
   */
  public TextField getSearchField() {
    return stockListPanel.getSearchField();
  }

  /**
   * Returns the chart component so the controller can push price data.
   *
   * @return the stock chart component
   */
  public StockChartComponent getStockChart() {
    return stockChart;
  }

  /** Delegates to {@link BuyPanel#setMode(Mode)}. */
  public void setMode(Mode mode) {
    buyPanel.setMode(mode);
  }

  /** Delegates to {@link BuyPanel#getMode()}. */
  public Mode getMode() {
    return buyPanel.getMode();
  }

  /** Delegates to {@link BuyPanel#getInputValue()}. */
  public BigDecimal getInputValue() {
    return buyPanel.getInputValue();
  }

  /** Delegates to {@link BuyPanel#isAmountMode()}. */
  public boolean isAmountMode() {
    return buyPanel.isAmountMode();
  }

  /** Delegates to {@link BuyPanel#setCostPreview(String, String, String)}. */
  public void setCostPreview(String gross, String commission, String total) {
    buyPanel.setCostPreview(gross, commission, total);
  }

  /** Delegates to {@link BuyPanel#setDerivedLabel(String)}. */
  public void setDerivedLabel(String text) {
    buyPanel.setDerivedLabel(text);
  }

  /** Delegates to {@link BuyPanel#setActionEnabled(boolean)}. */
  public void setActionEnabled(boolean enabled) {
    buyPanel.setActionEnabled(enabled);
  }

  /** Delegates to {@link BuyPanel#setCurrentPrice(BigDecimal)}. */
  public void setCurrentPrice(BigDecimal price) {
    buyPanel.setCurrentPrice(price);
  }

  /** Delegates to {@link BuyPanel#setInputAmount(BigDecimal, boolean)}. */
  public void setInputAmount(BigDecimal value, boolean asAmount) {
    buyPanel.setInputAmount(value, asAmount);
  }

  /** Delegates to {@link BuyPanel#setOwnedQuantity(BigDecimal)}. */
  public void setOwnedQuantity(BigDecimal qty) {
    buyPanel.setOwnedQuantity(qty);
  }

  /** Delegates to {@link BuyPanel#setCashBalance(BigDecimal)}. */
  public void setCashBalance(BigDecimal cash) {
    buyPanel.setCashBalance(cash);
  }

  // Callback registration

  /**
   * Registers a handler that runs when the user confirms a trade.
   *
   * @param handler the action handler
   */
  public void setOnAction(Consumer<Mode> handler) {
    buyPanel.setOnAction(handler);
  }

  /**
   * Registers a handler that runs when the input field changes.
   *
   * @param handler the handler to run on change
   */
  public void setOnInputChanged(Runnable handler) {
    buyPanel.setOnInputChanged(handler);
  }

  /**
   * Registers a handler that runs when the user toggles Buy/Sell mode.
   *
   * @param handler the handler accepting the new mode
   */
  public void setOnModeChanged(Consumer<Mode> handler) {
    buyPanel.setOnModeChanged(handler);
  }

  /**
   * Registers a handler that runs when a percentage button is clicked.
   *
   * @param handler the consumer that receives the selected percentage as a decimal
   */
  public void setOnPercentSelected(Consumer<BigDecimal> handler) {
    buyPanel.setOnPercentSelected(handler);
  }

  /**
   * Registers a handler that runs when a stock is selected by the user, receiving its symbol.
   *
   * @param handler the handler accepting the selected symbol
   */
  public void setOnSelectStock(Consumer<String> handler) {
    this.onSelectStock = handler;
  }

  /**
   * Registers a handler that runs when the exchange updates, so the controller can rebuild the
   * stock list.
   *
   * @param handler the handler to run on exchange update
   */
  public void setOnRefresh(Runnable handler) {
    this.onRefresh = handler;
  }
}
