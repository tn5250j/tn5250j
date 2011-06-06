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
package org.tn5250j.tools;

import org.tn5250j.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.io.*;
import java.beans.*;
import java.util.*;
import java.text.MessageFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.tn5250j.sql.AS400Xtfr;
import org.tn5250j.sql.SqlWizard;
import org.tn5250j.tools.filters.*;
import org.tn5250j.mailtools.SendEMailDialog;
import org.tn5250j.SessionPanel;
import org.tn5250j.SessionConfig;
import org.tn5250j.gui.GenericTn5250JFrame;
import org.tn5250j.gui.TN5250jFileChooser;
import org.tn5250j.gui.TN5250jFileFilter;
import org.tn5250j.framework.tn5250.tnvt;

public class XTFRFile
	extends GenericTn5250JFrame
	implements ActionListener, FTPStatusListener, ItemListener {

	private static final long serialVersionUID = 1L;
	private FTP5250Prot ftpProtocol;
	private AS400Xtfr axtfr;

	private GridBagConstraints gbc;
	private JTextField user;
	private JPasswordField password;
	private JTextField systemName;
	private JTextField hostFile;
	private JTextField localFile;
	private JRadioButton allFields;
	private JRadioButton selectedFields;
	private JComboBox decimalSeparator;
	private JComboBox fileFormat;
	private JCheckBox useQuery;
	private JButton queryWizard;
	private JTextArea queryStatement;
	private JButton customize;
	private JButton xtfrButton;

	private JRadioButton intDesc;
	private JRadioButton txtDesc;

	private JPanel as400QueryP;
	private JPanel as400p;

	boolean fieldsSelected;
	boolean emailIt;

	tnvt vt;
	XTFRFileFilter htmlFilter;
	XTFRFileFilter KSpreadFilter;
	XTFRFileFilter OOFilter;
	XTFRFileFilter ExcelFilter;
	XTFRFileFilter DelimitedFilter;
	XTFRFileFilter FixedWidthFilter;
	//   XTFRFileFilter ExcelWorkbookFilter;

	// default file filter used.
	XTFRFileFilter fileFilter;

	ProgressMonitor pm;
	JProgressBar progressBar;
	JTextArea taskOutput;
	JLabel fieldsLabel;
	JLabel textDescLabel;
	JLabel label;
	JLabel note;
	ProgressOptionPane monitor;
	JDialog dialog;
	XTFRFileFilter filter;
	SessionPanel session;

	static String messageProgress;

	public XTFRFile(Frame parent, tnvt pvt, SessionPanel session) {

      this(parent, pvt, session, null);
//		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//		this.session = session;
//		vt = pvt;
//		ftpProtocol = new FTP5250Prot(vt);
//		ftpProtocol.addFTPStatusListener(this);
//		axtfr = new AS400Xtfr(vt);
//		axtfr.addFTPStatusListener(this);
//		createProgressMonitor();
//		initFileFilters();
//		initXTFRInfo(null);
//
//		addWindowListener(new WindowAdapter() {
//
//			public void windowClosing(WindowEvent we) {
//				if (ftpProtocol.isConnected())
//					ftpProtocol.disconnect();
//			}
//
//		});
//
//		messageProgress = LangTool.getString("xtfr.messageProgress");
//		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	public XTFRFile(Frame parent, tnvt pvt, SessionPanel session, Properties XTFRProps) {

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		this.session = session;
		vt = pvt;
		ftpProtocol = new FTP5250Prot(vt);
		ftpProtocol.addFTPStatusListener(this);
		axtfr = new AS400Xtfr(vt);
		axtfr.addFTPStatusListener(this);
		createProgressMonitor();
		initFileFilters();
		initXTFRInfo(XTFRProps);

		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent we) {
				if (ftpProtocol != null && ftpProtocol.isConnected())
					ftpProtocol.disconnect();
			}

		});

		messageProgress = LangTool.getString("xtfr.messageProgress");
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	private void initFileFilters() {
		htmlFilter =
			new XTFRFileFilter(
				new String[] { "html", "htm" },
				"Hyper Text Markup Language");
		htmlFilter.setOutputFilterName(
			"org.tn5250j.tools.filters.HTMLOutputFilter");
		KSpreadFilter = new XTFRFileFilter("ksp", "KSpread KDE Spreadsheet");
		KSpreadFilter.setOutputFilterName(
			"org.tn5250j.tools.filters.KSpreadOutputFilter");
		OOFilter = new XTFRFileFilter("sxc", "OpenOffice");
		OOFilter.setOutputFilterName(
			"org.tn5250j.tools.filters.OpenOfficeOutputFilter");
		ExcelFilter = new XTFRFileFilter("xls", "Excel");
		ExcelFilter.setOutputFilterName(
			"org.tn5250j.tools.filters.ExcelOutputFilter");
		DelimitedFilter =
			new XTFRFileFilter(new String[] { "csv", "tab" }, "Delimited");
		DelimitedFilter.setOutputFilterName(
			"org.tn5250j.tools.filters.DelimitedOutputFilter");
		FixedWidthFilter = new XTFRFileFilter("txt", "Fixed Width");
		FixedWidthFilter.setOutputFilterName(
			"org.tn5250j.tools.filters.FixedWidthOutputFilter");
		//      ExcelWorkbookFilter = new XTFRFileFilter("xls", "Excel 95 97 XP 2000");
		//      ExcelWorkbookFilter.setOutputFilterName("org.tn5250j.tools.filters.ExcelWorkbookOutputFilter");
	}

	public void statusReceived(FTPStatusEvent statusevent) {

		if (monitor.isCanceled()) {
			ftpProtocol.setAborted();
		}
      else {
			final int prog = statusevent.getCurrentRecord();
			final int len = statusevent.getFileLength();
			Runnable udp = new Runnable() {
				public void run() {

					if (prog >= len) {

						progressBar.setValue(len);
						label.setText(LangTool.getString("xtfr.labelComplete"));
						note.setText(getTransferredNote(len));
						monitor.setDone();
						if (emailIt)
							emailMe();

					}
               else {
						progressBar.setValue(prog);
						note.setText(getProgressNote(prog, len));
					}
				}
			};
			SwingUtilities.invokeLater(udp);
		}
	}

	private String getProgressNote(int prog, int len) {

		Object[] args = { Integer.toString(prog), Integer.toString(len)};

		try {
			return MessageFormat.format(messageProgress, args);
		}
      catch (Exception exc) {
			System.out.println(" getProgressNote: " + exc.getMessage());
			return "Record " + prog + " of " + len;
		}
	}

	private void emailMe() {

		SendEMailDialog semd =
			new SendEMailDialog(
				(Frame) (this.getParent()),
				session,
				localFile.getText());

	}

	private String getTransferredNote(int len) {

		Object[] args = { Integer.toString(len)};

		try {
			return MessageFormat.format(
				LangTool.getString("xtfr.messageTransferred"),
				args);
		} catch (Exception exc) {
			System.out.println(" getTransferredNote: " + exc.getMessage());
			return len + " records transferred!";
		}
	}

	public void commandStatusReceived(FTPStatusEvent statusevent) {
		final String message = statusevent.getMessage() + '\n';
		Runnable cdp = new Runnable() {
			public void run() {
				taskOutput.setText(taskOutput.getText() + message);
			}
		};
		SwingUtilities.invokeLater(cdp);

	}

	public void fileInfoReceived(FTPStatusEvent statusevent) {

		hostFile.setText(ftpProtocol.getFullFileName(hostFile.getText()));

		if (allFields.isSelected()) {
			doTransfer();
		} else {
			selectFields();
		}
	}

	public void actionPerformed(ActionEvent e) {

      // process the save transfer information button
   	if (e.getActionCommand().equals("SAVE")) {

         saveXTFRInfo();

   	}

      // process the save transfer information button
   	if (e.getActionCommand().equals("LOAD")) {

         loadXTFRInfo();

   	}

		if (e.getActionCommand().equals("XTFR")
			|| e.getActionCommand().equals("EMAIL")) {

			saveXTFRFields();

			if (e.getActionCommand().equals("EMAIL"))
				emailIt = true;
			else
				emailIt = false;

			initializeMonitor();
			dialog.setVisible(true);

			if (useQuery.isSelected()) {

				axtfr.login(user.getText(), new String(password.getPassword()));
				// this will execute in it's own thread and will send a
				//    fileInfoReceived(FTPStatusEvent statusevent) event when
				//    finished without an error.
				axtfr.setDecimalChar(getDecimalChar());
				axtfr.connect(systemName.getText());

			} else {
				if (ftpProtocol!=null && ftpProtocol.connect(systemName.getText(), 21)) {

					if (ftpProtocol
						.login(
							user.getText(),
							new String(password.getPassword()))) {
						// this will execute in it's own thread and will send a
						//    fileInfoReceived(FTPStatusEvent statusevent) event when
						//    finished without an error.
						ftpProtocol.setDecimalChar(getDecimalChar());
						ftpProtocol.getFileInfo(
							hostFile.getText(),
							intDesc.isSelected());
					}
				} else {

					disconnect();
				}
			}
		}

		if (e.getActionCommand().equals("BROWSEPC")) {

			getPCFile();

		}

		if (e.getActionCommand().equals("CUSTOMIZE")) {

			filter.getOutputFilterInstance().setCustomProperties();

		}

	}

	private char getDecimalChar() {
		String ds = (String) decimalSeparator.getSelectedItem();
		return ds.charAt(1);
	}

	private void initializeMonitor() {

		progressBar.setValue(0);
		progressBar.setMinimum(0);
		progressBar.setMaximum(0);
		label.setText(LangTool.getString("xtfr.labelInProgress"));
		note.setText(LangTool.getString("xtfr.labelFileInfo"));
		progressBar.setStringPainted(false);
		monitor.reset();

	}

	private void disconnect() {
		if (ftpProtocol != null) {
			ftpProtocol.disconnect();
			ftpProtocol = null;
		}

	}

	private void doTransfer() {

		progressBar.setMaximum(ftpProtocol.getFileSize());
		progressBar.setStringPainted(true);

		fileFilter = getFilterByDescription();

		if (useQuery.isSelected()) {

			axtfr.setOutputFilter(fileFilter.getOutputFilterInstance());
			axtfr.getFile(
				hostFile.getText(),
				fileFilter.setExtension(localFile.getText()),
				queryStatement.getText().trim(),
				intDesc.isSelected());

		} else {
			ftpProtocol.setOutputFilter(fileFilter.getOutputFilterInstance());

			ftpProtocol.getFile(
				hostFile.getText(),
				fileFilter.setExtension(localFile.getText()));
		}
	}

/* *** NEVER USED LOCALLY ************************************************** */
//	private XTFRFileFilter getFilterByExtension() {
//
//		if (filter != null && filter.isExtensionInList(localFile.getText()))
//			return filter;
//
//		if (KSpreadFilter.isExtensionInList(localFile.getText()))
//			return KSpreadFilter;
//		if (OOFilter.isExtensionInList(localFile.getText()))
//			return OOFilter;
//		if (ExcelFilter.isExtensionInList(localFile.getText()))
//			return ExcelFilter;
//		if (DelimitedFilter.isExtensionInList(localFile.getText()))
//			return DelimitedFilter;
//		if (FixedWidthFilter.isExtensionInList(localFile.getText()))
//			return FixedWidthFilter;
//		//      if (ExcelWorkbookFilter.isExtensionInList(localFile.getText()))
//		//         return ExcelWorkbookFilter;
//
//		return htmlFilter;
//	}

	private XTFRFileFilter getFilterByDescription() {

		String desc = (String) fileFormat.getSelectedItem();

		//      if (filter.getDescription().equals(desc))
		//         return filter;

		if (KSpreadFilter.getDescription().equals(desc))
			return KSpreadFilter;
		if (OOFilter.getDescription().equals(desc))
			return OOFilter;
		if (ExcelFilter.getDescription().equals(desc))
			return ExcelFilter;
		if (DelimitedFilter.getDescription().equals(desc))
			return DelimitedFilter;
		if (FixedWidthFilter.getDescription().equals(desc))
			return FixedWidthFilter;
		//      if (ExcelWorkbookFilter.isExtensionInList(localFile.getText()))
		//         return ExcelWorkbookFilter;

		return htmlFilter;
	}

	private void createProgressMonitor() {

		progressBar = new JProgressBar(0, 0);
		progressBar.setValue(0);

		taskOutput = new JTextArea(5, 20);
		taskOutput.setMargin(new Insets(5, 5, 5, 5));
		taskOutput.setEditable(false);

		JPanel panel = new JPanel();
		note = new JLabel();
		note.setForeground(Color.blue);
		label = new JLabel();
		label.setForeground(Color.blue);
		panel.setLayout(new BorderLayout());
		panel.add(label, BorderLayout.NORTH);
		panel.add(note, BorderLayout.CENTER);
		panel.add(progressBar, BorderLayout.SOUTH);

		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(panel, BorderLayout.NORTH);
		contentPane.add(new JScrollPane(taskOutput), BorderLayout.CENTER);

		monitor = new ProgressOptionPane(contentPane);

		taskOutput.setRows(6);

		dialog =
			monitor.createDialog(
				this,
				LangTool.getString("xtfr.progressTitle"));

	}

	private void startWizard() {

		try {
			SqlWizard wizard =
				new SqlWizard(
					systemName.getText().trim(),
					user.getText(),
					new String(password.getPassword()));

			wizard.setQueryTextArea(queryStatement);
		} catch (NoClassDefFoundError ncdfe) {
			JOptionPane.showMessageDialog(
				this,
				LangTool.getString("messages.noAS400Toolbox"),
				"Error",
				JOptionPane.ERROR_MESSAGE,
				null);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Get the local file from a file chooser
	 */
	private void getPCFile() {

		String workingDir = System.getProperty("user.dir");
		TN5250jFileChooser pcFileChooser = new TN5250jFileChooser(workingDir);

		// set the file filters for the file chooser
		filter = getFilterByDescription();

		pcFileChooser.addChoosableFileFilter(filter);

		int ret = pcFileChooser.showSaveDialog(this);

		// check to see if something was actually chosen
		if (ret == JFileChooser.APPROVE_OPTION) {
			File file = pcFileChooser.getSelectedFile();
			filter = null;
			if (pcFileChooser.getFileFilter() instanceof XTFRFileFilter)
				filter = (XTFRFileFilter) pcFileChooser.getFileFilter();
			else
				filter = htmlFilter;

			localFile.setText(filter.setExtension(file));

		}

	}

	/**
	 * Creates the dialog components for prompting the user for the information
	 * of the transfer
	 */
	private void initXTFRInfo(Properties XTFRProps) {

		// create some reusable borders and layouts
		BorderLayout borderLayout = new BorderLayout();
		Border emptyBorder = BorderFactory.createEmptyBorder(10, 10, 0, 10);

		// main panel
		JPanel mp = new JPanel();
		mp.setLayout(borderLayout);

		// system panel
		JPanel sp = new JPanel();
		sp.setLayout(new BorderLayout());
		sp.setBorder(emptyBorder);

		// host panel for as400
		as400p = new JPanel();
		as400p.setBorder(
			BorderFactory.createTitledBorder(
				LangTool.getString("xtfr.labelAS400")));

		as400p.setLayout(new GridBagLayout());

		JLabel snpLabel =
			new JLabel(LangTool.getString("xtfr.labelSystemName"));

		systemName = new JTextField(vt.getHostName());
		systemName.setColumns(30);

		gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy =0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 5, 5);
		as400p.add(snpLabel, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 1; gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 10);
		as400p.add(systemName,gbc);
		JLabel hfnpLabel = new JLabel(LangTool.getString("xtfr.labelHostFile"));
		gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 5, 5);
		as400p.add(hfnpLabel, gbc);
		hostFile = new JTextField();
		hostFile.setColumns(30);
		gbc = new GridBagConstraints();
		gbc.gridx = 1; gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 10);
		as400p.add(hostFile, gbc);
		JLabel idpLabel = new JLabel(LangTool.getString("xtfr.labelUserId"));
		gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 5, 5);
		as400p.add(idpLabel, gbc);
		user = new JTextField();
		user.setColumns(15);
		gbc = new GridBagConstraints();
		gbc.gridx = 1; gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 5);
		as400p.add(user, gbc);
		// password panel
		JLabel pwpLabel = new JLabel(LangTool.getString("xtfr.labelPassword"));
		gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 5, 5);
		as400p.add(pwpLabel, gbc);
		password = new JPasswordField();
		password.setColumns(15);
                password.addKeyListener(new java.awt.event.KeyAdapter() {
                    public void keyPressed(java.awt.event.KeyEvent evt) {
                        txtONKeyPressed(evt);
                    }
                });
		gbc = new GridBagConstraints();
		gbc.gridx = 1; gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 5);
		as400p.add(password, gbc);
		// Query Wizard
		useQuery = new JCheckBox(LangTool.getString("xtfr.labelUseQuery"));
		useQuery.addItemListener(this);

		gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 5, 5);
		as400p.add(useQuery, gbc);
		//query button
		queryWizard = new JButton(LangTool.getString("xtfr.labelQueryWizard"));
		queryWizard.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startWizard();
			}
		});
		queryWizard.setEnabled(false);
		gbc = new GridBagConstraints();
		gbc.gridx = 1; gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 5, 0, 5);
		as400p.add(queryWizard, gbc);
		// Field Selection panel
		fieldsLabel = new JLabel(LangTool.getString("xtfr.labelFields"));
		gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 5;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 5, 5);
		as400p.add(fieldsLabel, gbc);
		allFields = new JRadioButton(LangTool.getString("xtfr.labelAllFields"));
		allFields.setSelected(true);
		gbc = new GridBagConstraints();
		gbc.gridx = 1; gbc.gridy = 5;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(0, 5, 0, 5);
		as400p.add(allFields, gbc);
		selectedFields =
			new JRadioButton(LangTool.getString("xtfr.labelSelectedFields"));
		gbc = new GridBagConstraints();
		gbc.gridx = 2; gbc.gridy = 5;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(0, 5, 0, 10);
		as400p.add(selectedFields, gbc);
		ButtonGroup fieldGroup = new ButtonGroup();
		fieldGroup.add(allFields);
		fieldGroup.add(selectedFields);
		// Field Text Description panel
		textDescLabel =
			new JLabel(LangTool.getString("xtfr.labelTxtDesc"));
		gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 6;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 5, 5);
		as400p.add(textDescLabel, gbc);
		txtDesc = new JRadioButton(LangTool.getString("xtfr.labelTxtDescFull"));
		txtDesc.setSelected(true);
		gbc = new GridBagConstraints();
		gbc.gridx = 1; gbc.gridy = 6;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(0, 5, 5, 5);
		as400p.add(txtDesc, gbc);
		intDesc = new JRadioButton(LangTool.getString("xtfr.labelTxtDescInt"));
		gbc = new GridBagConstraints();
		gbc.gridx = 2; gbc.gridy = 6;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(0, 5, 5, 10);
		as400p.add(intDesc, gbc);
		ButtonGroup txtDescGroup = new ButtonGroup();
		txtDescGroup.add(txtDesc);
		txtDescGroup.add(intDesc);

		// pc panel for pc information
		JPanel pcp = new JPanel(new GridBagLayout());
		pcp.setBorder(
			BorderFactory.createTitledBorder(
				LangTool.getString("xtfr.labelpc")));

		JLabel pffLabel =
			new JLabel(LangTool.getString("xtfr.labelFileFormat"));
		gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 5, 5);
		pcp.add(pffLabel, gbc);
		fileFormat = new JComboBox();
		fileFormat.setPreferredSize(new Dimension(220, 25));
		fileFormat.addItem(htmlFilter.getDescription());
		fileFormat.addItem(OOFilter.getDescription());
		fileFormat.addItem(ExcelFilter.getDescription());
		fileFormat.addItem(KSpreadFilter.getDescription());
		fileFormat.addItem(DelimitedFilter.getDescription());
		fileFormat.addItem(FixedWidthFilter.getDescription());
		fileFormat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				filter = getFilterByDescription();
				if (filter.getOutputFilterInstance().isCustomizable())
					customize.setEnabled(true);
				else
					customize.setEnabled(false);
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 1; gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 5, 0, 5);
		pcp.add(fileFormat, gbc);
		customize = new JButton(LangTool.getString("xtfr.labelCustomize"));
		customize.setPreferredSize(new Dimension(110, 25));
		customize.setActionCommand("CUSTOMIZE");
		customize.addActionListener(this);
		gbc = new GridBagConstraints();
		gbc.gridx = 2; gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 5, 0, 10);
		pcp.add(customize, gbc);
		// now make sure we set the customizable button enabled or not
		// depending on the filter.
		fileFormat.setSelectedIndex(0);

		JLabel pcpLabel = new JLabel(LangTool.getString("xtfr.labelPCFile"));
		gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 5, 5);
		pcp.add(pcpLabel, gbc);
		localFile = new JTextField();
		localFile.setColumns(15);
		gbc = new GridBagConstraints();
		gbc.gridx = 1; gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 5, 0, 5);
		pcp.add(localFile, gbc);
		JButton browsePC =
			new JButton(LangTool.getString("xtfr.labelPCBrowse"));
		browsePC.setActionCommand("BROWSEPC");
		browsePC.addActionListener(this);
		browsePC.setPreferredSize(new Dimension(110, 25));
		gbc = new GridBagConstraints();
		gbc.gridx = 2; gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 5, 0, 10);
		pcp.add(browsePC, gbc);

		decimalSeparator = new JComboBox();
		decimalSeparator.setPreferredSize(new Dimension(220, 25));
		decimalSeparator.addItem(LangTool.getString("xtfr.period"));
		decimalSeparator.addItem(LangTool.getString("xtfr.comma"));

		// obtain the decimal separator for the machine locale
		DecimalFormat formatter =
			(DecimalFormat) NumberFormat.getInstance(Locale.getDefault());

		if (formatter.getDecimalFormatSymbols().getDecimalSeparator() == '.')
			decimalSeparator.setSelectedIndex(0);
		else
			decimalSeparator.setSelectedIndex(1);
		gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 10, 5);
		pcp.add(new JLabel(LangTool.getString("xtfr.labelDecimal")), gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 1; gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 5, 5, 5);
		pcp.add(decimalSeparator, gbc);

		sp.add(as400p, BorderLayout.NORTH);
		sp.add(pcp, BorderLayout.SOUTH);

		// options panel
		JPanel op = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		xtfrButton = new JButton(LangTool.getString("xtfr.labelXTFR"));
		xtfrButton.addActionListener(this);
		xtfrButton.setActionCommand("XTFR");
		op.add(xtfrButton);

		JButton emailButton =
			new JButton(LangTool.getString("xtfr.labelXTFREmail"));
		emailButton.addActionListener(this);
		emailButton.setActionCommand("EMAIL");
		op.add(emailButton);

      // add transfer save information button
		JButton saveButton =
			new JButton(LangTool.getString("xtfr.labelXTFRSave"));
		saveButton.addActionListener(this);
		saveButton.setActionCommand("SAVE");
		op.add(saveButton);

      // add transfer load information button
		JButton loadButton =
			new JButton(LangTool.getString("xtfr.labelXTFRLoad"));
		loadButton.addActionListener(this);
		loadButton.setActionCommand("LOAD");
		op.add(loadButton);

		mp.add(sp, BorderLayout.CENTER);
		mp.add(op, BorderLayout.SOUTH);

		this.getContentPane().add(mp, BorderLayout.CENTER);

		//      this.setModal(false);
		//      this.setModal(true);
		this.setTitle(LangTool.getString("xtfr.title"));

		//QueryPanel when Use Query selected
		as400QueryP = new JPanel();
		as400QueryP.setLayout(new BorderLayout());

		queryStatement = new JTextArea(2, 40);
		JScrollPane scrollPane = new JScrollPane(queryStatement);
		queryStatement.setLineWrap(true);
		as400QueryP.add(scrollPane, BorderLayout.CENTER);

		initXTFRFields(XTFRProps);

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

		// now show the world what we can do
		setVisible(true);

	}
        
        private void txtONKeyPressed(java.awt.event.KeyEvent evt) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                xtfrButton.doClick();
            }
        }
	
        private void initXTFRFields(Properties props) {

      if (props == null) {
         SessionConfig config = session.getSession().getConfiguration();
         props = config.getProperties();
      }

		if (props.containsKey("xtfr.fileName"))
			hostFile.setText(props.getProperty("xtfr.fileName"));

		if (props.containsKey("xtfr.user"))
			user.setText(props.getProperty("xtfr.user"));

		if (props.containsKey("xtfr.useQuery")) {
			if (props.getProperty("xtfr.useQuery").equals("true"))
				useQuery.setSelected(true);
			else
				useQuery.setSelected(false);
		}

		if (props.containsKey("xtfr.queryStatement")) {
			queryStatement.setText(props.getProperty("xtfr.queryStatement"));
		}

		if (props.containsKey("xtfr.allFields")) {
			if (props.getProperty("xtfr.allFields").equals("true"))
				allFields.setSelected(true);
			else
				allFields.setSelected(false);
		}

		if (props.containsKey("xtfr.txtDesc")) {
			if (props.getProperty("xtfr.txtDesc").equals("true"))
				txtDesc.setSelected(true);
			else
				txtDesc.setSelected(false);
		}

		if (props.containsKey("xtfr.intDesc")) {
			if (props.getProperty("xtfr.intDesc").equals("true")) {
				intDesc.setSelected(true);
			} else {
				intDesc.setSelected(false);
			}
		}

		if (props.containsKey("xtfr.fileFormat"))
			fileFormat.setSelectedItem(props.getProperty("xtfr.fileFormat"));

		if (props.containsKey("xtfr.localFile"))
			localFile.setText(props.getProperty("xtfr.localFile"));

		if (props.containsKey("xtfr.decimalSeparator"))
			decimalSeparator.setSelectedItem(
				props.get("xtfr.decimalSeparator"));

	}

	private void saveXTFRFields() {

		SessionConfig config = session.getSession().getConfiguration();
		Properties props = config.getProperties();

		saveXTFRFields(props);

		config.setModified();

	}

	private void saveXTFRFields(Properties props) {

		if (hostFile.getText().trim().length() > 0)
			props.setProperty("xtfr.fileName", hostFile.getText().trim());
		else
			props.remove("xtfr.fileName");

		if (user.getText().trim().length() > 0)
			props.setProperty("xtfr.user", user.getText().trim());
		else
			props.remove("xtfr.user");

		if (useQuery.isSelected())
			props.setProperty("xtfr.useQuery", "true");
		else
			props.remove("xtfr.useQuery");

		if (queryStatement.getText().trim().length() > 0)
			props.setProperty(
				"xtfr.queryStatement",
				queryStatement.getText().trim());
		else
			props.remove("xtfr.queryStatement");

		if (allFields.isSelected())
			props.setProperty("xtfr.allFields", "true");
		else
			props.remove("xtfr.allFields");

        // TODO: save Fielddesc state as one propertyvalue (xtfr.fieldDesc=txt|int)  
		if (txtDesc.isSelected())
			props.setProperty("xtfr.txtDesc", "true");
		else
			props.remove("xtfr.txtDesc");
		if (intDesc.isSelected())
			props.setProperty("xtfr.intDesc", "true");
		else
			props.remove("xtfr.intDesc");

		props.setProperty(
			"xtfr.fileFormat",
			(String) fileFormat.getSelectedItem());

		if (localFile.getText().trim().length() > 0)
			props.setProperty("xtfr.localFile", localFile.getText().trim());
		else
			props.remove("xtfr.localFile");

		props.setProperty(
			"xtfr.decimalSeparator",
			(String) decimalSeparator.getSelectedItem());

	}

	private void saveXTFRInfo() {

      Properties xtfrProps = new Properties();
      xtfrProps.setProperty("xtfr.destination","FROM");
      this.saveXTFRFields(xtfrProps);
		String workingDir = System.getProperty("user.dir");
		TN5250jFileChooser pcFileChooser = new TN5250jFileChooser(workingDir);

      // set the file filters for the file chooser
      TN5250jFileFilter filter = new TN5250jFileFilter("dtf","Transfer from AS/400");

      pcFileChooser.addChoosableFileFilter(filter );

		int ret = pcFileChooser.showSaveDialog(this);

		// check to see if something was actually chosen
		if (ret == JFileChooser.APPROVE_OPTION) {

			File file = pcFileChooser.getSelectedFile();

         file = new File(filter.setExtension(file));

         try {
            FileOutputStream out = new FileOutputStream(file);
               // save off the width and height to be restored later
            xtfrProps.store(out,"------ Transfer Details --------");

            out.flush();
            out.close();
         }
         catch (FileNotFoundException fnfe) {}
         catch (IOException ioe) {}


		}

	}

	private void loadXTFRInfo() {

      Properties xtfrProps = new Properties();
//      xtfrProps.setProperty("xtfr.destination","FROM");
//      this.saveXTFRFields(xtfrProps);
		String workingDir = System.getProperty("user.dir");
		TN5250jFileChooser pcFileChooser = new TN5250jFileChooser(workingDir);

      // set the file filters for the file chooser
      TN5250jFileFilter filter = new TN5250jFileFilter("dtf","Transfer from AS/400");

      pcFileChooser.addChoosableFileFilter(filter );

		int ret = pcFileChooser.showOpenDialog(this);

		// check to see if something was actually chosen
		if (ret == JFileChooser.APPROVE_OPTION) {

			File file = pcFileChooser.getSelectedFile();

         try {
            FileInputStream in = new FileInputStream(file);
               // save off the width and height to be restored later
            xtfrProps.load(in);

            in.close();
         }
         catch (FileNotFoundException fnfe) {}
         catch (IOException ioe) {}


		}

      if (xtfrProps.containsKey("xtfr.destination") &&
               (xtfrProps.get("xtfr.destination").equals("FROM"))) {

         this.initXTFRFields(xtfrProps);
      }

	}

	/** Listens to the use query check boxe */
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		if (source == useQuery) {
			if (useQuery.isSelected()) {
				queryWizard.setEnabled(true);
				as400p.remove(fieldsLabel);
				as400p.remove(allFields);
				as400p.remove(selectedFields);
				as400p.remove(textDescLabel);
				as400p.remove(txtDesc);
				as400p.remove(intDesc);
				gbc = new GridBagConstraints();
				gbc.gridx = 0; gbc.gridy = 5;
				gbc.gridheight = 2;
				gbc.gridwidth = 3;
				gbc.fill = GridBagConstraints.BOTH;
				gbc.anchor = GridBagConstraints.WEST;
				gbc.insets = new Insets(5, 10, 10, 10);
				as400p.add(as400QueryP, gbc);
			} else {
				queryWizard.setEnabled(false);
				as400p.remove(as400QueryP);
				gbc = new GridBagConstraints();
				gbc.gridx = 0; gbc.gridy = 5;
				gbc.anchor = GridBagConstraints.WEST;
				gbc.insets = new Insets(5, 10, 5, 5);
				as400p.add(fieldsLabel, gbc);
				allFields.setSelected(true);
				gbc = new GridBagConstraints();
				gbc.gridx = 1; gbc.gridy = 5;
				gbc.anchor = GridBagConstraints.CENTER;
				gbc.insets = new Insets(0, 5, 0, 5);
				as400p.add(allFields, gbc);
				gbc = new GridBagConstraints();
				gbc.gridx = 2; gbc.gridy = 5;
				gbc.anchor = GridBagConstraints.CENTER;
				gbc.insets = new Insets(0, 5, 0, 10);
				as400p.add(selectedFields, gbc);
				// Field Text Description panel
				gbc = new GridBagConstraints();
				gbc.gridx = 0; gbc.gridy = 6;
				gbc.anchor = GridBagConstraints.WEST;
				gbc.insets = new Insets(5, 10, 5, 5);
				as400p.add(textDescLabel, gbc);
				txtDesc.setSelected(true);
				gbc = new GridBagConstraints();
				gbc.gridx = 1; gbc.gridy = 6;
				gbc.anchor = GridBagConstraints.CENTER;
				gbc.insets = new Insets(0, 5, 5, 5);
				as400p.add(txtDesc, gbc);
				gbc = new GridBagConstraints();
				gbc.gridx = 2; gbc.gridy = 6;
				gbc.anchor = GridBagConstraints.CENTER;
				gbc.insets = new Insets(0, 5, 5, 10);
				as400p.add(intDesc, gbc);
			}
			this.validate();
			this.repaint();
		}
	}

	private void selectFields() {

		FFDTableModel ffdtm = new FFDTableModel();

		//Create table to hold field data
		JTable fields = new JTable(ffdtm);
		fields.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fields.setPreferredScrollableViewportSize(new Dimension(500, 200));

		//Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(fields);
		scrollPane.setVerticalScrollBarPolicy(
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JPanel jpm = new JPanel();
		jpm.add(scrollPane);

		Object[] message = new Object[1];
		message[0] = jpm;
		String[] options =
			{
				LangTool.getString("xtfr.tableSelectAll"),
				LangTool.getString("xtfr.tableSelectNone"),
				LangTool.getString("xtfr.tableDone")};

		int result = 0;
		while (result != 2) {
				result =
					JOptionPane.showOptionDialog(null,
			// the parent that the dialog blocks
		message, // the dialog message array
		LangTool.getString("xtfr.titleFieldSelection"),
			// the title of the dialog window
		JOptionPane.DEFAULT_OPTION, // option type
		JOptionPane.PLAIN_MESSAGE, // message type
		null, // optional icon, use null to use the default icon
		options, // options string array, will be made into buttons//
		options[1] // option that should be made into a default button
	);

			switch (result) {
				case 0 : // Select all
					ftpProtocol.selectAll();
					break;
				case 1 : // Select none
					ftpProtocol.selectNone();
					break;
				default :
					fieldsSelected = ftpProtocol.isFieldsSelected();
					if (ftpProtocol.isFieldsSelected())
						doTransfer();
					break;
			}
		}
	}

	/**
	 * Table model for File Field Definitions
	 */
	class FFDTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		final String[] cols =
			{
				LangTool.getString("xtfr.tableColA"),
				LangTool.getString("xtfr.tableColB")};

		public FFDTableModel() {
			super();

		}

		public int getColumnCount() {

			return cols.length;
		}

		public String getColumnName(int col) {
			return cols[col];
		}

		public int getRowCount() {

			return ftpProtocol.getNumberOfFields();
		}

		public Object getValueAt(int row, int col) {

			if (col == 0) {

				return new Boolean(ftpProtocol.isFieldSelected(row));

			}
			if (col == 1)
				return ftpProtocol.getFieldName(row);

			return null;

		}

		public Class<?> getColumnClass(int col) {
			return getValueAt(0, col).getClass();

		}

		public boolean isCellEditable(int row, int col) {
			if (col == 0)
				return true;
			else
				return false;

		}

		public void setValueAt(Object value, int row, int col) {

			fireTableCellUpdated(row, col);
			ftpProtocol.setFieldSelected(row, ((Boolean) value).booleanValue());

		}
	}

	/**
	 * Create a option pane to show status of the transfer
	 */
	private class ProgressOptionPane extends JOptionPane {

		private static final long serialVersionUID = 1L;

		ProgressOptionPane(Object messageList) {

			super(
				messageList,
				JOptionPane.INFORMATION_MESSAGE,
				JOptionPane.DEFAULT_OPTION,
				null,
				new Object[] {
					 UIManager.getString("OptionPane.cancelButtonText")},
				null);
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		}

		public void setDone() {
			Object[] option = this.getOptions();
			option[0] = LangTool.getString("xtfr.tableDone");
			this.setOptions(option);
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

		public void reset() {

			Object[] option = this.getOptions();
			option[0] = UIManager.getString("OptionPane.cancelButtonText");
			this.setOptions(option);
			monitor.setValue(null);

		}

		public int getMaxCharactersPerLineCount() {
			return 60;
		}

		/**
		 * Returns true if the user hits the Cancel button in the progress dialog.
		 *
		 * @return whether or not dialog was cancelled
		 */
		public boolean isCanceled() {
			if (this == null)
				return false;
			Object v = this.getValue();
			return (v != null);
		}

		// Equivalent to JOptionPane.createDialog,
		// but create a modeless dialog.
		// This is necessary because the Solaris implementation doesn't
		// support Dialog.setModal yet.
		public JDialog createDialog(Component parentComponent, String title) {

			Frame frame = JOptionPane.getFrameForComponent(parentComponent);
			final JDialog dialog = new JDialog(frame, title, false);
			Container contentPane = dialog.getContentPane();

			contentPane.setLayout(new BorderLayout());
			contentPane.add(this, BorderLayout.CENTER);
			dialog.pack();
			dialog.setLocationRelativeTo(parentComponent);
			dialog.addWindowListener(new WindowAdapter() {
				boolean gotFocus = false;

				public void windowClosing(WindowEvent we) {
					setValue(null);
				}

				public void windowActivated(WindowEvent we) {
					// Once window gets focus, set initial focus
					if (!gotFocus) {
						selectInitialValue();
						gotFocus = true;
					}
				}
			});

			addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					if (dialog.isVisible() 
						&& event.getSource() == ProgressOptionPane.this
						&& (event.getPropertyName().equals(VALUE_PROPERTY)
							|| event.getPropertyName().equals(
								INPUT_VALUE_PROPERTY))) {
						if (ftpProtocol != null) {
							ftpProtocol.setAborted();
						}
						dialog.setVisible(false);
						dialog.dispose();
					}
				}
			});
			return dialog;
		}
	}

}
