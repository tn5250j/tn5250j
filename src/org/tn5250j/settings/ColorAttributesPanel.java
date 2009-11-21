package org.tn5250j.settings;
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

import java.awt.event.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;

import org.tn5250j.tools.*;
import org.tn5250j.SessionConfig;

public class ColorAttributesPanel extends AttributesPanel {

   private static final long serialVersionUID = 1L;
JComboBox colorSchemaList;
   JComboBox colorList;
   JColorChooser jcc;
   Schema colorSchema;
   Properties schemaProps;


   public ColorAttributesPanel(SessionConfig config ) {
      super(config,"Colors");
   }

   /**Component initialization*/
   public void initPanel() throws Exception  {

      JPanel cp = new JPanel();
      cp.setLayout(new BorderLayout());

      JPanel cschp = new JPanel();
      TitledBorder tb = BorderFactory.createTitledBorder(LangTool.getString("sa.colorSchema"));
      cschp.setBorder(tb);
      colorSchemaList = new JComboBox();
      loadSchemas(colorSchemaList);

      colorSchemaList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                Object obj = cb.getSelectedItem();
                if (obj instanceof Schema) {

                  System.out.println(" we got a schema ");
                  colorSchema = (Schema)obj;
                }
                else {
                  colorSchema = null;
                }

            }
        });


      cschp.add(colorSchemaList);

      tb = BorderFactory.createTitledBorder(LangTool.getString("sa.colors"));
      cp.setBorder(tb);
      colorList = new JComboBox();
      colorList.addItem(LangTool.getString("sa.bg"));
      colorList.addItem(LangTool.getString("sa.blue"));
      colorList.addItem(LangTool.getString("sa.red"));
      colorList.addItem(LangTool.getString("sa.pink"));
      colorList.addItem(LangTool.getString("sa.green"));
      colorList.addItem(LangTool.getString("sa.turq"));
      colorList.addItem(LangTool.getString("sa.yellow"));
      colorList.addItem(LangTool.getString("sa.white"));
      colorList.addItem(LangTool.getString("sa.guiField"));
      colorList.addItem(LangTool.getString("sa.cursorColor"));
      colorList.addItem(LangTool.getString("sa.columnSep"));
      colorList.addItem(LangTool.getString("sa.hexAttrColor"));

      jcc = new JColorChooser();

      // set the default color for display as that being for back ground
      jcc.setColor(getColorProperty("colorBg"));

      colorList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                String newSelection = (String)cb.getSelectedItem();
                if (newSelection.equals(LangTool.getString("sa.bg"))) {
                  if (colorSchema != null)
                     jcc.setColor(colorSchema.getColorBg());
                  else
                     jcc.setColor(getColorProperty("colorBg"));
                }
                if (newSelection.equals(LangTool.getString("sa.blue"))) {
                  if (colorSchema != null)
                     jcc.setColor(colorSchema.getColorBlue());
                  else
                     jcc.setColor(getColorProperty("colorBlue"));
                }
                if (newSelection.equals(LangTool.getString("sa.red"))) {
                  if (colorSchema != null)
                     jcc.setColor(colorSchema.getColorRed());
                  else
                     jcc.setColor(getColorProperty("colorRed"));

                }
                if (newSelection.equals(LangTool.getString("sa.pink"))) {
                  if (colorSchema != null)
                     jcc.setColor(colorSchema.getColorPink());
                  else
                     jcc.setColor(getColorProperty("colorPink"));

                }
                if (newSelection.equals(LangTool.getString("sa.green"))) {
                  if (colorSchema != null)
                     jcc.setColor(colorSchema.getColorGreen());
                  else
                     jcc.setColor(getColorProperty("colorGreen"));

                }
                if (newSelection.equals(LangTool.getString("sa.turq"))) {
                  if (colorSchema != null)
                     jcc.setColor(colorSchema.getColorTurq());
                  else
                     jcc.setColor(getColorProperty("colorTurq"));

                }
                if (newSelection.equals(LangTool.getString("sa.yellow"))) {
                  if (colorSchema != null)
                     jcc.setColor(colorSchema.getColorYellow());
                  else
                     jcc.setColor(getColorProperty("colorYellow"));

                }
                if (newSelection.equals(LangTool.getString("sa.white"))) {
                  if (colorSchema != null)
                     jcc.setColor(colorSchema.getColorWhite());
                  else
                     jcc.setColor(getColorProperty("colorWhite"));
                }

                if (newSelection.equals(LangTool.getString("sa.guiField"))) {
                  if (colorSchema != null)
                     jcc.setColor(colorSchema.getColorGuiField());
                  else
                     jcc.setColor(getColorProperty("colorGUIField",Color.white));
                }
                if (newSelection.equals(LangTool.getString("sa.cursorColor"))) {
                  if (colorSchema != null)
                     jcc.setColor(colorSchema.getColorBg());
                  else
                     jcc.setColor(getColorProperty("colorCursor",
                              getColorProperty("colorBg")));
                }
                if (newSelection.equals(LangTool.getString("sa.columnSep"))) {
                  if (colorSchema != null)
                     jcc.setColor(colorSchema.getColorSeparator());
                  else
                     jcc.setColor(getColorProperty("colorSep",
                              getColorProperty("colorWhite")));
                }

                if (newSelection.equals(LangTool.getString("sa.hexAttrColor"))) {
                  if (colorSchema != null)
                     jcc.setColor(colorSchema.getColorHexAttr());
                  else
                     jcc.setColor(getColorProperty("colorHexAttr",
                              getColorProperty("colorWhite")));
                }
            }
        });


      cp.add(colorList,BorderLayout.NORTH);
      cp.add(jcc,BorderLayout.CENTER);

      add(cschp,BorderLayout.NORTH);
      add(cp,BorderLayout.CENTER);

   }

   public void save() {

   }

   public void applyAttributes() {

      String newSelection = (String)colorList.getSelectedItem();

      if (colorSchema != null) {

         if (!getColorProperty("colorBg").equals(colorSchema.getColorBg())) {
            changes.firePropertyChange(this,"colorBg",
                           getColorProperty("colorBg"),
                           colorSchema.getColorBg());

            setProperty("colorBg",Integer.toString(colorSchema.getColorBg().getRGB()));

         }
         if (!getColorProperty("colorBlue").equals(colorSchema.getColorBlue())) {
            changes.firePropertyChange(this,"colorBlue",
                           getColorProperty("colorBlue"),
                           colorSchema.getColorBlue());
            setProperty("colorBlue",Integer.toString(colorSchema.getColorBlue().getRGB()));

         }
         if (!getColorProperty("colorRed").equals(colorSchema.getColorRed())) {
            changes.firePropertyChange(this,"colorRed",
                           getColorProperty("colorRed"),
                           colorSchema.getColorRed());
            setProperty("colorRed",Integer.toString(colorSchema.getColorRed().getRGB()));

         }
         if (!getColorProperty("colorPink").equals(colorSchema.getColorPink())) {
            changes.firePropertyChange(this,"colorPink",
                           getColorProperty("colorPink"),
                           colorSchema.getColorPink());
            setProperty("colorPink",Integer.toString(colorSchema.getColorPink().getRGB()));

         }
         if (!getColorProperty("colorGreen").equals(colorSchema.getColorGreen())) {
            changes.firePropertyChange(this,"colorGreen",
                           getColorProperty("colorGreen"),
                           colorSchema.getColorGreen());

            setProperty("colorGreen",Integer.toString(colorSchema.getColorGreen().getRGB()));

         }
         if (!getColorProperty("colorTurq").equals(colorSchema.getColorTurq())) {
            changes.firePropertyChange(this,"colorTurq",
                           getColorProperty("colorTurq"),
                           colorSchema.getColorTurq());

            setProperty("colorTurq",Integer.toString(colorSchema.getColorTurq().getRGB()));

         }

         if (!getColorProperty("colorYellow").equals(colorSchema.getColorYellow())) {
            changes.firePropertyChange(this,"colorYellow",
                           getColorProperty("colorYellow"),
                           colorSchema.getColorYellow());
            setProperty("colorYellow",Integer.toString(colorSchema.getColorYellow().getRGB()));

         }
         if (!getColorProperty("colorWhite").equals(colorSchema.getColorWhite())) {
            changes.firePropertyChange(this,"colorWhite",
                           getColorProperty("colorWhite"),
                           colorSchema.getColorWhite());

            setProperty("colorWhite",Integer.toString(colorSchema.getColorWhite().getRGB()));

         }
         if (!getColorProperty("colorGUIField").equals(colorSchema.getColorGuiField())) {
            changes.firePropertyChange(this,"colorGUIField",
                           getColorProperty("colorGUIField"),
                           colorSchema.getColorGuiField());

            setProperty("colorGUIField",Integer.toString(colorSchema.getColorGuiField().getRGB()));

         }
         if (!getColorProperty("colorCursor").equals(colorSchema.getColorCursor())) {
            changes.firePropertyChange(this,"colorCursor",
                           getColorProperty("colorCursor"),
                           colorSchema.getColorCursor());

            setProperty("colorCursor",Integer.toString(colorSchema.getColorCursor().getRGB()));

         }

         if (!getColorProperty("colorSep").equals(colorSchema.getColorSeparator())) {
            changes.firePropertyChange(this,"colorSep",
                           getColorProperty("colorSep"),
                           colorSchema.getColorSeparator());

            setProperty("colorSep",
                     Integer.toString(colorSchema.getColorSeparator().getRGB()));

         }

         if (!getColorProperty("colorHexAttr").equals(colorSchema.getColorHexAttr())) {
            changes.firePropertyChange(this,"colorHexAttr",
                           getColorProperty("colorHexAttr"),
                           colorSchema.getColorHexAttr());

            setProperty("colorHexAttr",
                     Integer.toString(colorSchema.getColorHexAttr().getRGB()));

         }

      }
      else {

         Color nc = jcc.getColor();
         if (newSelection.equals(LangTool.getString("sa.bg"))) {
            if (!getColorProperty("colorBg").equals(nc)) {
               changes.firePropertyChange(this,"colorBg",
                              getColorProperty("colorBg"),
                              nc);

               setProperty("colorBg",Integer.toString(nc.getRGB()));

            }
         }
         if (newSelection.equals(LangTool.getString("sa.blue"))) {
            if (!getColorProperty("colorBlue").equals(nc)) {
               changes.firePropertyChange(this,"colorBlue",
                              getColorProperty("colorBlue"),
                              nc);
               setProperty("colorBlue",Integer.toString(nc.getRGB()));

            }
         }
         if (newSelection.equals(LangTool.getString("sa.red"))) {
            if (!getColorProperty("colorRed").equals(nc)) {
               changes.firePropertyChange(this,"colorRed",
                              getColorProperty("colorRed"),
                              nc);
               setProperty("colorRed",Integer.toString(nc.getRGB()));

            }
         }
         if (newSelection.equals(LangTool.getString("sa.pink"))) {
            if (!getColorProperty("colorPink").equals(nc)) {
               changes.firePropertyChange(this,"colorPink",
                              getColorProperty("colorPink"),
                              nc);
               setProperty("colorPink",Integer.toString(nc.getRGB()));

            }
         }
         if (newSelection.equals(LangTool.getString("sa.green"))) {
            if (!getColorProperty("colorGreen").equals(nc)) {
               changes.firePropertyChange(this,"colorGreen",
                              getColorProperty("colorGreen"),
                              nc);

               setProperty("colorGreen",Integer.toString(nc.getRGB()));

            }
         }
         if (newSelection.equals(LangTool.getString("sa.turq"))) {
            if (!getColorProperty("colorTurq").equals(nc)) {
               changes.firePropertyChange(this,"colorTurq",
                              getColorProperty("colorTurq"),
                              nc);

               setProperty("colorTurq",Integer.toString(nc.getRGB()));

            }

         }
         if (newSelection.equals(LangTool.getString("sa.yellow"))) {
            if (!getColorProperty("colorYellow").equals(nc)) {
               changes.firePropertyChange(this,"colorYellow",
                              getColorProperty("colorYellow"),
                              nc);
               setProperty("colorYellow",Integer.toString(nc.getRGB()));

            }
         }
         if (newSelection.equals(LangTool.getString("sa.white"))) {
            if (!getColorProperty("colorWhite").equals(nc)) {
               changes.firePropertyChange(this,"colorWhite",
                              getColorProperty("colorWhite"),
                              nc);

               setProperty("colorWhite",Integer.toString(nc.getRGB()));

            }
         }

         if (newSelection.equals(LangTool.getString("sa.guiField"))) {
            if (!getColorProperty("colorGUIField").equals(nc)) {
               changes.firePropertyChange(this,"colorGUIField",
                              getColorProperty("colorGUIField"),
                              nc);

               setProperty("colorGUIField",Integer.toString(nc.getRGB()));

            }
         }

         if (newSelection.equals(LangTool.getString("sa.cursorColor"))) {
            if (!getColorProperty("colorCursor").equals(nc)) {
               changes.firePropertyChange(this,"colorCursor",
                              getColorProperty("colorCursor"),
                              nc);

               setProperty("colorCursor",Integer.toString(nc.getRGB()));

            }
         }
         if (newSelection.equals(LangTool.getString("sa.columnSep"))) {
            if (!getColorProperty("colorSep").equals(nc)) {
               changes.firePropertyChange(this,"colorSep",
                              getColorProperty("colorSep"),
                              nc);

               setProperty("colorSep",Integer.toString(nc.getRGB()));

            }
         }
         if (newSelection.equals(LangTool.getString("sa.cursorColor"))) {
            if (!getColorProperty("colorCursor").equals(nc)) {
               changes.firePropertyChange(this,"colorCursor",
                              getColorProperty("colorCursor"),
                              nc);

               setProperty("colorCursor",Integer.toString(nc.getRGB()));

            }
         }

         if (newSelection.equals(LangTool.getString("sa.hexAttrColor"))) {
            if (!getColorProperty("colorHexAttr").equals(nc)) {
               changes.firePropertyChange(this,"colorHexAttr",
                              getColorProperty("colorHexAttr"),
                              nc);

               setProperty("colorHexAttr",Integer.toString(nc.getRGB()));

            }
         }

      }

   }

   private void loadSchemas(JComboBox schemas) {

      schemaProps = new Properties();
      URL file=null;

      try {
         ClassLoader cl = this.getClass().getClassLoader();
         if (cl == null)
            cl = ClassLoader.getSystemClassLoader();
         file = cl.getResource("tn5250jSchemas.properties");
         schemaProps.load(file.openStream());
      }
      catch (Exception e) {
         System.err.println(e);
      }

      schemas.addItem(LangTool.getString("sa.colorDefault"));
      int numSchemas = Integer.parseInt((String)schemaProps.get("schemas"));
      Schema s = null;
      String prefix = "";
      for (int x = 1; x <= numSchemas; x++) {
         s = new Schema();
         prefix = "schema" + x;
         s.setDescription((String)schemaProps.get(prefix + ".title"));
         s.setColorBg(getSchemaProp(prefix + ".colorBg"));
         s.setColorRed(getSchemaProp(prefix + ".colorRed"));
         s.setColorTurq(getSchemaProp(prefix + ".colorTurq"));
         s.setColorCursor(getSchemaProp(prefix + ".colorCursor"));
         s.setColorGuiField(getSchemaProp(prefix + ".colorGUIField"));
         s.setColorWhite(getSchemaProp(prefix + ".colorWhite"));
         s.setColorYellow(getSchemaProp(prefix + ".colorYellow"));
         s.setColorGreen(getSchemaProp(prefix + ".colorGreen"));
         s.setColorPink(getSchemaProp(prefix + ".colorPink"));
         s.setColorBlue(getSchemaProp(prefix + ".colorBlue"));
         s.setColorSeparator(getSchemaProp(prefix + ".colorSep"));
         s.setColorHexAttr(getSchemaProp(prefix + ".colorHexAttr"));
         schemas.addItem(s);
      }
   }

   private int getSchemaProp(String key) {

      if(schemaProps.containsKey(key)) {

         return Integer.parseInt((String)schemaProps.get(key));

      }
      else {
         return 0;
      }

   }

   class Schema {


      public String toString() {

         return description;

      }

      public void setDescription(String desc) {

         description = desc;
      }

      public void setColorBg(int color) {

         bg = new Color(color);
      }

      public Color getColorBg() {

         return bg;
      }

      public void setColorBlue(int color) {

         blue = new Color(color);
      }

      public Color getColorBlue() {

         return blue;
      }

      public void setColorRed(int color) {

         red = new Color(color);
      }

      public Color getColorRed() {

         return red;
      }

      public void setColorPink(int color) {

         pink = new Color(color);
      }

      public Color getColorPink() {

         return pink;
      }

      public void setColorGreen(int color) {

         green = new Color(color);
      }

      public Color getColorGreen() {

         return green;
      }

      public void setColorTurq(int color) {

         turq = new Color(color);
      }

      public Color getColorTurq() {

         return turq;
      }

      public void setColorYellow(int color) {

         yellow = new Color(color);
      }

      public Color getColorYellow() {

         return yellow;
      }

      public void setColorWhite(int color) {

         white = new Color(color);
      }

      public Color getColorWhite() {

         return white;
      }

      public void setColorGuiField(int color) {

         gui = new Color(color);
      }

      public Color getColorGuiField() {

         return gui;
      }

      public void setColorCursor(int color) {

         cursor = new Color(color);
      }

      public Color getColorCursor() {


         return cursor;
      }

      public void setColorSeparator(int color) {

         columnSep = new Color(color);
      }

      public Color getColorSeparator() {


         return columnSep;
      }

      public void setColorHexAttr(int color) {

         hexAttr = new Color(color);
      }

      public Color getColorHexAttr() {


         return hexAttr;
      }

      private String description;
      private Color bg;
      private Color blue;
      private Color red;
      private Color pink;
      private Color green;
      private Color turq;
      private Color white;
      private Color yellow;
      private Color gui;
      private Color cursor;
      private Color columnSep;
      private Color hexAttr;
   }
}