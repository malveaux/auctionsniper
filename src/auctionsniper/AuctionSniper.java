package auctionsniper;

import auctionsniper.util.Announcer;

import static auctionsniper.UserRequestListener.Item;

public class AuctionSniper implements AuctionEventListener {
  private final Item item;
  private final Auction auction;
  private final Announcer<SniperListener> listeners = Announcer.to(SniperListener.class);

  private SniperSnapshot snapshot;

  public AuctionSniper(Item item, Auction auction) {
    this.item = item;
    this.auction = auction;
    snapshot = SniperSnapshot.joining(item.identifier);
  }

  @Override
  public void currentPrice(int price, int increment, PriceSource priceSource) {
    switch (priceSource) {
      case FromSniper:
        snapshot = snapshot.winning(price);
        break;
      case FromOtherBidder:
        int bid = price + increment;
        if (item.allowsBid(bid)) {
          auction.bid(bid);
          snapshot = snapshot.bidding(price, bid);
        } else {
          snapshot = snapshot.losing(price);
        }

        break;
    }
    notifyChange();
  }

  @Override
  public void auctionClosed() {
    snapshot = snapshot.closed();
    notifyChange();
  }

  private void notifyChange() {
    listeners.announce().sniperStateChanged(snapshot);
  }

  public SniperSnapshot getSnapshot() {
    return snapshot;
  }

  public void addSniperListener(SniperListener listener) {
    listeners.addListener(listener);
  }
}
