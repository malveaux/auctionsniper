package auctionsniper.ui;

import auctionsniper.SniperSnapshot;
import auctionsniper.UserRequestListener;
import auctionsniper.util.Announcer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainWindow extends JFrame {
  public static final String MAIN_WINDOW_NAME = "Auction Sniper Main";
  public static final String APPLICATION_TITLE = "Auction Sniper";
  public static final String NEW_ITEM_ID_NAME = "New Item";
  public static final String JOIN_BUTTON_NAME = "Join Button";

  private static final String SNIPERS_TABLE_NAME = "Snipers";
  private final Announcer<UserRequestListener> userRequests = Announcer.to(UserRequestListener.class);
  private final SnipersTableModel snipers;

  public MainWindow(SnipersTableModel snipers) {
    super(APPLICATION_TITLE);
    this.snipers = snipers;
    setName(MAIN_WINDOW_NAME);
    fillContentPane(makeSnipersTable(), makeControls());
    pack();
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setVisible(true);
  }

  private JTable makeSnipersTable() {
    final JTable snipersTable = new JTable(snipers);
    snipersTable.setName(SNIPERS_TABLE_NAME);
    return snipersTable;
  }

  private JPanel makeControls() {
    JPanel controls = new JPanel(new FlowLayout());
    final JTextField itemIdField = new JTextField();
    itemIdField.setColumns(25);
    itemIdField.setName(NEW_ITEM_ID_NAME);
    controls.add(itemIdField);

    JButton joinAuctionButton = new JButton("Join Auction");
    joinAuctionButton.setName(JOIN_BUTTON_NAME);
    joinAuctionButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        userRequests.announce().joinAuction(itemIdField.getText());
      }
    });
    controls.add(joinAuctionButton);

    return controls;
  }

  private void fillContentPane(JTable snipersTable, JPanel controls) {
    final Container contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout());

    contentPane.add(controls, BorderLayout.NORTH);
    contentPane.add(new JScrollPane(snipersTable), BorderLayout.CENTER);
  }

  public void sniperStatusChanged(SniperSnapshot sniperSnapshot) {
    snipers.sniperStateChanged(sniperSnapshot);
  }

  public void addUserRequestListener(UserRequestListener userRequestListener) {
    userRequests.addListener(userRequestListener);
  }
}