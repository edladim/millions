package edu.ntnu.idi.idatt.millions.view.pages;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.function.BiConsumer;

public class TradingView extends VBox {

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

  }

  private VBox buildStockListPanel() {
    VBox panel = new VBox(24);
    panel.setPadding(new Insets(40, 24, 40 ,24));
    panel.setMaxWidth(Double.MAX_VALUE);

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
            makeHeaderCell("Stock",         220),
            makeHeaderCell("Price",         120),
            makeHeaderCell("Change",        120),
            makeHeaderCell("High",          110),
            makeHeaderCell("Low",           110),
            makeHeaderCell("",               80)
    );
    return header;
  }

  private Label makeHeaderCell(String text, double width) {
    Label l = new Label(text);
    l.getStyleClass().add("table-header-cell");
    l.setPrefWidth(width);
    l.setMinWidth(width);
    return l;
  }

  private Region buildDivider() {
    Region d = new Region();
    d.getStyleClass().add("divider");
    d.setPrefHeight(1);
    d.setMaxWidth(Double.MAX_VALUE);
    return d;
  }
}
