package edu.ntnu.idi.idatt.millions.controller;

import edu.ntnu.idi.idatt.millions.filehandler.LeaderboardStore;
import edu.ntnu.idi.idatt.millions.filehandler.StockFileException;
import edu.ntnu.idi.idatt.millions.filehandler.StockLoader;
import edu.ntnu.idi.idatt.millions.model.Exchange;
import edu.ntnu.idi.idatt.millions.model.Player;
import edu.ntnu.idi.idatt.millions.model.Stock;
import edu.ntnu.idi.idatt.millions.view.util.FontLoader;
import edu.ntnu.idi.idatt.millions.view.MainView;
import edu.ntnu.idi.idatt.millions.view.StartupScreen;
import edu.ntnu.idi.idatt.millions.view.util.Stylesheets;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>Controller responsible for the startup flow and initial game setup.</p>
 * <p>Shows the startup screen, validates inputs, and launches the main game
 * view. Stock loading and font loading are delegated to
 * {@link StockLoader} and {@link FontLoader} respectively.</p>
 */
public class SetupController {

  private final Stage primaryStage;
  private final StartupScreen screen;
  private final Scene startupScene;
  private File selectedFile = null;

  /**
   * <p>Creates a setup controller bound to the primary stage and caches the
   * startup scene so it can be reused when the player returns from a game.</p>
   *
   * @param primaryStage application window to control.
   */
  public SetupController(Stage primaryStage) {
    this.primaryStage = primaryStage;
    this.screen = new StartupScreen();

    screen.setOnBrowse(this::handleBrowse);
    screen.setOnStart(this::handleStart);

    ScrollPane scroll = new ScrollPane(screen);
    scroll.setFitToWidth(true);
    scroll.setFitToHeight(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scroll.getStyleClass().add("page-scroll");

    this.startupScene = new Scene(scroll, 600, 760);
    this.startupScene.getStylesheets().add(Stylesheets.load("/styles/main.css"));
  }

  /**
   * <p>Displays the startup screen and applies base styles.</p>
   *
   * <p>Refreshes the leaderboard each time the screen is shown so scores added
   * since the previous game appear immediately when the player returns from
   * the end-game overlay.</p>
   */
  public void show() {
    FontLoader.loadInter();
    screen.setLeaderboard(LeaderboardStore.loadTop());
    primaryStage.setScene(startupScene);
    primaryStage.setTitle("Millions");
    primaryStage.setMaximized(true);
    primaryStage.setResizable(true);
    primaryStage.setMinWidth(600);
    primaryStage.setMinHeight(0);
    primaryStage.centerOnScreen();
    primaryStage.show();
  }

  /**
   * <p>Opens a file chooser for selecting a stock data CSV file.</p>
   */
  public void handleBrowse() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select Stock Data File");
    fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
    );

    File file = fileChooser.showOpenDialog(primaryStage);
    if (file != null) {
      selectedFile = file;
      screen.setFileName(file.getName());
    }
  }

  /**
   * <p>Validates input, loads stock data, and launches the game on success.
   * On any failure (invalid capital, stock load error) an error message is
   * shown on the startup screen and the game does not start.</p>
   *
   * @param name player name input.
   * @param capitalText starting capital input.
   */
  private void handleStart(String name, String capitalText) {
    BigDecimal capital = parseCapital(capitalText);
    if (capital == null) return;
    List<Stock> stocks;
    try {
      stocks = StockLoader.load(selectedFile);
    } catch (StockFileException e) {
      screen.showError(e.getMessage());
      return;
    }

    Player player = new Player(name, capital);
    Exchange exchange = new Exchange("Global Exchange", stocks);

    launchGame(player, exchange);
  }

  /**
   * <p>Parses the starting-capital input. Shows an error on the startup
   * screen and returns {@code null} if the value is not a positive number.</p>
   *
   * @param capitalText the user input text
   * @return the parsed capital, or {@code null} on invalid input
   */
  private BigDecimal parseCapital(String capitalText) {
    try {
      BigDecimal capital = new BigDecimal(capitalText);
      if (capital.compareTo(BigDecimal.ZERO) <= 0) {
        screen.showError("Starting capital must be a positive number.");
        return null;
      }
      return capital;
    } catch (NumberFormatException e) {
      screen.showError("Starting capital must be a positive number.");
      return null;
    }
  }

  /**
   * <p>Creates the main view, wires the game controller, and switches the
   * stage to the game UI.</p>
   *
   * @param player initialized player model.
   * @param exchange initialized exchange model.
   */
  private void launchGame(Player player, Exchange exchange) {
    MainView mainView = new MainView();
    new GameController(mainView, player, exchange);

    mainView.setOnNewGame(this::show);
    mainView.setOnExit(Platform::exit);

    primaryStage.setScene(mainView.createScene());
    primaryStage.setTitle("Millions - " + player.getName());
    primaryStage.setMaximized(true);
    primaryStage.setResizable(true);
    primaryStage.setMinWidth(850);
    primaryStage.setMinHeight(580);
    primaryStage.centerOnScreen();
  }

}
