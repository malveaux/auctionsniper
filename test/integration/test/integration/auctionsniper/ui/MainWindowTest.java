package test.integration.auctionsniper.ui;

import auctionsniper.SniperPortfolio;
import auctionsniper.UserRequestListener;
import auctionsniper.ui.MainWindow;
import com.objogate.wl.swing.probe.ValueMatcherProbe;
import org.junit.Test;
import test.endtoend.auctionsniper.AuctionSniperDriver;

import static auctionsniper.UserRequestListener.Item;
import static org.hamcrest.core.IsEqual.equalTo;

public class MainWindowTest {
  {
    System.setProperty("com.objogate.wl.keyboard", "US");
  }

  private final SniperPortfolio portfolio = new SniperPortfolio();
  private final MainWindow mainWindow = new MainWindow(portfolio);
  private final AuctionSniperDriver driver = new AuctionSniperDriver(100);

  @Test
  public void makesUserRequestWhenJoinButtonClicked() {
    final ValueMatcherProbe<Item> buttonProbe =
      new ValueMatcherProbe<Item>(equalTo(new Item("an item-id", 789)), "join request");

    mainWindow.addUserRequestListener(
      new UserRequestListener() {
        @Override
        public void joinAuction(Item item) {
          buttonProbe.setReceivedValue(item);
        }
      });

    driver.startBiddingFor("an item-id", 789);
    driver.check(buttonProbe);
  }
}
