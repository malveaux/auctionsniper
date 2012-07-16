package auctionsniper;

import auctionsniper.ui.MainWindow;
import auctionsniper.ui.SnipersTableModel;
import auctionsniper.xmpp.XMPPAuctionHouse;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class Main {
  private static final int ARG_HOSTNAME = 0;
  private static final int ARG_USERNAME = 1;
  private static final int ARG_PASSWORD = 2;

  private final SnipersTableModel snipers = new SnipersTableModel();
  private MainWindow ui;
  @SuppressWarnings("unused")
  private ArrayList<Auction> notToBeGCd = new ArrayList<Auction>();

  public static void main(String... args) throws Exception {
    Main main = new Main();

    XMPPAuctionHouse auctionHouse = XMPPAuctionHouse.connect(args[ARG_HOSTNAME], args[ARG_USERNAME], args[ARG_PASSWORD]);
    main.disconnectWhenUICloses(auctionHouse);

    main.addUserRequestListenerFor(auctionHouse);
  }

  private void addUserRequestListenerFor(final AuctionHouse auctionHouse) {
    ui.addUserRequestListener(new UserRequestListener() {
      @Override
      public void joinAuction(String itemId) {
        snipers.addSniper(SniperSnapshot.joining(itemId));

        Auction auction = auctionHouse.auctionFor(itemId);
        notToBeGCd.add(auction);

        auction.addAuctionEventListener(
          new AuctionSniper(itemId, auction, new SwingThreadSniperListener(snipers)));

        auction.join();
      }
    });
  }

  public Main() throws Exception {
    startUserInterface();
  }

  private void startUserInterface() throws Exception {
    SwingUtilities.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        ui = new MainWindow(snipers);
      }
    });
  }

  private void disconnectWhenUICloses(final AuctionHouse auctionHouse) {
    ui.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent windowEvent) {
        auctionHouse.disconnect();
      }
    });
  }

  public class SwingThreadSniperListener implements SniperListener {
    private final SniperListener listener;

    SwingThreadSniperListener(SniperListener listener) {
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
}
