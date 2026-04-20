package edu.ntnu.idi.idatt.millions.view.pages;

import edu.ntnu.idi.idatt.millions.view.components.StockChartComponent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.function.BiConsumer;

public class TradingView extends BorderPane {

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

  private String selectedSymbol = null;
  private BiConsumer<String, String> onBuy;

  public TradingView() {
    getStyleClass().add("dashboard-view");

    setCenter(buildCenterPanel());
    setRight(buildBuyPanel());
  }

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

    VBox tableCard = new VBox(0, tableHeader, buildDivider(), scroll);
    tableCard.getStyleClass().add("stat-card");
    tableCard.setPadding(new Insets(24));
    VBox.setVgrow(tableCard, Priority.ALWAYS);

    panel.getChildren().addAll(header, searchField, tableCard);
    VBox.setVgrow(panel, Priority.ALWAYS);
    return panel;
  }

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

  private VBox buildBuyPanel() {
    VBox panel = new VBox(20);
    panel.getStyleClass().add("buy-panel");
    panel.setPrefHeight(250);
    panel.setMinWidth(320);
    panel.setMaxWidth(400);
    panel.setPadding(new Insets(40, 24, 40, 24));

    Label panelTitle = new Label("Buy Stock");
    panelTitle.getStyleClass().add("section-heading");

    TextField buySearchField = new TextField();
    buySearchField.setPromptText("Search stocks...");
    buySearchField.getStyleClass().add("search-field");
    buySearchField.setMaxWidth(Double.MAX_VALUE);

    VBox stockInfoCard = buildSelectedStockInfo();

    Label qtyLabel = new Label("Quantity");
    qtyLabel.getStyleClass().add("input-label");
    quantitySpinner = new Spinner<>(1, 10000, 1);
    quantitySpinner.setEditable(true);
    quantitySpinner.getStyleClass().add("search-field");
    quantitySpinner.valueProperty().addListener((obs, old, val) -> updateCostPreview());

    VBox costCard = buildCostCard();

    buyButton = new Button("Buy now");
    buyButton.getStyleClass().add("buy-btn");
    buyButton.setMaxWidth(Double.MAX_VALUE);
    buyButton.setDisable(true);
    buyButton.setOnAction(e -> {
      if (onBuy != null && selectedSymbol != null) {
        onBuy.accept(selectedSymbol, quantitySpinner.getValue().toString());
      }
    });

    panel.getChildren().addAll(
            panelTitle,
            buySearchField,
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

  private Label makeHeaderCell(String text, double width) {
    Label l = new Label(text);
    l.getStyleClass().add("table-header-cell");
    l.setPrefWidth(width);
    l.setMinWidth(width);
    return l;
  }

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

  private Label makeCostRow(String labelText, String value) {
    // We return a container disguised as a label – use HBox instead
    Label lbl = new Label(labelText + ":   " + value);
    lbl.getStyleClass().add("cost-row");
    lbl.setMaxWidth(Double.MAX_VALUE);
    return lbl;
  }

  private void updateCostPreview() {
    // Controller should observe quantityField and push updates via setCostPreview()
  }

  public void addStockRow(String symbol, String company, String price,
                          String change, String high, String low,
                          boolean isPositive) {

    Circle icon = new Circle(18, Color.web("#6366f1"));
    Label letter = new Label(String.valueOf(symbol.charAt(0)));
    letter.getStyleClass().add("mover-icon-letter");
    StackPane iconPane = new StackPane(icon, letter);

    Label nameLabel = new Label(symbol);
    nameLabel.getStyleClass().add("mover-name");
    Label compLabel = new Label(company);
    compLabel.getStyleClass().add("mover-symbol");
    VBox nameBox = new VBox(2, nameLabel, compLabel);
    HBox stockCell = new HBox(10, iconPane, nameBox);
    stockCell.setAlignment(Pos.CENTER_LEFT);
    stockCell.setPrefWidth(220);

    Label priceLabel  = makeDataCell(price,  120, "table-data-cell");
    Label changeLabel = makeDataCell(change, 120,
        isPositive ? "table-data-cell-profit" : "table-data-cell-loss");
    Label highLabel   = makeDataCell(high,   110, "table-data-cell");
    Label lowLabel    = makeDataCell(low,    110, "table-data-cell");

    Button selectBtn = new Button("Select");
    selectBtn.getStyleClass().add("select-btn");
    selectBtn.setPrefWidth(70);
    selectBtn.setOnAction(e -> selectStock(symbol, company, price, change, high, low, isPositive));

    HBox row = new HBox(stockCell, priceLabel, changeLabel, highLabel, lowLabel, selectBtn);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("holding-row");
    row.setPadding(new Insets(10, 0, 10, 0));

    stockListContainer.getChildren().add(row);
  }

  public void clearStocks() {
    stockListContainer.getChildren().clear();
  }

  private void selectStock(String symbol, String company, String price,
                           String change, String high, String low, boolean isPositive) {
    selectedSymbol = symbol;
    buySymbolLabel.setText(symbol);
    buyCompanyLabel.setText(company);
    buyPriceLabel.setText(price);
    buyChangeLabel.setText((isPositive ? "↗ " : "↘ ") + change);
    buyChangeLabel.getStyleClass().removeAll("mover-change-positive", "mover-change-negative");
    buyChangeLabel.getStyleClass().add(isPositive ? "mover-change-positive" : "mover-change-negative");
    buyHighLabel.setText("H: " + high);
    buyLowLabel.setText("L: " + low);

    stockChart.setStockInfo(symbol, company);
    buyButton.setDisable(false);
  }

  public void setCostPreview(String gross, String commission, String total) {
    estimatedCostLabel.setText("Estimated Cost:   " + gross);
    commissionLabel.setText("Commission (0.5%):   " + commission);
    totalCostLabel.setText("Total:   " + total);
  }

  public String getQuantityInput() {
    return quantitySpinner.getValue().toString();
  }

  public TextField getSearchField() {
    return searchField;
  }

  public void setOnBuy(BiConsumer<String, String> handler) {
    this.onBuy = handler;
  }

  private Label makeDataCell(String text, double width, String styleClass) {
    Label l = new Label(text);
    l.getStyleClass().add(styleClass);
    l.setPrefWidth(width);
    l.setMinWidth(width);
    return l;
  }

  private Region buildSpacer(double h) {
    Region r = new Region();
    r.setPrefHeight(h);
    return r;
  }

  private Region buildDivider() {
    Region d = new Region();
    d.getStyleClass().add("divider");
    d.setPrefHeight(1);
    d.setMaxWidth(Double.MAX_VALUE);
    return d;
  }
}
