package org.tn5250j.gui;
/*
=====================================================================

  SortTableModel.java

  Created by Claude Duguay
  Copyright (c) 2002
   This was taken from a Java Pro magazine article
   http://www.fawcette.com/javapro/codepage.asp?loccode=jp0208

   I have NOT asked for permission to use this.
=====================================================================
*/

import javax.swing.table.TableModel;

public interface SortTableModel extends TableModel {
   public boolean isSortable(int col);
   public void sortColumn(int col, boolean ascending);
}

