package edu.ntni.idi.idatt.millions.model;

import edu.ntni.idi.idatt.millions.model.transaction.TransactionArchive;

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
    this.money = stratingMoney;
    this.portfolio = new Portfolio();
    this.transactionArchive = new TransactionArchive();
  }

  public String getName() {
    return name;
  }

  public BigDecimal getMoney() {
    return money;
  }

  public void addMoney(BigDecimal amount) {
    money = money.add(amount);
  }

  public void withdrawMoney(BigDecimal amount) {
    money = money.subtract(amount);
  }


  public Portfolio getPortfolio() {
    return portfolio;
  }

  public TransactionArchive getTransactionArchive() {
    return transactionArchive;
  }

}
