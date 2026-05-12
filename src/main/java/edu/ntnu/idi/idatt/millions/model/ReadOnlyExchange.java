package edu.ntnu.idi.idatt.millions.model;

import java.util.List;

/**
 * <p>
 * Read-only view of an {@link Exchange} intended for use by observer classes
 * such as views.
 * </p>
 *
 * <p>
 * Exposing only getter methods ensures that observers can display exchange data
 * without being able to execute trades or advance the week. The full mutable
 * {@link Exchange} is only accessible to controllers.
 * </p>
 */
public interface ReadOnlyExchange {

  /**
   * Returns the exchange name.
   *
   * @return the exchange name
   */
  String getName();

  /**
   * Returns the current trading week number.
   *
   * @return the current week
   */
  int getWeek();

  /**
   * Returns an unmodifiable list of all stocks on the exchange.
   *
   * @return all stocks, never null
   */
  List<? extends ReadOnlyStock> getStocks();

  /**
   * Returns the top gaining stocks, limited to {@code limit} entries.
   *
   * @param limit the maximum number of results
   * @return the top gainers, never null
   */
  List<? extends ReadOnlyStock> getGainers(int limit);

  /**
   * Returns the worst performing stocks, limited to {@code limit} entries.
   *
   * @param limit the maximum number of results
   * @return the top losers, never null
   */
  List<? extends ReadOnlyStock> getLosers(int limit);

  /**
   * Finds all stocks matching the given search term (symbol or company name).
   *
   * @param searchTerm the search term, case-insensitive
   * @return matching stocks, never null
   */
  List<? extends ReadOnlyStock> findStocks(String searchTerm);

  /**
   * Returns the stock with the given symbol.
   *
   * @param symbol the ticker symbol
   * @return the matching stock
   * @throws IllegalArgumentException if the symbol does not exist
   */
  ReadOnlyStock getStock(String symbol);

  /**
   * Checks whether a stock with the given symbol exists on the exchange.
   *
   * @param symbol the ticker symbol
   * @return {@code true} if the stock exists
   */
  boolean hasStock(String symbol);
}
