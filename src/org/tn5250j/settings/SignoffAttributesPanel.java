package org.tn5250j.settings;
/**
 * Title: SignoffAttributesPanel
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

public class SignoffAttributesPanel extends AttributesPanel {

   JCheckBox signoffCheck;

   public SignoffAttributesPanel(SessionConfig config ) {
      super(config);
   }

   /**Component initialization*/
   protected void initPanel() throws Exception  {

      // define double click as enter
      JPanel soConfirm = new JPanel();
      soConfirm.setBorder(BorderFactory.createTitledBorder(
                           LangTool.getString("sa.titleSignoff")));

      signoffCheck = new JCheckBox(LangTool.getString("sa.confirmSignoff"));

      // check if double click sends enter
      signoffCheck.setSelected(getStringProperty("confirmSignoff").equals("Yes"));

      soConfirm.add(signoffCheck);
      add(soConfirm);

   }

   public void applyAttributes() {

      if (signoffCheck.isSelected()) {
         changes.firePropertyChange(this,"confirmSignoff",
                           getStringProperty("confirmSignoff"),
                           "Yes");
         setProperty("confirmSignoff","Yes");
      }
      else {
         changes.firePropertyChange(this,"confirmSignoff",
                           getStringProperty("confirmSignoff"),
                           "No");
         setProperty("confirmSignoff","No");
      }

   }
}