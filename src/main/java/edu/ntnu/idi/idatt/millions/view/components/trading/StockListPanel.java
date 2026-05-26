package edu.ntnu.idi.idatt.millions.view.components.trading;

import edu.ntnu.idi.idatt.millions.model.market.ReadOnlyStock;
import edu.ntnu.idi.idatt.millions.view.components.PaginatedTable;
import edu.ntnu.idi.idatt.millions.view.components.ViewWidgets;
import edu.ntnu.idi.idatt.millions.view.util.TableStyleUtils;
import edu.ntnu.idi.idatt.millions.view.util.ViewFormatter;
import java.math.BigDecimal;
import java.util.Comparator;
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
 * Panel that displays the paginated stock list with a search field and a sortable {@link
 * TableView}. Row selection fires two callbacks: {@code onStockActivated} (always, for immediate UI
 * update) and {@code onStockSelected} (only for user-initiated selections, for the controller).
 *
 * <p>Programmatic selection via {@link #selectStock(ReadOnlyStock)} and {@link
 * #setHighlightedStock(String)} use an internal suppression flag so the controller callback is not
 * fired spuriously, while still triggering the visual update.
 */
public class StockListPanel extends VBox {

  private static final int PAGE_SIZE = 8;

  private final TextField searchField;
  private final TableView<ReadOnlyStock> stockTable;
  private final PaginatedTable<ReadOnlyStock> stockList;

  /** Threshold below which High/Low columns are hidden to prevent clipping. */
  private static final double COMPACT_THRESHOLD = 480;

  private TableColumn<ReadOnlyStock, BigDecimal> highCol;
  private TableColumn<ReadOnlyStock, BigDecimal> lowCol;

  private String pendingHighlight = null;
  private boolean suppressSelectionEvent = false;

  private Consumer<ReadOnlyStock> onStockActivated;
  private Consumer<String> onStockSelected;

  /** Constructs the stock list panel and builds its layout. */
  public StockListPanel() {
    super(8);
    setPadding(Insets.EMPTY);
    setMaxWidth(Double.MAX_VALUE);
    VBox.setVgrow(this, Priority.ALWAYS);

    final VBox header = ViewWidgets.pageHeader("Trading", "Browse and buy stocks on the exchange");

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
    TableStyleUtils.applyFixedPageHeight(table, PAGE_SIZE);

    TableColumn<ReadOnlyStock, ReadOnlyStock> stockCol =
        TableStyleUtils.stockIconColumn(ReadOnlyStock::getSymbol, ReadOnlyStock::getCompany);
    stockCol.setMinWidth(180);
    stockCol.setPrefWidth(220);
    highCol = buildPriceLikeColumn("High", ReadOnlyStock::getHighestPrice);
    lowCol = buildPriceLikeColumn("Low", ReadOnlyStock::getLowestPrice);

    table.getColumns().add(stockCol);
    table.getColumns().add(buildPriceLikeColumn("Price", ReadOnlyStock::getSalesPrice));
    table.getColumns().add(buildChangeColumn());
    table.getColumns().add(highCol);
    table.getColumns().add(lowCol);

    table
        .widthProperty()
        .addListener(
            (_, _, newW) -> {
              boolean compact = newW.doubleValue() < COMPACT_THRESHOLD;
              highCol.setVisible(!compact);
              lowCol.setVisible(!compact);
            });

    table
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (_, _, sel) -> {
              if (sel == null) {
                return;
              }
              pendingHighlight = sel.getSymbol();
              if (onStockActivated != null) {
                onStockActivated.accept(sel);
              }
              if (!suppressSelectionEvent && onStockSelected != null) {
                onStockSelected.accept(sel.getSymbol());
              }
            });

    TableStyleUtils.wireSortHeaderHighlight(table);
    table.comparatorProperty().addListener((_, _, _) -> TableStyleUtils.applyHeaderStyles(table));

    return table;
  }

  /**
   * Builds a numeric price-style column ("Price", "High", "Low") that extracts a {@link BigDecimal}
   * from each stock and renders it using {@link ViewFormatter#price(BigDecimal)}.
   *
   * @param title the column header text
   * @param extractor function returning the value to display for a given stock
   * @return the configured column
   */
  private TableColumn<ReadOnlyStock, BigDecimal> buildPriceLikeColumn(
      String title, Function<ReadOnlyStock, BigDecimal> extractor) {
    TableColumn<ReadOnlyStock, BigDecimal> col = new TableColumn<>(title);
    col.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(extractor.apply(c.getValue())));
    col.setCellFactory(
        _ ->
            new TableCell<>() {
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
   * Builds the "Change" column that displays the latest percentage change with profit/loss
   * colouring. Sortable by the underlying numeric change.
   *
   * @return the configured change column
   */
  private TableColumn<ReadOnlyStock, ReadOnlyStock> buildChangeColumn() {
    TableColumn<ReadOnlyStock, ReadOnlyStock> col = new TableColumn<>("Change");
    col.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
    col.setCellFactory(
        _ ->
            new TableCell<>() {
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
                getStyleClass()
                    .add(percent.signum() >= 0 ? "table-data-cell-profit" : "table-data-cell-loss");
              }
            });
    col.setMinWidth(80);
    col.setComparator(Comparator.comparing(ReadOnlyStock::getLatestPercentChange));
    return col;
  }

  // Public API

  /**
   * Replaces the master stock list with the given stocks and re-applies the pending highlight so
   * the previously-selected row remains visible.
   *
   * @param stocks the stocks to display, never {@code null}
   */
  public void setStocks(List<? extends ReadOnlyStock> stocks) {
    stockList.setItems(stocks);
    if (pendingHighlight != null) {
      int page = stockList.findPageOf(s -> s.getSymbol().equals(pendingHighlight));
      if (page >= 0) {
        stockList.goToPage(page);
      }
    }
  }

  /**
   * Programmatically selects the given stock in the table without firing the {@code
   * onStockSelected} callback from the listener. Fires it once explicitly after selection so the
   * controller stays in sync.
   *
   * @param stock the stock to select; must exist in the current items list
   */
  public void selectStock(ReadOnlyStock stock) {
    pendingHighlight = stock.getSymbol();
    int targetPage = stockList.findPageOf(s -> s.getSymbol().equals(stock.getSymbol()));
    if (targetPage < 0) {
      return;
    }

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
    if (onStockSelected != null) {
      onStockSelected.accept(stock.getSymbol());
    }
  }

  /**
   * Marks the given symbol as selected without firing the {@code onStockSelected} callback. If the
   * symbol is not yet in the items list, the request is remembered and applied on the next {@link
   * #setStocks(List)} call.
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
   * Returns the search field used for stock filtering.
   *
   * @return the search text field
   */
  public TextField getSearchField() {
    return searchField;
  }

  /**
   * Registers a handler that runs on every stock selection, including programmatic ones. Use this
   * to drive immediate UI updates (e.g. the buy panel and chart).
   *
   * @param handler the consumer that receives the selected stock
   */
  public void setOnStockActivated(Consumer<ReadOnlyStock> handler) {
    this.onStockActivated = handler;
  }

  /**
   * Registers a handler that runs only on user-initiated stock selections, receiving the selected
   * symbol. Use this to notify the controller.
   *
   * @param handler the consumer that receives the selected symbol
   */
  public void setOnStockSelected(Consumer<String> handler) {
    this.onStockSelected = handler;
  }
}
