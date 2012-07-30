package auctionsniper.xmpp;

import auctionsniper.Auction;
import auctionsniper.AuctionHouse;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

public class XMPPAuctionHouse implements AuctionHouse {
  public static final String AUCTION_RESOURCE = "Auction";
  public static final String ITEM_ID_AS_LOGIN = "auction-%s";
  public static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;

  private XMPPConnection connection;

  public XMPPAuctionHouse(XMPPConnection connection) {
    this.connection = connection;
  }

  public static XMPPAuctionHouse connect(String hostname, String username, String password) throws XMPPException {
    XMPPConnection connection = new XMPPConnection(hostname);
    connection.connect();
    connection.login(username, password, AUCTION_RESOURCE);

    return new XMPPAuctionHouse(connection);
  }

  @Override
  public Auction auctionFor(String itemId) {
    return new XMPPAuction(connection, auctionId(itemId));
  }

  private String auctionId(String itemId) {
    return String.format(AUCTION_ID_FORMAT, itemId, connection.getServiceName());
  }

  @Override
  public void disconnect() {
    connection.disconnect();
  }
}
