package edu.ntnu.idi.idatt.millions.view.components.portfolio;

import edu.ntnu.idi.idatt.millions.model.Holding;
import edu.ntnu.idi.idatt.millions.view.util.TableStyleUtils;
import edu.ntnu.idi.idatt.millions.view.util.ViewFormatter;
import edu.ntnu.idi.idatt.millions.view.widgets.PaginatedTable;
import edu.ntnu.idi.idatt.millions.view.widgets.ViewWidgets;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

/**
 * <p>
 * Section panel that displays the player's aggregated stock holdings as a
 * paginated {@link TableView}. Each row represents one distinct stock symbol
 * and shows total quantity, weighted buy price, current price, total value,
 * unrealised gain/loss, and a Quick Sell button.
 * </p>
 *
 * <p>
 * Column headers and numeric values switch to compact short-form when the
 * table width drops below {@link TableStyleUtils#COMPACT_THRESHOLD} so the
 * panel remains readable at narrow viewport widths.
 * </p>
 */
public class HoldingsPanel extends VBox {

  private static final int PAGE_SIZE = 5;

  private final PaginatedTable<Holding> holdings;
  private BiConsumer<String, BigDecimal> onSell;

  /**
   * <p>Constructs the holdings panel and builds its layout as a section card.</p>
   */
  public HoldingsPanel() {
    getStyleClass().add("stat-card");
    setPadding(new Insets(24, 14, 24, 24));

    holdings = new PaginatedTable<>(buildHoldingsTable(), PAGE_SIZE, true);

    getChildren().addAll(
        ViewWidgets.sectionHeading("Holdings"),
        ViewWidgets.spacer(16),
        holdings.table(),
        holdings.pagination()
    );
  }

  // Table builders

  @SuppressWarnings({"deprecation", "unchecked"})
  private TableView<Holding> buildHoldingsTable() {
    TableView<Holding> table = new TableView<>(FXCollections.observableArrayList());
    table.getStyleClass().add("stock-table");
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    table.setPlaceholder(
        new Label("You don't own any shares yet. Head to Trading to get started."));

    TableColumn<Holding, Holding>     stockCol = TableStyleUtils.stockIconColumn(
        h -> h.stock().getSymbol(), h -> h.stock().getCompany());
    TableColumn<Holding, BigDecimal>  qtyCol   = buildHoldingValueCol("Quantity",
        Holding::totalQuantity,      ViewFormatter::quantity,   ViewFormatter::quantity, table);
    TableColumn<Holding, BigDecimal>  buyCol   = buildHoldingValueCol("Buy Price",
        Holding::weightedBuyPrice,   ViewFormatter::price,      ViewFormatter::wholePrice, table);
    TableColumn<Holding, BigDecimal>  currCol  = buildHoldingValueCol("Current Price",
        h -> h.stock().getSalesPrice(), ViewFormatter::price,   ViewFormatter::wholePrice, table);
    TableColumn<Holding, BigDecimal>  valueCol = buildHoldingValueCol("Value",
        Holding::getTotalValue,      ViewFormatter::price,      ViewFormatter::wholePrice, table);

    for (TableColumn<?, ?> col : List.of(qtyCol, buyCol, currCol, valueCol)) {
      col.setMinWidth(70);
      col.setPrefWidth(100);
    }

    TableColumn<Holding, BigDecimal> gainCol = buildGainLossColumn(table);
    gainCol.setPrefWidth(120);
    TableColumn<Holding, Holding> sellCol = buildQuickSellColumn(table);

    table.getColumns().addAll(stockCol, qtyCol, buyCol, currCol, valueCol, gainCol, sellCol);

    TableStyleUtils.bindCompactText(qtyCol,  table, "QTY", "Quantity");
    TableStyleUtils.bindCompactText(buyCol,  table, "BP",  "Buy Price");
    TableStyleUtils.bindCompactText(currCol, table, "CP",  "Current Price");
    TableStyleUtils.bindCompactText(gainCol, table, "G/L", "Gain / Loss");

    TableStyleUtils.applyFixedPageHeight(table, PAGE_SIZE);
    TableStyleUtils.wireSortHeaderHighlight(table);
    wireCompactRefresh(table);
    return table;
  }

  /**
   * <p>Builds a sortable numeric column that extracts a {@link BigDecimal} value
   * from each {@link Holding} and switches between a long and compact formatter
   * at the {@link TableStyleUtils#COMPACT_THRESHOLD} width breakpoint.</p>
   *
   * @param title          the column header text
   * @param extractor      function mapping a row to the displayed value
   * @param longFormatter  formatter used when the table has room
   * @param shortFormatter formatter used in compact mode
   * @param table          the parent table, used to read the current width
   * @return the configured column
   */
  private TableColumn<Holding, BigDecimal> buildHoldingValueCol(
      String title,
      Function<Holding, BigDecimal> extractor,
      Function<BigDecimal, String> longFormatter,
      Function<BigDecimal, String> shortFormatter,
      TableView<Holding> table) {
    TableColumn<Holding, BigDecimal> col = new TableColumn<>(title);
    col.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(extractor.apply(d.getValue())));
    col.setCellFactory(c -> new TableCell<>() {
      @Override
      protected void updateItem(BigDecimal item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
        } else {
          setText((table.getWidth() < TableStyleUtils.COMPACT_THRESHOLD ? shortFormatter : longFormatter).apply(item));
        }
      }
    });
    return col;
  }

  /**
   * <p>Builds the Gain / Loss column with profit/loss colouring via CSS classes.
   * Uses compact {@code ±$1234} format when the table is narrow.</p>
   *
   * @param table the parent table, used to read the current width
   * @return the configured gain/loss column
   */
  private TableColumn<Holding, BigDecimal> buildGainLossColumn(TableView<Holding> table) {
    TableColumn<Holding, BigDecimal> col = new TableColumn<>("Gain / Loss");
    col.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getGainOrLoss()));
    col.setCellFactory(c -> new TableCell<>() {
      @Override
      protected void updateItem(BigDecimal item, boolean empty) {
        super.updateItem(item, empty);
        getStyleClass().removeAll("table-data-cell-profit", "table-data-cell-loss");
        if (empty || item == null) {
          setText(null);
        } else {
          setText(table.getWidth() < TableStyleUtils.COMPACT_THRESHOLD
              ? ViewFormatter.signedWholePrice(item)
              : ViewFormatter.signedPrice(item));
          getStyleClass().add(item.signum() >= 0 ? "table-data-cell-profit" : "table-data-cell-loss");
        }
      }
    });
    col.setMinWidth(80);
    return col;
  }

  /**
   * <p>Builds the Quick Sell action column. Clicking the button invokes the
   * registered {@link #onSell} handler with the row's symbol and total quantity.
   * The button label shortens to "QS" in compact mode.</p>
   *
   * @param table the parent table, used to bind the button label to its width
   * @return the configured actions column
   */
  private TableColumn<Holding, Holding> buildQuickSellColumn(TableView<Holding> table) {
    TableColumn<Holding, Holding> col = new TableColumn<>("");
    col.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue()));
    col.setCellFactory(c -> new TableCell<>() {
      private final Button btn = new Button();
      {
        btn.getStyleClass().add("select-btn");
        btn.setMinWidth(0);
        btn.textProperty().bind(
            Bindings.when(table.widthProperty().lessThan(TableStyleUtils.COMPACT_THRESHOLD))
                .then("QS").otherwise("Quick Sell"));
      }

      @Override
      protected void updateItem(Holding item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setGraphic(null);
        } else {
          btn.setOnAction(e -> {
            if (onSell != null) onSell.accept(item.stock().getSymbol(), item.totalQuantity());
          });
          setGraphic(btn);
        }
      }
    });
    col.setSortable(false);
    col.setMinWidth(70);
    col.setPrefWidth(120);
    col.maxWidthProperty().bind(
        Bindings.when(table.widthProperty().lessThan(TableStyleUtils.COMPACT_THRESHOLD))
            .then(90).otherwise(150));
    return col;
  }

  // Helpers

  /**
   * <p>Adds a width listener that calls {@link TableView#refresh()} whenever
   * the table crosses the {@link TableStyleUtils#COMPACT_THRESHOLD}, so cells re-render
   * with the appropriate formatter.</p>
   */
  private static void wireCompactRefresh(TableView<?> table) {
    table.widthProperty().addListener((obs, oldW, newW) -> {
      boolean wasCompact = oldW.doubleValue() < TableStyleUtils.COMPACT_THRESHOLD;
      boolean isCompact  = newW.doubleValue() < TableStyleUtils.COMPACT_THRESHOLD;
      if (wasCompact != isCompact) table.refresh();
    });
  }

  // Public API

  /**
   * <p>Replaces the holdings table items with the given list.</p>
   *
   * @param items the holdings to display
   */
  public void setItems(List<Holding> items) {
    holdings.setItems(items);
  }

  /**
   * <p>Registers a handler invoked when the user clicks Quick Sell.
   * Receives the stock symbol and the total quantity to sell.</p>
   *
   * @param handler the consumer receiving {@code (symbol, totalQuantity)}
   */
  public void setOnSell(BiConsumer<String, BigDecimal> handler) {
    this.onSell = handler;
  }
}
