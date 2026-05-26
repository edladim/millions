package edu.ntnu.idi.idatt.millions.model.transaction;

import static org.junit.jupiter.api.Assertions.*;

import edu.ntnu.idi.idatt.millions.model.market.Stock;
import edu.ntnu.idi.idatt.millions.model.portfolio.Share;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link TransactionArchive}. Verifies that transactions are stored, filtered and
 * counted correctly. The tests cover:
 *
 * <ul>
 *   <li>Adding transactions
 *   <li>Checking if the archive is empty
 *   <li>Filtering transactions by week
 *   <li>Filtering purchases and sales
 *   <li>Counting distinct transaction weeks
 * </ul>
 */
public class TransactionArchiveTest {

  private TransactionArchive archive;
  private Share share;

  /** Initializes a fresh archive and sample share before each test. */
  @BeforeEach
  void setup() {
    archive = new TransactionArchive();

    Stock stock = new Stock("AAPL", "Apple Inc.", BigDecimal.valueOf(150));
    share = new Share(stock, BigDecimal.valueOf(100), BigDecimal.valueOf(10));
  }

  /** Verifies that a new archive is empty. */
  @Test
  void archiveShouldBeEmptyInitially() {
    assertTrue(archive.isEmpty());
  }

  /** Verifies that adding a transaction returns true and makes the archive non-empty. */
  @Test
  void addShouldStoreTransaction() {
    Purchase purchase = new Purchase(share, 1);

    assertTrue(archive.add(purchase));
    assertFalse(archive.isEmpty());
  }

  /** Verifies that transactions are correctly filtered by week. */
  @Test
  void shouldReturnTransactionsForSpecificWeek() {
    archive.add(new Purchase(share, 1));
    archive.add(new Purchase(share, 2));
    archive.add(new Sale(share, 1));

    List<Transaction> week1 = archive.getTransactions(1);
    assertEquals(2, week1.size());
  }

  /** Verifies that an empty list is returned when no transactions exist for a week. */
  @Test
  void shouldReturnEmptyListForWeekWithoutTransactions() {
    archive.add(new Purchase(share, 1));

    List<Transaction> week5 = archive.getTransactions(5);

    assertTrue(week5.isEmpty());
  }

  /** Verifies that only purchases are returned for a given week. */
  @Test
  void shouldReturnOnlyPurchasesForWeek() {
    archive.add(new Purchase(share, 1));
    archive.add(new Sale(share, 1));

    List<Purchase> purchases = archive.getPurchases(1);

    assertEquals(1, purchases.size());
    assertInstanceOf(Purchase.class, purchases.getFirst());
  }

  /** Verifies that only sales are returned for a given week. */
  @Test
  void shouldReturnOnlySalesForWeek() {
    archive.add(new Purchase(share, 1));
    archive.add(new Sale(share, 1));

    List<Sale> sales = archive.getSales(1);

    assertEquals(1, sales.size());
    assertInstanceOf(Sale.class, sales.getFirst());
  }

  /** Verifies that distinct weeks are counted correctly. */
  @Test
  void shouldCountDistinctWeeks() {
    archive.add(new Purchase(share, 1));
    archive.add(new Purchase(share, 1));
    archive.add(new Sale(share, 2));
    archive.add(new Sale(share, 3));

    assertEquals(3, archive.countDistinctWeeks());
  }

  /** Verifies that the distinct week count is zero when the archive is empty. */
  @Test
  void distinctWeeksShouldBeZeroWhenArchiveIsEmpty() {
    assertEquals(0, archive.countDistinctWeeks());
  }

  /** Verifies that {@code getAll} returns all transactions in insertion order. */
  @Test
  void getAllShouldReturnAllTransactionsInOrder() {
    Purchase purchase = new Purchase(share, 1);
    Sale sale = new Sale(share, 2);
    archive.add(purchase);
    archive.add(sale);

    List<Transaction> all = archive.getAll();

    assertEquals(2, all.size());
    assertEquals(purchase, all.get(0));
    assertEquals(sale, all.get(1));
  }

  /** Verifies that {@code getAll} returns an empty list when the archive is empty. */
  @Test
  void getAllShouldReturnEmptyListWhenArchiveIsEmpty() {
    assertTrue(archive.getAll().isEmpty());
  }

  /** Verifies that adding {@code null} throws {@link NullPointerException}. */
  @Test
  void addNullShouldThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> archive.add(null));
  }

  /**
   * Verifies that a negative week number in {@code getTransactions} throws {@link
   * IllegalArgumentException}.
   */
  @Test
  void getTransactionsWithNegativeWeekShouldThrow() {
    assertThrows(IllegalArgumentException.class, () -> archive.getTransactions(-1));
  }

  /**
   * Verifies that a negative week number in {@code getPurchases} throws {@link
   * IllegalArgumentException}.
   */
  @Test
  void getPurchasesWithNegativeWeekShouldThrow() {
    assertThrows(IllegalArgumentException.class, () -> archive.getPurchases(-1));
  }

  /**
   * Verifies that a negative week number in {@code getSales} throws {@link
   * IllegalArgumentException}.
   */
  @Test
  void getSalesWithNegativeWeekShouldThrow() {
    assertThrows(IllegalArgumentException.class, () -> archive.getSales(-1));
  }
}
