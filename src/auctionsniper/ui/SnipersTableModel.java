package auctionsniper.ui;

import auctionsniper.SniperListener;
import auctionsniper.SniperSnapshot;
import auctionsniper.SniperState;

import javax.swing.table.AbstractTableModel;

public class SnipersTableModel extends AbstractTableModel implements SniperListener {
  private final static SniperSnapshot STARTING_UP = new SniperSnapshot("", 0, 0, SniperState.JOINING);
  private SniperSnapshot snapshot = STARTING_UP;

  private static String[] STATUS_TEXT = {"Joining", "Bidding", "Winning", "Lost", "Won"};

  @Override
  public int getColumnCount() {
    return Column.values().length;
  }

  @Override
  public int getRowCount() {
    return 1;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return Column.at(columnIndex).valueIn(snapshot);
  }

  @Override
  public String getColumnName(int column) {
    return Column.at(column).name;
  }

  public static String textFor(SniperState state) {
    return STATUS_TEXT[state.ordinal()];
  }

  @Override
  public void sniperStateChanged(SniperSnapshot newSnapshot) {
    this.snapshot = newSnapshot;
    fireTableRowsUpdated(0, 0);
  }
}
