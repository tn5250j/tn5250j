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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.tn5250j.encoding.CharMappings;
import org.tn5250j.gui.model.EmulSession;
import org.tn5250j.gui.model.SslType;

public class SessionPropsDialog extends JDialog implements ActionListener {

	private final JPanel contentPanel = new JPanel();
	private JTextField txtSystemName;
	private JTextField txtHost;
	private JSpinner spinnerPort;
	private JTextField txtDeviceName;
	private JTextField txtConfigFile;
	private JTextField txtProxyHost;
	private JSpinner spinnerProxyPort;
	private JCheckBox cbUsePcDeviceName;
	private JComboBox comboSslType;
	private JComboBox comboCodepage;
	private JCheckBox cbUseToolbox;
	private JCheckBox cbUse132;
	private JCheckBox cbPing;
	private JCheckBox cbExtendedMode;
	private JCheckBox cbUseSystemName;
	private JCheckBox cbOpenNewFrame;
	private JCheckBox cbMonitorSessionStart;
	private JCheckBox cbStartNewVm;
	private JCheckBox cbUseProxy;

	private EmulSession session;
	private JButton btChooseFile;
	private JButton okButton;
	private JButton cancelButton;

	private volatile boolean dialogResult;
	private static final String ACTION_OK = "OK";
	private static final String ACTION_CANCEL = "CANCEL";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
			SessionPropsDialog dialog = new SessionPropsDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public SessionPropsDialog() {
		setModal(true);
		setBounds(100, 100, 500, 600);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setPreferredSize(new Dimension(400, 400));
			contentPanel.add(scrollPane);
			{
				JPanel panel = new JPanel();
				scrollPane.setViewportView(panel);
				GridBagLayout gbl_panel = new GridBagLayout();
				gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
				gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
				gbl_panel.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
				gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
				panel.setLayout(gbl_panel);
				{
					JLabel lblSystemName = new JLabel("System name");
					GridBagConstraints gbc_lblSystemName = new GridBagConstraints();
					gbc_lblSystemName.insets = new Insets(10, 10, 5, 5);
					gbc_lblSystemName.anchor = GridBagConstraints.WEST;
					gbc_lblSystemName.gridx = 1;
					gbc_lblSystemName.gridy = 0;
					panel.add(lblSystemName, gbc_lblSystemName);
				}
				{
					txtSystemName = new JTextField();
					GridBagConstraints gbc_txtSystemName = new GridBagConstraints();
					gbc_txtSystemName.fill = GridBagConstraints.HORIZONTAL;
					gbc_txtSystemName.anchor = GridBagConstraints.WEST;
					gbc_txtSystemName.insets = new Insets(10, 0, 5, 10);
					gbc_txtSystemName.gridx = 2;
					gbc_txtSystemName.gridy = 0;
					panel.add(txtSystemName, gbc_txtSystemName);
					txtSystemName.setColumns(10);
				}
				{
					JLabel lblHost = new JLabel("Host");
					GridBagConstraints gbc_lblHost = new GridBagConstraints();
					gbc_lblHost.anchor = GridBagConstraints.WEST;
					gbc_lblHost.insets = new Insets(0, 10, 5, 5);
					gbc_lblHost.gridx = 1;
					gbc_lblHost.gridy = 1;
					panel.add(lblHost, gbc_lblHost);
				}
				{
					txtHost = new JTextField();
					GridBagConstraints gbc_txtHost = new GridBagConstraints();
					gbc_txtHost.fill = GridBagConstraints.HORIZONTAL;
					gbc_txtHost.anchor = GridBagConstraints.WEST;
					gbc_txtHost.insets = new Insets(0, 0, 5, 10);
					gbc_txtHost.gridx = 2;
					gbc_txtHost.gridy = 1;
					panel.add(txtHost, gbc_txtHost);
					txtHost.setColumns(10);
				}
				{
					JLabel lblPort = new JLabel("Port");
					GridBagConstraints gbc_lblPort = new GridBagConstraints();
					gbc_lblPort.anchor = GridBagConstraints.WEST;
					gbc_lblPort.insets = new Insets(0, 10, 5, 5);
					gbc_lblPort.gridx = 1;
					gbc_lblPort.gridy = 2;
					panel.add(lblPort, gbc_lblPort);
				}
				{
					spinnerPort = new JSpinner();
					spinnerPort.setModel(new SpinnerNumberModel(23, 1, 65535, 1));
					GridBagConstraints gbc_spinnerPort = new GridBagConstraints();
					gbc_spinnerPort.anchor = GridBagConstraints.WEST;
					gbc_spinnerPort.insets = new Insets(0, 0, 5, 10);
					gbc_spinnerPort.gridx = 2;
					gbc_spinnerPort.gridy = 2;
					panel.add(spinnerPort, gbc_spinnerPort);
				}
				{
					JLabel lblDeviceName = new JLabel("Device name");
					GridBagConstraints gbc_lblDeviceName = new GridBagConstraints();
					gbc_lblDeviceName.anchor = GridBagConstraints.WEST;
					gbc_lblDeviceName.insets = new Insets(0, 10, 5, 5);
					gbc_lblDeviceName.gridx = 1;
					gbc_lblDeviceName.gridy = 3;
					panel.add(lblDeviceName, gbc_lblDeviceName);
				}
				{
					txtDeviceName = new JTextField();
					GridBagConstraints gbc_txtDeviceName = new GridBagConstraints();
					gbc_txtDeviceName.fill = GridBagConstraints.HORIZONTAL;
					gbc_txtDeviceName.anchor = GridBagConstraints.WEST;
					gbc_txtDeviceName.insets = new Insets(0, 0, 5, 10);
					gbc_txtDeviceName.gridx = 2;
					gbc_txtDeviceName.gridy = 3;
					panel.add(txtDeviceName, gbc_txtDeviceName);
					txtDeviceName.setColumns(10);
				}
				{
					cbUsePcDeviceName = new JCheckBox("Use PC name as device name");
					cbUsePcDeviceName.setHorizontalAlignment(SwingConstants.LEFT);
					GridBagConstraints gbc_cbUsePcDeviceName = new GridBagConstraints();
					gbc_cbUsePcDeviceName.insets = new Insets(0, 0, 5, 10);
					gbc_cbUsePcDeviceName.anchor = GridBagConstraints.WEST;
					gbc_cbUsePcDeviceName.gridx = 2;
					gbc_cbUsePcDeviceName.gridy = 4;
					panel.add(cbUsePcDeviceName, gbc_cbUsePcDeviceName);
				}
				{
					JLabel lblSslType = new JLabel("SSL Type");
					GridBagConstraints gbc_lblSslType = new GridBagConstraints();
					gbc_lblSslType.anchor = GridBagConstraints.WEST;
					gbc_lblSslType.insets = new Insets(0, 10, 5, 5);
					gbc_lblSslType.gridx = 1;
					gbc_lblSslType.gridy = 5;
					panel.add(lblSslType, gbc_lblSslType);
				}
				{
					comboSslType = new JComboBox();
					GridBagConstraints gbc_comboSslType = new GridBagConstraints();
					gbc_comboSslType.insets = new Insets(0, 0, 5, 10);
					gbc_comboSslType.anchor = GridBagConstraints.WEST;
					gbc_comboSslType.gridx = 2;
					gbc_comboSslType.gridy = 5;
					panel.add(comboSslType, gbc_comboSslType);
				}
				{
					JLabel lblCodepage = new JLabel("Codepage");
					GridBagConstraints gbc_lblCodepage = new GridBagConstraints();
					gbc_lblCodepage.anchor = GridBagConstraints.WEST;
					gbc_lblCodepage.insets = new Insets(0, 10, 5, 5);
					gbc_lblCodepage.gridx = 1;
					gbc_lblCodepage.gridy = 6;
					panel.add(lblCodepage, gbc_lblCodepage);
				}
				{
					comboCodepage = new JComboBox();
					GridBagConstraints gbc_comboCodepage = new GridBagConstraints();
					gbc_comboCodepage.insets = new Insets(0, 0, 5, 10);
					gbc_comboCodepage.anchor = GridBagConstraints.WEST;
					gbc_comboCodepage.gridx = 2;
					gbc_comboCodepage.gridy = 6;
					panel.add(comboCodepage, gbc_comboCodepage);
				}
				{
					cbUseToolbox = new JCheckBox("Use AS400 Toolbox");
					GridBagConstraints gbc_cbUseToolbox = new GridBagConstraints();
					gbc_cbUseToolbox.insets = new Insets(0, 0, 5, 10);
					gbc_cbUseToolbox.anchor = GridBagConstraints.WEST;
					gbc_cbUseToolbox.gridx = 2;
					gbc_cbUseToolbox.gridy = 7;
					panel.add(cbUseToolbox, gbc_cbUseToolbox);
				}
				{
					JLabel lblAnzeige = new JLabel("Anzeige");
					GridBagConstraints gbc_lblAnzeige = new GridBagConstraints();
					gbc_lblAnzeige.anchor = GridBagConstraints.WEST;
					gbc_lblAnzeige.insets = new Insets(0, 10, 5, 5);
					gbc_lblAnzeige.gridx = 1;
					gbc_lblAnzeige.gridy = 8;
					panel.add(lblAnzeige, gbc_lblAnzeige);
				}
				{
					cbUse132 = new JCheckBox("Use 132x27");
					GridBagConstraints gbc_cbUse132 = new GridBagConstraints();
					gbc_cbUse132.insets = new Insets(0, 0, 5, 10);
					gbc_cbUse132.anchor = GridBagConstraints.WEST;
					gbc_cbUse132.gridx = 2;
					gbc_cbUse132.gridy = 8;
					panel.add(cbUse132, gbc_cbUse132);
				}
				{
					JLabel lblKeepAlive = new JLabel("Keep alive");
					GridBagConstraints gbc_lblKeepAlive = new GridBagConstraints();
					gbc_lblKeepAlive.anchor = GridBagConstraints.WEST;
					gbc_lblKeepAlive.insets = new Insets(0, 10, 5, 5);
					gbc_lblKeepAlive.gridx = 1;
					gbc_lblKeepAlive.gridy = 9;
					panel.add(lblKeepAlive, gbc_lblKeepAlive);
				}
				{
					cbPing = new JCheckBox("Every 15 soconds");
					GridBagConstraints gbc_cbPing = new GridBagConstraints();
					gbc_cbPing.insets = new Insets(0, 0, 5, 10);
					gbc_cbPing.anchor = GridBagConstraints.WEST;
					gbc_cbPing.gridx = 2;
					gbc_cbPing.gridy = 9;
					panel.add(cbPing, gbc_cbPing);
				}
				{
					JSeparator separator = new JSeparator();
					GridBagConstraints gbc_separator = new GridBagConstraints();
					gbc_separator.gridwidth = 2;
					gbc_separator.fill = GridBagConstraints.HORIZONTAL;
					gbc_separator.insets = new Insets(5, 5, 5, 5);
					gbc_separator.gridx = 1;
					gbc_separator.gridy = 10;
					panel.add(separator, gbc_separator);
				}
				{
					btChooseFile = new JButton("...");
					GridBagConstraints gbc_btChooseFile = new GridBagConstraints();
					gbc_btChooseFile.insets = new Insets(0, 5, 5, 0);
					gbc_btChooseFile.gridx = 2;
					gbc_btChooseFile.gridy = 11;
					panel.add(btChooseFile, gbc_btChooseFile);
				}
				{
					JLabel lblKonfigurationFile = new JLabel("Konfiguration file");
					GridBagConstraints gbc_lblKonfigurationFile = new GridBagConstraints();
					gbc_lblKonfigurationFile.anchor = GridBagConstraints.EAST;
					gbc_lblKonfigurationFile.insets = new Insets(0, 10, 5, 5);
					gbc_lblKonfigurationFile.gridx = 1;
					gbc_lblKonfigurationFile.gridy = 11;
					panel.add(lblKonfigurationFile, gbc_lblKonfigurationFile);
				}
				{
					txtConfigFile = new JTextField();
					GridBagConstraints gbc_txtConfigFile = new GridBagConstraints();
					gbc_txtConfigFile.insets = new Insets(0, 0, 5, 10);
					gbc_txtConfigFile.anchor = GridBagConstraints.WEST;
					gbc_txtConfigFile.gridx = 2;
					gbc_txtConfigFile.gridy = 11;
					panel.add(txtConfigFile, gbc_txtConfigFile);
					txtConfigFile.setColumns(10);
				}
				{
					cbExtendedMode = new JCheckBox("Extended mode");
					GridBagConstraints gbc_cbExtendedMode = new GridBagConstraints();
					gbc_cbExtendedMode.anchor = GridBagConstraints.WEST;
					gbc_cbExtendedMode.insets = new Insets(0, 0, 5, 10);
					gbc_cbExtendedMode.gridx = 2;
					gbc_cbExtendedMode.gridy = 12;
					panel.add(cbExtendedMode, gbc_cbExtendedMode);
				}
				{
					cbUseSystemName = new JCheckBox("Use system name as description");
					GridBagConstraints gbc_cbUseSystemName = new GridBagConstraints();
					gbc_cbUseSystemName.insets = new Insets(0, 0, 5, 10);
					gbc_cbUseSystemName.anchor = GridBagConstraints.WEST;
					gbc_cbUseSystemName.gridx = 2;
					gbc_cbUseSystemName.gridy = 13;
					panel.add(cbUseSystemName, gbc_cbUseSystemName);
				}
				{
					cbOpenNewFrame = new JCheckBox("Open in new frame");
					GridBagConstraints gbc_cbOpenNewFrame = new GridBagConstraints();
					gbc_cbOpenNewFrame.insets = new Insets(0, 0, 5, 10);
					gbc_cbOpenNewFrame.anchor = GridBagConstraints.WEST;
					gbc_cbOpenNewFrame.gridx = 2;
					gbc_cbOpenNewFrame.gridy = 14;
					panel.add(cbOpenNewFrame, gbc_cbOpenNewFrame);
				}
				{
					cbMonitorSessionStart = new JCheckBox("Monitor session start");
					GridBagConstraints gbc_cbMonitorSessionStart = new GridBagConstraints();
					gbc_cbMonitorSessionStart.insets = new Insets(0, 0, 5, 10);
					gbc_cbMonitorSessionStart.anchor = GridBagConstraints.WEST;
					gbc_cbMonitorSessionStart.gridx = 2;
					gbc_cbMonitorSessionStart.gridy = 15;
					panel.add(cbMonitorSessionStart, gbc_cbMonitorSessionStart);
				}
				{
					cbStartNewVm = new JCheckBox("Start in a new JVM");
					GridBagConstraints gbc_cbStartNewVm = new GridBagConstraints();
					gbc_cbStartNewVm.insets = new Insets(0, 0, 5, 10);
					gbc_cbStartNewVm.anchor = GridBagConstraints.WEST;
					gbc_cbStartNewVm.gridx = 2;
					gbc_cbStartNewVm.gridy = 16;
					panel.add(cbStartNewVm, gbc_cbStartNewVm);
				}
				{
					JSeparator separator = new JSeparator();
					GridBagConstraints gbc_separator = new GridBagConstraints();
					gbc_separator.gridwidth = 2;
					gbc_separator.fill = GridBagConstraints.HORIZONTAL;
					gbc_separator.insets = new Insets(5, 5, 5, 5);
					gbc_separator.gridx = 1;
					gbc_separator.gridy = 17;
					panel.add(separator, gbc_separator);
				}
				{
					JLabel lblProxy = new JLabel("Proxy");
					GridBagConstraints gbc_lblProxy = new GridBagConstraints();
					gbc_lblProxy.anchor = GridBagConstraints.WEST;
					gbc_lblProxy.insets = new Insets(0, 10, 5, 5);
					gbc_lblProxy.gridx = 1;
					gbc_lblProxy.gridy = 18;
					panel.add(lblProxy, gbc_lblProxy);
				}
				{
					cbUseProxy = new JCheckBox("Benutzen");
					GridBagConstraints gbc_cbUseProxy = new GridBagConstraints();
					gbc_cbUseProxy.insets = new Insets(0, 0, 5, 10);
					gbc_cbUseProxy.anchor = GridBagConstraints.WEST;
					gbc_cbUseProxy.gridx = 2;
					gbc_cbUseProxy.gridy = 18;
					panel.add(cbUseProxy, gbc_cbUseProxy);
				}
				{
					JLabel lblProxyHost = new JLabel("Proxy host");
					GridBagConstraints gbc_lblProxyHost = new GridBagConstraints();
					gbc_lblProxyHost.anchor = GridBagConstraints.WEST;
					gbc_lblProxyHost.insets = new Insets(0, 10, 5, 5);
					gbc_lblProxyHost.gridx = 1;
					gbc_lblProxyHost.gridy = 19;
					panel.add(lblProxyHost, gbc_lblProxyHost);
				}
				{
					txtProxyHost = new JTextField();
					GridBagConstraints gbc_txtProxyHost = new GridBagConstraints();
					gbc_txtProxyHost.fill = GridBagConstraints.HORIZONTAL;
					gbc_txtProxyHost.anchor = GridBagConstraints.WEST;
					gbc_txtProxyHost.insets = new Insets(0, 0, 5, 10);
					gbc_txtProxyHost.gridx = 2;
					gbc_txtProxyHost.gridy = 19;
					panel.add(txtProxyHost, gbc_txtProxyHost);
					txtProxyHost.setColumns(10);
				}
				{
					JLabel lblProxyPort = new JLabel("Proxy port");
					GridBagConstraints gbc_lblProxyPort = new GridBagConstraints();
					gbc_lblProxyPort.anchor = GridBagConstraints.WEST;
					gbc_lblProxyPort.insets = new Insets(0, 10, 10, 5);
					gbc_lblProxyPort.gridx = 1;
					gbc_lblProxyPort.gridy = 20;
					panel.add(lblProxyPort, gbc_lblProxyPort);
				}
				{
					spinnerProxyPort = new JSpinner();
					spinnerProxyPort.setModel(new SpinnerNumberModel(23, 1, 65535, 1));
					GridBagConstraints gbc_spinnerProxyPort = new GridBagConstraints();
					gbc_spinnerProxyPort.insets = new Insets(0, 0, 10, 10);
					gbc_spinnerProxyPort.anchor = GridBagConstraints.WEST;
					gbc_spinnerProxyPort.gridx = 2;
					gbc_spinnerProxyPort.gridy = 20;
					panel.add(spinnerProxyPort, gbc_spinnerProxyPort);
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		initDefaultListener();
	}

	/**
	 * Set the model for this dialog ...
	 *
	 * @param session
	 */
	public void setSession(EmulSession session) {
		this.session = session;
	}

	/**
	 * @return The model for this dialog ...
	 */
	public EmulSession getSession() {
		return session;
	}

	/**
	 * @return true -&gt; OK and false -&gt; CANCEL
	 */
	public boolean showModal() {
		this.setModal(true);
		this.setVisible(true);
		return dialogResult;
	}

	@Override
	public void setVisible(boolean b) {
		if (session == null) {
			session = new EmulSession();
		} else {
			// use a clone to avoid conflicts ...
			session = (EmulSession) session.clone();
		}
		initValuesAndListeners();
		super.setVisible(b);
	}

	private void initDefaultListener() {
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand(ACTION_CANCEL);
		okButton.addActionListener(this);
		okButton.setActionCommand(ACTION_OK);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		dialogResult = "OK".equals(e.getActionCommand());
		setVisible(false);
	}

	private void initValuesAndListeners() {
		{
			txtHost.setText(session.getHost());
			txtHost.getDocument().addDocumentListener(new DocumentListenerAdapter() {
				@Override
				public void changed(DocumentEvent e) {
					session.setHost(txtHost.getText().trim());
				}
			});
		}
		{
			txtSystemName.setText(session.getName());
			txtSystemName.getDocument().addDocumentListener(new DocumentListenerAdapter() {
				@Override
				public void changed(DocumentEvent e) {
					session.setName(txtSystemName.getText().trim());
				}
			});
		}
		{
			((SpinnerNumberModel)spinnerPort.getModel()).setValue(session.getPort());
			spinnerPort.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					session.setPort(Integer.parseInt(spinnerPort.getModel().getValue().toString()));
				}
			});
		}
		{
			txtDeviceName.setText(session.getDevName());
			txtDeviceName.getDocument().addDocumentListener(new DocumentListenerAdapter() {
				@Override
				public void changed(DocumentEvent e) {
					session.setDevName(txtDeviceName.getText());
				}
			});
		}
		{
			cbUsePcDeviceName.setSelected(session.isUsePcAsDevName());
			cbUsePcDeviceName.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					session.setUsePcAsDevName(cbUsePcDeviceName.isSelected());
				}
			});
		}
		{
			final SslType[] ssltypes = new SslType[] {SslType.NONE, SslType.SSLv2, SslType.SSLv3, SslType.TLS };
			for (SslType st : ssltypes) {
				comboSslType.addItem(st);
			}
			comboSslType.setSelectedItem(session.getSslType());
			comboSslType.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					session.setSslType((SslType)comboSslType.getSelectedItem());
				}
			});
		}
		{
			cbUseToolbox.setSelected(session.isUseAs400Toolbox());
			cbUseToolbox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					session.setUseAs400Toolbox(cbUseToolbox.isSelected());
				}
			});
		}
		{
			cbUse132.setSelected(session.isUseWidth132());
			cbUse132.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					session.setUseWidth132(cbUse132.isSelected());
				}
			});
		}
		{
			cbPing.setSelected(session.isSendKeepAlive());
			cbPing.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					session.setSendKeepAlive(cbPing.isSelected());
				}
			});
		}
		{
			txtConfigFile.setText(session.getConfigFile());
			txtConfigFile.getDocument().addDocumentListener(new DocumentListenerAdapter() {
				@Override
				public void changed(DocumentEvent e) {
					session.setConfigFile(txtConfigFile.getText());
				}
			});
		}
		{
			btChooseFile.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final JFileChooser fc;
					if (session.getConfigFile() != null) {
						fc = new JFileChooser(session.getConfigFile());
					} else {
						fc = new JFileChooser();
					}
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnVal = fc.showOpenDialog(SessionPropsDialog.this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						session.setConfigFile(fc.getSelectedFile().getPath());
						txtConfigFile.setText(fc.getSelectedFile().getPath());
					}
				}
			});
		}
		{
			cbExtendedMode.setSelected(session.isEnhancedMode());
			cbExtendedMode.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					session.setEnhancedMode(cbExtendedMode.isSelected());
				}
			});
		}
		{
			cbUseSystemName.setSelected(session.isUseSysNameAsDescription());
			cbUseSystemName.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					session.setUseSysNameAsDescription(cbUseSystemName.isSelected());
				}
			});
		}
		{
			cbOpenNewFrame.setSelected(session.isOpenNewFrame());
			cbOpenNewFrame.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					session.setOpenNewFrame(cbOpenNewFrame.isSelected());
				}
			});
		}
		{
			cbMonitorSessionStart.setSelected(session.isMonitorSessionStart());
			cbMonitorSessionStart.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					session.setMonitorSessionStart(cbMonitorSessionStart.isSelected());
				}
			});
		}
		{
			cbStartNewVm.setSelected(session.isOpenNewJvm());
			cbStartNewVm.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					session.setOpenNewJvm(cbStartNewVm.isSelected());
				}
			});
		}
		{
			cbUseProxy.setSelected(session.isUseProxy());
			cbUseProxy.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					session.setUseProxy(cbUseProxy.isSelected());
					txtProxyHost.setEnabled(session.isUseProxy());
					spinnerProxyPort.setEnabled(session.isUseProxy());
				}
			});
		}
		{
			txtProxyHost.setText(session.getProxyHost());
			txtProxyHost.getDocument().addDocumentListener(new DocumentListenerAdapter() {
				@Override
				public void changed(DocumentEvent e) {
					session.setProxyHost(txtProxyHost.getText());
				}
			});
			txtProxyHost.setEnabled(session.isUseProxy());
		}
		{
			((SpinnerNumberModel)spinnerProxyPort.getModel()).setValue(session.getProxyPort());
			spinnerProxyPort.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					session.setProxyPort(Integer.parseInt(spinnerProxyPort.getModel().getValue().toString()));
				}
			});
			spinnerProxyPort.setEnabled(session.isUseProxy());
		}
		{
			String selcp = null;
			for (String s : CharMappings.getAvailableCodePages()) {
				comboCodepage.addItem(s);
				if (s.equals(session.getCodepage())) {
					selcp = s;
				}
			}
			if (selcp != null) {
				comboCodepage.setSelectedItem(selcp);
			}
			comboCodepage.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					session.setCodepage((String)comboCodepage.getSelectedItem());
				}
			});
		}
	}

	/*
	 * ========================================================================
	 */

	/**
	 * Simple adapter class ..
	 */
	private abstract static class DocumentListenerAdapter implements DocumentListener {

		/* (non-Javadoc)
		 * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
		 */
		@Override
		public void insertUpdate(DocumentEvent e) {
			changed(e);
		}

		/* (non-Javadoc)
		 * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
		 */
		@Override
		public void removeUpdate(DocumentEvent e) {
			changed(e);
		}

		/* (non-Javadoc)
		 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
		 */
		@Override
		public void changedUpdate(DocumentEvent e) {
			changed(e);
		}

		abstract void changed(DocumentEvent e);

	}
}
