package org.tn5250j;
/**
 * Title: tn5250J
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
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeSelectionModel;
import java.util.*;
import java.beans.*;
import java.io.*;
import org.tn5250j.tools.*;
import java.net.*;
import java.text.*;

public class SessionAttributes extends JDialog {

   String fileName;
   Properties props = null;
   Gui5250 gui = null;
   JPanel jpm = new JPanel(new BorderLayout());
   JComboBox fontsList;
   JComboBox colorSchemaList;
   JComboBox colorList;
   JColorChooser jcc;
   JRadioButton csLine;
   JRadioButton cFull;
   JRadioButton cHalf;
   JRadioButton cLine;
   JRadioButton saNormal;
   JRadioButton chNone;
   JRadioButton chHorz;
   JRadioButton chVert;
   JRadioButton chCross;
   JCheckBox guiCheck;
   JCheckBox guiShowUnderline;
   JCheckBox hsCheck;
   JCheckBox kpCheck;
   JCheckBox dceCheck;
   JTextField hsMore;
   JTextField hsBottom;
   Properties schemaProps;
   Schema colorSchema;
   JTextField verticalScale;
   JTextField horizontalScale;
   JTextField pointSize;

   private PropertyChangeSupport changes = null;

   public SessionAttributes(String fileName, Properties props, Frame parent) {
      super(parent);
      this.fileName = fileName;
      this.props = props;
      this.gui = gui;

      try {
         jbInit();
      }
      catch(Exception e) {
         e.printStackTrace();
      }
   }

   /**Component initialization*/
   private void jbInit() throws Exception  {

      changes = new PropertyChangeSupport(this);

      Dimension ps = null;
      // fonts
      Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
      // define fonts panel
      final JPanel fp = new JPanel();


      fp.setLayout(new BorderLayout());

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

      flp.add(fontsList);

//      NumberFormat nf = NumberFormat.getNumberInstance();
//      ((DecimalFormat)nf).setPositiveSuffix(" ");
//      ((DecimalFormat)nf).setMaximumFractionDigits(1);
//      ((DecimalFormat)nf).setMinimumFractionDigits(1);

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

      fp.add(flp,BorderLayout.NORTH);
      fp.add(fsp,BorderLayout.SOUTH);

      // define colors panel
      final JPanel cpp = new JPanel();
      cpp.setLayout(new BorderLayout());

      JPanel cp = new JPanel();
      cp.setLayout(new BorderLayout());

      JPanel cschp = new JPanel();
      tb = BorderFactory.createTitledBorder(LangTool.getString("sa.colorSchema"));
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
                  jcc.setColor(getColorProperty("colorBg"));
                }
                if (newSelection.equals(LangTool.getString("sa.blue"))) {
                  jcc.setColor(getColorProperty("colorBlue"));
                }
                if (newSelection.equals(LangTool.getString("sa.red"))) {
                  jcc.setColor(getColorProperty("colorRed"));

                }
                if (newSelection.equals(LangTool.getString("sa.pink"))) {
                  jcc.setColor(getColorProperty("colorPink"));

                }
                if (newSelection.equals(LangTool.getString("sa.green"))) {
                  jcc.setColor(getColorProperty("colorGreen"));

                }
                if (newSelection.equals(LangTool.getString("sa.turq"))) {
                  jcc.setColor(getColorProperty("colorTurq"));

                }
                if (newSelection.equals(LangTool.getString("sa.yellow"))) {
                  jcc.setColor(getColorProperty("colorYellow"));

                }
                if (newSelection.equals(LangTool.getString("sa.white"))) {
                  jcc.setColor(getColorProperty("colorWhite"));
                }

                if (newSelection.equals(LangTool.getString("sa.guiField"))) {
                  jcc.setColor(getColorProperty("colorGUIField",Color.white));
                }
                if (newSelection.equals(LangTool.getString("sa.cursorColor"))) {
                  jcc.setColor(getColorProperty("colorCursor",
                              getColorProperty("colorBg")));
                }
                if (newSelection.equals(LangTool.getString("sa.columnSep"))) {
                  jcc.setColor(getColorProperty("colorSep",
                              getColorProperty("colorWhite")));
                }

                if (newSelection.equals(LangTool.getString("sa.hexAttrColor"))) {
                  jcc.setColor(getColorProperty("colorHexAttr",
                              getColorProperty("colorWhite")));
                }

            }
        });


      cp.add(colorList,BorderLayout.NORTH);
      cp.add(jcc,BorderLayout.CENTER);

      cpp.add(cschp,BorderLayout.NORTH);
      cpp.add(cp,BorderLayout.CENTER);

      // define display panel
      final JPanel display = new JPanel();
      display.setLayout(new BoxLayout(display,BoxLayout.Y_AXIS));

      // define column separator panel
      JPanel csp = new JPanel();
      tb = BorderFactory.createTitledBorder(LangTool.getString("sa.cs"));
      csp.setBorder(tb);

      csLine = new JRadioButton(LangTool.getString("sa.csLine"));
      csLine.setActionCommand("Line");
      JRadioButton csDot = new JRadioButton(LangTool.getString("sa.csDot"));
      csLine.setActionCommand("Dot");

      // Group the radio buttons.
      ButtonGroup csGroup = new ButtonGroup();
      csGroup.add(csLine);
      csGroup.add(csDot);

      if (getStringProperty("colSeparator").equals("Dot"))
         csDot.setSelected(true);
      else
         csLine.setSelected(true);

      csp.add(csLine);
      csp.add(csDot);

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
      JPanel dcep = new JPanel();
      dcep.setBorder(BorderFactory.createTitledBorder(LangTool.getString("sa.crossHair")));

      dceCheck = new JCheckBox();

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
//      cgp.setLayout(new BoxLayout(cgp,BoxLayout.Y_AXIS));

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

      display.add(crp);
      display.add(chp);
      display.add(csp);
      display.add(sap);
      display.add(cgp);

      // define hotspot panel
      final JPanel hotspot = new JPanel();
      hotspot.setLayout(new BoxLayout(hotspot,BoxLayout.Y_AXIS));

      // define hsPanel panel
      JPanel hsp = new JPanel();
      hsp.setBorder(BorderFactory.createTitledBorder(LangTool.getString("sa.hsp")));
      hsCheck = new JCheckBox(LangTool.getString("sa.hsCheck"));

      if (getStringProperty("hotspots").equals("Yes"))
         hsCheck.setSelected(true);

      hsp.add(hsCheck);

      // define assignment panel
      JPanel hsap = new JPanel();
      hsap.setBorder(BorderFactory.createTitledBorder(LangTool.getString("sa.hsap")));
      hsap.setLayout(new GridLayout(2,2));

      JLabel moreLabel = new JLabel(LangTool.getString("sa.hsMore"));
      JLabel bottomLabel = new JLabel(LangTool.getString("sa.hsBottom"));
      hsMore = new JTextField(getStringProperty("hsMore"));
      hsBottom = new JTextField(getStringProperty("hsBottom"));

      hsap.add(moreLabel);
      hsap.add(hsMore);
      hsap.add(bottomLabel);
      hsap.add(hsBottom);

      hotspot.add(hsp);
      hotspot.add(hsap);

      // define Key Pad panel
      final JPanel kp = new JPanel();
      kp.setLayout(new BoxLayout(kp,BoxLayout.Y_AXIS));

      // define kpPanel panel
      JPanel kpp = new JPanel();
      kpp.setBorder(BorderFactory.createTitledBorder(LangTool.getString("sa.kpp")));
      kpCheck = new JCheckBox(LangTool.getString("sa.kpCheck"));

      if (getStringProperty("keypad").equals("Yes"))
         kpCheck.setSelected(true);

      kpp.add(kpCheck);

      kp.add(kpp);

      // define default
      final JPanel jp = new JPanel();
      jp.add(cpp,BorderLayout.CENTER);
      jp.setPreferredSize(cpp.getPreferredSize());

      //Create the nodes.
      DefaultMutableTreeNode top = new DefaultMutableTreeNode(fileName);
      createNodes(top);

      //Create a tree that allows one selection at a time.
      final JTree tree = new JTree(top);
      tree.getSelectionModel().setSelectionMode
             (TreeSelectionModel.SINGLE_TREE_SELECTION);

      //Listen for when the selection changes.
      tree.addTreeSelectionListener(new TreeSelectionListener() {
         public void valueChanged(TreeSelectionEvent e) {

            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                tree.getLastSelectedPathComponent();

            if (node == null)
               return;

            Object nodeInfo = node.getUserObject();

            if (nodeInfo.toString().equals(LangTool.getString("sa.nodeFonts"))) {
               jp.removeAll();
               jp.add(fp,BorderLayout.NORTH);
               jp.setPreferredSize(cpp.getPreferredSize());
               jp.validate();
               jpm.repaint();
            }

            if (nodeInfo.toString().equals(LangTool.getString("sa.nodeDisplay"))) {
               jp.removeAll();
               jp.add(display,BorderLayout.NORTH);
               jp.setPreferredSize(cpp.getPreferredSize());
               jp.validate();
               jpm.repaint();
            }

            if (nodeInfo.toString().equals(LangTool.getString("sa.nodeColors"))) {
               jp.removeAll();
               jp.add(cpp,BorderLayout.CENTER);
               jp.setPreferredSize(cpp.getPreferredSize());
               jp.validate();
               jpm.repaint();
            }
            if (nodeInfo.toString().equals(LangTool.getString("sa.nodeHS"))) {
               jp.removeAll();
               jp.add(hotspot,BorderLayout.CENTER);
               jp.setPreferredSize(cpp.getPreferredSize());
               jp.validate();
               jpm.repaint();
            }
            if (nodeInfo.toString().equals(LangTool.getString("sa.nodeKP"))) {
               jp.removeAll();
               jp.add(kp,BorderLayout.CENTER);
               jp.setPreferredSize(cpp.getPreferredSize());
               jp.validate();
               jpm.repaint();
            }

         }
      });


      // define tree selection panel
      JPanel jsp = new JPanel();
      jsp.setBackground(Color.white);
      jsp.add(tree);

      jpm.add(jp,BorderLayout.EAST);
      jpm.add(jsp,BorderLayout.WEST);

   }

   private void createNodes(DefaultMutableTreeNode top) {
        DefaultMutableTreeNode attrib = null;

        attrib = new DefaultMutableTreeNode(LangTool.getString("sa.nodeColors"));
        top.add(attrib);

        attrib = new DefaultMutableTreeNode(LangTool.getString("sa.nodeDisplay"));
        top.add(attrib);

        attrib = new DefaultMutableTreeNode(LangTool.getString("sa.nodeFonts"));
        top.add(attrib);

        attrib = new DefaultMutableTreeNode(LangTool.getString("sa.nodeHS"));
        top.add(attrib);

        attrib = new DefaultMutableTreeNode(LangTool.getString("sa.nodeKP"));
        top.add(attrib);

        attrib = new DefaultMutableTreeNode(LangTool.getString("sa.nodei18n"));
        top.add(attrib);

    }

   protected final String getStringProperty(String prop) {

      if (props.containsKey(prop))
         return (String)props.get(prop);
      else
         return "";

   }

   protected final int getIntProperty(String prop) {

      return Integer.parseInt((String)props.get(prop));

   }

   protected final Color getColorProperty(String prop) {

      if (props.containsKey(prop)) {
         Color c = new Color(getIntProperty(prop));
         return c;
      }
      else
         return null;

   }

   protected final Color getColorProperty(String prop, Color defColor) {

      if (props.containsKey(prop)) {
         Color c = new Color(getIntProperty(prop));
         return c;
      }
      else
         return defColor;

   }

   protected final void setProperty(String key, String val) {

      props.setProperty(key,val);

   }

   public Properties getProperties() {

      return props;
   }

   public void addPropertyChangeListener(PropertyChangeListener l) {

      changes.addPropertyChangeListener(l);
   }

   public void removePropertyChangeListener(PropertyChangeListener l) {

      changes.removePropertyChangeListener(l);
   }

   public void showIt() {

      Object[]      message = new Object[1];
      message[0] = jpm;
      String[] options = {LangTool.getString("sa.optApply"),
                           LangTool.getString("sa.optCancel"),
                           LangTool.getString("sa.optSave")
                          };

      final JOptionPane saOptionPane = new JOptionPane(
             message,                           // the dialog message array
             JOptionPane.PLAIN_MESSAGE,   // message type
             JOptionPane.YES_NO_CANCEL_OPTION,        // option type
             null,                              // optional icon, use null to use the default icon
             options,                           // options string array, will be made into buttons//
             options[1]                         // option that should be made into a default button
         );

      setTitle(LangTool.getString("sa.title"));
      setModal(true);
      setContentPane(saOptionPane);

      addWindowListener(new WindowAdapter() {
           public void windowClosing(WindowEvent we) {

           }
       });

       saOptionPane.addPropertyChangeListener(
           new PropertyChangeListener() {
               public void propertyChange(PropertyChangeEvent e) {
                   String prop = e.getPropertyName();

//                     System.out.println("prop > " + prop);

                   if (isVisible()
                    && (e.getSource() == saOptionPane)
                    && (prop.equals(JOptionPane.VALUE_PROPERTY) ||
                        prop.equals(JOptionPane.INPUT_VALUE_PROPERTY)))
                   {

                     doOptionStuff(saOptionPane);

                   }
               }
           });

      pack();

      //Center the dialog
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension dialogSize = getSize();
      if (dialogSize.height > screenSize.height)
         dialogSize.height = screenSize.height;
      if (dialogSize.width > screenSize.width)
         dialogSize.width = screenSize.width;
      setLocation((screenSize.width - dialogSize.width) / 2,
                     (screenSize.height - dialogSize.height) / 2);

      setVisible(true);

   }

   private void doOptionStuff(JOptionPane optionPane) {

      String result = (String)optionPane.getValue();
//      System.out.println("result > " + result);

      if (LangTool.getString("sa.optApply").equals(result)) {

         applyAttributes();
         optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

      }
      if (LangTool.getString("sa.optCancel").equals(result)) {
         setVisible(false);
         dispose();
      }
      if (LangTool.getString("sa.optSave").equals(result)) {

         if (props.containsKey("saveme")){
            props.remove("saveme");
         }
         saveProps();
         setVisible(false);
         dispose();
      }
   }

   private void applyAttributes() {

      if (!getStringProperty("font").equals(
               (String)fontsList.getSelectedItem())
         ) {
         changes.firePropertyChange("font",
                           getStringProperty("font"),
                           (String)fontsList.getSelectedItem());

         setProperty("font",(String)fontsList.getSelectedItem());
      }

      String newSelection = (String)colorList.getSelectedItem();

      if (colorSchema != null) {

         if (!getColorProperty("colorBg").equals(colorSchema.getColorBg())) {
            changes.firePropertyChange("colorBg",
                           getColorProperty("colorBg"),
                           colorSchema.getColorBg());

            setProperty("colorBg",Integer.toString(colorSchema.getColorBg().getRGB()));

         }
         if (!getColorProperty("colorBlue").equals(colorSchema.getColorBlue())) {
            changes.firePropertyChange("colorBlue",
                           getColorProperty("colorBlue"),
                           colorSchema.getColorBlue());
            setProperty("colorBlue",Integer.toString(colorSchema.getColorBlue().getRGB()));

         }
         if (!getColorProperty("colorRed").equals(colorSchema.getColorRed())) {
            changes.firePropertyChange("colorRed",
                           getColorProperty("colorRed"),
                           colorSchema.getColorRed());
            setProperty("colorRed",Integer.toString(colorSchema.getColorRed().getRGB()));

         }
         if (!getColorProperty("colorPink").equals(colorSchema.getColorPink())) {
            changes.firePropertyChange("colorPink",
                           getColorProperty("colorPink"),
                           colorSchema.getColorPink());
            setProperty("colorPink",Integer.toString(colorSchema.getColorPink().getRGB()));

         }
         if (!getColorProperty("colorGreen").equals(colorSchema.getColorGreen())) {
            changes.firePropertyChange("colorGreen",
                           getColorProperty("colorGreen"),
                           colorSchema.getColorGreen());

            setProperty("colorGreen",Integer.toString(colorSchema.getColorGreen().getRGB()));

         }
         if (!getColorProperty("colorTurq").equals(colorSchema.getColorTurq())) {
            changes.firePropertyChange("colorTurq",
                           getColorProperty("colorTurq"),
                           colorSchema.getColorTurq());

            setProperty("colorTurq",Integer.toString(colorSchema.getColorTurq().getRGB()));

         }

         if (!getColorProperty("colorYellow").equals(colorSchema.getColorYellow())) {
            changes.firePropertyChange("colorYellow",
                           getColorProperty("colorYellow"),
                           colorSchema.getColorYellow());
            setProperty("colorYellow",Integer.toString(colorSchema.getColorYellow().getRGB()));

         }
         if (!getColorProperty("colorWhite").equals(colorSchema.getColorWhite())) {
            changes.firePropertyChange("colorWhite",
                           getColorProperty("colorWhite"),
                           colorSchema.getColorWhite());

            setProperty("colorWhite",Integer.toString(colorSchema.getColorWhite().getRGB()));

         }
         if (!getColorProperty("colorGUIField").equals(colorSchema.getColorGuiField())) {
            changes.firePropertyChange("colorGUIField",
                           getColorProperty("colorGUIField"),
                           colorSchema.getColorGuiField());

            setProperty("colorGUIField",Integer.toString(colorSchema.getColorGuiField().getRGB()));

         }
         if (!getColorProperty("colorCursor").equals(colorSchema.getColorCursor())) {
            changes.firePropertyChange("colorCursor",
                           getColorProperty("colorCursor"),
                           colorSchema.getColorCursor());

            setProperty("colorCursor",Integer.toString(colorSchema.getColorCursor().getRGB()));

         }

         if (!getColorProperty("colorSep").equals(colorSchema.getColorSeparator())) {
            changes.firePropertyChange("colorSep",
                           getColorProperty("colorSep"),
                           colorSchema.getColorSeparator());

            setProperty("colorSep",
                     Integer.toString(colorSchema.getColorSeparator().getRGB()));

         }

         if (!getColorProperty("colorHexAttr").equals(colorSchema.getColorHexAttr())) {
            changes.firePropertyChange("colorHexAttr",
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
               changes.firePropertyChange("colorBg",
                              getColorProperty("colorBg"),
                              nc);

               setProperty("colorBg",Integer.toString(nc.getRGB()));

            }
         }
         if (newSelection.equals(LangTool.getString("sa.blue"))) {
            if (!getColorProperty("colorBlue").equals(nc)) {
               changes.firePropertyChange("colorBlue",
                              getColorProperty("colorBlue"),
                              nc);
               setProperty("colorBlue",Integer.toString(nc.getRGB()));

            }
         }
         if (newSelection.equals(LangTool.getString("sa.red"))) {
            if (!getColorProperty("colorRed").equals(nc)) {
               changes.firePropertyChange("colorRed",
                              getColorProperty("colorRed"),
                              nc);
               setProperty("colorRed",Integer.toString(nc.getRGB()));

            }
         }
         if (newSelection.equals(LangTool.getString("sa.pink"))) {
            if (!getColorProperty("colorPink").equals(nc)) {
               changes.firePropertyChange("colorPink",
                              getColorProperty("colorPink"),
                              nc);
               setProperty("colorPink",Integer.toString(nc.getRGB()));

            }
         }
         if (newSelection.equals(LangTool.getString("sa.green"))) {
            if (!getColorProperty("colorGreen").equals(nc)) {
               changes.firePropertyChange("colorGreen",
                              getColorProperty("colorGreen"),
                              nc);

               setProperty("colorGreen",Integer.toString(nc.getRGB()));

            }
         }
         if (newSelection.equals(LangTool.getString("sa.turq"))) {
            if (!getColorProperty("colorTurq").equals(nc)) {
               changes.firePropertyChange("colorTurq",
                              getColorProperty("colorTurq"),
                              nc);

               setProperty("colorTurq",Integer.toString(nc.getRGB()));

            }

         }
         if (newSelection.equals(LangTool.getString("sa.yellow"))) {
            if (!getColorProperty("colorYellow").equals(nc)) {
               changes.firePropertyChange("colorYellow",
                              getColorProperty("colorYellow"),
                              nc);
               setProperty("colorYellow",Integer.toString(nc.getRGB()));

            }
         }
         if (newSelection.equals(LangTool.getString("sa.white"))) {
            if (!getColorProperty("colorWhite").equals(nc)) {
               changes.firePropertyChange("colorWhite",
                              getColorProperty("colorWhite"),
                              nc);

               setProperty("colorWhite",Integer.toString(nc.getRGB()));

            }
         }

         if (newSelection.equals(LangTool.getString("sa.guiField"))) {
            if (!getColorProperty("colorGUIField").equals(nc)) {
               changes.firePropertyChange("colorGUIField",
                              getColorProperty("colorGUIField"),
                              nc);

               setProperty("colorGUIField",Integer.toString(nc.getRGB()));

            }
         }

         if (newSelection.equals(LangTool.getString("sa.cursorColor"))) {
            if (!getColorProperty("colorCursor").equals(nc)) {
               changes.firePropertyChange("colorCursor",
                              getColorProperty("colorCursor"),
                              nc);

               setProperty("colorCursor",Integer.toString(nc.getRGB()));

            }
         }
         if (newSelection.equals(LangTool.getString("sa.columnSep"))) {
            if (!getColorProperty("colorSep").equals(nc)) {
               changes.firePropertyChange("colorSep",
                              getColorProperty("colorSep"),
                              nc);

               setProperty("colorSep",Integer.toString(nc.getRGB()));

            }
         }
         if (newSelection.equals(LangTool.getString("sa.cursorColor"))) {
            if (!getColorProperty("colorCursor").equals(nc)) {
               changes.firePropertyChange("colorCursor",
                              getColorProperty("colorCursor"),
                              nc);

               setProperty("colorCursor",Integer.toString(nc.getRGB()));

            }
         }

         if (newSelection.equals(LangTool.getString("sa.hexAttrColor"))) {
            if (!getColorProperty("colorHexAttr").equals(nc)) {
               changes.firePropertyChange("colorHexAttr",
                              getColorProperty("colorHexAttr"),
                              nc);

               setProperty("colorHexAttr",Integer.toString(nc.getRGB()));

            }
         }

      }

      if (csLine.isSelected()) {
         changes.firePropertyChange("colSeparator",
                           getStringProperty("colSeparator"),
                           "Line");
         setProperty("colSeparator","Line");
      }
      else {
         changes.firePropertyChange("colSeparator",
                           getStringProperty("colSeparator"),
                           "Dot");
         setProperty("colSeparator","Dot");

      }

      if (csLine.isSelected()) {
         changes.firePropertyChange("colSeparator",
                           getStringProperty("colSeparator"),
                           "Line");
         setProperty("colSeparator","Line");
      }
      else {
         changes.firePropertyChange("colSeparator",
                           getStringProperty("colSeparator"),
                           "Dot");
         setProperty("colSeparator","Dot");

      }

      if (cFull.isSelected()) {
         changes.firePropertyChange("cursorSize",
                           getStringProperty("cursorSize"),
                           "Full");
         setProperty("cursorSize","Full");

      }
      if (cHalf.isSelected()) {
         changes.firePropertyChange("cursorSize",
                           getStringProperty("cursorSize"),
                           "Half");
         setProperty("cursorSize","Half");
      }
      if (cLine.isSelected()) {
         changes.firePropertyChange("cursorSize",
                           getStringProperty("cursorSize"),
                           "Line");

         setProperty("cursorSize","Line");
      }

      if (chNone.isSelected()) {
         changes.firePropertyChange("crossHair",
                           getStringProperty("crossHair"),
                           "None");
         setProperty("crossHair","None");

      }

      if (chHorz.isSelected()) {
         changes.firePropertyChange("crossHair",
                           getStringProperty("crossHair"),
                           "Horz");
         setProperty("crossHair","Horz");

      }

      if (chVert.isSelected()) {
         changes.firePropertyChange("crossHair",
                           getStringProperty("crossHair"),
                           "Vert");
         setProperty("crossHair","Vert");

      }

      if (chCross.isSelected()) {
         changes.firePropertyChange("crossHair",
                           getStringProperty("crossHair"),
                           "Both");
         setProperty("crossHair","Both");

      }


      if (saNormal.isSelected()) {
         changes.firePropertyChange("showAttr",
                           getStringProperty("showAttr"),
                           "Normal");
         setProperty("showAttr","Normal");
      }
      else {
         changes.firePropertyChange("showAttr",
                           getStringProperty("showAttr"),
                           "Hex");
         setProperty("showAttr","Hex");

      }

      if (dceCheck.isSelected()) {
         changes.firePropertyChange("doubleClick",
                           getStringProperty("doubleClick"),
                           "Yes");
         setProperty("doubleClick","Yes");
      }
      else {
         changes.firePropertyChange("doubleClick",
                           getStringProperty("doubleClick"),
                           "No");
         setProperty("doubleClick","No");
      }

      if (guiCheck.isSelected()) {
         changes.firePropertyChange("guiInterface",
                           getStringProperty("guiInterface"),
                           "Yes");
         setProperty("guiInterface","Yes");
      }
      else {
         changes.firePropertyChange("guiInterface",
                           getStringProperty("guiInterface"),
                           "No");
         setProperty("guiInterface","No");
      }

      if (guiShowUnderline.isSelected()) {
         changes.firePropertyChange("guiShowUnderline",
                           getStringProperty("guiShowUnderline"),
                           "Yes");
         setProperty("guiShowUnderline","Yes");
      }
      else {
         changes.firePropertyChange("guiShowUnderline",
                           getStringProperty("guiShowUnderline"),
                           "No");
         setProperty("guiShowUnderline","No");
      }

      if (hsCheck.isSelected()) {
         changes.firePropertyChange("hotspots",
                           getStringProperty("hotspots"),
                           "Yes");
         setProperty("hotspots","Yes");
      }
      else {
         changes.firePropertyChange("hotspots",
                           getStringProperty("hotspots"),
                           "No");
         setProperty("hotspots","No");
      }


      if (kpCheck.isSelected()) {
         changes.firePropertyChange("keypad",
                           getStringProperty("keypad"),
                           "Yes");
         setProperty("keypad","Yes");
      }
      else {
         changes.firePropertyChange("keypad",
                           getStringProperty("keypad"),
                           "No");
         setProperty("keypad","No");
      }

      changes.firePropertyChange("hsMore",
                        getStringProperty("hsMore"),
                        hsMore.getText());
      setProperty("hsMore",hsMore.getText());

      changes.firePropertyChange("hsBottom",
                        getStringProperty("hsBottom"),
                        hsBottom.getText());
      setProperty("hsBottom",hsBottom.getText());

      changes.firePropertyChange("fontScaleHeight",
                        getStringProperty("fontScaleHeight"),
                        verticalScale.getText());
      setProperty("fontScaleHeight",verticalScale.getText());

      changes.firePropertyChange("fontScaleWidth",
                        getStringProperty("fontScaleWidth"),
                        horizontalScale.getText());
      setProperty("fontScaleWidth",horizontalScale.getText());

      changes.firePropertyChange("fontPointSize",
                        getStringProperty("fontPointSize"),
                        pointSize.getText());
      setProperty("fontPointSize",pointSize.getText());

      setProperty("saveme","yes");

   }

   private void saveProps() {

      try {
         FileOutputStream out = new FileOutputStream(fileName);
         props.store(out,"------ Defaults --------");

      }
      catch (FileNotFoundException fnfe) {}
      catch (IOException ioe) {}


   }

   private void loadSchemas(JComboBox schemas) {

      schemaProps = new Properties();
      URL file=null;

      try {
         ClassLoader cl = this.getClass().getClassLoader();
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

      System.out.println(" loaded schemas " + numSchemas);
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