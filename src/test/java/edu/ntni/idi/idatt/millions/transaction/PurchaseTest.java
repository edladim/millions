package edu.ntni.idi.idatt.millions.transaction;

import edu.ntni.idi.idatt.millions.Player;
import edu.ntni.idi.idatt.millions.Share;
import edu.ntni.idi.idatt.millions.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class PurchaseTest {
  private Share share;
  private Stock stock;
  private Player player;
  private Purchase purchase;

  @BeforeEach
  public void setUp() {
    stock = new Stock("AAPL", "Apple Inc.", BigDecimal.valueOf(150.0));
    share = new Share(stock, BigDecimal.valueOf(10), BigDecimal.valueOf(150.0));
    player = new Player("Test Player", new BigDecimal("10000"));
    purchase = new Purchase(share, 5);
  }

  @Test
  public void testGetShare() {
    assertEquals(share, purchase.getShare());
  }

  @Test
  public void testGetWeek() {
    assertEquals(5, purchase.getWeek());
  }

  @Test
  public void testGetCalculator() {
    assertNotNull(purchase.getCalculator());
  }

  @Test
  public void testIsCommittedInitiallyFalse() {
    assertFalse(purchase.isCommitted());
  }

  @Test
  public void testCommitAddsShareToPortfolio() {
    assertFalse(player.getPortfolio().getShares().contains(share));
    purchase.commit(player);
    assertTrue(player.getPortfolio().getShares().contains(share));
  }

  @Test
  public void testCommitDeductsMoneyFromPlayer() {
    BigDecimal moneyBefore = player.getMoney();
    purchase.commit(player);
    assertTrue(player.getMoney().compareTo(moneyBefore) < 0);
  }


}

