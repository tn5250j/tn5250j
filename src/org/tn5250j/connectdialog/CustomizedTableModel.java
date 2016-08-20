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

import org.tn5250j.gui.SortTableModel;
import org.tn5250j.tools.LangTool;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

class CustomizedTableModel extends AbstractTableModel implements SortTableModel {
  private static final long serialVersionUID = 1L;

  private final String[] cols = {LangTool.getString("customized.name"),
      LangTool.getString("customized.window"),
      LangTool.getString("customized.unix")};

  private List<CustomizedExternalProgram> externalPrograms = new ArrayList<CustomizedExternalProgram>();
  private int sortedColumn = 0;
  private boolean isAscending = true;
  private final Properties externalProgramConfig;

  CustomizedTableModel(Properties externalProgramConfig) {
    super();
    this.externalProgramConfig = externalProgramConfig;
    resetSorted();
  }

  private void resetSorted() {
    externalPrograms.clear();

    String count = externalProgramConfig.getProperty("etn.pgm.support.total.num");
    if (count != null && count.length() > 0) {
      int total = Integer.parseInt(count);
      for (int i = 1; i <= total; i++) {
        String program = externalProgramConfig.getProperty("etn.pgm." + i + ".command.name");
        String wCommand = externalProgramConfig.getProperty("etn.pgm." + i + ".command.window");
        String uCommand = externalProgramConfig.getProperty("etn.pgm." + i + ".command.unix");
        externalPrograms.add(new CustomizedExternalProgram(program, wCommand, uCommand));
      }
    }

    sortColumn(sortedColumn, isAscending);
  }

  public boolean isSortable(int col) {
    if (col == 0) return true;
    return false;
  }

  public void sortColumn(int col, boolean ascending) {
    sortedColumn = col;
    isAscending = ascending;
    Collections.sort(externalPrograms);
    if (!isAscending) {
      Collections.reverse(externalPrograms);
    }
  }

  public int getColumnCount() {

    return cols.length;
  }

  public String getColumnName(int col) {
    return cols[col];
  }

  public int getRowCount() {
    return externalPrograms.size();
  }

  /*
   * Implement this so that the default session can be selected.
   */
  public void setValueAt(Object value, int row, int col) {

  }

  public Object getValueAt(int row, int col) {
    CustomizedExternalProgram c = externalPrograms.get(row);
    if (col == 0)
      return c.getName();
    if (col == 1)
      return c.getWCommand();
    if (col == 2) {
      return c.getUCommand();
    }
    return null;

  }

  public boolean isCellEditable(int row, int col) {
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
    fireTableRowsInserted(externalPrograms.size() - 1, externalPrograms.size() - 1);
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
