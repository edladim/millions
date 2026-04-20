package edu.ntnu.idi.idatt.millions.controller;

import edu.ntnu.idi.idatt.millions.view.StartupScreen;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
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



}
