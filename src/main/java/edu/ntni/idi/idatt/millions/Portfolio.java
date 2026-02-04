package edu.ntni.idi.idatt.millions;

import java.util.List;

public class Portfolio {
  private List<Share> shares;

  public Portfolio(List<Share> shares) {
    this.shares = shares;
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

  public List<Share> getShares(String symbol) {
    return shares.stream().filter(share -> share.getStock().getSymbol().equals(symbol)).toList();
  }

  public boolean contains(Share share) {
    return shares.contains(share);
  }
}