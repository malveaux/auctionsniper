package test.unit.auctionsniper;

import auctionsniper.*;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.States;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static auctionsniper.AuctionEventListener.PriceSource;
import static auctionsniper.SniperState.*;
import static auctionsniper.UserRequestListener.Item;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(JMock.class)
public class AuctionSniperTest {
  private static final String ITEM_ID = "auction-54321";
  public static final Item ITEM = new Item(ITEM_ID, 1234);
  private final Mockery context = new Mockery();
  private final SniperListener sniperListener =
    context.mock(SniperListener.class);
  private final Auction auction = context.mock(Auction.class);
  private final AuctionSniper sniper = new AuctionSniper(ITEM, auction);
  private final States sniperState = context.states("sniper");

  @Before
  public void attachListener() {
    sniper.addSniperListener(sniperListener);
  }

  @Test
  public void reportsLostWhenAuctionClosesImmediately() {
    context.checking(new Expectations() {{
      one(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 0, 0, LOST));
    }});
    sniper.auctionClosed();
  }

  @Test
  public void reportsLostIfAuctionClosesWhenBidding() {
    context.checking(new Expectations() {{
      ignoring(auction);
      allowing(sniperListener).sniperStateChanged(with(aSniperThatIs(BIDDING)));
      then(sniperState.is("bidding"));

      atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 123, 168, LOST));
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
      atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, price, bid, BIDDING));
    }});
    sniper.currentPrice(price, increment, PriceSource.FromOtherBidder);
  }

  @Test
  public void reportsIsWinningWhenCurrentPriceComesFromSniper() {
    context.checking(new Expectations() {{
      ignoring(auction);
      allowing(sniperListener).sniperStateChanged(with(aSniperThatIs(BIDDING)));

      atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 135, 135, WINNING));
    }});
    sniper.currentPrice(123, 12, PriceSource.FromOtherBidder);
    sniper.currentPrice(135, 45, PriceSource.FromSniper);
  }

  @Test
  public void reportsWonIfAuctionClosesWhenWinning() {
    context.checking(new Expectations() {{
      ignoring(auction);
      allowing(sniperListener).sniperStateChanged(with(aSniperThatIs(WINNING)));
      then(sniperState.is("winning"));

      atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 135, 0, WON));
      when(sniperState.is("winning"));
    }});
    sniper.currentPrice(135, 45, PriceSource.FromSniper);
    sniper.auctionClosed();
  }

  @Test
  public void doesNotBidAndReportsLosingIfSubsequentPriceIsAboveStopPrice() {
    allowingSniperBidding();
    context.checking(new Expectations() {{
      int bid = 123 + 45;
      allowing(auction).bid(bid);
      atLeast(1).of(sniperListener).sniperStateChanged(
        new SniperSnapshot(ITEM_ID, 2345, bid, LOSING));
      when(sniperState.is("bidding"));
    }});
    sniper.currentPrice(123, 45, PriceSource.FromOtherBidder);
    sniper.currentPrice(2345, 25, PriceSource.FromOtherBidder);
  }

  private void allowingSniperBidding() {
    context.checking(new Expectations() {{
      allowing(sniperListener).sniperStateChanged(with(aSniperThatIs(BIDDING)));
      then(sniperState.is("bidding"));
    }});
  }

  @Test
  public void doesNotBidAndReportsLosingIfFirstPriceIsAboveStopPrice() {
    final int price = 1233;
    final int increment = 25;

    context.checking(new Expectations() {{
      atLeast(1).of(sniperListener).sniperStateChanged(
        new SniperSnapshot(ITEM_ID, price, 0, LOSING));
    }});
    sniper.currentPrice(price, increment, PriceSource.FromOtherBidder);
  }

  @Test
  public void reportsLostIfAuctionClosesWhenLosing() {
    allowingSniperLosing();
    context.checking(new Expectations() {{
      atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 1230, 0, LOST));
      when(sniperState.is("losing"));
    }});
    sniper.currentPrice(1230, 456, PriceSource.FromOtherBidder);
    sniper.auctionClosed();
  }

  private void allowingSniperLosing() {
    context.checking(new Expectations() {{
      allowing(sniperListener).sniperStateChanged(with(aSniperThatIs(LOSING)));
      then(sniperState.is("losing"));
    }});
  }

  @Test
  public void continuesToBeLosingOnceStopPriceHasBeenReached() {
    final Sequence states = context.sequence("sniper states");
    final int price1 = 1233;
    final int price2 = 1258;

    context.checking(new Expectations() {{
      atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, price1, 0, LOSING));
      inSequence(states);
      atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, price2, 0, LOSING));
      inSequence(states);
    }});
    sniper.currentPrice(price1, 25, PriceSource.FromOtherBidder);
    sniper.currentPrice(price2, 25, PriceSource.FromOtherBidder);
  }

  @Test
  public void doesNotBidAndReportsLosingIfPriceAfterWinningIsAboveStopPrice() {
    final int price = 1233;
    final int increment = 25;

    allowingSniperBidding();
    allowingSniperWinning();
    context.checking(new Expectations() {{
      int bid = 123 + 45;
      allowing(auction).bid(bid);

      atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, price, bid, LOSING));
      when(sniperState.is("winning"));
    }});
    sniper.currentPrice(123, 45, PriceSource.FromOtherBidder);
    sniper.currentPrice(168, 45, PriceSource.FromSniper);
    sniper.currentPrice(price, increment, PriceSource.FromOtherBidder);
  }

  private void allowingSniperWinning() {
    context.checking(new Expectations() {{
      allowing(sniperListener).sniperStateChanged(with(aSniperThatIs(WINNING)));
      then(sniperState.is("winning"));
    }});
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
