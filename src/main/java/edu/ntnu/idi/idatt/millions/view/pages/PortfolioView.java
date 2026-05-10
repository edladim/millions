package edu.ntnu.idi.idatt.millions.view.pages;

import edu.ntnu.idi.idatt.millions.model.ReadOnlyPlayer;
import edu.ntnu.idi.idatt.millions.model.ReadOnlyPortfolio;
import edu.ntnu.idi.idatt.millions.model.Share;
import edu.ntnu.idi.idatt.millions.observer.PlayerObserver;
import edu.ntnu.idi.idatt.millions.observer.PortfolioObserver;
import edu.ntnu.idi.idatt.millions.view.ViewFormatter;
import edu.ntnu.idi.idatt.millions.view.components.StockChartComponent;
import java.math.BigDecimal;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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

  private static final String ZERO_PRICE     = ViewFormatter.price(BigDecimal.ZERO);
  private static final String VALUE_STYLE    = "stat-card-value";

  private ReadOnlyPlayer player;
  private Consumer<Share> onSell;

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
    setPadding(new Insets(20));

    getChildren().addAll(
            buildHeader(),
            buildPortfolioChart(),
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
    portfolioChart = new StockChartComponent("Portfolio value", "");
    portfolioChart.setYAxisLabel("Value ($)");
    portfolioChart.setChartHeight(240);
    portfolioChart.setPrefHeight(300);
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
   * <p>Uses an {@link HBox} where every card grows equally so they always
   * fill the full width of the view.</p>
   *
   * @return the summary row container
   */
  private HBox buildSummaryRow() {
    HBox row = new HBox(16);
    row.setMaxWidth(Double.MAX_VALUE);

    VBox netWorthCard   = buildSummaryCard("Net Worth",       ZERO_PRICE, VALUE_STYLE);
    VBox cashCard       = buildSummaryCard("Cash Balance",    ZERO_PRICE, VALUE_STYLE);
    VBox portfolioCard  = buildSummaryCard("Portfolio Value", ZERO_PRICE, VALUE_STYLE);
    VBox statusCard     = buildSummaryCard("Status",          "Novice",   "stat-card-value-status");

    netWorthLabel       = (Label) netWorthCard.getChildren().get(1);
    cashBalanceLabel    = (Label) cashCard.getChildren().get(1);
    portfolioValueLabel = (Label) portfolioCard.getChildren().get(1);
    statusLabel         = (Label) statusCard.getChildren().get(1);

    for (VBox card : new VBox[]{netWorthCard, cashCard, portfolioCard, statusCard}) {
      HBox.setHgrow(card, Priority.ALWAYS);
      card.setMaxWidth(Double.MAX_VALUE);
    }

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
    card.setPadding(new Insets(10));
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

    ScrollPane holdingsScroll = new ScrollPane(holdingsContainer);
    holdingsScroll.setFitToWidth(true);
    holdingsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    holdingsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    holdingsScroll.getStyleClass().add("stock-scroll");

    VBox section = new VBox(0, heading, buildSpacer(16), tableHeader, buildDivider(), holdingsScroll);
    section.getStyleClass().add("stat-card");
    section.setPadding(new Insets(24));
    section.setMinHeight(220);
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
    header.setMaxWidth(Double.MAX_VALUE);

    Label stock    = makeHeaderCell("Stock",         120, true);
    Label quantity = makeHeaderCell("Quantity",       60, true);
    Label buyPrice = makeHeaderCell("Buy Price",      70, true);
    Label current  = makeHeaderCell("Current Price",  80, true);
    Label value    = makeHeaderCell("Value",          70, true);
    Label gainLoss = makeHeaderCell("Gain / Loss",    80, true);
    Label actions  = makeHeaderCell("",               70, false);

    header.getChildren().addAll(stock, quantity, buyPrice, current, value, gainLoss, actions);
    return header;
  }

  @Override
  public void onPortfolioUpdated(ReadOnlyPortfolio portfolio) {
    if (player != null) {
      refreshPortfolioData(player);
    }
  }

  @Override
  public void onPlayerUpdated(ReadOnlyPlayer player) {
    this.player = player;
    refreshPortfolioData(player);
  }

  /**
   * <p>Refreshes the view with values from the provided player.</p>
   *
   * @param player a read-only view of the player whose portfolio data should be displayed
   */
  private void refreshPortfolioData(ReadOnlyPlayer player) {
    ReadOnlyPortfolio portfolio = player.getPortfolio();

    netWorthLabel.setText(ViewFormatter.price(player.getNetWorth()));
    cashBalanceLabel.setText(ViewFormatter.price(player.getMoney()));
    portfolioValueLabel.setText(ViewFormatter.price(portfolio.getTotalValue()));
    statusLabel.setText(player.getStatus().name());

    portfolioChart.setData(player.getHistoricalNetWorth());

    holdingsContainer.getChildren().clear();

    if (portfolio.getShares().isEmpty()) {
      holdingsContainer.getChildren().add(emptyLabel);
      return;
    }

    for (Share share : portfolio.getShares()) {
      holdingsContainer.getChildren().add(buildHoldingRow(share));
    }
  }

  /**
   * <p>Registers a handler that runs when the user requests to sell a share.</p>
   *
   * @param handler the handler receiving the share to sell
   */
  public void setOnSell(Consumer<Share> handler) {
    this.onSell = handler;
  }

  private HBox buildHoldingRow(Share share) {
    String symbol  = share.getStock().getSymbol();
    String company = share.getStock().getCompany();

    Label stockLabel = new Label(symbol + "\n" + company);
    stockLabel.getStyleClass().add("mover-name");
    stockLabel.setMinWidth(120);
    stockLabel.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(stockLabel, Priority.ALWAYS);

    BigDecimal gainOrLoss = share.getGainOrLoss();
    boolean isPositive = gainOrLoss.compareTo(BigDecimal.ZERO) >= 0;

    Label qtyLabel       = makeDataCell(ViewFormatter.quantity(share.getQuantity()),            60);
    Label buyPriceLabel  = makeDataCell(ViewFormatter.price(share.getPurchasePrice()),          70);
    Label currPriceLabel = makeDataCell(ViewFormatter.price(share.getStock().getSalesPrice()),  80);
    Label valueLabel     = makeDataCell(ViewFormatter.price(share.getCurrentValue()),           70);
    Label gainLabel      = makeDataCell(ViewFormatter.signedPrice(gainOrLoss),                  80);
    gainLabel.getStyleClass().add(isPositive ? "table-data-cell-profit" : "table-data-cell-loss");

    Button sellBtn = new Button("Sell");
    sellBtn.getStyleClass().add("select-btn");
    sellBtn.setPrefWidth(60);
    sellBtn.setOnAction(e -> { if (onSell != null) onSell.accept(share); });

    HBox row = new HBox(stockLabel, qtyLabel, buyPriceLabel, currPriceLabel, valueLabel, gainLabel, sellBtn);
    row.setAlignment(Pos.CENTER_LEFT);
    row.setMaxWidth(Double.MAX_VALUE);
    row.getStyleClass().add("holding-row");
    row.setPadding(new Insets(10, 0, 10, 0));
    return row;
  }

  /**
   * <p>Creates a data cell label. When {@code grow} is {@code true} the cell
   * expands to fill available horizontal space; otherwise it stays at its
   * minimum width.</p>
   *
   * @param text     the cell text
   * @param minWidth the minimum width in pixels
   * @return the data cell label
   */
  private Label makeDataCell(String text, double minWidth) {
    Label l = new Label(text);
    l.getStyleClass().add("table-data-cell");
    l.setMinWidth(minWidth);
    l.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(l, Priority.ALWAYS);
    return l;
  }

  /**
   * <p>Creates a header cell label. When {@code grow} is {@code true} the cell
   * expands to fill available horizontal space; when {@code false} it keeps a
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
