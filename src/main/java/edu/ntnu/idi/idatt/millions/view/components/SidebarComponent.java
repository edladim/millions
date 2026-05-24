package edu.ntnu.idi.idatt.millions.view.components;

import edu.ntnu.idi.idatt.millions.model.ReadOnlyExchange;
import edu.ntnu.idi.idatt.millions.observer.ExchangeObserver;
import edu.ntnu.idi.idatt.millions.view.Page;
import edu.ntnu.idi.idatt.millions.view.util.Stylesheets;
import edu.ntnu.idi.idatt.millions.view.util.ViewFormatter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Optional;
import java.util.function.Consumer;
import javafx.beans.binding.Bindings;

/**
 * <p>
 * Sidebar UI component that contains navigation buttons, a week indicator,
 * and an action to advance the week.
 * </p>
 *
 * <p>
 * The component exposes callbacks for navigation and week-advance actions and
 * provides a method for updating the visible week label.
 * </p>
 */
public class SidebarComponent extends VBox implements ExchangeObserver {

  private Button dashboardBtn;
  private Button portfolioBtn;
  private Button tradingBtn;
  private Label weekLabel;

  private Runnable onAdvanceWeek;
  private Runnable onRetire;
  private Consumer<Page> onNavigate;

  /**
   * <p>Constructs the sidebar and builds its initial layout.</p>
   */
  public SidebarComponent() {
    getStyleClass().add("sidebar");
    setMinWidth(170);
    setSpacing(4);
    setPadding(new Insets(24, 16, 24, 16));

    getChildren().addAll(
            setLogo(),
            buildSpacer(24),
            buildNavSection(),
            buildBottomSection()
    );
  }

  /**
   * <p>Builds the logo section shown at the top of the sidebar.</p>
   *
   * @return the logo container
   */
  private HBox setLogo() {
    FontIcon dashboardIcon = new FontIcon("fas-wave-square");
    dashboardIcon.getStyleClass().add("logo-icon-label");
    dashboardIcon.setIconSize(16);

    Label name = new Label("Millions");
    name.getStyleClass().add("logo-text");

    HBox logo = new HBox(10, dashboardIcon, name);
    logo.setAlignment(Pos.CENTER_LEFT);
    logo.setPadding(new Insets(0, 0, 16, 4));

    return logo;
  }

  /**
   * <p>Builds the navigation section containing page buttons.</p>
   *
   * @return the navigation container
   */
  private VBox buildNavSection() {
    dashboardBtn = buildNavButton("Dashboard", new FontIcon("fas-digital-tachograph"), Page.DASHBOARD);
    portfolioBtn = buildNavButton("My Portfolio", new FontIcon("fas-id-card"), Page.PORTFOLIO);
    tradingBtn = buildNavButton("Trading", new FontIcon("fas-chart-line"), Page.TRADING);

    return new VBox(4, dashboardBtn, portfolioBtn, tradingBtn);
  }

  /**
   * <p>Creates a navigation button with icon and click handler.</p>
   *
   * @param text the button label
   * @param icon the icon to display
   * @param page the page associated with the button
   * @return a configured navigation button
   */
  private Button buildNavButton(String text, FontIcon icon, Page page) {
    Button btn = new Button(text);
    btn.setGraphic(icon);
    btn.getStyleClass().add("nav-btn");
    btn.setMaxWidth(Double.MAX_VALUE);
    btn.setOnAction(e -> {
      setActivePage(page);
      if (onNavigate != null) onNavigate.accept(page);
    });
    return btn;
  }

  /**
   * <p>Builds the bottom section containing the advance button and week box.</p>
   *
   * @return the bottom section container
   */
  private VBox buildBottomSection() {
    VBox bottom = new VBox(12);
    VBox.setVgrow(bottom, Priority.ALWAYS);
    bottom.setAlignment(Pos.BOTTOM_CENTER);

    Button advanceBtn = new Button();
    advanceBtn.textProperty().bind(
        Bindings.when(widthProperty().lessThan(190))
            .then("Advance")
            .otherwise("Advance Week")
    );
    advanceBtn.getStyleClass().add("advance-btn");
    advanceBtn.setMaxWidth(Double.MAX_VALUE);
    advanceBtn.setOnAction(e -> {
      if (onAdvanceWeek != null) onAdvanceWeek.run();
    });

    Button retireBtn = new Button("Retire");
    retireBtn.getStyleClass().add("sell-all-btn");
    retireBtn.setMaxWidth(Double.MAX_VALUE);
    retireBtn.setOnAction(e -> confirmAndRetire());

    HBox weekBox = buildWeekBox();

    bottom.getChildren().addAll(advanceBtn, retireBtn, weekBox);
    return bottom;
  }

  /**
   * <p>Shows a styled confirmation dialog before triggering retirement.</p>
   *
   * <p>If the user confirms, the registered retire handler is invoked, which
   * causes the controller to liquidate the portfolio and end the game.</p>
   */
  private void confirmAndRetire() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.initOwner(getScene().getWindow());
    alert.setTitle("Confirm retirement");
    alert.setHeaderText("Are you sure you want to retire?");
    alert.setContentText("This will sell all your shares and end the game.");

    alert.getDialogPane().getStylesheets()
        .add(Stylesheets.load("/styles/main.css"));
    alert.getDialogPane().getStyleClass().add("retire-alert");

    ButtonType yes = new ButtonType("Retire", ButtonBar.ButtonData.OK_DONE);
    ButtonType no = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    alert.getButtonTypes().setAll(yes, no);

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get().equals(yes) && onRetire != null) {
      onRetire.run();
    }
  }

  /**
   * <p>Builds the week indicator box.</p>
   *
   * @return the week box container
   */
  private HBox buildWeekBox() {
    Label currentWeekLabel = new Label("Current Week");
    currentWeekLabel.getStyleClass().add("week-box-title");

    weekLabel = new Label(ViewFormatter.week(1));
    weekLabel.getStyleClass().add("week-box-value");

    FontIcon weekIcon = new FontIcon("fas-calendar-week");
    weekIcon.getStyleClass().add("week-box-icon");

    VBox textBox = new VBox(2, currentWeekLabel, weekLabel);

    HBox weekBox = new HBox(10, weekIcon, textBox);
    weekBox.setAlignment(Pos.CENTER_LEFT);
    weekBox.getStyleClass().add("week-box");
    weekBox.setPadding(new Insets(14, 16, 14, 16));
    weekBox.setMaxWidth(Double.MAX_VALUE);
    return weekBox;
  }

  /**
   * <p>Marks the active page by updating the button style classes.</p>
   *
   * @param page the page that should be highlighted as active
   */
  public void setActivePage(Page page) {
    dashboardBtn.getStyleClass().removeAll("nav-btn-active");
    portfolioBtn.getStyleClass().removeAll("nav-btn-active");
    tradingBtn.getStyleClass().removeAll("nav-btn-active");

    switch (page) {
      case DASHBOARD -> dashboardBtn.getStyleClass().add("nav-btn-active");
      case PORTFOLIO -> portfolioBtn.getStyleClass().add("nav-btn-active");
      case TRADING -> tradingBtn.getStyleClass().add("nav-btn-active");
    }
  }

  /**
   * <p>Creates a spacer region with a fixed height.</p>
   *
   * @param height the spacer height in pixels
   * @return the spacer region
   */
  private Region buildSpacer(double height) {
    Region r = new Region();
    r.setPrefHeight(height);
    return r;
  }

  /**
   * <p>Updates the displayed week number in the sidebar.</p>
   *
   * @param week the current week number to display
   */
  private void setWeek(int week) {
    weekLabel.setText(ViewFormatter.week(week));
  }

  /**
   * <p>Registers a handler that runs when the "Advance Week" button is pressed.</p>
   *
   * @param handler the action to run on button press
   */
  public void setOnAdvanceWeek(Runnable handler) {
    this.onAdvanceWeek = handler;
  }

  /**
   * <p>Registers a handler that runs when a navigation button is selected.</p>
   *
   * @param handler the consumer that receives the selected page
   */
  public void setOnNavigate(Consumer<Page> handler) {
    this.onNavigate = handler;
  }

  /**
   * <p>Registers a handler that runs after the user confirms retirement.</p>
   *
   * @param handler the action to run when retirement is confirmed
   */
  public void setOnRetire(Runnable handler) {
    this.onRetire = handler;
  }

  @Override
  public void onExchangeUpdated(ReadOnlyExchange exchange) {
    setWeek(exchange.getWeek());
  }
}
