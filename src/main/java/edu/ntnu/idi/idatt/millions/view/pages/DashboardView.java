package edu.ntnu.idi.idatt.millions.view.pages;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class DashboardView extends VBox {

  private Label portfolioValueLabel;
  private Label portfolioChangeLabel;
  private Label totalAssetsLabel;
  private Label costBasisLabel;
  private Label totalProfitLabel;
  private VBox moversContainer;

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

  public VBox buildHeader() {
    Label title = new Label("DashBoard");
    title.getStyleClass().add("page-title");

    Label subtitle = new Label("Welcome back to your investment game");
    subtitle.getStyleClass().add("page-subtitle");

    VBox header = new VBox(4, title, subtitle);
    return header;
  }

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

  private HBox buildStatsRow() {
    HBox row = new HBox(16);
    row.setMaxWidth(Double.MAX_VALUE);

    VBox totalAssets = buildStatsCard("Total Assets", "4", false);
    VBox costBasis = buildStatsCard("Cost Basis", "$0.00", false);
    VBox profitLoss = buildStatsCard("Total Profit/Loss", "$+0.00", true);

    totalAssetsLabel = (Label) totalAssets.getChildren().get(1);
    costBasisLabel = (Label) costBasis.getChildren().get(1);
    totalProfitLabel = (Label) totalAssets.getChildren().get(1);

    HBox.setHgrow(totalAssets, Priority.ALWAYS);
    HBox.setHgrow(costBasis, Priority.ALWAYS);
    HBox.setHgrow(profitLoss, Priority.ALWAYS);

    row.getChildren().addAll(totalAssets, costBasis, profitLoss);
    return row;
  }

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

  private VBox buildMoversSection() {
    Label heading = new Label("Biggest Movers This Week");
    heading.getStyleClass().add("section-heading");

    moversContainer = new VBox(8);

    VBox section = new VBox(16, heading, moversContainer);
    section.getStyleClass().add("stat-card");
    section.setPadding(new Insets(24));
    return section;
  }

  public void setPortfolioValue(String value) {
    portfolioValueLabel.setText(value);
  }

  public void setPortfolioChange(String change, boolean isPositive) {
    portfolioChangeLabel.setText("↗ " + change);
    portfolioChangeLabel.getStyleClass().removeAll("banner-change-negative");
    if (!isPositive) {
      portfolioChangeLabel.getStyleClass().add("banner-change-negative");
    }
  }

  public void setTotalAssets(String value) {
    totalAssetsLabel.setText(value);
  }

  public void setCostBasis(String value) {
    costBasisLabel.setText(value);
  }

  public void setTotalProfit(String value, boolean isPositive) {
    totalProfitLabel.setText(value);
    totalProfitLabel.getStyleClass().removeAll("stat-card-value-loss");
    if (!isPositive) {
      totalProfitLabel.getStyleClass().add("stat-card-value-loss");
    }
  }

  public void clearMovers() {
    moversContainer.getChildren().clear();
  }

}
