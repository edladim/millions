package edu.ntnu.idi.idatt.millions.view.components.portfolio;

import edu.ntnu.idi.idatt.millions.model.transaction.Purchase;
import edu.ntnu.idi.idatt.millions.model.transaction.Transaction;
import edu.ntnu.idi.idatt.millions.view.util.TableStyleUtils;
import edu.ntnu.idi.idatt.millions.view.util.ViewFormatter;
import edu.ntnu.idi.idatt.millions.view.widgets.PaginatedTable;
import edu.ntnu.idi.idatt.millions.view.widgets.ViewWidgets;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

/**
 * <p>
 * Section panel that displays the player's transaction history as a paginated
 * {@link TableView}, sorted most-recent-first by default. Rows are colour-coded
 * by type: buy rows receive the {@code tx-row-buy} CSS class and sell rows
 * receive {@code tx-row-sell}.
 * </p>
 *
 * <p>
 * The Quantity column header shortens to "QTY" at narrow viewport widths
 * (below {@link TableStyleUtils#COMPACT_THRESHOLD}).
 * </p>
 */
public class TransactionHistoryPanel extends VBox {

  private static final int PAGE_SIZE = 5;

  private final PaginatedTable<Transaction> transactions;

  /**
   * <p>Constructs the transaction history panel and builds its layout as a
   * section card.</p>
   */
  public TransactionHistoryPanel() {
    getStyleClass().add("stat-card");
    setPadding(new Insets(24, 14, 24, 24));

    transactions = new PaginatedTable<>(buildTransactionTable(), PAGE_SIZE, true);

    getChildren().addAll(
        ViewWidgets.sectionHeading("Transaction History"),
        ViewWidgets.spacer(16),
        transactions.table(),
        transactions.pagination()
    );
  }

  // Table builders

  @SuppressWarnings({"deprecation", "unchecked"})
  private TableView<Transaction> buildTransactionTable() {
    TableView<Transaction> table = new TableView<>(FXCollections.observableArrayList());
    table.getStyleClass().add("stock-table");
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    table.setPlaceholder(
        new Label("No transactions yet. Buy or sell stocks to see your history."));

    table.setRowFactory(tv -> new TableRow<>() {
      @Override
      protected void updateItem(Transaction tx, boolean empty) {
        super.updateItem(tx, empty);
        getStyleClass().removeAll("tx-row-buy", "tx-row-sell");
        if (!empty && tx != null) {
          getStyleClass().add(tx instanceof Purchase ? "tx-row-buy" : "tx-row-sell");
        }
      }
    });

    TableColumn<Transaction, Transaction> stockCol = TableStyleUtils.stockIconColumn(
        tx -> tx.getShare().getStock().getSymbol(),
        tx -> tx.getShare().getStock().getCompany());
    TableColumn<Transaction, BigDecimal>  qtyCol   = buildTxBigDecimalCol(
        "Quantity", tx -> tx.getShare().getQuantity(),          ViewFormatter::quantity);
    TableColumn<Transaction, BigDecimal>  priceCol = buildTxBigDecimalCol(
        "Price",    tx -> tx.getShare().getPurchasePrice(),     ViewFormatter::price);
    TableColumn<Transaction, BigDecimal>  valueCol = buildTxBigDecimalCol(
        "Value",    tx -> tx.getCalculator().calculateGross(),  ViewFormatter::price);

    for (TableColumn<?, ?> col : List.of(qtyCol, priceCol, valueCol)) {
      col.setMinWidth(70);
    }

    TableColumn<Transaction, Transaction> typeCol = buildTxTypeColumn();
    TableColumn<Transaction, Integer>     weekCol = buildTxWeekColumn();

    table.getColumns().addAll(stockCol, qtyCol, priceCol, valueCol, typeCol, weekCol);

    TableStyleUtils.bindCompactText(qtyCol, table, "QTY", "Quantity");

    weekCol.setSortType(TableColumn.SortType.DESCENDING);
    table.getSortOrder().add(weekCol);

    TableStyleUtils.applyFixedPageHeight(table, PAGE_SIZE);
    TableStyleUtils.wireSortHeaderHighlight(table);
    return table;
  }

  /**
   * <p>Builds a sortable {@link BigDecimal} column for the transaction table.</p>
   *
   * @param title     the column header text
   * @param extractor function mapping a transaction to the displayed value
   * @param formatter function mapping the value to its display string
   * @return the configured column
   */
  private TableColumn<Transaction, BigDecimal> buildTxBigDecimalCol(
      String title,
      Function<Transaction, BigDecimal> extractor,
      Function<BigDecimal, String> formatter) {
    TableColumn<Transaction, BigDecimal> col = new TableColumn<>(title);
    col.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(extractor.apply(d.getValue())));
    col.setCellFactory(c -> new TableCell<>() {
      @Override
      protected void updateItem(BigDecimal item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : formatter.apply(item));
      }
    });
    return col;
  }

  /**
   * <p>Builds the Type column. Cell text is "Buy" or "Sell" coloured via
   * {@code tx-type-buy} / {@code tx-type-sell} CSS classes.</p>
   *
   * @return the configured type column
   */
  private TableColumn<Transaction, Transaction> buildTxTypeColumn() {
    TableColumn<Transaction, Transaction> col = new TableColumn<>("Type");
    col.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue()));
    col.setCellFactory(c -> new TableCell<>() {
      @Override
      protected void updateItem(Transaction item, boolean empty) {
        super.updateItem(item, empty);
        getStyleClass().removeAll("tx-type-buy", "tx-type-sell");
        if (empty || item == null) {
          setText(null);
        } else {
          boolean isBuy = item instanceof Purchase;
          setText(isBuy ? "Buy" : "Sell");
          getStyleClass().add(isBuy ? "tx-type-buy" : "tx-type-sell");
        }
      }
    });
    col.setSortable(false);
    col.setMinWidth(55);
    return col;
  }

  /**
   * <p>Builds the Week column, displaying values as {@code "W<n>"}
   * (e.g. {@code W12}).</p>
   *
   * @return the configured week column
   */
  private TableColumn<Transaction, Integer> buildTxWeekColumn() {
    TableColumn<Transaction, Integer> col = new TableColumn<>("Week");
    col.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getWeek()));
    col.setCellFactory(c -> new TableCell<>() {
      @Override
      protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : "W" + item);
      }
    });
    col.setMinWidth(55);
    return col;
  }

  // Public API

  /**
   * <p>Replaces the transaction history table items with the given list.</p>
   *
   * @param items the transactions to display
   */
  public void setItems(List<Transaction> items) {
    transactions.setItems(items);
  }
}
