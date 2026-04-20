package edu.ntnu.idi.idatt.millions.controller;

import edu.ntnu.idi.idatt.millions.view.StartupScreen;
import javafx.stage.Stage;

import java.io.File;

public class SetupController {

  private static final String DEFAULT_STOCK_FILE = "/StockData.csv";
  private final Stage primaryStage;
  private final StartupScreen screen;
  private File selectedFile = null;

  public SetupController(Stage primaryStage) {
    this.primaryStage = primaryStage;
    this.screen = new StartupScreen();

  }

}
