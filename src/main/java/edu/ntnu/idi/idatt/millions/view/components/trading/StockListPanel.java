package edu.ntnu.idi.idatt.millions.view.components.trading;

import edu.ntnu.idi.idatt.millions.model.ReadOnlyStock;
import edu.ntnu.idi.idatt.millions.view.util.TableStyleUtils;
import edu.ntnu.idi.idatt.millions.view.util.ViewFormatter;
import edu.ntnu.idi.idatt.millions.view.widgets.PaginatedTable;
import edu.ntnu.idi.idatt.millions.view.widgets.ViewWidgets;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * <p>
 * Panel that displays the paginated stock list with a search field and
 * a sortable {@link TableView}. Row selection fires two callbacks:
 * {@code onStockActivated} (always, for immediate UI update) and
 * {@code onStockSelected} (only for user-initiated selections, for the
 * controller).
 * </p>
 *
 * <p>
 * Programmatic selection via {@link #selectStock(ReadOnlyStock)} and
 * {@link #setHighlightedStock(String)} use an internal suppression flag
 * so the controller callback is not fired spuriously, while still
 * triggering the visual update.
 * </p>
 */
public class StockListPanel extends VBox {

  private static final int PAGE_SIZE = 8;
  private static final double ROW_HEIGHT = 58;
  private static final double TABLE_HEADER_HEIGHT = 40;

  private final TextField searchField;
  private final TableView<ReadOnlyStock> stockTable;
  private final PaginatedTable<ReadOnlyStock> stockList;

  private String pendingHighlight = null;
  private boolean suppressSelectionEvent = false;

  private Consumer<ReadOnlyStock> onStockActivated;
  private Consumer<String> onStockSelected;

  /**
   * <p>Constructs the stock list panel and builds its layout.</p>
   */
  public StockListPanel() {
    super(8);
    setPadding(Insets.EMPTY);
    setMaxWidth(Double.MAX_VALUE);
    VBox.setVgrow(this, Priority.ALWAYS);

    VBox header = ViewWidgets.pageHeader("Trading", "Browse and buy stocks on the exchange");

    searchField = new TextField();
    searchField.setPromptText("Search by name or symbol");
    searchField.getStyleClass().add("search-field");
    searchField.setMaxWidth(Double.MAX_VALUE);

    stockTable = buildStockTable();
    stockList = new PaginatedTable<>(stockTable, PAGE_SIZE, false);
    stockList.setOnPageRendered(this::applyPendingHighlightSafely);

    VBox tableCard = ViewWidgets.sectionCard(stockTable, stockList.pagination());
    tableCard.setPadding(new Insets(24, 14, 24, 24));

    getChildren().addAll(header, searchField, tableCard);
  }

  // Internal helpers

  private void applyPendingHighlightSafely() {
    suppressSelectionEvent = true;
    try {
      applyPendingHighlight();
    } finally {
      suppressSelectionEvent = false;
    }
  }

  private void applyPendingHighlight() {
    if (pendingHighlight == null) {
      stockTable.getSelectionModel().clearSelection();
      return;
    }
    for (ReadOnlyStock s : stockTable.getItems()) {
      if (s.getSymbol().equals(pendingHighlight)) {
        stockTable.getSelectionModel().select(s);
        return;
      }
    }
    stockTable.getSelectionModel().clearSelection();
  }

  // Table builders

  private TableView<ReadOnlyStock> buildStockTable() {
    TableView<ReadOnlyStock> table = new TableView<>();
    table.getStyleClass().add("stock-table");
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    table.setPlaceholder(new Label("No stocks match your search."));
    TableStyleUtils.applyFixedPageHeight(table, PAGE_SIZE, ROW_HEIGHT, TABLE_HEADER_HEIGHT);

    table.getColumns().add(buildStockColumn());
    table.getColumns().add(buildPriceLikeColumn("Price", ReadOnlyStock::getSalesPrice));
    table.getColumns().add(buildChangeColumn());
    table.getColumns().add(buildPriceLikeColumn("High", ReadOnlyStock::getHighestPrice));
    table.getColumns().add(buildPriceLikeColumn("Low", ReadOnlyStock::getLowestPrice));

    table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, sel) -> {
      if (sel == null) return;
      pendingHighlight = sel.getSymbol();
      if (onStockActivated != null) onStockActivated.accept(sel);
      if (!suppressSelectionEvent && onStockSelected != null) {
        onStockSelected.accept(sel.getSymbol());
      }
    });

    TableStyleUtils.wireSortHeaderHighlight(table);
    table.comparatorProperty().addListener((obs, old, neu) -> TableStyleUtils.applyHeaderStyles(table));

    return table;
  }

  /**
   * <p>Builds the "Stock" column that renders an icon together with the symbol
   * and company name. Sorting is by symbol, case-insensitive.</p>
   *
   * @return the configured stock column
   */
  private TableColumn<ReadOnlyStock, ReadOnlyStock> buildStockColumn() {
    TableColumn<ReadOnlyStock, ReadOnlyStock> col = new TableColumn<>("Stock");
    col.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
    col.setCellFactory(c -> new TableCell<>() {
      @Override
      protected void updateItem(ReadOnlyStock stock, boolean empty) {
        super.updateItem(stock, empty);
        if (empty || stock == null) {
          setGraphic(null);
          setText(null);
          return;
        }
        setGraphic(ViewWidgets.stockIconBlock(stock.getSymbol(), stock.getCompany()));
      }
    });
    col.setMinWidth(180);
    col.setPrefWidth(220);
    col.setComparator((a, b) -> a.getSymbol().compareToIgnoreCase(b.getSymbol()));
    return col;
  }

  /**
   * <p>Builds a numeric price-style column ("Price", "High", "Low") that
   * extracts a {@link BigDecimal} from each stock and renders it using
   * {@link ViewFormatter#price(BigDecimal)}.</p>
   *
   * @param title     the column header text
   * @param extractor function returning the value to display for a given stock
   * @return the configured column
   */
  private TableColumn<ReadOnlyStock, BigDecimal> buildPriceLikeColumn(
      String title, Function<ReadOnlyStock, BigDecimal> extractor) {
    TableColumn<ReadOnlyStock, BigDecimal> col = new TableColumn<>(title);
    col.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(extractor.apply(c.getValue())));
    col.setCellFactory(c -> new TableCell<>() {
      @Override
      protected void updateItem(BigDecimal value, boolean empty) {
        super.updateItem(value, empty);
        getStyleClass().remove("table-data-cell");
        if (empty || value == null) {
          setText(null);
          return;
        }
        setText(ViewFormatter.price(value));
        getStyleClass().add("table-data-cell");
      }
    });
    col.setMinWidth(70);
    return col;
  }

  /**
   * <p>Builds the "Change" column that displays the latest percentage change
   * with profit/loss colouring. Sortable by the underlying numeric change.</p>
   *
   * @return the configured change column
   */
  private TableColumn<ReadOnlyStock, ReadOnlyStock> buildChangeColumn() {
    TableColumn<ReadOnlyStock, ReadOnlyStock> col = new TableColumn<>("Change");
    col.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
    col.setCellFactory(c -> new TableCell<>() {
      @Override
      protected void updateItem(ReadOnlyStock stock, boolean empty) {
        super.updateItem(stock, empty);
        getStyleClass().removeAll("table-data-cell-profit", "table-data-cell-loss");
        if (empty || stock == null) {
          setText(null);
          return;
        }
        BigDecimal percent = stock.getLatestPercentChange();
        setText(ViewFormatter.percent(percent));
        getStyleClass().add(percent.signum() >= 0 ? "table-data-cell-profit" : "table-data-cell-loss");
      }
    });
    col.setMinWidth(80);
    col.setComparator((a, b) -> a.getLatestPercentChange().compareTo(b.getLatestPercentChange()));
    return col;
  }

  // Public API

  /**
   * <p>Replaces the master stock list with the given stocks and re-applies
   * the pending highlight so the previously-selected row remains visible.</p>
   *
   * @param stocks the stocks to display, never {@code null}
   */
  public void setStocks(List<? extends ReadOnlyStock> stocks) {
    stockList.setItems(stocks);
    if (pendingHighlight != null) {
      int page = stockList.findPageOf(s -> s.getSymbol().equals(pendingHighlight));
      if (page >= 0) stockList.goToPage(page);
    }
  }

  /**
   * <p>Programmatically selects the given stock in the table without firing
   * the {@code onStockSelected} callback from the listener. Fires it once
   * explicitly after selection so the controller stays in sync.</p>
   *
   * @param stock the stock to select; must exist in the current items list
   */
  public void selectStock(ReadOnlyStock stock) {
    pendingHighlight = stock.getSymbol();
    int targetPage = stockList.findPageOf(s -> s.getSymbol().equals(stock.getSymbol()));
    if (targetPage < 0) return;

    if (targetPage != stockList.currentPage()) {
      // goToPage fires applyPendingHighlightSafely via the page-rendered hook,
      // which selects the row under suppression.
      stockList.goToPage(targetPage);
    } else {
      suppressSelectionEvent = true;
      try {
        stockTable.getSelectionModel().clearSelection();
        stockTable.getSelectionModel().select(stock);
      } finally {
        suppressSelectionEvent = false;
      }
    }

    // Fire explicitly since the listener was suppressed.
    if (onStockSelected != null) onStockSelected.accept(stock.getSymbol());
  }

  /**
   * <p>Marks the given symbol as selected without firing the
   * {@code onStockSelected} callback. If the symbol is not yet in the
   * items list, the request is remembered and applied on the next
   * {@link #setStocks(List)} call.</p>
   *
   * @param symbol the symbol to highlight, or {@code null} to clear
   */
  public void setHighlightedStock(String symbol) {
    pendingHighlight = symbol;
    suppressSelectionEvent = true;
    try {
      applyPendingHighlight();
    } finally {
      suppressSelectionEvent = false;
    }
  }

  /**
   * <p>Returns the search field used for stock filtering.</p>
   *
   * @return the search text field
   */
  public TextField getSearchField() {
    return searchField;
  }

  /**
   * <p>Registers a handler that runs on every stock selection, including
   * programmatic ones. Use this to drive immediate UI updates (e.g. the
   * buy panel and chart).</p>
   *
   * @param handler the consumer that receives the selected stock
   */
  public void setOnStockActivated(Consumer<ReadOnlyStock> handler) {
    this.onStockActivated = handler;
  }

  /**
   * <p>Registers a handler that runs only on user-initiated stock selections,
   * receiving the selected symbol. Use this to notify the controller.</p>
   *
   * @param handler the consumer that receives the selected symbol
   */
  public void setOnStockSelected(Consumer<String> handler) {
    this.onStockSelected = handler;
  }
}
