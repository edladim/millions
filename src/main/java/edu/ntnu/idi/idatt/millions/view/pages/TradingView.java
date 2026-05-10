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

import java.math.BigDecimal;
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

  private TextField searchField;
  private VBox stockListContainer;
  private StockChartComponent stockChart;
  private Label buySymbolLabel;
  private Label buyCompanyLabel;
  private Label buyPriceLabel;
  private Label buyHighLabel;
  private Label buyLowLabel;
  private Label buyChangeLabel;
  private Spinner<Integer> quantitySpinner;
  private Label estimatedCostLabel;
  private Label commissionLabel;
  private Label totalCostLabel;
  private Button buyButton;

  private Consumer<String> onBuy;
  private Runnable onQuantityChanged;
  private Consumer<String> onSelectStock;
  private Runnable onRefresh;

  /**
   * <p>Constructs the trading view and builds its initial layout.</p>
   */
  public TradingView() {
    getStyleClass().add("dashboard-view");

    setCenter(buildCenterPanel());
    setRight(buildBuyPanel());
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
    stockChart.setPrefHeight(350);
    stockChart.setMaxHeight(370);
    VBox.setVgrow(stockChart, Priority.ALWAYS);

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
    panel.setMaxHeight(550);
    VBox.setVgrow(panel, Priority.NEVER);

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

    VBox tableCard = new VBox(0, tableHeader, buildDivider(), scroll);
    tableCard.getStyleClass().add("stat-card");
    tableCard.setPadding(new Insets(24));
    VBox.setVgrow(tableCard, Priority.ALWAYS);

    panel.getChildren().addAll(header, searchField, tableCard);
    VBox.setVgrow(panel, Priority.NEVER);
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

    header.getChildren().addAll(
            makeHeaderCell("Stock", 220),
            makeHeaderCell("Price", 120),
            makeHeaderCell("Change", 120),
            makeHeaderCell("High", 110),
            makeHeaderCell("Low", 110),
            makeHeaderCell("", 80)
    );
    return header;
  }

  /**
   * <p>Builds the buy panel containing stock info, quantity input, and summary.</p>
   *
   * @return the buy panel container
   */
  private VBox buildBuyPanel() {
    VBox panel = new VBox(20);
    panel.getStyleClass().add("buy-panel");
    panel.setPrefHeight(250);
    panel.setMinWidth(320);
    panel.setMaxWidth(400);
    panel.setPadding(new Insets(40, 24, 40, 24));

    Label panelTitle = new Label("Buy Stock");
    panelTitle.getStyleClass().add("section-heading");

    VBox stockInfoCard = buildSelectedStockInfo();

    Label qtyLabel = new Label("Quantity");
    qtyLabel.getStyleClass().add("input-label");
    quantitySpinner = new Spinner<>(1, 10000, 1);
    quantitySpinner.setEditable(true);
    quantitySpinner.getStyleClass().add("search-field");
    quantitySpinner.valueProperty().addListener((_, _, _) -> updateCostPreview());

    VBox costCard = buildCostCard();

    buyButton = new Button("Buy now");
    buyButton.getStyleClass().add("buy-btn");
    buyButton.setMaxWidth(Double.MAX_VALUE);
    buyButton.setDisable(true);
    buyButton.setOnAction(e -> {
      if (onBuy != null) {
        onBuy.accept(quantitySpinner.getValue().toString());
      }
    });

    panel.getChildren().addAll(
            panelTitle,
            stockInfoCard,
            buildSpacer(4),
            qtyLabel,
            quantitySpinner,
            costCard,
            buildSpacer(4),
            buyButton
    );

    VBox.setVgrow(costCard, Priority.NEVER);
    return panel;
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

    VBox card = new VBox(6, buySymbolLabel, buyCompanyLabel, priceRow, highLow);
    card.getStyleClass().add("stat-card");
    card.setPadding(new Insets(16));
    return card;
  }

  /**
   * <p>Creates a table header cell label with a fixed width.</p>
   *
   * @param text  the header text
   * @param width the fixed width in pixels
   * @return the header label
   */
  private Label makeHeaderCell(String text, double width) {
    Label l = new Label(text);
    l.getStyleClass().add("table-header-cell");
    l.setPrefWidth(width);
    l.setMinWidth(width);
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
    return lbl;
  }

  /**
   * <p>Triggers a refresh of the cost preview based on current input.</p>
   */
  private void updateCostPreview() {
    if (onQuantityChanged != null) onQuantityChanged.run();
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
    VBox nameBox = new VBox(2, nameLabel, compLabel);
    HBox stockCell = new HBox(10, iconPane, nameBox);
    stockCell.setAlignment(Pos.CENTER_LEFT);
    stockCell.setPrefWidth(220);

    Label priceLabel  = makeDataCell(ViewFormatter.price(stock.getSalesPrice()),       120, "table-data-cell");
    Label changeLabel = makeDataCell(ViewFormatter.signedAmount(change),               120,
        isPositive ? "table-data-cell-profit" : "table-data-cell-loss");
    Label highLabel   = makeDataCell(ViewFormatter.price(stock.getHighestPrice()),     110, "table-data-cell");
    Label lowLabel    = makeDataCell(ViewFormatter.price(stock.getLowestPrice()),      110, "table-data-cell");

    Button selectBtn = new Button("Select");
    selectBtn.getStyleClass().add("select-btn");
    selectBtn.setPrefWidth(70);
    selectBtn.setOnAction(e -> selectStock(stock));

    HBox row = new HBox(stockCell, priceLabel, changeLabel, highLabel, lowLabel, selectBtn);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("holding-row");
    row.setPadding(new Insets(10, 0, 10, 0));

    stockListContainer.getChildren().add(row);
  }

  /**
   * <p>Clears all stock rows from the list.</p>
   */
  public void clearStocks() {
    stockListContainer.getChildren().clear();
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

    buyButton.setDisable(false);

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
   * <p>Returns the current quantity input value.</p>
   *
   * @return the quantity text
   */
  public String getQuantityInput() {
    return quantitySpinner.getValue().toString();
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
   * <p>Registers a handler that runs when the user confirms a buy.</p>
   *
   * @param handler the buy handler accepting the quantity string
   */
  public void setOnBuy(Consumer<String> handler) {
    this.onBuy = handler;
  }

  /**
   * <p>Registers a handler that runs when the quantity spinner value changes.</p>
   *
   * @param handler the handler to run on change
   */
  public void setOnQuantityChanged(Runnable handler) {
    this.onQuantityChanged = handler;
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

  @Override
  public void onExchangeUpdated(ReadOnlyExchange exchange) {
    if (onRefresh != null) onRefresh.run();
  }

  public StockChartComponent getStockChart() {
    return stockChart;
  }

  /**
   * <p>Creates a table data cell label with a fixed width.</p>
   *
   * @param text       the cell text
   * @param width      the fixed width in pixels
   * @param styleClass the style class to apply
   * @return the data cell label
   */
  private Label makeDataCell(String text, double width, String styleClass) {
    Label l = new Label(text);
    l.getStyleClass().add(styleClass);
    l.setPrefWidth(width);
    l.setMinWidth(width);
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
