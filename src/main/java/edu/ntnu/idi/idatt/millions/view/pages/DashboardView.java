package edu.ntnu.idi.idatt.millions.view.pages;

import edu.ntnu.idi.idatt.millions.model.ReadOnlyExchange;
import edu.ntnu.idi.idatt.millions.model.ReadOnlyPlayer;
import edu.ntnu.idi.idatt.millions.model.ReadOnlyPortfolio;
import edu.ntnu.idi.idatt.millions.model.ReadOnlyStock;
import edu.ntnu.idi.idatt.millions.observer.ExchangeObserver;
import edu.ntnu.idi.idatt.millions.observer.PlayerObserver;
import edu.ntnu.idi.idatt.millions.observer.PortfolioObserver;
import edu.ntnu.idi.idatt.millions.view.util.ViewFormatter;
import edu.ntnu.idi.idatt.millions.view.widgets.ViewWidgets;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * <p>
 * Dashboard page view that renders portfolio summary, statistics, and movers.
 * </p>
 *
 * <p>
 * The view implements {@link ExchangeObserver}, {@link PlayerObserver}, and
 * {@link PortfolioObserver} to refresh its sections whenever the underlying
 * models change. Movers support paginated "Load more" with a cap of
 * {@value #MAX_MOVERS} entries per section.
 * </p>
 */
public class DashboardView extends VBox implements PortfolioObserver, PlayerObserver, ExchangeObserver {

  private static final int MOVERS_PAGE_SIZE = 5;
  private static final int MAX_MOVERS       = 20;

  private ReadOnlyPlayer player;
  private ReadOnlyExchange currentExchange;

  private Label portfolioValueLabel;
  private Label portfolioChangeLabel;
  private Label totalAssetsLabel;
  private Label costBasisLabel;
  private Label totalProfitLabel;
  private VBox moversPositiveContainer;
  private VBox moversNegativeContainer;
  private Label loadMoreGainersLabel;
  private Label loadMoreLosersLabel;
  private int gainersDisplayCount = MOVERS_PAGE_SIZE;
  private int losersDisplayCount  = MOVERS_PAGE_SIZE;
  private Consumer<String> onStockClicked;

  /**
   * <p>Constructs the dashboard view and builds its initial layout.</p>
   */
  public DashboardView() {
    getStyleClass().add("dashboard-view");
    setSpacing(24);
    setPadding(new Insets(40));

    getChildren().addAll(
            ViewWidgets.pageHeader("Dashboard", "Welcome back to your investment game"),
            buildPortfolioBanner(),
            buildStatsRow(),
            buildMoversSection()
    );
  }

  /**
   * <p>Builds the banner showing the portfolio value and change.</p>
   *
   * @return the banner wrapper
   */
  private StackPane buildPortfolioBanner() {
    VBox content = new VBox(8);
    content.setPadding(new Insets(32));
    content.getStyleClass().add("portfolio-banner");

    Label heading = new Label("My Portfolio Value");
    heading.getStyleClass().add("banner-heading");

    portfolioValueLabel = new Label("$0.00");
    portfolioValueLabel.getStyleClass().add("banner-value");

    portfolioChangeLabel = new Label("↗ +0.00% (+$0.00)");
    portfolioChangeLabel.getStyleClass().add("banner-change");

    content.getChildren().addAll(heading, portfolioValueLabel, portfolioChangeLabel);

    StackPane wrapper = new StackPane(content);
    wrapper.setMaxWidth(Double.MAX_VALUE);
    return wrapper;
  }

  /**
   * <p>Builds the row of statistics cards.</p>
   *
   * @return the stats row container
   */
  private HBox buildStatsRow() {
    HBox row = new HBox(16);
    row.setMaxWidth(Double.MAX_VALUE);

    ViewWidgets.SummaryCard totalAssets = buildStatsCard("Total Assets",      "0");
    ViewWidgets.SummaryCard costBasis   = buildStatsCard("Cost Basis",        "$0.00");
    ViewWidgets.SummaryCard profitLoss  = buildStatsCard("Total Profit/Loss", "+$0.00");

    totalAssetsLabel = totalAssets.valueLabel();
    costBasisLabel   = costBasis.valueLabel();
    totalProfitLabel = profitLoss.valueLabel();

    HBox.setHgrow(totalAssets.card(), Priority.ALWAYS);
    HBox.setHgrow(costBasis.card(),   Priority.ALWAYS);
    HBox.setHgrow(profitLoss.card(),  Priority.ALWAYS);

    row.getChildren().addAll(totalAssets.card(), costBasis.card(), profitLoss.card());
    return row;
  }

  /**
   * <p>Builds a statistics card via {@link ViewWidgets#summaryCard} and bumps
   * its padding up to {@code 24} to match the dashboard's larger card style.</p>
   *
   * @param title        the card title
   * @param initialValue the initial text shown in the value label
   * @return both the card container and its value label
   */
  private static ViewWidgets.SummaryCard buildStatsCard(String title, String initialValue) {
    ViewWidgets.SummaryCard card = ViewWidgets.summaryCard(title, initialValue, "stat-card-value");
    card.card().setPadding(new Insets(24));
    return card;
  }

  /**
   * <p>Builds the movers section container.</p>
   *
   * @return the movers section container
   */
  private VBox buildMoversSection() {
    moversPositiveContainer = new VBox(8);
    moversNegativeContainer = new VBox(8);

    loadMoreGainersLabel = buildLoadMoreLabel(() -> {
      gainersDisplayCount = Math.min(gainersDisplayCount + MOVERS_PAGE_SIZE, MAX_MOVERS);
      refreshMovers();
    });

    loadMoreLosersLabel = buildLoadMoreLabel(() -> {
      losersDisplayCount = Math.min(losersDisplayCount + MOVERS_PAGE_SIZE, MAX_MOVERS);
      refreshMovers();
    });

    VBox positiveSection = ViewWidgets.sectionCard(
        ViewWidgets.sectionSubheading("Top Performers"),
        moversPositiveContainer, loadMoreGainersLabel);
    positiveSection.setSpacing(8);
    positiveSection.setMaxWidth(Double.MAX_VALUE);

    VBox negativeSection = ViewWidgets.sectionCard(
        ViewWidgets.sectionSubheading("Worst Performers"),
        moversNegativeContainer, loadMoreLosersLabel);
    negativeSection.setSpacing(8);
    negativeSection.setMaxWidth(Double.MAX_VALUE);

    HBox section = new HBox(12, positiveSection, negativeSection);
    section.setMaxWidth(Double.MAX_VALUE);
    section.setFillHeight(false);
    HBox.setHgrow(positiveSection, Priority.ALWAYS);
    HBox.setHgrow(negativeSection, Priority.ALWAYS);

    VBox fullContainer = new VBox(12, ViewWidgets.sectionHeading("Biggest Movers This Week"), section);
    fullContainer.setMaxWidth(Double.MAX_VALUE);
    return fullContainer;
  }

  /**
   * <p>Creates a "Load more" label with a click handler.</p>
   *
   * @param onLoad the action to run when clicked
   * @return the configured label
   */
  private Label buildLoadMoreLabel(Runnable onLoad) {
    Label label = new Label("Load more");
    label.getStyleClass().add("load-more-label");
    label.setMaxWidth(Double.MAX_VALUE);
    label.setAlignment(Pos.CENTER);
    label.setVisible(false);
    label.setManaged(false);
    label.setOnMouseClicked(e -> onLoad.run());
    return label;
  }

  /**
   * <p>Shows or hides a label without consuming layout space when hidden.</p>
   *
   * @param label   the label to toggle
   * @param visible {@code true} to show
   */
  private void setLoadMoreVisible(Label label, boolean visible) {
    label.setVisible(visible);
    label.setManaged(visible);
  }

  /**
   * <p>Updates the displayed portfolio change value and styling.</p>
   *
   * @param change     the formatted change text
   * @param isPositive whether the change is positive
   */
  private void setPortfolioChange(String change, boolean isPositive) {
    portfolioChangeLabel.setText((isPositive ? "↗ " : "↘ ") + change);
    portfolioChangeLabel.getStyleClass().removeAll("banner-change-negative");
    if (!isPositive) {
      portfolioChangeLabel.getStyleClass().add("banner-change-negative");
    }
  }

  /**
   * <p>Updates the displayed total profit value and styling.</p>
   *
   * @param value      the formatted profit/loss value
   * @param isPositive whether the value is positive
   */
  private void setTotalProfit(String value, boolean isPositive) {
    totalProfitLabel.setText(value);
    totalProfitLabel.getStyleClass().removeAll("stat-card-value-profit", "stat-card-value-loss");
    totalProfitLabel.getStyleClass().add(isPositive ? "stat-card-value-profit" : "stat-card-value-loss");
  }

  @Override
  public void onPlayerUpdated(ReadOnlyPlayer player) {
    this.player = player;
    refreshStats();
  }

  @Override
  public void onPortfolioUpdated(ReadOnlyPortfolio portfolio) {
    if (player != null) {
      refreshStats();
    }
  }

  @Override
  public void onExchangeUpdated(ReadOnlyExchange exchange) {
    this.currentExchange = exchange;
    gainersDisplayCount  = MOVERS_PAGE_SIZE;
    losersDisplayCount   = MOVERS_PAGE_SIZE;
    refreshMovers();
  }

  /**
   * <p>Renders the movers lists up to the current display counts and toggles
   * the "Load more" labels accordingly. Gainers and losers each start their
   * rank numbering at #1.</p>
   */
  private void refreshMovers() {
    moversPositiveContainer.getChildren().clear();
    moversNegativeContainer.getChildren().clear();

    List<? extends ReadOnlyStock> topPerformers = currentExchange.getTopPerformers(gainersDisplayCount + 1);
    int toShowG = Math.min(gainersDisplayCount, topPerformers.size());
    for (int i = 0; i < toShowG; i++) {
      moversPositiveContainer.getChildren().add(buildMoverRow(i + 1, topPerformers.get(i)));
    }
    setLoadMoreVisible(loadMoreGainersLabel,
        topPerformers.size() > gainersDisplayCount && gainersDisplayCount < MAX_MOVERS);

    List<? extends ReadOnlyStock> bottomPerformers = currentExchange.getBottomPerformers(losersDisplayCount + 1);
    int toShowL = Math.min(losersDisplayCount, bottomPerformers.size());
    for (int i = 0; i < toShowL; i++) {
      moversNegativeContainer.getChildren().add(buildMoverRow(i + 1, bottomPerformers.get(i)));
    }
    setLoadMoreVisible(loadMoreLosersLabel,
        bottomPerformers.size() > losersDisplayCount && losersDisplayCount < MAX_MOVERS);
  }

  /**
   * <p>Recalculates and pushes all player and portfolio statistics to the
   * banner and stats cards. Called whenever the player or portfolio changes.</p>
   */
  private void refreshStats() {
    ReadOnlyPortfolio portfolio = player.getPortfolio();
    BigDecimal profit = player.getProfit();
    boolean isPositive = profit.compareTo(BigDecimal.ZERO) >= 0;

    long distinctStocks = portfolio.getDistinctStockCount();

    portfolioValueLabel.setText(ViewFormatter.price(player.getNetWorth()));
    totalAssetsLabel.setText(String.valueOf(distinctStocks));
    costBasisLabel.setText(ViewFormatter.price(portfolio.getTotalInvestment()));
    setTotalProfit(ViewFormatter.signedPrice(profit), isPositive);

    String changeStr = ViewFormatter.rateAsPercent(player.getReturnRate())
        + "  (" + ViewFormatter.signedPrice(profit) + ")";
    setPortfolioChange(changeStr, isPositive);
  }

  /**
   * <p>Builds a single row in a movers list showing rank, icon, company name,
   * current price, and percentage change.</p>
   *
   * @param rank  the display rank number (1-based)
   * @param stock the stock to display
   * @return the configured row container
   */
  private HBox buildMoverRow(int rank, ReadOnlyStock stock) {
    Label rankLabel = new Label("#" + rank);
    rankLabel.getStyleClass().add("mover-rank");
    rankLabel.setPrefWidth(32);

    HBox iconBlock = ViewWidgets.stockIconBlockCompanyFirst(stock.getSymbol(), stock.getCompany());
    HBox.setHgrow(iconBlock, Priority.ALWAYS);

    BigDecimal change = stock.getLatestPriceChange();
    boolean isPositive = change.compareTo(BigDecimal.ZERO) >= 0;
    Label priceLabel = new Label(ViewFormatter.price(stock.getSalesPrice()));
    priceLabel.getStyleClass().add("mover-price");
    Label changeLabel = new Label(ViewFormatter.changeArrowPercent(change, stock.getSalesPrice()));
    changeLabel.getStyleClass().add(isPositive ? "mover-change-positive" : "mover-change-negative");
    VBox priceBox = new VBox(2, priceLabel, changeLabel);
    priceBox.setAlignment(Pos.CENTER_RIGHT);

    HBox row = new HBox(12, rankLabel, iconBlock, priceBox);
    row.setPrefWidth(0);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("mover-row");
    row.setPadding(new Insets(8, 8, 8, 0));
    row.setOnMouseClicked(e -> {
      if (onStockClicked != null) onStockClicked.accept(stock.getSymbol());
    });
    return row;
  }

  /**
   * <p>Registers a handler invoked when the user clicks a mover row.</p>
   *
   * @param handler callback receiving the clicked stock's symbol
   */
  public void setOnStockClicked(Consumer<String> handler) {
    this.onStockClicked = handler;
  }

}
