package edu.ntnu.idi.idatt.millions.view.pages;

import edu.ntnu.idi.idatt.millions.model.Player;
import edu.ntnu.idi.idatt.millions.model.Portfolio;
import edu.ntnu.idi.idatt.millions.model.Share;
import edu.ntnu.idi.idatt.millions.observer.PlayerObserver;
import edu.ntnu.idi.idatt.millions.observer.PortfolioObserver;
import edu.ntnu.idi.idatt.millions.view.components.StockChartComponent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;



/**
 * <p>
 * Portfolio page view that renders balances, holdings, and a chart summary.
 * </p>
 *
 * <p>
 * The view exposes methods to refresh displayed data based on the current
 * {@link edu.ntnu.idi.idatt.millions.model.Player} state.
 * </p>
 */
public class PortfolioView extends VBox implements PortfolioObserver, PlayerObserver {
  private Player player;

  private Label netWorthLabel;
  private Label cashBalanceLabel;
  private Label portfolioValueLabel;
  private Label statusLabel;
  private VBox holdingsContainer;
  private Label emptyLabel;
  private StockChartComponent portfolioChart;

  /**
   * <p>Constructs the portfolio view and builds its initial layout.</p>
   */
  public PortfolioView() {
    getStyleClass().add("dashboard-view");
    setSpacing(24);
    setPadding(new Insets(40));

    getChildren().addAll(
            buildPortfolioChart(),
            buildHeader(),
            buildSummaryRow(),
            buildHoldingsSection()
    );
  }

  /**
   * <p>Builds the portfolio chart component.</p>
   *
   * @return the chart component
   */
  private StockChartComponent buildPortfolioChart() {
    portfolioChart = new StockChartComponent("–", "No stock selected");
    portfolioChart.setChartHeight(200);
    portfolioChart.setPrefHeight(250);
    return portfolioChart;
  }

  /**
   * <p>Builds the header section containing title and subtitle.</p>
   *
   * @return the header container
   */
  private VBox buildHeader() {
    Label title = new Label("My Portfolio");
    title.getStyleClass().add("page-title");

    Label subtitle = new Label("Your current holdings and balances");
    subtitle.getStyleClass().add("page-subtitle");

    return new VBox(4, title, subtitle);
  }

  /**
   * <p>Builds the summary row containing key portfolio metrics.</p>
   *
   * @return the summary row container
   */
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

  /**
   * <p>Builds a summary card with title and value labels.</p>
   *
   * @param title           the card title
   * @param value           the value text
   * @param valueStyleClass the style class applied to the value label
   * @return the summary card container
   */
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

  /**
   * <p>Builds the holdings section with table header and list container.</p>
   *
   * @return the holdings section container
   */
  private VBox buildHoldingsSection() {
    Label heading = new Label("Holdings");
    heading.getStyleClass().add("section-heading");

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

  /**
   * <p>Builds the table header row for the holdings list.</p>
   *
   * @return the table header container
   */
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

  @Override
  public void onPortfolioUpdated(Portfolio portfolio) {
    if (player != null) {
      refreshPortfolioData(player);
    }
  }

  @Override
  public void onPlayerUpdated(Player player) {
    this.player = player;
    refreshPortfolioData(player);
  }

  /**
   * <p>Refreshes the view with values from the provided player.</p>
   *
   * @param player the player whose portfolio data should be displayed
   */
  public void refreshPortfolioData(Player player) {
    Portfolio portfolio = player.getPortfolio();

    netWorthLabel.setText("$" + player.getNetWorth().toPlainString());
    cashBalanceLabel.setText("$" + player.getMoney().toPlainString());
    portfolioValueLabel.setText("$" + portfolio.getTotalValue().toPlainString());
    statusLabel.setText(player.getStatus().name());

    holdingsContainer.getChildren().clear();

    if (portfolio.getShares().isEmpty()) {
      holdingsContainer.getChildren().add(emptyLabel);
      return;
    }

    for (Share share : portfolio.getShares()) {
      Label row = new Label(
              share.getStock().getSymbol() + " x " + share.getQuantity()
      );
      row.getStyleClass().add("holding-row");
      holdingsContainer.getChildren().add(row);
    }
  }

  // --- helpers ---

  /**
   * <p>Creates a header cell label with a fixed width.</p>
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
