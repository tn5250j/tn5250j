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

import javax.swing.*;
import java.io.*;
import java.awt.*;
import javax.mail.*;
import java.util.*;

import org.tn5250j.Screen5250;
import org.tn5250j.Session;
import org.tn5250j.SessionConfig;
import org.tn5250j.tools.*;

/**
 * Send E-Mail dialog
 */
public class SendEMailDialog {

   JComboBox toAddress;
   JTextField subject;
   JTextArea bodyText;
   JTextField attachmentName;
   SessionConfig config;
   Session session;
   String fileName;

   /**
    * Constructor to send the screen information
    *
    * @param parent
    * @param session
    */
   public SendEMailDialog(Frame parent, Session session ) {

      if (!isEMailAvailable()) {

         JOptionPane.showMessageDialog(parent,
                                       LangTool.getString("messages.noEmailAPI"),
                                       "Error",
                                       JOptionPane.ERROR_MESSAGE,null);

      }
      else {

         this.session = session;
         Screen5250 screen = session.getScreen();

         Object[]      message = new Object[1];
         message[0] = setupMailPanel("tn5250j.txt");

         String[] options = new String[3];

         int result = 0;
         while (result == 0 || result == 2) {

            // setup the dialog options
            setOptions(options);

            result = JOptionPane.showOptionDialog(
                parent,                            // the parent that the dialog blocks
                message,                           // the dialog message array
                LangTool.getString("em.title"),    // the title of the dialog window
                JOptionPane.DEFAULT_OPTION,        // option type
                JOptionPane.QUESTION_MESSAGE,      // message type
                null,                              // optional icon, use null to use the default icon
                options,                           // options string array, will be made into buttons//
                options[0]                         // option that should be made into a default button
            );

            switch(result) {
               case 0: // Send it
                  SendEMail sem = new SendEMail();
                  sem.setConfigFile("SMTPProperties.cfg");
                  sem.setTo((String)toAddress.getSelectedItem());
                  sem.setSubject(subject.getText());
                  if (bodyText.getText().length() >0)
                     sem.setMessage(bodyText.getText());

                  if (attachmentName.getText().length() > 0)
                     sem.setAttachmentName(attachmentName.getText());

                  StringBuffer sb = new StringBuffer();
                  char[] s = screen.getScreenAsChars();
                  int c = screen.getCols();
                  int l = screen.getRows() * c;
                  int col = 0;
                  for (int x = 0; x < l; x++,col++) {
                     sb.append(s[x]);
                     if (col == c) {
                        sb.append('\n');
                        col = 0;
                     }
                  }

                  sem.setAttachment(sb.toString());

                  // send the information
                  sendIt(parent,sem);

                  sem.release();
                  sem = null;


                  break;
               case 1: // Cancel
      //		      System.out.println("Cancel");
                  break;
               case 2: // Configure SMTP
                  configureSMTP(parent);
      //		      System.out.println("Cancel");
                  break;
               default:
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
   public SendEMailDialog(Frame parent, Session session, String fileName ) {

      if (!isEMailAvailable()) {

         JOptionPane.showMessageDialog(parent,
               "The Java E-Mail API can not be found or is not installed\n" +
                  "Please read e-mail.txt file for installation instructions." ,"Error",
               JOptionPane.ERROR_MESSAGE,null);

      }
      else {

         this.session = session;
         Screen5250 screen = session.getScreen();

         Object[]      message = new Object[1];
         message[0] = setupMailPanel(fileName);
         String[] options = new String[3];


         int result = 0;
         while (result == 0 || result == 2) {

            // setup the dialog options
            setOptions(options);

            result = JOptionPane.showOptionDialog(
                parent,                            // the parent that the dialog blocks
                message,                           // the dialog message array
                LangTool.getString("em.titleFileTransfer"),    // the title of the dialog window
                JOptionPane.DEFAULT_OPTION,        // option type
                JOptionPane.QUESTION_MESSAGE,      // message type
                null,                              // optional icon, use null to use the default icon
                options,                           // options string array, will be made into buttons//
                options[0]                         // option that should be made into a default button
            );

            switch(result) {
               case 0: // Send it

                  SendEMail sem = new SendEMail();
                  sem.setConfigFile("SMTPProperties.cfg");
                  sem.setTo((String)toAddress.getSelectedItem());
                  sem.setSubject(subject.getText());
                  if (bodyText.getText().length() >0)
                     sem.setMessage(bodyText.getText());

                  if (attachmentName.getText().length() > 0)
                     sem.setAttachmentName(attachmentName.getText());

                  if (fileName != null && fileName.length() > 0)
                     sem.setFileName(fileName);

                  // send the information
                  sendIt(parent,sem);

                  sem.release();
                  sem = null;

                  break;
               case 1: // Cancel
      //		      System.out.println("Cancel");
                  break;
               case 2: // Configure SMTP
                  configureSMTP(parent);
      //		      System.out.println("Cancel");
                  break;
               default:
                  break;
            }
         }
      }
   }

   /**
    * Send the e-mail on its way.
    * @param sem
    */
   private void sendIt (Frame parent, SendEMail sem) {

      if (parent == null)
         parent = new JFrame();

      try {
         if (sem.send()) {

            JOptionPane.showMessageDialog(parent,
                                             LangTool.getString("em.confirmationMessage") +
                                             " " + (String)toAddress.getSelectedItem(),
                                             LangTool.getString("em.titleConfirmation"),
                                             JOptionPane.INFORMATION_MESSAGE);

            if (session != null) {
               config.setProperty("emailTo",
                     getToTokens(config.getStringProperty("emailTo"),
                                    toAddress));
               config.saveSessionProps();
               setToCombo(config.getStringProperty("emailTo"),toAddress);
            }
         }
      }
      catch (IOException ioe) {
         System.out.println(ioe.getMessage());
      }
      catch (Exception ex) {
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

      SMTPConfig smtp = new SMTPConfig(parent,"",true);
      smtp.setVisible(true);
      smtp.dispose();

   }

   /**
    * Show the error list from the e-mail API if there are errors
    *
    * @param parent
    * @param sfe
    */
   private void showFailedException(Frame parent, SendFailedException sfe) {

      String error = sfe.getMessage() + "\n";

      Address[] ia = sfe.getInvalidAddresses();

      for (int x = 0; x < ia.length; x++) {
         error += "Invalid Address: " + ia[x].toString() + "\n";
      }

      JTextArea ea = new JTextArea(error,6,50);
      JScrollPane errorScrollPane = new JScrollPane(ea);
      errorScrollPane.setHorizontalScrollBarPolicy(
      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      errorScrollPane.setVerticalScrollBarPolicy(
      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      JOptionPane.showMessageDialog(parent,
                                       errorScrollPane,
                                       LangTool.getString("em.titleConfirmation"),
                                       JOptionPane.ERROR_MESSAGE);


   }

   /**
    * Create the main e-mail panel for display
    *
    * @param fileName
    * @return
    */
   private JPanel setupMailPanel (String fileName) {

      JPanel semp = new JPanel();
      semp.setLayout(new AlignLayout(2,5,5));

      JLabel tol = new JLabel(LangTool.getString("em.to"));
      JLabel subl = new JLabel(LangTool.getString("em.subject"));
      JLabel bodyl = new JLabel(LangTool.getString("em.body"));
      JLabel fnl = new JLabel(LangTool.getString("em.fileName"));

      toAddress = new JComboBox();
      toAddress.setEditable(true);

      subject = new JTextField(30);
      bodyText = new JTextArea(6,30);
      JScrollPane bodyScrollPane = new JScrollPane(bodyText);
      bodyScrollPane.setHorizontalScrollBarPolicy(
      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      bodyScrollPane.setVerticalScrollBarPolicy(
      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      attachmentName = new JTextField(fileName,30);

      config = null;

      if (session != null) {
         config = session.getConfiguration();

         if (config.isPropertyExists("emailTo")) {
            setToCombo(config.getStringProperty("emailTo"),toAddress);
         }
      }

      semp.add(tol);
      semp.add(toAddress);
      semp.add(subl);
      semp.add(subject);
      semp.add(bodyl);
      semp.add(bodyScrollPane);
      semp.add(fnl);
      semp.add(attachmentName);

      return semp;

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

      while(tokenizer.hasMoreTokens())  {
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
      String selected = (String)boxen.getSelectedItem();

      sb.append(selected + '|');

      int c = boxen.getItemCount();

      for (int x = 0; x < c; x++) {
         if (!selected.equals((String)boxen.getItemAt(x)))
            sb.append((String)boxen.getItemAt(x) + '|');
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
      }
      catch (Exception ex) {
         System.out.println(" not there " + ex.getMessage());
         return false;
      }

   }

}