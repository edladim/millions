package edu.ntnu.idi.idatt.millions.model.transaction;

import edu.ntnu.idi.idatt.millions.model.player.Player;
import edu.ntnu.idi.idatt.millions.model.portfolio.Share;
import edu.ntnu.idi.idatt.millions.model.market.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Transaction}.
 *
 * <p>Since {@code Transaction} is an abstract class, a small concrete
 * test implementation is used to verify the shared behavior defined
 * in the superclass.</p>
 *
 * <p>The tests verify:</p>
 *
 * <ul>
 *   <li>Correct initialization of fields</li>
 *   <li>Getter methods</li>
 *   <li>Constructor validation</li>
 *   <li>Initial committed state</li>
 * </ul>
 */
class TransactionTest {

  private Share share;
  private TransactionCalculator calculator;
  private Transaction transaction;

  /**
   * Simple concrete implementation used only for testing the abstract class.
   */
  private static class TestTransaction extends Transaction {

    protected TestTransaction(Share share, int week, TransactionCalculator calculator) {
      super(share, week, calculator);
    }

    @Override
    public void commit(Player player) {
      committed = true;
    }
  }

  /**
   * Creates a reusable transaction before each test.
   */
  @BeforeEach
  void setUp() {

    Stock stock = new Stock("AAPL", "Apple Inc.", new BigDecimal("200"));
    share = new Share(stock, new BigDecimal("10"), new BigDecimal("100"));

    calculator = new PurchaseCalculator(share);

    transaction = new TestTransaction(share, 5, calculator);
  }

  /**
   * Verifies that the share associated with the transaction is returned correctly.
   */
  @Test
  void getShare_returnsCorrectShare() {
    assertEquals(share, transaction.getShare());
  }

  /**
   * Verifies that the transaction week is stored correctly.
   */
  @Test
  void getWeek_returnsCorrectWeek() {
    assertEquals(5, transaction.getWeek());
  }

  /**
   * Verifies that the calculator is stored correctly.
   */
  @Test
  void getCalculator_returnsCorrectCalculator() {
    assertEquals(calculator, transaction.getCalculator());
  }

  /**
   * Verifies that a new transaction is not committed initially.
   */
  @Test
  void isCommitted_newTransaction_returnsFalse() {
    assertFalse(transaction.isCommitted());
  }

  /**
   * Ensures that the constructor rejects null shares.
   */
  @Test
  void constructor_nullShare_throwsException() {

    assertThrows(NullPointerException.class,
        () -> new TestTransaction(null, 1, calculator));
  }

  /**
   * Ensures that the constructor rejects null calculators.
   */
  @Test
  void constructor_nullCalculator_throwsException() {

    assertThrows(NullPointerException.class,
        () -> new TestTransaction(share, 1, null));
  }

  /**
   * Ensures that negative week values are rejected.
   */
  @Test
  void constructor_negativeWeek_throwsException() {

    assertThrows(IllegalArgumentException.class,
        () -> new TestTransaction(share, -1, calculator));
  }

  /**
   * Ensures that week zero is rejected, since trading weeks start at 1.
   */
  @Test
  void constructor_zeroWeek_throwsException() {

    assertThrows(IllegalArgumentException.class,
        () -> new TestTransaction(share, 0, calculator));
  }
}