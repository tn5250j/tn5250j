/**
 * Title: UserDataPanel.java
 * Copyright:   Copyright (c) 2002
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
package org.tn5250j.spoolfile;

import java.awt.event.*;
import javax.swing.*;

import org.tn5250j.tools.AlignLayout;
import org.tn5250j.event.ToggleDocumentListener;
import org.tn5250j.gui.ToggleDocument;

public class UserDataTabPanel extends JPanel implements QueueFilterInterface,
                                                         ToggleDocumentListener {

   private static final long serialVersionUID = 1L;
JRadioButton all;
   JRadioButton select;
   JTextField userData;

   public UserDataTabPanel() {
      try {
         jbInit();
      }
      catch(Exception ex) {
         ex.printStackTrace();
      }
   }

   void jbInit() throws Exception {

      setLayout(new AlignLayout(2,5,5));

      all = new JRadioButton("All");

      all.setSelected(true);

      select = new JRadioButton("User Data");
      select.setSelected(false);
      select.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            select_itemStateChanged(e);
         }
      });

      userData = new JTextField(15);
//      userData.setEnabled(false);
      ToggleDocument td = new ToggleDocument();
      td.addToggleDocumentListener(this);
      userData.setDocument(td);

      ButtonGroup bg = new ButtonGroup();
      bg.add(all);
      bg.add(select);

      add(all);
      add(new JLabel(""));
      add(select);
      add(userData);

      setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

   }

   /**
    * Reset to default value(s)
    */
   public void reset() {

//      userData.setEnabled(false);
      userData.setText("");
      all.setSelected(true);

   }

   void select_itemStateChanged(ItemEvent e) {
//      if (select.isSelected())
//         userData.setEnabled(true);
//      else
//         userData.setEnabled(false);
   }

   public void toggleNotEmpty() {

      select.setSelected(true);

   }

   public void toggleEmpty() {

   }

   public String getUserData() {
      if (all.isSelected())
         return "";
      else
         return userData.getText().trim();
   }

   public void setUserData(String filter) {

      userData.setText(filter);
   }
}