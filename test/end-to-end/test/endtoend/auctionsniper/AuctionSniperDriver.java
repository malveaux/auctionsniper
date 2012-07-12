package test.endtoend.auctionsniper;

import com.objogate.wl.swing.AWTEventQueueProber;
import com.objogate.wl.swing.driver.JFrameDriver;
import com.objogate.wl.swing.driver.JLabelDriver;
import com.objogate.wl.swing.gesture.GesturePerformer;

import static test.endtoend.auctionsniper.ApplicationRunner.MAIN_WINDOW_NAME;
import static test.endtoend.auctionsniper.ApplicationRunner.SNIPER_STATUS_NAME;
import static org.hamcrest.core.IsEqual.equalTo;

public class AuctionSniperDriver extends JFrameDriver {
  public AuctionSniperDriver(int timeoutInMilliseconds) {
    super(new GesturePerformer(), JFrameDriver.topLevelFrame(
      named(MAIN_WINDOW_NAME),
      showingOnScreen()),
      new AWTEventQueueProber(timeoutInMilliseconds, 100));
  }

  public void showsSniperStatus(String statusText) {
    new JLabelDriver(this, named(SNIPER_STATUS_NAME)).hasText(equalTo(statusText));
  }
}
