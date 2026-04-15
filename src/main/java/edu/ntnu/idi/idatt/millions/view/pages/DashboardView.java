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


}
