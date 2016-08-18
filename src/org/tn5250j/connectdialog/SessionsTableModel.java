/*
 * Title: tn5250J
 * Copyright:   Copyright (c) 2016
 *
 * Description:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 */
package org.tn5250j.connectdialog;

import org.tn5250j.TN5250jConstants;
import org.tn5250j.gui.SortTableModel;
import org.tn5250j.tools.LangTool;

import javax.swing.table.AbstractTableModel;
import java.util.*;

/**
 * Table model to show all available sessions,
 * with 'name', 'host' and 'default' column
 */
class SessionsTableModel extends AbstractTableModel implements SortTableModel {

  private static final long serialVersionUID = 1L;

  private final Properties properties;

  private final String[] COLS = {LangTool.getString("conf.tableColA"),
      LangTool.getString("conf.tableColB"),
      LangTool.getString("conf.tableColC")};

  private List<SessionsDataModel> sortedItems = new ArrayList<SessionsDataModel>();

  SessionsTableModel(Properties properties) {
    super();
    this.properties = properties;
    resetSorted();
  }

  private void resetSorted() {
    Enumeration<Object> e = properties.keys();
    sortedItems.clear();
    String ses = null;
    while (e.hasMoreElements()) {
      ses = (String) e.nextElement();

      if (!ses.startsWith("emul.")) {
        String[] args = new String[TN5250jConstants.NUM_PARMS];
        Configure.parseArgs(properties.getProperty(ses), args);
        boolean deflt = ses.equals(properties.getProperty("emul.default", ""));
        sortedItems.add(new SessionsDataModel(ses, args[0], deflt));
      }
    }

    sortColumn(0, true);
  }

  public boolean isSortable(int col) {
    if (col == 0) return true;
    if (col == 1) return true;
    return false;
  }

  public void sortColumn(final int col, final boolean ascending) {
    if (col == 0) Collections.sort(sortedItems, new Comparator<SessionsDataModel>() {
      public int compare(SessionsDataModel sdm1, SessionsDataModel sdm2) {
        if (ascending) return sdm1.name.compareToIgnoreCase(sdm2.name);
        return sdm2.name.compareToIgnoreCase(sdm1.name);
      }
    });
    if (col == 1) Collections.sort(sortedItems, new Comparator<SessionsDataModel>() {
      public int compare(SessionsDataModel sdm1, SessionsDataModel sdm2) {
        if (ascending) return sdm1.host.compareToIgnoreCase(sdm2.host);
        return sdm2.host.compareToIgnoreCase(sdm1.host);
      }
    });
  }

  public int getColumnCount() {
    return COLS.length;
  }

  public String getColumnName(int col) {
    return COLS[col];
  }

  public int getRowCount() {
    return sortedItems.size();
  }

  /*
   * Implement this so that the default session can be selected.
   */
  public void setValueAt(Object value, int row, int col) {

    boolean which = (Boolean) value;
    final String newDefaultSession = sortedItems.get(row).name;
    if (which) {
      properties.setProperty("emul.default", newDefaultSession);
    } else {
      properties.setProperty("emul.default", "");
    }
    // update internal list of data models
    for (int i = 0, len = sortedItems.size(); i < len; i++) {
      final SessionsDataModel oldsdm = sortedItems.get(i);
      if (newDefaultSession.equals(oldsdm.name)) {
        sortedItems.set(i, new SessionsDataModel(oldsdm.name, oldsdm.host, (Boolean) value));
      } else if (oldsdm.deflt) {
        // clear the old default flag
        sortedItems.set(i, new SessionsDataModel(oldsdm.name, oldsdm.host, Boolean.FALSE));
      }
    }
    this.fireTableDataChanged();
  }

  public Object getValueAt(int row, int col) {
    switch (col) {
      case 0:
        return this.sortedItems.get(row).name;
      case 1:
        return this.sortedItems.get(row).host;
      case 2:
        return this.sortedItems.get(row).deflt;
      default:
        return null;
    }
  }

  /*
   * We need to implement this so that the default session column can
   *    be updated.
   */
  public boolean isCellEditable(int row, int col) {
    //Note that the data/cell address is constant,
    //no matter where the cell appears onscreen.
    if (col == 2) {
      return true;
    }
    return false;
  }

  /*
   * JTable uses this method to determine the default renderer/
   * editor for each cell.  If we didn't implement this method,
   * then the default column would contain text ("true"/"false"),
   * rather than a check box.
   */
  public Class<?> getColumnClass(int c) {
    return getValueAt(0, c).getClass();
  }

  void addSession() {
    resetSorted();
    fireTableRowsInserted(properties.size() - 1, properties.size() - 1);
  }

  void chgSession(int row) {
    resetSorted();
    fireTableRowsUpdated(row, row);
  }

  void removeSession(int row) {
    resetSorted();
    fireTableRowsDeleted(row, row);
  }

}
