package edu.ntnu.idi.idatt.millions.view;

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

    title = new Label("Game Over");
    finalStats = new Label();
    newGameBtn = new Button("New Game");
    exitBtn = new Button("Exit");

    card = new VBox(12, title, finalStats, newGameBtn, exitBtn);
    card.getStyleClass().add("endgame-card");

    getChildren().add(card);
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
