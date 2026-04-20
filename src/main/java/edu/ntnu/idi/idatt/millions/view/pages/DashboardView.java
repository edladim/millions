package edu.ntnu.idi.idatt.millions.view.pages;

import javafx.geometry.Insets;
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
 * The view exposes setters for updating the displayed values and a method for
 * clearing the movers list.
 * </p>
 */
public class DashboardView extends VBox {

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
  public VBox buildHeader() {
    Label title = new Label("DashBoard");
    title.getStyleClass().add("page-title");

    Label subtitle = new Label("Welcome back to your investment game");
    subtitle.getStyleClass().add("page-subtitle");

    VBox header = new VBox(4, title, subtitle);
    return header;
  }

  /**
   * <p>Builds the banner showing the portfolio value and change.</p>
   *
   * @return the banner wrapper
   */
  public StackPane buildPortfolioBanner() {
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

}
