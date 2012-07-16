package auctionsniper.ui;

import auctionsniper.*;
import auctionsniper.util.Defect;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public class SnipersTableModel extends AbstractTableModel implements SniperListener, PortfolioListener {
  private ArrayList<SniperSnapshot> snapshots = new ArrayList<SniperSnapshot>();

  private static String[] STATUS_TEXT = {"Joining", "Bidding", "Winning", "Lost", "Won"};

  @Override
  public int getColumnCount() {
    return Column.values().length;
  }

  @Override
  public int getRowCount() {
    return snapshots.size();
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return Column.at(columnIndex).valueIn(snapshots.get(rowIndex));
  }

  @Override
  public String getColumnName(int column) {
    return Column.at(column).name;
  }

  public static String textFor(SniperState state) {
    return STATUS_TEXT[state.ordinal()];
  }

  public void sniperAdded(AuctionSniper sniper) {
    sniper.addSniperListener(new SwingThreadSniperListener(this));
    addSniperSnapshot(sniper.getSnapshot());
  }

  @Override
  public void sniperStateChanged(SniperSnapshot newSnapshot) {
    int row = rowMatching(newSnapshot);
    snapshots.set(row, newSnapshot);
    fireTableRowsUpdated(row, row);
  }

  private int rowMatching(SniperSnapshot snapshot) {
    for (int i = 0; i < snapshots.size(); i++) {
      if (snapshot.isForSameItemAs(snapshots.get(i)))
        return i;
    }
    throw new Defect("Cannot find match for " + snapshot);
  }

  private void addSniperSnapshot(SniperSnapshot sniperSnapshot) {
    snapshots.add(sniperSnapshot);
    int row = snapshots.size() - 1;
    fireTableRowsInserted(row, row);
  }
}
