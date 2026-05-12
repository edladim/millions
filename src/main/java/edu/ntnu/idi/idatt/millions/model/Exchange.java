package edu.ntnu.idi.idatt.millions.model;

import edu.ntnu.idi.idatt.millions.model.fluctuator.MomentumFluctuator;
import edu.ntnu.idi.idatt.millions.model.fluctuator.PriceFluctuator;
import edu.ntnu.idi.idatt.millions.model.transaction.PurchaseFactory;
import edu.ntnu.idi.idatt.millions.model.transaction.SaleFactory;
import edu.ntnu.idi.idatt.millions.model.transaction.Transaction;
import edu.ntnu.idi.idatt.millions.model.transaction.TransactionFactory;
import edu.ntnu.idi.idatt.millions.observer.ExchangeObserver;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Represents a stock exchange where players can buy and sell stocks.
 *
 * <p>The exchange maintains a collection of stocks that can be traded.
 * Stocks are stored internally in a map using their ticker symbol as
 * the key.</p>
 *
 * <p>The exchange also tracks the current trading week and updates stock
 * prices when advancing to the next week.</p>
 */
public final class Exchange implements ReadOnlyExchange {

  private final List<ExchangeObserver> observers = new ArrayList<>();
  private final String name;
  private int week;
  private final Map<String, Stock> stockMap;
  private final Random random;
  private final PriceFluctuator fluctuator;
  private final TransactionFactory purchaseFactory = new PurchaseFactory();
  private final TransactionFactory saleFactory = new SaleFactory();

  /**
   * Creates a new exchange.
   *
   * @param name the exchange name
   * @param stocks the list of stocks traded on the exchange
   *
   * @throws NullPointerException if name or stocks is null
   * @throws IllegalArgumentException if name is blank or stock list is empty
   */
  public Exchange(String name, List<Stock> stocks) {

    this.name = validateName(name);
    Objects.requireNonNull(stocks, "Stock list cannot be null");
    if (stocks.isEmpty()) {
      throw new IllegalArgumentException("Exchange must contain at least one stock");
    }

    this.stockMap = new HashMap<>();
    this.random = new Random();
    this.fluctuator = new MomentumFluctuator();
    this.week = 1;

    for (Stock stock : stocks) {
      Objects.requireNonNull(stock, "Stock cannot be null");
      if (stockMap.containsKey(stock.getSymbol())) {
        throw new IllegalArgumentException("Duplicate stock symbol: " + stock.getSymbol());
      }

      stockMap.put(stock.getSymbol(), stock);
    }

    preSimulate(30);
  }

  /**
   * Returns the exchange name.
   *
   * @return the exchange name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the current trading week.
   *
   * @return the current week number
   */
  public int getWeek() {
    return week;
  }

  /**
   * Checks whether a stock with the given symbol exists.
   *
   * @param symbol the stock symbol
   *
   * @return true if the stock exists
   *
   * @throws NullPointerException if symbol is null
   * @throws IllegalArgumentException if symbol is blank
   */
  public boolean hasStock(String symbol) {
    symbol = validateSymbol(symbol);
    return stockMap.containsKey(symbol);
  }

  /**
   * Returns the stock associated with the given symbol.
   *
   * @param symbol the stock symbol
   *
   * @return the stock
   *
   * @throws IllegalArgumentException if the symbol does not exist
   */
  public Stock getStock(String symbol) {
    symbol = validateSymbol(symbol);
    Stock stock = stockMap.get(symbol);
    if (stock == null) {
      throw new IllegalArgumentException("Stock not found: " + symbol);
    }
    return stock;
  }

  /**
   * Retrieves a list of all stocks currently registered in the exchange.
   *
   * @return an unmodifiable list of stocks present in the exchange,
   *         never null but could be empty if no stocks are registered
   */
  public List<Stock> getStocks() {
    return List.copyOf(stockMap.values());
  }

  /**
   * Returns the top gaining stocks since last week, sorted by price change descending.
   *
   * <p>Only stocks with a positive latest price change are included.
   * The result is limited to at most {@code limit} entries.</p>
   *
   * @param limit the maximum number of stocks to return
   * @return an unmodifiable list of top gainers, never null, may be empty
   * @throws IllegalArgumentException if {@code limit} is not positive
   */
  public List<Stock> getGainers(int limit) {
    if (limit <= 0) {
      throw new IllegalArgumentException("Limit must be positive");
    }

    return stockMap.values().stream()
        .filter(stock -> stock.getLatestPriceChange().compareTo(BigDecimal.ZERO) > 0)
        .sorted(Comparator.comparingDouble(Exchange::percentChange).reversed())
        .limit(limit)
        .toList();
  }

  /**
   * Returns the worst performing stocks since last week, sorted by price change ascending.
   *
   * <p>Only stocks with a negative latest price change are included.
   * The result is limited to at most {@code limit} entries.</p>
   *
   * @param limit the maximum number of stocks to return
   * @return an unmodifiable list of top losers, never null, may be empty
   * @throws IllegalArgumentException if {@code limit} is not positive
   */
  public List<Stock> getLosers(int limit) {
    if (limit <= 0) {
      throw new IllegalArgumentException("Limit must be positive");
    }

    return stockMap.values().stream()
        .filter(stock -> stock.getLatestPriceChange().compareTo(BigDecimal.ZERO) < 0)
        .sorted(Comparator.comparingDouble(Exchange::percentChange))
        .limit(limit)
        .toList();
  }

  /**
   * Finds all stocks matching a search term.
   *
   * <p>The search checks both the symbol and company name and is
   * case-insensitive.</p>
   *
   * @param searchTerm the search term
   *
   * @return a list of matching stocks
   *
   * @throws NullPointerException if searchTerm is null
   */
  public List<Stock> findStocks(String searchTerm) {
    Objects.requireNonNull(searchTerm, "Search term cannot be null");
    String query = searchTerm.toLowerCase().trim();
    return stockMap.values().stream()
        .filter(stock ->
            stock.getSymbol().toLowerCase().contains(query)
                || stock.getCompany().toLowerCase().contains(query))
        .toList();
  }

  /**
   * Buys shares of a stock for a player.
   *
   * @param symbol the stock symbol
   * @param quantity number of shares
   * @param player the player performing the purchase
   *
   * @return the created transaction
   */
  public Transaction buy(String symbol, BigDecimal quantity, Player player) {
    Objects.requireNonNull(player, "Player cannot be null");
    Objects.requireNonNull(quantity, "Quantity cannot be null");

    if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Quantity must be positive");
    }

    Stock stock = getStock(symbol);
    Share share = new Share(stock, quantity, stock.getSalesPrice());
    return purchaseFactory.createAndCommit(share, week, player);
  }

  /**
   * Sells a share owned by a player.
   *
   * @param share the share to sell
   * @param player the player performing the sale
   *
   * @return the created transaction
   */
  public Transaction sell(Share share, Player player) {

    Objects.requireNonNull(share, "Share cannot be null");
    Objects.requireNonNull(player, "Player cannot be null");

    return saleFactory.createAndCommit(share, week, player);
  }

  /**
   * Sells a specified quantity of a stock, drawing from the player's existing
   * share lots in the order they were acquired.
   *
   * <p>If a lot is fully consumed, it is sold whole. If the requested quantity
   * lands inside a lot, that lot is split: the remaining portion is kept in
   * the portfolio at its original purchase price, and the sold portion is
   * committed as a sale transaction.</p>
   *
   * @param symbol the stock symbol to sell
   * @param quantity the quantity to sell, must be positive and not exceed the
   *                 total quantity owned of {@code symbol}
   * @param player the player performing the sale
   * @return the transaction created for the final sale lot
   *
   * @throws NullPointerException if any argument is null
   * @throws IllegalArgumentException if {@code quantity} is not positive
   * @throws IllegalStateException if the player does not own enough of the stock
   */
  public Transaction sell(String symbol, BigDecimal quantity, Player player) {
    Objects.requireNonNull(symbol, "Symbol cannot be null");
    Objects.requireNonNull(quantity, "Quantity cannot be null");
    Objects.requireNonNull(player, "Player cannot be null");
    if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Quantity must be positive");
    }

    Portfolio portfolio = player.getPortfolio();
    List<Share> matching = portfolio.getShares(symbol);
    BigDecimal totalOwned = matching.stream()
        .map(Share::getQuantity)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (quantity.compareTo(totalOwned) > 0) {
      throw new IllegalStateException(
          "Cannot sell more than owned (owned: " + totalOwned + ")");
    }

    Stock stock = getStock(symbol);
    BigDecimal remaining = quantity;
    Transaction lastTx = null;
    for (Share share : matching) {
      if (remaining.signum() <= 0) break;

      if (share.getQuantity().compareTo(remaining) <= 0) {
        lastTx = sell(share, player);
        remaining = remaining.subtract(share.getQuantity());
      } else {
        portfolio.removeShare(share);
        BigDecimal kept = share.getQuantity().subtract(remaining);
        portfolio.addShare(new Share(stock, kept, share.getPurchasePrice()));

        Share toSell = new Share(stock, remaining, share.getPurchasePrice());
        portfolio.addShare(toSell);
        lastTx = sell(toSell, player);
        remaining = BigDecimal.ZERO;
      }
    }
    return lastTx;
  }

  /**
   * Registers an {@link ExchangeObserver} to be notified when the exchange advances.
   *
   * @param observer the observer to add, cannot be null
   * @throws NullPointerException if {@code observer} is null
   */
  public void addObserver(ExchangeObserver observer) {
    Objects.requireNonNull(observer, "Observer cannot be null");
    if (!observers.contains(observer)) {
      observers.add(observer);
    }
  }

  /**
   * Advances the exchange to the next trading week.
   *
   * <p>This increments the week number, updates stock prices randomly,
   * and notifies all registered {@link ExchangeObserver}s.</p>
   */
  public void advance() {
    week++;
    fluctuator.beginWeek(random);

    for (Stock stock : stockMap.values()) {
      BigDecimal newPrice = fluctuator.nextPrice(
          stock.getSymbol(), stock.getSalesPrice(), random);
      stock.addNewSalesPrice(newPrice);
    }

    for (ExchangeObserver observer : observers) {
      observer.onExchangeUpdated(this);
    }
  }

  /**
   * Returns the percentage price change for a stock as a {@code double}, used
   * for sorting gainers and losers by relative move rather than absolute dollar change.
   *
   * @param stock the stock to evaluate
   * @return percentage change, or {@code 0.0} if the previous price was zero
   */
  private static double percentChange(Stock stock) {
    BigDecimal change = stock.getLatestPriceChange();
    BigDecimal prev   = stock.getSalesPrice().subtract(change);
    if (prev.signum() == 0) return 0.0;
    return change.divide(prev, 8, RoundingMode.HALF_UP).doubleValue();
  }

  /**
   * Pre-generates price history for all stocks without incrementing the week
   * counter or notifying observers. Called once during construction so charts
   * have historical data from the very first frame.
   *
   * @param weeks the number of historical weeks to simulate
   */
  private void preSimulate(int weeks) {
    for (int i = 0; i < weeks; i++) {
      fluctuator.beginWeek(random);
      for (Stock stock : stockMap.values()) {
        BigDecimal newPrice = fluctuator.nextPrice(
            stock.getSymbol(), stock.getSalesPrice(), random);
        stock.addNewSalesPrice(newPrice);
      }
    }
  }

  private static String validateName(String name) {
    name = Objects.requireNonNull(name, "Name cannot be null").trim();
    if (name.isEmpty()) {
      throw new IllegalArgumentException("Name cannot be blank");
    }
    return name;
  }

  private static String validateSymbol(String symbol) {
    symbol = Objects.requireNonNull(symbol, "Symbol cannot be null").trim();
    if (symbol.isEmpty()) {
      throw new IllegalArgumentException("Symbol cannot be blank");
    }
    return symbol;
  }
}