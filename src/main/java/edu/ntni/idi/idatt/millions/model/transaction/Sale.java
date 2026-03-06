package edu.ntni.idi.idatt.millions.model.transaction;

import edu.ntni.idi.idatt.millions.model.Player;
import edu.ntni.idi.idatt.millions.model.Share;

public class Sale extends Transaction {

  public Sale(Share share, int week) {
    super(share, week, new SaleCalculator(share));
  }

  @Override
  public void commit(Player player) {
    if (!player.getPortfolio().getShares().contains(this.getShare()) || isCommitted()) {
      throw new IllegalArgumentException("Player does not own enough shares, or transaction already committed");
    }
    player.addMoney(getCalculator().calculateTotal());
    player.getPortfolio().removeShare(this.getShare());
    player.getTransactionArchive().add(this);
    committed = true;
  }
}