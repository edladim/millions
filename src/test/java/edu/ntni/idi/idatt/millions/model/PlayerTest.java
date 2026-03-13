package edu.ntni.idi.idatt.millions.model;

import edu.ntni.idi.idatt.millions.model.transaction.TransactionArchive;
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

    BigDecimal expectedNetWorth = new BigDecimal("13009.00");

    assertEquals(expectedNetWorth, player.getNetWorth());
  }

}