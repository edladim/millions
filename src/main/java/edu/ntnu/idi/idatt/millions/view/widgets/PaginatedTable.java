package edu.ntnu.idi.idatt.millions.view.widgets;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;

/**
 * <p>
 * Glue class that pairs a {@link TableView} with a {@link Pagination} control
 * so the table only shows a fixed-size slice of a larger backing list at a
 * time.
 * </p>
 *
 * <p>
 * The class owns a private <em>master</em> list of all rows; the bound
 * {@link TableView} only contains the items of the current page. A custom
 * {@link TableView#sortPolicyProperty()} sorts the master list (not just the
 * current page slice) so column-header clicks sort across all pages, and the
 * {@link Pagination} control auto-hides when only one page exists.
 * </p>
 *
 * <p>
 * Typical usage:
 * </p>
 * <pre>{@code
 * PaginatedTable<HoldingRow> holdings = new PaginatedTable<>(holdingsTable, 5, true);
 * holdings.setItems(rows);          // when data changes
 * section.getChildren().addAll(     // layout uses both nodes
 *     holdings.table(), holdings.pagination());
 * }</pre>
 *
 * @param <T> the row type
 */
public class PaginatedTable<T> {

  private final TableView<T> table;
  private final Pagination pagination;
  private final ObservableList<T> master = FXCollections.observableArrayList();
  private final int pageSize;
  private Runnable onPageRendered = () -> {};

  /**
   * <p>Wraps the given table with pagination of the given page size and style.</p>
   *
   * @param table       the {@link TableView} to drive
   * @param pageSize    the number of rows shown per page
   * @param bulletStyle if {@code true}, the pagination control uses bullet
   *                    indicators (suitable for few pages); otherwise it uses
   *                    numeric indicators (suitable for many pages)
   */
  public PaginatedTable(TableView<T> table, int pageSize, boolean bulletStyle) {
    this.table = table;
    this.pageSize = pageSize;
    this.pagination = buildPagination(bulletStyle);
    wireSortPolicy();
  }

  /**
   * @return the wrapped table (use to add columns and put it in a layout)
   */
  public TableView<T> table() {
    return table;
  }

  /**
   * @return the pagination control (add it to a layout next to the table)
   */
  public Pagination pagination() {
    return pagination;
  }

  /**
   * @return the current zero-based page index
   */
  public int currentPage() {
    return pagination.getCurrentPageIndex();
  }

  /**
   * <p>Registers a callback that runs after each page render. Useful for
   * re-applying a selection, highlight, or other view-specific state when
   * the visible slice changes.</p>
   *
   * @param hook the callback to run after every page render
   */
  public void setOnPageRendered(Runnable hook) {
    this.onPageRendered = hook == null ? () -> {} : hook;
  }

  /**
   * <p>Replaces the master list with the given items, re-applies the current
   * sort comparator, recomputes the page count, and renders the current page.
   * Hides the pagination control entirely if only one page is needed.</p>
   *
   * @param items the new full list of rows
   */
  public void setItems(List<? extends T> items) {
    master.setAll(items);
    Comparator<T> cmp = table.getComparator();
    if (cmp != null) FXCollections.sort(master, cmp);
    syncPagination();
    showPage(pagination.getCurrentPageIndex());
  }

  /**
   * <p>Jumps the pagination to the given page index (no-op if out of range).
   * Triggers a page render via the {@link Pagination}'s page factory.</p>
   *
   * @param index zero-based page index to jump to
   */
  public void goToPage(int index) {
    if (index < 0 || index >= pagination.getPageCount()) return;
    pagination.setCurrentPageIndex(index);
  }

  /**
   * <p>Finds the page index of the first row in the master list matching the
   * given predicate, or {@code -1} if no row matches.</p>
   *
   * @param match the predicate to test each row against
   * @return the matching row's page index, or {@code -1} if not found
   */
  public int findPageOf(Predicate<T> match) {
    for (int i = 0; i < master.size(); i++) {
      if (match.test(master.get(i))) return i / pageSize;
    }
    return -1;
  }

  /**
   * <p>Renders the page at the given index by replacing the table's items with
   * the matching slice of the master list. Always invokes the
   * {@link #setOnPageRendered post-render hook} when finished.</p>
   *
   * @param pageIndex the zero-based page index
   */
  public void showPage(int pageIndex) {
    int from = Math.max(0, pageIndex * pageSize);
    int to   = Math.min(from + pageSize, master.size());
    if (from >= master.size()) {
      table.getItems().clear();
    } else {
      table.getItems().setAll(master.subList(from, to));
    }
    onPageRendered.run();
  }

  /**
   * <p>Recalculates the page count from the master list, clamps the current
   * page index to a valid range, and toggles the pagination control's
   * visibility so it disappears entirely when only one page is needed.</p>
   */
  private void syncPagination() {
    int pageCount = Math.max(1, (int) Math.ceil(master.size() / (double) pageSize));
    pagination.setPageCount(pageCount);
    if (pagination.getCurrentPageIndex() >= pageCount) {
      pagination.setCurrentPageIndex(0);
    }
    boolean show = pageCount > 1;
    pagination.setVisible(show);
    pagination.setManaged(show);
  }

  /**
   * <p>Builds the {@link Pagination} control with the requested visual style.
   * The page factory returns an empty {@link Region} because the actual data
   * is rendered by the {@link TableView} above the control; the control only
   * acts as the page selector.</p>
   *
   * @param bulletStyle whether to use bullet (dot) indicators instead of numbers
   * @return the configured pagination control
   */
  private Pagination buildPagination(boolean bulletStyle) {
    Pagination p = new Pagination(1, 0);
    if (bulletStyle) {
      p.getStyleClass().add(Pagination.STYLE_CLASS_BULLET);
      p.setMaxPageIndicatorCount(10);
    } else {
      p.setMaxPageIndicatorCount(5);
    }
    p.setPageFactory(pageIndex -> {
      showPage(pageIndex);
      return new Region();
    });
    return p;
  }

  /**
   * <p>Overrides the table's default sort policy to sort the full master list
   * (not just the visible page slice) and then re-render the current page,
   * so column-header clicks affect all rows across pages.</p>
   */
  private void wireSortPolicy() {
    table.setSortPolicy(t -> {
      Comparator<T> cmp = t.getComparator();
      if (cmp != null) FXCollections.sort(master, cmp);
      showPage(pagination.getCurrentPageIndex());
      return true;
    });
  }
}
