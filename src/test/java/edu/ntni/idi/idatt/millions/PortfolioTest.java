package edu.ntni.idi.idatt.millions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

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
  public void testContainsShare() {
    portfolio.addShare(share1);
    assertTrue(portfolio.contains(share1));
    assertFalse(portfolio.contains(share2));
  }

  @Test
  public void testAddShare() {
    portfolio.addShare(share1);
    assertEquals(1, portfolio.getShares().size());
    assertTrue(portfolio.contains(share1));
  }

  @Test
  public void testAddMultipleShares() {
    portfolio.addShare(share1);
    portfolio.addShare(share2);
    portfolio.addShare(share3);
    assertEquals(3, portfolio.getShares().size());
  }

  @Test
  public void testRemoveShare() {
    portfolio.addShare(share1);
    portfolio.addShare(share2);
    assertTrue(portfolio.removeShare(share1));
    assertEquals(1, portfolio.getShares().size());
    assertFalse(portfolio.contains(share1));
  }

  @Test
  public void testRemoveNonExistingShare() {
    portfolio.addShare(share1);
    assertFalse(portfolio.removeShare(share2));
    assertEquals(1, portfolio.getShares().size());
  }

  @Test
  public void testGetShare() {
    portfolio.addShare(share1);
    portfolio.addShare(share2);
    List<Share> shares = portfolio.getShares();
    assertEquals(2, shares.size());
  }

  @Test
  public void testGetShareBySymbol() {
    portfolio.addShare(share1);
    portfolio.addShare(share2);
    portfolio.addShare(share3);

    List<Share> appleShares = portfolio.getSharesBySymbol("AAPL");
    assertEquals(2, appleShares.size());

    List<Share> googleShares = portfolio.getSharesBySymbol("GOOGL");
    assertEquals(1, googleShares.size());
  }

  @Test
  public void testGetSharesByNonExistentSymbol() {
    portfolio.addShare(share1);
    List<Share> shares = portfolio.getSharesBySymbol("MSFT");
    assertTrue(shares.isEmpty());
  }

  @Test
  public void testEmptyPortfolio() {
    assertTrue(portfolio.getShares().isEmpty());
    assertFalse(portfolio.contains(share1));
  }

}
