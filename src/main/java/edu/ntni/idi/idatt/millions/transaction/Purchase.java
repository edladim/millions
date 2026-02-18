package edu.ntni.idi.idatt.millions.transaction;

import edu.ntni.idi.idatt.millions.Player;
import edu.ntni.idi.idatt.millions.calculator.PurchaseCalculator;
import edu.ntni.idi.idatt.millions.Share;

import java.math.BigDecimal;

public class Purchase extends Transaction{
  public Purchase(Share share, int week) {
    super(share, week, new PurchaseCalculator(share));
  }
  @Override
  public void commit(Player player) {
    BigDecimal total = getCalculator().calculateTotal();
    if (player.getMoney().compareTo(total) < 0 || isCommitted()) {
      throw new IllegalArgumentException("Transaction cannot be completed: insufficient funds or already committed");
    }
    player.withdrawMoney(getCalculator().calculateTotal());
    player.getPortfolio().addShare(getShare());
    player.getTransactionArchive().addTransaction(this);
    setCommitted(true);
  }
}
