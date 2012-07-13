package auctionsniper;

public class AuctionSniper implements AuctionEventListener {
  private final String itemId;
  private final Auction auction;
  private final SniperListener sniperListener;
  private SniperSnapshot snapshot;

  public AuctionSniper(String itemId, Auction auction, SniperListener sniperListener) {
    this.itemId = itemId;
    this.auction = auction;
    this.sniperListener = sniperListener;
    snapshot = SniperSnapshot.joining(itemId);
  }

  @Override
  public void currentPrice(int price, int increment, PriceSource priceSource) {
    switch (priceSource) {
      case FromSniper:
        snapshot = snapshot.winning(price);
        break;
      case FromOtherBidder:
        int bid = price + increment;
        auction.bid(bid);
        snapshot = snapshot.bidding(price, bid);
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
    sniperListener.sniperStateChanged(snapshot);
  }
}
