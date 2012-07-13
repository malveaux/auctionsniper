package test.endtoend.auctionsniper;

import com.objogate.wl.swing.AWTEventQueueProber;
import com.objogate.wl.swing.driver.JFrameDriver;
import com.objogate.wl.swing.driver.JTableDriver;
import com.objogate.wl.swing.gesture.GesturePerformer;

import static auctionsniper.ui.MainWindow.MAIN_WINDOW_NAME;
import static com.objogate.wl.swing.matcher.IterableComponentsMatcher.matching;
import static com.objogate.wl.swing.matcher.JLabelTextMatcher.withLabelText;

public class AuctionSniperDriver extends JFrameDriver {
  public AuctionSniperDriver(int timeoutInMilliseconds) {
    super(new GesturePerformer(), JFrameDriver.topLevelFrame(
      named(MAIN_WINDOW_NAME),
      showingOnScreen()),
      new AWTEventQueueProber(timeoutInMilliseconds, 100));
  }

  public void showsSniperStatus(String statusText) {
    new JTableDriver(this).hasCell(withLabelText(statusText));
  }

  public void showsSniperStatus(String itemId, int lastPrice, int lastBid, String statusText) {
    new JTableDriver(this).hasRow(
      matching(withLabelText(itemId), withLabelText(String.valueOf(lastPrice)),
        withLabelText(String.valueOf(lastBid)), withLabelText(statusText)));
  }


}
