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
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import org.tn5250j.tools.*;

public class Connect extends JDialog implements ActionListener,
                                                   TN5250jConstants {

   // panels to be displayed
   JPanel configOptions = new JPanel();
   JPanel sessionPanel = new JPanel();
   JPanel options = new JPanel();
   JPanel sessionOpts = new JPanel();
   JPanel sessionOptPanel = new JPanel();
   JTable sessions = null;

   // button needing global access
   JButton editButton = null;
   JButton removeButton = null;
   JButton connectButton = null;

   // custom table model
   ConfigureTableModel ctm = null;

   // The scroll pane that holds the table.
   JScrollPane scrollPane;

   // ListSelectionModel of our custom table.
   ListSelectionModel rowSM = null;

   // Properties
   Properties props = null;

   // property input structures
   JTextField systemName = null;
   JTextField systemId = null;
   JTextField port = null;
   JTextField deviceName = null;
   JTextField  fpn = null;
   JComboBox  cpb = null;
   JCheckBox ec = null;
   JCheckBox tc = null;
   JRadioButton sdNormal = null;

   //  Selection value for connection
   String connectKey = null;

   public Connect(Frame frame, String title, Properties prop) {
      super(frame, title, true);
      props = prop;
      try {
         jbInit();
      }
      catch(Exception ex) {
         ex.printStackTrace();
      }
   }

   void jbInit() throws Exception {

      // make it non resizable
      setResizable(false);
      this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

      // create some reusable borders and layouts
      Border etchedBorder = BorderFactory.createEtchedBorder();
      BorderLayout borderLayout = new BorderLayout();

      // get an instance of our table model
      ctm = new ConfigureTableModel();

      // create a table using our custom table model
      sessions = new JTable(ctm);
      sessions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      sessions.setPreferredScrollableViewportSize(new Dimension(500,200));
      sessions.setShowGrid(false);

      //Create the scroll pane and add the table to it.
      scrollPane = new JScrollPane(sessions);
      scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      //Setup our selection model listener
      rowSM = sessions.getSelectionModel();
          rowSM.addListSelectionListener(new ListSelectionListener() {
              public void valueChanged(ListSelectionEvent e) {

                  //Ignore extra messages.
                  if (e.getValueIsAdjusting())
                     return;

                  ListSelectionModel lsm =
                      (ListSelectionModel)e.getSource();

                  if (lsm.isSelectionEmpty()) {
                      //no rows are selected
                     editButton.setEnabled(false);
                     removeButton.setEnabled(false);
                     connectButton.setEnabled(false);
                  } else {

                     int selectedRow = lsm.getMinSelectionIndex();
                      //selectedRow is selected
                     editButton.setEnabled(true);
                     removeButton.setEnabled(true);
                     connectButton.setEnabled(true);
                  }
              }
          });

      //Setup panels
      configOptions.setLayout(borderLayout);

      sessionPanel.setLayout(borderLayout);

      configOptions.add(sessionPanel, BorderLayout.CENTER);

      sessionOpts.add(scrollPane, BorderLayout.CENTER);

      sessionPanel.add(sessionOpts, BorderLayout.NORTH);
      sessionPanel.add(sessionOptPanel, BorderLayout.SOUTH);
      sessionPanel.setBorder(BorderFactory.createRaisedBevelBorder());

      // add the option buttons
      addOptButton(LangTool.getString("ss.optAdd"),"ADD",sessionOptPanel);

      removeButton = addOptButton(LangTool.getString("ss.optDelete"),
                                    "REMOVE",
                                    sessionOptPanel,
                                    false);

      editButton = addOptButton(LangTool.getString("ss.optEdit"),
                                    "EDIT",
                                    sessionOptPanel,
                                    false);

      connectButton = addOptButton(LangTool.getString("ss.optConnect"),"CONNECT",options,false);

      addOptButton(LangTool.getString("ss.optCancel"),"DONE",options);


      // add the panels to our dialog
      getContentPane().add(sessionPanel,BorderLayout.CENTER);
      getContentPane().add(options, BorderLayout.SOUTH);

      // pack it and center it on the screen
      pack();

      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = getSize();
      if (frameSize.height > screenSize.height)
         frameSize.height = screenSize.height;
      if (frameSize.width > screenSize.width)
         frameSize.width = screenSize.width;
      setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

      // set default selection value as the first row
      if (sessions.getRowCount() > 0) {

         sessions.getSelectionModel().setSelectionInterval(0,0);
      }

      // now show the world what we and they can do
      show();

   }

   private JButton addOptButton(String text, String ac, Container container) {

      return addOptButton(text,ac,container,true);
   }

   private JButton addOptButton(String text,
                              String ac,
                              Container container,
                              boolean enabled) {

      JButton button = new JButton(text);
      button.setEnabled(enabled);
      button.setActionCommand(ac);

      // we check if there was mnemonic specified and if there was then we
      //    set it.
      int mnemIdx = text.indexOf("&");
      if (mnemIdx >= 0) {
         StringBuffer sb = new StringBuffer(text);
         sb.deleteCharAt(mnemIdx);
         button.setText(sb.toString());
         button.setMnemonic(text.charAt(mnemIdx+1));
      }
      button.addActionListener(this);
      button.setAlignmentX(Component.CENTER_ALIGNMENT);
      container.add(button);

      return button;
   }

   // Process out button actions
   public void actionPerformed(ActionEvent e) {

      if (e.getActionCommand().equals("DONE")) {
         saveProps();
         setVisible(false);
      }

      if (e.getActionCommand().equals("ADD")) {
         Configure.doEntry((JFrame)getParent(),null,props);
         ctm.addSession();
      }
      if (e.getActionCommand().equals("REMOVE")) {
         removeEntry();
         editButton.setEnabled(false);
         removeButton.setEnabled(false);
      }

      if (e.getActionCommand().equals("EDIT")) {
         int selectedRow = rowSM.getMinSelectionIndex();
         Configure.doEntry((JFrame)getParent(),(String)ctm.getValueAt(selectedRow,0),props);
         ctm.chgSession(selectedRow);
      }

      if (e.getActionCommand().equals("CONNECT")) {
         int selectedRow = rowSM.getMinSelectionIndex();
         connectKey = (String)ctm.getValueAt(selectedRow,0);
         saveProps();
         setVisible(false);
      }
   }

   public String getConnectKey() {

      return connectKey;
   }
      private void saveProps() {

      try {
         FileOutputStream out = new FileOutputStream("sessions");
            // save off the width and height to be restored later
         props.store(out,"------ Session Information --------");
      }
      catch (FileNotFoundException fnfe) {}
      catch (IOException ioe) {}


   }

   private void addLabelComponent(String text,Component comp,Container container) {

      JLabel label = new JLabel(text);
      label.setAlignmentX(Component.LEFT_ALIGNMENT);
      label.setHorizontalTextPosition(JLabel.LEFT);
      container.add(label);
      container.add(comp);

   }

   private void removeEntry() {
      int selectedRow = rowSM.getMinSelectionIndex();
      props.remove(ctm.getValueAt(selectedRow,0));
      ctm.removeSession(selectedRow);
   }

   class ConfigureTableModel extends AbstractTableModel {

      final String[] cols = {
                  LangTool.getString("conf.tableColA"),
                  LangTool.getString("conf.tableColB"),
                  LangTool.getString("conf.tableColC")};

      public ConfigureTableModel() {
         super();

      }

      public int getColumnCount() {

        return cols.length;
      }

      public String getColumnName(int col) {
        return cols[col];
      }

      public int getRowCount() {
         Enumeration e = props.keys();
         int x = 0;
         String ses = null;
         while (e.hasMoreElements()) {
            ses = (String)e.nextElement();

            if (!ses.startsWith("emul.")) {
               x++;
            }
         }

        return x;
      }

      /*
       * Implement this so that the default session can be selected.
       *
       */
      public void setValueAt(Object value, int row, int col) {

           boolean which = ((Boolean)value).booleanValue();
           if (which)
              props.setProperty("emul.default",getPropValue(row,null));
           else
              props.setProperty("emul.default","");

      }

      public Object getValueAt(int row, int col) {

        if (col == 0)
          return getPropValue(row,null);
        if (col == 1)
          return getPropValue(row,"0");
        if (col == 2) {
          if (getPropValue(row,null).equals(props.getProperty("emul.default","")))
             return new Boolean(true);
          else
             return new Boolean(false);
        }
        return null;

      }

      /*
       * We need to implement this so that the default session column can
       *    be updated.
       */
      public boolean isCellEditable(int row, int col) {
         //Note that the data/cell address is constant,
         //no matter where the cell appears onscreen.
         if (col == 2) {
             return true;
         } else {
             return false;
         }
      }

      /*
       * JTable uses this method to determine the default renderer/
       * editor for each cell.  If we didn't implement this method,
       * then the default column would contain text ("true"/"false"),
       * rather than a check box.
       */
      public Class getColumnClass(int c) {
         return getValueAt(0, c).getClass();
      }

      private String getPropValue(int row,String param) {

         Enumeration e = props.keys();
         int x = 0;
         String prop = "";
         String[] args = new String[NUM_PARMS];
         String ses = null;

         while (e.hasMoreElements() && x <=row) {
            ses = (String)e.nextElement();

            if (!ses.startsWith("emul.")) {
               prop = ses;
               x++;
            }
         }
         if (param == null)
          return prop;
         else {
          Configure.parseArgs(props.getProperty(prop),args);
          if (param.equals("0"))
            return args[0];
         }
         return null;
      }

      public void addSession() {
         fireTableRowsInserted(props.size()-1,props.size()-1);
      }

      public void chgSession(int row) {
         fireTableRowsUpdated(row,row);
      }

      public void removeSession(int row) {
         fireTableRowsDeleted(row,row);
      }

   }
}
