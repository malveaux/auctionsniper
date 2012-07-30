package test.unit.auctionsniper;

import auctionsniper.*;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.States;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import static auctionsniper.AuctionEventListener.PriceSource;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(JMock.class)
public class AuctionSniperTest {
  private static final String ITEM_ID = "auction-54321";
  private final Mockery context = new Mockery();
  private final SniperListener sniperListener =
    context.mock(SniperListener.class);
  private final Auction auction = context.mock(Auction.class);
  private final AuctionSniper sniper = new AuctionSniper(ITEM_ID, auction, sniperListener);
  private final States sniperState = context.states("sniper");

  @Test
  public void reportsLostWhenAuctionClosesImmediately() {
    context.checking(new Expectations() {{
      one(sniperListener).sniperLost();
    }});

    sniper.auctionClosed();
  }

  @Test
  public void reportsLostIfAuctionClosesWhenBidding() {
    context.checking(new Expectations() {{
      ignoring(auction);
      allowing(sniperListener).sniperStateChanged(with(aSniperThatIs(SniperState.BIDDING)));
      then(sniperState.is("bidding"));

      atLeast(1).of(sniperListener).sniperLost();
      when(sniperState.is("bidding"));
    }});

    sniper.currentPrice(123, 45, PriceSource.FromOtherBidder);
    sniper.auctionClosed();
  }

  @Test
  public void bidsHigherAndReportsBiddingWhenNewPriceArrives() {
    final int price = 1001;
    final int increment = 25;
    final int bid = price + increment;

    context.checking(new Expectations() {{
      one(auction).bid(bid);
      atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, price, bid, SniperState.BIDDING));
    }});

    sniper.currentPrice(price, increment, PriceSource.FromOtherBidder);
  }

  @Test
  public void reportsIsWinningWhenCurrentPriceComesFromSniper() {
    context.checking(new Expectations() {{
      ignoring(auction);
      allowing(sniperListener).sniperStateChanged(with(aSniperThatIs(SniperState.BIDDING)));

      atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 135, 135, SniperState.WINNING));
    }});

    sniper.currentPrice(123, 12, PriceSource.FromOtherBidder);
    sniper.currentPrice(135, 45, PriceSource.FromSniper);
  }

  @Test
  public void reportsWonIfAuctionClosesWhenWinning() {
    context.checking(new Expectations() {{
      ignoring(auction);
      allowing(sniperListener).sniperStateChanged(with(aSniperThatIs(SniperState.WINNING)));
      then(sniperState.is("winning"));

      atLeast(1).of(sniperListener).sniperWon();
      when(sniperState.is("winning"));
    }});

    sniper.currentPrice(123, 45, PriceSource.FromSniper);
    sniper.auctionClosed();
  }

  private Matcher<SniperSnapshot> aSniperThatIs(final SniperState state) {
    return new FeatureMatcher<SniperSnapshot, SniperState>(equalTo(state), "sniper that is ", "was") {
      @Override
      protected SniperState featureValueOf(SniperSnapshot actual) {
        return actual.state;
      }
    };
  }
}
