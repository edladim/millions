package edu.ntnu.idi.idatt.millions;

import edu.ntnu.idi.idatt.millions.controller.SetupController;
import edu.ntnu.idi.idatt.millions.view.util.AppIcon;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Application entry point for the Millions stock trading game.
 *
 * <p>Bootstraps the JavaFX runtime and hands control to {@link SetupController}, which displays the
 * startup screen and transitions to the main game view once the player has configured their
 * session.
 */
public class Main extends Application {

  /**
   * JavaFX entry point. Creates the setup controller and shows the startup screen.
   *
   * @param primaryStage the primary window provided by the JavaFX runtime
   */
  @Override
  public void start(Stage primaryStage) {
    AppIcon.apply(primaryStage);
    SetupController setup = new SetupController(primaryStage);
    setup.show();
  }

  /**
   * JVM entry point. Delegates to {@link Application#launch(String...)}.
   *
   * @param args command-line arguments (unused)
   */
  static void main(String[] args) {
    launch(args);
  }
}
