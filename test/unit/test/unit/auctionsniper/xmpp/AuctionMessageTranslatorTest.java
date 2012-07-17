package test.unit.auctionsniper.xmpp;

import auctionsniper.AuctionEventListener;
import auctionsniper.xmpp.AuctionMessageTranslator;
import auctionsniper.xmpp.XMPPFailureReporter;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import static auctionsniper.AuctionEventListener.PriceSource;

@RunWith(JMock.class)
public class AuctionMessageTranslatorTest {
  public static final String SNIPER_ID = "sniper";

  private final Mockery context = new Mockery();
  private final AuctionEventListener listener = context.mock(AuctionEventListener.class);
  private final XMPPFailureReporter failureReporter = context.mock(XMPPFailureReporter.class);

  private static final Chat UNUSED_CHAT = null;
  private final AuctionMessageTranslator translator =
    new AuctionMessageTranslator(SNIPER_ID, listener, failureReporter);

  @Test
  public void notifiesAuctionClosedWhenCloseMessageReceived() {
    context.checking(new Expectations() {{
      oneOf(listener).auctionClosed();
    }});

    translator.processMessage(UNUSED_CHAT, message("SOLVersion: 1.1; Event: CLOSE;"));
  }

  @Test
  public void notifiesBidDetailsWhenCurrentPriceMessageReceivedFromOtherBidder() {
    context.checking(new Expectations() {{
      oneOf(listener).currentPrice(192, 7, PriceSource.FromOtherBidder);
    }});

    translator.processMessage(UNUSED_CHAT,
      message("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: Someone else;"));
  }

  @Test
  public void notifiesBidDetailsWhenCurrentPriceMessageReceivedFromSniper() {
    context.checking(new Expectations() {{
      exactly(1).of(listener).currentPrice(234, 5, PriceSource.FromSniper);
    }});

    translator.processMessage(UNUSED_CHAT,
      message("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 234; Increment: 5; Bidder: " + SNIPER_ID + ";"));
  }

  @Test
  public void notifiesAuctionFailedWhenBadMessageReceived() {
    final String badMessage = "a bad message";

    expectFailureWithMessage(badMessage);

    translator.processMessage(UNUSED_CHAT, message(badMessage));
  }

  private Message message(String messageBody) {
    Message message = new Message();
    message.setBody(messageBody);
    return message;
  }

  private void expectFailureWithMessage(final String badMessage) {
    context.checking(new Expectations() {{
      exactly(1).of(listener).auctionFailed();
      oneOf(failureReporter).cannotTranslateMessage(
        with(SNIPER_ID), with(badMessage), with(any(Exception.class)));
    }});
  }

  @Test
  public void notifiesAuctionFailedWhenEventTypeMissing() {
    final String badMessage = "SOLVersion: 1.1; CurrentPrice: 234; Increment: 5; Bidder: " + SNIPER_ID + ";";

    expectFailureWithMessage(badMessage);

    translator.processMessage(UNUSED_CHAT, message(badMessage));
  }
}
