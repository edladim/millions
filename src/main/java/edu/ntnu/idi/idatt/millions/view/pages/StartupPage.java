package edu.ntnu.idi.idatt.millions.view.pages;

import edu.ntnu.idi.idatt.millions.model.player.LeaderboardEntry;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * <p>Startup screen view that collects initial player settings before the game starts.</p>
 * <p>Builds the background, card UI, and input fields, and exposes callbacks for browsing
 * and starting the game. Also renders the top-{@code N} leaderboard underneath the
 * Start Game button — entries are injected by the controller via
 * {@link #setLeaderboard(List)} so this view stays free of file I/O.</p>
 */
public class StartupPage extends StackPane{

  private static final int LEADERBOARD_SIZE = 5;

  private TextField nameField;
  private TextField capitalField;
  private Label fileNameLabel;
  private Label errorLabel;
  private VBox leaderboardRows;
  private Runnable onBrowse;
  private BiConsumer<String, String> onStart;

  /**
   * <p>Creates the startup screen and initializes UI layout, styles, and fade-in animation.</p>
   */
  public StartupPage() {
    getStyleClass().add("startup-root");
    setAlignment(Pos.CENTER);

    getChildren().addAll(
            buildBackground(),
            buildCard()
    );

    setLeaderboard(Collections.emptyList());

    FadeTransition fade = new FadeTransition(Duration.millis(400), this);
    fade.setFromValue(0);
    fade.setToValue(1);
    fade.play();
  }

  /**
   * <p>Builds the decorative background layer for the startup screen.</p>
   *
   * @return background stack pane with blobs.
   */
  private StackPane buildBackground() {
    StackPane bg = new StackPane();
    bg.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    bg.getStyleClass().add("startup-bg");

    bg.getChildren().addAll(
            buildBlob(320, 320, "#6366f1", 0.18, -300, -200),
            buildBlob(280, 280, "#818cf8", 0.14,  300,  250),
            buildBlob(200, 200, "#a5b4fc", 0.12, -100,  300)
    );

    return bg;
  }

  /**
   * <p>Builds a single translucent blob used in the background.</p>
   *
   * @param w width of the blob.
   * @param h height of the blob.
   * @param color hex color string.
   * @param opacity alpha value from 0.0 to 1.0.
   * @param tx translate X offset.
   * @param ty translate Y offset.
   * @return a stack pane containing the blob rectangle.
   */
  private StackPane buildBlob(double w, double h, String color,
                              double opacity, double tx, double ty) {
    Rectangle blob = new Rectangle(w, h);
    blob.setArcWidth(w);
    blob.setArcHeight(h);
    blob.setFill(Color.web(color, opacity));
    blob.setTranslateX(tx);
    blob.setTranslateY(ty);

    StackPane p = new StackPane(blob);
    p.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    p.setMouseTransparent(true);
    return p;
  }

  /**
   * <p>Builds the main card containing header and body sections.</p>
   *
   * @return card container.
   */
  private VBox buildCard() {
    VBox card = new VBox(0);
    card.getStyleClass().add("startup-card");
    card.setMaxWidth(550);
    card.setMinWidth(550);

    card.getChildren().addAll(
            buildCardHeader(),
            buildCardBody()
    );

    return card;
  }

  /**
   * <p>Builds the header section with logo, title, subtitle, and feature pills.</p>
   *
   * @return header container.
   */
  private VBox buildCardHeader() {
    FontIcon icon = new FontIcon("fas-wave-square");
    icon.setIconSize(28);
    icon.setIconColor(Color.WHITE);
    icon.getStyleClass().add("logo-icon-label");

    Label title = new Label("Millions");
    title.getStyleClass().add("startup-title");

    Label subtitle = new Label("Your personal stock market simulator");
    subtitle.getStyleClass().add("startup-subtitle");

    HBox pills = buildFeaturePills();

    VBox header = new VBox(8, icon, title, subtitle, pills);
    header.setAlignment(Pos.CENTER);
    header.setPadding(new Insets(24, 40, 20, 40));
    header.getStyleClass().add("startup-header");
    return header;
  }

  /**
   * <p>Builds the row of feature pills shown under the header.</p>
   *
   * @return pills container.
   */
  private HBox buildFeaturePills() {
    HBox pills = new HBox(10,
            buildPill(new FontIcon("fas-dollar-sign"), "Live Prices"),
            buildPill(new FontIcon("fas-id-card"), "Portfolio"),
            buildPill(new FontIcon("fas-chart-line"), "Trading")
    );
    pills.setAlignment(Pos.CENTER);
    return pills;
  }

  /**
   * <p>Builds a single feature pill with an icon and label.</p>
   *
   * @param icon icon to display in the pill.
   * @param text label text for the pill.
   * @return pill container.
   */
  private HBox buildPill(FontIcon icon, String text) {
    Label label = new Label(text);
    label.getStyleClass().add("startup-pill-text");

    icon.getStyleClass().add("startup-pill-icon");
    icon.setIconSize(14);

    HBox pill = new HBox(6, icon, label);
    pill.getStyleClass().add("startup-pill");
    pill.setAlignment(Pos.CENTER);
    return pill;
  }

  /**
   * <p>Builds the body section with input fields, file selection, start button, and leaderboard.</p>
   *
   * @return body container.
   */
  private VBox buildCardBody() {
    VBox body = new VBox(20);
    body.setPadding(new Insets(32, 40, 36, 40));
    body.getStyleClass().add("startup-body");

    nameField = new TextField();
    nameField.setPromptText("e.g. Warren Buffett");
    nameField.getStyleClass().add("startup-field");

    capitalField = new TextField("10000");
    capitalField.getStyleClass().add("startup-field");

    VBox fileSection = buildFileSection();

    errorLabel = new Label("");
    errorLabel.getStyleClass().add("startup-error");
    errorLabel.setVisible(false);
    errorLabel.setManaged(false);

    Button startBtn = new Button("Start Game  →");
    startBtn.getStyleClass().add("startup-start-btn");
    startBtn.setMaxWidth(Double.MAX_VALUE);
    startBtn.setOnAction(e -> handleStart());

    nameField.setOnAction(e -> handleStart());
    capitalField.setOnAction(e -> handleStart());

    body.getChildren().addAll(
            buildFieldGroup("Your Name", nameField),
            buildFieldGroup("Starting Capital ($)", capitalField),
            buildFieldGroup("Stock Data File", fileSection),
            errorLabel,
            startBtn,
            buildLeaderboardSection()
    );

    return body;
  }

  /**
   * <p>Builds the file selection row with filename label and browse button.</p>
   *
   * @return file section container.
   */
  private VBox buildFileSection() {
    fileNameLabel = new Label("StockData.csv  (default)");
    fileNameLabel.getStyleClass().add("startup-file-label");
    fileNameLabel.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(fileNameLabel, Priority.ALWAYS);

    Button browseBtn = new Button("Browse…");
    browseBtn.getStyleClass().add("startup-browse-btn");
    browseBtn.setOnAction(e -> { if (onBrowse != null) onBrowse.run(); });

    HBox fileRow = new HBox(12, fileNameLabel, browseBtn);
    fileRow.setAlignment(Pos.CENTER_LEFT);
    fileRow.getStyleClass().add("startup-file-row");

    Label hint = new Label("Leave blank to use the built-in StockData.csv");
    hint.getStyleClass().add("startup-hint");

    VBox section = new VBox(6, fileRow, hint);
    return section;
  }

  /**
   * <p>Builds the leaderboard section shown beneath the Start Game button.</p>
   *
   * <p>The row container is stored on the instance so that
   * {@link #setLeaderboard(List)} can rebuild the rows without rebuilding the
   * surrounding header.</p>
   *
   * @return leaderboard section container.
   */
  private VBox buildLeaderboardSection() {
    FontIcon trophy = new FontIcon("fas-trophy");
    trophy.setIconSize(14);
    trophy.getStyleClass().add("leaderboard-trophy");

    Label title = new Label("Leaderboard");
    title.getStyleClass().add("leaderboard-title");

    HBox header = new HBox(8, trophy, title);
    header.setAlignment(Pos.CENTER_LEFT);

    leaderboardRows = new VBox(4);

    VBox section = new VBox(10, header, leaderboardRows);
    section.getStyleClass().add("leaderboard-section");
    return section;
  }

  /**
   * <p>Builds a single leaderboard row with rank, name, and score.</p>
   *
   * @param rank  the 1-based rank to display.
   * @param name  the player's name, or {@code "—"} for an empty slot.
   * @param score the score string to display, or {@code "—"} for an empty slot.
   * @param empty whether this row represents an empty slot (used for styling).
   * @return row container.
   */
  private HBox buildLeaderboardRow(int rank, String name, String score, boolean empty) {
    Label rankLabel = new Label("#" + rank);
    rankLabel.getStyleClass().add("leaderboard-rank");

    Label nameLabel = new Label(name);
    nameLabel.getStyleClass().add("leaderboard-name");
    nameLabel.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(nameLabel, Priority.ALWAYS);

    Label scoreLabel = new Label(score);
    scoreLabel.getStyleClass().add("leaderboard-score");

    HBox row = new HBox(12, rankLabel, nameLabel, scoreLabel);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("leaderboard-row");
    if (empty) {
      row.getStyleClass().add("leaderboard-row-empty");
    }
    return row;
  }

  /**
   * <p>Builds a labeled field group for inputs.</p>
   *
   * @param labelText text shown above the input.
   * @param input input node to place under the label.
   * @return grouped container.
   */
  private VBox buildFieldGroup(String labelText, Node input) {
    Label label = new Label(labelText);
    label.getStyleClass().add("startup-field-label");
    VBox group = new VBox(6, label, input);
    return group;
  }

  /**
   * <p>Validates inputs and triggers the start callback if valid.</p>
   */
  private void handleStart() {
    hideError();

    String name = nameField.getText().trim();
    if (name.isBlank()) {
      showError("Please enter your name.");
      nameField.requestFocus();
      return;
    }

    if (onStart != null) {
      onStart.accept(name, capitalField.getText().trim());
    }
  }

  /**
   * <p>Registers a callback for the browse action.</p>
   *
   * @param handler runnable to execute when browsing for a file.
   */
  public void setOnBrowse(Runnable handler) { this.onBrowse = handler; }

  /**
   * <p>Registers a callback for the start action.</p>
   *
   * @param handler consumer receiving name and capital input.
   */
  public void setOnStart(BiConsumer<String, String> handler) { this.onStart  = handler; }

  /**
   * <p>Gets the trimmed name input from the field.</p>
   *
   * @return name input string.
   */
  public String getNameInput()    { return nameField.getText().trim(); }

  /**
   * <p>Gets the trimmed capital input from the field.</p>
   *
   * @return capital input string.
   */
  public String getCapitalInput() { return capitalField.getText().trim(); }

  /**
   * <p>Updates the displayed filename and applies the selected styling.</p>
   *
   * @param name display name of the chosen file.
   */
  public void setFileName(String name) {
    fileNameLabel.setText(name);
    fileNameLabel.getStyleClass().removeAll("startup-file-label-default");
    fileNameLabel.getStyleClass().add("startup-file-label-selected");
  }

  /**
   * <p>Replaces the rendered leaderboard rows with the given entries.</p>
   *
   * <p>The first {@code MAX_ENTRIES} entries are rendered; remaining slots are
   * padded with placeholder rows so the layout height stays stable regardless
   * of how many real entries exist.</p>
   *
   * @param entries the entries to display, already sorted by score descending.
   *                Must be non-null but may be empty.
   */
  public void setLeaderboard(List<LeaderboardEntry> entries) {
    if (leaderboardRows == null) return;
    leaderboardRows.getChildren().clear();
    for (int i = 0; i < LEADERBOARD_SIZE; i++) {
      if (i < entries.size()) {
        LeaderboardEntry entry = entries.get(i);
        leaderboardRows.getChildren().add(
            buildLeaderboardRow(i + 1, entry.name(),
                String.format("%,d", entry.score()), false));
      } else {
        leaderboardRows.getChildren().add(
            buildLeaderboardRow(i + 1, "—", "—", true));
      }
    }
  }

  /**
   * <p>Displays an error message with a brief fade-in animation.</p>
   *
   * @param message error text to display.
   */
  public void showError(String message) {
    errorLabel.setText("⚠  " + message);
    errorLabel.setVisible(true);
    errorLabel.setManaged(true);

    FadeTransition fade = new FadeTransition(Duration.millis(200), errorLabel);
    fade.setFromValue(0);
    fade.setToValue(1);
    fade.play();
  }

  /**
   * <p>Hides the error message and removes it from layout flow.</p>
   */
  public void hideError() {
    errorLabel.setVisible(false);
    errorLabel.setManaged(false);
  }

}
