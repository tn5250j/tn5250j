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
import java.awt.Frame;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

import org.tn5250j.gui.model.EmulConfig;
import org.tn5250j.gui.model.EmulSession;
import org.tn5250j.gui.model.SslType;
import org.tn5250j.tools.GUIGraphicsUtils;
import org.tn5250j.tools.LangTool;

public class SessionsDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 2959214173388919093L;

	private static final String OK_ACTION = "OK";

	private final JPanel contentPanel = new JPanel();
	
	private final EmulConfig emulConfig = new EmulConfig();
	
	private JTable conTable;
	private JButton btnEdit;
	private JButton btnDelete;
	private JButton btnNew;
	private JButton btnQuickConnect;
	private JButton btnOptions;
	private JButton okButton;
	private JButton cancelButton;
	
	private volatile EmulSession selectedSession = null;
	private volatile boolean dialogResult;
	private static final String ACTION_OK = OK_ACTION;
	private static final String ACTION_CANCEL = "CANCEL";

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
	   }
	   catch(Exception e) {
		   try {
			   UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		   } catch (Exception ex) {
			   // we don't care. Cause this should always work.
		   }
	   }
		try {
			SessionsDialog dialog = new SessionsDialog(null, "test", new EmulConfig() );
			dialog.emulConfig.addSession(new EmulSession("test1", "234234", false));
			dialog.emulConfig.addSession(new EmulSession("test2", "dgd234234", false));
			dialog.emulConfig.addSession(new EmulSession("test3", "99234dg234", false));
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
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
	public SessionsDialog(Frame owner, String title, EmulConfig sessions) {
		super(owner, title, true);
		this.setIconImages(GUIGraphicsUtils.getApplicationIcons());
		
		for (EmulSession session : sessions.getSessions()) {
			emulConfig.addSession(session);
		}
		
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
			panel.setBackground(SystemColor.activeCaptionText);
			panel.setForeground(new Color(0, 0, 0));
			contentPanel.add(panel, BorderLayout.NORTH);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JLabel lblTitle = new JLabel("Tn5250j");
				panel.add(lblTitle);
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
	
	public EmulSession getSelectedSession() {
		return selectedSession;
	}

	private void initValuesAndListeners() {
		{
			final SessionDataModel model = new SessionDataModel();
			conTable.setModel(model);
			conTable.setRowSorter(new TableRowSorter<SessionDataModel>(model));
			conTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					btnDelete.setEnabled(conTable.getSelectedRowCount() > 0);
					okButton.setEnabled(conTable.getSelectedRowCount() > 0);
					btnEdit.setEnabled(conTable.getSelectedRowCount() > 0);
					if (conTable.getSelectedRowCount() > 0) {
						final int idx = conTable.convertRowIndexToModel(conTable.getSelectedRow());
						selectedSession = emulConfig.getSessions().get(idx);
					}
				}
			});
			conTable.createDefaultColumnsFromModel();
			final JTableHeader tableHeader = new JTableHeader(conTable.getColumnModel()) {
				private static final long serialVersionUID = 7271021945579035652L;
				@Override
				public String getToolTipText(MouseEvent event) {
	                java.awt.Point p = event.getPoint();
	                int index = columnModel.getColumnIndexAtX(p.x);
	                int realIndex = 
	                        columnModel.getColumn(index).getModelIndex();
	                
	    			switch (realIndex) {
	    			case 0:
	    				return LangTool.getString("conf.tableColA");
	    			case 1:
	    				return LangTool.getString("conf.tableColB");
	    			case 2:
	    				return "Port";
	    			case 3:
	    				return "SSL";
	    			default:
	    				break;
	    			}
	    			return null;
				}
			};
			tableHeader.setReorderingAllowed(true);
			tableHeader.setResizingAllowed(true);
			conTable.setTableHeader(tableHeader);
			conTable.getColumnModel().getColumn(0).setPreferredWidth(250);
			conTable.getColumnModel().getColumn(1).setPreferredWidth(250);
			conTable.getColumnModel().getColumn(2).setPreferredWidth(50);
			conTable.getColumnModel().getColumn(3).setPreferredWidth(50);
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
					EmulSession session = null;
					if (0 <= idx && idx < emulConfig.getSessions().size()) {
						session = emulConfig.getSessions().get(idx);
					}
					if (session != null) {
						SessionPropsDialog editdlg = new SessionPropsDialog();
						editdlg.setTitle(LangTool.getString("conf.optEdit"));
						editdlg.setSession(session);
						final boolean ok = editdlg.showModal();
						if (ok) {
							emulConfig.getSessions().set(idx, editdlg.getSession());
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
					SessionPropsDialog createdlg = new SessionPropsDialog();
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
					if (0 <= idx && idx < emulConfig.getSessions().size()) {
						emulConfig.getSessions().remove(idx);
						SessionDataModel model = (SessionDataModel)conTable.getModel();
						model.fireTableRowsDeleted(idx, idx);
					}
				}
			});
		}
		{
			okButton.setEnabled(conTable.getSelectedRowCount() > 0);
		}
	}

	/*
	 * ========================================================================
	 */
	
	private final class SessionDataModel extends DefaultTableModel {

		private static final long serialVersionUID = -8824174311096000429L;

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return LangTool.getString("conf.tableColA");
			case 1:
				return LangTool.getString("conf.tableColB");
			case 2:
				return "Port";
			case 3:
				return "SSL";
			default:
				break;
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int column) {
			switch (column) {
			case 0:
				return String.class;
			case 1:
				return String.class;
			case 2:
				return Integer.class;
			case 3:
				return Boolean.class;
			default:
				break;
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		@Override
		public int getRowCount() {
			return emulConfig.getSessions().size();
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public Object getValueAt(int row, int column) {
			final EmulSession rowitem = emulConfig.getSessions().get(row);
			switch (column) {
			case 0:
				return rowitem.getName();
			case 1:
				return rowitem.getHost();
			case 2:
				return new Integer(rowitem.getPort());
			case 3:
				return (rowitem.getSslType() != SslType.NONE) ? Boolean.TRUE : Boolean.FALSE;
			default:
				break;
			}
			return null;
		}
		
	}
	
}
