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
import java.net.*;
import java.text.*;

import org.tn5250j.tools.*;
import org.tn5250j.settings.*;

public class SessionAttributes extends JDialog {

   String fileName;
   Properties props = null;
   JPanel jpm = new JPanel(new BorderLayout());

   JCheckBox hsCheck;
   JCheckBox kpCheck;
   JCheckBox dceCheck;
   JTextField connectMacro;
   JTextField hsMore;
   JTextField hsBottom;
   JCheckBox defaultPrinter;

   ColorAttributesPanel cpp;
   FontAttributesPanel fp;
   DisplayAttributesPanel display;
   CursorAttributesPanel cuPanel;
   SignoffAttributesPanel signoff;

   private SessionConfig changes = null;
   JTree tree = new JTree();

   public SessionAttributes(Frame parent, SessionConfig config ) {
      super(parent);

      this.fileName = config.getConfigurationResource();
      this.props = config.getProperties();
      changes = config;

      try {
         jbInit();
      }
      catch(Exception e) {
         e.printStackTrace();
      }
   }

   /**Component initialization*/
   private void jbInit() throws Exception  {

      Dimension ps = null;

      // define font panel
      fp = new FontAttributesPanel(changes);
      // define colors panel
      cpp = new ColorAttributesPanel(changes);
      // define display panel
      display = new DisplayAttributesPanel(changes);
      // define cursor panel
      cuPanel = new CursorAttributesPanel(changes);
      // define signoff panel
      signoff = new SignoffAttributesPanel(changes);

      // define onConnect panel
      final JPanel onConnect = new JPanel();
      onConnect.setLayout(new BoxLayout(onConnect,BoxLayout.Y_AXIS));

      // define onConnect macro to run
      JPanel ocMacrop = new JPanel();
      ocMacrop.setBorder(BorderFactory.createTitledBorder(LangTool.getString("sa.connectMacro")));

      connectMacro = new JTextField();
      connectMacro.setColumns(30);

      // sets the connect macro
      connectMacro.setText(getStringProperty("connectMacro"));

      ocMacrop.add(connectMacro);
      onConnect.add(ocMacrop);

      // define mouse panel
      final JPanel mouse = new JPanel();
      mouse.setLayout(new BoxLayout(mouse,BoxLayout.Y_AXIS));

      // define double click as enter
      JPanel dcep = new JPanel();
      dcep.setBorder(BorderFactory.createTitledBorder(LangTool.getString("sa.doubleClick")));

      dceCheck = new JCheckBox(LangTool.getString("sa.sendEnter"));

      // check if double click sends enter
      dceCheck.setSelected(getStringProperty("doubleClick").equals("Yes"));

      dcep.add(dceCheck);
      mouse.add(dcep);

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

      // define Printer panel
      final JPanel pp = new JPanel();
      pp.setLayout(new BoxLayout(pp,BoxLayout.Y_AXIS));

      // define ppPanel panel
      JPanel ppp = new JPanel();
      ppp.setBorder(BorderFactory.createTitledBorder(LangTool.getString("sa.print")));
      defaultPrinter = new JCheckBox(LangTool.getString("sa.defaultPrinter"));

      if (getStringProperty("defaultPrinter").equals("Yes"))
         defaultPrinter.setSelected(true);

      ppp.add(defaultPrinter);

      pp.add(ppp);

      // define default
      final JPanel jp = new JPanel();

      jp.add(cpp,BorderLayout.CENTER);
      jp.setPreferredSize(cpp.getPreferredSize());

      //Create the nodes.
      DefaultMutableTreeNode top = new DefaultMutableTreeNode(fileName);
      createNodes(top);

      //Create a tree that allows one selection at a time.
      tree = new JTree(top);

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

            if (nodeInfo.toString().equals(LangTool.getString("sa.nodeColors"))) {
               jp.removeAll();
               jp.add(cpp,BorderLayout.CENTER);
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

            if (nodeInfo.toString().equals(LangTool.getString("sa.nodeCursor"))) {
               jp.removeAll();
               jp.add(cuPanel,BorderLayout.CENTER);
               jp.setPreferredSize(cpp.getPreferredSize());
               jp.validate();
               jpm.repaint();
            }

            if (nodeInfo.toString().equals(LangTool.getString("sa.nodeSignoff"))) {
               jp.removeAll();
               jp.add(signoff,BorderLayout.CENTER);
               jp.setPreferredSize(cpp.getPreferredSize());
               jp.validate();
               jpm.repaint();
            }

            if (nodeInfo.toString().equals(LangTool.getString("sa.nodeOnConnect"))) {
               jp.removeAll();
               jp.add(onConnect,BorderLayout.CENTER);
               jp.setPreferredSize(cpp.getPreferredSize());
               jp.validate();
               jpm.repaint();
            }

            if (nodeInfo.toString().equals(LangTool.getString("sa.nodeMouse"))) {
               jp.removeAll();
               jp.add(mouse,BorderLayout.CENTER);
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
            if (nodeInfo.toString().equals(LangTool.getString("sa.nodePrinter"))) {
               jp.removeAll();
               jp.add(pp,BorderLayout.CENTER);
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
//        attrib = new DefaultMutableTreeNode(cpp);
        top.add(attrib);

        attrib = new DefaultMutableTreeNode(LangTool.getString("sa.nodeDisplay"));
        top.add(attrib);

        attrib = new DefaultMutableTreeNode(LangTool.getString("sa.nodeCursor"));
        top.add(attrib);

        attrib = new DefaultMutableTreeNode(LangTool.getString("sa.nodeFonts"));
        top.add(attrib);

        attrib = new DefaultMutableTreeNode(LangTool.getString("sa.nodeSignoff"));
        top.add(attrib);

        attrib = new DefaultMutableTreeNode(LangTool.getString("sa.nodeOnConnect"));
        top.add(attrib);

        attrib = new DefaultMutableTreeNode(LangTool.getString("sa.nodeMouse"));
        top.add(attrib);

        attrib = new DefaultMutableTreeNode(LangTool.getString("sa.nodeHS"));
        top.add(attrib);

        attrib = new DefaultMutableTreeNode(LangTool.getString("sa.nodeKP"));
        top.add(attrib);

        attrib = new DefaultMutableTreeNode(LangTool.getString("sa.nodePrinter"));
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

   protected final String getStringProperty(String prop,String defaultValue) {

      if (props.containsKey(prop)) {
         String p = (String)props.get(prop);
         if (p.length() > 0)
            return p;
         else
            return defaultValue;
      }
      else
         return defaultValue;

   }

   protected final int getIntProperty(String prop) {

      return Integer.parseInt((String)props.get(prop));

   }

   protected final void setProperty(String key, String val) {

      props.setProperty(key,val);

   }

   public Properties getProperties() {

      return props;
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

//      DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
//      Enumeration e = root.children();
//      while (e.hasMoreElements())
//         System.out.println(e.nextElement());
//      (DefaultMutableTreeNode)tree.getModel().getRoot()
//         for (int x = 0; x < compa.length; x ++) {
//
//            if (compa[x] instanceof org.tn5250j.settings.AttributesPanel) {
//               System.out.println(compa.length + " " + x);
//
//   //            ((AttributesPanel)compa[x])
//            }
//         }


      // apply the font attributes
      fp.applyAttributes();
      // apply the color attributes
      cpp.applyAttributes();
      // apply the display attributes
      display.applyAttributes();
      // apply the cursor attributes
      cuPanel.applyAttributes();
      // apply the signoff attributes
      signoff.applyAttributes();

      if (dceCheck.isSelected()) {
         changes.firePropertyChange(this,"doubleClick",
                           getStringProperty("doubleClick"),
                           "Yes");
         setProperty("doubleClick","Yes");
      }
      else {
         changes.firePropertyChange(this,"doubleClick",
                           getStringProperty("doubleClick"),
                           "No");
         setProperty("doubleClick","No");
      }

      if (hsCheck.isSelected()) {
         changes.firePropertyChange(this,"hotspots",
                           getStringProperty("hotspots"),
                           "Yes");
         setProperty("hotspots","Yes");
      }
      else {
         changes.firePropertyChange(this,"hotspots",
                           getStringProperty("hotspots"),
                           "No");
         setProperty("hotspots","No");
      }


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

      changes.firePropertyChange(this,"hsMore",
                        getStringProperty("hsMore"),
                        hsMore.getText());
      setProperty("hsMore",hsMore.getText());

      changes.firePropertyChange(this,"connectMacro",
                        getStringProperty("connectMacro"),
                        connectMacro.getText());
      setProperty("connectMacro",connectMacro.getText());

      changes.firePropertyChange(this,"hsBottom",
                        getStringProperty("hsBottom"),
                        hsBottom.getText());
      setProperty("hsBottom",hsBottom.getText());

      if (defaultPrinter.isSelected()) {
         changes.firePropertyChange(this,"defaultPrinter",
                           getStringProperty("defaultPrinter"),
                           "Yes");
         setProperty("defaultPrinter","Yes");
      }
      else {
         changes.firePropertyChange(this,"defaultPrinter",
                           getStringProperty("defaultPrinter"),
                           "No");
         setProperty("defaultPrinter","No");
      }


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

}