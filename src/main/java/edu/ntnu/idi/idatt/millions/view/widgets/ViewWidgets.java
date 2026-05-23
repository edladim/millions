package edu.ntnu.idi.idatt.millions.view.widgets;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

/**
 * <p>
 * Static factory methods for layout fragments that appear repeatedly across
 * the page views (Dashboard, Portfolio, Trading).
 * </p>
 *
 * <p>
 * Centralising these patterns keeps the views slim and ensures consistent
 * spacing, style classes, and overall look. Each factory returns a fully
 * configured node; callers may further tweak the result (e.g. override
 * padding) as needed.
 * </p>
 *
 * <p>This class cannot be instantiated.</p>
 */
public final class ViewWidgets {

  private ViewWidgets() {}

  /**
   * <p>Pair returned by {@link #summaryCard(String, String, String)} giving
   * callers typed access to both the outer card and its value label without
   * having to cast {@code card.getChildren().get(1)}.</p>
   *
   * @param card       the card container
   * @param valueLabel the label inside the card that displays the dynamic value
   */
  public record SummaryCard(VBox card, Label valueLabel) {}

  /**
   * <p>Builds a page header with a large title and a muted subtitle.</p>
   *
   * @param title    the page title text
   * @param subtitle the page subtitle text
   * @return a {@link VBox} containing the title above the subtitle
   */
  public static VBox pageHeader(String title, String subtitle) {
    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().add("page-title");

    Label subtitleLabel = new Label(subtitle);
    subtitleLabel.getStyleClass().add("page-subtitle");

    return new VBox(4, titleLabel, subtitleLabel);
  }

  /**
   * <p>Builds a small summary card: muted title above a bold value, used in
   * dashboards and portfolio overviews. The card stretches to fill horizontal
   * space when placed in an {@link HBox} with {@code HBox.setHgrow(card, ALWAYS)}.</p>
   *
   * @param title           the card's heading
   * @param initialValue    the initial value text (often {@code "$0.00"})
   * @param valueStyleClass the CSS class to apply to the value label
   * @return both the card container and its value label
   */
  public static SummaryCard summaryCard(String title, String initialValue, String valueStyleClass) {
    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().add("stat-card-title");

    Label valueLabel = new Label(initialValue);
    valueLabel.getStyleClass().add(valueStyleClass);

    VBox card = new VBox(12, titleLabel, valueLabel);
    card.getStyleClass().add("stat-card");
    card.setPadding(new Insets(10));
    card.setMaxWidth(Double.MAX_VALUE);
    return new SummaryCard(card, valueLabel);
  }

  /**
   * <p>Builds a section-sized card: a {@code .stat-card}-styled container with
   * 24&nbsp;px padding on all sides, used to wrap larger content groups such
   * as movers, holdings, and transaction tables.</p>
   *
   * <p>Callers that need asymmetric padding (e.g. {@code Insets(24, 14, 24, 24)}
   * to compensate for a TableView scrollbar gutter) can call
   * {@link VBox#setPadding(Insets)} on the returned card.</p>
   *
   * @param children the child nodes to add, in order
   * @return a {@link VBox} configured as a section card
   */
  public static VBox sectionCard(Node... children) {
    VBox card = new VBox(children);
    card.getStyleClass().add("stat-card");
    card.setPadding(new Insets(24));
    return card;
  }

  /**
   * <p>Builds a stock identity block with a coloured circle (containing the
   * symbol's first letter), the stock ticker symbol in bold, and the company
   * name in muted text below it.</p>
   *
   * <p>This is the variant used in the Trading and Portfolio tables, where
   * the ticker symbol is the primary identifier.</p>
   *
   * @param symbol  the ticker symbol; its first character is shown in the icon
   * @param company the full company name, shown in muted text below the symbol
   * @return an {@link HBox} containing the icon and the two-line label group
   */
  public static HBox stockIconBlock(String symbol, String company) {
    return buildIconBlock(symbol, symbol, company);
  }

  /**
   * <p>Stock identity block variant with the <em>company name</em> shown bold
   * and the ticker symbol underneath in muted text. Used in the Dashboard
   * top-gainers / top-losers panels where the human-readable company name is
   * the primary identifier.</p>
   *
   * @param symbol  the ticker symbol; its first character is shown in the icon
   * @param company the full company name, shown bold as the primary label
   * @return an {@link HBox} containing the icon and the two-line label group
   */
  public static HBox stockIconBlockCompanyFirst(String symbol, String company) {
    return buildIconBlock(symbol, company, symbol);
  }

  /**
   * <p>Internal builder shared by {@link #stockIconBlock(String, String)} and
   * {@link #stockIconBlockCompanyFirst(String, String)}. The icon always
   * displays the first character of the ticker symbol regardless of which
   * label is rendered as primary.</p>
   *
   * @param symbol  the ticker symbol; used only to pick the icon letter
   * @param primary the bold (top) label text
   * @param muted   the muted (bottom) label text
   * @return the configured row
   */
  private static HBox buildIconBlock(String symbol, String primary, String muted) {
    Circle circle = new Circle(18);
    circle.getStyleClass().add("stock-icon");
    Label letter = new Label(String.valueOf(symbol.charAt(0)));
    letter.getStyleClass().add("mover-icon-letter");
    StackPane iconPane = new StackPane(circle, letter);

    Label primaryLabel = new Label(primary);
    primaryLabel.getStyleClass().add("mover-name");

    Label mutedLabel = new Label(muted);
    mutedLabel.getStyleClass().add("mover-symbol");
    mutedLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
    mutedLabel.setMinWidth(0);

    VBox labels = new VBox(2, primaryLabel, mutedLabel);
    HBox.setHgrow(labels, Priority.ALWAYS);
    HBox row = new HBox(10, iconPane, labels);
    row.setAlignment(Pos.CENTER_LEFT);
    return row;
  }

  /**
   * <p>Creates a section-heading label (large, bold) used to introduce a
   * group of content within a card.</p>
   *
   * @param text the heading text
   * @return the configured label
   */
  public static Label sectionHeading(String text) {
    Label label = new Label(text);
    label.getStyleClass().add("section-heading");
    return label;
  }

  /**
   * <p>Creates a smaller, uppercase sub-heading label used to label
   * sub-sections inside a card (e.g. "Top Performers").</p>
   *
   * @param text the sub-heading text
   * @return the configured label
   */
  public static Label sectionSubheading(String text) {
    Label label = new Label(text);
    label.getStyleClass().add("section-subheading");
    return label;
  }

  /**
   * <p>Creates a centred placeholder label shown when a list or section has
   * no data (e.g. "You don't own any shares yet.").</p>
   *
   * @param text the placeholder text
   * @return the configured label
   */
  public static Label emptyLabel(String text) {
    Label label = new Label(text);
    label.getStyleClass().add("empty-label");
    label.setMaxWidth(Double.MAX_VALUE);
    label.setAlignment(Pos.CENTER);
    return label;
  }

  /**
   * <p>Creates an invisible vertical spacer of the given height. Use for
   * adding fixed vertical gaps in a {@link VBox} without relying on spacing.</p>
   *
   * @param height the spacer height in pixels
   * @return the spacer region
   */
  public static Region spacer(double height) {
    Region region = new Region();
    region.setPrefHeight(height);
    return region;
  }

  /**
   * <p>Creates a 1&nbsp;px-tall horizontal divider line styled via the
   * {@code .divider} CSS class.</p>
   *
   * @return the divider region
   */
  public static Region divider() {
    Region region = new Region();
    region.getStyleClass().add("divider");
    region.setPrefHeight(1);
    region.setMaxWidth(Double.MAX_VALUE);
    return region;
  }
}
