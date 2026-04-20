package edu.ntnu.idi.idatt.millions.controller;

import edu.ntnu.idi.idatt.millions.filehandler.CsvStockReader;
import edu.ntnu.idi.idatt.millions.filehandler.StockReader;
import edu.ntnu.idi.idatt.millions.model.Exchange;
import edu.ntnu.idi.idatt.millions.model.Player;
import edu.ntnu.idi.idatt.millions.model.Stock;
import edu.ntnu.idi.idatt.millions.view.MainView;
import edu.ntnu.idi.idatt.millions.view.StartupScreen;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

public class SetupController {

  private static final String DEFAULT_STOCK_FILE = "/StockData.csv";
  private final Stage primaryStage;
  private final StartupScreen screen;
  private File selectedFile = null;

  public SetupController(Stage primaryStage) {
    this.primaryStage = primaryStage;
    this.screen = new StartupScreen();

    screen.setOnBrowse(this::handleBrowse);
  }

  public void show() {
    Scene scene = new Scene(screen, 520, 660);
    scene.getStylesheets().add(
            getClass().getResource("/styles/main.css").toExternalForm()


    );

    primaryStage.setScene(scene);
    primaryStage.setTitle("Millions");
    primaryStage.setWidth(520);
    primaryStage.setHeight(660);
    primaryStage.setResizable(false);
    primaryStage.centerOnScreen();
    primaryStage.show();
  }

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

    Player   player   = new Player(name, capital);
    Exchange exchange = new Exchange("Global Exchange", stocks);

    launchGame(player, exchange);

  }

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

  private void launchGame(Player player, Exchange exchange) {
    MainView mainView = new MainView();

    primaryStage.setScene(mainView.createScene());
    primaryStage.setTitle("Millions - " + player.getName());
    primaryStage.setWidth(1280);
    primaryStage.setHeight(800);
    primaryStage.setResizable(true);
    primaryStage.centerOnScreen();

  }

}
