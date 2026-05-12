package edu.ntnu.idi.idatt.millions.model;

import edu.ntnu.idi.idatt.millions.model.transaction.TransactionArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Player}.
 *
 * <p>The tests verify correct behavior of the player model including:</p>
 *
 * <ul>
 *   <li>Initialization of player state</li>
 *   <li>Money balance operations</li>
 *   <li>Validation of constructor arguments</li>
 *   <li>Validation of money operations</li>
 *   <li>Correct creation of portfolio and transaction archive</li>
 * </ul>
 *
 * <p>All tests use {@link BigDecimal#compareTo(BigDecimal)} to avoid
 * scale issues when comparing monetary values.</p>
 */
class PlayerTest {

  private Player player;
  private BigDecimal startingMoney;

  /**
   * Creates a fresh player instance before each test.
   */
  @BeforeEach
  void setup() {
    startingMoney = new BigDecimal("10000");
    player = new Player("Test Player", startingMoney);
  }

  /**
   * Verifies that the player name is stored correctly.
   */
  @Test
  void getName_returnsCorrectName() {
    assertEquals("Test Player", player.getName());
  }

  /**
   * Verifies that the player starts with the correct balance.
   */
  @Test
  void getMoney_initialBalanceIsStartingMoney() {
    assertEquals(0, startingMoney.compareTo(player.getMoney()));
  }

  /**
   * Verifies that money can be added to the player's balance.
   */
  @Test
  void addMoney_increasesBalance() {
    player.addMoney(new BigDecimal("500"));
    BigDecimal expected = new BigDecimal("10500");
    assertEquals(0, expected.compareTo(player.getMoney()));
  }

  /**
   * Verifies that money can be withdrawn from the player's balance.
   */
  @Test
  void withdrawMoney_decreasesBalance() {
    player.withdrawMoney(new BigDecimal("2000"));
    BigDecimal expected = new BigDecimal("8000");
    assertEquals(0, expected.compareTo(player.getMoney()));
  }

  /**
   * Verifies that withdrawing more money than available throws an exception.
   */
  @Test
  void withdrawMoney_insufficientFunds_throwsException() {
    assertThrows(IllegalStateException.class,
        () -> player.withdrawMoney(new BigDecimal("20000")));
  }

  /**
   * Verifies that adding a negative amount is not allowed.
   */
  @Test
  void addMoney_negativeAmount_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> player.addMoney(new BigDecimal("-10")));
  }

  /**
   * Verifies that withdrawing a negative amount is not allowed.
   */
  @Test
  void withdrawMoney_negativeAmount_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> player.withdrawMoney(new BigDecimal("-10")));
  }

  /**
   * Verifies that the player's portfolio is created automatically.
   */
  @Test
  void getPortfolio_returnsPortfolioInstance() {
    assertNotNull(player.getPortfolio());
    assertInstanceOf(Portfolio.class, player.getPortfolio());
  }

  /**
   * Verifies that the transaction archive is created automatically.
   */
  @Test
  void getTransactionArchive_returnsArchiveInstance() {
    assertNotNull(player.getTransactionArchive());
    assertInstanceOf(TransactionArchive.class, player.getTransactionArchive());
  }

  /**
   * Verifies that the constructor rejects null names.
   */
  @Test
  void constructor_nullName_throwsException() {
    assertThrows(NullPointerException.class,
        () -> new Player(null, startingMoney));
  }

  /**
   * Verifies that the constructor rejects blank names.
   */
  @Test
  void constructor_blankName_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> new Player("   ", startingMoney));
  }

  /**
   * Verifies that the constructor rejects null starting money.
   */
  @Test
  void constructor_nullStartingMoney_throwsException() {
    assertThrows(NullPointerException.class,
        () -> new Player("Test", null));
  }

  /**
   * Verifies that the constructor rejects negative starting money.
   */
  @Test
  void constructor_negativeStartingMoney_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> new Player("Test", new BigDecimal("-100")));
  }

  /**
   * Verifies correct net worth calculation
   */
  @Test
  void testGetNetWorth_returnsCorrectAmount() {
    Stock apple = new Stock("AAPL", "Apple", new BigDecimal("200"));
    Stock google = new Stock("GOOGL", "Google", new BigDecimal("100"));
    Share share1 = new Share(apple, new BigDecimal("10"), new BigDecimal("150"));
    Share share2 = new Share(apple, new BigDecimal("5"), new BigDecimal("140"));
    Share share3 = new Share(google, new BigDecimal("3"), new BigDecimal("80"));

    player.getPortfolio().addShare(share1);
    player.getPortfolio().addShare(share2);
    player.getPortfolio().addShare(share3);

    BigDecimal expectedNetWorth = new BigDecimal("13018.9");

    assertEquals(0, expectedNetWorth.compareTo(player.getNetWorth()));
  }

  /**
   * Verifies that profit is zero when net worth equals starting capital.
   */
  @Test
  void getProfit_noChange_returnsZero() {
    assertEquals(0, BigDecimal.ZERO.compareTo(player.getProfit()));
  }

  /**
   * Verifies that profit is positive when net worth exceeds starting capital.
   */
  @Test
  void getProfit_moneyAdded_returnsPositiveProfit() {
    player.addMoney(new BigDecimal("500"));
    assertEquals(0, new BigDecimal("500").compareTo(player.getProfit()));
  }

  /**
   * Verifies that profit is negative when net worth is below starting capital.
   */
  @Test
  void getProfit_moneyWithdrawn_returnsNegativeProfit() {
    player.withdrawMoney(new BigDecimal("2000"));
    assertEquals(0, new BigDecimal("-2000").compareTo(player.getProfit()));
  }

  /**
   * Verifies that the return rate is zero when net worth equals starting capital.
   */
  @Test
  void getReturnRate_noChange_returnsZero() {
    assertEquals(0, BigDecimal.ZERO.compareTo(player.getReturnRate()));
  }

  /**
   * Verifies that the return rate is calculated correctly for a known gain.
   * A gain of 2000 on 10000 starting capital should yield a 20% return rate (0.20).
   */
  @Test
  void getReturnRate_twentyPercentGain_returnsCorrectRate() {
    player.addMoney(new BigDecimal("2000"));
    assertEquals(0, new BigDecimal("0.20").compareTo(player.getReturnRate()));
  }

  /**
   * Verifies that the return rate is negative when the player has lost value.
   */
  @Test
  void getReturnRate_loss_returnsNegativeRate() {
    player.withdrawMoney(new BigDecimal("5000"));
    assertTrue(player.getReturnRate().compareTo(BigDecimal.ZERO) < 0);
  }

  /**
   * Verifies that the return rate is zero when starting money is zero,
   * guarding against division by zero.
   */
  @Test
  void getReturnRate_zeroStartingMoney_returnsZero() {
    Player broke = new Player("Broke", BigDecimal.ZERO);
    assertEquals(0, BigDecimal.ZERO.compareTo(broke.getReturnRate()));
  }

  /**
   * Verifies that a player with no transactions has zero active weeks.
   */
  @Test
  void getActiveWeeks_noTransactions_returnsZero() {
    assertEquals(0, player.getActiveWeeks());
  }

  /**
   * Verifies status assignment for each return-rate band.
   */
  @Test
  void getStatus_excellentGrowth_returnsBernardMadoff() {
    player.addMoney(new BigDecimal("20000")); // +200%
    assertEquals(PlayerStatus.BERNARD_MADOFF, player.getStatus());
  }

  @Test
  void getStatus_goodGrowth_returnsRayDailo() {
    player.addMoney(new BigDecimal("10000")); // +100%
    assertEquals(PlayerStatus.RAY_DAILO, player.getStatus());
  }

  @Test
  void getStatus_alrightGrowth_returnsInvestor() {
    player.addMoney(new BigDecimal("2000")); // +20%
    assertEquals(PlayerStatus.INVESTOR, player.getStatus());
  }

  @Test
  void getStatus_averageBand_returnsAverageJoe() {
    player.withdrawMoney(new BigDecimal("1000")); // -10%
    assertEquals(PlayerStatus.AVERAGE_JOE, player.getStatus());
  }

  @Test
  void getStatus_badBand_returnsMaxMinus() {
    player.withdrawMoney(new BigDecimal("3000")); // -30%
    assertEquals(PlayerStatus.MAX_MINUS, player.getStatus());
  }

  @Test
  void getStatus_worstBand_returnsBuyHighBjorn() {
    player.withdrawMoney(new BigDecimal("6000")); // -60%
    assertEquals(PlayerStatus.BUY_HIGH_BJORN, player.getStatus());
  }
}