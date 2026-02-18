package edu.ntni.idi.idatt.millions;

import java.math.BigDecimal;

public class Player {
  private String name;
  private BigDecimal stratingMoney;
  private BigDecimal money;
  private Portfolio portfolio;
  private TransactionArchive transactionArchive;

  public Player(String name, BigDecimal stratingMoney) {
    this.name = name;
    this.stratingMoney = stratingMoney;
    this.portfolio = new Portfolio();
    this.transactionArchive = new TransactionArchive();
  }

  public String getName() {
    return name;
  }

  public BigDecimal getMoney() {
    return money;
  }

  public void addMoney(BigDecimal money) {
    this.money = this.money.add(money);
  }

  public void withdrawMoney(BigDecimal amount) {

  }


  public Portfolio getPortfolio() {
    return portfolio;
  }

  public TransactionArchive getTransactionArchive() {
    return transactionArchive;
  }

}
