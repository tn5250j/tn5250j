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
package org.tn5250j.settings;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import javax.swing.*;

import org.tn5250j.tools.*;
import org.tn5250j.SessionConfig;

public class SignoffAttributesPanel extends AttributesPanel {

   private static final long serialVersionUID = 1L;
JCheckBox signoffCheck;
   JTextField fromRow;
   JTextField fromCol;
   JTextField toRow;
   JTextField toCol;

   public SignoffAttributesPanel(SessionConfig config ) {
      super(config,"Signoff");
   }

   /**Component initialization*/
   public void initPanel() throws Exception  {

      setLayout(new BorderLayout());
      contentPane = new JPanel();
      contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));
      add(contentPane,BorderLayout.NORTH);

      // define signoff confirmation panel
      JPanel soConfirm = new JPanel();
      soConfirm.setBorder(BorderFactory.createTitledBorder(
                           LangTool.getString("sa.titleSignoff")));

      signoffCheck = new JCheckBox(LangTool.getString("sa.confirmSignoff"));

      // check if signoff confirmation is to be checked
      signoffCheck.setSelected(getStringProperty("confirmSignoff").equals("Yes"));

      signoffCheck.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            doItemStateChanged(e);
         }
      });

      soConfirm.add(signoffCheck);

      // define signoff confirmation screen region
      JPanel soRegion = new JPanel();
      soRegion.setBorder(BorderFactory.createTitledBorder(
                           LangTool.getString("sa.titleSignoffRegion")));

      AlignLayout rowcol = new AlignLayout(4,5,5);
      soRegion.setLayout(rowcol);

      soRegion.add(new JLabel(LangTool.getString("sa.fromRow")));
      fromRow = new JTextField("1",5);
      soRegion.add(fromRow);
      soRegion.add(new JLabel(LangTool.getString("sa.fromColumn")));
      fromCol = new JTextField("1",5);
      soRegion.add(fromCol);
      soRegion.add(new JLabel(LangTool.getString("sa.toRow")));
      toRow = new JTextField("24",5);
      soRegion.add(toRow);
      soRegion.add(new JLabel(LangTool.getString("sa.toColumn")));
      toCol = new JTextField("80",5);
      soRegion.add(toCol);

      loadRegion();

      toggleRegion(signoffCheck.isSelected());

      contentPane.add(soConfirm);
      contentPane.add(soRegion);

   }

   private void loadRegion() {

      Rectangle region = getRectangleProperty("signOnRegion");

      if (region.x == 0)
         fromRow.setText("1");
      else
         fromRow.setText(Integer.toString(region.x));

      if (region.y == 0)
         fromCol.setText("1");
      else
         fromCol.setText(Integer.toString(region.y));

      if (region.width == 0)
         toRow.setText("24");
      else
         toRow.setText(Integer.toString(region.width));

      if (region.height == 0)
         toCol.setText("80");
      else
         toCol.setText(Integer.toString(region.height));

   }

   /**
    * React on the state change for signoff confirmation
    *
    * @param e Item event to react to
    */
   private void doItemStateChanged(ItemEvent e) {

      toggleRegion(false);

      if (e.getStateChange() == ItemEvent.SELECTED) {
         if (signoffCheck.isSelected()) {
            toggleRegion(true);
         }
      }
   }

   private void toggleRegion(boolean state) {

      fromRow.setEnabled(state);
      fromCol.setEnabled(state);
      toRow.setEnabled(state);
      toCol.setEnabled(state);

   }

   public void save() {

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

      Rectangle region = new Rectangle(Integer.parseInt(fromRow.getText()),
                                          Integer.parseInt(fromCol.getText()),
                                          Integer.parseInt(toRow.getText()),
                                          Integer.parseInt(toCol.getText()));
      if (region.x < 0)
         region.x = 1;
      if (region.y < 0)
         region.y = 1;
      if (region.width > 24)
         region.width = 24;
      if (region.height > 80)
         region.height = 80;

      setRectangleProperty("signOnRegion",region);
   }
}