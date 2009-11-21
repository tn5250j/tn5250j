package org.tn5250j.gui;
/*
=====================================================================

  SortHeaderRenderer.java

  Created by Claude Duguay
  Copyright (c) 2002
   This was taken from a Java Pro magazine article
   http://www.fawcette.com/javapro/codepage.asp?loccode=jp0208

   I have NOT asked for permission to use this.

=====================================================================
*/

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class SortHeaderRenderer extends DefaultTableCellRenderer {

   private static final long serialVersionUID = 1L;
public static Icon NONSORTED =  new SortArrowIcon(SortArrowIcon.NONE);
   public static Icon ASCENDING =  new SortArrowIcon(SortArrowIcon.ASCENDING);
   public static Icon DECENDING =  new SortArrowIcon(SortArrowIcon.DECENDING);

   public SortHeaderRenderer() {
      setHorizontalTextPosition(LEFT);
      setHorizontalAlignment(CENTER);
   }

   public Component getTableCellRendererComponent( JTable table,
                              Object value,
                              boolean isSelected,
                              boolean hasFocus, int row, int col) {

      int index = -1;
      boolean ascending = true;
      if (table instanceof JSortTable) {
         JSortTable sortTable = (JSortTable)table;
         index = sortTable.getSortedColumnIndex();
         ascending = sortTable.isSortedColumnAscending();
      }
      if (table != null) {
         JTableHeader header = table.getTableHeader();
         if (header != null) {
            setForeground(header.getForeground());
            setBackground(header.getBackground());
            setFont(header.getFont());
         }
      }

      Icon icon = ascending ? ASCENDING : DECENDING;
      setIcon(col == index ? icon : NONSORTED);
      setText((value == null) ? "" : value.toString());
      setBorder(UIManager.getBorder("TableHeader.cellBorder"));
      return this;
   }
}

