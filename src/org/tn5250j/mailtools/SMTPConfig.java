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

import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.util.Properties;
import java.awt.event.*;
import java.util.Enumeration;

import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.tools.AlignLayout;
import org.tn5250j.tools.LangTool;

public class SMTPConfig extends JDialog {

   JPanel mainPanel = new JPanel();
   BorderLayout borderLayout1 = new BorderLayout();
   AlignLayout alignLayout = new AlignLayout(2,5,5);
   JPanel configPanel = new JPanel();
   JPanel optionsPanel = new JPanel();
   JLabel labelHost = new JLabel();
   JTextField fieldHost = new JTextField();
   JLabel labelPort = new JLabel();
   JTextField fieldPort = new JTextField();
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
      }
      catch(Exception ex) {
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
      fieldPort.setColumns(5);
      labelFrom.setText(LangTool.getString("em.labelFrom"));
      fieldFrom.setColumns(20);
      optDone.setText(LangTool.getString("em.optDone"));
      optDone.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(ActionEvent e) {
            optDone_actionPerformed(e);
         }
      });

      optCancel.setText(LangTool.getString("em.optCancelLabel"));
      optCancel.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(ActionEvent e) {
            optCancel_actionPerformed(e);
         }
      });

      labelFileName.setText(LangTool.getString("em.labelFileName"));
      fieldFileName.setText("tn5250j.txt");
      fieldFileName.setColumns(20);
      optionsPanel.add(optDone, null);
      optionsPanel.add(optCancel, null);
      getContentPane().add(mainPanel);

      mainPanel.add(configPanel, BorderLayout.CENTER);
      configPanel.add(labelHost, null);
      mainPanel.add(optionsPanel,  BorderLayout.SOUTH);
      configPanel.add(fieldHost, null);
      configPanel.add(labelPort, null);
      configPanel.add(fieldPort, null);
      configPanel.add(labelFrom, null);
      configPanel.add(fieldFrom, null);
      configPanel.add(labelFileName, null);
      configPanel.add(fieldFileName, null);
      configPanel.setLayout(alignLayout);

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

      setLocation((screenSize.width - frameSize.width) / 2,
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

      SMTPProperties = ConfigureFactory.getInstance().getProperties("smtp",
                           smtpFileName);

      if (SMTPProperties.size() > 0)
         return true;
      else
         return false;
    }

   void optDone_actionPerformed(ActionEvent e) {

      SMTPProperties.setProperty("mail.smtp.host",fieldHost.getText());
      SMTPProperties.setProperty("mail.smtp.port",fieldPort.getText());
      SMTPProperties.setProperty("mail.smtp.from",fieldFrom.getText());

      // file name
      SMTPProperties.setProperty("fileName",fieldFileName.getText());

      for (Enumeration x = SMTPProperties.propertyNames();x.hasMoreElements();)
         System.out.println(SMTPProperties.get(x.nextElement()));

      ConfigureFactory.getInstance().saveSettings("smtp", smtpFileName,
                                 "------ SMTP Defaults --------");
      this.setVisible(false);

   }

   void optCancel_actionPerformed(ActionEvent e) {

      this.setVisible(false);

   }

}