package edu.ntni.idi.idatt.millions.transaction;

import edu.ntni.idi.idatt.millions.Player;
import edu.ntni.idi.idatt.millions.Share;
import edu.ntni.idi.idatt.millions.TransactionCalculator;

public class Sale extends Transaction {

  public Sale(Share share, int week) {
    super(share, week, new SaleCalculator());
  }

  @Override
  public void commit(Player player) {

  }
}