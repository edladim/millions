package edu.ntni.idi.idatt.millions.transaction;

import edu.ntni.idi.idatt.millions.Player;
import edu.ntni.idi.idatt.millions.Share;
import edu.ntni.idi.idatt.millions.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class SaleTest {
  private Share share;
  private Stock stock;
  private Player player;
  private Sale sale;

  @BeforeEach
  public void setUp() {
    stock = new Stock("AAPL", "Apple Inc.", BigDecimal.valueOf(150.0));
    share = new Share(stock, BigDecimal.valueOf(10), BigDecimal.valueOf(150.0));
    player = new Player("Test Player", new BigDecimal("10000"));
    player.getPortfolio().addShare(share);
    sale = new Sale(share, 5);
  }

  @Test
  void testGetShare() {
    assertEquals(share, sale.getShare());
  }

  @Test
  void testGetCalculator() {
    assertNotNull(sale.getCalculator());
  }

  @Test
  void testGetWeek() {
    assertEquals(5, sale.getWeek());
  }

  @Test
  void testIsCommittedInitiallyFalse() {
    assertFalse(sale.isCommitted());
  }

  @Test
  void testCommitRemovesShareFromPortfolio() {
    assertTrue(player.getPortfolio().getShares().contains(share));
    sale.commit(player);
    assertFalse(player.getPortfolio().getShares().contains(share));
  }

  @Test
  void testCommitAddsMoneyToPlayer() {
    BigDecimal moneyBefore = player.getMoney();
    sale.commit(player);
    assertTrue(player.getMoney().compareTo(moneyBefore) > 0);
  }

  @Test
  void testCommitChangesCommittedStatus() {
    sale.commit(player);
    assertTrue(sale.isCommitted());
  }

  @Test
  void testCommitAddsTransactionToArchive() {
    sale.commit(player);
    assertTrue(player.getTransactionArchive().getSales(5).contains(sale));
  }

  @Test
  void testCommitedThrowsIfShareNotOwned() {
    player.getPortfolio().removeShare(share);
    assertThrows(IllegalArgumentException.class, () -> sale.commit(player));
  }


}
