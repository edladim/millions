package edu.ntnu.idi.idatt.millions.view;

import edu.ntnu.idi.idatt.millions.view.pages.Page;

import edu.ntnu.idi.idatt.millions.view.components.SidebarComponent;
import edu.ntnu.idi.idatt.millions.view.dialogs.EndGameOverlay;
import edu.ntnu.idi.idatt.millions.view.dialogs.EndGameOverlay.StatRow;
import edu.ntnu.idi.idatt.millions.view.pages.DashboardView;
import edu.ntnu.idi.idatt.millions.view.pages.PortfolioView;
import edu.ntnu.idi.idatt.millions.view.pages.TradingView;
import edu.ntnu.idi.idatt.millions.view.util.Stylesheets;
import edu.ntnu.idi.idatt.millions.view.util.ViewFormatter;
import java.math.BigDecimal;
import java.util.List;
import java.util.OptionalInt;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

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

  /**
   * <p>Constructs the main view and initializes child views.</p>
   */
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
    tradingScroll   = wrapInScroll(tradingView,   true);
    tradingScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

    root.setLeft(sidebar);
    sidebar.prefHeightProperty().bind(root.heightProperty());

    showPage(Page.DASHBOARD);
    sidebar.setActivePage(Page.DASHBOARD);

    sidebar.setOnNavigate(this::showPage);

    sidebar.setOnAdvanceWeek(() -> {
      if (onAdvanceWeek != null) onAdvanceWeek.run();
    });

    sidebar.setOnRetire(() -> {
      if (onRetire != null) onRetire.run();
    });
  }

  /**
   * <p>Programmatically navigates to the given page.</p>
   *
   * @param page the page to show
   */
  public void navigateTo(Page page) {
    showPage(page);
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
    Scene scene = new Scene(rootStack);
    scene.getStylesheets().add(Stylesheets.load("/styles/main.css"));
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
   * <p>Displays the end-game overlay with the player's final stats.</p>
   *
   * <p>Builds the stats text using {@link ViewFormatter} so all formatting
   * stays in the view layer, then delegates to the overlay for display.</p>
   *
   * @param netWorth   the player's final net worth
   * @param profit     the player's total profit
   * @param returnRate the player's return rate as a decimal (e.g. 0.20 for 20%)
   * @param weeks      number of weeks played
   * @param score      the computed Millions Score
   * @param rank       the player's leaderboard rank (1-based) if they made the
   *                   top list, or empty if they did not qualify
   */
  public void showEndGame(BigDecimal netWorth,
                          BigDecimal profit,
                          BigDecimal returnRate,
                          int weeks,
                          long score,
                          OptionalInt rank) {
    String rankText = rank.isPresent() ? "#" + rank.getAsInt() : "Not in top 5";
    endGameOverlay.show(List.of(
        new StatRow("Net Worth",      ViewFormatter.wholePrice(netWorth)),
        new StatRow("Profit",         ViewFormatter.price(profit)),
        new StatRow("Return Rate",    ViewFormatter.rateAsPercent(returnRate)),
        new StatRow("Weeks played",   String.valueOf(weeks)),
        new StatRow("Millions Score", String.valueOf(score)),
        new StatRow("Your Rank",      rankText)
    ));
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
   * <p>Registers a handler that runs when the user confirms retirement.</p>
   *
   * @param handler the action to run
   */
  public void setOnRetire(Runnable handler) { this.onRetire = handler; }

  /**
   * <p>Registers a handler that runs when the user clicks "New Game" on the
   * end-game overlay.</p>
   *
   * @param handler the action to run
   */
  public void setOnNewGame(Runnable handler) { endGameOverlay.setOnNewGame(handler); }

  /**
   * <p>Registers a handler that runs when the user clicks "Exit" on the
   * end-game overlay.</p>
   *
   * @param handler the action to run
   */
  public void setOnExit(Runnable handler) { endGameOverlay.setOnExit(handler); }

}
