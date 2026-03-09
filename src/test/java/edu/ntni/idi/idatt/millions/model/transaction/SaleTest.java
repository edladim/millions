package edu.ntni.idi.idatt.millions.model.transaction;

import edu.ntni.idi.idatt.millions.model.Player;
import edu.ntni.idi.idatt.millions.model.Share;
import edu.ntni.idi.idatt.millions.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Sale}.
 *
 * <p>The tests verify that sale transactions correctly:</p>
 * <ul>
 *   <li>Expose transaction information</li>
 *   <li>Transfer money to the player</li>
 *   <li>Remove shares from the portfolio</li>
 *   <li>Store the transaction in the archive</li>
 *   <li>Reject invalid commit scenarios</li>
 * </ul>
 */
class SaleTest {

  private Share share;
  private Player player;
  private Sale sale;

  /**
   * Creates a share owned by the player before each test.
   */
  @BeforeEach
  void setUp() {
    Stock stock = new Stock("AAPL", "Apple Inc.", new BigDecimal("150"));
    share = new Share(stock, new BigDecimal("10"), new BigDecimal("150"));
    player = new Player("Test Player", new BigDecimal("10000"));
    player.getPortfolio().addShare(share);
    sale = new Sale(share, 5);
  }

  /** Verifies that the correct share is returned. */
  @Test
  void getShare_returnsCorrectShare() {
    assertEquals(share, sale.getShare());
  }

  /** Verifies that the calculator is of type SaleCalculator. */
  @Test
  void getCalculator_returnsSaleCalculator() {
    assertInstanceOf(SaleCalculator.class, sale.getCalculator());
  }

  /** Verifies that the week value is stored correctly. */
  @Test
  void getWeek_returnsCorrectWeek() {
    assertEquals(5, sale.getWeek());
  }

  /** Verifies that the transaction is initially not committed. */
  @Test
  void isCommitted_initiallyFalse() {
    assertFalse(sale.isCommitted());
  }

  /** Verifies that committing removes the share from the portfolio. */
  @Test
  void commit_removesShareFromPortfolio() {
    sale.commit(player);
    assertFalse(player.getPortfolio().contains(share));
  }

  /** Verifies that committing adds money to the player. */
  @Test
  void commit_addsMoneyToPlayer() {
    BigDecimal before = player.getMoney();
    sale.commit(player);
    assertTrue(player.getMoney().compareTo(before) > 0);
  }

  /** Verifies that the transaction is stored in the archive. */
  @Test
  void commit_addsTransactionToArchive() {
    sale.commit(player);
    assertTrue(player.getTransactionArchive()
        .getSales(5)
        .contains(sale));
  }

  /** Verifies that the committed flag is set. */
  @Test
  void commit_setsCommittedFlag() {
    sale.commit(player);
    assertTrue(sale.isCommitted());
  }

  /** Verifies that selling a share the player does not own fails. */
  @Test
  void commit_shareNotOwned_throwsException() {
    player.getPortfolio().removeShare(share);
    assertThrows(IllegalStateException.class,
        () -> sale.commit(player));
  }

  /** Verifies that committing twice is not allowed. */
  @Test
  void commit_twice_throwsException() {
    sale.commit(player);
    assertThrows(IllegalStateException.class,
        () -> sale.commit(player));
  }

  /** Verifies that null player input is rejected. */
  @Test
  void commit_nullPlayer_throwsException() {

    assertThrows(NullPointerException.class,
        () -> sale.commit(null));
  }
}