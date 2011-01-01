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
package org.tn5250j.spoolfile;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import com.ibm.as400.access.*;
import com.ibm.as400.vaccess.*;

import org.tn5250j.gui.*;
import org.tn5250j.framework.tn5250.tnvt;
import org.tn5250j.tools.GUIGraphicsUtils;
import org.tn5250j.tools.LangTool;
import org.tn5250j.SessionPanel;

public class SpoolExporter extends GenericTn5250JFrame {

   private static final long serialVersionUID = 1L;
SpoolFilterPane filter;
   // custom table model
   SpoolTableModel stm;

   // The scroll pane that holds the table.
   JScrollPane scrollPane;

   // ListSelectionModel of our custom table.
   ListSelectionModel rowSM;

   // table of spools to work on
   JSortTable spools;

   // status line
   JLabel status;

   // AS400 connection
   AS400 system;

   // Connection vt
   tnvt vt;
   SessionPanel session;

   Vector data = new Vector();
   Vector row = new Vector();
   Vector names = new Vector();

   SpooledFileList splfList;

   public SpoolExporter(tnvt vt, SessionPanel session) {

      this.vt = vt;
      this.session = session;

      try {
         jbInit();
      }
      catch(Exception e) {
         e.printStackTrace();
      }
   }

   private void jbInit() throws Exception {

      this.setTitle(LangTool.getString("spool.title"));

      this.setIconImages(GUIGraphicsUtils.getApplicationIcons());

      this.getContentPane().add(createFilterPanel(), BorderLayout.NORTH);

      // get an instance of our table model
      stm = new SpoolTableModel();

      // create a table using our custom table model
      spools = new JSortTable(stm);

      TableColumn column = null;

      for (int x = 0;x < stm.getColumnCount(); x++) {
         column = spools.getColumnModel().getColumn(x);
         column.setPreferredWidth(stm.getColumnPreferredSize(x));

      }

      spools.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

      // create our mouse listener on the table
      spools.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseClicked(MouseEvent e) {
            spools_mouseClicked(e);
         }

         public void mousePressed (MouseEvent event) {
            if (SwingUtilities.isRightMouseButton(event))
               showPopupMenu(event);
         }

         public void mouseReleased (MouseEvent event) {
            if (SwingUtilities.isRightMouseButton(event))
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

              }
          });

      rowSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      this.getContentPane().add(scrollPane, BorderLayout.CENTER);


      status = new JLabel("0 " + LangTool.getString("spool.count"));
      status.setBorder(BorderFactory.createEtchedBorder());
      this.getContentPane().add(status, BorderLayout.SOUTH);

      packFrame = true;
      this.centerFrame();

//      pack();
//
//      //Center the window
//      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//      Dimension frameSize = getSize();
//      if (frameSize.height > screenSize.height)
//         frameSize.height = screenSize.height;
//      if (frameSize.width > screenSize.width)
//         frameSize.width = screenSize.width;
//
//      setLocation((screenSize.width - frameSize.width) / 2,
//                     (screenSize.height - frameSize.height) / 2);

      addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent event) {
            // close the spool file list
//            if (splfList != null)
//               splfList.close();

            // close the system connection
            if (system != null) {
               // close the spool file list if allocated
               if (splfList != null) {
                  splfList.close();
                  splfList = null;
               }

               system.disconnectAllServices();
            }
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

      reset.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(ActionEvent e) {
            filter.resetCurrent();
         }
      });

      bp.add(resetAll);
      resetAll.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(ActionEvent e) {
            filter.resetAll();
         }
      });

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
//      SpooledFileList splfList = null;
      if ( splfList != null ) {
         splfList.removePrintObjectListListener(stm);
         splfList.close();
         splfList = null;
      }

      // clear our data
      data.clear();

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

         if (filter.getUserData().length() > 0)
            splfList.setUserDataFilter(filter.getUserData());

         // retrieve the output queues
//         splfList.openSynchronously();

         // if we have something update the status
//         if (splfList != null) {
//            final int count = splfList.size();
//            updateStatus(count + " " + LangTool.getString("spool.count"));
//         }

         // load the data into our sortable data model
//         int numProcessed = loadDataModel(splfList);

         // set the spool list to be displayed
         stm.setDataVector(data,names);

         // make sure we make the column names fit correct size
         TableColumn column = null;

         for (int x = 0;x < stm.getColumnCount(); x++) {
            column = spools.getColumnModel().getColumn(x);
            column.setPreferredWidth(stm.getColumnPreferredSize(x));

         }

         splfList.openAsynchronously();
         splfList.addPrintObjectListListener(stm);

         // if we have something update the status
         if (splfList != null) {
            updateStatus(splfList.size() + " " + LangTool.getString("spool.count"));
         }

//         splfList.close();

      }
      catch(Exception erp) {
         updateStatus(erp.getMessage(),true);
      }

//      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
   }

   /**
    * Load the data model vectors with the information from the Spooled File List
    *
    * @param splfList Spooled File List to load from
    */
//      private int loadDataModel(SpooledFileList splfList) {
//
//         // clear our data
//         data.clear();
//
//         // if there is nothing then do nothing
//         if (splfList == null)
//            return 0;
//
//         int splfListSize = splfList.size();
//
//         // if we have no spooled files then display nothing
//         if (splfListSize <= 0)
//            return 0;
//
//         String text = status.getText();
//
//         boolean spoolFilter = filter.getSpoolName().length() > 0;
//         String spoolName = filter.getSpoolName();
//         int numSpooled = splfList.size();
//         int numProcessed = 0;
//
//         // iterate throw the spooled file list and load the values into our
//         //  data vector
//         int count = 0;
//         Enumeration enumm = (splfList.getObjects());
//         while(enumm.hasMoreElements()) {
//            SpooledFile p = (SpooledFile)enumm.nextElement();
//            Vector row = new Vector();
//
//            updateStatus(text + " " + ++count + " of " + numSpooled);
//
//            if (spoolFilter && !spoolName.equals(p.getName()))
//               continue;
//
//            numProcessed++;
//
//            row.add(p.getName());
//
//            loadIntegerAttribute(p, row, PrintObject.ATTR_SPLFNUM);
//            loadStringAttribute(p, row, PrintObject.ATTR_JOBNAME);
//            loadStringAttribute(p, row, PrintObject.ATTR_JOBUSER);
//            loadStringAttribute(p, row, PrintObject.ATTR_JOBNUMBER);
//            loadStringAttribute(p, row, PrintObject.ATTR_OUTPUT_QUEUE);
//            loadStringAttribute(p, row, PrintObject.ATTR_USERDATA);
//            loadStringAttribute(p, row, PrintObject.ATTR_SPLFSTATUS);
//            loadIntegerAttribute(p, row, PrintObject.ATTR_PAGES);
//            loadIntegerAttribute(p, row, PrintObject.ATTR_CURPAGE);
//            loadIntegerAttribute(p, row, PrintObject.ATTR_COPIES);
//            loadStringAttribute(p, row, PrintObject.ATTR_FORMTYPE);
//            loadStringAttribute(p, row, PrintObject.ATTR_OUTPTY);
//            loadCreateDateTime(p, row);
//            loadIntegerAttribute(p, row, PrintObject.ATTR_NUMBYTES);
//
//            // now add our row of columns into our data
//            data.add(row);
//         }
//
//         return numProcessed;
//      }

   /**
    * Load a Printer Object string attribute into our row vector
    *
    * @param p
    * @param row
    * @param attribute
    */
   private void loadStringAttribute(SpooledFile p, Vector row, int attribute) {
      try {
         row.add(p.getStringAttribute(attribute));
      }
      catch (Exception ex) {
//         System.out.println(ex.getMessage());
         row.add("Attribute Not supported");
      }
   }

   /**
    * Load a Printer Object integer/numeric attribute into our row vector
    *
    * @param p
    * @param row
    * @param attribute
    */
   private void loadIntegerAttribute(SpooledFile p, Vector row, int attribute) {
      try {
         row.add(p.getIntegerAttribute(attribute));
      }
      catch (Exception ex) {
//         System.out.println(ex.getMessage());
         row.add("Attribute Not supported");
      }
   }

   /**
    * Format the create date and time into a string to be used
    * @param p
    * @param row
    */
   private void loadCreateDateTime(SpooledFile p, Vector row) {

      try {
         String datetime = formatDate(p.getStringAttribute(PrintObject.ATTR_DATE)) +
                           " " +
                           formatTime(p.getStringAttribute(PrintObject.ATTR_TIME));
         row.add(datetime);
      }
      catch (Exception ex) {
//         System.out.println(ex.getMessage());
         row.add("Attribute Not supported");
      }
   }

   /**
    * Format the date string from the string passed
    *    format is cyymmdd
    *    c  - century -  0 1900
    *                   1 2000
    *    yy -  year
    *    mm -  month
    *    dd -  day
    *
    * @param dateString String in the format as above
    * @return  formatted date string
    */
   static String formatDate(String dateString) {

      if(dateString != null) {

         char[] dateArray = dateString.toCharArray();
         // check if the length is correct length for formatting the string should
         //  be in the format cyymmdd where
         //    c = 0 -> 19
         //    c = 1 -> 20
         if (dateArray.length != 7)
            return dateString;

         StringBuffer db = new StringBuffer(10);

         // this will strip out the starting century char as described above
         db.append(dateArray,1,6);

         // now we find out what the century byte was and insert the correct
         //  2 char number century in the buffer.
         if (dateArray[0] == '0')
            db.insert(0,"19");
         else
            db.insert(0,"20");

         db.insert(4,'/'); // add the first date seperator
         db.insert(7,'/'); // add the second date seperator
         return db.toString();
      }
      else
         return "";

   }

   /**
    * Format the time string with separator of ':'
    *
    * @param timeString
    * @return
    */
   static String formatTime(String timeString) {

      if(timeString != null) {

         StringBuffer tb = new StringBuffer(timeString);

         tb.insert(tb.length()-2,':');
         tb.insert(tb.length()-5,':');
         return tb.toString();
      }
      else
         return "";

   }

   /**
    * Show the popup menu of actions for the current table row.
    * @param me
    */
   private void showPopupMenu(MouseEvent me) {

      JPopupMenu jpm = new JPopupMenu();
      JMenuItem menuItem;
      Action action;

      final int row = spools.rowAtPoint(me.getPoint());
      final int col = spools.convertColumnIndexToModel(
                        spools.columnAtPoint(me.getPoint()));
//      System.out.println(" column clicked " + col);
//      System.out.println(" column clicked to model " + spools.convertColumnIndexToModel(col));

      action = new AbstractAction(LangTool.getString("spool.optionView")) {
            private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
               System.out.println(row + " is selected ");
               spools.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
               SwingUtilities.invokeLater(
                  new Runnable () {
                     public void run() {
                        displayViewer(getSpooledFile(row));
                     }
                  }
               );
            }
      };

      jpm.add(action);

      action = new AbstractAction(LangTool.getString("spool.optionProps")) {
            private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {

               JOptionPane.showMessageDialog(null,"Not Available yet","Not yet",
                                    JOptionPane.WARNING_MESSAGE);
            }
      };
      jpm.add(action);

      jpm.addSeparator();
      action = new AbstractAction(LangTool.getString("spool.optionExport")) {
         private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
            SpoolExportWizard sew = new SpoolExportWizard(getSpooledFile(row),
                                                            session);
            sew.setVisible(true);
         }
      };

      jpm.add(action);
      jpm.addSeparator();

      switch (col) {
         case 0:
         case 3:
         case 6:
            action = new AbstractAction(LangTool.getString("spool.labelFilter")) {
               private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
                  setFilter(row,col);
               }
            };

            jpm.add(action);
            jpm.addSeparator();
            break;
      }

      action = new AbstractAction(LangTool.getString("spool.optionHold")) {
            private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {

               doSpoolStuff(getSpooledFile(row),e.getActionCommand());

            }
      };
      jpm.add(action);

      action = new AbstractAction(LangTool.getString("spool.optionRelease")) {
            private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {

               doSpoolStuff(getSpooledFile(row),e.getActionCommand());
            }
      };

      jpm.add(action);

      action = new AbstractAction(LangTool.getString("spool.optionDelete")) {
            private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {

               doSpoolStuff(getSpooledFile(row),e.getActionCommand());
            }
      };

      jpm.add(action);

      GUIGraphicsUtils.positionPopup(spools,jpm,me.getX(),me.getY());

   }

   /**
    * Return the spooledfile from the row given from the table
    *
    * @param row from the data vector to retreive from
    * @return Spooled File of selected row
    */
   private SpooledFile getSpooledFile(int row) {

      Vector rows = (Vector)data.get(row);
      SpooledFile splf = new SpooledFile(system,
                                          (String)rows.get(0), // splf name
                                          ((Integer)rows.get(1)).intValue(), // splf number
                                          (String)rows.get(2), // job name
                                          (String)rows.get(3), // job user
                                          (String)rows.get(4));   // job number

      return splf;
   }

   /**
    * Take the appropriate action on the selected spool file
    * @param splf Spooled File to work on
    * @param action Action to take on the spooled file
    */
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

   /**
    * Process the mouse event on the table
    * @param e Mouse event passed
    */
   void spools_mouseClicked(MouseEvent e) {
      if (e.getClickCount() > 1) {
         final int row = spools.rowAtPoint(e.getPoint());
         spools.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         SwingUtilities.invokeLater(
            new Runnable () {
               public void run() {
                  displayViewer(getSpooledFile(row));
               }
            }
         );

      }
   }

   /**
    * Display the spooled file using the internal AS400 toolbox viewer
    *
    * @param splf SpooledFile to view
    */
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

   /**
    * Calls the filter object to set the appropriate filter in the filter options
    *
    * @param row
    * @param col
    */
   private void setFilter(int row, int col) {

      switch (col) {

         case 0:
            filter.setSpoolName((String)spools.getModel().getValueAt(row,col));
            break;
         case 3:
            filter.setUser((String)spools.getModel().getValueAt(row,col));
            break;
         case 6:
            filter.setUserData((String)spools.getModel().getValueAt(row,col));
            break;
         default:
            break;

      }
      System.out.println((String)spools.getModel().getValueAt(row,col));

   }
   /**
    * Update the status bar with the text.  If it is an error then change the
    * text color red else use black
    *
    * @param stat Message to display
    * @param error Whether it is an error message or not
    */
   private void updateStatus(final String stat, boolean error) {

      if (error)
         status.setForeground(Color.red);
      else
         status.setForeground(Color.black);

      SwingUtilities.invokeLater(
         new Runnable () {
            public void run() {
               status.setText(stat);
            }
         }
      );

   }

   /**
    * Update the status bar with the text in normal color
    *
    * @param stat Message to display
    */
   private void updateStatus(String stat) {

      updateStatus(stat,false);
   }

   /**
    * Custom table model used to display the spooled file list with the
    * attributes.
    *
    */
   class SpoolTableModel extends DefaultSortTableModel implements PrintObjectListListener {

      private static final long serialVersionUID = 1L;
	String[] cols;
      int[] colsSizes;

      final String colLayout = "Spool Name|100|Spool Number|90|Job Name|100|Job User|100|Job Number|90|Queue|200|User Data|100|Status|100|Total Pages|90|Current Page|90|Copies|90|Form Type|100|Priority|40|Creation Date/Time|175|Size|120";

      /**
       * Constructor
       */
      public SpoolTableModel() {

         super();
         StringTokenizer stringtokenizer = new StringTokenizer(colLayout, "|");

         // allocate the column sizes array
         colsSizes = new int[stringtokenizer.countTokens() / 2];
         // allocate the column names array
         cols = new String[stringtokenizer.countTokens() / 2];
         int i = 0;
         while(stringtokenizer.hasMoreTokens())  {
            cols[i] = stringtokenizer.nextToken();
            colsSizes[i++] = Integer.parseInt(stringtokenizer.nextToken());
         }

      }

      public int getColumnCount() {
        return cols.length;
      }

      public String getColumnName(int col) {
        return cols[col];
      }

      public int getColumnPreferredSize(int col) {
         return colsSizes[col];
      }

      /**
       * Override to not allow any rows to be editable
       *
       * @param row
       * @param col
       */
      public boolean isCellEditable(int row, int col) {

         return false;
      }

      public void listClosed(PrintObjectListEvent e) {
//                System.out.println("list closed");

         SwingUtilities.invokeLater(new Thread() {
            public void run() {
               fireTableDataChanged();
            }
         });
      }

      public void listCompleted(PrintObjectListEvent e) {
//                System.out.println("list completed");
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

         SwingUtilities.invokeLater(new Thread() {
            public void run() {
               fireTableDataChanged();
            }
         });
      }

      public void listErrorOccurred(PrintObjectListEvent e) {

         System.err.println("list error occurred : " + e.getException().getMessage());

         SwingUtilities.invokeLater(new Thread() {
            public void run() {
               fireTableDataChanged();
            }
         });
      }

      public void listObjectAdded(PrintObjectListEvent e) {
//         System.out.println("list object added");
         boolean spoolFilter = filter.getSpoolName().length() > 0;
         String spoolName = filter.getSpoolName();
         SpooledFile p = (SpooledFile)e.getObject();

         Vector row = new Vector();

         // do not process if the name is not equal to the filter.
         if (spoolFilter && !spoolName.equals(p.getName()))
            return;

         row.add(p.getName());

         loadIntegerAttribute(p, row, PrintObject.ATTR_SPLFNUM);
         loadStringAttribute(p, row, PrintObject.ATTR_JOBNAME);
         loadStringAttribute(p, row, PrintObject.ATTR_JOBUSER);
         loadStringAttribute(p, row, PrintObject.ATTR_JOBNUMBER);
         loadStringAttribute(p, row, PrintObject.ATTR_OUTPUT_QUEUE);
         loadStringAttribute(p, row, PrintObject.ATTR_USERDATA);
         loadStringAttribute(p, row, PrintObject.ATTR_SPLFSTATUS);
         loadIntegerAttribute(p, row, PrintObject.ATTR_PAGES);
         loadIntegerAttribute(p, row, PrintObject.ATTR_CURPAGE);
         loadIntegerAttribute(p, row, PrintObject.ATTR_COPIES);
         loadStringAttribute(p, row, PrintObject.ATTR_FORMTYPE);
         loadStringAttribute(p, row, PrintObject.ATTR_OUTPTY);
         loadCreateDateTime(p, row);
         loadIntegerAttribute(p, row, PrintObject.ATTR_NUMBYTES);

         //  We need to synchronize here so we will not get any errors if the
         //   user hits a column header to sort by.
         synchronized (data) {
            // now add our row of columns into our data
            data.add(row);
         }

         SwingUtilities.invokeLater(new Thread() {
            public void run() {
                 fireTableDataChanged();
                 updateStatus(data.size() + " " + LangTool.getString("spool.count"));
            }
         });
      }

      public void listOpened(PrintObjectListEvent e) {
         System.out.println("list opened");
         SwingUtilities.invokeLater(new Thread() {
            public void run() {
              fireTableDataChanged();
            }
         });
      }
   }

}