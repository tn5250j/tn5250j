package org.tn5250j.settings;
/**
 * Title: DisplayAttributesPanel
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

public class DisplayAttributesPanel extends AttributesPanel {

   JRadioButton csLine;
   JRadioButton csDot;
   JRadioButton csShortLine;
   JRadioButton saNormal;
   JCheckBox guiCheck;
   JCheckBox guiShowUnderline;

   public DisplayAttributesPanel(SessionConfig config ) {
      super(config,"Display");
   }

   /**Component initialization*/
   public void initPanel() throws Exception  {

      setLayout(new BorderLayout());
      contentPane = new JPanel();
      contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));
      add(contentPane,BorderLayout.NORTH);

      // define column separator panel
      JPanel csp = new JPanel();
      TitledBorder tb = BorderFactory.createTitledBorder(LangTool.getString("sa.cs"));
      csp.setBorder(tb);

      csLine = new JRadioButton(LangTool.getString("sa.csLine"));
      csLine.setActionCommand("Line");
      csDot = new JRadioButton(LangTool.getString("sa.csDot"));
      csDot.setActionCommand("Dot");
      csShortLine = new JRadioButton(LangTool.getString("sa.csShortLine"));
      csShortLine.setActionCommand("ShortLine");

      // Group the radio buttons.
      ButtonGroup csGroup = new ButtonGroup();
      csGroup.add(csLine);
      csGroup.add(csDot);
      csGroup.add(csShortLine);

      if (getStringProperty("colSeparator").equals("Dot"))
         csDot.setSelected(true);
      else if (getStringProperty("colSeparator").equals("ShortLine"))
         csShortLine.setSelected(true);
      else
         csLine.setSelected(true);

      csp.add(csLine);
      csp.add(csDot);
      csp.add(csShortLine);


      // define show attributs panel
      JPanel sap = new JPanel();
      sap.setBorder(
         BorderFactory.createTitledBorder(LangTool.getString("sa.showAttr")));

      saNormal = new JRadioButton(LangTool.getString("sa.showNormal"));
      saNormal.setActionCommand("Normal");
      JRadioButton saHex = new JRadioButton(LangTool.getString("sa.showHex"));
      saHex.setActionCommand("Hex");

      // Group the radio buttons.
      ButtonGroup saGroup = new ButtonGroup();
      saGroup.add(saNormal);
      saGroup.add(saHex);

      if (getStringProperty("showAttr").equals("Hex"))
         saHex.setSelected(true);
      else
         saNormal.setSelected(true);

      sap.add(saNormal);
      sap.add(saHex);

      // define gui panel
      JPanel cgp = new JPanel();
      cgp.setBorder(BorderFactory.createTitledBorder(LangTool.getString("sa.cgp")));
      cgp.setLayout(new AlignLayout(1,5,5));

      guiCheck = new JCheckBox(LangTool.getString("sa.guiCheck"));
      guiShowUnderline = new JCheckBox(LangTool.getString("sa.guiShowUnderline"));

      if (getStringProperty("guiInterface").equals("Yes"))
         guiCheck.setSelected(true);

      // since this is a new property added then it might not exist in existing
      //    profiles and it should be defaulted to yes.
      String under = getStringProperty("guiShowUnderline");
      if (under.equals("Yes") || under.length() == 0)
         guiShowUnderline.setSelected(true);

      cgp.add(guiCheck);
      cgp.add(guiShowUnderline);

      contentPane.add(csp);
      contentPane.add(sap);
      contentPane.add(cgp);

   }

   public void save() {

   }

   public void applyAttributes() {

      if (csLine.isSelected()) {
         changes.firePropertyChange(this,"colSeparator",
                           getStringProperty("colSeparator"),
                           "Line");
         setProperty("colSeparator","Line");
      }
      else if (csShortLine.isSelected()) {
         changes.firePropertyChange(this,"colSeparator",
                           getStringProperty("colSeparator"),
                           "ShortLine");
         setProperty("colSeparator","ShortLine");
      }

      else {
         changes.firePropertyChange(this,"colSeparator",
                           getStringProperty("colSeparator"),
                           "Dot");
         setProperty("colSeparator","Dot");

      }

      if (saNormal.isSelected()) {
         changes.firePropertyChange(this,"showAttr",
                           getStringProperty("showAttr"),
                           "Normal");
         setProperty("showAttr","Normal");
      }
      else {
         changes.firePropertyChange(this,"showAttr",
                           getStringProperty("showAttr"),
                           "Hex");
         setProperty("showAttr","Hex");

      }

      if (guiCheck.isSelected()) {
         changes.firePropertyChange(this,"guiInterface",
                           getStringProperty("guiInterface"),
                           "Yes");
         setProperty("guiInterface","Yes");
      }
      else {
         changes.firePropertyChange(this,"guiInterface",
                           getStringProperty("guiInterface"),
                           "No");
         setProperty("guiInterface","No");
      }

      if (guiShowUnderline.isSelected()) {
         changes.firePropertyChange(this,"guiShowUnderline",
                           getStringProperty("guiShowUnderline"),
                           "Yes");
         setProperty("guiShowUnderline","Yes");
      }
      else {
         changes.firePropertyChange(this,"guiShowUnderline",
                           getStringProperty("guiShowUnderline"),
                           "No");
         setProperty("guiShowUnderline","No");
      }

   }
}