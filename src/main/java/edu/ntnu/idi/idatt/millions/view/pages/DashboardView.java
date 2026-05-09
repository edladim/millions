package edu.ntnu.idi.idatt.millions.view.pages;

import edu.ntnu.idi.idatt.millions.model.ReadOnlyExchange;
import edu.ntnu.idi.idatt.millions.model.ReadOnlyPlayer;
import edu.ntnu.idi.idatt.millions.model.ReadOnlyPortfolio;
import edu.ntnu.idi.idatt.millions.model.ReadOnlyStock;
import edu.ntnu.idi.idatt.millions.observer.ExchangeObserver;
import edu.ntnu.idi.idatt.millions.observer.PlayerObserver;
import edu.ntnu.idi.idatt.millions.observer.PortfolioObserver;
import edu.ntnu.idi.idatt.millions.view.ViewFormatter;
import java.math.BigDecimal;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * <p>
 * Dashboard page view that renders portfolio summary, statistics, and movers.
 * </p>
 *
 * <p>
 * The view exposes setters for updating the displayed values and a method for
 * clearing the movers list.
 * </p>
 */
public class DashboardView extends VBox implements PortfolioObserver, PlayerObserver, ExchangeObserver {

  private ReadOnlyPlayer player;

  private Label portfolioValueLabel;
  private Label portfolioChangeLabel;
  private Label totalAssetsLabel;
  private Label costBasisLabel;
  private Label totalProfitLabel;
  private VBox moversContainer;

  /**
   * <p>Constructs the dashboard view and builds its initial layout.</p>
   */
  public DashboardView() {
    getStyleClass().add("dashboard-view");
    setSpacing(24);
    setPadding(new Insets(40));

    getChildren().addAll(
            buildHeader(),
            buildPortfolioBanner(),
            buildStatsRow(),
            buildMoversSection()
    );
  }

  /**
   * <p>Builds the header section containing title and subtitle.</p>
   *
   * @return the header container
   */
  private VBox buildHeader() {
    Label title = new Label("DashBoard");
    title.getStyleClass().add("page-title");

    Label subtitle = new Label("Welcome back to your investment game");
    subtitle.getStyleClass().add("page-subtitle");

    return new VBox(4, title, subtitle);
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

    portfolioChangeLabel = new Label("↗ +0.00% ($+0.00)");
    portfolioChangeLabel.getStyleClass().add("banner-change");

    content.getChildren().addAll(heading, portfolioValueLabel, portfolioChangeLabel);

    StackPane wrapper = new StackPane(content);
    wrapper.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(wrapper, Priority.ALWAYS);
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

    VBox totalAssets = buildStatsCard("Total Assets", "4", false);
    VBox costBasis = buildStatsCard("Cost Basis", "$0.00", false);
    VBox profitLoss = buildStatsCard("Total Profit/Loss", "$+0.00", true);

    totalAssetsLabel = (Label) totalAssets.getChildren().get(1);
    costBasisLabel = (Label) costBasis.getChildren().get(1);
    totalProfitLabel = (Label) profitLoss.getChildren().get(1);

    HBox.setHgrow(totalAssets, Priority.ALWAYS);
    HBox.setHgrow(costBasis, Priority.ALWAYS);
    HBox.setHgrow(profitLoss, Priority.ALWAYS);

    row.getChildren().addAll(totalAssets, costBasis, profitLoss);
    return row;
  }

  /**
   * <p>Builds a single statistics card.</p>
   *
   * @param title   the card title
   * @param value   the card value text
   * @param isProfit whether the value represents profit styling
   * @return the stats card container
   */
  private VBox buildStatsCard(String title, String value, boolean isProfit) {
    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().add("stats-card-title");

    Label valueLabel = new Label(value);
    valueLabel.getStyleClass().add(isProfit ? "stats-card-value-profit" : "stats-card-value");

    VBox card = new VBox(12, titleLabel, valueLabel);
    card.getStyleClass().add("stats-card");
    card.setPadding(new Insets(24));
    card.setMaxWidth(Double.MAX_VALUE);
    return card;
  }

  /**
   * <p>Builds the movers section container.</p>
   *
   * @return the movers section container
   */
  private VBox buildMoversSection() {
    Label heading = new Label("Biggest Movers This Week");
    heading.getStyleClass().add("section-heading");

    moversContainer = new VBox(8);

    VBox section = new VBox(16, heading, moversContainer);
    section.getStyleClass().add("stat-card");
    section.setPadding(new Insets(24));
    return section;
  }

  /**
   * <p>Updates the displayed portfolio value.</p>
   *
   * @param value the formatted portfolio value
   */
  public void setPortfolioValue(String value) {
    portfolioValueLabel.setText(value);
  }

  /**
   * <p>Updates the displayed portfolio change value and styling.</p>
   *
   * @param change     the formatted change text
   * @param isPositive whether the change is positive
   */
  public void setPortfolioChange(String change, boolean isPositive) {
    portfolioChangeLabel.setText("↗ " + change);
    portfolioChangeLabel.getStyleClass().removeAll("banner-change-negative");
    if (!isPositive) {
      portfolioChangeLabel.getStyleClass().add("banner-change-negative");
    }
  }

  /**
   * <p>Updates the displayed total assets value.</p>
   *
   * @param value the formatted assets value
   */
  public void setTotalAssets(String value) {
    totalAssetsLabel.setText(value);
  }

  /**
   * <p>Updates the displayed cost basis value.</p>
   *
   * @param value the formatted cost basis value
   */
  public void setCostBasis(String value) {
    costBasisLabel.setText(value);
  }

  /**
   * <p>Updates the displayed total profit value and styling.</p>
   *
   * @param value      the formatted profit/loss value
   * @param isPositive whether the value is positive
   */
  public void setTotalProfit(String value, boolean isPositive) {
    totalProfitLabel.setText(value);
    totalProfitLabel.getStyleClass().removeAll("stat-card-value-loss");
    if (!isPositive) {
      totalProfitLabel.getStyleClass().add("stat-card-value-loss");
    }
  }

  /**
   * <p>Clears all items from the movers list.</p>
   */
  public void clearMovers() {
    moversContainer.getChildren().clear();
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
    clearMovers();
    int rank = 1;
    for (ReadOnlyStock stock : exchange.getGainers(5)) {
      moversContainer.getChildren().add(buildMoverRow(rank++, stock));
    }
  }

  private void refreshStats() {
    ReadOnlyPortfolio portfolio = player.getPortfolio();
    BigDecimal profit = player.getProfit();
    boolean isPositive = profit.compareTo(BigDecimal.ZERO) >= 0;

    setPortfolioValue(ViewFormatter.price(portfolio.getTotalValue()));
    setTotalAssets(String.valueOf(portfolio.size()));
    setCostBasis(ViewFormatter.price(portfolio.getTotalInvestment()));
    setTotalProfit(ViewFormatter.signedPrice(profit), isPositive);

    String changeStr = ViewFormatter.rateAsPercent(player.getReturnRate())
        + "  (" + ViewFormatter.signedPrice(profit) + ")";
    setPortfolioChange(changeStr, isPositive);
  }

  private HBox buildMoverRow(int rank, ReadOnlyStock stock) {
    Label rankLabel = new Label("#" + rank);
    rankLabel.getStyleClass().add("mover-rank");
    rankLabel.setPrefWidth(32);

    Circle icon = new Circle(18, Color.web("#6366f1"));
    Label letter = new Label(String.valueOf(stock.getSymbol().charAt(0)));
    letter.getStyleClass().add("mover-icon-letter");
    StackPane iconPane = new StackPane(icon, letter);

    Label name = new Label(stock.getCompany());
    name.getStyleClass().add("mover-name");
    Label symbol = new Label(stock.getSymbol());
    symbol.getStyleClass().add("mover-symbol");
    VBox nameBox = new VBox(2, name, symbol);

    BigDecimal change = stock.getLatestPriceChange();
    boolean isPositive = change.compareTo(BigDecimal.ZERO) >= 0;
    Label priceLabel = new Label(ViewFormatter.price(stock.getSalesPrice()));
    priceLabel.getStyleClass().add("mover-price");
    Label changeLabel = new Label(ViewFormatter.changeArrow(change));
    changeLabel.getStyleClass().add(isPositive ? "mover-change-positive" : "mover-change-negative");
    VBox priceBox = new VBox(2, priceLabel, changeLabel);
    priceBox.setAlignment(Pos.CENTER_RIGHT);

    HBox row = new HBox(12, rankLabel, iconPane, nameBox);
    HBox.setHgrow(nameBox, Priority.ALWAYS);
    row.getChildren().add(priceBox);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("mover-row");
    row.setPadding(new Insets(8, 0, 8, 0));
    return row;
  }

}
