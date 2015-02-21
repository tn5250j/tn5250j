/**
 * Title: SqlWizard.java
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

package org.tn5250j.sql;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.ibm.as400.vaccess.*;
import com.ibm.as400.access.*;
import java.sql.*;
import org.tn5250j.tools.LangTool;
import org.tn5250j.tools.GUIGraphicsUtils;
import org.tn5250j.tools.system.OperatingSystem;

/**
 *
 */

public class SqlWizard extends JFrame {

   private static final long serialVersionUID = 1L;
private SQLConnection connection;
   private AS400 system;
   private SQLQueryBuilderPane queryBuilder;
   private SQLResultSetTablePane tablePane;
   private String name;
   private String password;
   private String host;
   private String queryText;
   private JTextArea queryTextArea;

   public SqlWizard(String host, String name, String password ) {

      this.host = host;
      this.name = name;
      this.password = password;

      enableEvents(AWTEvent.WINDOW_EVENT_MASK);

      try {
         jbInit();
      }
      catch(Exception e) {
         e.printStackTrace();
      }
   }

   private void jbInit() throws Exception {

      try {

         setIconImages(GUIGraphicsUtils.getApplicationIcons());

         // set title
         setTitle(LangTool.getString("xtfr.wizardTitle"));

         // Load the JDBC driver.
         Driver driver2 = (Driver)Class.forName("com.ibm.as400.access.AS400JDBCDriver").newInstance();
         DriverManager.registerDriver(driver2);

         // Get a connection to the database.  Since we do not
         // provide a user id or password, a prompt will appear.
         connection = new SQLConnection("jdbc:as400://" + host, name, password);

         // Create an SQLQueryBuilderPane
         // object. Assume that "connection"
         // is an SQLConnection object that is
         // created and initialized elsewhere.
         queryBuilder = new SQLQueryBuilderPane(connection);
         queryBuilder.setTableSchemas(new String[] {"*USRLIBL"});

         // Load the data needed for the query
         // builder.
         queryBuilder.load();

         JButton done = new JButton(LangTool.getString("xtfr.tableDone"));
         done.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
               fillQueryTextArea();

            }
         });
         JPanel panel = new JPanel();
         panel.add(done);
         getContentPane().add(queryBuilder, BorderLayout.CENTER);
         getContentPane().add(panel, BorderLayout.SOUTH);

         Dimension max = new Dimension(OperatingSystem.getScreenBounds().width,
                                       OperatingSystem.getScreenBounds().height);

         pack();

         if (getSize().width > max.width)
            setSize(max.width,getSize().height);

         if (getSize().height > max.height)
            setSize(getSize().width,max.height);

         //Center the window
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         Dimension frameSize = getSize();
         if (frameSize.height > screenSize.height)
            frameSize.height = screenSize.height;
         if (frameSize.width > screenSize.width)
            frameSize.width = screenSize.width;

         setLocation((screenSize.width - frameSize.width) / 2,
                        (screenSize.height - frameSize.height) / 2);

         setVisible(true);
      }
      catch (ClassNotFoundException cnfe) {

         JOptionPane.showMessageDialog(null,"Error loading AS400 JDBC Driver",
                                             "Error",
                                             JOptionPane.ERROR_MESSAGE);

      }
   }

   private void fillQueryTextArea() {
      queryTextArea.append(queryBuilder.getQuery());

      this.setVisible(false);
      this.dispose();
   }

   public void setQueryTextArea(JTextArea qta) {
      queryTextArea = qta;
   }
}
