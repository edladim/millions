package edu.ntni.idi.idatt.millions.transaction;

import edu.ntni.idi.idatt.millions.Player;
import edu.ntni.idi.idatt.millions.calculator.PurchaseCalculator;
import edu.ntni.idi.idatt.millions.Share;

public class Purchase extends Transaction{
  public Purchase(Share share, int week) {
    super(share, week, new PurchaseCalculator());
  }
  @Override
  public void commit(Player player) {

  }
}
