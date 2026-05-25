package edu.ntnu.idi.idatt.millions.model.market;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Stock}.
 *
 * <p>These tests verify that {@code Stock}:
 *
 * <ul>
 *   <li>validates constructor and method arguments
 *   <li>protects its internal price list (defensive copy)
 *   <li>implements {@code equals()} and {@code hashCode()} based on the symbol
 * </ul>
 *
 * <p>Both valid cases and exceptional cases are tested to ensure full branch coverage.
 */
class StockTest {

  /**
   * Verifies that a valid construction initializes the object correctly and stores the initial sale
   * price as the first element in the price history.
   */
  @Test
  void constructor_validInput_createsStock() {
    Stock stock = new Stock("AAPL", "Apple Inc.", new BigDecimal("100"));

    assertEquals("AAPL", stock.getSymbol());
    assertEquals("Apple Inc.", stock.getCompany());
    assertEquals(new BigDecimal("100"), stock.getSalesPrice());
    assertEquals(1, stock.getHistoricalPrices().size());
  }

  /** Ensures the ticker symbol is mandatory and null is rejected. */
  @Test
  void constructor_nullSymbol_throwsException() {
    assertThrows(NullPointerException.class, () -> new Stock(null, "Apple", BigDecimal.TEN));
  }

  /** Ensures the ticker symbol cannot be blank (after trimming). */
  @Test
  void constructor_blankSymbol_throwsException() {
    assertThrows(IllegalArgumentException.class, () -> new Stock("   ", "Apple", BigDecimal.TEN));
  }

  /** Ensures the company name is mandatory and null is rejected. */
  @Test
  void constructor_nullCompany_throwsException() {
    assertThrows(NullPointerException.class, () -> new Stock("AAPL", null, BigDecimal.TEN));
  }

  /** Ensures the company name cannot be blank (after trimming). */
  @Test
  void constructor_blankCompany_throwsException() {
    assertThrows(IllegalArgumentException.class, () -> new Stock("AAPL", "   ", BigDecimal.TEN));
  }

  /** Ensures negative initial prices are rejected to keep the object in a valid state. */
  @Test
  void constructor_negativePrice_throwsException() {
    assertThrows(
        IllegalArgumentException.class, () -> new Stock("AAPL", "Apple", new BigDecimal("-1")));
  }

  /**
   * Verifies that adding a valid new sales price appends to the history and updates the latest
   * price returned by {@link Stock#getSalesPrice()}.
   */
  @Test
  void addNewSalesPrice_validPrice_addsPrice() {
    Stock stock = new Stock("AAPL", "Apple", BigDecimal.TEN);

    stock.addNewSalesPrice(new BigDecimal("20"));

    assertEquals(new BigDecimal("20"), stock.getSalesPrice());
    assertEquals(2, stock.getHistoricalPrices().size());
  }

  /** Ensures null prices are rejected. */
  @Test
  void addNewSalesPrice_null_throwsException() {
    Stock stock = new Stock("AAPL", "Apple", BigDecimal.TEN);

    assertThrows(NullPointerException.class, () -> stock.addNewSalesPrice(null));
  }

  /** Ensures negative sales prices are rejected. */
  @Test
  void addNewSalesPrice_negative_throwsException() {
    Stock stock = new Stock("AAPL", "Apple", BigDecimal.TEN);

    assertThrows(
        IllegalArgumentException.class, () -> stock.addNewSalesPrice(new BigDecimal("-5")));
  }

  /**
   * Verifies that {@link Stock#getHistoricalPrices()} returns an unmodifiable snapshot/view. This
   * prevents external callers from mutating internal state.
   */
  @Test
  void getHistoricalPrices_returnsUnmodifiableList() {
    Stock stock = new Stock("AAPL", "Apple", BigDecimal.TEN);
    List<BigDecimal> prices = stock.getHistoricalPrices();

    assertThrows(UnsupportedOperationException.class, () -> prices.add(BigDecimal.ONE));
  }

  /** Verifies that the highest price is correctly identified from the price history. */
  @Test
  void getHighestPrice_multiplePrices_returnsMax() {
    Stock stock = new Stock("AAPL", "Apple", new BigDecimal("100"));
    stock.addNewSalesPrice(new BigDecimal("250"));
    stock.addNewSalesPrice(new BigDecimal("75"));

    assertEquals(new BigDecimal("250"), stock.getHighestPrice());
  }

  /** Verifies that a single recorded price is returned as the highest. */
  @Test
  void getHighestPrice_singlePrice_returnsThatPrice() {
    Stock stock = new Stock("AAPL", "Apple", new BigDecimal("100"));

    assertEquals(new BigDecimal("100"), stock.getHighestPrice());
  }

  /** Verifies that the lowest price is correctly identified from the price history. */
  @Test
  void getLowestPrice_multiplePrices_returnsMin() {
    Stock stock = new Stock("AAPL", "Apple", new BigDecimal("100"));
    stock.addNewSalesPrice(new BigDecimal("250"));
    stock.addNewSalesPrice(new BigDecimal("75"));

    assertEquals(new BigDecimal("75"), stock.getLowestPrice());
  }

  /** Verifies that a single recorded price is returned as the lowest. */
  @Test
  void getLowestPrice_singlePrice_returnsThatPrice() {
    Stock stock = new Stock("AAPL", "Apple", new BigDecimal("100"));

    assertEquals(new BigDecimal("100"), stock.getLowestPrice());
  }

  /**
   * Verifies that the latest price change is the difference between the last and second-to-last
   * recorded price.
   */
  @Test
  void getLatestPriceChange_multiplePrices_returnsDifference() {
    Stock stock = new Stock("AAPL", "Apple", new BigDecimal("100"));
    stock.addNewSalesPrice(new BigDecimal("130"));

    assertEquals(new BigDecimal("30"), stock.getLatestPriceChange());
  }

  /** Verifies that a price decrease is returned as a negative value. */
  @Test
  void getLatestPriceChange_priceDecreased_returnsNegative() {
    Stock stock = new Stock("AAPL", "Apple", new BigDecimal("100"));
    stock.addNewSalesPrice(new BigDecimal("80"));

    assertEquals(new BigDecimal("-20"), stock.getLatestPriceChange());
  }

  /**
   * Verifies that a single recorded price results in zero change, as there is no previous price to
   * compare against.
   */
  @Test
  void getLatestPriceChange_singlePrice_returnsZero() {
    Stock stock = new Stock("AAPL", "Apple", new BigDecimal("100"));

    assertEquals(BigDecimal.ZERO, stock.getLatestPriceChange());
  }

  /**
   * Verifies that equality is based exclusively on ticker symbol, independent of company name and
   * price history, and that the hash code contract holds.
   */
  @Test
  void equals_sameSymbol_returnsTrue() {
    Stock s1 = new Stock("AAPL", "Apple1", BigDecimal.TEN);
    Stock s2 = new Stock("AAPL", "Apple2", BigDecimal.ONE);

    assertEquals(s1, s2);
    assertEquals(s1.hashCode(), s2.hashCode());
  }

  /** Ensures two stocks with different ticker symbols are not considered equal. */
  @Test
  void equals_differentSymbol_returnsFalse() {
    Stock s1 = new Stock("AAPL", "Apple", BigDecimal.TEN);
    Stock s2 = new Stock("TSLA", "Tesla", BigDecimal.TEN);

    assertNotEquals(s1, s2);
  }

  /** Ensures the equals implementation safely handles null. */
  @Test
  void equals_null_returnsFalse() {
    Stock stock = new Stock("AAPL", "Apple", BigDecimal.TEN);
    assertFalse(stock.equals(null));
  }

  /** Ensures equals returns false for unrelated types. */
  @Test
  void equals_differentType_returnsFalse() {
    Stock stock = new Stock("AAPL", "Apple", BigDecimal.TEN);
    assertFalse(stock.equals("not a stock"));
  }

  /** Verifies that the price history preserves insertion order. */
  @Test
  void getHistoricalPrices_preservesInsertionOrder() {
    Stock stock = new Stock("AAPL", "Apple", BigDecimal.TEN);
    stock.addNewSalesPrice(new BigDecimal("12"));

    assertEquals(List.of(BigDecimal.TEN, new BigDecimal("12")), stock.getHistoricalPrices());
  }

  /** Verifies that hashCode is based on the ticker symbol. */
  @Test
  void hashCode_sameSymbol_matches() {
    Stock s1 = new Stock("AAPL", "Apple", BigDecimal.TEN);
    Stock s2 = new Stock("AAPL", "Apple Inc.", BigDecimal.ONE);

    assertEquals(s1.hashCode(), s2.hashCode());
  }

  /** Verifies that toString includes the symbol, company, and price history. */
  @Test
  void toString_includesSymbolCompanyAndPrices() {
    Stock stock = new Stock("AAPL", "Apple", BigDecimal.TEN);

    String result = stock.toString();

    assertTrue(result.contains("AAPL"));
    assertTrue(result.contains("Apple"));
    assertTrue(result.contains("10"));
  }

  /** Verifies that equals returns true when comparing the same instance. */
  @Test
  void equals_sameInstance_returnsTrue() {
    Stock stock = new Stock("AAPL", "Apple", BigDecimal.TEN);

    assertEquals(stock, stock);
  }
}
