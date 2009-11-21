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
package org.tn5250j.mailtools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.tools.LangTool;

public class SMTPConfig extends JDialog {

	private static final long serialVersionUID = 1L;
	
	JPanel mainPanel = new JPanel();
	BorderLayout borderLayout1 = new BorderLayout();
	JPanel configPanel = new JPanel(new GridBagLayout());
	GridBagConstraints gbc;
	JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
	JLabel labelHost = new JLabel();
	JTextField fieldHost = new JTextField();
	JLabel labelPort = new JLabel();
	JTextField fieldPort = new JTextField();
	JLabel labelDefault = new JLabel();
	JLabel labelName = new JLabel();
	JTextField fieldName = new JTextField();
	JLabel labelFrom = new JLabel();
	JTextField fieldFrom = new JTextField();
	JButton optDone = new JButton();
	JButton optCancel = new JButton();
	JLabel labelFileName = new JLabel();
	JTextField fieldFileName = new JTextField();
	Properties SMTPProperties;
	//   String fileName;

	private static final String smtpFileName = "SMTPProperties.cfg";

	public SMTPConfig(Frame frame, String title, boolean modal) {
		super(frame, title, modal);
		try {
			jbInit();
			pack();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public SMTPConfig() {
		this(null, "", false);
	}

	void jbInit() throws Exception {

		setTitle(LangTool.getString("em.configTitle"));
		mainPanel.setLayout(borderLayout1);
		labelHost.setText(LangTool.getString("em.labelHost"));
		fieldHost.setColumns(20);
		labelPort.setText(LangTool.getString("em.labelPort"));
		fieldPort.setColumns(3);
		labelDefault.setText(LangTool.getString("em.labelDefault"));
		labelName.setText(LangTool.getString("em.labelName"));
		fieldName.setColumns(20);
		labelFrom.setText(LangTool.getString("em.labelFrom"));
		fieldFrom.setColumns(20);
		optDone.setPreferredSize(new Dimension(100, 27));
		optDone.setText(LangTool.getString("em.optDone"));
		optDone.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				optDone_actionPerformed(e);
			}
		});

		optCancel.setPreferredSize(new Dimension(100, 27));
		optCancel.setText(LangTool.getString("em.optCancelLabel"));
		optCancel.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				optCancel_actionPerformed(e);
			}
		});

		labelFileName.setText(LangTool.getString("em.labelFileName"));
		fieldFileName.setText("tn5250j.txt");
		fieldFileName.setColumns(20);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		//gbc.gridwidth = 1;
		gbc.insets = new Insets(10, 10, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;
		configPanel.add(labelHost, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(10, 5, 5, 10);
		gbc.anchor = GridBagConstraints.WEST;
		configPanel.add(fieldHost, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new Insets(5, 10, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;		
		configPanel.add(labelPort, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;		
		configPanel.add(fieldPort, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.insets = new Insets(5, 15, 5, 10);
		gbc.anchor = GridBagConstraints.WEST;		
		configPanel.add(labelDefault, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.insets = new Insets(5, 10, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;		
		configPanel.add(labelName, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(5, 5, 5, 10);
		gbc.anchor = GridBagConstraints.WEST;		
		configPanel.add(fieldName, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.insets = new Insets(5, 10, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;		
		configPanel.add(labelFrom, gbc);
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(5, 5, 5, 10);
		gbc.anchor = GridBagConstraints.WEST;		
		configPanel.add(fieldFrom, gbc);
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(5, 10, 0, 5);
		gbc.anchor = GridBagConstraints.WEST;		
		configPanel.add(labelFileName, gbc);
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(5, 5, 0, 10);
		gbc.anchor = GridBagConstraints.WEST;		
		configPanel.add(fieldFileName, gbc);

		mainPanel.add(configPanel, BorderLayout.NORTH);
		optionsPanel.add(optDone);
		optionsPanel.add(optCancel);
		mainPanel.add(optionsPanel, BorderLayout.SOUTH);

		getContentPane().add(mainPanel);

		if (loadConfig(null)) {

			setProperties();

		}

		centerMe();

	}

	private void centerMe() {
		pack();

		//Center the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = getSize();
		if (frameSize.height > screenSize.height)
			frameSize.height = screenSize.height;
		if (frameSize.width > screenSize.width)
			frameSize.width = screenSize.width;

		setLocation(
			(screenSize.width - frameSize.width) / 2,
			(screenSize.height - frameSize.height) / 2);

	}

	private void setProperties() {

		//   mail.smtp.host=            Fill in the host name or ip address of your SMTP
		//                              mail server.
		//
		//   mail.smtp.port=            Fill in the port to use to connect
		//
		//   mail.smtp.from=            This is the e-mail address from.  For example I would
		//                              place kjpou@hotmail.com here as follows:
		//
		//                              mail.smtp.from=kjpou@hotmail.com

		fieldHost.setText(SMTPProperties.getProperty("mail.smtp.host"));
		fieldPort.setText(SMTPProperties.getProperty("mail.smtp.port"));
		fieldFrom.setText(SMTPProperties.getProperty("mail.smtp.from"));
		fieldName.setText(SMTPProperties.getProperty("mail.smtp.realname"));

		// file name
		fieldFileName.setText(SMTPProperties.getProperty("fileName"));

	}

	/**
	 * <p>Loads the given configuration file.
	 *
	 * @param name Configuration file name
	 * @return true if the configuration file was loaded
	 */
	private boolean loadConfig(String name) throws Exception {

		SMTPProperties =
			ConfigureFactory.getInstance().getProperties("smtp", smtpFileName);

		if (SMTPProperties.size() > 0)
			return true;
		else
			return false;
	}

	private void optDone_actionPerformed(ActionEvent e) {

		SMTPProperties.setProperty("mail.smtp.host", fieldHost.getText());
		SMTPProperties.setProperty("mail.smtp.port", fieldPort.getText());
		SMTPProperties.setProperty("mail.smtp.from", fieldFrom.getText());
		SMTPProperties.setProperty("mail.smtp.realname", fieldName.getText());

		// file name
		SMTPProperties.setProperty("fileName", fieldFileName.getText());

		for (Enumeration<?> x = SMTPProperties.propertyNames();
			x.hasMoreElements();
			)
			System.out.println(SMTPProperties.get(x.nextElement()));

		ConfigureFactory.getInstance().saveSettings(
			"smtp",
			smtpFileName,
			"------ SMTP Defaults --------");
		this.setVisible(false);

	}

	void optCancel_actionPerformed(ActionEvent e) {

		this.setVisible(false);

	}

}