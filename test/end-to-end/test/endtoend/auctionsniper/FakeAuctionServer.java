package test.endtoend.auctionsniper;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
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

  public void hasReceivedJoinRequestFromSniper() throws InterruptedException {
    messageListener.receivesAMessage();
  }

  public void announceClosed() throws XMPPException {
    currentChat.sendMessage(new Message());
  }

  public void stop() {
    connection.disconnect();
  }

  public class SingleMessageListener implements MessageListener {
    private final ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<Message>(1);

    @Override
    public void processMessage(Chat chat, Message message) {
      messages.add(message);
    }

    public void receivesAMessage() throws InterruptedException {
      assertThat("Message", messages.poll(5, TimeUnit.SECONDS), is(notNullValue()));
    }
  }
}
