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