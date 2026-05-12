package edu.ntnu.idi.idatt.millions.view.pages;

import edu.ntnu.idi.idatt.millions.model.PlayerStatus;
import edu.ntnu.idi.idatt.millions.model.ReadOnlyPlayer;
import edu.ntnu.idi.idatt.millions.model.ReadOnlyPortfolio;
import edu.ntnu.idi.idatt.millions.model.Share;
import edu.ntnu.idi.idatt.millions.model.transaction.Purchase;
import edu.ntnu.idi.idatt.millions.model.transaction.Transaction;
import edu.ntnu.idi.idatt.millions.observer.PlayerObserver;
import edu.ntnu.idi.idatt.millions.observer.PortfolioObserver;
import edu.ntnu.idi.idatt.millions.view.ViewFormatter;
import edu.ntnu.idi.idatt.millions.view.components.StockChartComponent;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
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

  private static final String ZERO_PRICE  = ViewFormatter.price(BigDecimal.ZERO);
  private static final String VALUE_STYLE = "stat-card-value";
  private static final int PAGE_SIZE = 6;

  private ReadOnlyPlayer player;
  private Consumer<Share> onSell;

  private Label netWorthLabel;
  private Label cashBalanceLabel;
  private Label portfolioValueLabel;
  private Label statusLabel;
  private VBox holdingsContainer;
  private Label emptyLabel;
  private Label loadMoreHoldingsLabel;
  private VBox transactionContainer;
  private Label emptyTransactionLabel;
  private Label loadMoreTxLabel;
  private StockChartComponent portfolioChart;

  private int holdingsDisplayCount = PAGE_SIZE;
  private int txDisplayCount = PAGE_SIZE;

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
            buildHoldingsSection(),
            buildTransactionHistorySection()
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

    VBox netWorthCard = buildSummaryCard("Net Worth", ZERO_PRICE, VALUE_STYLE);
    VBox cashCard = buildSummaryCard("Cash Balance", ZERO_PRICE, VALUE_STYLE);
    VBox portfolioCard = buildSummaryCard("Portfolio Value", ZERO_PRICE, VALUE_STYLE);
    VBox statusCard = buildSummaryCard("Status", "Novice",   "stat-card-value-status");

    netWorthLabel = (Label) netWorthCard.getChildren().get(1);
    cashBalanceLabel = (Label) cashCard.getChildren().get(1);
    portfolioValueLabel = (Label) portfolioCard.getChildren().get(1);
    statusLabel = (Label) statusCard.getChildren().get(1);

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
    holdingsContainer = new VBox(4);

    emptyLabel = new Label("You don't own any shares yet. Head to Trading to get started.");
    emptyLabel.getStyleClass().add("empty-label");
    emptyLabel.setMaxWidth(Double.MAX_VALUE);
    emptyLabel.setAlignment(Pos.CENTER);
    holdingsContainer.getChildren().add(emptyLabel);

    Label heading = new Label("Holdings");
    heading.getStyleClass().add("section-heading");

    HBox tableHeader = buildHoldingsTableHeader();

    ScrollPane holdingsScroll = new ScrollPane(holdingsContainer);
    holdingsScroll.setFitToWidth(true);
    holdingsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    holdingsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    holdingsScroll.getStyleClass().add("stock-scroll");

    loadMoreHoldingsLabel = new Label("Load more");
    loadMoreHoldingsLabel.getStyleClass().add("load-more-label");
    loadMoreHoldingsLabel.setMaxWidth(Double.MAX_VALUE);
    loadMoreHoldingsLabel.setAlignment(Pos.CENTER);
    loadMoreHoldingsLabel.setVisible(false);
    loadMoreHoldingsLabel.setManaged(false);
    loadMoreHoldingsLabel.setOnMouseClicked(e -> {
      holdingsDisplayCount += PAGE_SIZE;
      if (player != null) refreshPortfolioData(player);
    });

    VBox section = new VBox(0, heading, buildSpacer(16), tableHeader, buildDivider(),
        holdingsScroll, loadMoreHoldingsLabel);
    section.getStyleClass().add("stat-card");
    section.setPadding(new Insets(24));
    section.setMinHeight(220);
    return section;
  }

  /**
   * <p>Builds the responsive table header row for the holdings list.
   * Column labels abbreviate when the container is narrow.</p>
   *
   * @return the table header container
   */
  private HBox buildHoldingsTableHeader() {
    HBox header = new HBox();
    header.getStyleClass().add("table-header-row");
    header.setPadding(new Insets(0, 0, 8, 0));
    header.setMaxWidth(Double.MAX_VALUE);

    Label stock = makeHeaderCell("Stock", 80, true);
    stock.setPrefWidth(150);
    stock.setMaxWidth(Double.MAX_VALUE);

    Label quantity = makeFixedHeaderCell("Quantity", 75);
    quantity.textProperty().bind(
        Bindings.when(holdingsContainer.widthProperty().lessThan(700))
            .then("QTY").otherwise("Quantity"));

    Label buyPrice = makeFixedHeaderCell("Buy Price", 85);
    buyPrice.textProperty().bind(
        Bindings.when(holdingsContainer.widthProperty().lessThan(700))
            .then("BP").otherwise("Buy Price"));

    Label current = makeFixedHeaderCell("Current Price", 95);
    current.textProperty().bind(
        Bindings.when(holdingsContainer.widthProperty().lessThan(700))
            .then("CP").otherwise("Current Price"));

    Label value    = makeFixedHeaderCell("Value",       80);
    Label gainLoss = makeFixedHeaderCell("Gain / Loss", 90);
    gainLoss.textProperty().bind(
        Bindings.when(holdingsContainer.widthProperty().lessThan(700))
            .then("G/L").otherwise("Gain / Loss"));

    Label actions = makeHeaderCell("", 45, true);
    actions.setPrefWidth(90);

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

    PlayerStatus status = player.getStatus();
    String statusName = status.toString().replace("_", " ");
    statusLabel.setText(statusName.charAt(0) + statusName.substring(1).toUpperCase());
    statusLabel.getStyleClass().removeAll(
            "stat-card-value-status-worst"
            , "stat-card-value-status-bad"
            , "stat-card-value-status-investor"
            , "stat-card-value-status-average"
            , "stat-card-value-status-good"
            , "stat-card-value-status-excellent"
    );
    if (status == PlayerStatus.BUY_HIGH_BJORN) {
      statusLabel.getStyleClass().add("stat-card-value-status-worst");
    } else if (status == PlayerStatus.MAX_MINUS) {
      statusLabel.getStyleClass().add("stat-card-value-status-bad");
    } else if (status == PlayerStatus.AVERAGE_JOE) {
      statusLabel.getStyleClass().add("stat-card-value-status-average");
    } else if (status == PlayerStatus.INVESTOR) {
      statusLabel.getStyleClass().add("stat-card-value-status-investor");
    } else if (status == PlayerStatus.RAY_DAILO) {
      statusLabel.getStyleClass().add("stat-card-value-status-good");
    } else if (status == PlayerStatus.BERNARD_MADOFF) {
      statusLabel.getStyleClass().add("stat-card-value-status-excellent");
    }


    portfolioChart.setData(player.getHistoricalNetWorth());

    holdingsContainer.getChildren().clear();
    List<Share> shares = portfolio.getShares();
    if (shares.isEmpty()) {
      holdingsContainer.getChildren().add(emptyLabel);
      setLoadMoreVisible(loadMoreHoldingsLabel, false);
    } else {
      int toShow = Math.min(holdingsDisplayCount, shares.size());
      for (int i = 0; i < toShow; i++) {
        holdingsContainer.getChildren().add(buildHoldingRow(shares.get(i)));
      }
      setLoadMoreVisible(loadMoreHoldingsLabel, toShow < shares.size());
    }

    List<Transaction> transactions = player.getTransactions();
    transactionContainer.getChildren().clear();
    if (transactions.isEmpty()) {
      transactionContainer.getChildren().add(emptyTransactionLabel);
      setLoadMoreVisible(loadMoreTxLabel, false);
    } else {
      int toShow = Math.min(txDisplayCount, transactions.size());
      for (int i = transactions.size() - 1; i >= transactions.size() - toShow; i--) {
        transactionContainer.getChildren().add(buildTransactionRow(transactions.get(i)));
      }
      setLoadMoreVisible(loadMoreTxLabel, toShow < transactions.size());
    }
  }

  private void setLoadMoreVisible(Label label, boolean visible) {
    label.setVisible(visible);
    label.setManaged(visible);
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
    String symbol = share.getStock().getSymbol();
    String company = share.getStock().getCompany();

    Label stockLabel = new Label(symbol + "\n" + company);
    stockLabel.getStyleClass().add("mover-name");
    stockLabel.setMinWidth(80);
    stockLabel.setPrefWidth(150);
    stockLabel.setMaxWidth(Double.MAX_VALUE);
    stockLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
    HBox.setHgrow(stockLabel, Priority.ALWAYS);

    BigDecimal gainOrLoss = share.getGainOrLoss();
    boolean isPositive = gainOrLoss.compareTo(BigDecimal.ZERO) >= 0;

    Label qtyLabel = makeFixedDataCell(ViewFormatter.quantity(share.getQuantity()), 75);
    Label buyPriceLabel = makeFixedDataCell(ViewFormatter.price(share.getPurchasePrice()), 85);
    Label currPriceLabel = makeFixedDataCell(ViewFormatter.price(share.getStock().getSalesPrice()), 95);
    Label valueLabel = makeFixedDataCell(ViewFormatter.price(share.getCurrentValue()), 80);
    Label gainLabel = makeFixedDataCell(ViewFormatter.signedPrice(gainOrLoss), 90);
    gainLabel.getStyleClass().add(isPositive ? "table-data-cell-profit" : "table-data-cell-loss");

    Button sellBtn = new Button("Quick Sell");
    sellBtn.getStyleClass().add("select-btn");
    sellBtn.setMinWidth(45);
    sellBtn.setPrefWidth(90);
    sellBtn.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(sellBtn, Priority.ALWAYS);
    sellBtn.textProperty().bind(
        Bindings.when(holdingsContainer.widthProperty().lessThan(700))
            .then("QS")
            .otherwise("Quick Sell")
    );
    sellBtn.setOnAction(e -> { if (onSell != null) onSell.accept(share); });

    HBox row = new HBox(stockLabel, qtyLabel, buyPriceLabel, currPriceLabel, valueLabel, gainLabel, sellBtn);
    row.setAlignment(Pos.CENTER_LEFT);
    row.setMaxWidth(Double.MAX_VALUE);
    row.getStyleClass().add("holding-row");
    row.setPadding(new Insets(10, 0, 10, 0));
    return row;
  }

  /**
   * <p>Builds the transaction history section with table header and list container.</p>
   *
   * @return the transaction history section container
   */
  private VBox buildTransactionHistorySection() {
    transactionContainer = new VBox(4);

    emptyTransactionLabel = new Label("No transactions yet. Buy or sell stocks to see your history.");
    emptyTransactionLabel.getStyleClass().add("empty-label");
    emptyTransactionLabel.setMaxWidth(Double.MAX_VALUE);
    emptyTransactionLabel.setAlignment(Pos.CENTER);
    transactionContainer.getChildren().add(emptyTransactionLabel);

    Label heading = new Label("Transaction History");
    heading.getStyleClass().add("section-heading");

    HBox tableHeader = buildTransactionTableHeader();

    ScrollPane txScroll = new ScrollPane(transactionContainer);
    txScroll.setFitToWidth(true);
    txScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    txScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    txScroll.getStyleClass().add("stock-scroll");

    loadMoreTxLabel = new Label("Load more");
    loadMoreTxLabel.getStyleClass().add("load-more-label");
    loadMoreTxLabel.setMaxWidth(Double.MAX_VALUE);
    loadMoreTxLabel.setAlignment(Pos.CENTER);
    loadMoreTxLabel.setVisible(false);
    loadMoreTxLabel.setManaged(false);
    loadMoreTxLabel.setOnMouseClicked(e -> {
      txDisplayCount += PAGE_SIZE;
      if (player != null) refreshPortfolioData(player);
    });

    VBox section = new VBox(0, heading, buildSpacer(16), tableHeader, buildDivider(),
        txScroll, loadMoreTxLabel);
    section.getStyleClass().add("stat-card");
    section.setPadding(new Insets(24));
    section.setMinHeight(220);
    return section;
  }

  /**
   * <p>Builds the table header row for the transaction history list.</p>
   *
   * @return the table header container
   */
  private HBox buildTransactionTableHeader() {
    HBox header = new HBox();
    header.getStyleClass().add("table-header-row");
    header.setPadding(new Insets(0, 0, 8, 0));
    header.setMaxWidth(Double.MAX_VALUE);

    Label txStock = makeHeaderCell("Stock", 80, true);
    txStock.setPrefWidth(150);
    txStock.setMaxWidth(Double.MAX_VALUE);

    header.getChildren().addAll(
        txStock,
        makeFixedHeaderCell("Quantity", 70),
        makeFixedHeaderCell("Price", 80),
        makeFixedHeaderCell("Value", 80),
        makeFixedHeaderCell("Type", 80),
        makeFixedHeaderCell("Week", 70)
    );
    return header;
  }

  /**
   * <p>Builds a single transaction history row.</p>
   *
   * <p>Rows are coloured green for purchases and red for sales.</p>
   *
   * @param tx the transaction to display
   * @return the row container
   */
  private HBox buildTransactionRow(Transaction tx) {
    boolean isBuy = tx instanceof Purchase;

    String symbol = tx.getShare().getStock().getSymbol();
    String company = tx.getShare().getStock().getCompany();

    Label stockLabel = new Label(symbol + "\n" + company);
    stockLabel.getStyleClass().add("mover-name");
    stockLabel.setMinWidth(80);
    stockLabel.setPrefWidth(150);
    stockLabel.setMaxWidth(Double.MAX_VALUE);
    stockLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
    HBox.setHgrow(stockLabel, Priority.ALWAYS);

    Label qtyLabel = makeFixedDataCell(ViewFormatter.quantity(tx.getShare().getQuantity()), 70);
    Label priceLabel = makeFixedDataCell(ViewFormatter.price(tx.getShare().getPurchasePrice()), 80);
    Label valueLabel = makeFixedDataCell(ViewFormatter.price(tx.getCalculator().calculateGross()), 80);

    Label typeLabel = makeFixedDataCell(isBuy ? "Buy" : "Sell", 80);
    typeLabel.getStyleClass().add(isBuy ? "tx-type-buy" : "tx-type-sell");

    Label weekLabel = makeFixedDataCell("W" + tx.getWeek(), 70);

    HBox row = new HBox(stockLabel, qtyLabel, priceLabel, valueLabel, typeLabel, weekLabel);
    row.setAlignment(Pos.CENTER_LEFT);
    row.setMaxWidth(Double.MAX_VALUE);
    row.getStyleClass().addAll("holding-row", isBuy ? "tx-row-buy" : "tx-row-sell");
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
   * <p>Creates a data cell label with a fixed width matching its header column.
   * Fixed widths prevent columns from shifting when adjacent header text changes.</p>
   *
   * @param text  the cell text
   * @param width the fixed column width in pixels
   * @return the data cell label
   */
  private Label makeFixedDataCell(String text, double width) {
    Label l = new Label(text);
    l.getStyleClass().add("table-data-cell");
    l.setMinWidth(width * 0.5);
    l.setPrefWidth(width);
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
   * <p>Creates a header cell with a fixed width that never grows or shrinks.
   * Using a fixed width on both header and data cells ensures columns remain
   * perfectly aligned even when responsive text bindings change the label text.</p>
   *
   * @param text  the header text
   * @param width the fixed column width in pixels
   * @return the header label
   */
  private Label makeFixedHeaderCell(String text, double width) {
    Label l = new Label(text);
    l.getStyleClass().add("table-header-cell");
    l.setMinWidth(width * 0.5);
    l.setPrefWidth(width);
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
