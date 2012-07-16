package auctionsniper.ui;

import auctionsniper.SniperListener;
import auctionsniper.SniperSnapshot;

import javax.swing.*;

public class SwingThreadSniperListener implements SniperListener {
  private final SniperListener listener;

  public SwingThreadSniperListener(SniperListener listener) {
    this.listener = listener;
  }

  @Override
  public void sniperStateChanged(final SniperSnapshot snapshot) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        listener.sniperStateChanged(snapshot);
      }
    });
  }
}
