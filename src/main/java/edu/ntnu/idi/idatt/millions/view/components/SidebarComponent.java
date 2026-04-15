package edu.ntnu.idi.idatt.millions.view.components;

import edu.ntnu.idi.idatt.millions.view.Page;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.function.Consumer;

public class SidebarComponent extends VBox {

  private Button dashboardBtn;
  private Button portfolioBtn;
  private Button tradingBtn;
  private Label weekLabel;

  private Runnable onAdvanceWeek;
  private Consumer<Page> onNavigate;

  public SidebarComponent() {
    getStyleClass().add("sidebar");
    setPrefWidth(260);
    setMinWidth(260);
    setMaxWidth(260);
    setSpacing(4);
    setPadding(new Insets(24, 16, 24, 16));

  }

  private FontIcon setLogo() {
    FontIcon dashboardIcon = new FontIcon("fas-wave-squared");
    dashboardIcon.setStyle("-fx-icon-color: white;");
    dashboardIcon.setIconSize(16);
    return dashboardIcon;
  }

  private VBox buildNavSection() {
    dashboardBtn = buildNavButton("Dashboard", new FontIcon("fas-digital-tachograph"), Page.DASHBOARD);
    portfolioBtn = buildNavButton("My Portfolio", new FontIcon("fas-id-card"), Page.PORTFOLIO);
    tradingBtn = buildNavButton("Trading", new FontIcon("fas-chart-line"), Page.TRADING);

    return new VBox(4, dashboardBtn, portfolioBtn, tradingBtn);
  }

  private Button buildNavButton(String text, FontIcon icon, Page page) {
    Button btn = new Button(icon + text);
    btn.getStyleClass().add("nav-btn");
    btn.setMaxWidth(Double.MAX_VALUE);
    btn.setOnAction(e -> {
      setActivePage(page);
      if (onNavigate != null) onNavigate.accept(page);
    });
    return btn;
  }

  private VBox buildBottomSection() {
    VBox bottom = new VBox(12);
    VBox.setVgrow(bottom, Priority.ALWAYS);
    bottom.setAlignment(Pos.BOTTOM_CENTER);

    Button advanceBtn = new Button("Advance Week, ");
    advanceBtn.getStyleClass().add("advance-btn");
    advanceBtn.setMaxWidth(Double.MAX_VALUE);
    advanceBtn.setOnAction(e -> {
      if (onAdvanceWeek != null) onAdvanceWeek.run();
    });

    HBox weekBox = buildWeekBox();

    bottom.getChildren().addAll(advanceBtn, weekBox);
    return bottom;
  }

  private HBox buildWeekBox() {
    Label currentWeekLabel = new Label("Current Week");
    currentWeekLabel.getStyleClass().add("week-box-title");

    weekLabel = new Label("Week 1");
    weekLabel.getStyleClass().add("week-box-value");

    FontIcon weekIcon = new FontIcon("fas-calendar-week");
    weekIcon.getStyleClass().add("week-box-color");

    VBox textBox = new VBox(2, currentWeekLabel, weekLabel);

    HBox weekBox = new HBox(10, weekIcon, textBox);
    weekBox.setAlignment(Pos.CENTER_LEFT);
    weekBox.getStyleClass().add("week-box");
    weekBox.setPadding(new Insets(14, 16, 14, 16));
    weekBox.setMaxWidth(Double.MAX_VALUE);
    return weekBox;
  }

  public void setActivePage(Page page) {
    dashboardBtn.getStyleClass().removeAll("nav-btn-active");
    portfolioBtn.getStyleClass().removeAll("nav-btn-active");
    tradingBtn.getStyleClass().removeAll("nav-btn-active");

    switch (page) {
      case DASHBOARD -> dashboardBtn.getStyleClass().add("nav-btn-active");
      case PORTFOLIO -> portfolioBtn.getStyleClass().add("nav-btn-active");
      case TRADING   -> tradingBtn.getStyleClass().add("nav-btn-active");
    }
  }

  private Region buildSpacer(double height) {
    Region r = new Region();
    r.setPrefHeight(height);
    return r;
  }

  public void setWeek(int week) {
    weekLabel.setText("Week " + week);
  }

  public void setOnAdvanceWeek(Runnable handler) {
    this.onAdvanceWeek = handler;
  }

  public void setOnNavigate(Consumer<Page> handler) {
    this.onNavigate = handler;
  }
}
