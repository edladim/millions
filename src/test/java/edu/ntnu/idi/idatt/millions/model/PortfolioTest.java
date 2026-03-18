package edu.ntnu.idi.idatt.millions.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Portfolio}.
 *
 * <p>The tests verify that the portfolio correctly manages shares,
 * enforces validation rules, and calculates portfolio values.</p>
 *
 * <p>The following behaviors are tested:</p>
 * <ul>
 *   <li>Adding and removing shares</li>
 *   <li>Checking if shares exist in the portfolio</li>
 *   <li>Filtering shares by stock symbol</li>
 *   <li>Defensive copying of returned collections</li>
 *   <li>Portfolio value calculations</li>
 *   <li>Validation and exceptional cases</li>
 * </ul>
 */
class PortfolioTest {
  private Portfolio portfolio;
  private Share share1;
  private Share share2;
  private Share share3;
  private Stock apple;
  private Stock google;

  /**
   * Creates a portfolio and sample shares before each test.
   */
  @BeforeEach
  void setUp() {
    portfolio = new Portfolio();

    apple = new Stock("AAPL", "Apple", new BigDecimal("200"));
    google = new Stock("GOOGL", "Google", new BigDecimal("100"));

    share1 = new Share(apple, new BigDecimal("10"), new BigDecimal("150"));
    share2 = new Share(apple, new BigDecimal("5"), new BigDecimal("140"));
    share3 = new Share(google, new BigDecimal("3"), new BigDecimal("80"));
  }

  /**
   * Verifies that shares can be added to the portfolio.
   */
  @Test
  void addShare_addsShareSuccessfully() {
    assertTrue(portfolio.addShare(share1));
    assertEquals(1, portfolio.size());
    assertTrue(portfolio.contains(share1));
  }

  /**
   * Ensures null shares cannot be added.
   */
  @Test
  void addShare_nullShare_throwsException() {
    assertThrows(NullPointerException.class,
        () -> portfolio.addShare(null));
  }

  /**
   * Verifies that shares can be removed from the portfolio.
   */
  @Test
  void removeShare_existingShare_removesShare() {
    portfolio.addShare(share1);

    assertTrue(portfolio.removeShare(share1));
    assertEquals(0, portfolio.size());
  }

  /**
   * Removing a non-existing share should return false.
   */
  @Test
  void removeShare_nonExistingShare_returnsFalse() {
    portfolio.addShare(share1);

    assertFalse(portfolio.removeShare(share2));
  }

  /**
   * Ensures null shares cannot be removed.
   */
  @Test
  void removeShare_nullShare_throwsException() {
    assertThrows(NullPointerException.class,
        () -> portfolio.removeShare(null));
  }

  /**
   * Verifies the contains method works correctly.
   */
  @Test
  void contains_returnsCorrectResult() {
    portfolio.addShare(share1);

    assertTrue(portfolio.contains(share1));
    assertFalse(portfolio.contains(share2));
  }

  /**
   * Ensures contains rejects null values.
   */
  @Test
  void contains_nullShare_throwsException() {
    assertThrows(NullPointerException.class,
        () -> portfolio.contains(null));
  }

  /**
   * Verifies that getShares returns all shares.
   */
  @Test
  void getShares_returnsAllShares() {
    portfolio.addShare(share1);
    portfolio.addShare(share2);

    List<Share> shares = portfolio.getShares();

    assertEquals(2, shares.size());
  }

  /**
   * Ensures the returned share list cannot be modified externally.
   */
  @Test
  void getShares_returnsUnmodifiableList() {
    portfolio.addShare(share1);

    List<Share> shares = portfolio.getShares();

    assertThrows(UnsupportedOperationException.class,
        () -> shares.add(share2));
  }

  /**
   * Verifies filtering shares by stock symbol.
   */
  @Test
  void getSharesBySymbol_returnsMatchingShares() {
    portfolio.addShare(share1);
    portfolio.addShare(share2);
    portfolio.addShare(share3);

    List<Share> appleShares = portfolio.getShares("AAPL");

    assertEquals(2, appleShares.size());
  }

  /**
   * Ensures filtering with a symbol that does not exist returns an empty list.
   */
  @Test
  void getSharesBySymbol_nonExistingSymbol_returnsEmptyList() {
    portfolio.addShare(share1);

    List<Share> result = portfolio.getShares("MSFT");

    assertTrue(result.isEmpty());
  }

  /**
   * Ensures invalid symbol input is rejected.
   */
  @Test
  void getSharesBySymbol_invalidSymbol_throwsException() {
    assertThrows(NullPointerException.class,
        () -> portfolio.getShares(null));

    assertThrows(IllegalArgumentException.class,
        () -> portfolio.getShares(" "));
  }

  /**
   * Verifies the constructor that accepts a list of shares.
   */
  @Test
  void constructor_withShares_initializesPortfolio() {
    Portfolio portfolio = new Portfolio(List.of(share1, share2));

    assertEquals(2, portfolio.size());
  }

  /**
   * Ensures constructor rejects null lists.
   */
  @Test
  void constructor_nullList_throwsException() {
    assertThrows(NullPointerException.class,
        () -> new Portfolio(null));
  }

  /**
   * Verifies calculation of total portfolio value.
   */
  @Test
  void getTotalValue_calculatesCorrectValue() {
    portfolio.addShare(share1);
    portfolio.addShare(share3);

    BigDecimal value = portfolio.getTotalValue();

    assertEquals(new BigDecimal("2300"), value);
  }

  /**
   * Verifies calculation of total invested capital.
   */
  @Test
  void getTotalInvestment_calculatesCorrectValue() {
    portfolio.addShare(share1);
    portfolio.addShare(share3);

    BigDecimal investment = portfolio.getTotalInvestment();

    assertEquals(new BigDecimal("1740"), investment);
  }

  /**
   * Verifies calculation of gain or loss.
   */
  @Test
  void getTotalGainOrLoss_calculatesCorrectValue() {
    portfolio.addShare(share1);
    portfolio.addShare(share3);

    BigDecimal gain = portfolio.getTotalGainOrLoss();

    assertEquals(new BigDecimal("560"), gain);
  }

  /**
   * Verifies correct net worth calculation
   */
  @Test
  void testGetNetWorth_returnsCorrectValue() {
    portfolio.addShare(share1);
    portfolio.addShare(share2);
    portfolio.addShare(share3);

    BigDecimal expectedNetWorth = new BigDecimal("3018.9");

    assertEquals(0, expectedNetWorth.compareTo(portfolio.getNetWorth()));
  }

}