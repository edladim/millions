package edu.ntnu.idi.idatt.millions.view;

import edu.ntnu.idi.idatt.millions.view.components.SidebarComponent;
import edu.ntnu.idi.idatt.millions.view.pages.DashboardView;
import edu.ntnu.idi.idatt.millions.view.pages.PortfolioView;
import edu.ntnu.idi.idatt.millions.view.pages.TradingView;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

public class MainView {

  private final BorderPane root;
  private final SidebarComponent sidebar;

  private final Node dashboardView;
  private final Node portfolioView;
  private final Node tradingView;

  private Runnable onAdvanceWeek;

  public MainView() {
    root = new BorderPane();
    sidebar = new SidebarComponent();
    dashboardView = new DashboardView();
    portfolioView = new PortfolioView();
    tradingView = new TradingView();

    root.setLeft(sidebar);
    root.getStylesheets().add("main-root");

    showPage(Page.DASHBOARD);
    sidebar.setActivePage(Page.DASHBOARD);

    sidebar.setOnNavigate(this::showPage);

    sidebar.setOnAdvanceWeek(() -> {
      if (onAdvanceWeek != null) onAdvanceWeek.run();
    });
  }

  private void showPage(Page page) {
    switch (page) {
      case DASHBOARD -> root.setCenter(dashboardView);
      case PORTFOLIO -> root.setCenter(portfolioView);
      case TRADING -> root.setCenter(tradingView);
    }
    sidebar.setActivePage(page);
  }

  public Scene createScene() {
    Scene scene = new Scene(root, 1280, 800);
    scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
    scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
    return scene;
  }

  public Node getDashboardView() { return dashboardView; }
  public Node getPortfolioView() { return portfolioView; }
  public Node getTradingView() { return tradingView; }
  public Node getSidebar() { return sidebar; }

  public void setOnAdvanceWeek(Runnable handler) { this.onAdvanceWeek = handler; }

  public void setWeek(int week) {
    sidebar.setWeek(week);
  }
}
