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

import org.tn5250j.tools.*;
import org.tn5250j.SessionConfig;

public class OnConnectAttributesPanel extends AttributesPanel {

   private static final long serialVersionUID = 1L;
JTextField connectMacro;

   public OnConnectAttributesPanel(SessionConfig config ) {
      super(config,"OnConnect");
   }

   /**Component initialization*/
   public void initPanel() throws Exception  {

      setLayout(new BorderLayout());
      contentPane = new JPanel();
      contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));
      add(contentPane,BorderLayout.NORTH);

      // define onConnect macro to run
      JPanel ocMacrop = new JPanel();
      ocMacrop.setBorder(BorderFactory.createTitledBorder(LangTool.getString("sa.connectMacro")));

      connectMacro = new JTextField();
      connectMacro.setColumns(30);

      // sets the connect macro
      connectMacro.setText(getStringProperty("connectMacro"));

      ocMacrop.add(connectMacro);
      contentPane.add(ocMacrop);

   }

   public void save() {

   }

   public void applyAttributes() {

      changes.firePropertyChange(this,"connectMacro",
                        getStringProperty("connectMacro"),
                        connectMacro.getText());
      setProperty("connectMacro",connectMacro.getText());

   }
}