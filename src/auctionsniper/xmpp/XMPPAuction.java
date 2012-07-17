package auctionsniper.xmpp;

import auctionsniper.Auction;
import auctionsniper.AuctionEventListener;
import auctionsniper.util.Announcer;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

public class XMPPAuction implements Auction {
  public static final String JOIN_COMMAND_FORMAT = "SOLVersion: 1.1; Command: JOIN;";
  public static final String BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price %d;";

  private final Announcer<AuctionEventListener> auctionEventListeners = Announcer.to(AuctionEventListener.class);
  private final Chat chat;

  public XMPPAuction(XMPPConnection connection, String auctionJID) {
    AuctionMessageTranslator translator = translatorFor(connection);
    this.chat = connection.getChatManager().createChat(auctionJID, translator);
    addAuctionEventListener(chatDisconnectorFor(translator));
  }

  private AuctionEventListener chatDisconnectorFor(final AuctionMessageTranslator translator) {
    return new AuctionEventListener() {
      @Override
      public void auctionFailed() {
        chat.removeMessageListener(translator);
      }

      @Override
      public void currentPrice(int price, int increment, PriceSource priceSource) {
      } // Empty

      @Override
      public void auctionClosed() {
      } // Empty
    };
  }

  private AuctionMessageTranslator translatorFor(XMPPConnection connection) {
    return new AuctionMessageTranslator(connection.getUser(), auctionEventListeners.announce());
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
