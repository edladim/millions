package edu.ntni.idi.idatt.millions.model.transaction;

import edu.ntni.idi.idatt.millions.model.Player;
import edu.ntni.idi.idatt.millions.model.Share;

public abstract class Transaction {
  private Share share;
  private int week;
  private TransactionCalculator calculator;
  protected boolean committed;

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

  public boolean isCommitted() {
    return committed;
  }

  public void commit(Player player) {

  }
}
