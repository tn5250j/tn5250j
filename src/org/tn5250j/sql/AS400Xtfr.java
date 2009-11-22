package org.tn5250j.sql;

/**
 * Title: AS400Xtfr.java
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

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;

import org.tn5250j.event.FTPStatusEvent;
import org.tn5250j.event.FTPStatusListener;
import org.tn5250j.framework.tn5250.tnvt;
import org.tn5250j.tools.filters.FileFieldDef;
import org.tn5250j.tools.filters.OutputFilterInterface;

public class AS400Xtfr {

   private boolean loggedIn;
   private String hostName;
   private int timeout = 50000;
   private boolean connected;
   private ArrayList ffd;
   private tnvt vt;
   private Vector<FTPStatusListener> listeners;
   private FTPStatusEvent status;
   private boolean aborted;
   private char decChar;
   private OutputFilterInterface ofi;
   private Thread getThread;
   private String user;
   private String pass;
   private Connection connection;

   public AS400Xtfr (tnvt v) {
      vt = v;
      status = new FTPStatusEvent(this);
      // obtain the decimal separator for the machine locale
      DecimalFormat formatter =
            (DecimalFormat)NumberFormat.getInstance(Locale.getDefault());

      decChar = formatter.getDecimalFormatSymbols().getDecimalSeparator();
   }

   public void setOutputFilter (OutputFilterInterface o) {
      ofi = o;
   }

   public void setDecimalChar(char dec) {
      decChar = dec;
   }

   /**
    * Set up ftp sockets and connect to an as400
    */
   public boolean connect(String host) {

      connection   = null;
      hostName = host.toUpperCase();

      try {
         printFTPInfo("Connecting to " + hostName);

         Driver driver2 = (Driver)Class.forName("com.ibm.as400.access.AS400JDBCDriver").newInstance();
         DriverManager.registerDriver(driver2);

         // Get a connection to the database.  Since we do not
         // provide a user id or password, a prompt will appear.
         // modified the connection string to add the translate binary = true
         //   as suggested from Luca
         connection = DriverManager.getConnection ("jdbc:as400://" + hostName +
                                                   ";decimal separator=" +
                                                   decChar +
                                                ";extended metadata=true;translate binary=true",
                                                      user,pass);

         printFTPInfo("jdbc:as400://" + hostName +
                        ";decimal separator=" +
                        decChar +
                        ";extended metadata=true;translate binary=true");

         fireInfoEvent();
         printFTPInfo("Connected to " + hostName);
         return true;
      }
      catch (NoClassDefFoundError ncdf) {
         printFTPInfo("Error: JDBC Driver not found.  Please check classpath." );

      }
      catch (Exception e) {
//         JOptionPane.showMessageDialog(this,
//                           "Error: " + e.getMessage() + "\n\n" +
//                           "There was an error connecting to host "
//                           + system.toUpperCase() +
//                           "\n\nPlease make sure that you run " +
//                           "the command STRHOSTSVR",
//                           "Host connection error",
//                           JOptionPane.ERROR_MESSAGE);
         printFTPInfo("Error: " + e.getMessage() + "\n\n" +
                           "There was an error connecting to host "
                           + host.toUpperCase() +
                           "\n\nPlease make sure that you run " +
                           "the command STRHOSTSVR");

         System.out.println ( "Exception while retrieving data : " + e.getMessage());
      }
      return false;
   }

   /**
    * Send quit command to ftp server and close connections
    */
   public void disconnect() {
//      try {
//         if (isConnected()) {
//            executeCommand("QUIT");
//            ftpOutputStream.close();
//            ftpInputStream.close();
//            ftpConnectionSocket.close();
//            connected = false;
//         }
//      }
//      catch(Exception _ex) { }
   }

   /**
    * returns whether or not the system is connected to an AS400 or not
    */
   public boolean isConnected() {

      return connected;
   }

   /**
    * Add a FTPStatusListener to the listener list.
    *
    * @param listener  The FTPStatusListener to be added
    */
   public synchronized void addFTPStatusListener(FTPStatusListener listener) {

      if (listeners == null) {
          listeners = new java.util.Vector<FTPStatusListener>(3);
      }
      listeners.addElement(listener);

   }

   /**
    * Notify all registered listeners of the FTPStatusEvent.
    *
    */
   private void fireStatusEvent() {

      if (listeners != null) {
         int size = listeners.size();
         for (int i = 0; i < size; i++) {
            FTPStatusListener target =
                    listeners.elementAt(i);
            target.statusReceived(status);
         }
      }
   }

   /**
    * Notify all registered listeners of the command status.
    *
    */
   private void fireCommandEvent() {

      if (listeners != null) {
         int size = listeners.size();
         for (int i = 0; i < size; i++) {
            FTPStatusListener target =
                    listeners.elementAt(i);
            target.commandStatusReceived(status);
         }
      }
   }

   /**
    * Notify all registered listeners of the file information status.
    *
    */
   private void fireInfoEvent() {

      if (listeners != null) {
         int size = listeners.size();
         for (int i = 0; i < size; i++) {
            FTPStatusListener target =
                    listeners.elementAt(i);
            target.fileInfoReceived(status);
         }
      }
   }

   /**
    * Remove a FTPStatusListener from the listener list.
    *
    * @param listener  The FTPStatusListener to be removed
    */
   public synchronized void removeFTPStatusListener(FTPStatusListener listener) {
      if (listeners == null) {
          return;
      }
      listeners.removeElement(listener);

   }

   /**
    * Send the user id and password to the connected host
    *
    * @param user  The user name
    * @param password  The password of the user
    */
   public boolean login(String user, String passWord) {

      aborted = false;
      loggedIn = true;

      this.user = user;
      this.pass = passWord;

      return true;
   }

   /**
    * Returns whether a field is selected for output or not
    *
    */
   public boolean isFieldSelected(int which) {

      FileFieldDef ffD = (FileFieldDef)ffd.get(which);
      return ffD.isWriteField();

   }

   /**
    * Select all the fields for output
    */
   protected void selectAll() {

      FileFieldDef f;
      for (int x = 0; x < ffd.size(); x++) {
         f = (FileFieldDef)ffd.get(x);
         f.setWriteField(true);
      }

   }

   /**
    * Unselect all fields for output.  This is a convenience method to unselect
    * all fields for a file that will only need to output a couple of fields
    */
   protected void selectNone() {
      FileFieldDef f;
      for (int x = 0; x < ffd.size(); x++) {
         f = (FileFieldDef)ffd.get(x);
         f.setWriteField(false);
      }

   }

   /**
    * Returns whether there are any fields selected or not
    */
   public boolean isFieldsSelected() {

      FileFieldDef f;
      for (int x = 0; x < ffd.size(); x++) {
         f = (FileFieldDef)ffd.get(x);
         if (f.isWriteField())
            return true;
      }
      return false;
   }

   /**
    * Convenience method to select or unselect a field for output
    */
   public void setFieldSelected(int which,boolean value) {

      FileFieldDef ffD = (FileFieldDef)ffd.get(which);
      ffD.setWriteField(value);

   }

   /**
    * Convenience method to return the name of a field
    */
   public String getFieldName(int which) {

      FileFieldDef ffD = (FileFieldDef)ffd.get(which);
      return ffD.getFieldName();

   }

   /**
    * Returns the number of fields in the File Field Definition array of fields
    * returned from the DSPFFD command
    */
   public int getNumberOfFields() {

      return ffd.size();
   }

   /**
    * Transfer the file information to an output file
    */
   public boolean getFile(String remoteFile, String localFile, String statement,
                           boolean useInternal) {

      boolean flag = true;

      if(connection == null) {
         printFTPInfo("Not connected to any server!");
         return false;
      }

      final String localFileF = localFile;
      final String query = statement;
      final boolean internal = useInternal;


      Runnable getRun = new Runnable () {

            // set the thread to run.
            public void run() {
               try {

                  DatabaseMetaData dmd = connection.getMetaData ();

                  // Execute the query.
                  Statement select = connection.createStatement ();

                  ResultSet rs = select.executeQuery (query);
                  ResultSetMetaData rsmd = rs.getMetaData();

                  int numCols = rsmd.getColumnCount();


                  ResultSet rsd = dmd.getColumns(null,"VISIONR","CXREF",null);

                  while (rsd.next ()) {

                     System.out.println(rsd.getString(12));
                  }

                  if (ffd != null) {
                     ffd.clear();
                     ffd = null;
                  }

                  ffd = new ArrayList();

                  printFTPInfo("Number of columns: " + rsmd.getColumnCount());

                  for (int x = 1; x <= numCols; x++) {

                     printFTPInfo("Column " + x + ": " + rsmd.getColumnLabel(x) +
                                                   " " + rsmd.getColumnName(x) +
                                                   " " + rsmd.getColumnType(x) +
                                                   " " + rsmd.getColumnTypeName(x) +
                                                   " " + rsmd.getPrecision(x) +
                                                   " " + rsmd.getScale(x) +
                                                   " cn " + rsmd.getCatalogName(x) +
                                                   " tn " + rsmd.getTableName(x) +
                                                   " sn " + rsmd.getSchemaName(x));

                        FileFieldDef ffDesc = new FileFieldDef(vt,decChar);

                        if (internal)
                           // WHFLDI  Field name internal
                           ffDesc.setFieldName(rsmd.getColumnName(x) );
                        else
                           // WHFLD  Field name text description
                           ffDesc.setFieldName(rsmd.getColumnLabel(x));

                        ffDesc.setNeedsTranslation(false);
                        // WHFOBO  Field starting offset
                        ffDesc.setStartOffset("0");
                        // WHFLDB  Field length
                        ffDesc.setFieldLength(Integer.toString(rsmd.getColumnDisplaySize(x)));
                        // WHFLDD  Number of digits
                        ffDesc.setNumDigits(Integer.toString(rsmd.getPrecision(x)));
                        // WHFLDP  Number of decimal positions
                        ffDesc.setDecPositions(Integer.toString(rsmd.getScale(x)));
                        // WHFLDT  Field type
                        switch (rsmd.getColumnType(x)) {
                           case 2:
                              ffDesc.setFieldType("S");
                              break;
                           case 3:
                              ffDesc.setFieldType("P");
                              break;
                           default:
                              ffDesc.setFieldType(" ");
                        }

                        // WHFTXT  Text description
                        ffDesc.setFieldText("");
                        // set selected
                        ffDesc.setWriteField(true);

                        ffd.add(ffDesc);

                  }

                  writeHeader(localFileF);

                  int processed = 0;
                  // Iterate throught the rows in the result set and output
                  // the columns for each row.
                  StringBuffer rb = new StringBuffer();

                  while (rs.next () && !aborted) {
                     for (int x = 1; x <= numCols; x++) {
                        ((FileFieldDef)ffd.get(x - 1)).setFieldData(rs.getString(x));
                     }
                     status.setCurrentRecord(processed++);
                     status.setFileLength(processed + 1);
                     rb.setLength(0);
                     ofi.parseFields(null,ffd,rb);
                     fireStatusEvent();
//                     System.out.println(" record > " + processed);
                  }

                  printFTPInfo("Transfer Successful ");

                  status.setCurrentRecord(processed);
                  status.setFileLength(processed);
                  fireStatusEvent();
                  writeFooter();
               }
               catch(SQLException sqle) {
                  printFTPInfo("SQL Exception ! " + sqle.getMessage());
               }
//               catch(InterruptedException iioe) {
//                  printFTPInfo("Interrupted! " + iioe.getMessage());
//               }
               catch(FileNotFoundException fnfe) {
                  printFTPInfo("File Not found Exception ! " + fnfe.getMessage());
               }

//               catch(Exception _ex) {
//                  printFTPInfo("Error! " + _ex);
//                  System.out.println(_ex.printStackTrace());
//               }
               finally {

                  // Clean up.
                  try {
                      if (connection != null)
                          connection.close ();
                  }
                  catch (SQLException e) {
                      // Ignore.
                  }

                  if (ffd != null) {
                     ffd.clear();
                     ffd = null;
                  }

                  // Clean up the memory a little
                  System.gc();
               }

            }
         };

      getThread = new Thread(getRun);
      getThread.start();

      return flag;

   }

/* *** NEVER USED ********************************************************** */
//   private void loadFields() {
//
//
//        ResultSet resultSet = null;
//        try
//        {
//            // Get database meta data
//            DatabaseMetaData metaData = connection_.getMetaData();
//
//            // Create new array to hold table values.
//            data_ = new String[ROW_INCREMENT][NUM_COLUMNS_];
//            types_ = new int[ROW_INCREMENT];
//
//            // Loop through each database file.
//            String library, table, tprefix;
//            int sepIndex;
//            int curRow;
//            for (int i=0; i<tables_.length; ++i)
//            {
//                // Get meta data.
//                sepIndex = tables_[i].indexOf(".");
//                if (sepIndex == -1)
//                {
//                    // Incorrect table specification, send error
//                    // and continue to next table.
//                    // Create generic exception to hold error message
//                    Exception e = new Exception(ResourceLoader.getText("EXC_TABLE_SPEC_NOT_VALID"));
//                    errorListeners_.fireError(e);
//                }
//                else
//                {
//                    library = tables_[i].substring(0, sepIndex);
//                    table = tables_[i].substring(sepIndex+1);
//                    if (tables_.length > 1)
//                        tprefix = table + "."; // need to qualify field names
//                    else
//                        tprefix = "";  // only 1 table, can just use field names
//
//                    resultSet = metaData.getColumns(null, library, table, null);
//
//                    // Loop through fields for this database file.
//                    while (resultSet.next())
//                    {
//                        curRow = numRows_; // current row in table
//
//                        // make sure we have room in table for this row.
//                        if (curRow >= data_.length)                         // @D1C
//                        {
//                            String[][] newData =
//                                new String[data_.length + ROW_INCREMENT][NUM_COLUMNS_];
//                            System.arraycopy(data_, 0, newData, 0, data_.length);
//                            data_ = newData;
//                            int[] newTypes =
//                                new int[types_.length + ROW_INCREMENT];
//                            System.arraycopy(types_, 0, newTypes, 0, types_.length);
//                            types_ = newTypes;
//                        }
//
//                        // Store SQL type for use by getSQLType,
//                        // although this is not externalized in the table.
//                        types_[curRow] = resultSet.getInt(5);
//
//                        // Add field info to table
//                        data_[curRow][FIELD_NAME_] = tprefix + resultSet.getString(4).trim();
//                        data_[curRow][FIELD_TYPE_] = resultSet.getString(6);
//                        // The following code should not be necessary when using
//                        // most drivers, but makes the length values correct
//                        // when using the AS400 JDBC driver.
//                        // These values came from the ODBC description of precision
//                        // (in 2.0 ref, Appendix D page 624).
//                        switch (types_[curRow])
//                        {
//                            case Types.SMALLINT:
//                                data_[curRow][FIELD_LENGTH_] = "5";
//                                break;
//                            case Types.INTEGER:
//                                data_[curRow][FIELD_LENGTH_] = "10";
//                                break;
//                            case Types.TIME:
//                                data_[curRow][FIELD_LENGTH_] = "8";
//                                break;
//                            case Types.TIMESTAMP:
//                                // We always give length = 23, even though
//                                // we should give 19 if there is no decimals.
//                                // In order to not mess up 'correct' values,
//                                // only change it if we know the value is bad.
//                                if (resultSet.getInt(7) == 10)
//                                    data_[curRow][FIELD_LENGTH_] = "23";
//                                break;
//                            case Types.DATE:
//                                data_[curRow][FIELD_LENGTH_] = "10";
//                                break;
//                            case Types.DOUBLE:
//                                if (resultSet.getInt(7) == 4)
//                                    // single precision (type REAL)
//                                    data_[curRow][FIELD_LENGTH_] = "7";
//                                else
//                                    // double precison (type FLOAT)
//                                    data_[curRow][FIELD_LENGTH_] = "15";
//                                break;
//                            default:
//                                // Other types are correct.
//                                data_[curRow][FIELD_LENGTH_] = resultSet.getString(7);
//                        }
//                        data_[curRow][FIELD_DECIMALS_] = resultSet.getString(9);
//                        data_[curRow][FIELD_NULLS_] = resultSet.getString(18);
//                        data_[curRow][FIELD_DESC_] = resultSet.getString(12);
//
//                        numRows_++;
//                    }
//                }
//            }
//        }
//        catch (SQLException e)
//        {
//            // In case of error, set fields to init state
//            data_ = new String[0][0];
//            types_ = new int[0];
//            numRows_ = 0;
//            errorListeners_.fireError(e);
//            error_ = true;
//        }
//        finally
//        {
//            if (resultSet != null)
//            {
//                try
//                {
//                    resultSet.close();
//                }
//                catch(SQLException e)
//                {
//                    errorListeners_.fireError(e);
//                }
//            }
//        }
//
//
//   }
//   /**
//    * Parse the field field definition of the data and return a string buffer of
//    * the output to be written
//    */
//   private void parseFFD(byte[] cByte,StringBuffer rb) {
//
//      ofi.parseFields(cByte,ffd,rb);
//   }

/* *** NEVER USED ********************************************************** */
//   /**
//    * Abort the current file transfer
//    */
//
//   public void setAborted() {
//      aborted = true;
//   }

   /**
    * Print ftp command events and responses
    */
   private void printFTPInfo(String msgText) {

      status.setMessage(msgText);
      fireCommandEvent();

   }

   /**
    * Write the html header of the output file
    */
   private void writeHeader(String fileName) throws
                           FileNotFoundException {

      ofi.createFileInstance(fileName);

      ofi.writeHeader(fileName,hostName,ffd,decChar);

   }

   /**
    * write the footer of the html output
    */
   private void writeFooter() {

      ofi.writeFooter(ffd);

   }

}
