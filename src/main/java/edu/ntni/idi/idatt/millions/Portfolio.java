package edu.ntni.idi.idatt.millions;

import java.util.ArrayList;
import java.util.List;

public class Portfolio {
  private List<Share> shares;

  public Portfolio() {
    this.shares = new ArrayList<>();
  }

  public boolean addShare(Share share) {
    return shares.add(share);
  }

  public boolean removeShare(Share share) {
    return shares.remove(share);
  }

  public List<Share> getShares() {
    return shares;
  }

  public List<Share> getSharesBySymbol(String symbol) {
    return shares.stream().filter(share -> share.getStock().getSymbol().equals(symbol)).toList();
  }

  public boolean contains(Share share) {
    return shares.contains(share);
  }
}