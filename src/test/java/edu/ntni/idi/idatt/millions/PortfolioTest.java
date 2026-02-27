package edu.ntni.idi.idatt.millions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class PortfolioTest {
  private Portfolio portfolio;
  private Share share1;
  private Share share2;
  private Share share3;
  private Stock stock1;
  private Stock stock2;

  @BeforeEach
  public void setUp() {
    portfolio = new Portfolio();
    stock1 = new Stock("AAPL", "Apple Inc.", BigDecimal.valueOf(145.0));
    stock2 = new Stock("GOOGL", "Alphabet Inc.", BigDecimal.valueOf(145.0));
    share1 = new Share(stock1, BigDecimal.valueOf(10), BigDecimal.valueOf(150.0));
    share2 = new Share(stock1, BigDecimal.valueOf(5), BigDecimal.valueOf(155.0));
    share3 = new Share(stock2, BigDecimal.valueOf(20), BigDecimal.valueOf(2800.0));
  }

  @Test
  public void testAddShare() {
    portfolio.addShare(share1);
    assertEquals(1, portfolio.getShares().size());
    assertTrue(portfolio.contains(share1));
  }

  @Test
  public void testRemoveShare() {
    portfolio.addShare(share1);
    portfolio.removeShare(share1);
    assertEquals(0, portfolio.getShares().size());
    assertFalse(portfolio.contains(share1));
  }

  @Test
  public void testGetShare() {
    portfolio.addShare(share1);
    portfolio.addShare(share2);
  }
}
