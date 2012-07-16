package auctionsniper.ui;

import auctionsniper.SniperSnapshot;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
  public static final String MAIN_WINDOW_NAME = "Auction Sniper Main";
  public static final String APPLICATION_TITLE = "Auction Sniper";
  public static final String NEW_ITEM_ID_NAME = "New Item";
  public static final String JOIN_BUTTON_NAME = "Join Button";

  private static final String SNIPERS_TABLE_NAME = "Snipers";
  private final SnipersTableModel snipers;

  public MainWindow(SnipersTableModel snipers) {
    super(APPLICATION_TITLE);
    this.snipers = snipers;
    setName(MAIN_WINDOW_NAME);
    fillContentPane(makeSnipersTable());
    pack();
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setVisible(true);
  }

  private void fillContentPane(JTable snipersTable) {
    final Container contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout());

    contentPane.add(new JScrollPane(snipersTable), BorderLayout.CENTER);
  }

  private JTable makeSnipersTable() {
    final JTable snipersTable = new JTable(snipers);
    snipersTable.setName(SNIPERS_TABLE_NAME);
    return snipersTable;
  }

  public void sniperStatusChanged(SniperSnapshot sniperSnapshot) {
    snipers.sniperStateChanged(sniperSnapshot);
  }

}