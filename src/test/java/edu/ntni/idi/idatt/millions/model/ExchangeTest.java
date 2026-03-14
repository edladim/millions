package edu.ntni.idi.idatt.millions.model;

import edu.ntni.idi.idatt.millions.model.transaction.Purchase;
import edu.ntni.idi.idatt.millions.model.transaction.Sale;
import edu.ntni.idi.idatt.millions.model.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Exchange}.
 *
 * <p>The tests verify that the exchange correctly:</p>
 * <ul>
 *   <li>Initializes with stocks</li>
 *   <li>Retrieves stocks</li>
 *   <li>Searches for stocks</li>
 *   <li>Creates buy transactions</li>
 *   <li>Creates sell transactions</li>
 *   <li>Advances weeks and updates prices</li>
 * </ul>
 *
 * <p>Validation and exceptional cases are also tested to ensure
 * robust behavior.</p>
 */
class ExchangeTest {

  private Exchange exchange;
  private Stock apple;
  private Stock google;
  private Player player;

  /**
   * Creates an exchange with two stocks before each test.
   */
  @BeforeEach
  void setUp() {
    apple = new Stock("AAPL", "Apple Inc.", new BigDecimal("150"));
    google = new Stock("GOOGL", "Alphabet Inc.", new BigDecimal("200"));
    exchange = new Exchange("NASDAQ", List.of(apple, google));
    player = new Player("Trader", new BigDecimal("10000"));
  }

  /** Verifies that the exchange name is stored correctly. */
  @Test
  void getName_returnsCorrectName() {
    assertEquals("NASDAQ", exchange.getName());
  }

  /** Verifies that the exchange starts at week 1. */
  @Test
  void getWeek_initialWeekIsOne() {
    assertEquals(1, exchange.getWeek());
  }

  /**
   * Verifies that getStocks returns all registered stocks.
   */
  @Test
  void getStocks_returnsAllStocks() {
    List<Stock> stocks = exchange.getStocks();
    assertEquals(2, stocks.size());
    assertTrue(stocks.contains(apple));
    assertTrue(stocks.contains(google));
  }

  /**
   * Verifies that the returned stock list is unmodifiable.
   */
  @Test
  void getStocks_returnsUnmodifiableList() {
    List<Stock> stocks = exchange.getStocks();
    assertThrows(UnsupportedOperationException.class,
        () -> stocks.add(new Stock("MSFT", "Microsoft", new BigDecimal("100"))));
  }

  /** Verifies that existing stocks are detected. */
  @Test
  void hasStock_existingSymbol_returnsTrue() {
    assertTrue(exchange.hasStock("AAPL"));
  }

  /** Verifies that non-existing stocks are reported correctly. */
  @Test
  void hasStock_nonExistingSymbol_returnsFalse() {
    assertFalse(exchange.hasStock("MSFT"));
  }

  /** Verifies that a stock can be retrieved by a symbol. */
  @Test
  void getStock_returnsCorrectStock() {
    Stock stock = exchange.getStock("AAPL");
    assertEquals("AAPL", stock.getSymbol());
  }

  /** Verifies that requesting a non-existent stock throws an exception. */
  @Test
  void getStock_invalidSymbol_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> exchange.getStock("MSFT"));
  }

  /**
   * Verifies that getGainers returns stocks with positive price change,
   * sorted descending by change, and respects the limit.
   */
  @Test
  void getGainers_returnsTopGainersSortedDescending() {
    apple.addNewSalesPrice(new BigDecimal("200"));
    google.addNewSalesPrice(new BigDecimal("210"));

    List<Stock> gainers = exchange.getGainers(2);

    assertEquals(2, gainers.size());
    assertEquals(apple, gainers.getFirst());
    assertEquals(google, gainers.getLast());
  }

  /**
   * Verifies that getGainers respects the limit parameter.
   */
  @Test
  void getGainers_limitIsRespected() {
    apple.addNewSalesPrice(new BigDecimal("200"));
    google.addNewSalesPrice(new BigDecimal("210"));

    List<Stock> gainers = exchange.getGainers(1);

    assertEquals(1, gainers.size());
    assertEquals(apple, gainers.getFirst());
  }

  /**
   * Verifies that getGainers excludes stocks with no positive price change.
   */
  @Test
  void getGainers_excludesNonGainers() {
    apple.addNewSalesPrice(new BigDecimal("200"));
    google.addNewSalesPrice(new BigDecimal("180"));

    List<Stock> gainers = exchange.getGainers(2);

    assertEquals(1, gainers.size());
    assertTrue(gainers.contains(apple));
    assertFalse(gainers.contains(google));
  }

  /**
   * Verifies that getGainers returns an empty list when no stocks have gained.
   */
  @Test
  void getGainers_noGainers_returnsEmptyList() {
    apple.addNewSalesPrice(new BigDecimal("100"));
    google.addNewSalesPrice(new BigDecimal("180"));

    assertTrue(exchange.getGainers(2).isEmpty());
  }

  /**
   * Verifies that an invalid limit throws an exception.
   */
  @Test
  void getGainers_invalidLimit_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> exchange.getGainers(0));
  }

  /**
   * Verifies that getLosers returns stocks with negative price change,
   * sorted ascending by change, and respects the limit.
   */
  @Test
  void getLosers_returnsTopLosersSortedAscending() {
    apple.addNewSalesPrice(new BigDecimal("100"));
    google.addNewSalesPrice(new BigDecimal("190"));

    List<Stock> losers = exchange.getLosers(2);

    assertEquals(2, losers.size());
    assertEquals(apple, losers.getFirst());
    assertEquals(google, losers.getLast());
  }

  /**
   * Verifies that getLosers respects the limit parameter.
   */
  @Test
  void getLosers_limitIsRespected() {
    apple.addNewSalesPrice(new BigDecimal("100"));
    google.addNewSalesPrice(new BigDecimal("190"));

    List<Stock> losers = exchange.getLosers(1);

    assertEquals(1, losers.size());
    assertEquals(apple, losers.getFirst());
  }

  /**
   * Verifies that getLosers excludes stocks with no negative price change.
   */
  @Test
  void getLosers_excludesNonLosers() {
    apple.addNewSalesPrice(new BigDecimal("200"));
    google.addNewSalesPrice(new BigDecimal("190"));

    List<Stock> losers = exchange.getLosers(2);

    assertEquals(1, losers.size());
    assertTrue(losers.contains(google));
    assertFalse(losers.contains(apple));
  }

  /**
   * Verifies that getLosers returns an empty list when no stocks have lost value.
   */
  @Test
  void getLosers_noLosers_returnsEmptyList() {
    apple.addNewSalesPrice(new BigDecimal("200"));
    google.addNewSalesPrice(new BigDecimal("200"));

    assertTrue(exchange.getLosers(2).isEmpty());
  }

  /**
   * Verifies that an invalid limit throws an exception.
   */
  @Test
  void getLosers_invalidLimit_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> exchange.getLosers(0));
  }

  /** Verifies that searching by symbol returns matching stocks. */
  @Test
  void findStocks_symbolSearch_returnsMatchingStocks() {
    List<Stock> result = exchange.findStocks("AAP");
    assertTrue(result.contains(apple));
  }

  /** Verifies that searching by company name returns matching stocks. */
  @Test
  void findStocks_companySearch_returnsMatchingStocks() {
    List<Stock> result = exchange.findStocks("Alphabet");
    assertTrue(result.contains(google));
  }

  /** Verifies that buy creates a purchase transaction. */
  @Test
  void buy_createsPurchaseTransaction() {
    Transaction transaction = exchange.buy("AAPL",
        new BigDecimal("5"),
        player);
    assertInstanceOf(Purchase.class, transaction);
  }

  /** Verifies that sell creates a sale transaction. */
  @Test
  void sell_createsSaleTransaction() {
    Share share = new Share(apple, new BigDecimal("5"), new BigDecimal("150"));
    Transaction transaction = exchange.sell(share, player);
    assertInstanceOf(Sale.class, transaction);
  }

  /** Verifies that advancing the exchange increments the week. */
  @Test
  void advance_incrementsWeek() {
    exchange.advance();
    assertEquals(2, exchange.getWeek());
  }

  /** Verifies that advancing the exchange updates stock prices. */
  @Test
  void advance_updatesStockPrices() {
    BigDecimal oldPrice = apple.getSalesPrice();
    exchange.advance();
    BigDecimal newPrice = apple.getSalesPrice();
    assertNotEquals(0, oldPrice.compareTo(newPrice));
  }

  /** Verifies that invalid buy quantity throws an exception. */
  @Test
  void buy_invalidQuantity_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> exchange.buy("AAPL", BigDecimal.ZERO, player));
  }

  /** Verifies that null player in buy throws an exception. */
  @Test
  void buy_nullPlayer_throwsException() {
    assertThrows(NullPointerException.class,
        () -> exchange.buy("AAPL", new BigDecimal("5"), null));
  }

  /** Verifies that null share in sell throws an exception. */
  @Test
  void sell_nullShare_throwsException() {
    assertThrows(NullPointerException.class,
        () -> exchange.sell(null, player));
  }
}