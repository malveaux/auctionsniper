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

import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(JMock.class)
public class SniperLauncherTest {
  private Mockery context = new Mockery();
  private final States auctionState = context.states("auction state")
    .startsAs("not joined");
  private final AuctionHouse auctionHouse = context.mock(AuctionHouse.class);
  private final Auction auction = context.mock(Auction.class);
  private final SniperCollector sniperCollector = context.mock(SniperCollector.class);
  private SniperLauncher launcher = new SniperLauncher(auctionHouse, sniperCollector);

  @Test
  public void addsNewSniperToCollectorAndThenJoinsAuction() {
    final UserRequestListener.Item item = new UserRequestListener.Item("item123", 789);
    context.checking(new Expectations() {{
      allowing(auctionHouse).auctionFor(item);
      will(returnValue(auction));

      oneOf(auction).addAuctionEventListener(with(sniperForItem(item)));
      when(auctionState.is("not joined"));
      oneOf(sniperCollector).addSniper(with(sniperForItem(item)));
      when(auctionState.is("not joined"));

      one(auction).join();
      then(auctionState.is("joined"));
    }});

    launcher.joinAuction(item);
  }

  private Matcher<AuctionSniper> sniperForItem(UserRequestListener.Item item) {
    return new FeatureMatcher<AuctionSniper, String>(equalTo(item.identifier), "sniper with item id", "item") {
      @Override
      protected String featureValueOf(AuctionSniper actual) {
        return actual.getSnapshot().itemId;
      }
    };
  }
}
