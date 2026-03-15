package edu.ntni.idi.idatt.millions.model;

import edu.ntni.idi.idatt.millions.model.transaction.TransactionArchive;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a player in the trading game.
 *
 * <p>A player has a name, a starting amount of money and a current
 * balance that changes as transactions are performed.</p>
 *
 * <p>The player also owns a {@link Portfolio} containing shares
 * and a {@link TransactionArchive} storing completed transactions.</p>
 */
public final class Player {

  private static final BigDecimal INVESTOR_GROWTH_REQUIREMENT = new BigDecimal("0.20");
  private static final BigDecimal SPECULATOR_GROWTH_REQUIREMENT = BigDecimal.ONE;

  private final String name;
  private final BigDecimal startingMoney;
  private BigDecimal money;
  private final Portfolio portfolio;
  private final TransactionArchive transactionArchive;

  /**
   * Creates a new player.
   *
   * <p>The player starts with a given amount of money which becomes both
   * the starting capital and the initial balance.</p>
   *
   * @param name the player's name
   * @param startingMoney the initial capital
   *
   * @throws NullPointerException if name or startingMoney is null
   * @throws IllegalArgumentException if name is blank or startingMoney is negative
   */
  public Player(String name, BigDecimal startingMoney) {
    this.name = validateName(name);
    this.startingMoney = validateAmount(startingMoney);
    this.money = startingMoney;
    this.portfolio = new Portfolio();
    this.transactionArchive = new TransactionArchive();
  }

  /**
   * Returns the player's name.
   *
   * @return the player name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the player's current money balance.
   *
   * @return the current balance
   */
  public BigDecimal getMoney() {
    return money;
  }

  /**
   * Adds money to the player's balance.
   *
   * @param amount the amount to add
   *
   * @throws NullPointerException if amount is null
   * @throws IllegalArgumentException if amount is negative
   */
  public void addMoney(BigDecimal amount) {
    amount = validateAmount(amount);
    money = money.add(amount);
  }

  /**
   * Withdraws money from the player's balance.
   *
   * @param amount the amount to withdraw
   *
   * @throws NullPointerException if amount is null
   * @throws IllegalArgumentException if amount is negative
   * @throws IllegalStateException if the player has insufficient funds
   */
  public void withdrawMoney(BigDecimal amount) {
    amount = validateAmount(amount);
    if (money.compareTo(amount) < 0) {
      throw new IllegalStateException("Insufficient funds");
    }
    money = money.subtract(amount);
  }

  /**
   * Returns the player's portfolio.
   *
   * @return the portfolio
   */
  public Portfolio getPortfolio() {
    return portfolio;
  }

  /**
   * Returns the player's transaction archive.
   *
   * @return the transaction archive
   */
  public TransactionArchive getTransactionArchive() {
    return transactionArchive;
  }

  private static String validateName(String name) {
    name = Objects.requireNonNull(name, "Name cannot be null").trim();
    if (name.isEmpty()) {
      throw new IllegalArgumentException("Name cannot be blank");
    }
    return name;
  }

  private static BigDecimal validateAmount(BigDecimal amount) {
    Objects.requireNonNull(amount, "Amount cannot be null");
    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Amount cannot be negative");
    }
    return amount;
  }

  /**
   * Calculates the player's total net worth.
   *
   * <p>The net worth is the sum of the portfolio net worth
   * (amount received after selling all shares) and current cash balance.</p>
   *
   * @return the total net worth, never null
   */
  public BigDecimal getNetWorth() {
    return portfolio.getNetWorth().add(money);
  }


  /**
   * Calculates the player's profit relative to the starting capital.
   *
   * <p>A positive value means the player has increased net worth,
   * while a negative value means the player has lost value overall.</p>
   *
   * @return the profit compared to starting capital, never null
   */
  public BigDecimal getProfit() {
    return getNetWorth().subtract(startingMoney);
  }

  /**
   * Calculates the player's growth rate relative to the starting capital.
   *
   * <p>The growth rate is expressed as a decimal value where
   * {@code 0.20} means 20% growth and {@code 1.00} means 100% growth.</p>
   *
   * @return the growth rate, never null
   */
  public BigDecimal getReturnRate() {
    if (startingMoney.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }

    return getProfit().divide(startingMoney, 10, java.math.RoundingMode.HALF_UP);
  }

  /**
   * Returns the number of distinct weeks in which the player has traded.
   *
   * <p>A week is counted if the player has completed at least one
   * transaction during that week.</p>
   *
   * @return the number of active trading weeks
   */
  public int getActiveWeeks() {
    return transactionArchive.countDistinctWeeks();
  }
}