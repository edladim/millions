package edu.ntnu.idi.idatt.millions.controller;

import edu.ntnu.idi.idatt.millions.filehandler.CsvStockReader;
import edu.ntnu.idi.idatt.millions.model.Exchange;
import edu.ntnu.idi.idatt.millions.model.Player;
import edu.ntnu.idi.idatt.millions.model.Stock;
import edu.ntnu.idi.idatt.millions.view.MainView;
import edu.ntnu.idi.idatt.millions.view.StartupScreen;
import edu.ntnu.idi.idatt.millions.view.Stylesheets;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>Controller responsible for the startup flow and initial game setup.</p>
 * <p>Shows the startup screen, handles file selection, validates inputs, and
 * launches the main game view.</p>
 */
public class SetupController {

  private static final String DEFAULT_STOCK_FILE = "/StockData.csv";
  private final Stage primaryStage;
  private final StartupScreen screen;
  private File selectedFile = null;

  /**
   * <p>Creates a setup controller bound to the primary stage.</p>
   *
   * @param primaryStage application window to control.
   */
  public SetupController(Stage primaryStage) {
    this.primaryStage = primaryStage;
    this.screen = new StartupScreen();

    screen.setOnBrowse(this::handleBrowse);
    screen.setOnStart(this::handleStart);
  }

  /**
   * <p>Loads bundled Inter font weights so the CSS {@code -fx-font-family: "Inter"}
   * declaration resolves correctly on all platforms.</p>
   *
   * <p>Missing font files are silently ignored — the app will fall back to the
   * next font in the CSS chain.</p>
   */
  private void loadFonts() {
    String[] weights = {"Regular", "Medium", "SemiBold", "Bold"};
    for (String w : weights) {
      var stream = getClass().getResourceAsStream("/fonts/Inter-" + w + ".ttf");
      if (stream != null) {
        Font.loadFont(stream, 14);
      }
    }
  }

  /**
   * <p>Displays the startup screen and applies base styles.</p>
   */
  public void show() {
    loadFonts();
    Scene scene = new Scene(screen, 520, 660);
    scene.getStylesheets().add(Stylesheets.load("/styles/main.css"));

    primaryStage.setScene(scene);
    primaryStage.setTitle("Millions");
    primaryStage.setMaximized(true);
    primaryStage.setResizable(true);
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
   * <p>Validates input and starts the game if successful.</p>
   *
   * @param name player name input.
   * @param capitalText starting capital input.
   */
  private void handleStart(String name, String capitalText) {
    BigDecimal capital;
    try {
      capital = new BigDecimal(capitalText);
      if (capital.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
    } catch (NumberFormatException e) {
      screen.showError("Starting capital must be a positive number.");
      return;
    }

    List<Stock> stocks = loadStocks();
    if (stocks == null) return;

    if (stocks.isEmpty()) {
      screen.showError("The file contained no valid stocks. Check the format.");
      return;
    }

    Player player   = new Player(name, capital);
    Exchange exchange = new Exchange("Global Exchange", stocks);

    launchGame(player, exchange);

  }

  /**
   * <p>Loads stock data from a selected file or the default classpath resource.</p>
   *
   * @return list of loaded stocks, or null if loading failed.
   */
  private List<Stock> loadStocks() {
    if (selectedFile != null) {
      try {
        return new CsvStockReader(selectedFile.toPath()).readStockData();
      } catch (Exception e) {
        screen.showError("Could not read file: " + e.getMessage());
        return null;
      }
    }

    try {
      InputStream stream = getClass().getResourceAsStream(DEFAULT_STOCK_FILE);
      if (stream == null) {
        screen.showError(
                "Default StockData.csv not found. Please select a file manually."
        );
        return null;
      }
      return CsvStockReader.fromClasspath(DEFAULT_STOCK_FILE).readStockData();
    } catch (Exception e) {
      screen.showError("Could not load default stock data: " + e.getMessage());
      return null;
    }
  }

  /**
   * <p>Creates the main view and switches the stage to the game UI.</p>
   *
   * @param player initialized player model.
   * @param exchange initialized exchange model.
   */
  private void launchGame(Player player, Exchange exchange) {
    MainView mainView = new MainView();
    new MainController(mainView, player, exchange);

    primaryStage.setScene(mainView.createScene());
    primaryStage.setTitle("Millions - " + player.getName());
    primaryStage.setMaximized(true);
    primaryStage.setResizable(true);
    primaryStage.setMinWidth(820);
    primaryStage.setMinHeight(580);
    primaryStage.centerOnScreen();
  }

}
