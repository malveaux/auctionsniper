package auctionsniper.xmpp;

public class XMPPAuctionException extends Exception {
  public XMPPAuctionException(String message, Exception exception) {
    super(message, exception);
  }
}
