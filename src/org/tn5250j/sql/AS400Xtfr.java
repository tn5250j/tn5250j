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

import java.util.*;
import java.io.*;
import java.net.*;
import java.text.*;
import org.tn5250j.*;
import org.tn5250j.event.*;
import org.tn5250j.tools.filters.*;
import java.sql.*;

public class AS400Xtfr {

   private boolean loggedIn;
   private String hostName;
   private int timeout = 50000;
   private boolean connected;
   private ArrayList ffd;
   private tnvt vt;
   private Vector listeners;
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
         connection = DriverManager.getConnection ("jdbc:as400://" + hostName +
                                                   ";decimal separator=" +
                                                   decChar +
                                                   ";extended metadata=true",
                                                      user,pass);

         printFTPInfo("jdbc:as400://" + hostName +
                                                   ";decimal separator=" +
                                                   decChar );
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
          listeners = new java.util.Vector(3);
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
                    (FTPStatusListener)listeners.elementAt(i);
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
                    (FTPStatusListener)listeners.elementAt(i);
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
                    (FTPStatusListener)listeners.elementAt(i);
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
   public boolean getFile(String remoteFile, String localFile, String statement) {

      boolean flag = true;

      if(connection == null) {
         printFTPInfo("Not connected to any server!");
         return false;
      }

      final String localFileF = localFile;
      final String remoteFileF = remoteFile;
      final String query = statement;

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
                                                   " " + rsmd.getCatalogName(x) +
                                                   " " + rsmd.getSchemaName(x));

                        FileFieldDef ffDesc = new FileFieldDef(vt,decChar);

//                        if (useInternal)
//                           // WHFLDI  Field name internal
//                           ffDesc.setFieldName(rsmd.getColumnLabel(x) );
//                        else
                           // WHFLD  Field name text description
                           ffDesc.setFieldName(rsmd.getColumnName(x));

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

   /**
    * Parse the field field definition of the data and return a string buffer of
    * the output to be written
    */
   private void parseFFD(byte[] cByte,StringBuffer rb) {

      ofi.parseFields(cByte,ffd,rb);
   }

   /**
    * Abort the current file transfer
    */

   public void setAborted() {
      aborted = true;
   }

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

   /**
    * Convert an as400 packed field to an integer
    */
   private int packed2int(byte[] cByte,int startOffset, int length) {

      StringBuffer sb = new StringBuffer();

      int end = startOffset + length - 1;

      // example field of buffer length 4 with decimal precision 0
      //    output length is (4 * 2) -1 = 7
      //
      //    each byte of the buffer contains 2 digits, one in the zone
      //    portion and one in the zone portion of the byte, the last
      //    byte of the field contains the last digit in the ZONE
      //    portion and the sign is contained in the DIGIT portion.
      //
      //    The number 1234567 would be represented as follows:
      //    byte 1 of 4 -> 12
      //    byte 2 of 4 -> 34
      //    byte 3 of 4 -> 56
      //    byte 4 of 4 -> 7F    The F siginifies a positive number
      //
      //    The number -1234567 would be represented as follows:
      //    byte 1 of 4 -> 12
      //    byte 2 of 4 -> 34
      //    byte 3 of 4 -> 56
      //    byte 4 of 4 -> 7D    The D siginifies a negative number
      //
      for (int f = startOffset-1;f < end -1; f++) {
         byte bzd = cByte[f];
         int byteZ = (bzd >> 4) & 0x0f ;  // get the zone portion
         int byteD = (bzd & 0x0f);        // get the digit portion

         sb.append(byteZ); // assign the zone portion as the first digit
         sb.append(byteD); // assign the digit portion as the second digit
      }

      // here we obtain the last byte to determine the sign of the field
      byte bzd = cByte[end-1];

      int byteZ = (bzd >> 4) & 0x0f ;  // get the zone portion
      int byteD = (bzd & 0x0f);        // get the digit portion
      sb.append(byteZ);                // append the zone portion as the
                                       // the last digit of the number
      // convert to integer
      int p2i = Integer.parseInt(sb.toString());

      // Here we interrogate the the DIGIT portion for the sign
      //    0x0f = positive   -> 0x0f | 0x0d = 0x0f
      //    0x0d = negative   -> 0x0d | 0x0d = 0x0d
      if ((byteD | 0x0d) == 0x0d)
         p2i *= -1;

      return p2i;

   }

}
