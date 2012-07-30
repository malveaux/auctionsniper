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

  public void startBiddingIn(final FakeAuctionServer... auctions) {
    startSniper();
    for (FakeAuctionServer auction : auctions) {
      openBiddingFor(auction, Integer.MAX_VALUE);
    }
  }

  private void startSniper() {
    Thread thread = new Thread("Test Application") {
      @Override
      public void run() {
        try {
          main(arguments());
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
  }

  protected static String[] arguments(FakeAuctionServer... auctions) {
    String[] arguments = new String[auctions.length + 3];
    arguments[0] = XMPP_HOSTNAME;
    arguments[1] = SNIPER_ID;
    arguments[2] = SNIPER_PASSWORD;
    for (int i = 0; i < auctions.length; ++i) {
      arguments[i + 3] = auctions[i].getItemId();
    }

    return arguments;
  }

  public void startBiddingWithStopPrice(FakeAuctionServer auction, int stopPrice) {
    startSniper();
    openBiddingFor(auction, stopPrice);
  }

  private void openBiddingFor(FakeAuctionServer auction, int stopPrice) {
    final String itemId = auction.getItemId();
    driver.startBiddingFor(itemId, stopPrice);
    driver.showsSniperStatus(itemId, 0, 0, textFor(SniperState.JOINING));
  }

  public void stop() {
    if (driver != null) {
      driver.dispose();
    }
  }

  public void hasShownSniperIsBidding(FakeAuctionServer auction, int lastPrice, int lastBid) {
    driver.showsSniperStatus(auction.getItemId(), lastPrice, lastBid, textFor(SniperState.BIDDING));
  }

  public void hasShownSniperIsWinning(FakeAuctionServer auction, int winningBid) {
    driver.showsSniperStatus(auction.getItemId(), winningBid, winningBid, textFor(SniperState.WINNING));
  }

  public void showsSniperHasWonAuction(FakeAuctionServer auction, int lastPrice) {
    driver.showsSniperStatus(auction.getItemId(), lastPrice, lastPrice, textFor(SniperState.WON));
  }

  public void hasShownSniperIsLosing(FakeAuctionServer auction, int lastPrice, int lastBid) {
    driver.showsSniperStatus(auction.getItemId(), lastPrice, lastBid, textFor(SniperState.LOSING));
  }

  public void showsSniperHasLostAuction(FakeAuctionServer auction, int lastPrice, int lastBid) {
    driver.showsSniperStatus(auction.getItemId(), lastPrice, lastBid, textFor(SniperState.LOST));
  }

  public void showSniperHasFailed(FakeAuctionServer auction) {
    driver.showsSniperStatus(auction.getItemId(), 0, 0, textFor(SniperState.FAILED));
  }

  public void reportsInvalidMessage(FakeAuctionServer auction, String brokenMessage) {
    //To change body of created methods use File | Settings | File Templates.
  }
}
