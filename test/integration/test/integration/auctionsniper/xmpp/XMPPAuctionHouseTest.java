package test.integration.auctionsniper.xmpp;

import auctionsniper.Auction;
import auctionsniper.AuctionEventListener;
import auctionsniper.xmpp.XMPPAuctionException;
import auctionsniper.xmpp.XMPPAuctionHouse;
import org.jivesoftware.smack.XMPPException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import test.endtoend.auctionsniper.ApplicationRunner;
import test.endtoend.auctionsniper.FakeAuctionServer;

import java.util.concurrent.CountDownLatch;

import static auctionsniper.UserRequestListener.Item;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;

public class XMPPAuctionHouseTest {
  private final FakeAuctionServer server = new FakeAuctionServer("item-54321");
  private XMPPAuctionHouse auctionHouse;

  @Before
  public void openConnection() throws XMPPAuctionException {
    auctionHouse = XMPPAuctionHouse.connect(FakeAuctionServer.XMPP_HOSTNAME, ApplicationRunner.SNIPER_ID, ApplicationRunner.SNIPER_PASSWORD);
  }

  @After
  public void closeConnection() {
    auctionHouse.disconnect();
  }

  @Before
  public void startAuction() throws XMPPException {
    server.startSellingItem();
  }

  @Test
  public void receivesEventsFromAuctionServerAfterJoining() throws Exception {
    CountDownLatch auctionWasClosed = new CountDownLatch(1);

    Auction auction = auctionHouse.auctionFor(new Item(server.getItemId(), 567));
    auction.addAuctionEventListener(auctionClosedListener(auctionWasClosed));

    auction.join();

    server.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID);
    server.announceClosed();

    assertTrue("should have been closed", auctionWasClosed.await(2, SECONDS));
  }

  private AuctionEventListener auctionClosedListener(final CountDownLatch auctionWasClosed) {
    return new AuctionEventListener() {
      @Override
      public void auctionClosed() {
        auctionWasClosed.countDown();
      }

      @Override
      public void auctionFailed() {
        // Not implemented
      }

      @Override
      public void currentPrice(int price, int increment, PriceSource priceSource) {
        // Not implemented
      }
    };
  }


}
