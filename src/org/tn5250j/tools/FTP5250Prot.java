package org.tn5250j.tools;

/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.1
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

public class FTP5250Prot {

   private Socket ftpConnectionSocket;
   private BufferedReader ftpInputStream;
   private PrintStream ftpOutputStream;
   private String serverOS;
   private InetAddress localHost;
   private boolean loggedIn;
   private String lastResponse;
   private int lastIntResponse;
   private String hostName;
   private int timeout = 50000;
   private String type;
   private String connectedOS;
   private boolean connected;
   private String remoteDir;
   private ArrayList ffd;
   private tnvt vt;
   private int recordLength;
   private int recordOutLength;
   private int fileSize;
   private Vector listeners;
   private FTPStatusEvent status;
   private boolean aborted;
   private char decChar;
   private OutputFilterInterface ofi;

   public FTP5250Prot (tnvt v) {
      vt = v;
      type = "ASCII";
      connectedOS = "OS/400";
      status = new FTPStatusEvent(this);
      // obtain the decimal separator for the machine locale
      DecimalFormat formatter =
            (DecimalFormat)NumberFormat.getInstance(Locale.getDefault()) ;

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
   public boolean connect(String host, int port) {

      try {

         hostName = host;
         ftpConnectionSocket = new Socket(host, port);
         ftpConnectionSocket.setSoTimeout(timeout);
         localHost = ftpConnectionSocket.getLocalAddress();
         ftpInputStream = new BufferedReader(new InputStreamReader(ftpConnectionSocket.getInputStream()));
         ftpOutputStream = new PrintStream(ftpConnectionSocket.getOutputStream());
         parseResponse();
         fileSize = 0;

         if (lastIntResponse == 220) {
            connected = true;
            return true;
         }
         else {
            connected = false;
            return false;
         }
      }
      catch(Exception _ex) {
         return false;
      }

   }

   /**
    * Send quit command to ftp server and close connections
    */
   public void disconnect() {
      try {
         executeCommand("QUIT");
         ftpOutputStream.close();
         ftpInputStream.close();
         ftpConnectionSocket.close();
      }
      catch(Exception _ex) { }
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

      if(ftpOutputStream == null)
      {
         printFTPInfo("Not connected to any server!");
         return false;
      }
      aborted = false;
      loggedIn = true;

      // send user command to server
      executeCommand("USER", user);

      // send password to server
      int resp = executeCommand("PASS", passWord);

      if(resp == 230) {
         loggedIn = true;
      }
      else {
         loggedIn = false;
         return false;
      }

      // check if the connected to server is an as400 or not
      if (!isConnectedToOS400()) {
         printFTPInfo("Remote server is not an OS/400.  Disconnecting!");
         disconnect();
      }

      getRemoteDirectory();
      return true;
   }

   /**
    * Print out the remote directory of the
    *
    *    not used right now but maybe in the future to obtain a list of
    *    files to select for download
    */
   protected void printDirListing () {

      try {

         Socket passSocket;

         // This will create a passive socket and execute the NLST command
         passSocket = createPassiveSocket("NLST");

         BufferedReader br = new BufferedReader(new InputStreamReader(passSocket.getInputStream()));
         String file;
         while((file = br.readLine()) != null) {
            System.out.println(file);
         }
         passSocket.close();
         parseResponse();
      }
      catch(Exception _ex) {

      }

   }

   /**
    * Checks whether the remote system is an OS400 or not
    */
   private boolean isConnectedToOS400 () {

      // get type of system connected to
      executeCommand("SYST");

      // check whether this is an OS/400 system or not
      if (lastResponse.toUpperCase().indexOf("OS/400") >= 0)
         return true;
      else
         return false;
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
    * Returns the remote directy
    */
   private void getRemoteDirectory() {

      executeCommand("PWD");

      int i = lastResponse.indexOf("\"");
      int j = lastResponse.lastIndexOf("\"");
      if(i != -1 && j != -1)
         remoteDir = lastResponse.substring(i + 1, j);
      else
         remoteDir = "Can't parse remote dir!";
   }

   /**
    * Creates a passive socket to the remote host to allow the transfer of data
    *
    */
   private Socket createPassiveSocket(String cmd) {

      ServerSocket ss = null;

      try {
         try {

            // The following line does not return the correct address of the
            //    host.  It is a know bug for linux BUG ID 4269403
            //    Since it is a know bug we have to hack the damn thing.
            //    We will use the address from localHost that was obtained
            //    from the ftpConnection socket.

//            byte abyte0[] = InetAddress.getLocalHost().getAddress();
            byte abyte0[] = localHost.getAddress();
            ss = new ServerSocket(0);
            ss.setSoTimeout(timeout);
            StringBuffer pb = new StringBuffer("PORT ");
            for(int i = 0; i < abyte0.length; i++) {
               pb.append(abyte0[i] & 0xff);
               pb.append(",");
            }

            pb.append(ss.getLocalPort() >>> 8 & 0xff);
            pb.append(",");
            pb.append(ss.getLocalPort() & 0xff);
            executeCommand(pb.toString());
            executeCommand(cmd);

            if(lastResponse.startsWith("5") || lastResponse.startsWith("4")) {
               return null;
            }

            Socket socket = ss.accept();
            socket.setSoTimeout(timeout);
            return socket;
         }
         catch(IOException ioexception) {
             printFTPInfo("I/O error while setting up a ServerSocket on the client machine!" + ioexception);
         }
         return null;
      }
      finally {
         try {
             ss.close();
         }
         catch(IOException ioexception1) {
             printFTPInfo("createPassiveSocket.close() exception!" + ioexception1);
         }
      }
   }

   /**
    * Retrieves the File Field Definitions and Member information for the remote
    *    file to be transferred
    */
   protected boolean getFileInfo(String tFile, boolean useInternal) {

      int memberOffset = tFile.indexOf(".");
      String file2 = null;
      String member2 = null;

      if (memberOffset > 0) {

         System.out.println(tFile.substring(0,memberOffset));
         file2 = tFile.substring(0,memberOffset);
         member2 = tFile.substring(memberOffset + 1);
      }
      else {
         file2 = tFile;
      }

      final String file = file2;
      final String member = member2;
      final boolean internal = useInternal;

      Runnable getInfo = new Runnable () {

         // set the thread to run.
         public void run() {

            executeCommand("RCMD","dspffd FILE(" + file + ") OUTPUT(*OUTFILE) " +
                        "OUTFILE(QTEMP/FFD) ");

            if (lastResponse.startsWith("2")) {
               if (loadFFD(internal)) {
                  if (lastResponse.startsWith("2")) {
                     if (getMbrInfo(file,member)) {
                        fireInfoEvent();
                     }
                  }
               }
            }
         }
      };

      Thread infoThread = new Thread(getInfo);
      infoThread.start();
      return true;

   }

   /**
    * Loads the File Field Definition array with the field information of the
    * remote file
    */
   private boolean loadFFD(boolean useInternal) {

      Socket socket = null;
      BufferedReader dis = null;
      String remoteFile = "QTEMP/FFD";
      String recLength = "";
      try {
         socket = createPassiveSocket("RETR " + remoteFile);
         if(socket == null) {
             return false;
         }


         dis = new BufferedReader(new InputStreamReader(socket.getInputStream()));

         String data;
         if (ffd != null) {
            ffd.clear();
            ffd = null;
         }

         ffd = new ArrayList();
         while((data = dis.readLine()) != null) {
            FileFieldDef ffDesc = new FileFieldDef(vt,decChar);

            if (useInternal)
               // WHFLDI  Field name internal
               ffDesc.setFieldName(data.substring(129,129+10));
            else
               // WHFLD  Field name text description
               ffDesc.setFieldName(data.substring(168,168+50).trim());
            // WHFOBO  Field starting offset
            ffDesc.setStartOffset(data.substring(149,149+5));
            // WHFLDB  Field length
            ffDesc.setFieldLength(data.substring(159,159+5));
            // WHFLDD  Number of digits
            ffDesc.setNumDigits(data.substring(164,164+2));
            // WHFLDP  Number of decimal positions
            ffDesc.setDecPositions(data.substring(166,166+2));
            // WHFLDT  Field type
            ffDesc.setFieldType(data.substring(321,321+1));
            // WHFTXT  Text description
            ffDesc.setFieldText(data.substring(168,168+50));
            // set selected
            ffDesc.setWriteField(true);

            recLength = data.substring(124,124+5);

            ffd.add(ffDesc);
         }

         printFTPInfo("Field Information Transfer complete!");

      }
      catch(Exception _ex) {
         printFTPInfo("I/O error!");
         return false;
      }
      finally {
         try {
             socket.close();
         }
         catch(Exception _ex) { }
         try {
             dis.close();
         }
         catch(Exception _ex) { }
      }

      int l = 0;
      int o = 0;
      int r = Integer.parseInt(recLength);
      FileFieldDef f;
      printFTPInfo("<----------------- File Field Information ---------------->");

      for (int x = 0; x < ffd.size(); x++) {
         f = (FileFieldDef)ffd.get(x);
         l += f.getFieldLength();
         o += f.getBufferOutLength();
         printFTPInfo(f.toString());
//         System.out.println(f);
      }
      recordLength = l;
      recordOutLength = o;
      System.out.println(r + " " + l + " " + o);
      parseResponse();
      return true;

   }

   /**
    * Executes the command to obtain the member information of the remote file
    */
   protected boolean getMbrInfo(String file, String member) {

      executeCommand("RCMD","dspfd FILE(" + file + ")" +
                        " TYPE(*MBR)" +
                        " OUTPUT(*OUTFILE) " +
                        "OUTFILE(QTEMP/FML) ");

      if (!lastResponse.startsWith("2"))
         return false;

      if (getMbrSize(member))

      if (!lastResponse.startsWith("2"))
         return false;

      return true;
   }

   /**
    * Parses the information obtained by the DSPFD command to obtain the size of
    * the remote file and member.
    */
   private boolean getMbrSize(String member) {

      boolean flag = true;

      if(ftpOutputStream == null) {
         printFTPInfo("Not connected to any server!");
         return false;
      }
      if(!loggedIn) {
         printFTPInfo("Login was not successful! Aborting!");
         return false;
      }

      Socket socket = null;
      DataInputStream datainputstream = null;
      executeCommand("TYPE","I");
      String remoteFile = "QTEMP/FML";

      try {
         socket = createPassiveSocket("RETR " + remoteFile);
         if(socket != null) {
            datainputstream = new DataInputStream(socket.getInputStream());

            byte abyte0[] = new byte[858];

            int c = 0;
            int kj = 0;
            int len = 0;
            StringBuffer sb = new StringBuffer(10);

            printFTPInfo("<----------------- Member Information ---------------->");

            for(int j = 0; j != -1 && !aborted;) {

               j = datainputstream.read();
               if(j == -1)
                  break;
               c ++;
               abyte0[len++] = (byte)j;

               if (len == abyte0.length) {
                  sb.setLength(0);

                  // the offset for member name MBNAME is 164 with offset of 1 but
                  //   we have to offset the buffer by 0 which makes it 164 - 1
                  //   or 163
                  for (int f = 0;f < 10; f++) {
                     sb.append(vt.getASCIIChar(abyte0[163 + f] & 0xff));
                  }

                  printFTPInfo(sb + " " + packed2int(abyte0,345,5));

                  if (member == null && fileSize == 0) {
                     // get current number of records
                     fileSize = packed2int(abyte0,345,5);
                     status.setFileLength(fileSize);
                     member = sb.toString();
                  }
                  else {
                     if (sb.toString().equalsIgnoreCase(member)) {
                        // get current number of records
                        fileSize = packed2int(abyte0,345,5);
                        status.setFileLength(fileSize);

                     }
                     else {
                        fileSize = packed2int(abyte0,345,5);
                        status.setFileLength(fileSize);
                     }
                  }

                  len =0;

               }
            }

            printFTPInfo("Member list Transfer complete!");
         }
         else
            flag = false;

      }
      catch(Exception _ex) {
         printFTPInfo("Error! " + _ex);
         return false;
      }
      finally {
         try {
             socket.close();
         }
         catch(Exception _ex) { }
         try {
             datainputstream.close();
         }
         catch(Exception _ex) { }
      }

      parseResponse();
      return flag;

   }

   /**
    * Convenience method to return the file size of the file and member that is
    * being transferred
    */
   public int getFileSize() {

      return fileSize;
   }

   /**
    * Print output of the help command
    *
    *    Not used just a test method for me
    */
   protected boolean printHelp() {

      executeCommand("HELP");
      return true;
   }

   /**
    * Transfer the file information to an output file
    */
   protected boolean getFile(String remoteFile, String localFile) {

      boolean flag = true;

      if(ftpOutputStream == null) {
         printFTPInfo("Not connected to any server!");
         return false;
      }
      if(!loggedIn) {
         printFTPInfo("Login was not successful! Aborting!");
         return false;
      }

      final String localFileF = localFile;
      final String remoteFileF = remoteFile;

      Runnable getRun = new Runnable () {

         // set the thread to run.
         public void run() {

            Socket socket = null;
            DataInputStream datainputstream = null;
            String localFileFull = localFileF;
            executeCommand("TYPE","I");

            try {
               socket = createPassiveSocket("RETR " + remoteFileF);
               if(socket != null) {
                  datainputstream = new DataInputStream(socket.getInputStream());

                  writeHeader(localFileFull);

                  byte abyte0[] = new byte[recordLength];
                  StringBuffer rb = new StringBuffer(recordOutLength);

                  int c = 0;
                  int kj = 0;
                  int len = 0;

                  for(int j = 0; j != -1 && !aborted;) {

                     j = datainputstream.read();
                     if(j == -1)
                        break;
                     c ++;
                     abyte0[len++] = (byte)j;
                     if (len == recordLength) {
                        rb.setLength(0);
                        parseFFD(abyte0,rb);
                        len =0;

                        status.setCurrentRecord(c / recordLength);
                        fireStatusEvent();
                     }
         //            if ((c / recordLength) == 200)
         //               aborted = true;
                  }
                  System.out.println(c);
                  if (c == 0) {
                     status.setCurrentRecord(c);
                     fireStatusEvent();
                  }
                  else {
                     parseResponse();
                  }
                  writeFooter();
//                  parseResponse();
                  printFTPInfo("Transfer complete!");

               }
            }
            catch(Exception _ex) {
               printFTPInfo("Error! " + _ex);
            }
            finally {
               try {
                   socket.close();
               }
               catch(Exception _ex) { }
               try {
                  datainputstream.close();
               }
               catch(Exception _ex) { }
               try {
                  writeFooter();
               }
               catch(Exception _ex) { }
            }

         }
      };

      Thread getThread = new Thread(getRun);
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
    * Execute the command without parameters on the remote ftp host
    */

   private int executeCommand(String cmd) {
      return executeCommand(cmd, null);
   }

   /**
    * Execute a command with parameters on the remote ftp host
    */
   private int executeCommand(String cmd, String params) {

      if(ftpOutputStream == null) {
         printFTPInfo("Not connected to any server!");
         return 0;
      }

      if(!loggedIn) {
         printFTPInfo("Login was not successful! Aborting!");
         return 0;
      }

      if(params != null)
         ftpOutputStream.print(cmd + " " + params + "\r\n");
      else
         ftpOutputStream.print(cmd + "\r\n");

      if(!cmd.equals("PASS"))
         printFTPInfo("SENT: " + cmd + " " + (params != null ? params : ""));
      else
         printFTPInfo("SENT: PASS ****************");

      parseResponse();
      return lastIntResponse;
   }

   /**
    * Parse the response returned from the remote host to be used for success
    * or failure of a command
    */
   private String parseResponse() {
      try {

         String response = ftpInputStream.readLine();
         String response2 = response;
         boolean append = true;

         // we loop until we get a valid numeric response
         while(response2 == null ||
                     response2.length() < 4 ||
                     !Character.isDigit(response2.charAt(0)) ||
                     !Character.isDigit(response2.charAt(1)) ||
                     !Character.isDigit(response2.charAt(2)) ||
                     response2.charAt(3) != ' ') {
            if(append) {
               response += "\n";
               append = false;
            }
            response2 = ftpInputStream.readLine();
            response += response2 + "\n";
         }

         // convert the numeric response to an int for testing later
         lastIntResponse = Integer.parseInt(response.substring(0, 3));
         // save off for printing later
         lastResponse = response;
         // print out the response
         printFTPInfo(lastResponse);

         return lastResponse;
      }
      catch(Exception exception) {
         System.out.println(exception);
         exception.printStackTrace();
         return "0000 Response Invalid";
      }
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
