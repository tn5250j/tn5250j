/**
 * Title: ErrorResetAttributesPanel
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
package org.tn5250j.settings;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;

import org.tn5250j.tools.*;
import org.tn5250j.SessionConfig;

public class ErrorResetAttributesPanel extends AttributesPanel {

   JCheckBox resetRequired;

   public ErrorResetAttributesPanel(SessionConfig config ) {
      super(config,"ErrorReset");
   }

   /**Component initialization*/
   public void initPanel() throws Exception  {

      setLayout(new BorderLayout());
      contentPane = new JPanel();
      contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));
      add(contentPane,BorderLayout.NORTH);

      // define double click as enter
      JPanel reset = new JPanel();
      reset.setBorder(BorderFactory.createTitledBorder(
                           LangTool.getString("sa.titleErrorReset")));

      resetRequired = new JCheckBox(LangTool.getString("sa.errorReset"));

      // check if reset required is set or not
      resetRequired.setSelected(getStringProperty("resetRequired").equals("Yes"));

      reset.add(resetRequired);
      contentPane.add(reset);

   }

   public void save() {

   }

   public void applyAttributes() {

      if (resetRequired.isSelected()) {
         changes.firePropertyChange(this,"resetRequired",
                           getStringProperty("resetRequired"),
                           "Yes");
         setProperty("resetRequired","Yes");
      }
      else {
         changes.firePropertyChange(this,"resetRequired",
                           getStringProperty("resetRequired"),
                           "No");
         setProperty("resetRequired","No");
      }

   }
}