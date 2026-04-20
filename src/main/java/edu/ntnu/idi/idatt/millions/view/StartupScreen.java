package edu.ntnu.idi.idatt.millions.view;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.function.BiConsumer;

public class StartupScreen extends StackPane{

  private TextField nameField;
  private TextField capitalField;
  private Label fileNameLabel;
  private Label     errorLabel;
  private Runnable onBrowse;
  private BiConsumer<String, String> onStart;

  public StartupScreen() {
    getStyleClass().add("startup-root");
    setAlignment(Pos.CENTER);

    getChildren().addAll(
            buildBackground(),
            buildCard()
    );

    FadeTransition fade = new FadeTransition(Duration.millis(400), this);
    fade.setFromValue(0);
    fade.setToValue(1);
    fade.play();
  }

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

  private VBox buildCardHeader() {
    FontIcon icon = new FontIcon("fas-wave-square");
    icon.setIconSize(34);
    icon.setIconColor(Color.WHITE);
    icon.getStyleClass().add("logo-icon-label");

    Label title = new Label("Millions");
    title.getStyleClass().add("startup-title");

    Label subtitle = new Label("Your personal stock market simulator");
    subtitle.getStyleClass().add("startup-subtitle");

    HBox pills = buildFeaturePills();

    VBox header = new VBox(10, icon, title, subtitle, pills);
    header.setAlignment(Pos.CENTER);
    header.setPadding(new Insets(44, 40, 36, 40));
    header.getStyleClass().add("startup-header");
    return header;
  }

  private HBox buildFeaturePills() {
    HBox pills = new HBox(10,
            buildPill(new FontIcon("fas-dollar-sign"), "Live Prices"),
            buildPill(new FontIcon("fas-id-card"), "Portfolio"),
            buildPill(new FontIcon("fas-chart-line"), "Trading")
    );
    pills.setAlignment(Pos.CENTER);
    return pills;
  }

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

  private VBox buildCardBody() {
    VBox body = new VBox(20);
    body.setPadding(new Insets(32, 40, 36, 40));
    body.getStyleClass().add("startup-body");

    // Name field
    nameField = new TextField();
    nameField.setPromptText("e.g. Warren Buffett");
    nameField.getStyleClass().add("startup-field");

    // Capital field
    capitalField = new TextField("10000");
    capitalField.getStyleClass().add("startup-field");

    // File row
    VBox fileSection = buildFileSection();

    // Error label
    errorLabel = new Label("");
    errorLabel.getStyleClass().add("startup-error");
    errorLabel.setVisible(false);
    errorLabel.setManaged(false);

    // Start button
    Button startBtn = new Button("Start Game  →");
    startBtn.getStyleClass().add("startup-start-btn");
    startBtn.setMaxWidth(Double.MAX_VALUE);
    startBtn.setOnAction(e -> handleStart());

    // Enter key also triggers start
    nameField.setOnAction(e -> handleStart());
    capitalField.setOnAction(e -> handleStart());

    body.getChildren().addAll(
            buildFieldGroup("Your Name", nameField),
            buildFieldGroup("Starting Capital ($)", capitalField),
            buildFieldGroup("Stock Data File", fileSection),
            errorLabel,
            startBtn
    );

    return body;
  }

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

  private VBox buildFieldGroup(String labelText, javafx.scene.Node input) {
    Label label = new Label(labelText);
    label.getStyleClass().add("startup-field-label");
    VBox group = new VBox(6, label, input);
    return group;
  }

  private void handleStart() {
    hideError();

    String name = nameField.getText().trim();
    if (name.isBlank()) {
      showError("Please enter your name.");
      nameField.requestFocus();
      return;
    }

    String capitalText = capitalField.getText().trim();
    try {
      double capital = Double.parseDouble(capitalText);
      if (capital <= 0) throw new NumberFormatException();
    } catch (NumberFormatException e) {
      showError("Starting capital must be a positive number.");
      capitalField.requestFocus();
      return;
    }

    if (onStart != null) {
      onStart.accept(name, capitalText);
    }
  }

  public void setOnBrowse(Runnable handler)                       { this.onBrowse = handler; }
  public void setOnStart(BiConsumer<String, String> handler)      { this.onStart  = handler; }

  public String getNameInput()    { return nameField.getText().trim(); }
  public String getCapitalInput() { return capitalField.getText().trim(); }

  public void setFileName(String name) {
    fileNameLabel.setText(name);
    fileNameLabel.getStyleClass().removeAll("startup-file-label-default");
    fileNameLabel.getStyleClass().add("startup-file-label-selected");
  }

  public void showError(String message) {
    errorLabel.setText("⚠  " + message);
    errorLabel.setVisible(true);
    errorLabel.setManaged(true);

    FadeTransition fade = new FadeTransition(Duration.millis(200), errorLabel);
    fade.setFromValue(0);
    fade.setToValue(1);
    fade.play();
  }

  public void hideError() {
    errorLabel.setVisible(false);
    errorLabel.setManaged(false);
  }

}
