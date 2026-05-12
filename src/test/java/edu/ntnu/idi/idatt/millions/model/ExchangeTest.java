package edu.ntnu.idi.idatt.millions.model;

import edu.ntnu.idi.idatt.millions.model.transaction.Purchase;
import edu.ntnu.idi.idatt.millions.model.transaction.Sale;
import edu.ntnu.idi.idatt.millions.model.transaction.Transaction;
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
 *   <li>Executes and commits buy transactions</li>
 *   <li>Executes and commits sell transactions</li>
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
   * Creates an exchange with two stocks and a player before each test.
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

  /** Verifies that a null name throws a NullPointerException. */
  @Test
  void constructor_nullName_throwsException() {
    assertThrows(NullPointerException.class,
        () -> new Exchange(null, List.of(apple)));
  }

  /** Verifies that a blank name throws an IllegalArgumentException. */
  @Test
  void constructor_blankName_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> new Exchange("  ", List.of(apple)));
  }

  /** Verifies that an empty stock list throws an IllegalArgumentException. */
  @Test
  void constructor_emptyStockList_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> new Exchange("NASDAQ", List.of()));
  }

  /** Verifies that a null stock list throws a NullPointerException. */
  @Test
  void constructor_nullStockList_throwsException() {
    assertThrows(NullPointerException.class,
        () -> new Exchange("NASDAQ", null));
  }

  /** Verifies that duplicate stock symbols throw an IllegalArgumentException. */
  @Test
  void constructor_duplicateSymbol_throwsException() {
    Stock duplicate = new Stock("AAPL", "Apple Clone", new BigDecimal("100"));
    assertThrows(IllegalArgumentException.class,
        () -> new Exchange("NASDAQ", List.of(apple, duplicate)));
  }

  /** Verifies that getStocks returns all registered stocks. */
  @Test
  void getStocks_returnsAllStocks() {
    List<Stock> stocks = exchange.getStocks();
    assertEquals(2, stocks.size());
    assertTrue(stocks.contains(apple));
    assertTrue(stocks.contains(google));
  }

  /** Verifies that the returned stock list is unmodifiable. */
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

  /** Verifies that a stock can be retrieved by symbol. */
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

  /** Verifies that searching with a null term throws an exception. */
  @Test
  void findStocks_nullSearchTerm_throwsException() {
    assertThrows(NullPointerException.class,
        () -> exchange.findStocks(null));
  }

  /** Verifies that buy returns a committed Purchase transaction. */
  @Test
  void buy_returnsCommittedPurchaseTransaction() {
    Transaction transaction = exchange.buy("AAPL", new BigDecimal("5"), player);
    assertInstanceOf(Purchase.class, transaction);
    assertTrue(transaction.isCommitted());
  }

  /** Verifies that buy deducts the correct total cost from the player's balance. */
  @Test
  void buy_deductsCorrectAmountFromPlayer() {
    BigDecimal moneyBefore = player.getMoney();
    Transaction transaction = exchange.buy("AAPL", new BigDecimal("5"), player);
    BigDecimal expectedCost = transaction.getCalculator().calculateTotal();
    assertEquals(moneyBefore.subtract(expectedCost), player.getMoney());
  }

  /** Verifies that buy adds the share to the player's portfolio. */
  @Test
  void buy_addsShareToPlayersPortfolio() {
    exchange.buy("AAPL", new BigDecimal("5"), player);
    assertEquals(1, player.getPortfolio().getShares().size());
  }

  /** Verifies that buy stores the transaction in the player's archive. */
  @Test
  void buy_storesTransactionInArchive() {
    exchange.buy("AAPL", new BigDecimal("5"), player);
    assertFalse(player.getTransactionArchive().isEmpty());
  }

  /** Verifies that buy with insufficient funds throws an exception. */
  @Test
  void buy_insufficientFunds_throwsException() {
    Player poorPlayer = new Player("Poor", new BigDecimal("1"));
    assertThrows(IllegalStateException.class,
        () -> exchange.buy("AAPL", new BigDecimal("100"), poorPlayer));
  }

  /** Verifies that buy with zero quantity throws an exception. */
  @Test
  void buy_invalidQuantity_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> exchange.buy("AAPL", BigDecimal.ZERO, player));
  }

  /** Verifies that buy with a null player throws an exception. */
  @Test
  void buy_nullPlayer_throwsException() {
    assertThrows(NullPointerException.class,
        () -> exchange.buy("AAPL", new BigDecimal("5"), null));
  }

  /** Verifies that buy with a null quantity throws an exception. */
  @Test
  void buy_nullQuantity_throwsException() {
    assertThrows(NullPointerException.class,
        () -> exchange.buy("AAPL", null, player));
  }

  /** Verifies that buy with an unknown symbol throws an exception. */
  @Test
  void buy_unknownSymbol_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> exchange.buy("MSFT", new BigDecimal("5"), player));
  }

  /** Verifies that sell returns a committed Sale transaction. */
  @Test
  void sell_returnsCommittedSaleTransaction() {
    Transaction purchase = exchange.buy("AAPL", new BigDecimal("5"), player);
    Share share = purchase.getShare();

    Transaction sale = exchange.sell(share, player);
    assertInstanceOf(Sale.class, sale);
    assertTrue(sale.isCommitted());
  }

  /** Verifies that sell adds the correct amount to the player's balance. */
  @Test
  void sell_addsCorrectAmountToPlayer() {
    exchange.buy("AAPL", new BigDecimal("5"), player);
    Share share = player.getPortfolio().getShares().getFirst();
    BigDecimal moneyBefore = player.getMoney();

    Transaction sale = exchange.sell(share, player);
    BigDecimal expectedProceeds = sale.getCalculator().calculateTotal();
    assertEquals(moneyBefore.add(expectedProceeds), player.getMoney());
  }

  /** Verifies that sell removes the share from the player's portfolio. */
  @Test
  void sell_removesShareFromPlayersPortfolio() {
    exchange.buy("AAPL", new BigDecimal("5"), player);
    Share share = player.getPortfolio().getShares().getFirst();

    exchange.sell(share, player);
    assertTrue(player.getPortfolio().getShares().isEmpty());
  }

  /** Verifies that sell stores the transaction in the player's archive. */
  @Test
  void sell_storesTransactionInArchive() {
    exchange.buy("AAPL", new BigDecimal("5"), player);
    Share share = player.getPortfolio().getShares().getFirst();

    exchange.sell(share, player);
    assertEquals(2, player.getTransactionArchive().getTransactions(1).size());
  }

  /** Verifies that selling a share the player does not own throws an exception. */
  @Test
  void sell_shareNotOwnedByPlayer_throwsException() {
    Share unownedShare = new Share(apple, new BigDecimal("5"), new BigDecimal("150"));
    assertThrows(IllegalStateException.class,
        () -> exchange.sell(unownedShare, player));
  }

  /** Verifies that a null share in sell throws an exception. */
  @Test
  void sell_nullShare_throwsException() {
    assertThrows(NullPointerException.class,
        () -> exchange.sell(null, player));
  }

  /** Verifies that a null player in sell throws an exception. */
  @Test
  void sell_nullPlayer_throwsException() {
    Share share = new Share(apple, new BigDecimal("5"), new BigDecimal("150"));
    assertThrows(NullPointerException.class,
        () -> exchange.sell(share, null));
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
    assertNotEquals(0, oldPrice.compareTo(apple.getSalesPrice()));
  }

  /** Verifies that stock prices remain positive after advancing. */
  @Test
  void advance_pricesRemainPositive() {
    for (int i = 0; i < 100; i++) {
      exchange.advance();
    }
    exchange.getStocks().forEach(stock ->
        assertTrue(stock.getSalesPrice().compareTo(BigDecimal.ZERO) > 0));
  }

  /**
   * Verifies that getGainers returns stocks with positive price change,
   * sorted descending by change, and respects the limit.
   */
  @Test
  void getGainers_returnsTopGainersSortedDescending() {
    apple.addNewSalesPrice(apple.getSalesPrice().multiply(new BigDecimal("1.50")));
    google.addNewSalesPrice(google.getSalesPrice().multiply(new BigDecimal("1.10")));

    List<Stock> gainers = exchange.getGainers(2);

    assertEquals(2, gainers.size());
    assertEquals(apple, gainers.getFirst());
    assertEquals(google, gainers.getLast());
  }

  /** Verifies that getGainers respects the limit parameter. */
  @Test
  void getGainers_limitIsRespected() {
    apple.addNewSalesPrice(apple.getSalesPrice().multiply(new BigDecimal("1.50")));
    google.addNewSalesPrice(google.getSalesPrice().multiply(new BigDecimal("1.10")));

    List<Stock> gainers = exchange.getGainers(1);

    assertEquals(1, gainers.size());
    assertEquals(apple, gainers.getFirst());
  }

  /** Verifies that getGainers excludes stocks with no positive price change. */
  @Test
  void getGainers_excludesNonGainers() {
    apple.addNewSalesPrice(apple.getSalesPrice().multiply(new BigDecimal("1.50")));
    google.addNewSalesPrice(google.getSalesPrice().multiply(new BigDecimal("0.90")));

    List<Stock> gainers = exchange.getGainers(2);

    assertEquals(1, gainers.size());
    assertTrue(gainers.contains(apple));
    assertFalse(gainers.contains(google));
  }

  /** Verifies that getGainers returns an empty list when no stocks have gained. */
  @Test
  void getGainers_noGainers_returnsEmptyList() {
    apple.addNewSalesPrice(apple.getSalesPrice());
    google.addNewSalesPrice(google.getSalesPrice().multiply(new BigDecimal("0.90")));

    assertTrue(exchange.getGainers(2).isEmpty());
  }

  /** Verifies that an invalid limit throws an exception. */
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
    apple.addNewSalesPrice(apple.getSalesPrice().multiply(new BigDecimal("0.50")));
    google.addNewSalesPrice(google.getSalesPrice().multiply(new BigDecimal("0.90")));

    List<Stock> losers = exchange.getLosers(2);

    assertEquals(2, losers.size());
    assertEquals(apple, losers.getFirst());
    assertEquals(google, losers.getLast());
  }

  /** Verifies that getLosers respects the limit parameter. */
  @Test
  void getLosers_limitIsRespected() {
    apple.addNewSalesPrice(apple.getSalesPrice().multiply(new BigDecimal("0.50")));
    google.addNewSalesPrice(google.getSalesPrice().multiply(new BigDecimal("0.90")));

    List<Stock> losers = exchange.getLosers(1);

    assertEquals(1, losers.size());
    assertEquals(apple, losers.getFirst());
  }

  /** Verifies that getLosers excludes stocks with no negative price change. */
  @Test
  void getLosers_excludesNonLosers() {
    apple.addNewSalesPrice(apple.getSalesPrice().multiply(new BigDecimal("1.50")));
    google.addNewSalesPrice(google.getSalesPrice().multiply(new BigDecimal("0.90")));

    List<Stock> losers = exchange.getLosers(2);

    assertEquals(1, losers.size());
    assertTrue(losers.contains(google));
    assertFalse(losers.contains(apple));
  }

  /** Verifies that getLosers returns an empty list when no stocks have lost value. */
  @Test
  void getLosers_noLosers_returnsEmptyList() {
    apple.addNewSalesPrice(apple.getSalesPrice().multiply(new BigDecimal("1.50")));
    google.addNewSalesPrice(google.getSalesPrice());

    assertTrue(exchange.getLosers(2).isEmpty());
  }

  /** Verifies that an invalid limit throws an exception. */
  @Test
  void getLosers_invalidLimit_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> exchange.getLosers(0));
  }
}
