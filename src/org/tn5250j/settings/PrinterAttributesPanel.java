package org.tn5250j.settings;
/**
 * Title: PrinterAttributesPanel
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

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;

import org.tn5250j.tools.*;
import org.tn5250j.SessionConfig;

public class PrinterAttributesPanel extends AttributesPanel {

   JCheckBox defaultPrinter;

   public PrinterAttributesPanel(SessionConfig config ) {
      super(config,"Printer");
   }

   /**Component initialization*/
   public void initPanel() throws Exception  {

      setLayout(new BorderLayout());
      contentPane = new JPanel();
      contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));
      add(contentPane,BorderLayout.NORTH);

      // define ppPanel panel
      JPanel ppp = new JPanel();
      ppp.setBorder(BorderFactory.createTitledBorder(LangTool.getString("sa.print")));
      defaultPrinter = new JCheckBox(LangTool.getString("sa.defaultPrinter"));

      if (getStringProperty("defaultPrinter").equals("Yes"))
         defaultPrinter.setSelected(true);

      ppp.add(defaultPrinter);

      contentPane.add(ppp);

   }

   public void save() {

   }

   public void applyAttributes() {

      if (defaultPrinter.isSelected()) {
         changes.firePropertyChange(this,"defaultPrinter",
                           getStringProperty("defaultPrinter"),
                           "Yes");
         setProperty("defaultPrinter","Yes");
      }
      else {
         changes.firePropertyChange(this,"defaultPrinter",
                           getStringProperty("defaultPrinter"),
                           "No");
         setProperty("defaultPrinter","No");
      }

   }
}