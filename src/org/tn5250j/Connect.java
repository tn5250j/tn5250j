/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.5
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
package org.tn5250j;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import org.tn5250j.gui.JSortTable;
import org.tn5250j.gui.SortTableModel;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.tools.LangTool;
import org.tn5250j.gui.TN5250jMultiSelectList;

public class Connect
	extends JDialog
	implements ActionListener, ChangeListener, TN5250jConstants {

	// panels to be displayed
	JPanel configOptions = new JPanel();
	JPanel sessionPanel = new JPanel();
	JPanel options = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
	JPanel interfacePanel = new JPanel();
	JPanel sessionOpts = new JPanel();
	JPanel sessionOptPanel = new JPanel(new FlowLayout(
		FlowLayout.CENTER, 30, 10));
	JPanel emulOptPanel = new JPanel();
	JPanel emptyPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
	JPanel accessPanel = new JPanel();

	JTable sessions = null;
	GridBagConstraints gbc;

	// button needing global access
	JButton editButton = null;
	JButton removeButton = null;
	JButton connectButton = null;
	JButton applyButton = null;

	// custom table model
	ConfigureTableModel ctm = null;

	// The scroll pane that holds the table.
	JScrollPane scrollPane;

	// ListSelectionModel of our custom table.
	ListSelectionModel rowSM = null;

	// Properties
	Properties props = null;

	// property input structures
	JRadioButton intTABS = null;
	JRadioButton intMDI = null;
	JCheckBox hideTabBar = null;
	JCheckBox showMe = null;

	// create some reusable borders and layouts
	Border etchedBorder = BorderFactory.createEtchedBorder();
	BorderLayout borderLayout = new BorderLayout();

	//  Selection value for connection
	String connectKey = null;

	public Connect(Frame frame, String title, Properties prop) {

		super(frame, title, true);

		props =
			ConfigureFactory.getInstance().getProperties(
				GlobalConfigure.SESSIONS);

		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void jbInit() throws Exception {

		// make it non resizable
		setResizable(false);

		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		// create sessions panel
		createSessionsPanel();

		// create emulator options panel
		createEmulatorOptionsPanel();

		// create the button options
		createButtonOptions();

		JTabbedPane optionTabs = new JTabbedPane();

		optionTabs.addChangeListener(this);

		optionTabs.addTab(
			LangTool.getString("ss.labelConnections"),
			sessionPanel);
		optionTabs.addTab(LangTool.getString("ss.labelOptions1"), emulOptPanel);

      createAccessPanel();
		optionTabs.addTab("Option Access", accessPanel);

		// add the panels to our dialog
		getContentPane().add(optionTabs, BorderLayout.CENTER);
		getContentPane().add(options, BorderLayout.SOUTH);

		// pack it and center it on the screen
		pack();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = getSize();
		if (frameSize.height > screenSize.height)
			frameSize.height = screenSize.height;
		if (frameSize.width > screenSize.width)
			frameSize.width = screenSize.width;

		setLocation(
			(screenSize.width - frameSize.width) / 2,
			(screenSize.height - frameSize.height) / 2);

		// set default selection value as the first row or default session
		if (sessions.getRowCount() > 0) {
			int selInterval = 0;
			for (int x = 0; x < sessions.getRowCount(); x++) {
				if (((Boolean) ctm.getValueAt(x, 2)).booleanValue())
					selInterval = x;
			}
			sessions.getSelectionModel().setSelectionInterval(
				selInterval,
				selInterval);
		}

		// Oh man what a pain in the ass.  Had to add a window listener to request
		// focus of the sessions list.
		addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {

						sessions.requestFocus();
					}
				});
			}
		});

		// now show the world what we and they can do
		this.setVisible(true);

	}

	public void stateChanged(ChangeEvent e) {

		JTabbedPane p = (JTabbedPane) e.getSource();
		int index = p.getSelectedIndex();
		if (!p
			.getTitleAt(index)
			.equals(LangTool.getString("ss.labelConnections"))) {
			connectButton.setEnabled(false);
			this.setTitle(LangTool.getString("ss.title") + " - " +
				LangTool.getString("ss.labelOptions1"));
			}
		else {
			this.setTitle(LangTool.getString("ss.title") + " - " +
				LangTool.getString("ss.labelConnections"));
			connectButton.setEnabled(true);
		}
	}

	private void createSessionsPanel() {

		// get an instance of our table model
		ctm = new ConfigureTableModel();

		// create a table using our custom table model
		sessions = new JSortTable(ctm);

		// Add enter as default key for connect with this session
		Action connect = new AbstractAction("connect") {
			public void actionPerformed(ActionEvent e) {
				doActionConnect();
			}
		};

		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
		sessions.getInputMap().put(enter, "connect");
		sessions.getActionMap().put("connect", connect);

		sessions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sessions.setPreferredScrollableViewportSize(new Dimension(500, 200));
		sessions.setShowGrid(false);

		//Create the scroll pane and add the table to it.
		scrollPane = new JScrollPane(sessions);
		scrollPane.setVerticalScrollBarPolicy(
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		// This will make the connect dialog react to two clicks instead of having
		//  to click on the selection and then clicking twice
		sessions.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent event) {
				if (event.getClickCount() == 2) {
					doActionConnect();
				}
			}

		});

		//Setup our selection model listener
		rowSM = sessions.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {

				//Ignore extra messages.
				if (e.getValueIsAdjusting())
					return;

				ListSelectionModel lsm = (ListSelectionModel) e.getSource();

				if (lsm.isSelectionEmpty()) {
					//no rows are selected
					editButton.setEnabled(false);
					removeButton.setEnabled(false);
					connectButton.setEnabled(false);
				} else {

					int selectedRow = lsm.getMinSelectionIndex();
					//selectedRow is selected
					editButton.setEnabled(true);
					removeButton.setEnabled(true);
					connectButton.setEnabled(true);
				}
			}
		});

		//Setup panels
		configOptions.setLayout(borderLayout);

		sessionPanel.setLayout(borderLayout);

		configOptions.add(sessionPanel, BorderLayout.CENTER);

		//emptyPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		//emptyPane.setPreferredSize(new Dimension(200, 10));
		sessionOpts.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		sessionOpts.add(scrollPane, BorderLayout.CENTER);

		sessionPanel.add(sessionOpts, BorderLayout.NORTH);
		sessionPanel.add(sessionOptPanel, BorderLayout.SOUTH);
		//sessionPanel.setBorder(BorderFactory.createRaisedBevelBorder());

		// add the option buttons
		addOptButton(LangTool.getString("ss.optAdd"), "ADD", sessionOptPanel);

		removeButton =
			addOptButton(
				LangTool.getString("ss.optDelete"),
				"REMOVE",
				sessionOptPanel,
				false);

		editButton =
			addOptButton(
				LangTool.getString("ss.optEdit"),
				"EDIT",
				sessionOptPanel,
				false);

	}

	private void createEmulatorOptionsPanel() {

		// create emulator options panel
		emulOptPanel.setLayout(new BorderLayout());

		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

		emulOptPanel.add(contentPane, BorderLayout.NORTH);

		// setup the frame interface panel
		interfacePanel = new JPanel(new GridBagLayout());

		TitledBorder tb =
			BorderFactory.createTitledBorder(
				LangTool.getString("conf.labelPresentation"));
		tb.setTitleJustification(TitledBorder.CENTER);

		interfacePanel.setBorder(tb);

		ButtonGroup intGroup = new ButtonGroup();

		// create the checkbox for hiding the tab bar when only one tab exists
		hideTabBar = new JCheckBox(LangTool.getString("conf.labelHideTabBar"));

		hideTabBar.setSelected(false);
		if (props.containsKey("emul.hideTabBar")) {
			if (props.getProperty("emul.hideTabBar").equals("yes"))
				hideTabBar.setSelected(true);
		}

		hideTabBar.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				hideTabBar_itemStateChanged(e);
			}
		});

		intTABS = new JRadioButton(LangTool.getString("conf.labelTABS"));
		intTABS.setSelected(true);
		intTABS.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				intTABS_itemStateChanged(e);
			}
		});
		intMDI = new JRadioButton(LangTool.getString("conf.labelMDI"));

		// add the interface options to the group control
		intGroup.add(intTABS);
		intGroup.add(intMDI);

		if (props.containsKey("emul.interface")) {
			if (props.getProperty("emul.interface").equalsIgnoreCase("MDI"))
				intMDI.setSelected(true);
		}

		gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 10, 5, 10);
		interfacePanel.add(intTABS, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 27, 5, 10);
		interfacePanel.add(hideTabBar, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 10, 10);
		interfacePanel.add(intMDI, gbc);
		//interfacePanel.add(new JLabel());

		// create show me panel
		JPanel showMePanel = new JPanel();
		TitledBorder smb = BorderFactory.createTitledBorder(
			LangTool.getString("ss.title"));
		smb.setTitleJustification(TitledBorder.CENTER);

		showMePanel.setBorder(smb);

		showMe = new JCheckBox(LangTool.getString("ss.labelShowMe"));
		if (props.containsKey("emul.showConnectDialog"))
			showMe.setSelected(true);

		showMe.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				showMe_itemStateChanged(e);
			}
		});

		showMePanel.add(showMe);

		contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		contentPane.add(interfacePanel);
		//contentPane.add(Box.createHorizontalStrut(10));
		contentPane.add(Box.createVerticalStrut(10));
		contentPane.add(showMePanel);
	}

   private void createAccessPanel() {

      TN5250jMultiSelectList sel = new TN5250jMultiSelectList();
      Vector v = new Vector();
      for (int x = 0;x < 40; x++) {

         v.add("option " + x);

      }

      sel.setListData(v);

      int[] indexes = {5,15,20};

      sel.setSelectedIndices(indexes);
      sel.setSourceColumnHeader("Active");
      sel.setSelectionColumnHeader("In Active");

      sel.setSourceHeader("Active");
      sel.setSelectionHeader("In Active");
		// create emulator options panel
		accessPanel.setLayout(new BorderLayout());

      accessPanel.add(sel,BorderLayout.CENTER);

   }

	private void createButtonOptions() {

		connectButton =
			addOptButton(
				LangTool.getString("ss.optConnect"),
				"CONNECT",
				options,
				false);

		applyButton =
			addOptButton(
				LangTool.getString("ss.optApply"),
				"APPLY",
				options,
				true);

		addOptButton(LangTool.getString("ss.optCancel"), "DONE", options);

	}

	private JButton addOptButton(String text, String ac, Container container) {

		return addOptButton(text, ac, container, true);
	}

	private JButton addOptButton(
		String text,
		String ac,
		Container container,
		boolean enabled) {

		JButton button = new JButton(text);
		button.setEnabled(enabled);
		button.setActionCommand(ac);
        button.setPreferredSize(new Dimension(140, 28));

		// we check if there was mnemonic specified and if there was then we
		//    set it.
		int mnemIdx = text.indexOf("&");
		if (mnemIdx >= 0) {
			StringBuffer sb = new StringBuffer(text);
			sb.deleteCharAt(mnemIdx);
			button.setText(sb.toString());
			button.setMnemonic(text.charAt(mnemIdx + 1));
		}
		button.addActionListener(this);
		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		container.add(button);

		return button;
	}

	// Process out button actions
	public void actionPerformed(ActionEvent e) {

		if (e.getActionCommand().equals("DONE")) {
			saveProps();
			setVisible(false);
		}

		if (e.getActionCommand().equals("ADD")) {
			Configure.doEntry((JFrame) getParent(), null, props);
			ctm.addSession();
		}
		if (e.getActionCommand().equals("REMOVE")) {
			removeEntry();
			editButton.setEnabled(false);
			removeButton.setEnabled(false);
		}

		if (e.getActionCommand().equals("EDIT")) {
			int selectedRow = rowSM.getMinSelectionIndex();
			Configure.doEntry(
				(JFrame) getParent(),
				(String) ctm.getValueAt(selectedRow, 0),
				props);
			ctm.chgSession(selectedRow);
		}

		if (e.getActionCommand().equals("CONNECT")) {
			doActionConnect();
		}

		if (e.getActionCommand().equals("APPLY")) {
			saveProps();
		}
	}

	private void doActionConnect() {

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		int selectedRow = rowSM.getMinSelectionIndex();
		connectKey = (String) ctm.getValueAt(selectedRow, 0);
		saveProps();

		// this thread.sleep will get rid of those extra keystrokes that keep
		//    propogating to other peers.  This is a very annoying bug that
		//    should be fixed.  This seems to work through 1.4.0 but in 1.4.1
		//    beta seems to be broken again.  WHY!!!!!
		//try {Thread.sleep(500);}catch(java.lang.InterruptedException ie) {}

		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

		this.dispose();

	}

	public String getConnectKey() {

		return connectKey;
	}

	private void saveProps() {

		ConfigureFactory.getInstance().saveSettings(
			GlobalConfigure.SESSIONS,
			"------ Session Information --------");

	}

	private void addLabelComponent(
		String text,
		Component comp,
		Container container) {

		JLabel label = new JLabel(text);
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		label.setHorizontalTextPosition(JLabel.LEFT);
		container.add(label);
		container.add(comp);

	}

	private void removeEntry() {
		int selectedRow = rowSM.getMinSelectionIndex();
		props.remove(ctm.getValueAt(selectedRow, 0));
		ctm.removeSession(selectedRow);
	}

	void intTABS_itemStateChanged(ItemEvent e) {

		if (intTABS.isSelected()) {
			props.remove("emul.interface");
			hideTabBar.setEnabled(true);

		} else {

			props.setProperty("emul.interface", "MDI");
			hideTabBar.setEnabled(false);

		}

	}

	void hideTabBar_itemStateChanged(ItemEvent e) {

		if (hideTabBar.isSelected())
			props.setProperty("emul.hideTabBar", "yes");
		else
			props.remove("emul.hideTabBar");

	}

	void showMe_itemStateChanged(ItemEvent e) {

		if (showMe.isSelected()) {
			props.setProperty("emul.showConnectDialog", "");
		} else {

			props.remove("emul.showConnectDialog");
		}

	}

	class ConfigureTableModel
		extends AbstractTableModel
		implements SortTableModel {

		final String[] cols =
			{
				LangTool.getString("conf.tableColA"),
				LangTool.getString("conf.tableColB"),
				LangTool.getString("conf.tableColC")};

		Vector mySort = new Vector();
		int sortedColumn = 0;
		boolean isAscending = true;

		public ConfigureTableModel() {
			super();
			resetSorted();
		}

		private void resetSorted() {

			Enumeration e = props.keys();
			mySort.clear();
			int x = 0;
			String ses = null;
			while (e.hasMoreElements()) {
				ses = (String) e.nextElement();

				if (!ses.startsWith("emul.")) {
					mySort.add(ses);
				}
			}

			sortColumn(sortedColumn, isAscending);
		}

		public boolean isSortable(int col) {
			if (col == 0)
				return true;
			else
				return false;
		}

		public void sortColumn(int col, boolean ascending) {
			sortedColumn = col;
			isAscending = ascending;
			Collections.sort(mySort, new SessionComparator(col, ascending));
		}

		public int getColumnCount() {

			return cols.length;
		}

		public String getColumnName(int col) {
			return cols[col];
		}

		public int getRowCount() {
			return mySort.size();
		}

		/*
		 * Implement this so that the default session can be selected.
		 *
		 */
		public void setValueAt(Object value, int row, int col) {

			boolean which = ((Boolean) value).booleanValue();
			if (which)
				props.setProperty("emul.default", getPropValue(row, null));
			else
				props.setProperty("emul.default", "");

		}

		public Object getValueAt(int row, int col) {

			if (col == 0)
				return getPropValue(row, null);
			if (col == 1)
				return getPropValue(row, "0");

			// remember if you change this here you need to change the default
			//    selection option of the main table to pass in the correct
			//    column number.
			if (col == 2) {
				if (getPropValue(row, null)
					.equals(props.getProperty("emul.default", "")))
					return new Boolean(true);
				else
					return new Boolean(false);
			}
			return null;

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
			} else {
				return false;
			}
		}

		/*
		 * JTable uses this method to determine the default renderer/
		 * editor for each cell.  If we didn't implement this method,
		 * then the default column would contain text ("true"/"false"),
		 * rather than a check box.
		 */
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		private String getPropValue(int row, String param) {

			String prop = "";
			String[] args = new String[NUM_PARMS];
			String ses = null;

			prop = (String) mySort.get(row);

			if (param == null)
				return prop;
			else {
				Configure.parseArgs(props.getProperty(prop), args);
				if (param.equals("0"))
					return args[0];
			}
			return null;
		}

		public void addSession() {
			resetSorted();
			fireTableRowsInserted(props.size() - 1, props.size() - 1);
		}

		public void chgSession(int row) {
			resetSorted();
			fireTableRowsUpdated(row, row);
		}

		public void removeSession(int row) {
			resetSorted();
			fireTableRowsDeleted(row, row);
		}

	}

	public class SessionComparator implements Comparator {
		protected int index;
		protected boolean ascending;

		public SessionComparator(int index, boolean ascending) {
			this.index = index;
			this.ascending = ascending;
		}

		public int compare(Object one, Object two) {

			if (one instanceof String && two instanceof String) {

				String s1 = one.toString();
				String s2 = two.toString();
				int result = 0;

				if (ascending)
					result = s1.compareTo(s2);
				else
					result = s2.compareTo(s1);

				if (result < 0) {
					return -1;
				} else if (result > 0) {
					return 1;
				} else
					return 0;
			} else {

				if (one instanceof Boolean && two instanceof Boolean) {
					boolean bOne = ((Boolean) one).booleanValue();
					boolean bTwo = ((Boolean) two).booleanValue();

					if (ascending) {
						if (bOne == bTwo) {
							return 0;
						} else if (bOne) { // Define false < true
							return 1;
						} else {
							return -1;
						}
					} else {
						if (bOne == bTwo) {
							return 0;
						} else if (bTwo) { // Define false < true
							return 1;
						} else {
							return -1;
						}
					}
				} else {
					if (one instanceof Comparable
						&& two instanceof Comparable) {
						Comparable cOne = (Comparable) one;
						Comparable cTwo = (Comparable) two;
						if (ascending) {
							return cOne.compareTo(cTwo);
						} else {
							return cTwo.compareTo(cOne);
						}
					}
				}
				return 1;
			}
		}
	}
}
