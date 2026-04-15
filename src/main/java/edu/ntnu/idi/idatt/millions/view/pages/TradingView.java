package edu.ntnu.idi.idatt.millions.view.pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

import java.util.function.BiConsumer;

public class TradingView extends BorderPane {

  private TextField searchField;
  private VBox stockListContainer;
  private Label buySymbolLabel;
  private Label buyCompanyLabel;
  private Label buyPriceLabel;
  private Label buyHighLabel;
  private Label buyLowLabel;
  private Label buyChangeLabel;
  private TextField quantityField;
  private Label estimatedCostLabel;
  private Label commissionLabel;
  private Label totalCostLabel;
  private Button buyButton;

  private String selectedSymbol = null;
  private BiConsumer<String, String> onBuy;

  public TradingView() {
    getStyleClass().add("dashboard-view");

    setCenter(buildStockListPanel());
    setRight(buildBuyPanel());
  }

  private VBox buildStockListPanel() {
    VBox panel = new VBox(16);
    panel.setPadding(new Insets(24));
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

    panel.getChildren().addAll(scroll, searchField, tableCard);
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

    VBox stockInfoCard = buildSelectedStockInfo();

    Label qtyLabel = new Label("Quantity");
    qtyLabel.getStyleClass().add("input-label");
    quantityField = new TextField("1");
    quantityField.getStyleClass().add("search-field");
    quantityField.textProperty().addListener((obs, old, val) -> updateCostPreview());

    VBox costCard = buildCostCard();

    buyButton = new Button("Buy now");
    buyButton.getStyleClass().add("buy-btn");
    buyButton.setMaxWidth(Double.MAX_VALUE);
    buyButton.setDisable(true);
    buyButton.setOnAction(e -> {
      if (onBuy != null && selectedSymbol != null) {
        onBuy.accept(selectedSymbol, quantityField.getText());
      }
    });

    Label noneSelected = new Label("Select a stock from the list to start trading");
    noneSelected.getStyleClass().add("empty-label");
    noneSelected.setWrapText(true);

    panel.getChildren().addAll(
            panelTitle,
            noneSelected,
            stockInfoCard,
            buildSpacer(4),
            qtyLabel,
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
