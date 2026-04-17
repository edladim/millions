package edu.ntnu.idi.idatt.millions.observer;

import edu.ntnu.idi.idatt.millions.model.Player;
import edu.ntnu.idi.idatt.millions.model.Portfolio;
import edu.ntnu.idi.idatt.millions.view.pages.PortfolioView;

public class PortfolioObserverA implements PortfolioObserver {

  public final Portfolio portfolio;
  private final PortfolioView view;
  private final Player player;

  public PortfolioObserverA(Portfolio portfolio, PortfolioView view, Player player) {
    this.portfolio = portfolio;
    this.view = view;
    this.player = player;
  }

  @Override
  public void updatePortfolio() {
    view.refreshPortfolioData(player);
  }
}
