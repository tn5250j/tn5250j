package org.tn5250j.settings;
/**
 * Title: CursorAttributesPanel
 * Copyright:   Copyright (c) 2001, 2002
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

public class CursorAttributesPanel extends AttributesPanel {

   private static final long serialVersionUID = 1L;
JRadioButton cFull;
   JRadioButton cHalf;
   JRadioButton cLine;
   JRadioButton chNone;
   JRadioButton chHorz;
   JRadioButton chVert;
   JRadioButton chCross;
   JCheckBox rulerFixed;
   JTextField cursorBottOffset;
   JRadioButton blink;

   public CursorAttributesPanel(SessionConfig config ) {
      super(config,"Cursor");
   }

   /**Component initialization*/
   public void initPanel() throws Exception  {

      setLayout(new BorderLayout());
      contentPane = new JPanel();
      contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));
      add(contentPane,BorderLayout.NORTH);

      // define cursor size panel
      JPanel crp = new JPanel();
      crp.setBorder(BorderFactory.createTitledBorder(LangTool.getString("sa.crsSize")));
      cFull = new JRadioButton(LangTool.getString("sa.cFull"));
      cHalf = new JRadioButton(LangTool.getString("sa.cHalf"));
      cLine = new JRadioButton(LangTool.getString("sa.cLine"));

      // Group the radio buttons.
      ButtonGroup cGroup = new ButtonGroup();
      cGroup.add(cFull);
      cGroup.add(cHalf);
      cGroup.add(cLine);

      int cursorSize = 0;

      if (getStringProperty("cursorSize").equals("Full"))
         cursorSize = 2;
      if (getStringProperty("cursorSize").equals("Half"))
         cursorSize = 1;

      switch (cursorSize) {

         case 0:
            cLine.setSelected(true);
            break;
         case 1:
            cHalf.setSelected(true);
            break;
         case 2:
            cFull.setSelected(true);
            break;


      }
      crp.add(cFull);
      crp.add(cHalf);
      crp.add(cLine);

      // define cursor ruler panel
      JPanel chp = new JPanel();
      chp.setBorder(BorderFactory.createTitledBorder(LangTool.getString("sa.crossHair")));
      chNone = new JRadioButton(LangTool.getString("sa.chNone"));
      chHorz = new JRadioButton(LangTool.getString("sa.chHorz"));
      chVert = new JRadioButton(LangTool.getString("sa.chVert"));
      chCross = new JRadioButton(LangTool.getString("sa.chCross"));

      // Group the radio buttons.
      ButtonGroup chGroup = new ButtonGroup();
      chGroup.add(chNone);
      chGroup.add(chHorz);
      chGroup.add(chVert);
      chGroup.add(chCross);

      int crossHair = 0;

      if (getStringProperty("crossHair").equals("Horz"))
         crossHair = 1;
      if (getStringProperty("crossHair").equals("Vert"))
         crossHair = 2;
      if (getStringProperty("crossHair").equals("Both"))
         crossHair = 3;

      switch (crossHair) {

         case 0:
            chNone.setSelected(true);
            break;
         case 1:
            chHorz.setSelected(true);
            break;
         case 2:
            chVert.setSelected(true);
            break;
         case 3:
            chCross.setSelected(true);
            break;


      }
      chp.add(chNone);
      chp.add(chHorz);
      chp.add(chVert);
      chp.add(chCross);


      // define double click as enter
      JPanel rulerFPanel = new JPanel();
      rulerFPanel.setBorder(BorderFactory.createTitledBorder(""));

      rulerFixed = new JCheckBox(LangTool.getString("sa.rulerFixed"));
      rulerFixed.setSelected(true);
      if (getStringProperty("rulerFixed").equals("Yes"))
         rulerFixed.setSelected(false);

      rulerFPanel.add(rulerFixed);

      // define cursor ruler panel
      JPanel blinkPanel = new JPanel();
      blinkPanel.setBorder(BorderFactory.createTitledBorder(LangTool.getString("sa.blinkCursor")));

      blink = new JRadioButton(LangTool.getString("sa.blinkYes"));
      JRadioButton noBlink = new JRadioButton(LangTool.getString("sa.blinkNo"));

      // Group the radio buttons.
      ButtonGroup blinkGroup = new ButtonGroup();
      blinkGroup.add(blink);
      blinkGroup.add(noBlink);

      blink.setSelected(false);

      if (getStringProperty("cursorBlink").equals("Yes"))
         blink.setSelected(true);
      else
         noBlink.setSelected(true);

      blinkPanel.add(blink);
      blinkPanel.add(noBlink);

      // define bottom offset panel for cursor
      JPanel bottOffPanel = new JPanel();
      bottOffPanel.setBorder(BorderFactory.createTitledBorder(
                                    LangTool.getString("sa.curBottOffset")));

      cursorBottOffset = new JTextField(5);

      try {
         int i = Integer.parseInt(getStringProperty("cursorBottOffset","0"));
         cursorBottOffset.setText(Integer.toString(i));
      }
      catch (NumberFormatException ne) {
         cursorBottOffset.setText("0");
      }


      bottOffPanel.add(cursorBottOffset);

      contentPane.add(crp);
      contentPane.add(chp);
      contentPane.add(rulerFPanel);
      contentPane.add(blinkPanel);
      contentPane.add(bottOffPanel);

   }

   public void save() {

   }

   public void applyAttributes() {

      if (cFull.isSelected()) {
         changes.firePropertyChange(this,"cursorSize",
                           getStringProperty("cursorSize"),
                           "Full");
         setProperty("cursorSize","Full");

      }
      if (cHalf.isSelected()) {
         changes.firePropertyChange(this,"cursorSize",
                           getStringProperty("cursorSize"),
                           "Half");
         setProperty("cursorSize","Half");
      }
      if (cLine.isSelected()) {
         changes.firePropertyChange(this,"cursorSize",
                           getStringProperty("cursorSize"),
                           "Line");

         setProperty("cursorSize","Line");
      }

      if (chNone.isSelected()) {
         changes.firePropertyChange(this,"crossHair",
                           getStringProperty("crossHair"),
                           "None");
         setProperty("crossHair","None");

      }

      if (chHorz.isSelected()) {
         changes.firePropertyChange(this,"crossHair",
                           getStringProperty("crossHair"),
                           "Horz");
         setProperty("crossHair","Horz");

      }

      if (chVert.isSelected()) {
         changes.firePropertyChange(this,"crossHair",
                           getStringProperty("crossHair"),
                           "Vert");
         setProperty("crossHair","Vert");

      }

      if (chCross.isSelected()) {
         changes.firePropertyChange(this,"crossHair",
                           getStringProperty("crossHair"),
                           "Both");
         setProperty("crossHair","Both");

      }

      if (rulerFixed.isSelected()) {
         changes.firePropertyChange(this,"rulerFixed",
                           getStringProperty("rulerFixed"),
                           "No");
         setProperty("rulerFixed","No");
      }
      else {
         changes.firePropertyChange(this,"rulerFixed",
                           getStringProperty("rulerFixed"),
                           "Yes");
         setProperty("rulerFixed","Yes");
      }

      changes.firePropertyChange(this,"cursorBottOffset",
                        getStringProperty("cursorBottOffset"),
                        cursorBottOffset.getText());
      setProperty("cursorBottOffset",cursorBottOffset.getText());

      if (blink.isSelected()) {
         changes.firePropertyChange(this,"cursorBlink",
                           getStringProperty("cursorBlink"),
                           "Yes");
         setProperty("cursorBlink","Yes");
      }
      else {

         changes.firePropertyChange(this,"cursorBlink",
                           getStringProperty("cursorBlink"),
                           "No");
         setProperty("cursorBlink","No");

      }

   }
}
