package edu.ntnu.idi.idatt.millions.view.pages;

import edu.ntnu.idi.idatt.millions.model.player.PlayerStatus;
import edu.ntnu.idi.idatt.millions.model.player.ReadOnlyPlayer;
import edu.ntnu.idi.idatt.millions.model.portfolio.ReadOnlyPortfolio;
import edu.ntnu.idi.idatt.millions.model.transaction.Transaction;
import edu.ntnu.idi.idatt.millions.observer.PlayerObserver;
import edu.ntnu.idi.idatt.millions.observer.PortfolioObserver;
import edu.ntnu.idi.idatt.millions.view.components.portfolio.HoldingsPanel;
import edu.ntnu.idi.idatt.millions.view.components.portfolio.TransactionHistoryPanel;
import edu.ntnu.idi.idatt.millions.view.components.StockChartComponent;
import edu.ntnu.idi.idatt.millions.view.util.ViewFormatter;
import edu.ntnu.idi.idatt.millions.view.components.ViewWidgets;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * <p>
 * Portfolio page view that renders balances, holdings, and a transaction history.
 * </p>
 *
 * <p>
 * The view assembles three sub-sections: a summary row of stat cards, a
 * {@link HoldingsPanel}, and a {@link TransactionHistoryPanel}. It implements
 * {@link PlayerObserver} and {@link PortfolioObserver} so it refreshes
 * automatically whenever the player's cash or portfolio changes.
 * </p>
 */
public class PortfolioView extends VBox implements PortfolioObserver, PlayerObserver {

  private static final String ZERO_PRICE  = ViewFormatter.price(BigDecimal.ZERO);
  private static final String VALUE_STYLE = "stat-card-value";

  /** Stat-card width (px) below which monetary values render in compact form. */
  private static final double CARD_COMPACT_THRESHOLD = 170;

  private ReadOnlyPlayer player;

  private final ObjectProperty<BigDecimal> netWorthValue       = new SimpleObjectProperty<>(BigDecimal.ZERO);
  private final ObjectProperty<BigDecimal> cashBalanceValue    = new SimpleObjectProperty<>(BigDecimal.ZERO);
  private final ObjectProperty<BigDecimal> portfolioValueValue = new SimpleObjectProperty<>(BigDecimal.ZERO);

  private Label statusLabel;
  private final StockChartComponent portfolioChart;
  private final HoldingsPanel holdingsPanel;
  private final TransactionHistoryPanel transactionHistoryPanel;

  /**
   * <p>Constructs the portfolio view and builds its initial layout.</p>
   */
  public PortfolioView() {
    getStyleClass().add("dashboard-view");
    setSpacing(24);
    setPadding(new Insets(20));

    portfolioChart          = buildPortfolioChart();
    holdingsPanel           = new HoldingsPanel();
    transactionHistoryPanel = new TransactionHistoryPanel();

    getChildren().addAll(
        ViewWidgets.pageHeader("My Portfolio", "Your current holdings and balances"),
        portfolioChart,
        buildSummaryRow(),
        holdingsPanel,
        transactionHistoryPanel
    );
  }

  // Layout builders

  private StockChartComponent buildPortfolioChart() {
    StockChartComponent chart = new StockChartComponent("Portfolio value", "");
    chart.setYAxisLabel("Value ($)");
    chart.setChartHeight(240);
    chart.setPrefHeight(300);
    return chart;
  }

  /**
   * <p>Builds the summary row that shows key portfolio metrics as stat cards.
   * Every card grows equally to fill the full row width. Monetary values bind
   * responsively: compact whole-dollar format when narrow, full format when wide.</p>
   *
   * @return the summary row container
   */
  private HBox buildSummaryRow() {
    HBox row = new HBox(16);
    row.setMaxWidth(Double.MAX_VALUE);

    ViewWidgets.SummaryCard netWorth  = ViewWidgets.summaryCard("Net Worth",       ZERO_PRICE, VALUE_STYLE);
    ViewWidgets.SummaryCard cash      = ViewWidgets.summaryCard("Cash Balance",    ZERO_PRICE, VALUE_STYLE);
    ViewWidgets.SummaryCard portfolio = ViewWidgets.summaryCard("Portfolio Value", ZERO_PRICE, VALUE_STYLE);
    ViewWidgets.SummaryCard status    = ViewWidgets.summaryCard("Status",          "Novice",   "stat-card-value-status");

    bindResponsivePrice(netWorth.valueLabel(),  netWorthValue,       netWorth.card());
    bindResponsivePrice(cash.valueLabel(),      cashBalanceValue,    cash.card());
    bindResponsivePrice(portfolio.valueLabel(), portfolioValueValue, portfolio.card());
    statusLabel = status.valueLabel();

    for (VBox card : new VBox[]{netWorth.card(), cash.card(), portfolio.card(), status.card()}) {
      HBox.setHgrow(card, Priority.ALWAYS);
      card.setMaxWidth(Double.MAX_VALUE);
    }

    row.getChildren().addAll(netWorth.card(), cash.card(), portfolio.card(), status.card());
    return row;
  }

  /**
   * <p>Binds a stat-card value label's text so it shows the full price when the
   * card has room and a compact whole-dollar format when the card width drops
   * below {@link #CARD_COMPACT_THRESHOLD}.</p>
   *
   * @param label the label whose text to bind
   * @param value the monetary value property
   * @param card  the containing card whose width drives the compact toggle
   */
  private static void bindResponsivePrice(Label label, ObjectProperty<BigDecimal> value, VBox card) {
    label.textProperty().bind(Bindings.createStringBinding(() -> {
      BigDecimal v = value.get();
      if (v == null) return "";
      return card.getWidth() < CARD_COMPACT_THRESHOLD
          ? ViewFormatter.wholePrice(v)
          : ViewFormatter.price(v);
    }, value, card.widthProperty()));
  }

  // Observer callbacks

  @Override
  public void onPortfolioUpdated(ReadOnlyPortfolio portfolio) {
    if (player != null) refreshPortfolioData(player);
  }

  @Override
  public void onPlayerUpdated(ReadOnlyPlayer player) {
    this.player = player;
    refreshPortfolioData(player);
  }

  /**
   * <p>Refreshes all displayed data from the provided player snapshot:
   * stat cards, chart, holdings table, and transaction history table.</p>
   *
   * @param player a read-only view of the player whose data should be displayed
   */
  private void refreshPortfolioData(ReadOnlyPlayer player) {
    ReadOnlyPortfolio portfolio = player.getPortfolio();

    netWorthValue.set(player.getNetWorth());
    cashBalanceValue.set(player.getMoney());
    portfolioValueValue.set(portfolio.getTotalValue());

    updateStatusLabel(player.getStatus());
    portfolioChart.setData(player.getHistoricalNetWorth());

    holdingsPanel.setItems(portfolio.getHoldings());

    List<Transaction> txs = new ArrayList<>(player.getTransactions());
    Collections.reverse(txs);
    transactionHistoryPanel.setItems(txs);
  }

  /**
   * <p>Updates the Status label text and colour class to reflect the
   * current {@link PlayerStatus}.</p>
   *
   * @param status the player's current status
   */
  private void updateStatusLabel(PlayerStatus status) {
    statusLabel.setText(status.displayName());
    statusLabel.getStyleClass().removeAll(ViewFormatter.allPlayerStatusCss());
    statusLabel.getStyleClass().add(ViewFormatter.playerStatusCss(status));
  }

  // Public API

  /**
   * <p>Registers a handler invoked when the user clicks Quick Sell in the
   * holdings table. Receives the stock symbol and total quantity to sell.</p>
   *
   * @param handler the consumer receiving {@code (symbol, totalQuantity)}
   */
  public void setOnSell(BiConsumer<String, BigDecimal> handler) {
    holdingsPanel.setOnSell(handler);
  }
}
