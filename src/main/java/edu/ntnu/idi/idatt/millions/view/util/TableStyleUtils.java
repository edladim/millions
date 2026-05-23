package edu.ntnu.idi.idatt.millions.view.util;

import java.util.HashSet;
import java.util.Set;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * <p>
 * Static helpers for {@link TableView} styling that is shared across multiple
 * pages but cannot be expressed in pure CSS due to JavaFX's CSS limitations on
 * dynamic header state and pixel-perfect height locking.
 * </p>
 *
 * <p>This class cannot be instantiated.</p>
 */
public final class TableStyleUtils {

  /** CSS style class applied to header labels of currently sorted columns. */
  private static final String SORT_HEADER_ACTIVE_CLASS = "sort-header-active";

  private TableStyleUtils() {}

  /**
   * <p>Registers a listener on the table's sort order so the header label of
   * every column currently being sorted on is tagged with the
   * {@code .sort-header-active} CSS class, and the highlight is cleared from
   * other columns.</p>
   *
   * <p>Also schedules an initial pass on the JavaFX application thread so the
   * highlight is applied once the table skin has built its header nodes.</p>
   *
   * @param table the table whose sort header should be highlighted
   */
  public static void wireSortHeaderHighlight(TableView<?> table) {
    table.getSortOrder().addListener((ListChangeListener<TableColumn<?, ?>>)
        c -> applyHeaderStyles(table));
    Platform.runLater(() -> applyHeaderStyles(table));
  }

  /**
   * <p>Adds or removes the {@code .sort-header-active} CSS class on each
   * column header label according to the current sort order.</p>
   *
   * <p>Runs on the JavaFX application thread to ensure column header nodes
   * are present in the skin before the lookup.</p>
   *
   * @param table the table whose headers should be re-styled
   */
  public static void applyHeaderStyles(TableView<?> table) {
    Platform.runLater(() -> {
      Set<String> sortedTitles = new HashSet<>();
      for (TableColumn<?, ?> c : table.getSortOrder()) {
        sortedTitles.add(c.getText());
      }
      for (Node header : table.lookupAll(".column-header")) {
        Node labelNode = header.lookup(".label");
        if (!(labelNode instanceof Label lbl)) continue;
        String text = lbl.getText();
        if (text == null) continue;
        boolean shouldHighlight = sortedTitles.contains(text);
        boolean hasHighlight = lbl.getStyleClass().contains(SORT_HEADER_ACTIVE_CLASS);
        if (shouldHighlight && !hasHighlight) {
          lbl.getStyleClass().add(SORT_HEADER_ACTIVE_CLASS);
        } else if (!shouldHighlight && hasHighlight) {
          lbl.getStyleClass().remove(SORT_HEADER_ACTIVE_CLASS);
        }
      }
    });
  }

  /**
   * <p>Locks a table's height to exactly fit {@code pageSize} rows plus the
   * column header, so the surrounding card does not jump when the user
   * navigates between pages with a partial last page.</p>
   *
   * @param table the table whose height should be locked
   * @param pageSize the number of rows per page
   * @param rowHeight the fixed cell height in pixels
   * @param headerHeight the reserved column-header height in pixels
   */
  public static void applyFixedPageHeight(
      TableView<?> table, int pageSize, double rowHeight, double headerHeight) {
    table.setFixedCellSize(rowHeight);
    double total = pageSize * rowHeight + headerHeight;
    table.setMinHeight(total);
    table.setPrefHeight(total);
    table.setMaxHeight(total);
  }
}
