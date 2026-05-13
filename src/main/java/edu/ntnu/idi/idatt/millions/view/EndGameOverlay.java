package edu.ntnu.idi.idatt.millions.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class EndGameOverlay extends StackPane {

  private final VBox card;
  private final Label title;
  private final Label stats;
  private final Button newGameBtn;
  private final Button exitBtn;

  public EndGameOverlay() {
    setVisible(false);
    setManaged(false);
    setPickOnBounds(true);

    title = new Label("Game Over");
    stats = new Label();
    newGameBtn = new Button("New Game");
    exitBtn = new Button("Exit");

    card = new VBox(12, title, stats, newGameBtn, exitBtn);

    getChildren().add(card);
    StackPane.setAlignment(card, Pos.CENTER);
  }
}
