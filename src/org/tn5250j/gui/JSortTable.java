package org.tn5250j.gui;
/*
=====================================================================

  JSortTable.java

  Created by Claude Duguay
  Copyright (c) 2002
   This was taken from a Java Pro magazine article
   http://www.fawcette.com/javapro/codepage.asp?loccode=jp0208

   I have NOT asked for permission to use this.

=====================================================================
*/

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class JSortTable extends JTable implements MouseListener {

  private static final long serialVersionUID = 1L;
  private int sortedColumnIndex = -1;
  private boolean sortedColumnAscending = true;

  public JSortTable(SortTableModel model) {
    super(model);
    initSortHeader();
  }

  private void initSortHeader() {
    JTableHeader header = getTableHeader();
    header.setDefaultRenderer(new SortHeaderRenderer());
    header.addMouseListener(this);
  }

  int getSortedColumnIndex() {
    return sortedColumnIndex;
  }

  boolean isSortedColumnAscending() {
    return sortedColumnAscending;
  }

  public void mouseReleased(MouseEvent event) {
    TableColumnModel colModel = getColumnModel();
    int index = colModel.getColumnIndexAtX(event.getX());
    int modelIndex = colModel.getColumn(index).getModelIndex();

    SortTableModel model = (SortTableModel) getModel();
    if (model.isSortable(modelIndex)) {
      // toggle ascension, if already sorted
      if (sortedColumnIndex == index) {
        sortedColumnAscending = !sortedColumnAscending;
      }
      sortedColumnIndex = index;

      model.sortColumn(modelIndex, sortedColumnAscending);
    }
  }

  public void mousePressed(MouseEvent event) {
  }

  public void mouseClicked(MouseEvent event) {
  }

  public void mouseEntered(MouseEvent event) {
  }

  public void mouseExited(MouseEvent event) {
  }
}
