package edu.ntni.idi.idatt.millions.model;

import edu.ntni.idi.idatt.millions.model.transaction.Purchase;
import edu.ntni.idi.idatt.millions.model.transaction.Sale;
import edu.ntni.idi.idatt.millions.model.transaction.Transaction;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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
public final class Exchange {

  private final String name;
  private int week;
  private final Map<String, Stock> stockMap;
  private final Random random;

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
    this.week = 1;

    for (Stock stock : stocks) {
      Objects.requireNonNull(stock, "Stock cannot be null");
      if (stockMap.containsKey(stock.getSymbol())) {
        throw new IllegalArgumentException("Duplicate stock symbol: " + stock.getSymbol());
      }

      stockMap.put(stock.getSymbol(), stock);
    }
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

    return new Purchase(share, week);
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

    return new Sale(share, week);
  }

  /**
   * Advances the exchange to the next trading week.
   *
   * <p>This increments the week number and updates stock prices.
   * Prices change randomly but remain positive.</p>
   */
  public void advance() {
    week++;

    for (Stock stock : stockMap.values()) {
      BigDecimal currentPrice = stock.getSalesPrice();
      double change = (random.nextDouble() - 0.5) * 0.2;
      BigDecimal multiplier = BigDecimal.valueOf(1 + change);
      BigDecimal newPrice = currentPrice.multiply(multiplier);

      if (newPrice.compareTo(BigDecimal.ONE) < 0) {
        newPrice = BigDecimal.ONE;
      }

      stock.addNewSalesPrice(newPrice);
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