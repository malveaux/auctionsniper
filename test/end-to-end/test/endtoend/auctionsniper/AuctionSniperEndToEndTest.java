package test.endtoend.auctionsniper;

import org.junit.After;
import org.junit.Test;

public class AuctionSniperEndToEndTest {
  {
    System.setProperty("com.objogate.wl.keyboard", "US");
  }

  private final FakeAuctionServer auction = new FakeAuctionServer("item-54321");
  private final FakeAuctionServer auction2 = new FakeAuctionServer("item-65432");

  private final ApplicationRunner application = new ApplicationRunner();

  @Test
  public void sniperJoinsAuctionUntilAuctionCloses() throws Exception {
    auction.startSellingItem();
    application.startBiddingIn(auction);
    auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID);
    auction.announceClosed();
    application.showsSniperHasLostAuction(auction, 0, 0);
  }

  @Test
  public void sniperMakesAHigherBidButLoses() throws Exception {
    auction.startSellingItem();
    application.startBiddingIn(auction);
    auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID);

    auction.reportPrice(1000, 98, "other bidder");
    application.hasShownSniperIsBidding(auction, 1000, 1098);

    auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);

    auction.announceClosed();
    application.showsSniperHasLostAuction(auction, 1000, 1098);
  }

  @Test
  public void sniperWinsAnAuctionByBiddingHigher() throws Exception {
    auction.startSellingItem();
    application.startBiddingIn(auction);
    auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID);

    auction.reportPrice(1000, 98, "other bidder");
    application.hasShownSniperIsBidding(auction, 1000, 1098); // last price, last bid

    auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);

    auction.reportPrice(1098, 97, ApplicationRunner.SNIPER_XMPP_ID);
    application.hasShownSniperIsWinning(auction, 1098); // winning bid

    auction.announceClosed();
    application.showsSniperHasWonAuction(auction, 1098); // last price
  }

  @Test
  public void sniperBidsForMultipleItems() throws Exception {
    auction.startSellingItem();
    auction2.startSellingItem();

    application.startBiddingIn(auction, auction2);
    auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID);
    auction2.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID);

    auction.reportPrice(1000, 98, "other bidder");
    auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);

    auction2.reportPrice(500, 21, "other bidder");
    auction2.hasReceivedBid(521, ApplicationRunner.SNIPER_XMPP_ID);

    auction.reportPrice(1098, 97, ApplicationRunner.SNIPER_XMPP_ID);
    auction2.reportPrice(521, 22, ApplicationRunner.SNIPER_XMPP_ID);

    application.hasShownSniperIsWinning(auction, 1098);
    application.hasShownSniperIsWinning(auction2, 521);

    auction.announceClosed();
    auction2.announceClosed();

    application.showsSniperHasWonAuction(auction, 1098);
    application.showsSniperHasWonAuction(auction2, 521);
  }

  @Test
  public void sniperLosesAnAuctionWhenThePriceIsTooHigh() throws Exception {
    auction.startSellingItem();
    application.startBiddingWithStopPrice(auction, 1100);
    auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID);
    auction.reportPrice(1000, 98, "other bidder");
    application.hasShownSniperIsBidding(auction, 1000, 1098);

    auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);

    auction.reportPrice(1197, 10, "third party");
    application.hasShownSniperIsLosing(auction, 1197, 1098);

    auction.reportPrice(1207, 10, "fourth party");
    application.hasShownSniperIsLosing(auction, 1207, 1098);

    auction.announceClosed();
    application.showsSniperHasLostAuction(auction, 1207, 1098);
  }

  // Additional cleanup
  @After
  public void stopAuction() {
    auction.stop();
    auction2.stop();
  }

  @After
  public void stopApplication() {
    application.stop();
  }
}
