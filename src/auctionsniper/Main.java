package auctionsniper;

import auctionsniper.ui.MainWindow;
import auctionsniper.ui.SnipersTableModel;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class Main {
  private static final int ARG_HOSTNAME = 0;
  private static final int ARG_USERNAME = 1;
  private static final int ARG_PASSWORD = 2;

  public static final String AUCTION_RESOURCE = "Auction";
  public static final String ITEM_ID_AS_LOGIN = "auction-%s";
  public static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;
  public static final String JOIN_COMMAND_FORMAT = "SOLVersion: 1.1; Command: JOIN;";
  public static final String BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price %d;";

  private final SnipersTableModel snipers = new SnipersTableModel();
  private MainWindow ui;
  @SuppressWarnings("unused")
  private ArrayList<Chat> notToBeGCd = new ArrayList<Chat>();

  public static void main(String... args) throws Exception {
    Main main = new Main();

    XMPPConnection connection = connect(args[ARG_HOSTNAME], args[ARG_USERNAME], args[ARG_PASSWORD]);
    main.disconnectWhenUICloses(connection);

    main.addUserRequestListenerFor(connection);
  }

  private void addUserRequestListenerFor(final XMPPConnection connection) {
    ui.addUserRequestListener(new UserRequestListener() {
      @Override
      public void joinAuction(String itemId) {
        snipers.addSniper(SniperSnapshot.joining(itemId));
        Chat chat = connection.getChatManager().createChat(auctionId(itemId, connection), null);

        notToBeGCd.add(chat);

        Auction auction = new XMPPAuction(chat);

        chat.addMessageListener(new AuctionMessageTranslator(connection.getUser(),
          new AuctionSniper(itemId, auction, new SwingThreadSniperListener(snipers))));

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

  private void disconnectWhenUICloses(final XMPPConnection connection) {
    ui.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent windowEvent) {
        connection.disconnect();
      }
    });
  }

  private static String auctionId(String itemId, XMPPConnection connection) {
    return String.format(AUCTION_ID_FORMAT, itemId, connection.getServiceName());
  }

  private static XMPPConnection connect(String hostname, String username, String password) throws XMPPException {
    XMPPConnection connection = new XMPPConnection(hostname);
    connection.connect();
    connection.login(username, password, AUCTION_RESOURCE);

    return connection;
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
