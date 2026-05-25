package edu.ntnu.idi.idatt.millions.view.components.trading;

import edu.ntnu.idi.idatt.millions.model.market.ReadOnlyStock;
import edu.ntnu.idi.idatt.millions.view.util.ViewFormatter;
import edu.ntnu.idi.idatt.millions.view.components.ViewWidgets;
import edu.ntnu.idi.idatt.millions.view.pages.TradingView.Mode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * <p>
 * Buy/sell panel that lets the player enter a quantity or dollar amount
 * and submit a trade for the currently selected stock. Contains the
 * mode toggle (Buy / Sell), selected stock info, cash/owned summary,
 * input field with quantity↔amount toggle, quick-percentage buttons,
 * and an order-summary cost card.
 * </p>
 *
 * <p>
 * All interaction callbacks ({@link #setOnAction}, {@link #setOnInputChanged},
 * {@link #setOnModeChanged}, {@link #setOnPercentSelected}) are registered
 * by the controller via {@link edu.ntnu.idi.idatt.millions.view.pages.TradingView}.
 * </p>
 */
public class BuyPanel extends VBox {

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

  private Consumer<Mode> onAction;
  private Runnable onInputChanged;
  private Consumer<Mode> onModeChanged;
  private Consumer<BigDecimal> onPercentSelected;

  /**
   * <p>Constructs the buy panel and builds its initial layout.</p>
   */
  public BuyPanel() {
    super(16);
    getStyleClass().add("buy-panel");
    setMinWidth(220);
    setPrefWidth(300);
    setMaxWidth(300);
    setPadding(new Insets(24, 20, 24, 20));

    HBox modeToggle = buildModeToggle();

    panelTitle = new Label("Buy Stock");
    panelTitle.getStyleClass().add("section-heading");

    VBox stockInfoCard = buildSelectedStockInfo();
    HBox playerInfoCard = buildPlayerInfoCard();
    HBox inputHeader = buildInputHeader();

    inputSpinner = buildInputSpinner();

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

    getChildren().addAll(
        modeToggle,
        panelTitle,
        stockInfoCard,
        playerInfoCard,
        inputHeader,
        inputSpinner,
        percentageCard,
        derivedLabel,
        costCard,
        ViewWidgets.spacer(4),
        actionButton
    );

    VBox.setVgrow(costCard, Priority.NEVER);
  }

  // Layout builders

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

  private Spinner<Double> buildInputSpinner() {
    Spinner<Double> spinner = new Spinner<>();
    SpinnerValueFactory.DoubleSpinnerValueFactory factory =
        new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1_000_000_000.0, 1.0, 1.0);
    factory.setConverter(new javafx.util.StringConverter<>() {
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
    spinner.setValueFactory(factory);
    spinner.setEditable(true);
    spinner.getStyleClass().add("search-field");
    spinner.setMaxWidth(Double.MAX_VALUE);
    spinner.getEditor().textProperty().addListener((_, _, _) -> {
      if (onInputChanged != null) onInputChanged.run();
    });
    return spinner;
  }

  private HBox buildPercentageCard() {
    String[]     labels   = {"25%", "50%", "100%"};
    BigDecimal[] percents = {new BigDecimal("0.25"), new BigDecimal("0.50"), new BigDecimal("1.00")};

    HBox card = new HBox(5);
    card.setAlignment(Pos.CENTER);
    card.getStyleClass().add("stat-card");
    card.setPadding(new Insets(12, 16, 12, 16));

    for (int i = 0; i < labels.length; i++) {
      BigDecimal pct = percents[i];
      Button btn = new Button(labels[i]);
      btn.getStyleClass().add("mode-tab");
      btn.setMaxWidth(Double.MAX_VALUE);
      btn.setOnAction(e -> firePercent(pct));
      HBox.setHgrow(btn, Priority.ALWAYS);
      card.getChildren().add(btn);
    }

    return card;
  }

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

  private Label makeCostRow(String labelText, String value) {
    Label lbl = new Label(labelText + ":   " + value);
    lbl.getStyleClass().add("cost-row");
    lbl.setMaxWidth(Double.MAX_VALUE);
    lbl.setMinWidth(0);
    lbl.setTextOverrun(OverrunStyle.ELLIPSIS);
    return lbl;
  }

  // Internal logic

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

  private void setSpinnerValue(BigDecimal value) {
    SpinnerValueFactory.DoubleSpinnerValueFactory factory =
        (SpinnerValueFactory.DoubleSpinnerValueFactory) inputSpinner.getValueFactory();
    factory.setValue(value.doubleValue());
    String stripped = value.stripTrailingZeros().toPlainString();
    if (stripped.startsWith(".")) stripped = "0" + stripped;
    inputSpinner.getEditor().setText(stripped);
  }

  private void firePercent(BigDecimal percent) {
    if (onPercentSelected != null) onPercentSelected.accept(percent);
  }

  // Public API

  /**
   * <p>Updates the stock info card (symbol, company, price, change, high, low)
   * and enables the action button for the given stock.</p>
   *
   * @param stock the stock currently selected in the list
   */
  public void updateStockInfo(ReadOnlyStock stock) {
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
    actionButton.setDisable(false);
  }

  /**
   * <p>Switches the panel between Buy and Sell mode, updating tab styling,
   * panel title, action button text, and notifying the registered mode handler.</p>
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
   * <p>Updates the displayed order cost preview values.</p>
   *
   * @param gross      the estimated cost string
   * @param commission the commission string
   * @param total      the total cost string
   */
  public void setCostPreview(String gross, String commission, String total) {
    estimatedCostLabel.setText("Estimated Cost:   " + gross);
    commissionLabel.setText("Commission (0.5%):   " + commission);
    totalCostLabel.setText("Total:   " + total);
  }

  /**
   * <p>Returns the current input value as entered by the user. Returns
   * {@link BigDecimal#ZERO} if the field is empty or unparseable.</p>
   *
   * @return the parsed input value, never {@code null}
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
   * @return {@code true} if the user is entering a dollar amount
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
   * <p>Updates the "Stocks Owned" value in the player info card.
   * Pass {@code null} or a non-positive value to show the empty placeholder.</p>
   *
   * @param qty the owned quantity, or {@code null} to clear
   */
  public void setOwnedQuantity(BigDecimal qty) {
    ownedValueLabel.setText(
        (qty == null || qty.signum() <= 0) ? "–" : ViewFormatter.quantity(qty));
  }

  /**
   * <p>Updates the cash balance shown in the player info card.</p>
   *
   * @param cash the player's current cash balance
   */
  public void setCashBalance(BigDecimal cash) {
    cashBalanceLabel.setText(cash == null ? "$0.00" : ViewFormatter.price(cash));
  }

  /**
   * <p>Sets the small derived-value hint shown below the input field
   * (e.g. {@code "≈ 2.78 shares"} when in amount mode).</p>
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
   * <p>Sets the current sales price of the selected stock so the panel can
   * convert between quantity- and amount-input when the user toggles the mode.</p>
   *
   * @param price the current stock price, may be {@code null} when no stock is selected
   */
  public void setCurrentPrice(BigDecimal price) {
    this.currentPrice = price;
  }

  /**
   * <p>Fills the input with the given value, toggling input mode if needed
   * to match {@code asAmount}.</p>
   *
   * @param value    the value to set
   * @param asAmount {@code true} to interpret as a dollar amount, {@code false} for quantity
   */
  public void setInputAmount(BigDecimal value, boolean asAmount) {
    if (asAmount != amountMode) toggleInputMode();
    setSpinnerValue(value);
  }

  /**
   * <p>Registers a handler that runs when the user confirms a trade via the
   * action button. The handler receives the current panel mode.</p>
   *
   * @param handler the action handler
   */
  public void setOnAction(Consumer<Mode> handler) {
    this.onAction = handler;
  }

  /**
   * <p>Registers a handler that runs whenever the input field text or
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
   * <p>Registers a handler that runs when a percentage button (25/50/100%)
   * is clicked. The handler receives the selected percentage as a decimal.</p>
   *
   * @param handler the consumer that receives the selected percentage
   */
  public void setOnPercentSelected(Consumer<BigDecimal> handler) {
    this.onPercentSelected = handler;
  }
}
