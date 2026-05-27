package edu.ntnu.idi.idatt.millions.view;

import edu.ntnu.idi.idatt.millions.view.components.SidebarComponent;
import edu.ntnu.idi.idatt.millions.view.dialogs.EndGameOverlay;
import edu.ntnu.idi.idatt.millions.view.dialogs.EndGameOverlay.StatRow;
import edu.ntnu.idi.idatt.millions.view.pages.DashboardView;
import edu.ntnu.idi.idatt.millions.view.pages.Page;
import edu.ntnu.idi.idatt.millions.view.pages.PortfolioView;
import edu.ntnu.idi.idatt.millions.view.pages.TradingView;
import edu.ntnu.idi.idatt.millions.view.util.Stylesheets;
import edu.ntnu.idi.idatt.millions.view.util.ViewFormatter;
import java.math.BigDecimal;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Root view that wires together the sidebar and page views.
 *
 * <p>The view manages page navigation, scene creation, and exposes access to child views for
 * controllers.
 */
public class MainView {

  private final StackPane rootStack;
  private final BorderPane root;
  private final SidebarComponent sidebar;
  private final EndGameOverlay endGameOverlay;

  private final DashboardView dashboardView;
  private final PortfolioView portfolioView;
  private final TradingView tradingView;

  private final ScrollPane dashboardScroll;
  private final ScrollPane portfolioScroll;
  private final ScrollPane tradingScroll;

  private Runnable onAdvanceWeek;
  private Runnable onRetire;

  /** Constructs the main view and initializes child views. */
  public MainView() {
    rootStack = new StackPane();
    root = new BorderPane();
    sidebar = new SidebarComponent();
    dashboardView = new DashboardView();
    portfolioView = new PortfolioView();
    tradingView = new TradingView();
    endGameOverlay = new EndGameOverlay();

    rootStack.getChildren().addAll(root, endGameOverlay);

    dashboardScroll = wrapInScroll(dashboardView, true);
    portfolioScroll = wrapInScroll(portfolioView, true);
    tradingScroll = wrapInScroll(tradingView, true);
    tradingScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

    root.setLeft(sidebar);
    sidebar.prefHeightProperty().bind(root.heightProperty());

    showPage(Page.DASHBOARD);
    sidebar.setActivePage(Page.DASHBOARD);

    sidebar.setOnNavigate(this::showPage);

    sidebar.setOnAdvanceWeek(
        () -> {
          if (onAdvanceWeek != null) {
            onAdvanceWeek.run();
          }
        });

    sidebar.setOnRetire(
        () -> {
          if (onRetire != null) {
            onRetire.run();
          }
        });
  }

  /**
   * Programmatically navigates to the given page.
   *
   * @param page the page to show
   */
  public void navigateTo(Page page) {
    showPage(page);
  }

  /**
   * Displays the requested page in the center region.
   *
   * @param page the page to show
   */
  private void showPage(Page page) {
    switch (page) {
      case DASHBOARD -> root.setCenter(dashboardScroll);
      case PORTFOLIO -> root.setCenter(portfolioScroll);
      case TRADING -> root.setCenter(tradingScroll);
      default -> throw new AssertionError("Unhandled page: " + page);
    }
    sidebar.setActivePage(page);
  }

  /**
   * Wraps a page in a transparent {@link ScrollPane} so vertical (and optionally horizontal)
   * overflow becomes scrollable instead of clipped.
   *
   * @param content the page content to wrap
   * @param fitToWidth if {@code true} the content is sized to the viewport width and only vertical
   *     scrolling is enabled; if {@code false} horizontal scrolling is allowed when the content's
   *     preferred width exceeds the viewport
   * @return a configured scroll pane wrapping the content
   */
  private static ScrollPane wrapInScroll(Region content, boolean fitToWidth) {
    ScrollPane scroll = new ScrollPane(content);
    scroll.setFitToWidth(fitToWidth);
    scroll.getStyleClass().add("page-scroll");
    scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scroll.setHbarPolicy(
        fitToWidth ? ScrollPane.ScrollBarPolicy.NEVER : ScrollPane.ScrollBarPolicy.AS_NEEDED);
    return scroll;
  }

  /**
   * Creates the scene and applies stylesheets.
   *
   * @return the initialized scene
   */
  public Scene createScene() {
    Scene scene = new Scene(rootStack);
    scene.getStylesheets().add(Stylesheets.load("/styles/main.css"));
    root.prefHeightProperty().bind(scene.heightProperty());
    root.minHeightProperty().bind(scene.heightProperty());

    DoubleBinding sidebarWidth =
        Bindings.createDoubleBinding(
            () -> Math.clamp(scene.getWidth() * 0.18, 170, 260), scene.widthProperty());
    sidebar.prefWidthProperty().bind(sidebarWidth);
    sidebar.maxWidthProperty().bind(sidebarWidth);

    return scene;
  }

  /**
   * Displays the end-game overlay with the player's final stats.
   *
   * <p>Builds the stats text using {@link ViewFormatter} so all formatting stays in the view layer,
   * then delegates to the overlay for display.
   *
   * @param netWorth the player's final net worth
   * @param profit the player's total profit
   * @param returnRate the player's return rate as a decimal (e.g. 0.20 for 20%)
   * @param weeks number of weeks played
   * @param score the computed Millions Score
   * @param rank the player's leaderboard rank (1-based) if they made the top list, or {@code null}
   *     if they did not qualify
   */
  public void showEndGame(
      BigDecimal netWorth,
      BigDecimal profit,
      BigDecimal returnRate,
      int weeks,
      long score,
      Integer rank) {
    String rankText = rank != null ? "#" + rank : "Not in top 5";
    endGameOverlay.show(
        List.of(
            new StatRow("Net Worth", ViewFormatter.wholePrice(netWorth)),
            new StatRow("Profit", ViewFormatter.price(profit)),
            new StatRow("Return Rate", ViewFormatter.rateAsPercent(returnRate)),
            new StatRow("Weeks played", String.valueOf(weeks)),
            new StatRow("Millions Score", String.valueOf(score)),
            new StatRow("Your Rank", rankText)));
  }

  /**
   * Returns the dashboard view.
   *
   * @return the dashboard view
   */
  public DashboardView getDashboardView() {
    return dashboardView;
  }

  /**
   * Returns the portfolio view.
   *
   * @return the portfolio view
   */
  public PortfolioView getPortfolioView() {
    return portfolioView;
  }

  /**
   * Returns the trading view.
   *
   * @return the trading view
   */
  public TradingView getTradingView() {
    return tradingView;
  }

  /**
   * Returns the sidebar component.
   *
   * @return the sidebar component
   */
  public SidebarComponent getSidebar() {
    return sidebar;
  }

  /**
   * Registers a handler that runs when "Advance Week" is triggered.
   *
   * @param handler the action to run
   */
  public void setOnAdvanceWeek(Runnable handler) {
    this.onAdvanceWeek = handler;
  }

  /**
   * Registers a handler that runs when the user confirms retirement.
   *
   * @param handler the action to run
   */
  public void setOnRetire(Runnable handler) {
    this.onRetire = handler;
  }

  /**
   * Registers a handler that runs when the user clicks "New Game" on the end-game overlay.
   *
   * @param handler the action to run
   */
  public void setOnNewGame(Runnable handler) {
    endGameOverlay.setOnNewGame(handler);
  }

  /**
   * Registers a handler that runs when the user clicks "Exit" on the end-game overlay.
   *
   * @param handler the action to run
   */
  public void setOnExit(Runnable handler) {
    endGameOverlay.setOnExit(handler);
  }
}
