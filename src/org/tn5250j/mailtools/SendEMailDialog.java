package org.tn5250j.mailtools;

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

import javax.swing.*;
import org.tn5250j.tools.*;
import java.io.*;
import java.awt.*;
import org.tn5250j.Screen5250;
import javax.mail.*;

public class SendEMailDialog {

   public SendEMailDialog(Frame parent, Screen5250 screen ) {

      if (!isEMailAvailable()) {

         JOptionPane.showMessageDialog(parent,
               "The Java E-Mail API can not be found or is not installed\n" +
                  "Please read e-mail.txt file for installation instructions." ,"Error",
               JOptionPane.ERROR_MESSAGE,null);

      }
      else {
         JPanel semp = new JPanel();
         semp.setLayout(new AlignLayout(2,5,5));
         JLabel tol = new JLabel(LangTool.getString("em.to"));
         JTextField tot = new JTextField(30);
         JLabel subl = new JLabel(LangTool.getString("em.subject"));
         JTextField subt = new JTextField(30);
         JLabel fnl = new JLabel(LangTool.getString("em.fileName"));
         JTextField fnt = new JTextField("tn5250j.txt",30);
         semp.add(tol);
         semp.add(tot);
         semp.add(subl);
         semp.add(subt);
         semp.add(fnl);
         semp.add(fnt);

         Object[]      message = new Object[1];
         message[0] = semp;
         String[] options = {LangTool.getString("em.optSendLabel"),
                              LangTool.getString("em.optCancelLabel"),
                              LangTool.getString("em.optConfigureLabel")};

         int result = 0;
         while (result == 0 || result == 2) {
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
                  sem.setTo(tot.getText());
                  sem.setSubject(subt.getText());
                  if (fnt.getText().length() > 0)
                     sem.setFileName(fnt.getText());

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


                  sem.setMessage(sb.toString());
                  try {
                     sem.send();
                  }
                  catch (IOException ioe) {
                     System.out.println(ioe.getMessage());
                  }
                  sem.release();
                  sem = null;
                  System.out.println("Message sent");

                  break;
               case 1: // Cancel
      //		      System.out.println("Cancel");
                  break;
               case 2: // Configure SMTP
                  SMTPConfig smtp = new SMTPConfig(parent,"",true);
                  smtp.setVisible(true);
                  smtp.dispose();
      //		      System.out.println("Cancel");
                  break;
               default:
                  break;
            }
         }
      }
   }

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