package edu.ntnu.idi.idatt.millions.view.pages;

import edu.ntnu.idi.idatt.millions.model.ReadOnlyExchange;
import edu.ntnu.idi.idatt.millions.model.ReadOnlyStock;
import edu.ntnu.idi.idatt.millions.observer.ExchangeObserver;
import edu.ntnu.idi.idatt.millions.view.ViewFormatter;
import edu.ntnu.idi.idatt.millions.view.components.StockChartComponent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.javafx.FontIcon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Consumer;

/**
 * <p>
 * Trading page view that renders stock listings, a chart preview, and a buy panel.
 * </p>
 *
 * <p>
 * The view exposes methods for adding rows, updating the selected stock, and
 * setting order cost previews.
 * </p>
 */
public class TradingView extends BorderPane implements ExchangeObserver {

  /** Action mode of the trading panel. */
  public enum Mode { BUY, SELL }

  private TextField searchField;
  private VBox stockListContainer;
  private Label loadMoreLabel;
  private StockChartComponent stockChart;
  private Label buySymbolLabel;
  private Label buyCompanyLabel;
  private Label buyPriceLabel;
  private Label buyHighLabel;
  private Label buyLowLabel;
  private Label buyChangeLabel;
  private Button buyTab;
  private Button sellTab;
  private Label panelTitle;
  private Label cashBalanceLabel;
  private Label ownedValueLabel;
  private Label inputLabel;
  private String highlightedSymbol = null;
  private HBox selectedRow = null;
  private Button selectedButton = null;
  private Spinner<Double> inputSpinner;
  private Label derivedLabel;
  private BigDecimal currentPrice;
  private Label estimatedCostLabel;
  private Label commissionLabel;
  private Label totalCostLabel;
  private Button actionButton;

  private Mode currentMode = Mode.BUY;
  private boolean amountMode = false;

  private VBox buyPanelWrapper;

  private Consumer<Mode> onAction;
  private Runnable onInputChanged;
  private Consumer<Mode> onModeChanged;
  private Consumer<String> onSelectStock;
  private Runnable onRefresh;
  private Runnable onLoadMore;

  /**
   * <p>Constructs the trading view and builds its initial layout.</p>
   */
  public TradingView() {
    getStyleClass().add("dashboard-view");

    buyPanelWrapper = buildBuyPanelWrapper();

    this.setCenter(buildCenterPanel());
    this.setRight(buyPanelWrapper);
  }

  /**
   * <p>Builds the center panel containing the chart and stock list.</p>
   *
   * @return the center panel container
   */
  private VBox buildCenterPanel() {
    VBox center = new VBox(5);
    center.setPadding(new Insets(24));
    center.setMaxWidth(Double.MAX_VALUE);
    VBox.setVgrow(center, Priority.ALWAYS);

    stockChart = new StockChartComponent("–", "No stock selected");
    stockChart.setChartHeight(250);
    stockChart.setPrefHeight(300);

    VBox stockPanel = buildStockListPanel();
    VBox.setVgrow(stockPanel, Priority.ALWAYS);

    center.getChildren().addAll(stockChart, buildDivider(), stockPanel);
    return center;
  }

  /**
   * <p>Builds the stock list panel with header, search, and table.</p>
   *
   * @return the stock list panel container
   */
  private VBox buildStockListPanel() {
    VBox panel = new VBox(8);
    panel.setPadding(new Insets(0,24,24,24));
    panel.setMaxWidth(Double.MAX_VALUE);
    VBox.setVgrow(panel, Priority.ALWAYS);

    Label title = new Label("Trading");
    title.getStyleClass().add("page-title");
    Label subtitle = new Label("Browse and buy stocks on the exchange");
    subtitle.getStyleClass().add("page-subtitle");
    VBox header = new VBox(4, title, subtitle);

    searchField = new TextField();
    searchField.setPromptText("Search by name or symbol");
    searchField.getStyleClass().add("search-field");
    searchField.setMaxWidth(Double.MAX_VALUE);

    HBox tableHeader = buildStockTableHeader();

    stockListContainer = new VBox(4);

    ScrollPane scroll = new ScrollPane(stockListContainer);
    scroll.setFitToWidth(true);
    scroll.getStyleClass().add("stock-scroll");
    scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    VBox.setVgrow(scroll, Priority.ALWAYS);

    loadMoreLabel = new Label("Load more");
    loadMoreLabel.getStyleClass().add("load-more-label");
    loadMoreLabel.setMaxWidth(Double.MAX_VALUE);
    loadMoreLabel.setAlignment(Pos.CENTER);
    loadMoreLabel.setVisible(false);
    loadMoreLabel.setManaged(false);
    loadMoreLabel.setOnMouseClicked(e -> { if (onLoadMore != null) onLoadMore.run(); });

    VBox tableCard = new VBox(0, tableHeader, buildDivider(), scroll, loadMoreLabel);
    tableCard.getStyleClass().add("stat-card");
    tableCard.setPadding(new Insets(24));
    VBox.setVgrow(tableCard, Priority.ALWAYS);

    panel.getChildren().addAll(header, searchField, tableCard);
    VBox.setVgrow(panel, Priority.ALWAYS);
    return panel;
  }

  /**
   * <p>Builds the header row for the stock table.</p>
   *
   * @return the table header container
   */
  private HBox buildStockTableHeader() {
    HBox header = new HBox();
    header.setPadding(new Insets(0, 0, 8, 0));
    header.setMaxWidth(Double.MAX_VALUE);

    Label stockHeader = makeHeaderCell("Stock", 80, false);
    stockHeader.setPrefWidth(190);
    stockHeader.setMaxWidth(190);

    header.getChildren().addAll(
            stockHeader,
            makeHeaderCell("Price",   70, true),
            makeHeaderCell("Change",  70, true),
            makeHeaderCell("High",    60, true),
            makeHeaderCell("Low",     60, true),
            makeHeaderCell("",        70, false)
    );
    return header;
  }

  /**
   * <p>Wraps the buy panel in a transparent container with padding so it
   * appears as a floating card with margins on the top, right, and bottom
   * instead of being attached to the edges of the trading view.</p>
   *
   * @return the wrapper container holding the buy panel
   */
  private VBox buildBuyPanelWrapper() {
    VBox wrapper = new VBox(buildBuyPanel());
    wrapper.getStyleClass().add("buy-panel-wrapper");
    wrapper.setPadding(new Insets(24, 24, 24, 0));
    return wrapper;
  }

  /**
   * <p>Builds the buy/sell panel containing the mode tabs, stock info,
   * input field with quantity/amount toggle, and order summary.</p>
   *
   * @return the panel container
   */
  private VBox buildBuyPanel() {
    VBox panel = new VBox(16);
    panel.getStyleClass().add("buy-panel");
    panel.setMinWidth(220);
    panel.setPrefWidth(300);
    panel.setMaxWidth(300);
    panel.setPadding(new Insets(24, 20, 24, 20));

    HBox modeToggle = buildModeToggle();

    panelTitle = new Label("Buy Stock");
    panelTitle.getStyleClass().add("section-heading");

    VBox stockInfoCard = buildSelectedStockInfo();

    HBox playerInfoCard = buildPlayerInfoCard();

    HBox inputHeader = buildInputHeader();

    inputSpinner = new Spinner<>();
    SpinnerValueFactory.DoubleSpinnerValueFactory spinnerFactory =
        new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1_000_000_000.0, 0.0, 1.0);
    spinnerFactory.setConverter(new javafx.util.StringConverter<>() {
      @Override public String toString(Double v) {
        if (v == null || v == 0.0) return "";
        return (v == Math.floor(v) && !Double.isInfinite(v))
            ? String.valueOf(v.longValue()) : String.valueOf(v);
      }
      @Override public Double fromString(String s) {
        if (s == null || s.isBlank()) return 0.0;
        try { return Double.parseDouble(s.trim().replace(",", ".")); }
        catch (NumberFormatException e) { return 0.0; }
      }
    });
    inputSpinner.setValueFactory(spinnerFactory);
    inputSpinner.setEditable(true);
    inputSpinner.getStyleClass().add("search-field");
    inputSpinner.setMaxWidth(Double.MAX_VALUE);
    inputSpinner.getEditor().textProperty().addListener((_, _, _) -> {
      if (onInputChanged != null) onInputChanged.run();
    });

    derivedLabel = new Label();
    derivedLabel.getStyleClass().add("input-derived");
    derivedLabel.setMaxWidth(Double.MAX_VALUE);
    derivedLabel.setMinWidth(0);
    derivedLabel.setTextOverrun(OverrunStyle.ELLIPSIS);

    VBox costCard = buildCostCard();

    actionButton = new Button("Buy now");
    actionButton.getStyleClass().add("buy-btn");
    actionButton.setMaxWidth(Double.MAX_VALUE);
    actionButton.setDisable(true);
    actionButton.setOnAction(e -> {
      if (onAction != null) onAction.accept(currentMode);
    });

    panel.getChildren().addAll(
            modeToggle,
            panelTitle,
            stockInfoCard,
            playerInfoCard,
            inputHeader,
            inputSpinner,
            derivedLabel,
            costCard,
            buildSpacer(4),
            actionButton
    );

    VBox.setVgrow(costCard, Priority.NEVER);
    return panel;
  }

  /**
   * <p>Builds the compact player info card showing cash balance and how many
   * shares of the selected stock the player currently owns.</p>
   *
   * @return the info card container
   */
  private HBox buildPlayerInfoCard() {
    Label cashTitle = new Label("Cash Balance");
    cashTitle.getStyleClass().add("stat-card-title");
    cashBalanceLabel = new Label("$0.00");
    cashBalanceLabel.getStyleClass().add("player-info-value");
    VBox cashBox = new VBox(3, cashTitle, cashBalanceLabel);
    HBox.setHgrow(cashBox, Priority.ALWAYS);

    Label ownedTitle = new Label("Stocks Owned");
    ownedTitle.getStyleClass().add("stat-card-title");
    ownedValueLabel = new Label("–");
    ownedValueLabel.getStyleClass().add("player-info-value");
    VBox ownedBox = new VBox(3, ownedTitle, ownedValueLabel);

    HBox card = new HBox(0, cashBox, ownedBox);
    card.getStyleClass().add("stat-card");
    card.setPadding(new Insets(12, 16, 12, 16));
    card.setAlignment(Pos.CENTER_LEFT);
    return card;
  }

  /**
   * <p>Builds the Buy/Sell mode toggle row at the top of the panel.</p>
   *
   * @return the toggle container
   */
  private HBox buildModeToggle() {
    buyTab = new Button("Buy");
    buyTab.getStyleClass().addAll("mode-tab", "mode-tab-active");
    buyTab.setMaxWidth(Double.MAX_VALUE);
    buyTab.setOnAction(e -> setMode(Mode.BUY));

    sellTab = new Button("Sell");
    sellTab.getStyleClass().add("mode-tab");
    sellTab.setMaxWidth(Double.MAX_VALUE);
    sellTab.setOnAction(e -> setMode(Mode.SELL));

    HBox.setHgrow(buyTab, Priority.ALWAYS);
    HBox.setHgrow(sellTab, Priority.ALWAYS);

    HBox toggle = new HBox(8, buyTab, sellTab);
    toggle.setMaxWidth(Double.MAX_VALUE);
    return toggle;
  }

  /**
   * <p>Builds the input-mode header row containing the input label and the
   * swap button that toggles between quantity and dollar-amount input.</p>
   *
   * @return the header container
   */
  private HBox buildInputHeader() {
    inputLabel = new Label("Quantity");
    inputLabel.getStyleClass().add("input-label");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    FontIcon swapIcon = new FontIcon("fas-exchange-alt");
    swapIcon.setIconSize(12);
    Button swapBtn = new Button();
    swapBtn.setGraphic(swapIcon);
    swapBtn.getStyleClass().add("swap-btn");
    swapBtn.setOnAction(e -> toggleInputMode());

    HBox header = new HBox(inputLabel, spacer, swapBtn);
    header.setAlignment(Pos.CENTER_LEFT);
    return header;
  }

  /**
   * <p>Switches the panel between Buy and Sell mode, updating tab styling,
   * action button text, and notifying the registered mode handler.</p>
   *
   * @param mode the new mode
   */
  private void setMode(Mode mode) {
    if (currentMode == mode) return;
    currentMode = mode;

    buyTab.getStyleClass().removeAll("mode-tab-active");
    sellTab.getStyleClass().removeAll("mode-tab-active");
    (mode == Mode.BUY ? buyTab : sellTab).getStyleClass().add("mode-tab-active");

    panelTitle.setText(mode == Mode.BUY ? "Buy Stock" : "Sell Stock");
    actionButton.setText(mode == Mode.BUY ? "Buy now" : "Sell now");
    actionButton.getStyleClass().removeAll("action-btn-sell");
    if (mode == Mode.SELL) actionButton.getStyleClass().add("action-btn-sell");

    if (onModeChanged != null) onModeChanged.accept(mode);
  }

  /**
   * <p>Toggles the input between quantity entry and dollar-amount entry. If
   * the user has entered a value and a stock is selected, the existing value
   * is converted across the toggle (quantity ↔ amount) using the current
   * stock price so the user keeps their intended order size.</p>
   */
  private void toggleInputMode() {
    BigDecimal current = getInputValue();
    boolean wasAmount = amountMode;
    amountMode = !amountMode;
    inputLabel.setText(amountMode ? "Amount ($)" : "Quantity");

    if (current.signum() > 0 && currentPrice != null && currentPrice.signum() > 0) {
      BigDecimal converted = wasAmount
          ? current.divide(currentPrice, 4, RoundingMode.HALF_UP)
          : current.multiply(currentPrice).setScale(2, RoundingMode.HALF_UP);
      setSpinnerValue(converted);
    } else {
      setSpinnerValue(BigDecimal.ZERO);
    }

    if (onInputChanged != null) onInputChanged.run();
  }

  /**
   * <p>Pushes a new value into the spinner, updating both the underlying
   * value factory and the editor text so listeners observe the change.</p>
   *
   * @param value the value to set
   */
  private void setSpinnerValue(BigDecimal value) {
    String stripped = value.stripTrailingZeros().toPlainString();
    if (stripped.startsWith(".")) stripped = "0" + stripped;
    inputSpinner.getEditor().setText(stripped);
    SpinnerValueFactory.DoubleSpinnerValueFactory factory =
        (SpinnerValueFactory.DoubleSpinnerValueFactory) inputSpinner.getValueFactory();
    factory.setValue(value.doubleValue());
  }

  /**
   * <p>Builds the card that shows the selected stock information.</p>
   *
   * @return the stock info card container
   */
  private VBox buildSelectedStockInfo() {
    buySymbolLabel  = new Label("–");
    buySymbolLabel.getStyleClass().add("buy-stock-symbol");
    buyCompanyLabel = new Label("No stock selected");
    buyCompanyLabel.getStyleClass().add("mover-symbol");
    buyPriceLabel   = new Label("$0.00");
    buyPriceLabel.getStyleClass().add("buy-stock-price");
    buyChangeLabel  = new Label("+0.00%");
    buyChangeLabel.getStyleClass().add("mover-change-positive");
    buyHighLabel    = new Label("H: $0.00");
    buyHighLabel.getStyleClass().add("stat-card-title");
    buyLowLabel     = new Label("L: $0.00");
    buyLowLabel.getStyleClass().add("stat-card-title");

    HBox highLow = new HBox(12, buyHighLabel, buyLowLabel);

    HBox priceRow = new HBox(10, buyPriceLabel, buyChangeLabel);
    priceRow.setAlignment(Pos.CENTER_LEFT);

    HBox symbolRow = new HBox(8, buySymbolLabel, buyCompanyLabel);
    symbolRow.setAlignment(Pos.BASELINE_LEFT);

    VBox card = new VBox(6, symbolRow, priceRow, highLow);
    card.getStyleClass().add("stat-card");
    card.setPadding(new Insets(16));
    return card;
  }

  /**
   * <p>Creates a table header cell label. When {@code grow} is {@code true}
   * the cell expands to fill available space; when {@code false} it keeps a
   * fixed preferred width (used for the action column).</p>
   *
   * @param text     the header text
   * @param minWidth the minimum width in pixels
   * @param grow     whether the cell should grow to fill remaining space
   * @return the header label
   */
  private Label makeHeaderCell(String text, double minWidth, boolean grow) {
    Label l = new Label(text);
    l.getStyleClass().add("table-header-cell");
    l.setMinWidth(minWidth);
    if (grow) {
      l.setMaxWidth(Double.MAX_VALUE);
      HBox.setHgrow(l, Priority.ALWAYS);
    } else {
      l.setPrefWidth(minWidth);
    }
    return l;
  }

  /**
   * <p>Builds the order summary cost card.</p>
   *
   * @return the cost card container
   */
  private VBox buildCostCard() {
    Label title = new Label("Order Summary");
    title.getStyleClass().add("stat-card-title");

    estimatedCostLabel = makeCostRow("Estimated Cost", "$0.00");
    commissionLabel    = makeCostRow("Commission (0.5%)", "$0.00");

    Region divider = new Region();
    divider.getStyleClass().add("divider");
    divider.setPrefHeight(1);
    divider.setMaxWidth(Double.MAX_VALUE);

    totalCostLabel = makeCostRow("Total", "$0.00");
    totalCostLabel.getStyleClass().add("cost-row-total");

    VBox card = new VBox(10, title, estimatedCostLabel, commissionLabel, divider, totalCostLabel);
    card.getStyleClass().add("stat-card");
    card.setPadding(new Insets(16));
    return card;
  }

  /**
   * <p>Creates a cost row label with text and value.</p>
   *
   * @param labelText the label text
   * @param value     the value text
   * @return the cost row label
   */
  private Label makeCostRow(String labelText, String value) {
    // We return a container disguised as a label use HBox instead
    Label lbl = new Label(labelText + ":   " + value);
    lbl.getStyleClass().add("cost-row");
    lbl.setMaxWidth(Double.MAX_VALUE);
    lbl.setMinWidth(0);
    lbl.setTextOverrun(OverrunStyle.ELLIPSIS);
    return lbl;
  }

  /**
   * <p>Triggers a refresh of the cost preview based on current input.</p>
   */
  private void updateCostPreview() {
    if (onInputChanged != null) onInputChanged.run();
  }

  /**
   * <p>Adds a stock row to the stock list panel.</p>
   *
   * @param stock the read-only stock to display in the row
   */
  public void addStockRow(ReadOnlyStock stock) {
    BigDecimal change = stock.getLatestPriceChange();
    boolean isPositive = change.compareTo(BigDecimal.ZERO) >= 0;

    Circle icon = new Circle(18, Color.web("#6366f1"));
    Label letter = new Label(String.valueOf(stock.getSymbol().charAt(0)));
    letter.getStyleClass().add("mover-icon-letter");
    StackPane iconPane = new StackPane(icon, letter);

    Label nameLabel = new Label(stock.getSymbol());
    nameLabel.getStyleClass().add("mover-name");
    Label compLabel = new Label(stock.getCompany());
    compLabel.getStyleClass().add("mover-symbol");
    compLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
    compLabel.setMinWidth(0);
    compLabel.setMaxWidth(Double.MAX_VALUE);
    VBox nameBox = new VBox(2, nameLabel, compLabel);
    HBox.setHgrow(nameBox, Priority.ALWAYS);
    HBox stockCell = new HBox(10, iconPane, nameBox);
    stockCell.setAlignment(Pos.CENTER_LEFT);
    stockCell.setMinWidth(80);
    stockCell.setPrefWidth(190);
    stockCell.setMaxWidth(190);

    Label priceLabel  = makeDataCell(ViewFormatter.price(stock.getSalesPrice()),  70, "table-data-cell");
    Label changeLabel = makeDataCell(ViewFormatter.signedAmount(change),          70,
        isPositive ? "table-data-cell-profit" : "table-data-cell-loss");
    Label highLabel   = makeDataCell(ViewFormatter.price(stock.getHighestPrice()), 60, "table-data-cell");
    Label lowLabel    = makeDataCell(ViewFormatter.price(stock.getLowestPrice()),  60, "table-data-cell");

    Button selectBtn = new Button("Select");
    selectBtn.getStyleClass().add("select-btn");
    selectBtn.setPrefWidth(70);
    selectBtn.setMinWidth(65);

    HBox row = new HBox(stockCell, priceLabel, changeLabel, highLabel, lowLabel, selectBtn);
    row.setAlignment(Pos.CENTER_LEFT);
    row.setMaxWidth(Double.MAX_VALUE);
    row.getStyleClass().add("holding-row");
    row.setPadding(new Insets(10, 0, 10, 0));

    if (stock.getSymbol().equals(highlightedSymbol)) {
      row.getStyleClass().add("stock-row-selected");
      selectBtn.getStyleClass().add("select-btn-active");
      selectedRow = row;
      selectedButton = selectBtn;
    }

    selectBtn.setOnAction(e -> {
      if (selectedRow != null) selectedRow.getStyleClass().remove("stock-row-selected");
      if (selectedButton != null) selectedButton.getStyleClass().remove("select-btn-active");
      row.getStyleClass().add("stock-row-selected");
      selectBtn.getStyleClass().add("select-btn-active");
      selectedRow = row;
      selectedButton = selectBtn;
      selectStock(stock);
    });

    stockListContainer.getChildren().add(row);
  }

  /**
   * <p>Clears all stock rows from the list.</p>
   */
  public void clearStocks() {
    stockListContainer.getChildren().clear();
    selectedRow = null;
    selectedButton = null;
  }

  /**
   * <p>Selects a stock and updates the buy panel and chart.</p>
   *
   * @param stock the read-only stock that was selected
   */
  private void selectStock(ReadOnlyStock stock) {
    BigDecimal change = stock.getLatestPriceChange();
    boolean isPositive = change.compareTo(BigDecimal.ZERO) >= 0;

    buySymbolLabel.setText(stock.getSymbol());
    buyCompanyLabel.setText(stock.getCompany());
    buyPriceLabel.setText(ViewFormatter.price(stock.getSalesPrice()));
    buyChangeLabel.setText(ViewFormatter.priceChangeArrow(change));
    buyChangeLabel.getStyleClass().removeAll("mover-change-positive", "mover-change-negative");
    buyChangeLabel.getStyleClass().add(isPositive ? "mover-change-positive" : "mover-change-negative");
    buyHighLabel.setText("H: " + ViewFormatter.price(stock.getHighestPrice()));
    buyLowLabel.setText("L: " + ViewFormatter.price(stock.getLowestPrice()));

    stockChart.setStockInfo(stock.getSymbol(), stock.getCompany());
    actionButton.setDisable(false);
    highlightedSymbol = stock.getSymbol();

    if (onSelectStock != null) onSelectStock.accept(stock.getSymbol());
    updateCostPreview();
  }

  /**
   * <p>Updates the displayed order cost preview values.</p>
   *
   * @param gross      the estimated cost
   * @param commission the commission value
   * @param total      the total cost
   */
  public void setCostPreview(String gross, String commission, String total) {
    estimatedCostLabel.setText("Estimated Cost:   " + gross);
    commissionLabel.setText("Commission (0.5%):   " + commission);
    totalCostLabel.setText("Total:   " + total);
  }

  /**
   * <p>Returns the current input value as entered by the user. Returns
   * {@link BigDecimal#ZERO} if the field is empty or unparseable so callers
   * can compute previews without throwing.</p>
   *
   * @return the parsed input value
   */
  public BigDecimal getInputValue() {
    String text = inputSpinner.getEditor().getText();
    if (text == null || text.isBlank()) return BigDecimal.ZERO;
    try {
      return new BigDecimal(text.trim().replace(",", "."));
    } catch (NumberFormatException e) {
      return BigDecimal.ZERO;
    }
  }

  /**
   * <p>Returns whether the input field is in dollar-amount mode.</p>
   *
   * @return {@code true} if the user is entering a dollar amount,
   *         {@code false} for quantity (shares)
   */
  public boolean isAmountMode() {
    return amountMode;
  }

  /**
   * <p>Returns the current panel mode.</p>
   *
   * @return {@link Mode#BUY} or {@link Mode#SELL}
   */
  public Mode getMode() {
    return currentMode;
  }

  /**
   * <p>Updates the "Owned: X" hint shown above the input in Sell mode.
   * Pass a non-positive value to hide the label.</p>
   *
   * @param qty the owned quantity, or {@code null}/zero to hide
   */
  public void setOwnedQuantity(BigDecimal qty) {
    if (ownedValueLabel == null) return;
    ownedValueLabel.setText(
        (qty == null || qty.signum() <= 0) ? "–" : ViewFormatter.quantity(qty));
  }

  /**
   * <p>Updates the cash balance shown in the player info card.</p>
   *
   * @param cash the player's current cash balance
   */
  public void setCashBalance(BigDecimal cash) {
    if (cashBalanceLabel == null) return;
    cashBalanceLabel.setText(cash == null ? "$0.00" : ViewFormatter.price(cash));
  }

  /**
   * <p>Sets the symbol that should appear highlighted in the stock list.
   * Applied during the next {@link #addStockRow} call (i.e. after the next
   * render pass).</p>
   *
   * @param symbol the symbol to highlight, or {@code null} to clear
   */
  public void setHighlightedStock(String symbol) {
    this.highlightedSymbol = symbol;
  }

  /**
   * <p>Sets the small derived-value hint shown below the input field
   * (e.g. "≈ 2.78 shares" when in amount mode).</p>
   *
   * @param text the hint text, or empty string to clear
   */
  public void setDerivedLabel(String text) {
    derivedLabel.setText(text == null ? "" : text);
  }

  /**
   * <p>Enables or disables the action (Buy/Sell) button.</p>
   *
   * @param enabled {@code true} to enable
   */
  public void setActionEnabled(boolean enabled) {
    actionButton.setDisable(!enabled);
  }

  /**
   * <p>Clears the input field without triggering side effects beyond the
   * input-change listener.</p>
   */
  public void clearInput() {
    setSpinnerValue(BigDecimal.ZERO);
    inputSpinner.getEditor().clear();
    derivedLabel.setText("");
  }

  /**
   * <p>Sets the current sales price of the selected stock so the view can
   * convert between quantity- and amount-input when the user toggles the
   * input mode.</p>
   *
   * @param price the current stock price, may be {@code null} when no stock
   *              is selected
   */
  public void setCurrentPrice(BigDecimal price) {
    this.currentPrice = price;
  }

  /**
   * <p>Returns the search field used for stock filtering.</p>
   *
   * @return the search field
   */
  public TextField getSearchField() {
    return searchField;
  }

  /**
   * <p>Registers a handler that runs when the user confirms a buy or sell
   * via the action button. The handler receives the current panel mode.</p>
   *
   * @param handler the action handler
   */
  public void setOnAction(Consumer<Mode> handler) {
    this.onAction = handler;
  }

  /**
   * <p>Registers a handler that runs whenever the input field text or the
   * input mode (quantity/amount) changes.</p>
   *
   * @param handler the handler to run on change
   */
  public void setOnInputChanged(Runnable handler) {
    this.onInputChanged = handler;
  }

  /**
   * <p>Registers a handler that runs when the user toggles between Buy and
   * Sell mode.</p>
   *
   * @param handler the handler accepting the new mode
   */
  public void setOnModeChanged(Consumer<Mode> handler) {
    this.onModeChanged = handler;
  }

  /**
   * <p>Registers a handler that runs when a stock is selected, receiving its symbol.</p>
   *
   * @param handler the handler accepting the selected symbol
   */
  public void setOnSelectStock(Consumer<String> handler) {
    this.onSelectStock = handler;
  }

  /**
   * <p>Registers a handler that runs when the exchange updates, so the controller
   * can rebuild the stock list respecting any active search filter.</p>
   *
   * @param handler the handler to run on exchange update
   */
  public void setOnRefresh(Runnable handler) {
    this.onRefresh = handler;
  }

  /**
   * <p>Registers a handler that runs when the user clicks the "Load more" label.</p>
   *
   * @param handler the action to run
   */
  public void setOnLoadMore(Runnable handler) {
    this.onLoadMore = handler;
  }

  /**
   * <p>Shows or hides the "Load more" label below the stock list.</p>
   *
   * @param visible {@code true} to show the label, {@code false} to hide it
   */
  public void setLoadMoreVisible(boolean visible) {
    loadMoreLabel.setVisible(visible);
    loadMoreLabel.setManaged(visible);
  }

  @Override
  public void onExchangeUpdated(ReadOnlyExchange exchange) {
    if (onRefresh != null) onRefresh.run();
  }

  public StockChartComponent getStockChart() {
    return stockChart;
  }

  /**
   * <p>Creates a table data cell label that grows to fill available space.</p>
   *
   * @param text       the cell text
   * @param minWidth   the minimum width in pixels
   * @param styleClass the style class to apply
   * @return the data cell label
   */
  private Label makeDataCell(String text, double minWidth, String styleClass) {
    Label l = new Label(text);
    l.getStyleClass().add(styleClass);
    l.setMinWidth(minWidth);
    l.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(l, Priority.ALWAYS);
    return l;
  }

  /**
   * <p>Creates a vertical spacer region with a fixed height.</p>
   *
   * @param h the spacer height in pixels
   * @return the spacer region
   */
  private Region buildSpacer(double h) {
    Region r = new Region();
    r.setPrefHeight(h);
    return r;
  }

  /**
   * <p>Creates a divider line for section separation.</p>
   *
   * @return the divider region
   */
  private Region buildDivider() {
    Region d = new Region();
    d.getStyleClass().add("divider");
    d.setPrefHeight(1);
    d.setMaxWidth(Double.MAX_VALUE);
    return d;
  }
}
