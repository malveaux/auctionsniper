package test.endtoend.auctionsniper;

import auctionsniper.xmpp.XMPPAuction;
import org.hamcrest.Matcher;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;

public class FakeAuctionServer {
  public static final String ITEM_ID_AS_LOGIN = "auction-%s";
  public static final String AUCTION_RESOURCE = "Auction";
  public static final String XMPP_HOSTNAME = "localhost";
  private static final String AUCTION_PASSWORD = "auction";

  private final SingleMessageListener messageListener = new SingleMessageListener();

  private final String itemId;
  private final XMPPConnection connection;
  private Chat currentChat;

  public FakeAuctionServer(String itemId) {
    this.itemId = itemId;
    this.connection = new XMPPConnection(XMPP_HOSTNAME);
  }

  public void startSellingItem() throws XMPPException {
    connection.connect();
    connection.login(format(ITEM_ID_AS_LOGIN, itemId), AUCTION_PASSWORD, AUCTION_RESOURCE);
    connection.getChatManager().addChatListener(new ChatManagerListener() {
      @Override
      public void chatCreated(Chat chat, boolean b) {
        currentChat = chat;
        chat.addMessageListener(messageListener);
      }
    });
  }

  public String getItemId() {
    return itemId;
  }

  public void hasReceivedJoinRequestFromSniper(String sniperId) throws InterruptedException {
    messageListener.receivesAMessageMatching(sniperId, equalTo(XMPPAuction.JOIN_COMMAND_FORMAT));
  }

  public void announceClosed() throws XMPPException {
    currentChat.sendMessage("SOLVersion: 1.1; Event: CLOSE;");
  }

  public void stop() {
    connection.disconnect();
  }

  public void reportPrice(int price, int increment, String bidder) throws XMPPException {
    currentChat.sendMessage(
      String.format("SOLVersion: 1.1; Event: PRICE; "
        + "CurrentPrice: %d; Increment: %d; Bidder: %s;", price, increment, bidder));
  }

  public void hasReceivedBid(int bid, String sniperId) throws InterruptedException {
    assertThat(currentChat.getParticipant(), equalTo(sniperId));

    messageListener.receivesAMessageMatching(sniperId, equalTo(format(XMPPAuction.BID_COMMAND_FORMAT, bid)));
  }

  public void sendInvalidMessageContaining(String brokenMessage) throws XMPPException {
    currentChat.sendMessage(brokenMessage);
  }

  public class SingleMessageListener implements MessageListener {
    private final ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<Message>(1);

    @Override
    public void processMessage(Chat chat, Message message) {
      messages.add(message);
    }

    public void receivesAMessageMatching(String sniperId, Matcher<? super String> messageMatcher)
      throws InterruptedException {
      final Message message = messages.poll(5, TimeUnit.SECONDS);
      assertThat(message, hasProperty("body", messageMatcher));
    }
  }
}
