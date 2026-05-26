package edu.ntnu.idi.idatt.millions.model.transaction;

import static org.junit.jupiter.api.Assertions.*;

import edu.ntnu.idi.idatt.millions.model.market.Stock;
import edu.ntnu.idi.idatt.millions.model.player.Player;
import edu.ntnu.idi.idatt.millions.model.portfolio.Share;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Purchase}.
 *
 * <p>The tests verify that purchase transactions correctly:
 *
 * <ul>
 *   <li>Expose transaction information
 *   <li>Execute the commit operation
 *   <li>Change the player's money and portfolio
 *   <li>Store the transaction in the archive
 *   <li>Reject invalid commit scenarios
 * </ul>
 */
class PurchaseTest {

  private Share share;
  private Player player;
  private Purchase purchase;

  /** Creates a fresh purchase transaction before each test. */
  @BeforeEach
  void setUp() {

    Stock stock = new Stock("AAPL", "Apple Inc.", new BigDecimal("150"));
    share = new Share(stock, new BigDecimal("10"), new BigDecimal("150"));
    player = new Player("Test Player", new BigDecimal("10000"));
    purchase = new Purchase(share, 5);
  }

  /** Verifies that the correct share is stored in the transaction. */
  @Test
  void getShare_returnsCorrectShare() {
    assertEquals(share, purchase.getShare());
  }

  /** Verifies that the week value is stored correctly. */
  @Test
  void getWeek_returnsCorrectWeek() {
    assertEquals(5, purchase.getWeek());
  }

  /** Verifies that a purchase calculator is created automatically. */
  @Test
  void getCalculator_returnsPurchaseCalculator() {
    assertInstanceOf(PurchaseCalculator.class, purchase.getCalculator());
  }

  /** Verifies that a transaction is not committed initially. */
  @Test
  void isCommitted_initiallyFalse() {
    assertFalse(purchase.isCommitted());
  }

  /** Verifies that committing the purchase adds the share to the portfolio. */
  @Test
  void commit_addsShareToPortfolio() {
    purchase.commit(player);
    assertTrue(player.getPortfolio().contains(share));
  }

  /** Verifies that committing the purchase deducts money from the player. */
  @Test
  void commit_deductsMoneyFromPlayer() {
    BigDecimal before = player.getMoney();
    purchase.commit(player);
    assertTrue(player.getMoney().compareTo(before) < 0);
  }

  /** Verifies that the transaction is stored in the archive after commit. */
  @Test
  void commit_addsTransactionToArchive() {
    purchase.commit(player);
    assertTrue(player.getTransactionArchive().getTransactions(5).contains(purchase));
  }

  /** Verifies that the committed flag is set after commit. */
  @Test
  void commit_setsCommittedFlag() {
    purchase.commit(player);
    assertTrue(purchase.isCommitted());
  }

  /** Verifies that committing twice is not allowed. */
  @Test
  void commit_twice_throwsException() {
    purchase.commit(player);
    assertThrows(IllegalStateException.class, () -> purchase.commit(player));
  }

  /** Verifies that committing with insufficient funds fails. */
  @Test
  void commit_insufficientFunds_throwsException() {
    player.withdrawMoney(new BigDecimal("9999"));
    assertThrows(IllegalStateException.class, () -> purchase.commit(player));
  }

  /** Verifies that null player input is rejected. */
  @Test
  void commit_nullPlayer_throwsException() {
    assertThrows(NullPointerException.class, () -> purchase.commit(null));
  }

  /** Verifies that {@code isBuy()} returns {@code true} for a purchase. */
  @Test
  void isBuy_returnsTrue() {
    assertTrue(purchase.isBuy());
  }
}
