package auctionsniper;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;

public class XMPPAuction implements Auction {
  private final Chat chat;

  public XMPPAuction(Chat chat) {
    this.chat = chat;
  }

  @Override
  public void bid(int amount) {
    try {
      chat.sendMessage(String.format(Main.BID_COMMAND_FORMAT, amount));
    } catch (XMPPException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void join() {
    try {
      chat.sendMessage(Main.JOIN_COMMAND_FORMAT);
    } catch (XMPPException e) {
      e.printStackTrace();
    }
  }
}
