package test.endtoend.auctionsniper;

import auctionsniper.SniperState;
import auctionsniper.ui.MainWindow;

import static auctionsniper.Main.main;
import static auctionsniper.ui.SnipersTableModel.textFor;
import static test.endtoend.auctionsniper.FakeAuctionServer.AUCTION_RESOURCE;
import static test.endtoend.auctionsniper.FakeAuctionServer.XMPP_HOSTNAME;

public class ApplicationRunner {

  public static final String SNIPER_ID = "sniper";
  public static final String SNIPER_PASSWORD = "sniper";
  public static final String SNIPER_XMPP_ID = SNIPER_ID + "@" + XMPP_HOSTNAME + "/" + AUCTION_RESOURCE;

  private AuctionSniperDriver driver;
  private String itemId;

  public void startBiddingIn(final FakeAuctionServer auction) {
    itemId = auction.getItemId();

    Thread thread = new Thread("Test Application") {
      @Override
      public void run() {
        try {
          main(XMPP_HOSTNAME, SNIPER_ID, SNIPER_PASSWORD, auction.getItemId());
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };

    thread.setDaemon(true);
    thread.start();
    driver = new AuctionSniperDriver(1000);
    driver.hasTitle(MainWindow.APPLICATION_TITLE);
    driver.hasColumnTitles();
    driver.showsSniperStatus(textFor(SniperState.JOINING));
  }

  public void stop() {
    if (driver != null) {
      driver.dispose();
    }
  }

  public void showsSniperHasLostAuction() {
    driver.showsSniperStatus(textFor(SniperState.LOST));
  }

  public void hasShownSniperIsBidding(int lastPrice, int lastBid) {
    driver.showsSniperStatus(itemId, lastPrice, lastBid, textFor(SniperState.BIDDING));
  }

  public void hasShownSniperIsWinning(int winningBid) {
    driver.showsSniperStatus(itemId, winningBid, winningBid, textFor(SniperState.WINNING));
  }

  public void showsSniperHasWonAuction(int lastPrice) {
    driver.showsSniperStatus(itemId, lastPrice, lastPrice, textFor(SniperState.WON));
  }
}
