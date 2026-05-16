package edu.ntnu.idi.idatt.millions.view.pages;

import edu.ntnu.idi.idatt.millions.model.PlayerStatus;
import edu.ntnu.idi.idatt.millions.model.ReadOnlyPlayer;
import edu.ntnu.idi.idatt.millions.model.ReadOnlyPortfolio;
import edu.ntnu.idi.idatt.millions.model.Share;
import edu.ntnu.idi.idatt.millions.model.transaction.Purchase;
import edu.ntnu.idi.idatt.millions.model.transaction.Transaction;
import edu.ntnu.idi.idatt.millions.observer.PlayerObserver;
import edu.ntnu.idi.idatt.millions.observer.PortfolioObserver;
import edu.ntnu.idi.idatt.millions.view.ViewFormatter;
import edu.ntnu.idi.idatt.millions.view.components.StockChartComponent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;


/**
 * <p>
 * Portfolio page view that renders balances, holdings, and a transaction history.
 * </p>
 *
 * <p>
 * Holdings are aggregated by symbol into a {@link TableView}; each row shows the
 * combined quantity, weighted average buy price, current price, total value, and
 * unrealised gain/loss together with a Quick Sell button.
 * Transaction history is rendered in a second {@link TableView} sorted most-recent
 * first by default.
 * </p>
 *
 * <p>
 * The view implements {@link PlayerObserver} and {@link PortfolioObserver} so it
 * refreshes automatically whenever the player's cash or portfolio changes.
 * </p>
 */
public class PortfolioView extends VBox implements PortfolioObserver, PlayerObserver {

  private static final String ZERO_PRICE  = ViewFormatter.price(BigDecimal.ZERO);
  private static final String VALUE_STYLE = "stat-card-value";

  /** Table width (in px) below which column headers and button labels switch to short form. */
  private static final double COMPACT_THRESHOLD = 750;

  /** Approximate column header height + bottom padding, used for dynamic table height. */
  private static final double TABLE_HEADER_HEIGHT = 40;

  /** Row height used by the holdings and transactions tables; must match {@code .stock-table .table-row-cell -fx-cell-size}. */
  private static final double ROW_HEIGHT = 58;

  /** Stat-card width (in px) below which monetary values render in compact form (no thousands separator, no decimals). */
  private static final double CARD_COMPACT_THRESHOLD = 170;

  /** Number of rows shown per page in the Holdings and Transaction History tables. */
  private static final int PAGE_SIZE = 5;

  private ReadOnlyPlayer player;
  private BiConsumer<String, BigDecimal> onSell;

  private final ObjectProperty<BigDecimal> netWorthValue       = new SimpleObjectProperty<>(BigDecimal.ZERO);
  private final ObjectProperty<BigDecimal> cashBalanceValue    = new SimpleObjectProperty<>(BigDecimal.ZERO);
  private final ObjectProperty<BigDecimal> portfolioValueValue = new SimpleObjectProperty<>(BigDecimal.ZERO);

  /**
   * <p>Master lists that hold all rows. The corresponding {@link TableView} only shows
   * the current page's slice via {@link #showPage(TableView, ObservableList, int)}.</p>
   */
  private final ObservableList<HoldingRow>  holdingsMaster = FXCollections.observableArrayList();
  private final ObservableList<Transaction> txMaster       = FXCollections.observableArrayList();

  private Label statusLabel;
  private TableView<HoldingRow> holdingsTable;
  private TableView<Transaction> txTable;
  private Pagination holdingsPagination;
  private Pagination txPagination;
  private StockChartComponent portfolioChart;

  // ── Inner data class ──────────────────────────────────────────────────────

  /**
   * <p>Aggregated view of all share lots that belong to the same stock symbol.
   * Pre-computed so cell factories only need a simple field access.</p>
   *
   * @param symbol           the stock ticker symbol
   * @param company          the full company name
   * @param totalQty         total quantity owned across all lots
   * @param weightedBuyPrice quantity-weighted average purchase price
   * @param currentPrice     latest sales price from the exchange
   * @param totalValue       {@code currentPrice × totalQty}
   * @param gainOrLoss       {@code totalValue − totalInvestment}
   */
  private record HoldingRow(
      String symbol,
      String company,
      BigDecimal totalQty,
      BigDecimal weightedBuyPrice,
      BigDecimal currentPrice,
      BigDecimal totalValue,
      BigDecimal gainOrLoss
  ) {}

  // ── Constructor ───────────────────────────────────────────────────────────

  /**
   * <p>Constructs the portfolio view and builds its initial layout.</p>
   */
  public PortfolioView() {
    getStyleClass().add("dashboard-view");
    setSpacing(24);
    setPadding(new Insets(20));

    getChildren().addAll(
        buildHeader(),
        buildPortfolioChart(),
        buildSummaryRow(),
        buildHoldingsSection(),
        buildTransactionHistorySection()
    );
  }

  // ── Layout builders ───────────────────────────────────────────────────────

  /**
   * <p>Builds the page header containing the title and subtitle.</p>
   *
   * @return the header container
   */
  private VBox buildHeader() {
    Label title = new Label("My Portfolio");
    title.getStyleClass().add("page-title");

    Label subtitle = new Label("Your current holdings and balances");
    subtitle.getStyleClass().add("page-subtitle");

    return new VBox(4, title, subtitle);
  }

  /**
   * <p>Builds the portfolio net-worth chart.</p>
   *
   * @return the chart component
   */
  private StockChartComponent buildPortfolioChart() {
    portfolioChart = new StockChartComponent("Portfolio value", "");
    portfolioChart.setYAxisLabel("Value ($)");
    portfolioChart.setChartHeight(240);
    portfolioChart.setPrefHeight(300);
    return portfolioChart;
  }

  /**
   * <p>Builds the summary row that shows key portfolio metrics as stat cards.</p>
   *
   * <p>Every card grows equally so they fill the full row width.</p>
   *
   * @return the summary row container
   */
  private HBox buildSummaryRow() {
    HBox row = new HBox(16);
    row.setMaxWidth(Double.MAX_VALUE);

    VBox netWorthCard    = buildSummaryCard("Net Worth",      ZERO_PRICE, VALUE_STYLE);
    VBox cashCard        = buildSummaryCard("Cash Balance",   ZERO_PRICE, VALUE_STYLE);
    VBox portfolioCard   = buildSummaryCard("Portfolio Value",ZERO_PRICE, VALUE_STYLE);
    VBox statusCard      = buildSummaryCard("Status",         "Novice",   "stat-card-value-status");

    bindResponsivePrice((Label) netWorthCard.getChildren().get(1), netWorthValue, netWorthCard);
    bindResponsivePrice((Label) cashCard.getChildren().get(1),    cashBalanceValue, cashCard);
    bindResponsivePrice((Label) portfolioCard.getChildren().get(1), portfolioValueValue, portfolioCard);
    statusLabel = (Label) statusCard.getChildren().get(1);

    for (VBox card : new VBox[]{netWorthCard, cashCard, portfolioCard, statusCard}) {
      HBox.setHgrow(card, Priority.ALWAYS);
      card.setMaxWidth(Double.MAX_VALUE);
    }

    row.getChildren().addAll(netWorthCard, cashCard, portfolioCard, statusCard);
    return row;
  }

  /**
   * <p>Binds a stat-card value label's text so it shows the full price
   * ({@code "$9,989.45"}) when the card has room and a compact whole-dollar
   * format ({@code "$9989"}) when the card width drops below
   * {@link #CARD_COMPACT_THRESHOLD}. The binding tracks both the value and
   * the card's width so it re-renders on data change and on resize.</p>
   *
   * @param label the label whose text to bind
   * @param value the monetary value property
   * @param card  the containing card whose width drives the compact toggle
   */
  private static void bindResponsivePrice(Label label, ObjectProperty<BigDecimal> value, VBox card) {
    label.textProperty().bind(Bindings.createStringBinding(() -> {
      BigDecimal v = value.get();
      if (v == null) return "";
      return card.getWidth() < CARD_COMPACT_THRESHOLD
          ? ViewFormatter.wholePrice(v)
          : ViewFormatter.price(v);
    }, value, card.widthProperty()));
  }

  /**
   * <p>Builds a single summary stat card with a title and a value label.</p>
   *
   * @param title           the card heading
   * @param value           the initial value text
   * @param valueStyleClass the CSS class applied to the value label
   * @return the card container
   */
  private VBox buildSummaryCard(String title, String value, String valueStyleClass) {
    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().add("stat-card-title");

    Label valueLabel = new Label(value);
    valueLabel.getStyleClass().add(valueStyleClass);

    VBox card = new VBox(12, titleLabel, valueLabel);
    card.getStyleClass().add("stat-card");
    card.setPadding(new Insets(10));
    card.setMaxWidth(Double.MAX_VALUE);
    return card;
  }

  /**
   * <p>Builds the holdings section containing the aggregated holdings
   * {@link TableView}.</p>
   *
   * @return the holdings section container
   */
  private VBox buildHoldingsSection() {
    holdingsTable = buildHoldingsTable();
    holdingsPagination = buildPagination(
        pageIndex -> showPage(holdingsTable, holdingsMaster, pageIndex));
    wireSortToMaster(holdingsTable, holdingsMaster, holdingsPagination);

    Label heading = new Label("Holdings");
    heading.getStyleClass().add("section-heading");

    VBox section = new VBox(0, heading, buildSpacer(16), holdingsTable, holdingsPagination);
    section.getStyleClass().add("stat-card");
    section.setPadding(new Insets(24, 14, 24, 24));
    return section;
  }

  /**
   * <p>Builds the transaction history section containing the
   * {@link TableView} sorted most-recent-first by default.</p>
   *
   * @return the transaction history section container
   */
  private VBox buildTransactionHistorySection() {
    txTable = buildTransactionTable();
    txPagination = buildPagination(
        pageIndex -> showPage(txTable, txMaster, pageIndex));
    wireSortToMaster(txTable, txMaster, txPagination);

    Label heading = new Label("Transaction History");
    heading.getStyleClass().add("section-heading");

    VBox section = new VBox(0, heading, buildSpacer(16), txTable, txPagination);
    section.getStyleClass().add("stat-card");
    section.setPadding(new Insets(24, 14, 24, 24));
    return section;
  }

  /**
   * <p>Builds a bullet-style {@link Pagination} control that delegates page
   * rendering to the provided callback. The page factory returns an empty
   * region because the actual data is rendered by the surrounding
   * {@link TableView}; this control only acts as the page selector.</p>
   *
   * @param onPage the callback invoked with the selected page index
   * @return the configured pagination control
   */
  private static Pagination buildPagination(java.util.function.IntConsumer onPage) {
    Pagination p = new Pagination(1, 0);
    p.getStyleClass().add(Pagination.STYLE_CLASS_BULLET);
    p.setMaxPageIndicatorCount(10);
    p.setPageFactory(pageIndex -> {
      onPage.accept(pageIndex);
      return new Region();
    });
    return p;
  }

  /**
   * <p>Wires the table's sort behaviour to operate on the master list rather
   * than only on the currently visible page slice. When the user clicks a
   * column header, the master list is re-sorted with the table's current
   * comparator and the current page is re-rendered.</p>
   *
   * @param table      the table whose sort policy to override
   * @param master     the full backing list
   * @param pagination the pagination control providing the current page index
   * @param <T>        the row type
   */
  private static <T> void wireSortToMaster(
      TableView<T> table, ObservableList<T> master, Pagination pagination) {
    table.setSortPolicy(t -> {
      java.util.Comparator<T> cmp = t.getComparator();
      if (cmp != null) FXCollections.sort(master, cmp);
      showPage(table, master, pagination.getCurrentPageIndex());
      return true;
    });
  }

  /**
   * <p>Updates a paginated table to display the slice of {@code master} that
   * corresponds to {@code pageIndex}, based on {@link #PAGE_SIZE}.</p>
   *
   * @param table     the table whose visible items to update
   * @param master    the full backing list
   * @param pageIndex the zero-based page index to show
   * @param <T>       the row type
   */
  private static <T> void showPage(TableView<T> table, ObservableList<T> master, int pageIndex) {
    int from = Math.max(0, pageIndex * PAGE_SIZE);
    int to   = Math.min(from + PAGE_SIZE, master.size());
    if (from >= master.size()) {
      table.getItems().clear();
    } else {
      table.getItems().setAll(master.subList(from, to));
    }
  }

  /**
   * <p>Recalculates the page count from the master list size, clamps the
   * current page index to the valid range, and toggles the pagination
   * control's visibility so it disappears entirely when only one page exists.</p>
   *
   * @param master     the full backing list
   * @param pagination the pagination control to update
   */
  private static void syncPagination(ObservableList<?> master, Pagination pagination) {
    int pageCount = Math.max(1, (int) Math.ceil(master.size() / (double) PAGE_SIZE));
    pagination.setPageCount(pageCount);
    if (pagination.getCurrentPageIndex() >= pageCount) {
      pagination.setCurrentPageIndex(0);
    }
    boolean show = pageCount > 1;
    pagination.setVisible(show);
    pagination.setManaged(show);
  }

  // ── Table builders ────────────────────────────────────────────────────────

  /**
   * <p>Constructs and configures the holdings {@link TableView}.
   * Columns: Stock, Quantity, Buy&nbsp;Price, Current&nbsp;Price, Value,
   * Gain&nbsp;/&nbsp;Loss, and a Quick&nbsp;Sell action column.</p>
   *
   * @return the configured holdings table
   */
  @SuppressWarnings("deprecation")
  private TableView<HoldingRow> buildHoldingsTable() {
    TableView<HoldingRow> table = new TableView<>(FXCollections.observableArrayList());
    table.getStyleClass().add("stock-table");
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    table.setPlaceholder(
        new Label("You don't own any shares yet. Head to Trading to get started."));

    TableColumn<HoldingRow, HoldingRow> stockCol = buildHoldingStockColumn();

    TableColumn<HoldingRow, BigDecimal> qtyCol =
        buildHoldingValueCol("Quantity",     HoldingRow::totalQty,
            ViewFormatter::quantity, ViewFormatter::quantity, table);
    TableColumn<HoldingRow, BigDecimal> buyCol =
        buildHoldingValueCol("Buy Price",    HoldingRow::weightedBuyPrice,
            ViewFormatter::price, ViewFormatter::wholePrice, table);
    TableColumn<HoldingRow, BigDecimal> currCol =
        buildHoldingValueCol("Current Price", HoldingRow::currentPrice,
            ViewFormatter::price, ViewFormatter::wholePrice, table);
    TableColumn<HoldingRow, BigDecimal> valueCol =
        buildHoldingValueCol("Value",        HoldingRow::totalValue,
            ViewFormatter::price, ViewFormatter::wholePrice, table);

    for (TableColumn<?, ?> col : List.of(qtyCol, buyCol, currCol, valueCol)) {
      col.setMinWidth(70);
      col.setPrefWidth(100);
    }

    TableColumn<HoldingRow, BigDecimal> gainCol  = buildGainLossColumn(table);
    gainCol.setPrefWidth(120);
    TableColumn<HoldingRow, HoldingRow> sellCol  = buildQuickSellColumn(table);

    table.getColumns().addAll(stockCol, qtyCol, buyCol, currCol, valueCol, gainCol, sellCol);

    bindCompactText(qtyCol,   table, "QTY",   "Quantity");
    bindCompactText(buyCol,   table, "BP",    "Buy Price");
    bindCompactText(currCol,  table, "CP",    "Current Price");
    bindCompactText(gainCol,  table, "G/L",   "Gain / Loss");

    applyFixedPageHeight(table);
    wireSortHeaderHighlight(table);
    wireCompactRefresh(table);
    return table;
  }

  /**
   * <p>Adds a width listener that calls {@link TableView#refresh()} whenever
   * the table crosses {@link #COMPACT_THRESHOLD}, so cells using the
   * compact/long formatter pair re-render with the appropriate format.</p>
   *
   * @param table the table to wire up
   */
  private static void wireCompactRefresh(TableView<?> table) {
    table.widthProperty().addListener((obs, oldW, newW) -> {
      boolean wasCompact = oldW.doubleValue() < COMPACT_THRESHOLD;
      boolean isCompact  = newW.doubleValue() < COMPACT_THRESHOLD;
      if (wasCompact != isCompact) table.refresh();
    });
  }

  /**
   * <p>Builds the Stock column for the holdings table.
   * Each cell shows a coloured circle icon, the ticker symbol in bold,
   * and the company name in muted text below it — the same style as the
   * TradingView stock list.</p>
   *
   * @return the configured stock column
   */
  private TableColumn<HoldingRow, HoldingRow> buildHoldingStockColumn() {
    TableColumn<HoldingRow, HoldingRow> col = new TableColumn<>("Stock");
    col.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue()));
    col.setCellFactory(c -> new TableCell<>() {
      @Override
      protected void updateItem(HoldingRow item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setGraphic(null);
          return;
        }
        Circle icon = new Circle(18, Color.web("#6366f1"));
        Label letter = new Label(String.valueOf(item.symbol().charAt(0)));
        letter.getStyleClass().add("mover-icon-letter");
        StackPane iconPane = new StackPane(icon, letter);

        Label name = new Label(item.symbol());
        name.getStyleClass().add("mover-name");
        Label company = new Label(item.company());
        company.getStyleClass().add("mover-symbol");
        company.setTextOverrun(OverrunStyle.ELLIPSIS);
        company.setMinWidth(0);

        HBox row = new HBox(10, iconPane, new VBox(2, name, company));
        row.setAlignment(Pos.CENTER_LEFT);
        setGraphic(row);
      }
    });
    col.setMinWidth(160);
    col.setPrefWidth(200);
    col.setComparator((a, b) -> a.symbol().compareToIgnoreCase(b.symbol()));
    return col;
  }

  /**
   * <p>Builds a sortable numeric column for the holdings table that extracts
   * a {@link BigDecimal} value from each {@link HoldingRow} and formats it
   * with the long formatter when the table is wide and the short formatter
   * when the table width drops below {@link #COMPACT_THRESHOLD}.</p>
   *
   * @param title          the column header text
   * @param extractor      function mapping a row to the displayed value
   * @param longFormatter  formatter used when the table has room
   * @param shortFormatter formatter used when the table is in compact mode
   * @param table          the parent table, used to read the current width
   * @return the configured column
   */
  private TableColumn<HoldingRow, BigDecimal> buildHoldingValueCol(
      String title,
      Function<HoldingRow, BigDecimal> extractor,
      Function<BigDecimal, String> longFormatter,
      Function<BigDecimal, String> shortFormatter,
      TableView<HoldingRow> table
  ) {
    TableColumn<HoldingRow, BigDecimal> col = new TableColumn<>(title);
    col.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(extractor.apply(d.getValue())));
    col.setCellFactory(c -> new TableCell<>() {
      @Override
      protected void updateItem(BigDecimal item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
        } else {
          boolean compact = table.getWidth() < COMPACT_THRESHOLD;
          setText((compact ? shortFormatter : longFormatter).apply(item));
        }
      }
    });
    return col;
  }

  /**
   * <p>Builds the Gain&nbsp;/&nbsp;Loss column. Cell text is coloured green
   * for positive values and red for negative ones via the
   * {@code table-data-cell-profit} / {@code table-data-cell-loss} CSS classes.
   * Uses the compact {@code ±$1234} format when the table is narrow and the
   * full {@code ±$1,234.56} format otherwise.</p>
   *
   * @param table the parent table, used to read the current width
   * @return the configured gain/loss column
   */
  private TableColumn<HoldingRow, BigDecimal> buildGainLossColumn(TableView<HoldingRow> table) {
    TableColumn<HoldingRow, BigDecimal> col = new TableColumn<>("Gain / Loss");
    col.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().gainOrLoss()));
    col.setCellFactory(c -> new TableCell<>() {
      @Override
      protected void updateItem(BigDecimal item, boolean empty) {
        super.updateItem(item, empty);
        getStyleClass().removeAll("table-data-cell-profit", "table-data-cell-loss");
        if (empty || item == null) {
          setText(null);
        } else {
          boolean compact = table.getWidth() < COMPACT_THRESHOLD;
          setText(compact ? ViewFormatter.signedWholePrice(item) : ViewFormatter.signedPrice(item));
          getStyleClass().add(item.signum() >= 0 ? "table-data-cell-profit" : "table-data-cell-loss");
        }
      }
    });
    col.setMinWidth(80);
    return col;
  }

  /**
   * <p>Builds the Quick Sell action column. Each cell renders a button that
   * invokes the registered {@link #onSell} handler with the row's symbol and
   * total quantity when clicked. The button label shortens to "QS" when the
   * table width drops below {@link #COMPACT_THRESHOLD}.</p>
   *
   * @param table the parent table, used to bind the button label to its width
   * @return the configured actions column
   */
  private TableColumn<HoldingRow, HoldingRow> buildQuickSellColumn(TableView<HoldingRow> table) {
    TableColumn<HoldingRow, HoldingRow> col = new TableColumn<>("");
    col.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue()));
    col.setCellFactory(c -> new TableCell<>() {
      private final Button btn = new Button();
      {
        btn.getStyleClass().add("select-btn");
        btn.setMinWidth(0);
        btn.textProperty().bind(
            Bindings.when(table.widthProperty().lessThan(COMPACT_THRESHOLD))
                .then("QS")
                .otherwise("Quick Sell"));
      }

      @Override
      protected void updateItem(HoldingRow item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setGraphic(null);
        } else {
          btn.setOnAction(e -> {
            if (onSell != null) onSell.accept(item.symbol(), item.totalQty());
          });
          setGraphic(btn);
        }
      }
    });
    col.setSortable(false);
    col.setMinWidth(70);
    col.setPrefWidth(120);
    col.maxWidthProperty().bind(
        Bindings.when(table.widthProperty().lessThan(COMPACT_THRESHOLD))
            .then(90)
            .otherwise(150));
    return col;
  }

  /**
   * <p>Binds a column's header text to the table's width so it switches between
   * a short and long label at the {@link #COMPACT_THRESHOLD} breakpoint.</p>
   *
   * @param col       the column whose header text should be responsive
   * @param table     the parent table, providing the width property
   * @param shortText the label shown when the table is narrow
   * @param longText  the label shown when the table is wide
   */
  private static void bindCompactText(
      TableColumn<?, ?> col, TableView<?> table, String shortText, String longText) {
    col.textProperty().bind(
        Bindings.when(table.widthProperty().lessThan(COMPACT_THRESHOLD))
            .then(shortText)
            .otherwise(longText));
  }

  /**
   * <p>Fixes the table's height to fit exactly {@link #PAGE_SIZE} rows plus
   * the column header, so the surrounding card does not jump when the user
   * navigates between pages with a partial last page.</p>
   *
   * @param table the table whose height should be locked
   */
  private static void applyFixedPageHeight(TableView<?> table) {
    table.setFixedCellSize(ROW_HEIGHT);
    double total = PAGE_SIZE * ROW_HEIGHT + TABLE_HEADER_HEIGHT;
    table.setMinHeight(total);
    table.setPrefHeight(total);
    table.setMaxHeight(total);
  }

  /**
   * <p>Highlights the header label of the currently sorted column in primary
   * purple. Mirrors the behaviour of the TradingView stock table.</p>
   *
   * <p>The lookup runs on the JavaFX application thread after the next layout
   * pass since the column header nodes are created by the table's skin and may
   * not exist when the listener fires. A listener on the sort order ensures
   * the highlight follows user-initiated sort changes.</p>
   *
   * @param table the table whose sort header should be highlighted
   */
  private static void wireSortHeaderHighlight(TableView<?> table) {
    table.getSortOrder().addListener((javafx.collections.ListChangeListener<TableColumn<?, ?>>)
        c -> applyHeaderStyles(table));
    javafx.application.Platform.runLater(() -> applyHeaderStyles(table));
  }

  /**
   * <p>Applies the purple highlight to the label of every column currently in
   * the table's sort order and clears it from all other columns.</p>
   *
   * @param table the table whose headers should be re-styled
   */
  private static void applyHeaderStyles(TableView<?> table) {
    javafx.application.Platform.runLater(() -> {
      java.util.Set<String> sortedTitles = new java.util.HashSet<>();
      for (TableColumn<?, ?> c : table.getSortOrder()) {
        sortedTitles.add(c.getText());
      }
      for (javafx.scene.Node header : table.lookupAll(".column-header")) {
        javafx.scene.Node labelNode = header.lookup(".label");
        if (!(labelNode instanceof Label lbl)) continue;
        String text = lbl.getText();
        if (text == null) continue;
        lbl.setStyle(sortedTitles.contains(text) ? "-fx-text-fill: #6366f1;" : "");
      }
    });
  }

  /**
   * <p>Constructs and configures the transaction history {@link TableView}.
   * Columns: Stock, Quantity, Price, Value, Type, Week. Rows are coloured
   * by transaction type via a row factory. The table sorts descending by
   * week on first render so the most recent transaction appears at the top.</p>
   *
   * @return the configured transaction table
   */
  @SuppressWarnings("deprecation")
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

    TableColumn<Transaction, Transaction> stockCol = buildTxStockColumn();
    TableColumn<Transaction, BigDecimal>  qtyCol   = buildTxBigDecimalCol(
        "Quantity", tx -> tx.getShare().getQuantity(), ViewFormatter::quantity);
    TableColumn<Transaction, BigDecimal>  priceCol = buildTxBigDecimalCol(
        "Price",    tx -> tx.getShare().getPurchasePrice(), ViewFormatter::price);
    TableColumn<Transaction, BigDecimal>  valueCol = buildTxBigDecimalCol(
        "Value",    tx -> tx.getCalculator().calculateGross(), ViewFormatter::price);

    for (TableColumn<?, ?> col : List.of(qtyCol, priceCol, valueCol)) {
      col.setMinWidth(70);
    }

    TableColumn<Transaction, Transaction> typeCol = buildTxTypeColumn();
    TableColumn<Transaction, Integer>     weekCol = buildTxWeekColumn();

    table.getColumns().addAll(stockCol, qtyCol, priceCol, valueCol, typeCol, weekCol);

    bindCompactText(qtyCol, table, "QTY", "Quantity");

    weekCol.setSortType(TableColumn.SortType.DESCENDING);
    table.getSortOrder().add(weekCol);

    applyFixedPageHeight(table);
    wireSortHeaderHighlight(table);
    return table;
  }

  /**
   * <p>Builds the Stock column for the transaction table. Renders a circle
   * icon, ticker symbol, and company name — identical in style to the
   * holdings stock column.</p>
   *
   * @return the configured stock column
   */
  private TableColumn<Transaction, Transaction> buildTxStockColumn() {
    TableColumn<Transaction, Transaction> col = new TableColumn<>("Stock");
    col.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue()));
    col.setCellFactory(c -> new TableCell<>() {
      @Override
      protected void updateItem(Transaction item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setGraphic(null);
          return;
        }
        String symbol  = item.getShare().getStock().getSymbol();
        String company = item.getShare().getStock().getCompany();

        Circle icon = new Circle(18, Color.web("#6366f1"));
        Label letter = new Label(String.valueOf(symbol.charAt(0)));
        letter.getStyleClass().add("mover-icon-letter");
        StackPane iconPane = new StackPane(icon, letter);

        Label name = new Label(symbol);
        name.getStyleClass().add("mover-name");
        Label comp = new Label(company);
        comp.getStyleClass().add("mover-symbol");
        comp.setTextOverrun(OverrunStyle.ELLIPSIS);
        comp.setMinWidth(0);

        HBox row = new HBox(10, iconPane, new VBox(2, name, comp));
        row.setAlignment(Pos.CENTER_LEFT);
        setGraphic(row);
      }
    });
    col.setMinWidth(160);
    col.setPrefWidth(200);
    col.setComparator((a, b) ->
        a.getShare().getStock().getSymbol().compareToIgnoreCase(
        b.getShare().getStock().getSymbol()));
    return col;
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
      Function<BigDecimal, String> formatter
  ) {
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
   * <p>Builds the Type column for the transaction table. The cell text is
   * "Buy" or "Sell" coloured via {@code tx-type-buy} / {@code tx-type-sell}
   * CSS classes.</p>
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
   * <p>Builds the Week column for the transaction table, displaying values
   * as "W&lt;n&gt;" (e.g. {@code W12}).</p>
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

  // ── Data refresh ──────────────────────────────────────────────────────────

  @Override
  public void onPortfolioUpdated(ReadOnlyPortfolio portfolio) {
    if (player != null) refreshPortfolioData(player);
  }

  @Override
  public void onPlayerUpdated(ReadOnlyPlayer player) {
    this.player = player;
    refreshPortfolioData(player);
  }

  /**
   * <p>Refreshes all displayed data from the provided player snapshot:
   * stat cards, chart, holdings table, and transaction history table.</p>
   *
   * @param player a read-only view of the player whose data should be displayed
   */
  private void refreshPortfolioData(ReadOnlyPlayer player) {
    ReadOnlyPortfolio portfolio = player.getPortfolio();

    netWorthValue.set(player.getNetWorth());
    cashBalanceValue.set(player.getMoney());
    portfolioValueValue.set(portfolio.getTotalValue());

    updateStatusLabel(player.getStatus());
    portfolioChart.setData(player.getHistoricalNetWorth());

    holdingsMaster.setAll(groupSharesBySymbol(portfolio.getShares()));
    syncPagination(holdingsMaster, holdingsPagination);
    holdingsTable.sort();
    showPage(holdingsTable, holdingsMaster, holdingsPagination.getCurrentPageIndex());

    List<Transaction> txs = new ArrayList<>(player.getTransactions());
    java.util.Collections.reverse(txs);
    txMaster.setAll(txs);
    syncPagination(txMaster, txPagination);
    txTable.sort();
    showPage(txTable, txMaster, txPagination.getCurrentPageIndex());
  }

  /**
   * <p>Groups a flat list of share lots by stock symbol and converts each
   * group into an aggregated {@link HoldingRow}.</p>
   *
   * @param shares all share lots in the portfolio
   * @return one {@link HoldingRow} per unique symbol, in insertion order
   */
  private List<HoldingRow> groupSharesBySymbol(List<Share> shares) {
    Map<String, List<Share>> grouped = new LinkedHashMap<>();
    for (Share s : shares) {
      grouped.computeIfAbsent(s.getStock().getSymbol(), k -> new ArrayList<>()).add(s);
    }
    return grouped.values().stream().map(this::toHoldingRow).toList();
  }

  /**
   * <p>Aggregates a list of same-symbol share lots into a single
   * {@link HoldingRow}. The buy price is the quantity-weighted average
   * across all lots.</p>
   *
   * @param lots share lots that all belong to the same stock; must not be empty
   * @return the aggregated holding row
   */
  private HoldingRow toHoldingRow(List<Share> lots) {
    var stock = lots.get(0).getStock();
    BigDecimal totalQty = lots.stream()
        .map(Share::getQuantity)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal totalInvestment = lots.stream()
        .map(Share::getTotalInvestment)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal weightedBuyPrice = totalQty.signum() == 0
        ? BigDecimal.ZERO
        : totalInvestment.divide(totalQty, 4, RoundingMode.HALF_UP);
    BigDecimal currentPrice = stock.getSalesPrice();
    BigDecimal totalValue   = currentPrice.multiply(totalQty);
    BigDecimal gainOrLoss   = totalValue.subtract(totalInvestment);
    return new HoldingRow(
        stock.getSymbol(), stock.getCompany(),
        totalQty, weightedBuyPrice, currentPrice, totalValue, gainOrLoss);
  }

  /**
   * <p>Updates the Status label text and colour class to reflect the
   * current {@link PlayerStatus}.</p>
   *
   * @param status the player's current status
   */
  private void updateStatusLabel(PlayerStatus status) {
    String raw = status.toString().replace("_", " ");
    statusLabel.setText(raw.charAt(0) + raw.substring(1).toLowerCase());

    statusLabel.getStyleClass().removeAll(
        "stat-card-value-status-worst",
        "stat-card-value-status-bad",
        "stat-card-value-status-average",
        "stat-card-value-status-investor",
        "stat-card-value-status-good",
        "stat-card-value-status-excellent"
    );
    String css = switch (status) {
      case BUY_HIGH_BJORN  -> "stat-card-value-status-worst";
      case MAX_MINUS       -> "stat-card-value-status-bad";
      case AVERAGE_JOE     -> "stat-card-value-status-average";
      case INVESTOR        -> "stat-card-value-status-investor";
      case RAY_DALIO       -> "stat-card-value-status-good";
      case BERNARD_MADOFF  -> "stat-card-value-status-excellent";
    };
    statusLabel.getStyleClass().add(css);
  }

  // ── Public API ────────────────────────────────────────────────────────────

  /**
   * <p>Registers a handler that is invoked when the user clicks a Quick Sell
   * button in the holdings table.</p>
   *
   * <p>The handler receives the stock symbol and the total quantity to sell
   * (summed across all lots for that symbol).</p>
   *
   * @param handler the consumer receiving {@code (symbol, totalQuantity)}
   */
  public void setOnSell(BiConsumer<String, BigDecimal> handler) {
    this.onSell = handler;
  }

  // ── Utility builders ─────────────────────────────────────────────────────

  /**
   * <p>Creates a vertical spacer with a fixed height.</p>
   *
   * @param h the height in pixels
   * @return the spacer region
   */
  private Region buildSpacer(double h) {
    Region r = new Region();
    r.setPrefHeight(h);
    return r;
  }

}
