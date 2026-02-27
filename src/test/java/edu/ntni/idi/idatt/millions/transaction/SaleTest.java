package edu.ntni.idi.idatt.millions.transaction;

import edu.ntni.idi.idatt.millions.Player;
import edu.ntni.idi.idatt.millions.Share;
import edu.ntni.idi.idatt.millions.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
  public void testGetShare() {
    assertEquals(share, sale.getShare());
  }

  @Test
  public void testGetCalculator() {
    assertNotNull(sale.getCalculator());
  }


}
