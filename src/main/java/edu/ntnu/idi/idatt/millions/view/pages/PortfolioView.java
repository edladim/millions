package edu.ntnu.idi.idatt.millions.view.pages;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

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
