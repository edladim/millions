package edu.ntnu.idi.idatt.millions.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * <p>
 * Overlay component shown when the game ends.
 * </p>
 *
 * <p>
 * Renders a dimmed full-screen backdrop with a centered card containing
 * summary stats and actions for starting a new game or exiting.
 * </p>
 */
public class EndGameOverlay extends StackPane {

  private final VBox card;
  private final Label title;
  private final Label finalStats;
  private final Button newGameBtn;
  private final Button exitBtn;

  /**
   * <p>Constructs the end-game overlay and builds its UI.</p>
   */
  public EndGameOverlay() {
    getStyleClass().add("endgame-overlay");
    setVisible(false);
    setManaged(false);
    setPickOnBounds(true);

    StackPane bg = new StackPane();
    bg.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    bg.getStyleClass().add("endgame-overlay");
    bg.setMouseTransparent(true);

    card = new VBox(0);
    card.getStyleClass().add("startup-card");
    card.setMaxWidth(550);
    card.setMinWidth(550);

    title = new Label("Game Over");
    title.getStyleClass().add("startup-title");
    Label subtitle = new Label("Your final results");
    subtitle.getStyleClass().add("startup-subtitle");

    VBox header = new VBox(10, title, subtitle);
    header.setAlignment(Pos.CENTER);
    header.setPadding(new Insets(44, 40, 36, 40));
    header.getStyleClass().add("startup-header");

    finalStats = new Label();
    finalStats.getStyleClass().add("startup-hint");
    finalStats.setWrapText(true);

    newGameBtn = new Button("New Game  →");
    newGameBtn.getStyleClass().add("startup-start-btn");
    newGameBtn.setMaxWidth(Double.MAX_VALUE);

    exitBtn = new Button("Exit");
    exitBtn.getStyleClass().add("startup-browse-btn");
    exitBtn.setMaxWidth(Double.MAX_VALUE);

    VBox body = new VBox(20, finalStats, newGameBtn, exitBtn);
    body.setPadding(new Insets(32, 40, 36, 40));
    body.getStyleClass().add("startup-body");

    card.getChildren().addAll(header, body);

    getChildren().addAll(bg, card);
    StackPane.setAlignment(card, Pos.CENTER);
  }

  /**
   * <p>Displays the overlay and updates the stats text.</p>
   *
   * @param stats the formatted stats to show
   */
  public void show(String stats) {
    finalStats.setText(stats);
    setVisible(true);
    setManaged(true);
  }

  /**
   * <p>Hides the overlay and releases it from layout.</p>
   */
  public void hide() {
    setVisible(false);
    setManaged(false);
  }

  /**
   * <p>Registers a handler for the "New Game" action.</p>
   *
   * @param onNewGame the action to run
   */
  public void setOnNewGame(Runnable onNewGame) {
    newGameBtn.setOnAction(e -> onNewGame.run());
  }

  /**
   * <p>Registers a handler for the "Exit" action.</p>
   *
   * @param onExit the action to run
   */
  public void setOnExit(Runnable onExit) {
    exitBtn.setOnAction(e -> onExit.run());
  }
}
