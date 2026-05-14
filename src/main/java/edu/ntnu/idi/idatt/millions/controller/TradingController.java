package edu.ntnu.idi.idatt.millions.controller;

import edu.ntnu.idi.idatt.millions.model.Exchange;
import edu.ntnu.idi.idatt.millions.model.Player;
import edu.ntnu.idi.idatt.millions.model.ReadOnlyStock;
import edu.ntnu.idi.idatt.millions.model.Share;
import edu.ntnu.idi.idatt.millions.model.transaction.Transaction;
import edu.ntnu.idi.idatt.millions.view.TransactionDialog;
import edu.ntnu.idi.idatt.millions.view.ViewFormatter;
import edu.ntnu.idi.idatt.millions.view.pages.TradingView;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>Controller for the trading page.</p>
 * <p>Handles stock search, live cost preview, and buy/sell order execution
 * with quantity- or dollar-amount input.</p>
 */
public class TradingController {

  private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.005");
  private static final int PAGE_SIZE = 7;

  private final TradingView view;
  private final Exchange exchange;
  private final Player player;

  private String selectedSymbol = null;
  private String lastBuySymbol = null;
  private List<? extends ReadOnlyStock> currentResults = List.of();
  private int displayedCount = PAGE_SIZE;

  /**
   * <p>Creates a trading controller and wires all view callbacks.</p>
   *
   * @param view     the trading view to control.
   * @param exchange the exchange model.
   * @param player   the active player.
   */
  public TradingController(TradingView view, Exchange exchange, Player player) {
    this.view = view;
    this.exchange = exchange;
    this.player = player;

    view.setOnRefresh(() -> filterStocks(view.getSearchField().getText()));

    view.setOnLoadMore(() -> {
      displayedCount += PAGE_SIZE;
      renderStocks();
    });

    view.setOnSelectStock(symbol -> {
      selectedSymbol = symbol;
      lastBuySymbol  = symbol;
      ReadOnlyStock stock = exchange.getStock(symbol);
      view.getStockChart().setStockInfo(stock.getSymbol(), stock.getCompany());
      view.getStockChart().setData(stock.getHistoricalPrices(), exchange.getWeek());
      view.setCurrentPrice(exchange.getStock(symbol).getSalesPrice());
      updatePlayerInfo();
      updateCostPreview();
    });

    view.setOnInputChanged(this::updateCostPreview);

    view.setOnModeChanged(mode -> {
      String savedSymbol = selectedSymbol;
      selectedSymbol = null;
      view.setActionEnabled(false);

      // When switching back to Buy, fall back to the last stock shown in the panel
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
    });

    view.setOnAction(this::handleAction);

    view.getSearchField().textProperty().addListener(
        (_, _, text) -> filterStocks(text)
    );

    view.setOnPercentSelected(this::handlePercent);

    updatePlayerInfo();
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
   * currently selected stock, input value, and input mode.</p>
   */
  private void updateCostPreview() {
    if (selectedSymbol == null) return;

    ReadOnlyStock stock = exchange.getStock(selectedSymbol);
    BigDecimal price = stock.getSalesPrice();
    view.setCurrentPrice(price);
    BigDecimal qty = effectiveQuantity(price);
    BigDecimal gross = price.multiply(qty);
    BigDecimal commission = gross.multiply(COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);
    BigDecimal total = view.getMode() == TradingView.Mode.BUY
        ? gross.add(commission)
        : gross.subtract(commission);

    view.setCostPreview(
        ViewFormatter.price(gross),
        ViewFormatter.price(commission),
        ViewFormatter.price(total)
    );

    if (view.isAmountMode()) {
      view.setDerivedLabel("≈ " + ViewFormatter.quantity(qty) + " shares");
    } else {
      view.setDerivedLabel("≈ " + ViewFormatter.price(gross));
    }

    boolean canProceed = qty.signum() > 0;
    if (canProceed) {
      if (view.getMode() == TradingView.Mode.BUY) {
        canProceed = total.compareTo(player.getMoney()) <= 0;
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
   * <p>Dispatches the action button click to the right handler based on
   * the current panel mode.</p>
   *
   * @param mode the panel mode at the time of click
   */
  private void handleAction(TradingView.Mode mode) {
    if (selectedSymbol == null) return;
    if (mode == TradingView.Mode.BUY) {
      handleBuy(selectedSymbol);
    } else {
      handleSell(selectedSymbol);
    }
  }

  private void handlePercent(BigDecimal percent) {
    if (selectedSymbol == null) return;

    if (view.getMode() == TradingView.Mode.BUY) {
      BigDecimal cashPortion = player.getMoney().multiply(percent);
      BigDecimal divisor = BigDecimal.ONE.add(COMMISSION_RATE);
      BigDecimal amount = cashPortion.divide(divisor, 3, RoundingMode.DOWN);
      view.setInputAmount(amount, true);
    } else {
      BigDecimal owned = totalOwned(selectedSymbol);
      BigDecimal quantity = owned.multiply(percent);
      view.setInputAmount(quantity, false);
    }
  }

  /**
   * <p>Executes a buy order and shows a confirmation dialog with the full
   * cost breakdown.</p>
   *
   * @param symbol the stock symbol to buy.
   */
  private void handleBuy(String symbol) {
    try {
      ReadOnlyStock stock = exchange.getStock(symbol);
      BigDecimal qty = effectiveQuantity(stock.getSalesPrice());
      if (qty.signum() <= 0) {
        TransactionDialog.showError("Purchase failed", "Quantity must be positive.");
        return;
      }
      Transaction tx = exchange.buy(symbol, qty, player);
      TransactionDialog.showPurchaseConfirmation(tx, player.getMoney());
      updatePlayerInfo();
    } catch (IllegalStateException | IllegalArgumentException e) {
      TransactionDialog.showError("Purchase failed", e.getMessage());
    } catch (Exception e) {
      TransactionDialog.showError("Unexpected error",
          "Could not complete purchase: " + e.getMessage());
    }
  }

  /**
   * <p>Executes a sell order, drawing across the player's lots of the
   * selected stock, and shows a confirmation dialog.</p>
   *
   * @param symbol the stock symbol to sell.
   */
  private void handleSell(String symbol) {
    try {
      ReadOnlyStock stock = exchange.getStock(symbol);
      BigDecimal qty = effectiveQuantity(stock.getSalesPrice());
      if (qty.signum() <= 0) {
        TransactionDialog.showError("Sale failed", "Quantity must be positive.");
        return;
      }
      Transaction tx = exchange.sell(symbol, qty, player);
      TransactionDialog.showSaleConfirmation(tx, player.getMoney());
      filterStocks(view.getSearchField().getText());
      updatePlayerInfo();
    } catch (IllegalStateException | IllegalArgumentException e) {
      TransactionDialog.showError("Sale failed", e.getMessage());
    } catch (Exception e) {
      TransactionDialog.showError("Unexpected error",
          "Could not complete sale: " + e.getMessage());
    }
  }

  /**
   * <p>Filters the stock list by the given search text and resets pagination.
   * In Sell mode, the list is further filtered to only stocks the player
   * currently owns.</p>
   *
   * @param text the search term typed by the user.
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

    displayedCount = PAGE_SIZE;
    renderStocks();
  }

  /**
   * <p>Renders up to {@code displayedCount} rows from {@code currentResults}
   * and shows or hides the "Load more" label accordingly.</p>
   */
  private void renderStocks() {
    view.clearStocks();
    int toShow = Math.min(displayedCount, currentResults.size());
    for (int i = 0; i < toShow; i++) {
      view.addStockRow(currentResults.get(i));
    }
    view.setLoadMoreVisible(toShow < currentResults.size());
  }

  /**
   * <p>Selects the first available stock by default so the chart and buy panel
   * are populated on startup without requiring user interaction.</p>
   */
  public void selectDefault() {
    List<? extends ReadOnlyStock> stocks = exchange.getStocks();
    if (stocks.isEmpty()) return;
    ReadOnlyStock first = stocks.get(0);
    selectedSymbol = first.getSymbol();
    lastBuySymbol  = first.getSymbol();
    view.setHighlightedStock(first.getSymbol());
    view.setCurrentPrice(first.getSalesPrice());
    view.selectStock(first);
  }

  /**
   * <p>Focuses a stock by symbol: switches to Buy mode, populates the search
   * field with the symbol so the list is filtered, and selects the stock so
   * the chart and buy panel update.</p>
   *
   * @param symbol the ticker symbol to focus
   */
  public void focusStock(String symbol) {
    if (symbol == null || !exchange.hasStock(symbol)) return;
    view.setMode(TradingView.Mode.BUY);
    view.getSearchField().setText(symbol);
    selectedSymbol = symbol;
    lastBuySymbol  = symbol;
    view.setHighlightedStock(symbol);
    filterStocks(symbol);
    view.selectStock(exchange.getStock(symbol));
  }

  /**
   * <p>Refreshes the buy panel and chart for the currently selected stock.
   * Called after the exchange advances a week so that the displayed price,
   * percentage change, and historical chart reflect the new market state.</p>
   */
  public void updateChart() {
    if (selectedSymbol == null) return;
    try {
      view.selectStock(exchange.getStock(selectedSymbol));
    } catch (Exception _) {

    }
  }

}
