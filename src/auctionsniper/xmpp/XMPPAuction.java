package auctionsniper.xmpp;

import auctionsniper.Auction;
import auctionsniper.AuctionEventListener;
import auctionsniper.util.Announcer;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

public class XMPPAuction implements Auction {
  public static final String AUCTION_RESOURCE = "Auction";
  public static final String ITEM_ID_AS_LOGIN = "auction-%s";
  public static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;
  public static final String JOIN_COMMAND_FORMAT = "SOLVersion: 1.1; Command: JOIN;";
  public static final String BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price %d;";

  private final Announcer<AuctionEventListener> auctionEventListeners = Announcer.to(AuctionEventListener.class);
  private final Chat chat;

  public XMPPAuction(XMPPConnection connection, String itemId) {
    chat = connection.getChatManager().createChat(auctionId(itemId, connection),
      new AuctionMessageTranslator(connection.getUser(), auctionEventListeners.announce()));
  }

  private static String auctionId(String itemId, XMPPConnection connection) {
    return String.format(AUCTION_ID_FORMAT, itemId, connection.getServiceName());
  }

  @Override
  public void bid(int amount) {
    try {
      chat.sendMessage(String.format(BID_COMMAND_FORMAT, amount));
    } catch (XMPPException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void join() {
    try {
      chat.sendMessage(JOIN_COMMAND_FORMAT);
    } catch (XMPPException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void addAuctionEventListener(AuctionEventListener listener) {
    auctionEventListeners.addListener(listener);
  }
}
