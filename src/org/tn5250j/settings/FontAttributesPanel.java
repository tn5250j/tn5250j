/**
 * Title: ColorAttributesPanel
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

import org.tn5250j.tools.*;
import org.tn5250j.SessionConfig;

public class FontAttributesPanel extends AttributesPanel {

   private static final long serialVersionUID = 1L;
JComboBox fontsList;
   JCheckBox useAntialias;
   JTextField verticalScale;
   JTextField horizontalScale;
   JTextField pointSize;

   public FontAttributesPanel(SessionConfig config ) {
      super(config,"Fonts");
   }

   /**Component initialization*/
   public void initPanel() throws Exception  {

      setLayout(new BorderLayout());
      contentPane = new JPanel();
      contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));
      add(contentPane,BorderLayout.NORTH);

      // fonts
      Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();

      JPanel flp = new JPanel();
      TitledBorder tb = BorderFactory.createTitledBorder(LangTool.getString("sa.font"));
      flp.setBorder(tb);


      fontsList = new JComboBox();

      String font = getStringProperty("font");

      for (int x = 0; x < fonts.length; x++) {
         if (fonts[x].getFontName().indexOf('.') < 0)
            fontsList.addItem(fonts[x].getFontName());
      }

      fontsList.setSelectedItem(font);

      useAntialias = new JCheckBox(LangTool.getString("sa.useAntialias"));
      useAntialias.setSelected(getBooleanProperty("useAntialias",true));

      flp.add(fontsList);
      flp.add(useAntialias);

      JPanel fsp = new JPanel();
      fsp.setLayout(new AlignLayout(2,5,5));
      tb = BorderFactory.createTitledBorder(LangTool.getString("sa.scaleLabel"));
      fsp.setBorder(tb);


      verticalScale = new JTextField("1.2",5);
      horizontalScale = new JTextField("1.0",5);
      pointSize = new JTextField("0",5);
      if (getStringProperty("fontScaleWidth").length() != 0)
         horizontalScale.setText(getStringProperty("fontScaleWidth"));
      if (getStringProperty("fontScaleHeight").length() != 0)
         verticalScale.setText(getStringProperty("fontScaleHeight"));
      if (getStringProperty("fontPointSize").length() != 0)
         pointSize.setText(getStringProperty("fontPointSize"));
      fsp.add(new JLabel(LangTool.getString("sa.fixedPointSize")));
      fsp.add(pointSize);
      fsp.add(new JLabel(LangTool.getString("sa.horScaleLabel")));
      fsp.add(horizontalScale);
      fsp.add(new JLabel(LangTool.getString("sa.vertScaleLabel")));
      fsp.add(verticalScale);

      contentPane.add(flp);
      contentPane.add(fsp);

   }

   public void save() {

   }

   public void applyAttributes() {

      if (!getStringProperty("font").equals(
               fontsList.getSelectedItem())
         ) {
         changes.firePropertyChange(this,"font",
                           getStringProperty("font"),
                           fontsList.getSelectedItem());

         setProperty("font",(String)fontsList.getSelectedItem());
      }

      if (useAntialias.isSelected()) {
         changes.firePropertyChange(this,"useAntialias",
                           getStringProperty("useAntialias"),
                           "Yes");
         setProperty("useAntialias","Yes");
      }
      else {

         changes.firePropertyChange(this,"useAntialias",
                           getStringProperty("useAntialias"),
                           "No");
         setProperty("useAntialias","No");

      }

      changes.firePropertyChange(this,"fontScaleHeight",
                        getStringProperty("fontScaleHeight"),
                        verticalScale.getText());
      setProperty("fontScaleHeight",verticalScale.getText());

      changes.firePropertyChange(this,"fontScaleWidth",
                        getStringProperty("fontScaleWidth"),
                        horizontalScale.getText());
      setProperty("fontScaleWidth",horizontalScale.getText());

      changes.firePropertyChange(this,"fontPointSize",
                        getStringProperty("fontPointSize"),
                        pointSize.getText());
      setProperty("fontPointSize",pointSize.getText());
   }
}