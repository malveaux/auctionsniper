package test.unit.auctionsniper;

import auctionsniper.AuctionSniper;
import auctionsniper.PortfolioListener;
import auctionsniper.SniperPortfolio;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class SniperPortfolioTest {
  private final Mockery context = new Mockery();
  private final PortfolioListener listener = context.mock(PortfolioListener.class);
  SniperPortfolio portfolio = new SniperPortfolio();

  @Test
  public void notifiesListenersOfNewSnipers() {
    final AuctionSniper sniper = new AuctionSniper("item123", null);

    context.checking(new Expectations() {{
      one(listener).sniperAdded(sniper);
    }});

    portfolio.addPortfolioListener(listener);

    portfolio.addSniper(sniper);
  }

}
