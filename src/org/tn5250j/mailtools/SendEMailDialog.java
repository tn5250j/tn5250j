/**
 * Title: SendEMailDialog.java
 *
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

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.swing.*;

import org.tn5250j.Screen5250;
import org.tn5250j.Session;
import org.tn5250j.SessionConfig;
import org.tn5250j.gui.TN5250jFileChooser;
import org.tn5250j.tools.LangTool;
import org.tn5250j.tools.encoder.EncodeComponent;

/**
 * Send E-Mail dialog
 */
public class SendEMailDialog implements Runnable {

	JComboBox toAddress;
	JTextField subject;
	JTextArea bodyText;
	JTextField attachmentName;
	SessionConfig config;
	Session session;
	String fileName;
	JRadioButton text;
	JRadioButton graphic;
	GridBagConstraints gbc;
	JRadioButton normal;
	JRadioButton screenshot;
	JButton browse;
   boolean sendScreen;
   SendEMail sendEMail;

	/**
	 * Constructor to send the screen information
	 *
	 * @param parent
	 * @param session
    * @param sendScreen
	 */
	public SendEMailDialog(Frame parent, Session session) {
      this(parent,session,true);
	}

	/**
	 * Constructor to send the screen information
	 *
	 * @param parent
	 * @param session
	 */
	public SendEMailDialog(Frame parent, Session session, boolean sendScreen) {

		if (!isEMailAvailable()) {

			JOptionPane.showMessageDialog(
				parent,
				LangTool.getString("messages.noEmailAPI"),
				"Error",
				JOptionPane.ERROR_MESSAGE,
				null);

		}
		else {

			this.session = session;
			Screen5250 screen = session.getScreen();
         this.sendScreen = sendScreen;

			Object[] message = new Object[1];
			message[0] = setupMailPanel("tn5250j.txt");

			String[] options = new String[3];

			int result = 0;
			while (result == 0 || result == 2) {

				// setup the dialog options
				setOptions(options);

				result = JOptionPane.showOptionDialog(parent,
					// the parent that the dialog blocks
					message, // the dialog message array
					LangTool.getString("em.title"),
					// the title of the dialog window
					JOptionPane.DEFAULT_OPTION, // option type
					JOptionPane.PLAIN_MESSAGE, // message type
					null, // optional icon, use null to use the default icon
					options, // options string array, will be made into buttons
					options[0] // option that should be made into a default btn
				);

				switch (result) {
					case 0 : // Send it
						SendEMail sem = new SendEMail();
						sem.setConfigFile("SMTPProperties.cfg");
						sem.setTo((String) toAddress.getSelectedItem());
						sem.setSubject(subject.getText());
						if (bodyText.getText().length() > 0)
							sem.setMessage(bodyText.getText());

						if (attachmentName.getText().length() > 0)
							if (!normal.isSelected()) sem.setAttachmentName(attachmentName.getText());
							else sem.setAttachmentName(fileName);

						if (text.isSelected()) {

							StringBuffer sb = new StringBuffer();
							char[] s = screen.getScreenAsChars();
							int c = screen.getCols();
							int l = screen.getRows() * c;
							int col = 0;
							for (int x = 0; x < l; x++, col++) {
								sb.append(s[x]);
								if (col == c) {
									sb.append('\n');
									col = 0;
								}
							}

							sem.setAttachment(sb.toString());
						}
						else if (graphic.isSelected()){

							File dir = new File(System.getProperty("user.dir"));

							//  setup the temp file name
							String tempFile = "tn5250jTemp";

							try {
								// create the temporary file
								File f =
									File.createTempFile(tempFile, ".png", dir);

								System.out.println(f.getName());
								System.out.println(f.getCanonicalPath());

								// set it to delete on exit
								f.deleteOnExit();

								EncodeComponent.encode(
									EncodeComponent.PNG,
									session,
									f);
								sem.setFileName(f.getName());
							}
							catch (Exception ex) {
								System.out.println(ex.getMessage());
							}

						}
						else if (attachmentName.getText().length() > 0) {
							File f = new File(attachmentName.getText());
							sem.setFileName(f.toString());
						}

						// send the information
						sendIt(parent, sem);

						sem.release();
						sem = null;

						break;
					case 1 : // Cancel
						//		      System.out.println("Cancel");
						break;
					case 2 : // Configure SMTP
						configureSMTP(parent);
						//		      System.out.println("Cancel");
						break;
					default :
						break;
				}
			}
		}
	}

	/**
	 * Constructor to send a file
	 *
	 * @param parent
	 * @param session
	 */
	public SendEMailDialog(Frame parent, Session session, String fileName) {

		if (!isEMailAvailable()) {

			JOptionPane.showMessageDialog(
				parent,
				LangTool.getString("messages.noEmailAPI"),
				"Error",
				JOptionPane.ERROR_MESSAGE,
				null);
		}
		else {

			this.session = session;
			Screen5250 screen = session.getScreen();

			Object[] message = new Object[1];
			message[0] = setupMailPanel(fileName);
			String[] options = new String[3];

			int result = 0;
			while (result == 0 || result == 2) {

				// setup the dialog options
				setOptions(options);
				result = JOptionPane.showOptionDialog(parent,
				// the parent that the dialog blocks
				message, // the dialog message array
				LangTool.getString("em.titleFileTransfer"),
				// the title of the dialog window
				JOptionPane.DEFAULT_OPTION, // option type
				JOptionPane.PLAIN_MESSAGE, // message type
				null, // optional icon, use null to use the default icon
				options, // options string array, will be made into buttons//
				options[0] // option that should be made into a default button
			);

				switch (result) {
					case 0 : // Send it

						SendEMail sem = new SendEMail();
						sem.setConfigFile("SMTPProperties.cfg");
						sem.setTo((String) toAddress.getSelectedItem());
						sem.setSubject(subject.getText());
						if (bodyText.getText().length() > 0)
							sem.setMessage(bodyText.getText());

						if (attachmentName.getText().length() > 0)
							sem.setAttachmentName(attachmentName.getText());

						if (fileName != null && fileName.length() > 0)
							sem.setFileName(fileName);

						// send the information
						sendIt(parent, sem);

						sem.release();
						sem = null;

						break;
					case 1 : // Cancel
						//		      System.out.println("Cancel");
						break;
					case 2 : // Configure SMTP
						configureSMTP(parent);
						//		      System.out.println("Cancel");
						break;
					default :
						break;
				}
			}
		}
	}

	/**
	 * Send the e-mail on its way.
	 * @param sem
	 */
	private void sendIt(Frame parent, SendEMail sem) {

      setSendEMail(sem);
      new Thread(this).start();
//		if (parent == null)
//			parent = new JFrame();
//
//		try {
//			if (sem.send()) {
//
//				JOptionPane.showMessageDialog(
//					parent,
//					LangTool.getString("em.confirmationMessage")
//						+ " "
//						+ (String) toAddress.getSelectedItem(),
//					LangTool.getString("em.titleConfirmation"),
//					JOptionPane.INFORMATION_MESSAGE);
//
//				if (session != null) {
//					config.setProperty(
//						"emailTo",
//						getToTokens(
//							config.getStringProperty("emailTo"),
//							toAddress));
//					config.saveSessionProps();
//					setToCombo(config.getStringProperty("emailTo"), toAddress);
//				}
//			}
//		} catch (IOException ioe) {
//			System.out.println(ioe.getMessage());
//		} catch (Exception ex) {
//			System.out.println(ex.getMessage());
//		}
	}

   public void setSendEMail(SendEMail sem) {
      sendEMail = sem;
   }
   public void run() {

//		if (parent == null)
//			parent = new JFrame();

		try {
			if (sendEMail.send()) {

//				JOptionPane.showMessageDialog(
//					parent,
//					LangTool.getString("em.confirmationMessage")
//						+ " "
//						+ (String) toAddress.getSelectedItem(),
//					LangTool.getString("em.titleConfirmation"),
//					JOptionPane.INFORMATION_MESSAGE);
//
//				if (session != null) {
//					config.setProperty(
//						"emailTo",
//						getToTokens(
//							config.getStringProperty("emailTo"),
//							toAddress));
//					config.saveSessionProps();
//					setToCombo(config.getStringProperty("emailTo"), toAddress);
//				}
			}
//		} catch (IOException ioe) {
//			System.out.println(ioe.getMessage());
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
   }

	/**
	 * Configure the SMTP server information
	 *
	 * @param parent
	 */
	private void configureSMTP(Frame parent) {

		if (parent == null)
			parent = new JFrame();

		SMTPConfig smtp = new SMTPConfig(parent, "", true);
		smtp.setVisible(true);
		smtp.dispose();

	}

	/**
	 * Create the main e-mail panel for display
	 *
	 * @param fileName
	 * @return
	 */
	private JPanel setupMailPanel(String fileName) {

		JPanel semp = new JPanel();
		semp.setLayout(new GridBagLayout());

		text = new JRadioButton(LangTool.getString("em.text"));
		graphic = new JRadioButton(LangTool.getString("em.graphic"));
		normal = new JRadioButton(LangTool.getString("em.normalmail"),true);
		screenshot = new JRadioButton(LangTool.getString("em.screenshot"));

		// Group the radio buttons.
		ButtonGroup tGroup = new ButtonGroup();
		tGroup.add(text);
		tGroup.add(graphic);
		ButtonGroup mGroup = new ButtonGroup();
		mGroup.add(normal);
		mGroup.add(screenshot);

		text.setSelected(false);
		text.setEnabled(false);
		graphic.setEnabled(false);

		JLabel screenDump = new JLabel(LangTool.getString("em.screendump"));
		JLabel tol = new JLabel(LangTool.getString("em.to"));
		JLabel subl = new JLabel(LangTool.getString("em.subject"));
		JLabel bodyl = new JLabel(LangTool.getString("em.body"));
		JLabel fnl = new JLabel(LangTool.getString("em.fileName"));
		JLabel tom = new JLabel(LangTool.getString("em.typeofmail"));

		browse = new JButton(LangTool.getString("em.choosefile"));
		browse.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browse_actionPerformed(e);
			}
		});

		toAddress = new JComboBox();
		toAddress.setPreferredSize(new Dimension(175, 25));
		toAddress.setEditable(true);

		subject = new JTextField(30);
		bodyText = new JTextArea(6, 30);
		JScrollPane bodyScrollPane = new JScrollPane(bodyText);
		bodyScrollPane.setHorizontalScrollBarPolicy(
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		bodyScrollPane.setVerticalScrollBarPolicy(
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		attachmentName = new JTextField(fileName, 30);
		attachmentName.setText("");

		text.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(java.awt.event.ItemEvent e) {
				setAttachmentName();
			}
		});
		normal.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(java.awt.event.ItemEvent e) {
				setTypeOfMail();
			}
		});

      if (sendScreen) {
         screenshot.setSelected(true);
      }
      else {
   		normal.setSelected(true);
      }

		config = null;

		if (session != null) {
			config = session.getConfiguration();

			if (config.isPropertyExists("emailTo")) {
				setToCombo(config.getStringProperty("emailTo"), toAddress);
			}
		}

		semp.setBorder(BorderFactory.createEtchedBorder());
		gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 5, 5);
		semp.add(tom, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 1; gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 15, 5, 5);
		semp.add(normal, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 2; gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 45, 5, 10);
		semp.add(screenshot, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 10, 5, 5);
		semp.add(screenDump, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 1; gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 15, 5, 5);
		semp.add(text, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 2; gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 45, 5, 10);
		semp.add(graphic, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 5, 5);
		semp.add(tol, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 1; gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 10);
		semp.add(toAddress, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 5, 5);
		semp.add(subl, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 1; gbc.gridy = 3;
		gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 10);
		semp.add(subject, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 4;
		gbc.gridheight = 3;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 5, 5);
		semp.add(bodyl, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 1; gbc.gridy = 4;
		gbc.gridwidth = 2; gbc.gridheight = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 10);
		semp.add(bodyScrollPane, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 7;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 5, 5);
		semp.add(fnl, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 1; gbc.gridy = 7;
		gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 10);
		semp.add(attachmentName, gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 1; gbc.gridy = 8;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(5, 5, 10, 10);
		semp.add(browse, gbc);

		return semp;

	}

	private void browse_actionPerformed(ActionEvent e) {
		String workingDir = System.getProperty("user.dir");
		TN5250jFileChooser pcFileChooser = new TN5250jFileChooser(workingDir);

		int ret = pcFileChooser.showOpenDialog(new JFrame());

		// check to see if something was actually chosen
		if (ret == TN5250jFileChooser.APPROVE_OPTION) {
		   File file = pcFileChooser.getSelectedFile();
		   fileName = file.getName();
		   attachmentName.setText(file.toString());
		}
	}

	private void setAttachmentName() {

		if (text.isSelected()) {
			attachmentName.setText("tn5250j.txt");

		}
		else if (normal.isSelected()) {
			attachmentName.setText("tn5250j.png");
		}
		else {
			attachmentName.setText("tn5250j.png");
		}
	}

	private void setTypeOfMail() {

		if (normal.isSelected()) {
			text.setEnabled(false);
			graphic.setEnabled(false);
			attachmentName.setText("");
			browse.setEnabled(true);
		}
		else {
			text.setEnabled(true);
			graphic.setEnabled(true);
			text.setSelected(true);
			setAttachmentName();
			browse.setEnabled(false);
		}
	}

	private void setOptions(String[] options) {

		options[0] = LangTool.getString("em.optSendLabel");
		options[1] = LangTool.getString("em.optCancelLabel");

		File smtp = new File("SMTPProperties.cfg");

		if (smtp.exists())
			options[2] = LangTool.getString("em.optEditLabel");
		else
			options[2] = LangTool.getString("em.optConfigureLabel");

	}

	/**
	 * Set the combo box items to the string token from to.
	 * The separator is a '|' character.
	 *
	 * @param to
	 * @param boxen
	 */
	private void setToCombo(String to, JComboBox boxen) {

		StringTokenizer tokenizer = new StringTokenizer(to, "|");

		boxen.removeAllItems();

		while (tokenizer.hasMoreTokens()) {
			boxen.addItem(tokenizer.nextToken());
		}
	}

	/**
	 * Creates string of tokens from the combobox items.
	 * The separator is a '|' character.  It does not save duplicate items.
	 *
	 * @param to
	 * @param boxen
	 * @return
	 */
	private String getToTokens(String to, JComboBox boxen) {

		StringBuffer sb = new StringBuffer();
		String selected = (String) boxen.getSelectedItem();

		sb.append(selected + '|');

		int c = boxen.getItemCount();

		for (int x = 0; x < c; x++) {
			if (!selected.equals((String) boxen.getItemAt(x)))
				sb.append((String) boxen.getItemAt(x) + '|');
		}
		return sb.toString();
	}

	/**
	 * Checks to make sure that the e-mail api's are available
	 *
	 * @return whether or not the e-mail api's are available or not.
	 */
	private boolean isEMailAvailable() {

		try {
			Class.forName("javax.mail.Message");
			return true;
		} catch (Exception ex) {
			System.out.println(" not there " + ex.getMessage());
			return false;
		}

	}

	/**
	 * Create a option pane to show status of the transfer
	 */
	private class ProgressOptionPane extends JOptionPane {

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
//			monitor.setValue(null);

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
						dialog.setVisible(false);
						dialog.dispose();
					}
				}
			});
			return dialog;
		}
	}

}