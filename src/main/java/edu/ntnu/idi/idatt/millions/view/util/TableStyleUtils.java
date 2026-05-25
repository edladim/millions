package edu.ntnu.idi.idatt.millions.view.util;

import edu.ntnu.idi.idatt.millions.view.components.ViewWidgets;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Static helpers for {@link TableView} styling that is shared across multiple pages but cannot be
 * expressed in pure CSS due to JavaFX's CSS limitations on dynamic header state and pixel-perfect
 * height locking.
 *
 * <p>This class cannot be instantiated.
 */
public final class TableStyleUtils {

  /** CSS style class applied to header labels of currently sorted columns. */
  private static final String SORT_HEADER_ACTIVE_CLASS = "sort-header-active";

  /** Default fixed cell height used across all paginated tables. */
  public static final double ROW_HEIGHT = 58;

  /** Default reserved column-header height used across all paginated tables. */
  public static final double TABLE_HEADER_HEIGHT = 40;

  /**
   * Table width (px) below which compact short-form labels and formatters are used in portfolio and
   * transaction tables.
   */
  public static final double COMPACT_THRESHOLD = 750;

  private TableStyleUtils() {}

  /**
   * Registers a listener on the table's sort order so the header label of every column currently
   * being sorted on is tagged with the {@code .sort-header-active} CSS class, and the highlight is
   * cleared from other columns.
   *
   * <p>Also schedules an initial pass on the JavaFX application thread so the highlight is applied
   * once the table skin has built its header nodes.
   *
   * @param table the table whose sort header should be highlighted
   */
  public static void wireSortHeaderHighlight(TableView<?> table) {
    table
        .getSortOrder()
        .addListener((ListChangeListener<TableColumn<?, ?>>) c -> applyHeaderStyles(table));
    Platform.runLater(() -> applyHeaderStyles(table));
  }

  /**
   * Adds or removes the {@code .sort-header-active} CSS class on each column header label according
   * to the current sort order.
   *
   * <p>Runs on the JavaFX application thread to ensure column header nodes are present in the skin
   * before the lookup.
   *
   * @param table the table whose headers should be re-styled
   */
  public static void applyHeaderStyles(TableView<?> table) {
    Platform.runLater(
        () -> {
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
   * Locks a table's height to exactly fit {@code pageSize} rows plus the column header, so the
   * surrounding card does not jump when the user navigates between pages with a partial last page.
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

  /**
   * Overload that uses the standard {@link #ROW_HEIGHT} and {@link #TABLE_HEADER_HEIGHT} defaults.
   *
   * @param table the table whose height should be locked
   * @param pageSize the number of rows per page
   */
  public static void applyFixedPageHeight(TableView<?> table, int pageSize) {
    applyFixedPageHeight(table, pageSize, ROW_HEIGHT, TABLE_HEADER_HEIGHT);
  }

  /**
   * Binds a column's header text so it switches between a short and a long label when the table
   * width crosses {@link #COMPACT_THRESHOLD}.
   *
   * @param col the column whose header text to bind
   * @param table the parent table whose width drives the toggle
   * @param shortText the label shown when the table is narrow
   * @param longText the label shown when the table has room
   */
  public static void bindCompactText(
      TableColumn<?, ?> col, TableView<?> table, String shortText, String longText) {
    col.textProperty()
        .bind(
            Bindings.when(table.widthProperty().lessThan(COMPACT_THRESHOLD))
                .then(shortText)
                .otherwise(longText));
  }

  /**
   * Builds a reusable "Stock" column that renders a circle icon, the ticker symbol in bold, and the
   * company name in muted text below it. Sorting is case-insensitive by symbol. Default widths are
   * {@code minWidth=160}, {@code prefWidth=200}; callers may override after construction.
   *
   * @param <T> the row type
   * @param symbolFn function extracting the ticker symbol from a row
   * @param companyFn function extracting the company name from a row
   * @return the configured stock column
   */
  public static <T> TableColumn<T, T> stockIconColumn(
      Function<T, String> symbolFn, Function<T, String> companyFn) {
    TableColumn<T, T> col = new TableColumn<>("Stock");
    col.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue()));
    col.setCellFactory(
        c ->
            new TableCell<>() {
              @Override
              protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(
                    empty || item == null
                        ? null
                        : ViewWidgets.stockIconBlock(symbolFn.apply(item), companyFn.apply(item)));
              }
            });
    col.setMinWidth(160);
    col.setPrefWidth(200);
    col.setComparator((a, b) -> symbolFn.apply(a).compareToIgnoreCase(symbolFn.apply(b)));
    return col;
  }
}
