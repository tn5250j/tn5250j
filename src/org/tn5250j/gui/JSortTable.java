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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

public class JSortTable extends JTable implements MouseListener {
	private static final long serialVersionUID = 1L;
	protected int sortedColumnIndex = -1;
	protected boolean sortedColumnAscending = true;

	public JSortTable(SortTableModel model) {
		super(model);
		initSortHeader();
	}

	protected void initSortHeader() {
		JTableHeader header = getTableHeader();
		header.setDefaultRenderer(new SortHeaderRenderer());
		header.addMouseListener(this);
	}

	public int getSortedColumnIndex() {
		return sortedColumnIndex;
	}

	public boolean isSortedColumnAscending() {
		return sortedColumnAscending;
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		TableColumnModel colModel = getColumnModel();
		int index = colModel.getColumnIndexAtX(event.getX());
		int modelIndex = colModel.getColumn(index).getModelIndex();

		SortTableModel model = (SortTableModel)getModel();
		if (model.isSortable(modelIndex)) {
			// toggle ascension, if already sorted
			if (sortedColumnIndex == index) {
				sortedColumnAscending = !sortedColumnAscending;
			}
			sortedColumnIndex = index;

			model.sortColumn(modelIndex, sortedColumnAscending);
		}
	}

	@Override
	public void mousePressed(MouseEvent event) {}
	@Override
	public void mouseClicked(MouseEvent event) {}
	@Override
	public void mouseEntered(MouseEvent event) {}
	@Override
	public void mouseExited(MouseEvent event) {}
}

