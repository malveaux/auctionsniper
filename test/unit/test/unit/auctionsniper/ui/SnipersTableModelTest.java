package test.unit.auctionsniper.ui;

import auctionsniper.AuctionSniper;
import auctionsniper.SniperSnapshot;
import auctionsniper.SniperState;
import auctionsniper.ui.Column;
import auctionsniper.ui.SnipersTableModel;
import auctionsniper.util.Defect;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(JMock.class)
public class SnipersTableModelTest {
  private final Mockery context = new Mockery();
  private TableModelListener listener = context.mock(TableModelListener.class);
  private final SnipersTableModel model = new SnipersTableModel();


  @Before
  public void attachModelListener() {
    model.addTableModelListener(listener);
  }

  @Test
  public void hasEnoughColumns() {
    assertThat(model.getColumnCount(), equalTo(Column.values().length));
  }

  @Test
  public void setsSniperValuesInColumns() {
    AuctionSniper sniper = new AuctionSniper("item id", null);
    SniperSnapshot bidding = sniper.getSnapshot().bidding(555, 666);

    context.checking(new Expectations() {{
      allowing(listener).tableChanged(with(anyInsertionEvent()));
      one(listener).tableChanged(with(aRowChangedEvent()));
    }});

    model.sniperAdded(sniper);
    model.sniperStateChanged(bidding);

    assertRowMatchesSnapshot(0, bidding);
  }

  private Matcher<TableModelEvent> anyInsertionEvent() {
    return hasProperty("type", equalTo(TableModelEvent.INSERT));
  }

  private Matcher<TableModelEvent> aRowChangedEvent() {
    return samePropertyValuesAs(new TableModelEvent(model, 0));
  }

  private void assertColumnEquals(Column column, Object expected) {
    final int rowIndex = 0;
    final int columnIndex = column.ordinal();
    assertEquals(expected, model.getValueAt(rowIndex, columnIndex));
  }

  private Matcher<TableModelEvent> aChangeInRow(int row) {
    return samePropertyValuesAs(new TableModelEvent(model, row));
  }

  @Test
  public void setsUpColumnHeadings() {
    for (Column column : Column.values()) {
      assertEquals(column.name, model.getColumnName(column.ordinal()));
    }
  }

  @Test
  public void notifiesListenersWhenAddingASniper() {
    AuctionSniper sniper = new AuctionSniper("item123", null);
    context.checking(new Expectations() {{
      one(listener).tableChanged(with(anInsertionAtRow(0)));
    }});

    assertEquals(0, model.getRowCount());

    model.sniperAdded(sniper);

    assertEquals(1, model.getRowCount());
    assertRowMatchesSnapshot(0, sniper.getSnapshot());
  }

  Matcher<TableModelEvent> anInsertionAtRow(final int row) {
    return samePropertyValuesAs(new TableModelEvent(model, row, row, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
  }

  @Test
  public void holdsSnipersInAdditionOrder() {
    context.checking(new Expectations() {{
      ignoring(listener);
    }});

    model.sniperAdded(new AuctionSniper("item 0", null));
    model.sniperAdded(new AuctionSniper("item 1", null));

    assertEquals("item 0", cellValue(0, Column.ITEM_IDENTIFIER));
    assertEquals("item 1", cellValue(1, Column.ITEM_IDENTIFIER));
  }

  @Test
  public void updatesCorrectRowForSniper() {
    context.checking(new Expectations() {{
      ignoring(listener);
    }});

    AuctionSniper sniper1 = new AuctionSniper("item0", null);
    AuctionSniper sniper2 = new AuctionSniper("item1", null);
    model.sniperAdded(sniper1);
    model.sniperAdded(sniper2);

    SniperSnapshot sniper2Bidding = sniper2.getSnapshot().bidding(0, 0);
    model.sniperStateChanged(sniper2Bidding);

    assertRowMatchesSnapshot(0, sniper1.getSnapshot());
    assertRowMatchesSnapshot(1, sniper2Bidding);
  }

  private void assertRowMatchesSnapshot(int row, SniperSnapshot snapshot) {
    assertEquals(snapshot.itemId, cellValue(row, Column.ITEM_IDENTIFIER));
    assertEquals(snapshot.lastPrice, cellValue(row, Column.LAST_PRICE));
    assertEquals(snapshot.lastBid, cellValue(row, Column.LAST_BID));
    assertEquals(SnipersTableModel.textFor(snapshot.state), cellValue(row, Column.SNIPER_STATE));
  }

  private Object cellValue(int rowIndex, Column column) {
    return model.getValueAt(rowIndex, column.ordinal());
  }

  @Test(expected = Defect.class)
  public void throwsDefectIfNoExistingForAnUpdate() {
    model.sniperStateChanged(new SniperSnapshot("item 1", 123, 234, SniperState.WINNING));
  }
}
