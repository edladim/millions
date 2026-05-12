package edu.ntnu.idi.idatt.millions.model.transaction;

import edu.ntnu.idi.idatt.millions.model.Player;
import edu.ntnu.idi.idatt.millions.model.Share;
import edu.ntnu.idi.idatt.millions.model.Stock;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TransactionFactory} and its concrete subclasses
 * {@link PurchaseFactory} and {@link SaleFactory}.
 *
 * <p>The tests verify that each factory:</p>
 * <ul>
 *   <li>Creates the correct concrete {@link Transaction} type</li>
 *   <li>Correctly commits the transaction via {@code createAndCommit}</li>
 *   <li>Rejects null and invalid arguments</li>
 * </ul>
 */
class TransactionFactoryTest {

  private Share share;
  private Player player;

  /**
   * Creates a fresh share and a well-funded player before each test.
   */
  @BeforeEach
  void setUp() {
    Stock stock = new Stock("AAPL", "Apple Inc.", new BigDecimal("150"));
    share = new Share(stock, new BigDecimal("10"), new BigDecimal("150"));
    player = new Player("Test Player", new BigDecimal("100000"));
    player.getPortfolio().addShare(share);
  }

  // PurchaseFactory

  /** Verifies that PurchaseFactory.create returns a Purchase instance. */
  @Test
  void purchaseFactory_create_returnsPurchase() {
    TransactionFactory factory = new PurchaseFactory();
    Transaction t = factory.create(share, 1);
    assertInstanceOf(Purchase.class, t);
  }

  /** Verifies that the created Purchase carries the correct share and week. */
  @Test
  void purchaseFactory_create_setsShareAndWeek() {
    TransactionFactory factory = new PurchaseFactory();
    Transaction t = factory.create(share, 3);
    assertEquals(share, t.getShare());
    assertEquals(3, t.getWeek());
  }

  /** Verifies that createAndCommit commits the Purchase against the player. */
  @Test
  void purchaseFactory_createAndCommit_commitsTransaction() {
    player.getPortfolio().removeShare(share);
    TransactionFactory factory = new PurchaseFactory();
    Transaction t = factory.createAndCommit(share, 1, player);
    assertTrue(t.isCommitted());
    assertTrue(player.getPortfolio().contains(share));
  }

  /** Verifies that PurchaseFactory.create rejects a null share. */
  @Test
  void purchaseFactory_create_nullShare_throwsException() {
    TransactionFactory factory = new PurchaseFactory();
    assertThrows(NullPointerException.class, () -> factory.create(null, 1));
  }

  /** Verifies that PurchaseFactory.create rejects a non-positive week. */
  @Test
  void purchaseFactory_create_invalidWeek_throwsException() {
    TransactionFactory factory = new PurchaseFactory();
    assertThrows(IllegalArgumentException.class, () -> factory.create(share, 0));
  }

  // SaleFactory

  /** Verifies that SaleFactory.create returns a Sale instance. */
  @Test
  void saleFactory_create_returnsSale() {
    TransactionFactory factory = new SaleFactory();
    Transaction t = factory.create(share, 1);
    assertInstanceOf(Sale.class, t);
  }

  /** Verifies that the created Sale carries the correct share and week. */
  @Test
  void saleFactory_create_setsShareAndWeek() {
    TransactionFactory factory = new SaleFactory();
    Transaction t = factory.create(share, 7);
    assertEquals(share, t.getShare());
    assertEquals(7, t.getWeek());
  }

  /** Verifies that createAndCommit commits the Sale against the player. */
  @Test
  void saleFactory_createAndCommit_commitsTransaction() {
    TransactionFactory factory = new SaleFactory();
    Transaction t = factory.createAndCommit(share, 1, player);
    assertTrue(t.isCommitted());
    assertFalse(player.getPortfolio().contains(share));
  }

  /** Verifies that SaleFactory.create rejects a null share. */
  @Test
  void saleFactory_create_nullShare_throwsException() {
    TransactionFactory factory = new SaleFactory();
    assertThrows(NullPointerException.class, () -> factory.create(null, 1));
  }

  /** Verifies that SaleFactory.create rejects a non-positive week. */
  @Test
  void saleFactory_create_invalidWeek_throwsException() {
    TransactionFactory factory = new SaleFactory();
    assertThrows(IllegalArgumentException.class, () -> factory.create(share, 0));
  }

  // createAndCommit shared behaviour

  /** Verifies that createAndCommit rejects a null player. */
  @Test
  void createAndCommit_nullPlayer_throwsException() {
    TransactionFactory factory = new PurchaseFactory();
    assertThrows(NullPointerException.class,
        () -> factory.createAndCommit(share, 1, null));
  }
}