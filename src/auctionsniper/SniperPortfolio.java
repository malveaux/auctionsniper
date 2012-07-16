package auctionsniper;

import auctionsniper.util.Announcer;

import java.util.ArrayList;

public class SniperPortfolio implements SniperCollector {
  private final Announcer<PortfolioListener> announcer = Announcer.to(PortfolioListener.class);
  private ArrayList<AuctionSniper> snipers = new ArrayList<AuctionSniper>();

  public void addSniper(AuctionSniper sniper) {
    snipers.add(sniper);
    announcer.announce().sniperAdded(sniper);
  }

  public void addPortfolioListener(PortfolioListener listener) {
    announcer.addListener(listener);
  }
}
