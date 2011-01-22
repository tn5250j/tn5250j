/**
 * $Id$
 *
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2009
 * Company:
 * @author: master_jaf
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
package org.tn5250j.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Locale;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.tn5250j.TN5250jConstants;
import org.tn5250j.framework.transport.SslType;
import org.tn5250j.gui.model.EmulConfig;
import org.tn5250j.gui.model.EmulSessionProfile;
import org.tn5250j.tools.GUIGraphicsUtils;
import org.tn5250j.tools.LangTool;

public class SessionsDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 2959214173388919093L;

	private static final String OK_ACTION = "OK";

	private final JPanel contentPanel = new JPanel();

	private final EmulConfig emulConfig;

	private JTable conTable;
	private JButton btnEdit;
	private JButton btnDelete;
	private JButton btnNew;
	private JButton btnQuickConnect;
	private JButton btnOptions;
	private JButton okButton;
	private JButton cancelButton;

	private volatile boolean dialogResult;
	private static final String ACTION_OK = OK_ACTION;
	private static final String ACTION_CANCEL = "CANCEL";
	private JLabel lblSubTitle;
	private JPanel panelTitle;

	private ColumnIDs[] DEFAULT_COLUMN_IDS = new ColumnIDs[]{
			ColumnIDs.NAME,
			ColumnIDs.HOST,
			ColumnIDs.PORT,
			ColumnIDs.SSL,
			ColumnIDs.CODEPAGE
	};
//	private Vector<ColumnIDs> columnids = new Vector<ColumnIDs>();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		try  {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch(Exception e) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ex) {
				// we don't care. Cause this should always work.
			}
		}
		LangTool.init(Locale.getDefault());
		try {
			SessionsDialog dialog = new SessionsDialog(null, "test", new EmulConfig() );
			dialog.emulConfig.addSession(new EmulSessionProfile("test1", "234234", false));
			dialog.emulConfig.addSession(new EmulSessionProfile("test2", "dgd234234", false));
			dialog.emulConfig.addSession(new EmulSessionProfile("test3", "99234dg234", false));
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dialog.setLocation(0, 0);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 *
	 * @param owner
	 * @param title
	 * @param properties
	 */
	public SessionsDialog(Frame owner, String title, EmulConfig emulConfig) {
		super(owner, title, true);

		this.emulConfig = emulConfig;

		this.setIconImages(GUIGraphicsUtils.getApplicationIcons());

		setBounds(100, 100, 600, 400);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(5, 5));
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.CENTER);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JScrollPane scrollPane = new JScrollPane();
				panel.add(scrollPane, BorderLayout.CENTER);
				{
					conTable = new JTable();
					conTable.setShowGrid(false);
					conTable.setShowHorizontalLines(true);
					conTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					scrollPane.setViewportView(conTable);
				}
			}
		}
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.EAST);
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			{
				btnNew = new JButton("New");
				panel.add(btnNew);
			}
			{
				btnEdit = new JButton("Edit");
				btnEdit.setEnabled(conTable.getSelectedRowCount() > 0);
				panel.add(btnEdit);
			}
			{
				btnDelete = new JButton("Delete");
				btnDelete.setEnabled(conTable.getSelectedRowCount() > 0);
				panel.add(btnDelete);
			}
			{
				JSeparator separator = new JSeparator();
				panel.add(separator);
			}
			{
				btnQuickConnect = new JButton("Quick Connect");
				panel.add(btnQuickConnect);
			}
			{
				btnOptions = new JButton("Options");
				panel.add(btnOptions);
			}
		}
		{
			JPanel panel = new JPanel();
			panel.setBorder(new EmptyBorder(3, 5, 3, 5));
			panel.setBackground(Color.WHITE);
			panel.setForeground(SystemColor.activeCaption);
			contentPanel.add(panel, BorderLayout.NORTH);
			panel.setLayout(new BorderLayout(10, 0));
			{
				panelTitle = new JPanel();
				panelTitle.setForeground(Color.BLACK);
				panelTitle.setBackground(Color.WHITE);
				panel.add(panelTitle, BorderLayout.CENTER);
				panelTitle.setLayout(new BorderLayout(0, 0));
				{
					JLabel lblTitle = new JLabel("Tn5250j");
					lblTitle.setBackground(Color.WHITE);
					lblTitle.setForeground(Color.BLACK);
					panelTitle.add(lblTitle, BorderLayout.NORTH);
					lblTitle.setFont(new Font("Tahoma", Font.BOLD, 15));
				}
				{
					lblSubTitle = new JLabel("developer edition");
					lblSubTitle.setBackground(Color.WHITE);
					lblSubTitle.setForeground(Color.DARK_GRAY);
					panelTitle.add(lblSubTitle, BorderLayout.SOUTH);
				}
			}
			{
				JLabel lblLogoImg = new JLabel("");
				lblLogoImg.setForeground(Color.BLACK);
				lblLogoImg.setBackground(Color.WHITE);
				lblLogoImg.setFont(new Font("Tahoma", Font.PLAIN, 11));
				final Image img = GUIGraphicsUtils.getApplicationIcons().get(1);
				lblLogoImg.setIcon(new ImageIcon(img));
				panel.add(lblLogoImg, BorderLayout.EAST);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("Connect");
				okButton.setActionCommand(ACTION_OK);
				okButton.addActionListener(this);
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand(ACTION_CANCEL);
				cancelButton.addActionListener(this);
				buttonPane.add(cancelButton);
			}
		}
		initValuesAndListeners();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		dialogResult = OK_ACTION.equals(e.getActionCommand());
		setVisible(false);
	}

	/**
	 * @return true -&gt; OK and false -&gt; CANCEL
	 */
	public boolean showModal() {
		this.setModal(true);
		this.setVisible(true);
		return dialogResult;
	}

	public EmulSessionProfile getSelectedSession() {
		if (conTable.getSelectedRowCount() > 0) {
			final int idx = conTable.convertRowIndexToModel(conTable.getSelectedRow());
			return emulConfig.getProfiles().get(idx);
		}
		return null;
	}

	private void initValuesAndListeners() {
		// reset default layout model
		Vector<ColumnIDs> columnids = new Vector<ColumnIDs>();
		for (ColumnIDs colid : DEFAULT_COLUMN_IDS) {
			columnids.add(colid);
		}
		// start initializing
		{
			final SessionDataModel model = new SessionDataModel();
			model.setColumnIdentifiers(columnids);
			conTable.setModel(model);
			conTable.setRowSorter(new TableRowSorter<SessionDataModel>(model));
			conTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					btnDelete.setEnabled(conTable.getSelectedRowCount() > 0);
					okButton.setEnabled(conTable.getSelectedRowCount() > 0);
					btnEdit.setEnabled(conTable.getSelectedRowCount() > 0);
				}
			});
			// column model for visibility of columns
			conTable.setColumnModel(new DynamicColumnModel());
			conTable.createDefaultColumnsFromModel();
			// header and column width ...
			final JTableHeader tableHeader = new SessionTableHeader(conTable.getColumnModel());
			tableHeader.setReorderingAllowed(true);
			tableHeader.setResizingAllowed(true);
			conTable.setTableHeader(tableHeader);
			for (int i=0; i<columnids.size(); i++) {
				ColumnIDs colid = columnids.get(i);
				conTable.getColumnModel().getColumn(i).setPreferredWidth(colid.prefSize);
			}
			// double clicks
			conTable.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						final ActionEvent doubleclickAction = new ActionEvent(conTable, e.getID()*37, OK_ACTION);
						actionPerformed(doubleclickAction);
					}
				}
			});
			// enter also means connect
			conTable.addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent e) {
					if (e.getKeyChar() == KeyEvent.VK_ENTER) {
						final ActionEvent entAction = new ActionEvent(conTable, e.getID()*37, OK_ACTION);
						actionPerformed(entAction);
					}
				}
			});
		}
		{
			btnEdit.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final int idx = conTable.convertRowIndexToModel(conTable.getSelectedRow());
					EmulSessionProfile session = null;
					if (0 <= idx && idx < emulConfig.getProfiles().size()) {
						session = emulConfig.getProfiles().get(idx);
					}
					if (session != null) {
						SessionProfileDialog editdlg = new SessionProfileDialog();
						editdlg.setTitle(LangTool.getString("conf.optEdit"));
						editdlg.setSession(session);
						final boolean ok = editdlg.showModal();
						if (ok) {
							emulConfig.getProfiles().set(idx, editdlg.getSession());
							SessionDataModel model = (SessionDataModel)conTable.getModel();
							model.fireTableRowsUpdated(idx, idx);
						}
					}
				}
			});
		}
		{
			btnNew.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					SessionProfileDialog createdlg = new SessionProfileDialog();
					createdlg.setTitle(LangTool.getString("conf.optAdd"));
					final boolean ok = createdlg.showModal();
					if (ok) {
						emulConfig.addSession(createdlg.getSession());
						SessionDataModel model = (SessionDataModel)conTable.getModel();
						int count = emulConfig.getSessionCount() - 1; // zero based
						model.fireTableRowsInserted(count, count);
					}
				}
			});
		}
		{
			btnDelete.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final int idx = conTable.convertRowIndexToModel(conTable.getSelectedRow());
					if (0 <= idx && idx < emulConfig.getProfiles().size()) {
						emulConfig.getProfiles().remove(idx);
						SessionDataModel model = (SessionDataModel)conTable.getModel();
						model.fireTableRowsDeleted(idx, idx);
					}
				}
			});
		}
		{
			okButton.setEnabled(conTable.getSelectedRowCount() > 0);
		}
		{
			lblSubTitle.setText(TN5250jConstants.VERSION_INFO);
		}
	}

	/*
	 * ========================================================================
	 */

	/**
	 * @see {@link javax.swing.table.JTableHeader}
	 */
	private final class SessionTableHeader extends JTableHeader implements MouseListener {

		private static final long serialVersionUID = 7271021945579035652L;

		public SessionTableHeader(TableColumnModel cm) {
			super(cm);
			addMouseListener(this);
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 1 && SwingUtilities.isRightMouseButton(e)) {
				final DynamicColumnModel tcolmodel = (DynamicColumnModel)getTable().getColumnModel();
				final JPopupMenu popup = new JPopupMenu();
				for (int i=0; i<DEFAULT_COLUMN_IDS.length; i++) {
					final ColumnIDs colid = DEFAULT_COLUMN_IDS[i];
					final TableColumn tabcol = tcolmodel.getColumnByModelIndex(i);
					final boolean isset = tcolmodel.isVisible(tabcol);
					final JCheckBoxMenuItem mi = new JCheckBoxMenuItem(getTable().getModel().getColumnName(i), isset);
					mi.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							tcolmodel.setVisible(tabcol, !isset);
						}
					});
					popup.add(mi);
				}
				GUIGraphicsUtils.positionPopup(e.getComponent(),popup,e.getX(),e.getY());
				popup.setVisible(true);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) { /* not used */ }

		@Override
		public void mouseReleased(MouseEvent e) { /* not used */ }

		@Override
		public void mouseEntered(MouseEvent e) { /* not used */ }

		@Override
		public void mouseExited(MouseEvent e) {  /* not used */ }

		//		@Override
		//		public String getToolTipText(MouseEvent event) {
		//            java.awt.Point p = event.getPoint();
		//            int index = columnModel.getColumnIndexAtX(p.x);
		//            int realIndex = columnModel.getColumn(index).getModelIndex();
		//
		//			switch (realIndex) {
		//			case 0:
		//				return LangTool.getString("conf.tableColA");
		//			case 1:
		//				return LangTool.getString("conf.tableColB");
		//			case 2:
		//				return "Port";
		//			case 3:
		//				return "SSL";
		//			default:
		//				break;
		//			}
		//			return null;
		//		}
	};

	/**
	 * @see {@link javax.swing.table.DefaultTableModel}
	 */
	private final class SessionDataModel extends DefaultTableModel {

		private static final long serialVersionUID = -8824174311096000429L;

		@Override
		public String getColumnName(int column) {
			final ColumnIDs colid = (ColumnIDs) columnIdentifiers.get(column);
			switch (colid) {
			case NAME:
				return LangTool.getString("conf.tableColA");
			case HOST:
				return LangTool.getString("conf.tableColB");
			case PORT:
				return "Port"; //FIXME: i18n
			case SSL:
				return "SSL";
			case CODEPAGE:
				return "Codepage"; //FIXME: i18n
			default:
				break;
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int column) {
			final ColumnIDs colid = (ColumnIDs) columnIdentifiers.get(column);
			return colid.clazz;
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		@Override
		public int getRowCount() {
			return emulConfig.getProfiles().size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			final ColumnIDs colid = (ColumnIDs) columnIdentifiers.get(column);
			final EmulSessionProfile rowitem = emulConfig.getProfiles().get(row);
			switch (colid) {
			case NAME:
				return rowitem.getName();
			case HOST:
				return rowitem.getHost();
			case PORT:
				return new Integer(rowitem.getPort());
			case SSL:
				return (rowitem.getSslType() != SslType.NONE) ? Boolean.TRUE : Boolean.FALSE;
			case CODEPAGE:
				return rowitem.getCodepage();
			default:
				break;
			}
			return null;
		}

	}

	/*
	 * ========================================================================
	 */

	private static enum ColumnIDs {

		NAME    (String.class, 250),
		HOST    (String.class, 250),
		PORT    (Integer.class, 50),
		SSL     (Boolean.class, 50),
		CODEPAGE(String.class, 75);

		public final Class<?> clazz;
		public final int prefSize;

		private ColumnIDs(Class<?> clazz, int prefSize) {
			this.clazz = clazz;
			this.prefSize = prefSize;
		}

	}

	/*
	 * ========================================================================
	 */
	/**
	 * @author Stephen Kelvin, mail@StephenKelvin.de (based on version 0.9 04/03/01)
	 * @author master_jaf
	 */
	private static class DynamicColumnModel extends DefaultTableColumnModel {

		private static final long serialVersionUID = -3334354018142910222L;

		private Vector<TableColumn> allTableColumns = new Vector<TableColumn>();

		public void setVisible(TableColumn tabcol, boolean visible) {
			if(!visible) {
				super.removeColumn(tabcol);
			} else {
				int noVisibleColumns = tableColumns.size();
				int visIdx = 0;
				for(int i=0,len=allTableColumns.size(); i<len; i++) {
					TableColumn visibleColumn = (visIdx < noVisibleColumns ? tableColumns.get(visIdx) : null);
					TableColumn testColumn = allTableColumns.get(i);
					if(testColumn.equals(tabcol)) {
						if(visibleColumn != tabcol) {
							super.addColumn(tabcol);
							super.moveColumn(tableColumns.size() - 1, visIdx);
						}
						break;
					}
					if(testColumn.equals(visibleColumn)) {
						visIdx++;
					}
				}
			}
		}

		/**
		 * Makes all columns in this model visible
		 */
		public void setAllVisible() {
			for(int i=0,len=allTableColumns.size(); i<len; i++) {
				TableColumn visibleColumn = (i < tableColumns.size() ? tableColumns.get(i) : null);
				TableColumn invisibleColumn = allTableColumns.get(i);
				if(!visibleColumn.equals(invisibleColumn)) {
					super.addColumn(invisibleColumn);
					super.moveColumn(tableColumns.size() - 1, i);
				}
			}
		}

		/**
		 * @param colidx
		 * @return a {@link TableColumn} or null if not found
		 */
		public TableColumn getColumnByModelIndex(int colidx) {
			for (int i=0; i<allTableColumns.size(); i++) {
				TableColumn column = allTableColumns.elementAt(i);
				if(column.getModelIndex() == colidx) {
					return column;
				}
			}
			return null;
		}

		/**
		 * @param tabcol
		 * @return
		 */
		public boolean isVisible(TableColumn tabcol) {
			return tableColumns.indexOf(tabcol) >= 0;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableColumnModel#addColumn(javax.swing.table.TableColumn)
		 */
		@Override
		public void addColumn(TableColumn tabcol) {
			allTableColumns.addElement(tabcol);
			super.addColumn(tabcol);
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableColumnModel#removeColumn(javax.swing.table.TableColumn)
		 */
		@Override
		public void removeColumn(TableColumn tabcol) {
			int allColumnsIndex = allTableColumns.indexOf(tabcol);
			if(allColumnsIndex != -1) {
				allTableColumns.removeElementAt(allColumnsIndex);
			}
			super.removeColumn(tabcol);
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableColumnModel#moveColumn(int, int)
		 */
		@Override
		public void moveColumn(int oldIndex, int newIndex) {
			if ((oldIndex < 0) || (oldIndex >= getColumnCount()) ||
					(newIndex < 0) || (newIndex >= getColumnCount())) {
				throw new IllegalArgumentException("moveColumn() - Index out of range");
			}
			final TableColumn fromColumn = tableColumns.get(oldIndex);
			final TableColumn toColumn = tableColumns.get(newIndex);
			final int allColumnsOldIndex = allTableColumns.indexOf(fromColumn);
			final int allColumnsNewIndex = allTableColumns.indexOf(toColumn);
			if(oldIndex != newIndex) {
				allTableColumns.removeElementAt(allColumnsOldIndex);
				allTableColumns.insertElementAt(fromColumn, allColumnsNewIndex);
			}
			super.moveColumn(oldIndex, newIndex);
		}
	}
}
