package org.tn5250j.spoolfile;
/**
 * Title: OutputQueueTabPanel.java
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.tn5250j.tools.*;

public class OutputQueueTabPanel extends JPanel implements ActionListener {

   JRadioButton all;
   JRadioButton select;
   JTextField queue;
   JTextField library;

   public OutputQueueTabPanel() {
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

      select = new JRadioButton("Select Output Queue");
      select.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            select_itemStateChanged(e);
         }
      });
      select.addActionListener(this);

      library = new JTextField(10);
      queue = new JTextField(10);
      queue.setEnabled(false);
      library.setEnabled(false);

      ButtonGroup bg = new ButtonGroup();
      bg.add(all);
      bg.add(select);

      add(all);
      add(new JLabel(""));
      add(select);
      add(queue);
      add(new JLabel("Output queue library"));
      add(library);

      setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

   }

   public final void actionPerformed(ActionEvent actionevent) {
      String s = actionevent.getActionCommand();
   }

   void select_itemStateChanged(ItemEvent e) {
      if (select.isSelected()) {
         queue.setEnabled(true);
         library.setEnabled(true);
      }
      else {
         queue.setEnabled(false);
         library.setEnabled(false);
      }
   }

   public String getQueue() {
      if (all.isSelected())
         return "%ALL%";
      else
         return queue.getText().trim();
   }

   public String getLibrary() {

      if (all.isSelected())
         return "%ALL%";
      else
         return library.getText().trim();

   }
}