package org.tn5250j.spoolfile;
/**
 * Title: SpoolExporter.java
 * Copyright:   Copyright (c) 2002
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
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import com.ibm.as400.access.*;
import com.ibm.as400.vaccess.*;
import org.tn5250j.gui.*;
import org.tn5250j.tnvt;
import org.tn5250j.tools.GUIGraphicsUtils;
import org.tn5250j.tools.LangTool;
import org.tn5250j.My5250;

public class SpoolExporter extends JFrame {

   SpoolFilterPane filter;
   // custom table model
   SpoolTableModel stm;
   // The scroll pane that holds the table.
   JScrollPane scrollPane;
   // ListSelectionModel of our custom table.
   ListSelectionModel rowSM;
   JSortTable spools;
   // status line
   JLabel status;
   // List of available spooled files
   SpooledFileList splfList;

   // AS400 connection
   AS400 system;

   // Connection vt
   tnvt vt;

   public SpoolExporter(tnvt vt) {

      this.vt = vt;
      try {
         jbInit();
      }
      catch(Exception e) {
         e.printStackTrace();
      }
   }
   private void jbInit() throws Exception {

      this.setTitle(LangTool.getString("spool.title"));

      this.setIconImage(My5250.tnicon.getImage());

      this.getContentPane().add(createFilterPanel(), BorderLayout.NORTH);

      // get an instance of our table model
      stm = new SpoolTableModel();

      // create a table using our custom table model
      spools = new JSortTable(stm);

      // create our mouse listener on the table
      spools.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseClicked(MouseEvent e) {
            spools_mouseClicked(e);
         }

         public void mousePressed (MouseEvent event) {
            if (event.isPopupTrigger ())
               showPopupMenu(event);
         }

         public void mouseReleased (MouseEvent event) {
            if (event.isPopupTrigger ())
               showPopupMenu(event);
         }

      });

      spools.setShowGrid(false);
      //Create the scroll pane and add the table to it.
      scrollPane = new JScrollPane(spools);
      scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      // setup the number of rows we should be working with
      spools.setPreferredScrollableViewportSize(
         new Dimension(
            spools.getPreferredScrollableViewportSize().width,
            spools.getFontMetrics(spools.getFont()).getHeight() * 8)
      );

      scrollPane.getViewport().setBackground(spools.getBackground());
      scrollPane.setBackground(spools.getBackground());

      //Setup our selection model listener
      rowSM = spools.getSelectionModel();
          rowSM.addListSelectionListener(new ListSelectionListener() {
              public void valueChanged(ListSelectionEvent e) {

                  //Ignore extra messages.
                  if (e.getValueIsAdjusting())
                     return;

                  ListSelectionModel lsm =
                      (ListSelectionModel)e.getSource();

//                  if (lsm.isSelectionEmpty()) {
//                      //no rows are selected
//                     editButton.setEnabled(false);
//                     removeButton.setEnabled(false);
//                     connectButton.setEnabled(false);
//                  } else {
//
//                     int selectedRow = lsm.getMinSelectionIndex();
//                      //selectedRow is selected
//                     editButton.setEnabled(true);
//                     removeButton.setEnabled(true);
//                     connectButton.setEnabled(true);
//                  }
              }
          });

      rowSM.setSelectionMode(rowSM.SINGLE_SELECTION);

      this.getContentPane().add(scrollPane, BorderLayout.CENTER);


      status = new JLabel("0 " + LangTool.getString("spool.count"));
      status.setBorder(BorderFactory.createEtchedBorder());
      this.getContentPane().add(status, BorderLayout.SOUTH);

      pack();

      //Center the window
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = getSize();
      if (frameSize.height > screenSize.height)
         frameSize.height = screenSize.height;
      if (frameSize.width > screenSize.width)
         frameSize.width = screenSize.width;

      setLocation((screenSize.width - frameSize.width) / 2,
                     (screenSize.height - frameSize.height) / 2);

      addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent event) {
            // close the spool file list
            if (splfList != null)
               splfList.close();

            // close the system connection
            if (system != null)
               system.disconnectAllServices();

            setVisible(false);
            dispose();
         }
      });
   }

   private JPanel createFilterPanel() {

      // create filter panel
      JPanel fp = new JPanel();
      fp.setLayout(new BorderLayout());
      fp.setBorder(BorderFactory.createTitledBorder(
                                       LangTool.getString("spool.filterTitle")));

      filter = new SpoolFilterPane();

      // create button selection panel
      JPanel bp = new JPanel();
      JButton load = new JButton(LangTool.getString("spool.load"));
      JButton resetAll = new JButton(LangTool.getString("spool.resetAll"));
      JButton reset = new JButton(LangTool.getString("spool.resetPanel"));

      bp.add(load);
      load.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(ActionEvent e) {
            runLoader();
         }
      });

      bp.add(reset);
      bp.add(resetAll);

      fp.add(filter,BorderLayout.CENTER);
      fp.add(bp,BorderLayout.SOUTH);

      return fp;

   }

   private void runLoader() {
      Runnable loader = new Runnable () {
         public void run() {
            loadSpoolFiles();
         }
      };

      Thread t = new Thread(loader);
      t.setDaemon(true);
      t.start();
   }

   private void loadSpoolFiles() {

      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      try {
         updateStatus(LangTool.getString("spool.working"));

         // get a system object
         if (system == null)
            system = new AS400(vt.getHostName());

         // create a spoolfile list
         splfList = new SpooledFileList(system);

         // set the filters for the spoolfile list
         splfList.setUserFilter(filter.getUser());
         splfList.setQueueFilter("/QSYS.LIB/" + filter.getLibrary() + ".LIB/" +
                                 filter.getQueue() + ".OUTQ");

         // retrieve the output queues
         splfList.openSynchronously();

         // if we have something update the status
         if (splfList != null) {
            final int count = splfList.size();
            updateStatus(count + " " + LangTool.getString("spool.count"));
         }

         // set the spool list to be displayed
         stm.setSpoolList(splfList);

      }
      catch(Exception erp) {
         updateStatus(erp.getMessage(),true);
      }

      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
   }

   private void showPopupMenu(MouseEvent me) {

      JPopupMenu jpm = new JPopupMenu();
      JMenuItem menuItem;
      Action action;

      final int row = spools.rowAtPoint(me.getPoint());

      action = new AbstractAction(LangTool.getString("spool.optionView")) {
            public void actionPerformed(ActionEvent e) {
               System.out.println(row + " is selected ");
               spools.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
               final SpooledFile splf = (SpooledFile)splfList.getObject(row);
               SwingUtilities.invokeLater(
                  new Runnable () {
                     public void run() {
                        displayViewer(splf);
                     }
                  }
               );
            }
      };

      jpm.add(action);

      action = new AbstractAction(LangTool.getString("spool.optionProps")) {
            public void actionPerformed(ActionEvent e) {

               spools.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
               final SpooledFile splf = (SpooledFile)splfList.getObject(row);
               SwingUtilities.invokeLater(
                  new Runnable () {
                     public void run() {

                        displayViewer(splf);
                     }
                  }
               );
            }
      };
      jpm.add(action);

      jpm.addSeparator();
      action = new AbstractAction(LangTool.getString("spool.optionExport")) {
         public void actionPerformed(ActionEvent e) {
            final SpooledFile splf = (SpooledFile)splfList.getObject(row);
            SpoolExportWizard sew = new SpoolExportWizard(splf);
            sew.show();
         }
      };

      jpm.add(action);
      jpm.addSeparator();

      action = new AbstractAction(LangTool.getString("spool.optionHold")) {
            public void actionPerformed(ActionEvent e) {

               final SpooledFile splf = (SpooledFile)splfList.getObject(row);
               doSpoolStuff(splf,e.getActionCommand());

            }
      };
      jpm.add(action);

      action = new AbstractAction(LangTool.getString("spool.optionRelease")) {
            public void actionPerformed(ActionEvent e) {

               final SpooledFile splf = (SpooledFile)splfList.getObject(row);
               doSpoolStuff(splf,e.getActionCommand());
            }
      };

      jpm.add(action);

      action = new AbstractAction(LangTool.getString("spool.optionDelete")) {
            public void actionPerformed(ActionEvent e) {

               final SpooledFile splf = (SpooledFile)splfList.getObject(row);
               doSpoolStuff(splf,e.getActionCommand());
            }
      };

      jpm.add(action);

      GUIGraphicsUtils.positionPopup(spools,jpm,me.getX(),me.getY());

   }

   private void doSpoolStuff(SpooledFile splf, String action) {

      try {
         if (action.equals(LangTool.getString("spool.optionHold")))
            splf.hold(null);

         if (action.equals(LangTool.getString("spool.optionRelease")))
            splf.release();

         if (action.equals(LangTool.getString("spool.optionDelete")))
            splf.delete();
      }
      catch (Exception ex) {
         System.out.println(ex.getMessage());
      }
   }

   void spools_mouseClicked(MouseEvent e) {
      if (e.getClickCount() > 1) {
         int row = spools.rowAtPoint(e.getPoint());
         System.out.println(row + " is selected ");
         spools.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         final SpooledFile splf = (SpooledFile)splfList.getObject(row);
         SwingUtilities.invokeLater(
            new Runnable () {
               public void run() {
                  displayViewer(splf);
               }
            }
         );

      }
   }

   private void displayViewer(SpooledFile splf) {

      // Create the spooled file viewer
      SpooledFileViewer sfv = new SpooledFileViewer(splf, 1);
      try {
         sfv.load();
         JFrame viewer = new JFrame(LangTool.getString("spool.viewerTitle"));
         viewer.setIconImage(this.getIconImage());

         viewer.getContentPane().add(sfv);
         viewer.pack();
         viewer.setVisible(true);
      }
      catch (Exception exc) {
         updateStatus(exc.getMessage(),true);
      }

      spools.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
   }

   private void updateStatus(String stat, boolean error) {

      final String s = stat;
      if (error)
         status.setForeground(Color.red);
      else
         status.setForeground(Color.black);

      SwingUtilities.invokeLater(
         new Runnable () {
            public void run() {
               status.setText(s);
            }
         }
      );

   }

   private void updateStatus(String stat) {

      updateStatus(stat,false);
   }

   class SpoolTableModel extends AbstractTableModel implements SortTableModel {

//      final String[] cols = {
//                  LangTool.getString("conf.tableColA"),
//                  LangTool.getString("conf.tableColB"),
//                  LangTool.getString("conf.tableColC")};
      final String[] cols = {"Name","Job Name","User","Job Number","Number","Queue"};

      Vector mySort = new Vector();
      int sortedColumn = 0;
      boolean isAscending = true;
      SpooledFileList splfList;

      public SpoolTableModel() {
         super();
      }

      public void setSpoolList(SpooledFileList sfl) {

         splfList = sfl;
         resetSorted();
         fireTableStructureChanged();
      }

      public void resetSorted() {
         mySort.clear();
         if (splfList == null)
            return;

         int splfListSize = splfList.size();
         if (splfListSize <= 0)
            return;

         int x = 0;
         String ses = null;
         Enumeration enumm = (splfList.getObjects());
         while(enumm.hasMoreElements()) {
           SpooledFile p = (SpooledFile)enumm.nextElement();
         if (sortedColumn == 0) {

           try {
            mySort.add(p.getName());
           }
           catch (Exception ex) {
            System.out.println(ex.getMessage());
           }
         }

         if (sortedColumn == 1) {

           try {
            mySort.add(p.getStringAttribute(PrintObject.ATTR_JOBNAME));
           }
           catch (Exception ex) {
            System.out.println(ex.getMessage());
           }
         }

         if (sortedColumn == 2) {

           try {
            mySort.add(p.getStringAttribute(PrintObject.ATTR_JOBUSER));
           }
           catch (Exception ex) {
            System.out.println(ex.getMessage());
           }
         }

         if (sortedColumn == 3) {

           try {
            mySort.add(p.getStringAttribute(PrintObject.ATTR_JOBNUMBER));
           }
           catch (Exception ex) {
            System.out.println(ex.getMessage());
           }
         }

         if (sortedColumn == 4) {

           try {
            mySort.add(p.getIntegerAttribute(PrintObject.ATTR_SPLFNUM).toString());
           }
           catch (Exception ex) {
            System.out.println(ex.getMessage());
           }
         }

         if (sortedColumn == 5) {

           try {
            mySort.add(p.getStringAttribute(PrintObject.ATTR_OUTPUT_QUEUE));
           }
           catch (Exception ex) {
            System.out.println(ex.getMessage());
           }
         }
         }


//         sortColumn(sortedColumn,isAscending);
//         fireTableRowsUpdated(0,splfListSize - 1);
      }

      public boolean isSortable(int col) {
//         if (col == 0)
//            return true;
//         else
//            return false;
         return true;
      }

      public void sortColumn(int col, boolean ascending) {
         sortedColumn = col;
         isAscending = ascending;
         resetSorted();
         Collections.sort(mySort, new SpoolComparator(0, ascending));
         fireTableStructureChanged();
      }

      public int getColumnCount() {

        return cols.length;
      }

      public String getColumnName(int col) {
        return cols[col];
      }

      public int getRowCount() {
         return mySort.size();
      }

//      /*
//       * Implement this so that the default session can be selected.
//       *
//       */
//      public void setValueAt(Object value, int row, int col) {
//
//           boolean which = ((Boolean)value).booleanValue();
//           if (which)
//              props.setProperty("emul.default",getPropValue(row,null));
//           else
//              props.setProperty("emul.default","");
//
//      }

      public Object getValueAt(int row, int col) {

         SpooledFile p = (SpooledFile)splfList.getObject(row);

         if (col == 0) {

           try {
            return p.getName();
           }
           catch (Exception ex) {
            System.out.println(ex.getMessage());
           }
         }

         if (col == 1) {

           try {
            return p.getStringAttribute(PrintObject.ATTR_JOBNAME);
           }
           catch (Exception ex) {
            System.out.println(ex.getMessage());
           }
         }

         if (col == 2) {

           try {
            return p.getStringAttribute(PrintObject.ATTR_JOBUSER);
           }
           catch (Exception ex) {
            System.out.println(ex.getMessage());
           }
         }

         if (col == 3) {

           try {
            return p.getStringAttribute(PrintObject.ATTR_JOBNUMBER);
           }
           catch (Exception ex) {
            System.out.println(ex.getMessage());
           }
         }

         if (col == 4) {

           try {
            return p.getIntegerAttribute(PrintObject.ATTR_SPLFNUM).toString();
           }
           catch (Exception ex) {
            System.out.println(ex.getMessage());
           }
         }

         if (col == 5) {

           try {
            return p.getStringAttribute(PrintObject.ATTR_OUTPUT_QUEUE);
           }
           catch (Exception ex) {
            System.out.println(ex.getMessage());
           }
         }

         return null;

      }

//         /*
//          * We need to implement this so that the default session column can
//          *    be updated.
//          */
//         public boolean isCellEditable(int row, int col) {
//            //Note that the data/cell address is constant,
//            //no matter where the cell appears onscreen.
//   //         if (col == 2) {
//   //             return true;
//   //         }
//   //         else {
//                return false;
//   //         }
//         }

//      /*
//       * JTable uses this method to determine the default renderer/
//       * editor for each cell.  If we didn't implement this method,
//       * then the default column would contain text ("true"/"false"),
//       * rather than a check box.
//       */
//      public Class getColumnClass(int c) {
//         return " ".getClass();
//      }

//      private String getPropValue(int row,String param) {

//         String prop = "";
//         String[] args = new String[NUM_PARMS];
//         String ses = null;
//
//         prop = (String)mySort.get(row);
//
//         if (param == null)
//            return prop;
//         else {
//            Configure.parseArgs(props.getProperty(prop),args);
//            if (param.equals("0"))
//               return args[0];
//         }
//         return null;
//      }

//      public void addSession() {
//         resetSorted();
//         fireTableRowsInserted(props.size()-1,props.size()-1);
//      }
//
//      public void chgSession(int row) {
//         resetSorted();
//         fireTableRowsUpdated(row,row);
//      }
//
//      public void removeSession(int row) {
//         resetSorted();
//         fireTableRowsDeleted(row,row);
//      }

   }

   public class SpoolComparator implements Comparator {
      protected int index;
      protected boolean ascending;

      public SpoolComparator(int index, boolean ascending) {
         this.index = index;
         this.ascending = ascending;
      }

      public int compare(Object one, Object two) {

         if (one instanceof String &&
             two instanceof String) {

            String s1 = one.toString();
            String s2 = two.toString();
            int result = 0;

            if (ascending)
               result = s1.compareTo(s2);
            else
               result = s2.compareTo(s1);

            if (result < 0) {
               return -1;
            }
            else
               if (result > 0) {
                  return 1;
               }
               else
                  return 0;
         }
         else {

            if (one instanceof Boolean &&
                  two instanceof Boolean) {
               boolean bOne = ((Boolean)one).booleanValue();
               boolean bTwo = ((Boolean)two).booleanValue();

               if (ascending) {
                  if (bOne == bTwo) {
                     return 0;
                  }
                  else
                     if (bOne) { // Define false < true
                        return 1;
                     }
                     else {
                        return -1;
                     }
               }
               else {
                  if (bOne == bTwo) {
                     return 0;
                  }
                  else
                     if (bTwo) { // Define false < true
                        return 1;
                     }
                     else {
                        return -1;
                     }
                  }
               }
               else {
                  if (one instanceof Comparable &&
                      two instanceof Comparable) {
                  Comparable cOne = (Comparable)one;
                  Comparable cTwo = (Comparable)two;
                  if (ascending) {
                     return cOne.compareTo(cTwo);
                  }
                  else {
                     return cTwo.compareTo(cOne);
                  }
               }
            }
            return 1;
         }
      }
   }

}