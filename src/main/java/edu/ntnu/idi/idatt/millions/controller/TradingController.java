package edu.ntnu.idi.idatt.millions.controller;

import edu.ntnu.idi.idatt.millions.model.Exchange;
import edu.ntnu.idi.idatt.millions.model.Player;
import edu.ntnu.idi.idatt.millions.model.ReadOnlyStock;
import edu.ntnu.idi.idatt.millions.model.Share;
import edu.ntnu.idi.idatt.millions.model.transaction.CostPreviewCalculator;
import edu.ntnu.idi.idatt.millions.view.dialogs.TransactionDialog;
import edu.ntnu.idi.idatt.millions.view.util.ViewFormatter;
import edu.ntnu.idi.idatt.millions.view.pages.TradingView;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>Controller for the trading page.</p>
 * <p>Owns the wiring between the {@link TradingView} and the underlying
 * models: registers the view as an exchange observer, fires an initial push so
 * the chart and stock list populate on startup, and handles stock search,
 * live cost preview, and buy/sell order execution with quantity- or
 * dollar-amount input.</p>
 */
public class TradingController {

  private final TradingView view;
  private final Exchange exchange;
  private final Player player;

  private String selectedSymbol = null;
  private String lastBuySymbol = null;
  private List<? extends ReadOnlyStock> currentResults = List.of();

  /**
   * <p>Creates a trading controller, registers the view as an exchange
   * observer, and wires all view callbacks.</p>
   *
   * @param view     the trading view to control.
   * @param exchange the exchange model.
   * @param player   the active player.
   */
  public TradingController(TradingView view, Exchange exchange, Player player) {
    this.view = view;
    this.exchange = exchange;
    this.player = player;

    exchange.addObserver(view);

    view.setOnRefresh(this::onExchangeRefresh);
    view.setOnSelectStock(this::handleStockSelected);
    view.setOnInputChanged(this::updateCostPreview);
    view.setOnModeChanged(this::handleModeChanged);
    view.setOnAction(this::handleTrade);
    view.setOnPercentSelected(this::handlePercent);

    view.getSearchField().textProperty().addListener(
        (_, _, text) -> filterStocks(text)
    );

    view.onExchangeUpdated(exchange);
    updatePlayerInfo();
    selectDefault();
  }

  /**
   * <p>Reacts to an exchange update by re-filtering the stock list and
   * refreshing the chart for the currently selected stock.</p>
   */
  private void onExchangeRefresh() {
    filterStocks(view.getSearchField().getText());
    refreshChart();
  }

  /**
   * <p>Updates internal state and the view after the user selects a stock
   * from the list.</p>
   *
   * @param symbol the ticker symbol of the selected stock
   */
  private void handleStockSelected(String symbol) {
    selectedSymbol = symbol;
    lastBuySymbol  = symbol;
    ReadOnlyStock stock = exchange.getStock(symbol);
    view.getStockChart().setStockInfo(stock.getSymbol(), stock.getCompany());
    view.getStockChart().setData(stock.getHistoricalPrices(), exchange.getWeek());
    view.setCurrentPrice(stock.getSalesPrice());
    updatePlayerInfo();
    updateCostPreview();
  }

  /**
   * <p>Reacts to the user switching between Buy and Sell mode.</p>
   *
   * <p>When switching back to Buy, falls back to the last stock shown in the
   * panel so the user does not lose their place. Sell mode only re-selects a
   * stock the player actually owns.</p>
   *
   * @param mode the mode the panel switched to
   */
  private void handleModeChanged(TradingView.Mode mode) {
    String savedSymbol = selectedSymbol;
    selectedSymbol = null;
    view.setActionEnabled(false);

    String symbolToUse = savedSymbol != null ? savedSymbol
        : (mode == TradingView.Mode.BUY ? lastBuySymbol : null);

    boolean reselect = symbolToUse != null && (
        mode == TradingView.Mode.BUY
            || totalOwned(symbolToUse).signum() > 0
    );

    view.setHighlightedStock(reselect ? symbolToUse : null);
    filterStocks(view.getSearchField().getText());

    if (reselect) {
      selectedSymbol = symbolToUse;
      view.setCurrentPrice(exchange.getStock(symbolToUse).getSalesPrice());
      updateCostPreview();
    } else {
      view.setCostPreview("$0.00", "$0.00", "$0.00");
      view.setDerivedLabel("");
    }
    updatePlayerInfo();
  }

  /**
   * <p>Fills the input field with a value representing the given percentage of
   * the player's available capacity (cash for buy orders, owned shares for
   * sell orders). For buy orders the commission is subtracted up-front so the
   * resulting order fits within the cash budget.</p>
   *
   * @param percent the percentage to apply, as a decimal (e.g. {@code 0.25})
   */
  private void handlePercent(BigDecimal percent) {
    if (selectedSymbol == null) return;

    if (view.getMode() == TradingView.Mode.BUY) {
      BigDecimal cashPortion = player.getMoney().multiply(percent);
      BigDecimal divisor = BigDecimal.ONE.add(CostPreviewCalculator.COMMISSION_RATE);
      BigDecimal amount = cashPortion.divide(divisor, 3, RoundingMode.DOWN);
      view.setInputAmount(amount, true);
    } else {
      BigDecimal owned = totalOwned(selectedSymbol);
      BigDecimal quantity = owned.multiply(percent);
      view.setInputAmount(quantity, false);
    }
  }

  /**
   * <p>Computes the effective share quantity from the user's input,
   * converting from a dollar amount when the panel is in amount mode.</p>
   *
   * @param price the current sales price of the selected stock
   * @return the share quantity to trade, never null
   */
  private BigDecimal effectiveQuantity(BigDecimal price) {
    BigDecimal raw = view.getInputValue();
    if (raw.signum() <= 0) return BigDecimal.ZERO;
    if (view.isAmountMode()) {
      return raw.divide(price, 8, RoundingMode.HALF_UP);
    }
    return raw;
  }

  /**
   * <p>Recalculates and pushes the cost preview to the view based on the
   * currently selected stock, input value, and input mode. Delegates the
   * gross/commission/total math to {@link CostPreviewCalculator}.</p>
   */
  private void updateCostPreview() {
    if (selectedSymbol == null) return;

    ReadOnlyStock stock = exchange.getStock(selectedSymbol);
    BigDecimal price = stock.getSalesPrice();
    view.setCurrentPrice(price);
    BigDecimal qty = effectiveQuantity(price);

    CostPreviewCalculator preview = new CostPreviewCalculator(
        price, qty, view.getMode() == TradingView.Mode.BUY);

    view.setCostPreview(
        ViewFormatter.price(preview.getGross()),
        ViewFormatter.price(preview.getCommission()),
        ViewFormatter.price(preview.getTotal())
    );

    if (view.isAmountMode()) {
      view.setDerivedLabel("≈ " + ViewFormatter.quantity(qty) + " shares");
    } else {
      view.setDerivedLabel("≈ " + ViewFormatter.price(preview.getGross()));
    }

    boolean canProceed = qty.signum() > 0;
    if (canProceed) {
      if (view.getMode() == TradingView.Mode.BUY) {
        canProceed = preview.getTotal().compareTo(player.getMoney()) <= 0;
      } else {
        canProceed = qty.compareTo(totalOwned(selectedSymbol)) <= 0;
      }
    }
    view.setActionEnabled(canProceed);
  }

  /**
   * <p>Updates the "Owned: X" hint shown above the input field. Only visible
   * in Sell mode when a stock is selected.</p>
   */
  private void updateOwnedHint() {
    view.setOwnedQuantity(selectedSymbol == null ? null : totalOwned(selectedSymbol));
  }

  /**
   * <p>Refreshes the player info card: cash balance and owned quantity of
   * the currently selected stock.</p>
   */
  private void updatePlayerInfo() {
    view.setCashBalance(player.getMoney());
    updateOwnedHint();
  }

  /**
   * <p>Sums the player's owned quantity across all share lots for a symbol.</p>
   *
   * @param symbol the stock symbol
   * @return the total quantity owned
   */
  private BigDecimal totalOwned(String symbol) {
    return player.getPortfolio().getShares(symbol).stream()
        .map(Share::getQuantity)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * <p>Executes a buy or sell order based on the current panel mode.</p>
   *
   * <p>This is a template method: both modes share the same flow of
   * (1) reading the input quantity, (2) validating it, (3) executing through
   * {@link TransactionExecutor}, and (4) showing a confirmation dialog. Only
   * the transaction method, dialog, error title, and list-refresh side-effect
   * differ between Buy and Sell.</p>
   *
   * @param mode the panel mode at the time of click
   */
  private void handleTrade(TradingView.Mode mode) {
    if (selectedSymbol == null) return;

    final String symbol = selectedSymbol;
    final boolean isBuy = mode == TradingView.Mode.BUY;
    final String errorTitle = isBuy ? "Purchase failed" : "Sale failed";

    BigDecimal qty = effectiveQuantity(exchange.getStock(symbol).getSalesPrice());
    if (qty.signum() <= 0) {
      TransactionDialog.showError(errorTitle, "Quantity must be positive.");
      return;
    }

    TransactionExecutor.execute(
        errorTitle,
        () -> isBuy ? exchange.buy(symbol, qty, player)
                    : exchange.sell(symbol, qty, player),
        tx -> {
          if (isBuy) {
            TransactionDialog.showPurchaseConfirmation(tx, player.getMoney());
          } else {
            TransactionDialog.showSaleConfirmation(tx, player.getMoney());
            filterStocks(view.getSearchField().getText());
          }
          updatePlayerInfo();
        }
    );
  }

  /**
   * <p>Filters the stock list by the given search text and pushes the result
   * to the view. In Sell mode, the list is further filtered to only stocks
   * the player currently owns.</p>
   *
   * @param text the search term typed by the user
   */
  private void filterStocks(String text) {
    List<? extends ReadOnlyStock> base = text.isBlank()
        ? exchange.getStocks()
        : exchange.findStocks(text);

    if (view.getMode() == TradingView.Mode.SELL) {
      Set<String> owned = player.getPortfolio().getShares().stream()
          .map(s -> s.getStock().getSymbol())
          .collect(Collectors.toSet());
      currentResults = base.stream()
          .filter(s -> owned.contains(s.getSymbol()))
          .toList();
    } else {
      currentResults = base;
    }

    view.setStocks(currentResults);
  }

  /**
   * <p>Selects the first available stock by default so the chart and buy panel
   * are populated on startup without requiring user interaction. Called from
   * the constructor.</p>
   *
   * <p>The actual selection state is updated by {@link #handleStockSelected},
   * which is fired as a callback from {@code view.selectStock(...)}. This
   * method only performs the view-side calls that precede that callback.</p>
   */
  private void selectDefault() {
    List<? extends ReadOnlyStock> stocks = exchange.getStocks();
    if (stocks.isEmpty()) return;
    view.selectStock(stocks.get(0));
  }

  /**
   * <p>Focuses a stock by symbol: switches to Buy mode, populates the search
   * field with the symbol so the list is filtered, and selects the stock so
   * the chart and buy panel update.</p>
   *
   * <p>The actual selection state is updated by {@link #handleStockSelected},
   * which is fired as a callback from {@code view.selectStock(...)}.</p>
   *
   * @param symbol the ticker symbol to focus
   */
  public void focusStock(String symbol) {
    if (symbol == null || !exchange.hasStock(symbol)) return;
    view.setMode(TradingView.Mode.BUY);
    view.getSearchField().setText(symbol);
    view.setHighlightedStock(symbol);
    filterStocks(symbol);
    view.selectStock(exchange.getStock(symbol));
  }

  /**
   * <p>Refreshes the chart for the currently selected stock. Called whenever
   * the exchange notifies the view of an update, so a week-advance keeps the
   * chart in sync without explicit coordination from outside.</p>
   */
  private void refreshChart() {
    if (selectedSymbol == null) return;
    try {
      ReadOnlyStock stock = exchange.getStock(selectedSymbol);
      view.getStockChart().setStockInfo(stock.getSymbol(), stock.getCompany());
      view.getStockChart().setData(stock.getHistoricalPrices(), exchange.getWeek());
    } catch (IllegalArgumentException _) {
      // Expected: the previously-selected symbol is no longer in the exchange.
      // The list-refresh that runs alongside will drop the stale highlight.
    }
  }

}
