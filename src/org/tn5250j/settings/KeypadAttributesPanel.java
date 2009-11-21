package org.tn5250j.settings;
/**
 * Title: KeypadAttributesPanel
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

import org.tn5250j.tools.*;
import org.tn5250j.SessionConfig;

public class KeypadAttributesPanel extends AttributesPanel {

   private static final long serialVersionUID = 1L;
JCheckBox kpCheck;

   public KeypadAttributesPanel(SessionConfig config ) {
      super(config,"KP");
   }

   /**Component initialization*/
   public void initPanel() throws Exception  {

      setLayout(new BorderLayout());
      contentPane = new JPanel();
      contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));
      add(contentPane,BorderLayout.NORTH);

      // define Key Pad panel
      JPanel kpp = new JPanel();
      kpp.setBorder(BorderFactory.createTitledBorder(LangTool.getString("sa.kpp")));
      kpCheck = new JCheckBox(LangTool.getString("sa.kpCheck"));

      if (getStringProperty("keypad").equals("Yes"))
         kpCheck.setSelected(true);

      kpp.add(kpCheck);

      contentPane.add(kpp);

   }

   public void save() {

   }

   public void applyAttributes() {

      if (kpCheck.isSelected()) {
         changes.firePropertyChange(this,"keypad",
                           getStringProperty("keypad"),
                           "Yes");
         setProperty("keypad","Yes");
      }
      else {
         changes.firePropertyChange(this,"keypad",
                           getStringProperty("keypad"),
                           "No");
         setProperty("keypad","No");
      }

   }
}