package edu.ntni.idi.idatt.millions.transaction;

import edu.ntni.idi.idatt.millions.Player;
import edu.ntni.idi.idatt.millions.Share;
import edu.ntni.idi.idatt.millions.calculator.TransactionCalculator;

public abstract class Transaction {
  private Share share;
  private int week;
  private TransactionCalculator calculator;
  private boolean committed;

  protected Transaction(Share share, int week, TransactionCalculator calculator) {
    this.share = share;
    this.week = week;
    this.calculator = calculator;
  }

  public Share getShare() {
    return share;
  }

  public int getWeek() {
    return week;
  }

  public TransactionCalculator getCalculator() {
    return calculator;
  }

  public void setCommitted(boolean committed) {
    this.committed = committed;
  }

  public boolean isCommitted() {
    return committed;
  }

  public void commit(Player player) {

  }
}
