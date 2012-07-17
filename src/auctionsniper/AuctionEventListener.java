package auctionsniper;

import java.util.EventListener;

public interface AuctionEventListener extends EventListener {
  enum PriceSource {
    FromSniper, FromOtherBidder;
  }

  void currentPrice(int price, int increment, PriceSource priceSource);

  void auctionClosed();

  void auctionFailed();
}
