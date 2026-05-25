package edu.ntnu.idi.idatt.millions;

import edu.ntnu.idi.idatt.millions.controller.SetupController;
import edu.ntnu.idi.idatt.millions.view.util.AppIcon;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * <p>Application entry point for the Millions stock trading game.</p>
 *
 * <p>Bootstraps the JavaFX runtime and hands control to
 * {@link SetupController}, which displays the startup screen and
 * transitions to the main game view once the player has configured
 * their session.</p>
 */
public class Main extends Application {

  /**
   * <p>JavaFX entry point. Creates the setup controller and shows the startup screen.</p>
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
   * <p>JVM entry point. Delegates to {@link Application#launch(String...)}.</p>
   *
   * @param args command-line arguments (unused)
   */
  public static void main(String[] args) {
    launch();
  }
}
