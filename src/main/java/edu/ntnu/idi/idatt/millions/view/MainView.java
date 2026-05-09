package edu.ntnu.idi.idatt.millions.view;

import edu.ntnu.idi.idatt.millions.view.components.SidebarComponent;
import edu.ntnu.idi.idatt.millions.view.pages.DashboardView;
import edu.ntnu.idi.idatt.millions.view.pages.PortfolioView;
import edu.ntnu.idi.idatt.millions.view.pages.TradingView;
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

  private final DashboardView dashboardView;
  private final PortfolioView portfolioView;
  private final TradingView tradingView;

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
    sidebar.prefHeightProperty().bind(root.heightProperty());

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
    scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/main.css")).toExternalForm());
    scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/dashboard.css")).toExternalForm());
    root.prefHeightProperty().bind(scene.heightProperty());
    root.minHeightProperty().bind(scene.heightProperty());
    return scene;
  }

  /**
   * <p>Returns the dashboard view.</p>
   *
   * @return the dashboard view
   */
  public DashboardView getDashboardView() { return dashboardView; }

  /**
   * <p>Returns the portfolio view.</p>
   *
   * @return the portfolio view
   */
  public PortfolioView getPortfolioView() { return portfolioView; }

  /**
   * <p>Returns the trading view.</p>
   *
   * @return the trading view
   */
  public TradingView getTradingView() { return tradingView; }

  /**
   * <p>Returns the sidebar component.</p>
   *
   * @return the sidebar component
   */
  public SidebarComponent getSidebar() { return sidebar; }

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
