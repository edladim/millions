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
   * Verifies that a new player with no transactions starts as NOVICE.
   */
  @Test
  void getStatus_newPlayer_returnsNovice() {
    assertEquals(PlayerStatus.NOVICE, player.getStatus());
  }

  /**
   * Verifies that a player with at least 10 active weeks and 20% growth
   * is promoted to INVESTOR status.
   */
  @Test
  void getStatus_tenWeeksAndTwentyPercentGrowth_returnsInvestor() {
    Player investor = new Player("Investor", new BigDecimal("10000"));
    simulateActiveWeeks(investor, 10);
    investor.addMoney(new BigDecimal("2000"));

    assertEquals(PlayerStatus.INVESTOR, investor.getStatus());
  }

  /**
   * Verifies that a player with at least 20 active weeks and 100% growth
   * is promoted to SPECULATOR status.
   */
  @Test
  void getStatus_twentyWeeksAndHundredPercentGrowth_returnsSpeculator() {
    Player speculator = new Player("Speculator", new BigDecimal("10000"));
    simulateActiveWeeks(speculator, 20);
    speculator.addMoney(new BigDecimal("10000"));

    assertEquals(PlayerStatus.SPECULATOR, speculator.getStatus());
  }

  /**
   * Verifies that a player with enough weeks but insufficient growth
   * does not advance beyond NOVICE.
   */
  @Test
  void getStatus_tenWeeksButInsufficientGrowth_returnsNovice() {
    Player underperformer = new Player("Underperformer", new BigDecimal("10000"));
    simulateActiveWeeks(underperformer, 10);

    assertEquals(PlayerStatus.NOVICE, underperformer.getStatus());
  }

  /**
   * Verifies that a player with sufficient growth but too few weeks
   * does not advance to INVESTOR.
   */
  @Test
  void getStatus_twentyPercentGrowthButTooFewWeeks_returnsNovice() {
    player.addMoney(new BigDecimal("2000"));

    assertEquals(PlayerStatus.NOVICE, player.getStatus());
  }

  /**
   * Verifies that a player meeting INVESTOR requirements but not SPECULATOR
   * requirements is correctly assigned INVESTOR and not promoted further.
   */
  @Test
  void getStatus_investorRequirementsMet_doesNotPromoteToSpeculator() {
    Player investor = new Player("Investor", new BigDecimal("10000"));
    simulateActiveWeeks(investor, 10);
    investor.addMoney(new BigDecimal("2000")); // 20% growth, not 100%

    assertEquals(PlayerStatus.INVESTOR, investor.getStatus());
    assertNotEquals(PlayerStatus.SPECULATOR, investor.getStatus());
  }

  /**
   * Simulates a given number of distinct active trading weeks for a player
   * by adding one purchase transaction per week directly to the archive.
   *
   * @param player the player to simulate activity for
   * @param weeks  the number of distinct weeks to simulate
   */
  private void simulateActiveWeeks(Player player, int weeks) {
    for (int i = 0; i < weeks; i++) {
      player.updateHistoricalNetWorth();
    }
  }
}