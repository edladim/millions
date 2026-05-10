package edu.ntnu.idi.idatt.millions.view;

import edu.ntnu.idi.idatt.millions.view.components.SidebarComponent;
import edu.ntnu.idi.idatt.millions.view.pages.DashboardView;
import edu.ntnu.idi.idatt.millions.view.pages.PortfolioView;
import edu.ntnu.idi.idatt.millions.view.pages.TradingView;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

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

  private final ScrollPane dashboardScroll;
  private final ScrollPane portfolioScroll;
  private final ScrollPane tradingScroll;

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

    dashboardScroll = wrapInScroll(dashboardView, true);
    portfolioScroll = wrapInScroll(portfolioView, true);
    tradingScroll   = wrapInScroll(tradingView,   true);

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
      case DASHBOARD -> root.setCenter(dashboardScroll);
      case PORTFOLIO -> root.setCenter(portfolioScroll);
      case TRADING   -> root.setCenter(tradingScroll);
    }
    sidebar.setActivePage(page);
  }

  /**
   * <p>Wraps a page in a transparent {@link ScrollPane} so vertical (and
   * optionally horizontal) overflow becomes scrollable instead of clipped.</p>
   *
   * @param content     the page content to wrap
   * @param fitToWidth  if {@code true} the content is sized to the viewport
   *                    width and only vertical scrolling is enabled; if
   *                    {@code false} horizontal scrolling is allowed when the
   *                    content's preferred width exceeds the viewport
   * @return a configured scroll pane wrapping the content
   */
  private static ScrollPane wrapInScroll(Region content, boolean fitToWidth) {
    ScrollPane scroll = new ScrollPane(content);
    scroll.setFitToWidth(fitToWidth);
    scroll.getStyleClass().add("page-scroll");
    scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scroll.setHbarPolicy(fitToWidth
        ? ScrollPane.ScrollBarPolicy.NEVER
        : ScrollPane.ScrollBarPolicy.AS_NEEDED);
    return scroll;
  }

  /**
   * <p>Creates the scene and applies stylesheets.</p>
   *
   * @return the initialized scene
   */
  public Scene createScene() {
    Scene scene = new Scene(root);
    scene.getStylesheets().add(Stylesheets.load("/styles/main.css"));
    scene.getStylesheets().add(Stylesheets.load("/styles/dashboard.css"));
    root.prefHeightProperty().bind(scene.heightProperty());
    root.minHeightProperty().bind(scene.heightProperty());

    DoubleBinding sidebarWidth = Bindings.createDoubleBinding(
        () -> Math.max(170, Math.min(260, scene.getWidth() * 0.18)),
        scene.widthProperty()
    );
    sidebar.prefWidthProperty().bind(sidebarWidth);
    sidebar.maxWidthProperty().bind(sidebarWidth);

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

}
