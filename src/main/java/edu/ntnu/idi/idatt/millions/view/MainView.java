package edu.ntnu.idi.idatt.millions.view;

import edu.ntnu.idi.idatt.millions.view.components.SidebarComponent;
import edu.ntnu.idi.idatt.millions.view.pages.DashboardView;
import edu.ntnu.idi.idatt.millions.view.pages.PortfolioView;
import edu.ntnu.idi.idatt.millions.view.pages.TradingView;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

/**
 * <p>
 * Root view that wires together the sidebar and page views.
 * </p>
 *
 * <p>
 * The view manages page navigation, scene creation, and exposes access to
 * child views for controllers.
 * </p>
 */
public class MainView {

  private final BorderPane root;
  private final SidebarComponent sidebar;

  private final Node dashboardView;
  private final Node portfolioView;
  private final Node tradingView;

  private Runnable onAdvanceWeek;

  /**
   * <p>Constructs the main view and initializes child views.</p>
   */
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

  /**
   * <p>Displays the requested page in the center region.</p>
   *
   * @param page the page to show
   */
  private void showPage(Page page) {
    switch (page) {
      case DASHBOARD -> root.setCenter(dashboardView);
      case PORTFOLIO -> root.setCenter(portfolioView);
      case TRADING -> root.setCenter(tradingView);
    }
    sidebar.setActivePage(page);
  }

  /**
   * <p>Creates the scene and applies stylesheets.</p>
   *
   * @return the initialized scene
   */
  public Scene createScene() {
    Scene scene = new Scene(root);
    scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
    scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
    return scene;
  }

  /**
   * <p>Returns the dashboard view node.</p>
   *
   * @return the dashboard view
   */
  public Node getDashboardView() { return dashboardView; }

  /**
   * <p>Returns the portfolio view node.</p>
   *
   * @return the portfolio view
   */
  public Node getPortfolioView() { return portfolioView; }

  /**
   * <p>Returns the trading view node.</p>
   *
   * @return the trading view
   */
  public Node getTradingView() { return tradingView; }

  /**
   * <p>Returns the sidebar component node.</p>
   *
   * @return the sidebar component
   */
  public Node getSidebar() { return sidebar; }

  /**
   * <p>Registers a handler that runs when "Advance Week" is triggered.</p>
   *
   * @param handler the action to run
   */
  public void setOnAdvanceWeek(Runnable handler) { this.onAdvanceWeek = handler; }

  /**
   * <p>Updates the displayed week in the sidebar.</p>
   *
   * @param week the current week number
   */
  public void setWeek(int week) {
    sidebar.setWeek(week);
  }
}
