package edu.ntnu.idi.idatt.millions;

import edu.ntnu.idi.idatt.millions.controller.SetupController;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

  @Override
  public void start(Stage primaryStage) {
    SetupController setup = new SetupController(primaryStage);
    setup.show();
  }

  public static void main(String[] args) {
    launch();
  }
}
