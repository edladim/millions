package edu.ntnu.idi.idatt.millions.view.pages;

import edu.ntnu.idi.idatt.millions.model.ReadOnlyExchange;
import edu.ntnu.idi.idatt.millions.model.ReadOnlyStock;
import edu.ntnu.idi.idatt.millions.observer.ExchangeObserver;
import edu.ntnu.idi.idatt.millions.view.ViewFormatter;
import edu.ntnu.idi.idatt.millions.view.components.StockChartComponent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Pagination;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * <p>
 * Trading page view that renders the stock listing, a chart preview, and a
 * buy/sell panel.
 * </p>
 *
 * <p>
 * The stock listing is presented as a {@link TableView} of {@link ReadOnlyStock}
 * rows with custom cell factories so columns render rich content (icon + name,
 * coloured percentage change). Row selection drives the buy panel and fires the
 * registered {@code onSelectStock} callback.
 * </p>
 */
public class TradingView extends BorderPane implements ExchangeObserver {

  /** Action mode of the trading panel. */
  public enum Mode { BUY, SELL }

  /** Number of rows shown per page in the stock list. */
  private static final int PAGE_SIZE = 8;

  /** Row height (px); kept in sync with {@code .stock-table .table-row-cell -fx-cell-size} in main.css. */
  private static final double ROW_HEIGHT = 58;

  /** Reserved height for the column header inside the table viewport. */
  private static final double TABLE_HEADER_HEIGHT = 40;

  private TextField searchField;
  private TableView<ReadOnlyStock> stockTable;
  private Pagination stockPagination;

  /**
   * Master list holding every stock currently visible after search/mode filter.
   * The {@link TableView} only ever displays the slice for the current page.
   */
  private final ObservableList<ReadOnlyStock> stockMaster = FXCollections.observableArrayList();

  private StockChartComponent stockChart;

  private Label buySymbolLabel;
  private Label buyCompanyLabel;
  private Label buyPriceLabel;
  private Label buyHighLabel;
  private Label buyLowLabel;
  private Label buyChangeLabel;
  private Button buyTab;
  private Button sellTab;
  private Label panelTitle;
  private Label cashBalanceLabel;
  private Label ownedValueLabel;
  private Label inputLabel;

  private Spinner<Double> inputSpinner;
  private Label derivedLabel;
  private BigDecimal currentPrice;
  private Label estimatedCostLabel;
  private Label commissionLabel;
  private Label totalCostLabel;
  private Button actionButton;

  private Mode currentMode = Mode.BUY;
  private boolean amountMode = false;

  /**
   * Symbol that should appear selected in the table once items contain it.
   * Used by {@link #setHighlightedStock(String)} so callers can mark the
   * intended selection independently of the items list; the highlight is
   * re-applied automatically the next time {@link #setStocks(List)} runs.
   */
  private String pendingHighlight = null;

  /**
   * When {@code true}, the table's selection listener will not invoke the
   * {@code onSelectStock} callback. Used to apply programmatic selection
   * (e.g. restoring a highlight after refilling the table) without firing
   * controller-facing events.
   */
  private boolean suppressSelectionEvent = false;

  private VBox buyPanelWrapper;

  private Consumer<Mode> onAction;
  private Runnable onInputChanged;
  private Consumer<Mode> onModeChanged;
  private Consumer<String> onSelectStock;
  private Consumer<BigDecimal> onPercentSelected;
  private Runnable onRefresh;

  /**
   * <p>Constructs the trading view and builds its initial layout.</p>
   */
  public TradingView() {
    getStyleClass().add("dashboard-view");

    buyPanelWrapper = buildBuyPanelWrapper();

    this.setCenter(buildCenterPanel());
    this.setRight(buyPanelWrapper);
  }

  /**
   * <p>Builds the center panel containing the chart and stock list.</p>
   *
   * @return the center panel container
   */
  private VBox buildCenterPanel() {
    VBox center = new VBox(5);
    center.setPadding(new Insets(24));
    center.setMaxWidth(Double.MAX_VALUE);
    VBox.setVgrow(center, Priority.ALWAYS);

    stockChart = new StockChartComponent("–", "No stock selected");
    stockChart.setChartHeight(250);
    stockChart.setPrefHeight(300);

    VBox stockPanel = buildStockListPanel();
    VBox.setVgrow(stockPanel, Priority.ALWAYS);

    center.getChildren().addAll(stockChart, buildDivider(), stockPanel);
    return center;
  }

  /**
   * <p>Builds the stock list panel with header, search, and table.</p>
   *
   * @return the stock list panel container
   */
  private VBox buildStockListPanel() {
    VBox panel = new VBox(8);
    panel.setPadding(Insets.EMPTY);
    panel.setMaxWidth(Double.MAX_VALUE);
    VBox.setVgrow(panel, Priority.ALWAYS);

    Label title = new Label("Trading");
    title.getStyleClass().add("page-title");
    Label subtitle = new Label("Browse and buy stocks on the exchange");
    subtitle.getStyleClass().add("page-subtitle");
    VBox header = new VBox(4, title, subtitle);

    searchField = new TextField();
    searchField.setPromptText("Search by name or symbol");
    searchField.getStyleClass().add("search-field");
    searchField.setMaxWidth(Double.MAX_VALUE);

    stockTable = buildStockTable();
    stockPagination = buildStockPagination();
    wireSortToMaster();

    VBox tableCard = new VBox(stockTable, stockPagination);
    tableCard.getStyleClass().add("stat-card");
    tableCard.setPadding(new Insets(24, 14, 24, 24));

    panel.getChildren().addAll(header, searchField, tableCard);
    return panel;
  }

  /**
   * <p>Builds the bullet-style {@link Pagination} control that drives the
   * visible page of the stock table. The page factory updates the table's
   * items to the slice of {@link #stockMaster} that corresponds to the
   * selected page and returns an empty {@link Region} since the data is
   * rendered by the table above the control.</p>
   *
   * @return the configured pagination control
   */
  private Pagination buildStockPagination() {
    Pagination p = new Pagination(1, 0);
    p.setMaxPageIndicatorCount(5);
    p.setPageFactory(pageIndex -> {
      showPage(pageIndex);
      return new Region();
    });
    return p;
  }

  /**
   * <p>Wires the table's sort policy so column-header clicks sort the full
   * {@link #stockMaster} list rather than only the visible page slice, then
   * re-render the current page with the sorted data.</p>
   */
  private void wireSortToMaster() {
    stockTable.setSortPolicy(t -> {
      java.util.Comparator<ReadOnlyStock> cmp = t.getComparator();
      if (cmp != null) FXCollections.sort(stockMaster, cmp);
      showPage(stockPagination.getCurrentPageIndex());
      return true;
    });
  }

  /**
   * <p>Updates the table to display the {@link #PAGE_SIZE}-sized slice of
   * {@link #stockMaster} that begins at {@code pageIndex * PAGE_SIZE}, then
   * re-applies the pending highlight in case the selected row is on this
   * page.</p>
   *
   * @param pageIndex zero-based page index
   */
  private void showPage(int pageIndex) {
    int from = Math.max(0, pageIndex * PAGE_SIZE);
    int to   = Math.min(from + PAGE_SIZE, stockMaster.size());
    suppressSelectionEvent = true;
    try {
      if (from >= stockMaster.size()) {
        stockTable.getItems().clear();
      } else {
        stockTable.getItems().setAll(stockMaster.subList(from, to));
      }
      applyPendingHighlight();
    } finally {
      suppressSelectionEvent = false;
    }
  }

  /**
   * <p>Recomputes the page count from {@link #stockMaster}, jumps the
   * pagination control to the page that contains the row matching
   * {@link #pendingHighlight} (if any), and hides the bullet bar entirely
   * when only one page exists.</p>
   */
  private void syncPagination() {
    int pageCount = Math.max(1, (int) Math.ceil(stockMaster.size() / (double) PAGE_SIZE));
    stockPagination.setPageCount(pageCount);

    int targetPage = 0;
    if (pendingHighlight != null) {
      for (int i = 0; i < stockMaster.size(); i++) {
        if (stockMaster.get(i).getSymbol().equals(pendingHighlight)) {
          targetPage = i / PAGE_SIZE;
          break;
        }
      }
    }
    if (targetPage >= pageCount) targetPage = 0;
    stockPagination.setCurrentPageIndex(targetPage);

    boolean show = pageCount > 1;
    stockPagination.setVisible(show);
    stockPagination.setManaged(show);
  }

  /**
   * <p>Builds the stock {@link TableView} with its columns and selection
   * listener. The listener drives the buy panel and fires the
   * {@code onSelectStock} callback whenever a row is selected.</p>
   *
   * @return the configured stock table
   */
  private TableView<ReadOnlyStock> buildStockTable() {
    TableView<ReadOnlyStock> table = new TableView<>();
    table.getStyleClass().add("stock-table");
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    table.setPlaceholder(new Label("No stocks match your search."));
    double height = PAGE_SIZE * ROW_HEIGHT + TABLE_HEADER_HEIGHT;
    table.setMinHeight(height);
    table.setPrefHeight(height);
    table.setMaxHeight(height);
    table.setFixedCellSize(ROW_HEIGHT);

    table.getColumns().add(buildStockColumn());
    table.getColumns().add(buildPriceLikeColumn("Price", ReadOnlyStock::getSalesPrice));
    table.getColumns().add(buildChangeColumn());
    table.getColumns().add(buildPriceLikeColumn("High", ReadOnlyStock::getHighestPrice));
    table.getColumns().add(buildPriceLikeColumn("Low", ReadOnlyStock::getLowestPrice));

    table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, sel) -> {
      if (sel == null) return;
      pendingHighlight = sel.getSymbol();
      updateBuyPanel(sel);
      if (suppressSelectionEvent) return;
      if (onSelectStock != null) onSelectStock.accept(sel.getSymbol());
    });

    table.getSortOrder().addListener((javafx.beans.Observable o) -> applyHeaderStyles(table));
    table.comparatorProperty().addListener((obs, old, neu) -> applyHeaderStyles(table));

    javafx.application.Platform.runLater(() -> applyHeaderStyles(table));

    return table;
  }

  /**
   * <p>Recolours the header label of the currently-sorted column so the active
   * sort indicator is visually clear. Other header alignment and padding is
   * handled by CSS, which already renders correctly on the initial paint;
   * this method only patches the colour in response to sort changes.</p>
   *
   * <p>Uses {@code Platform.runLater} so the lookup runs after the skin has
   * assigned header labels, and uses inline styles via {@code setStyle} so
   * the change overrides any class-based rule.</p>
   *
   * @param table the table whose header colour should reflect the current sort
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
        Circle icon = new Circle(18, Color.web("#6366f1"));
        Label letter = new Label(String.valueOf(stock.getSymbol().charAt(0)));
        letter.getStyleClass().add("mover-icon-letter");
        StackPane iconPane = new StackPane(icon, letter);

        Label name = new Label(stock.getSymbol());
        name.getStyleClass().add("mover-name");
        Label company = new Label(stock.getCompany());
        company.getStyleClass().add("mover-symbol");
        company.setTextOverrun(OverrunStyle.ELLIPSIS);
        company.setMinWidth(0);
        VBox nameBox = new VBox(2, name, company);

        HBox row = new HBox(10, iconPane, nameBox);
        row.setAlignment(Pos.CENTER_LEFT);
        setGraphic(row);
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
    col.setCellFactory(c -> {
      TableCell<ReadOnlyStock, BigDecimal> cell = new TableCell<>() {
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
      };
      return cell;
    });
    col.setMinWidth(70);
    return col;
  }

  /**
   * <p>Builds the "Change" column that displays the latest percentage change
   * with profit/loss colouring. Sortable by the underlying numeric change so
   * the user can order stocks by movement.</p>
   *
   * @return the configured change column
   */
  private TableColumn<ReadOnlyStock, ReadOnlyStock> buildChangeColumn() {
    TableColumn<ReadOnlyStock, ReadOnlyStock> col = new TableColumn<>("Change");
    col.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
    col.setCellFactory(c -> {
      TableCell<ReadOnlyStock, ReadOnlyStock> cell = new TableCell<>() {
        @Override
        protected void updateItem(ReadOnlyStock stock, boolean empty) {
          super.updateItem(stock, empty);
          getStyleClass().removeAll("table-data-cell-profit", "table-data-cell-loss");
          if (empty || stock == null) {
            setText(null);
            return;
          }
          BigDecimal percent = percentChange(stock);
          setText(ViewFormatter.percent(percent));
          getStyleClass().add(percent.signum() >= 0 ? "table-data-cell-profit" : "table-data-cell-loss");
        }
      };
      return cell;
    });
    col.setMinWidth(80);
    col.setComparator((a, b) -> percentChange(a).compareTo(percentChange(b)));
    return col;
  }

  /**
   * <p>Computes the latest price change as a percentage of the previous price.
   * Returns {@link BigDecimal#ZERO} when no previous price exists, so callers
   * can sort and render uniformly without null checks.</p>
   *
   * @param stock the stock whose latest change to express as a percentage
   * @return the latest percentage change
   */
  private static BigDecimal percentChange(ReadOnlyStock stock) {
    BigDecimal change = stock.getLatestPriceChange();
    BigDecimal previous = stock.getSalesPrice().subtract(change);
    if (previous.signum() == 0) return BigDecimal.ZERO;
    return change.divide(previous, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
  }

  /**
   * <p>Replaces the master stock list with the given stocks, recomputes the
   * pagination, and renders the current page. The pending highlight is
   * re-applied so the previously-selected row stays visually selected across
   * filter changes.</p>
   *
   * @param stocks the stocks to display, never {@code null}
   */
  public void setStocks(List<? extends ReadOnlyStock> stocks) {
    stockMaster.setAll(stocks);
    java.util.Comparator<ReadOnlyStock> cmp = stockTable.getComparator();
    if (cmp != null) FXCollections.sort(stockMaster, cmp);
    syncPagination();
    showPage(stockPagination.getCurrentPageIndex());
  }

  /**
   * <p>Selects the row matching {@link #pendingHighlight} in the table. If
   * the pending symbol is not in the current items, the selection is
   * cleared.</p>
   *
   * <p>Must be called with {@link #suppressSelectionEvent} already set to
   * {@code true}; the caller is responsible for the surrounding try/finally
   * so the listener does not fire while the highlight is restored.</p>
   */
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

  /**
   * <p>Wraps the buy panel in a transparent container with padding so it
   * appears as a floating card with margins on the top, right, and bottom
   * instead of being attached to the edges of the trading view.</p>
   *
   * @return the wrapper container holding the buy panel
   */
  private VBox buildBuyPanelWrapper() {
    VBox wrapper = new VBox(buildBuyPanel());
    wrapper.getStyleClass().add("buy-panel-wrapper");
    wrapper.setPadding(new Insets(24, 24, 24, 0));
    return wrapper;
  }

  /**
   * <p>Builds the buy/sell panel containing the mode tabs, stock info,
   * input field with quantity/amount toggle, and order summary.</p>
   *
   * @return the panel container
   */
  private VBox buildBuyPanel() {
    VBox panel = new VBox(16);
    panel.getStyleClass().add("buy-panel");
    panel.setMinWidth(220);
    panel.setPrefWidth(300);
    panel.setMaxWidth(300);
    panel.setPadding(new Insets(24, 20, 24, 20));

    HBox modeToggle = buildModeToggle();

    panelTitle = new Label("Buy Stock");
    panelTitle.getStyleClass().add("section-heading");

    VBox stockInfoCard = buildSelectedStockInfo();

    HBox playerInfoCard = buildPlayerInfoCard();

    HBox inputHeader = buildInputHeader();

    inputSpinner = new Spinner<>();
    SpinnerValueFactory.DoubleSpinnerValueFactory spinnerFactory =
        new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1_000_000_000.0, 1.0, 1.0);
    spinnerFactory.setConverter(new javafx.util.StringConverter<>() {
      @Override public String toString(Double v) {
        if (v == null || v == 0.0) return "";
        return (v == Math.floor(v) && !Double.isInfinite(v))
            ? String.valueOf(v.longValue()) : String.valueOf(v);
      }
      @Override public Double fromString(String s) {
        if (s == null || s.isBlank()) return 0.0;
        try { return Double.parseDouble(s.trim().replace(",", ".")); }
        catch (NumberFormatException e) { return 0.0; }
      }
    });
    inputSpinner.setValueFactory(spinnerFactory);
    inputSpinner.setEditable(true);
    inputSpinner.getStyleClass().add("search-field");
    inputSpinner.setMaxWidth(Double.MAX_VALUE);
    inputSpinner.getEditor().textProperty().addListener((_, _, _) -> {
      if (onInputChanged != null) onInputChanged.run();
    });

    derivedLabel = new Label();
    derivedLabel.getStyleClass().add("input-derived");
    derivedLabel.setMaxWidth(Double.MAX_VALUE);
    derivedLabel.setMinWidth(0);
    derivedLabel.setTextOverrun(OverrunStyle.ELLIPSIS);

    VBox costCard = buildCostCard();

    actionButton = new Button("Buy now");
    actionButton.getStyleClass().add("buy-btn");
    actionButton.setMaxWidth(Double.MAX_VALUE);
    actionButton.setDisable(true);
    actionButton.setOnAction(e -> {
      if (onAction != null) onAction.accept(currentMode);
    });

    HBox percentageCard = buildPercentageCard();

    panel.getChildren().addAll(
            modeToggle,
            panelTitle,
            stockInfoCard,
            playerInfoCard,
            inputHeader,
            inputSpinner,
            percentageCard,
            derivedLabel,
            costCard,
            buildSpacer(4),
            actionButton
    );

    VBox.setVgrow(costCard, Priority.NEVER);
    return panel;
  }

  /**
   * <p>Builds a row of quick-select buttons that fill the input with 25%,
   * 50%, or 100% of the player's available capacity (cash for buy, owned
   * shares for sell).</p>
   *
   * @return the percentage-card container
   */
  private HBox buildPercentageCard() {
    Button twentyFivePercentButton = new Button("25%");
    Button fiftyPercentButton = new Button("50%");
    Button hundredPercentButton = new Button("100%");

    for (Button btn : new Button[]{twentyFivePercentButton, fiftyPercentButton, hundredPercentButton}) {
      btn.getStyleClass().add("mode-tab");
      btn.setMaxWidth(Double.MAX_VALUE);
      HBox.setHgrow(btn, Priority.ALWAYS);
    }

    twentyFivePercentButton.setOnAction(e -> firePercent(new BigDecimal("0.25")));
    fiftyPercentButton.setOnAction(e -> firePercent(new BigDecimal("0.50")));
    hundredPercentButton.setOnAction(e -> firePercent(new BigDecimal("1.00")));

    HBox percentageCard = new HBox(5, twentyFivePercentButton, fiftyPercentButton, hundredPercentButton);
    percentageCard.setAlignment(Pos.CENTER);
    percentageCard.getStyleClass().add("stat-card");
    percentageCard.setPadding(new Insets(12, 16, 12, 16));
    return percentageCard;
  }

  /**
   * <p>Notifies the percent-selected handler, if any.</p>
   *
   * @param percent the selected percentage as a decimal (e.g. {@code 0.25})
   */
  private void firePercent(BigDecimal percent) {
    if (onPercentSelected != null) onPercentSelected.accept(percent);
  }

  /**
   * <p>Builds the compact player info card showing cash balance and how many
   * shares of the selected stock the player currently owns.</p>
   *
   * @return the info card container
   */
  private HBox buildPlayerInfoCard() {
    Label cashTitle = new Label("Cash Balance");
    cashTitle.getStyleClass().add("stat-card-title");
    cashBalanceLabel = new Label("$0.00");
    cashBalanceLabel.getStyleClass().add("player-info-value");
    VBox cashBox = new VBox(3, cashTitle, cashBalanceLabel);
    HBox.setHgrow(cashBox, Priority.ALWAYS);

    Label ownedTitle = new Label("Stocks Owned");
    ownedTitle.getStyleClass().add("stat-card-title");
    ownedValueLabel = new Label("–");
    ownedValueLabel.getStyleClass().add("player-info-value");
    VBox ownedBox = new VBox(3, ownedTitle, ownedValueLabel);

    HBox card = new HBox(0, cashBox, ownedBox);
    card.getStyleClass().add("stat-card");
    card.setPadding(new Insets(12, 16, 12, 16));
    card.setAlignment(Pos.CENTER_LEFT);
    return card;
  }

  /**
   * <p>Builds the Buy/Sell mode toggle row at the top of the panel.</p>
   *
   * @return the toggle container
   */
  private HBox buildModeToggle() {
    buyTab = new Button("Buy");
    buyTab.getStyleClass().addAll("mode-tab", "mode-tab-active");
    buyTab.setMaxWidth(Double.MAX_VALUE);
    buyTab.setOnAction(e -> setMode(Mode.BUY));

    sellTab = new Button("Sell");
    sellTab.getStyleClass().add("mode-tab");
    sellTab.setMaxWidth(Double.MAX_VALUE);
    sellTab.setOnAction(e -> setMode(Mode.SELL));

    HBox.setHgrow(buyTab, Priority.ALWAYS);
    HBox.setHgrow(sellTab, Priority.ALWAYS);

    HBox toggle = new HBox(8, buyTab, sellTab);
    toggle.setMaxWidth(Double.MAX_VALUE);
    return toggle;
  }

  /**
   * <p>Builds the input-mode header row containing the input label and the
   * swap button that toggles between quantity and dollar-amount input.</p>
   *
   * @return the header container
   */
  private HBox buildInputHeader() {
    inputLabel = new Label("Quantity");
    inputLabel.getStyleClass().add("input-label");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    FontIcon swapIcon = new FontIcon("fas-exchange-alt");
    swapIcon.setIconSize(12);
    Button swapBtn = new Button();
    swapBtn.setGraphic(swapIcon);
    swapBtn.getStyleClass().add("swap-btn");
    swapBtn.setOnAction(e -> toggleInputMode());

    HBox header = new HBox(inputLabel, spacer, swapBtn);
    header.setAlignment(Pos.CENTER_LEFT);
    return header;
  }

  /**
   * <p>Switches the panel between Buy and Sell mode, updating tab styling,
   * action button text, and notifying the registered mode handler.</p>
   *
   * @param mode the new mode
   */
  public void setMode(Mode mode) {
    if (currentMode == mode) return;
    currentMode = mode;

    buyTab.getStyleClass().removeAll("mode-tab-active");
    sellTab.getStyleClass().removeAll("mode-tab-active");
    (mode == Mode.BUY ? buyTab : sellTab).getStyleClass().add("mode-tab-active");

    panelTitle.setText(mode == Mode.BUY ? "Buy Stock" : "Sell Stock");
    actionButton.setText(mode == Mode.BUY ? "Buy now" : "Sell now");
    actionButton.getStyleClass().removeAll("action-btn-sell");
    if (mode == Mode.SELL) actionButton.getStyleClass().add("action-btn-sell");

    if (onModeChanged != null) onModeChanged.accept(mode);
  }

  /**
   * <p>Toggles the input between quantity entry and dollar-amount entry. If
   * the user has entered a value and a stock is selected, the existing value
   * is converted across the toggle (quantity ↔ amount) using the current
   * stock price so the user keeps their intended order size.</p>
   */
  private void toggleInputMode() {
    BigDecimal current = getInputValue();
    boolean wasAmount = amountMode;
    amountMode = !amountMode;
    inputLabel.setText(amountMode ? "Amount ($)" : "Quantity");

    if (current.signum() > 0 && currentPrice != null && currentPrice.signum() > 0) {
      BigDecimal converted = wasAmount
          ? current.divide(currentPrice, 4, RoundingMode.HALF_UP)
          : current.multiply(currentPrice).setScale(2, RoundingMode.HALF_UP);
      setSpinnerValue(converted);
    } else {
      setSpinnerValue(BigDecimal.ZERO);
    }

    if (onInputChanged != null) onInputChanged.run();
  }

  /**
   * <p>Pushes a new value into the spinner, updating both the underlying
   * value factory and the editor text so listeners observe the change.</p>
   *
   * @param value the value to set
   */
  private void setSpinnerValue(BigDecimal value) {
    String stripped = value.stripTrailingZeros().toPlainString();
    if (stripped.startsWith(".")) stripped = "0" + stripped;
    inputSpinner.getEditor().setText(stripped);
    SpinnerValueFactory.DoubleSpinnerValueFactory factory =
        (SpinnerValueFactory.DoubleSpinnerValueFactory) inputSpinner.getValueFactory();
    factory.setValue(value.doubleValue());
  }

  /**
   * <p>Builds the card that shows the selected stock information.</p>
   *
   * @return the stock info card container
   */
  private VBox buildSelectedStockInfo() {
    buySymbolLabel = new Label("–");
    buySymbolLabel.getStyleClass().add("buy-stock-symbol");
    buyCompanyLabel = new Label("No stock selected");
    buyCompanyLabel.getStyleClass().add("mover-symbol");
    buyPriceLabel = new Label("$0.00");
    buyPriceLabel.getStyleClass().add("buy-stock-price");
    buyChangeLabel = new Label("+0.00%");
    buyChangeLabel.getStyleClass().add("mover-change-positive");
    buyHighLabel = new Label("H: $0.00");
    buyHighLabel.getStyleClass().add("stat-card-title");
    buyLowLabel = new Label("L: $0.00");
    buyLowLabel.getStyleClass().add("stat-card-title");

    HBox highLow = new HBox(12, buyHighLabel, buyLowLabel);

    HBox priceRow = new HBox(10, buyPriceLabel, buyChangeLabel);
    priceRow.setAlignment(Pos.CENTER_LEFT);

    HBox symbolRow = new HBox(8, buySymbolLabel, buyCompanyLabel);
    symbolRow.setAlignment(Pos.BASELINE_LEFT);

    VBox card = new VBox(6, symbolRow, priceRow, highLow);
    card.getStyleClass().add("stat-card");
    card.setPadding(new Insets(16));
    return card;
  }

  /**
   * <p>Builds the order summary cost card.</p>
   *
   * @return the cost card container
   */
  private VBox buildCostCard() {
    Label title = new Label("Order Summary");
    title.getStyleClass().add("stat-card-title");

    estimatedCostLabel = makeCostRow("Estimated Cost", "$0.00");
    commissionLabel = makeCostRow("Commission (0.5%)", "$0.00");

    Region divider = new Region();
    divider.getStyleClass().add("divider");
    divider.setPrefHeight(1);
    divider.setMaxWidth(Double.MAX_VALUE);

    totalCostLabel = makeCostRow("Total", "$0.00");
    totalCostLabel.getStyleClass().add("cost-row-total");

    VBox card = new VBox(10, title, estimatedCostLabel, commissionLabel, divider, totalCostLabel);
    card.getStyleClass().add("stat-card");
    card.setPadding(new Insets(16));
    return card;
  }

  /**
   * <p>Creates a cost row label with text and value separated by spacing.</p>
   *
   * @param labelText the label text
   * @param value     the value text
   * @return the cost row label
   */
  private Label makeCostRow(String labelText, String value) {
    Label lbl = new Label(labelText + ":   " + value);
    lbl.getStyleClass().add("cost-row");
    lbl.setMaxWidth(Double.MAX_VALUE);
    lbl.setMinWidth(0);
    lbl.setTextOverrun(OverrunStyle.ELLIPSIS);
    return lbl;
  }

  /**
   * <p>Renders the given stock into the buy panel header (symbol, company,
   * price, change, high, low), updates the chart's stock info, and enables
   * the action button.</p>
   *
   * @param stock the stock currently selected in the table
   */
  private void updateBuyPanel(ReadOnlyStock stock) {
    BigDecimal change = stock.getLatestPriceChange();
    boolean isPositive = change.compareTo(BigDecimal.ZERO) >= 0;

    buySymbolLabel.setText(stock.getSymbol());
    buyCompanyLabel.setText(stock.getCompany());
    buyPriceLabel.setText(ViewFormatter.price(stock.getSalesPrice()));
    buyChangeLabel.setText(ViewFormatter.changeArrowPercent(change, stock.getSalesPrice()));
    buyChangeLabel.getStyleClass().removeAll("mover-change-positive", "mover-change-negative");
    buyChangeLabel.getStyleClass().add(isPositive ? "mover-change-positive" : "mover-change-negative");
    buyHighLabel.setText("H: " + ViewFormatter.price(stock.getHighestPrice()));
    buyLowLabel.setText("L: " + ViewFormatter.price(stock.getLowestPrice()));

    stockChart.setStockInfo(stock.getSymbol(), stock.getCompany());
    actionButton.setDisable(false);
  }

  /**
   * <p>Programmatically selects the given stock in the table. Triggers the
   * selection listener, which updates the buy panel and fires the registered
   * {@code onSelectStock} callback.</p>
   *
   * @param stock the stock to select; it must already exist in the items list
   */
  public void selectStock(ReadOnlyStock stock) {
    pendingHighlight = stock.getSymbol();
    int masterIndex = -1;
    for (int i = 0; i < stockMaster.size(); i++) {
      if (stockMaster.get(i).getSymbol().equals(stock.getSymbol())) {
        masterIndex = i;
        break;
      }
    }
    if (masterIndex < 0) return;

    int targetPage = masterIndex / PAGE_SIZE;
    if (targetPage != stockPagination.getCurrentPageIndex()) {
      // setCurrentPageIndex fires the page factory, which calls showPage with
      // the selection suppressed so the listener does not invoke the callback.
      stockPagination.setCurrentPageIndex(targetPage);
    } else {
      suppressSelectionEvent = true;
      try {
        stockTable.getSelectionModel().clearSelection();
        stockTable.getSelectionModel().select(stock);
      } finally {
        suppressSelectionEvent = false;
      }
    }

    // Selection was made under suppression so the listener never invoked the
    // callback. Fire it once here to keep the controller in sync.
    if (onSelectStock != null) onSelectStock.accept(stock.getSymbol());
  }

  /**
   * <p>Updates the displayed order cost preview values.</p>
   *
   * @param gross      the estimated cost
   * @param commission the commission value
   * @param total      the total cost
   */
  public void setCostPreview(String gross, String commission, String total) {
    estimatedCostLabel.setText("Estimated Cost:   " + gross);
    commissionLabel.setText("Commission (0.5%):   " + commission);
    totalCostLabel.setText("Total:   " + total);
  }

  /**
   * <p>Returns the current input value as entered by the user. Returns
   * {@link BigDecimal#ZERO} if the field is empty or unparseable so callers
   * can compute previews without throwing.</p>
   *
   * @return the parsed input value
   */
  public BigDecimal getInputValue() {
    String text = inputSpinner.getEditor().getText();
    if (text == null || text.isBlank()) return BigDecimal.ZERO;
    try {
      return new BigDecimal(text.trim().replace(",", "."));
    } catch (NumberFormatException e) {
      return BigDecimal.ZERO;
    }
  }

  /**
   * <p>Returns whether the input field is in dollar-amount mode.</p>
   *
   * @return {@code true} if the user is entering a dollar amount,
   *         {@code false} for quantity (shares)
   */
  public boolean isAmountMode() {
    return amountMode;
  }

  /**
   * <p>Returns the current panel mode.</p>
   *
   * @return {@link Mode#BUY} or {@link Mode#SELL}
   */
  public Mode getMode() {
    return currentMode;
  }

  /**
   * <p>Updates the "Owned" value shown in the player info card.
   * Pass a non-positive value to display the empty placeholder.</p>
   *
   * @param qty the owned quantity, or {@code null}/zero to clear
   */
  public void setOwnedQuantity(BigDecimal qty) {
    if (ownedValueLabel == null) return;
    ownedValueLabel.setText(
        (qty == null || qty.signum() <= 0) ? "–" : ViewFormatter.quantity(qty));
  }

  /**
   * <p>Updates the cash balance shown in the player info card.</p>
   *
   * @param cash the player's current cash balance
   */
  public void setCashBalance(BigDecimal cash) {
    if (cashBalanceLabel == null) return;
    cashBalanceLabel.setText(cash == null ? "$0.00" : ViewFormatter.price(cash));
  }

  /**
   * <p>Selects the row matching the given symbol without firing the
   * {@code onSelectStock} callback. If the symbol does not yet exist in the
   * current items, the request is remembered and applied during the next
   * {@link #setStocks(List)} call.</p>
   *
   * @param symbol the symbol to mark as selected, or {@code null} to clear
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
   * <p>Sets the small derived-value hint shown below the input field
   * (e.g. "≈ 2.78 shares" when in amount mode).</p>
   *
   * @param text the hint text, or empty string to clear
   */
  public void setDerivedLabel(String text) {
    derivedLabel.setText(text == null ? "" : text);
  }

  /**
   * <p>Enables or disables the action (Buy/Sell) button.</p>
   *
   * @param enabled {@code true} to enable
   */
  public void setActionEnabled(boolean enabled) {
    actionButton.setDisable(!enabled);
  }

  /**
   * <p>Clears the input field without triggering side effects beyond the
   * input-change listener.</p>
   */
  public void clearInput() {
    setSpinnerValue(BigDecimal.ZERO);
    inputSpinner.getEditor().clear();
    derivedLabel.setText("");
  }

  /**
   * <p>Sets the current sales price of the selected stock so the view can
   * convert between quantity- and amount-input when the user toggles the
   * input mode.</p>
   *
   * @param price the current stock price, may be {@code null} when no stock
   *              is selected
   */
  public void setCurrentPrice(BigDecimal price) {
    this.currentPrice = price;
  }

  /**
   * <p>Returns the search field used for stock filtering.</p>
   *
   * @return the search field
   */
  public TextField getSearchField() {
    return searchField;
  }

  /**
   * <p>Registers a handler that runs when the user confirms a buy or sell
   * via the action button. The handler receives the current panel mode.</p>
   *
   * @param handler the action handler
   */
  public void setOnAction(Consumer<Mode> handler) {
    this.onAction = handler;
  }

  /**
   * <p>Registers a handler that runs whenever the input field text or the
   * input mode (quantity/amount) changes.</p>
   *
   * @param handler the handler to run on change
   */
  public void setOnInputChanged(Runnable handler) {
    this.onInputChanged = handler;
  }

  /**
   * <p>Registers a handler that runs when the user toggles between Buy and
   * Sell mode.</p>
   *
   * @param handler the handler accepting the new mode
   */
  public void setOnModeChanged(Consumer<Mode> handler) {
    this.onModeChanged = handler;
  }

  /**
   * <p>Registers a handler that runs when a stock is selected, receiving its symbol.</p>
   *
   * @param handler the handler accepting the selected symbol
   */
  public void setOnSelectStock(Consumer<String> handler) {
    this.onSelectStock = handler;
  }

  /**
   * <p>Registers a handler that runs when the exchange updates, so the controller
   * can rebuild the stock list respecting any active search filter.</p>
   *
   * @param handler the handler to run on exchange update
   */
  public void setOnRefresh(Runnable handler) {
    this.onRefresh = handler;
  }

  /**
   * <p>Registers a handler that runs when a percentage button (25/50/100%)
   * is clicked. The handler receives the selected percentage as a decimal.</p>
   *
   * @param handler the consumer that receives the selected percentage
   */
  public void setOnPercentSelected(Consumer<BigDecimal> handler) {
    this.onPercentSelected = handler;
  }

  /**
   * <p>Fills the input with the given value, toggling between quantity- and
   * amount-mode if needed to match {@code asAmount}.</p>
   *
   * @param value    the value to set in the input
   * @param asAmount {@code true} to interpret the value as a dollar amount,
   *                 {@code false} to interpret it as a share quantity
   */
  public void setInputAmount(BigDecimal value, boolean asAmount) {
    if (asAmount != amountMode) toggleInputMode();
    setSpinnerValue(value);
  }

  @Override
  public void onExchangeUpdated(ReadOnlyExchange exchange) {
    if (onRefresh != null) onRefresh.run();
  }

  /**
   * <p>Returns the chart component used by the trading view.</p>
   *
   * @return the chart component
   */
  public StockChartComponent getStockChart() {
    return stockChart;
  }

  /**
   * <p>Creates a vertical spacer region with a fixed height.</p>
   *
   * @param h the spacer height in pixels
   * @return the spacer region
   */
  private Region buildSpacer(double h) {
    Region r = new Region();
    r.setPrefHeight(h);
    return r;
  }

  /**
   * <p>Creates a divider line for section separation.</p>
   *
   * @return the divider region
   */
  private Region buildDivider() {
    Region d = new Region();
    d.getStyleClass().add("divider");
    d.setPrefHeight(1);
    d.setMaxWidth(Double.MAX_VALUE);
    return d;
  }
}
