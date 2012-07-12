package test.endtoend.auctionsniper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AuctionSniperEndToEndTest {
  private final FakeAuctionServer auction = new FakeAuctionServer("item-54321");
  private final ApplicationRunner application = new ApplicationRunner();

  @Before
  public void setupKeyboard() {
    System.setProperty("com.objogate.wl.keyboard", "US");
  }

  @Test
  public void sniperJoinsAuctionUntilAuctionCloses() throws Exception {
    auction.startSellingItem();
    application.startBiddingIn(auction);
    auction.hasReceivedJoinRequestFromSniper();
    auction.announceClosed();
    application.showsSniperHasLostAuction();
  }

  // Additional cleanup
  @After
  public void stopAuction() {
    auction.stop();
  }

  @After
  public void stopApplication() {
    application.stop();
  }
}
