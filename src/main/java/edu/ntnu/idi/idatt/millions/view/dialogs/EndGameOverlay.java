package edu.ntnu.idi.idatt.millions.view.dialogs;

import edu.ntnu.idi.idatt.millions.view.components.ViewWidgets;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * <p>
 * Overlay component shown when the game ends.
 * </p>
 *
 * <p>
 * Renders a dimmed full-screen backdrop with a centered card. The card is
 * sized to its content and floats centered in the overlay with equal space
 * above and below. Stat cards are built with {@link ViewWidgets#summaryCard}
 * using compact padding and spacing so everything fits within the minimum
 * window height. The Profit and Return Rate stats (indices 1 and 2) are
 * placed side by side.
 * </p>
 */
public class EndGameOverlay extends StackPane {

  /**
   * A single labelled stat shown on the end-game overlay.
   *
   * @param label the stat's display label (e.g. {@code "Net Worth"})
   * @param value the formatted stat value (e.g. {@code "$12 345"})
   */
  public record StatRow(String label, String value) {}

  private final VBox card;
  private final Label title;
  private final VBox statsBox;
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
    setPadding(new Insets(20, 0, 20, 0));

    card = new VBox(0);
    card.getStyleClass().add("startup-card");
    card.setMaxWidth(550);
    card.setMinWidth(550);

    title = new Label("Game Over");
    title.getStyleClass().add("endgame-title");
    Label subtitle = new Label("Your final results");
    subtitle.getStyleClass().add("startup-subtitle");

    VBox header = new VBox(6, title, subtitle);
    header.setAlignment(Pos.CENTER);
    header.setPadding(new Insets(16, 40, 14, 40));
    header.getStyleClass().add("startup-header");

    statsBox = new VBox(4);
    statsBox.setPadding(new Insets(8, 16, 8, 16));

    newGameBtn = new Button("New Game  →");
    newGameBtn.getStyleClass().add("startup-start-btn");
    newGameBtn.setMaxWidth(Double.MAX_VALUE);

    exitBtn = new Button("Exit");
    exitBtn.getStyleClass().add("startup-browse-btn");
    exitBtn.setMaxWidth(Double.MAX_VALUE);

    VBox buttonBox = new VBox(10, newGameBtn, exitBtn);
    buttonBox.setPadding(new Insets(12, 40, 18, 40));
    buttonBox.getStyleClass().add("startup-body");

    card.getChildren().addAll(header, statsBox, buttonBox);

    // VBox wrapper keeps card at its natural height and centers it vertically.
    // StackPane alone would stretch the card to fill the window height.
    VBox wrapper = new VBox(card);
    wrapper.setAlignment(Pos.CENTER);
    wrapper.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    getChildren().add(wrapper);
  }

  /**
   * <p>
   * Builds a compact stat card via {@link ViewWidgets#summaryCard} with reduced
   * padding and tighter internal spacing so the overlay fits within the minimum
   * window height.
   * </p>
   *
   * @param label the stat's display label
   * @param value the formatted stat value
   * @return a card VBox sized to its natural content height
   */
  private VBox buildStatCard(String label, String value) {
    VBox statCard = ViewWidgets.summaryCard(label, value, "endgame-stat-value").card();
    statCard.setSpacing(4);
    statCard.setPadding(new Insets(6, 12, 6, 12));
    return statCard;
  }

  /**
   * <p>
   * Displays the overlay and renders stat cards for each entry in {@code stats}.
   * Stats at index&nbsp;1 and&nbsp;2 (Profit and Return Rate) are placed side
   * by side; all other stats get their own full-width row.
   * </p>
   *
   * @param stats the list of label/value pairs to display
   */
  public void show(List<StatRow> stats) {
    statsBox.getChildren().clear();

    for (int i = 0; i < stats.size(); i++) {
      if (i == 1 && i + 1 < stats.size()) {
        VBox left  = buildStatCard(stats.get(i).label(),     stats.get(i).value());
        VBox right = buildStatCard(stats.get(i + 1).label(), stats.get(i + 1).value());
        HBox.setHgrow(left,  Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);

        HBox row = new HBox(4, left, right);
        statsBox.getChildren().add(row);
        i++;
      } else {
        statsBox.getChildren().add(buildStatCard(stats.get(i).label(), stats.get(i).value()));
      }
    }

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
