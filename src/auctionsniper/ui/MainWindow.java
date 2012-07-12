package auctionsniper.ui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class MainWindow extends JFrame {
  public static final String MAIN_WINDOW_NAME = "Auction Sniper Main";
  public static final String SNIPER_STATUS_NAME = "sniper status";
  public static final String STATUS_JOINING = "Joining";
  public static final String STATUS_BIDDING = "Bidding";
  public static final String STATUS_LOST = "Lost";

  private final JLabel sniperStatus = createLabel(STATUS_JOINING);

  private JLabel createLabel(String initialText) {
    JLabel result = new JLabel(initialText);
    result.setName(SNIPER_STATUS_NAME);
    result.setBorder(new LineBorder(Color.black));
    return result;
  }

  public MainWindow() {
    super("Auction Sniper");
    setName(MAIN_WINDOW_NAME);
    add(sniperStatus);
    pack();
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setVisible(true);
  }

  public void showStatus(String status) {
    sniperStatus.setText(status);
  }
}
