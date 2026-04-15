package edu.ntnu.idi.idatt.millions.view.pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import javax.swing.text.Position;

public class PortfolioView extends VBox {

  private Label netWorthLabel;
  private Label cashBalanceLabel;
  private Label portfolioValueLabel;
  private Label statusLabel;
  private VBox holdingsContainer;
  private Label emptyLabel;

  public PortfolioView() {
    getStyleClass().add("dashboard-view");
    setSpacing(24);
    setPadding(new Insets(40));


  }

  private VBox buildHeader() {
    Label title = new Label("My Portfolio");
    title.getStyleClass().add("page-title");

    Label subtitle = new Label("Your current holdings and balances");
    subtitle.getStyleClass().add("page-subtitle");

    return new VBox(4, title, subtitle);
  }

  private HBox buildSummaryRow() {
    HBox row = new HBox(16);
    row.setMaxWidth(Double.MAX_VALUE);

    VBox netWorthCard = buildSummaryCard("Net Worth", "$0.00", "stat-card-value");
    VBox cashCard = buildSummaryCard("Cash Balance", "$0.00", "stat-card-value");
    VBox portfolioCard = buildSummaryCard("Portfolio Value", "$0.00", "stat-card-value");
    VBox statusCard = buildSummaryCard("Status", "Novice", "stat-card-value-status");

    netWorthLabel = (Label) netWorthCard.getChildren().get(1);
    cashBalanceLabel = (Label) cashCard.getChildren().get(1);
    portfolioValueLabel = (Label) portfolioCard.getChildren().get(1);
    statusLabel = (Label) statusCard.getChildren().get(1);

    HBox.setHgrow(netWorthCard, Priority.ALWAYS);
    HBox.setHgrow(cashCard, Priority.ALWAYS);
    HBox.setHgrow(portfolioCard, Priority.ALWAYS);
    HBox.setHgrow(statusCard, Priority.ALWAYS);

    row.getChildren().addAll(netWorthCard, cashCard, portfolioCard, statusCard);
    return row;
  }

  private VBox buildSummaryCard(String title, String value, String valueStyleClass) {
    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().add("stat-card-title");

    Label valueLabel = new Label(value);
    valueLabel.getStyleClass().add(valueStyleClass);

    VBox card = new VBox(12, titleLabel, valueLabel);
    card.getStyleClass().add("stat-card");
    card.setPadding(new Insets(24));
    card.setMaxWidth(Double.MAX_VALUE);
    return card;
  }

  private VBox buildHoldingsSection() {
    Label heading = new Label("Holdings");
    heading.getStyleClass().add("section-heading");

    // Table header
    HBox tableHeader = buildTableHeader();

    holdingsContainer = new VBox(4);

    emptyLabel = new Label("You don't own any shares yet. Head to Trading to get started.");
    emptyLabel.getStyleClass().add("empty-label");
    emptyLabel.setMaxWidth(Double.MAX_VALUE);
    emptyLabel.setAlignment(Pos.CENTER);
    holdingsContainer.getChildren().add(emptyLabel);

    VBox section = new VBox(0, heading, buildSpacer(16), tableHeader, buildDivider(), holdingsContainer);
    section.getStyleClass().add("stat-card");
    section.setPadding(new Insets(24));
    VBox.setVgrow(section, Priority.ALWAYS);
    return section;
  }

  private HBox buildTableHeader() {
    HBox header = new HBox();
    header.getStyleClass().add("table-header-row");
    header.setPadding(new Insets(0, 0, 8, 0));

    Label stock    = makeHeaderCell("Stock",          200);
    Label quantity = makeHeaderCell("Quantity",       120);
    Label buyPrice = makeHeaderCell("Buy Price",      140);
    Label current  = makeHeaderCell("Current Price",  140);
    Label value    = makeHeaderCell("Value",          140);
    Label gainLoss = makeHeaderCell("Gain / Loss",    140);
    Label actions  = makeHeaderCell("",               100);

    header.getChildren().addAll(stock, quantity, buyPrice, current, value, gainLoss, actions);
    return header;
  }

  private Label makeHeaderCell(String text, double width) {
    Label l = new Label(text);
    l.getStyleClass().add("table-header-cell");
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
