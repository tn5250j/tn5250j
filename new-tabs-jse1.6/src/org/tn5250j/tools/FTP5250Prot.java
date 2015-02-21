package org.tn5250j.tools;

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

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.tn5250j.event.FTPStatusEvent;
import org.tn5250j.event.FTPStatusListener;
import org.tn5250j.framework.tn5250.tnvt;
import org.tn5250j.tools.filters.FileFieldDef;
import org.tn5250j.tools.filters.OutputFilterInterface;

public class FTP5250Prot {

   private Socket ftpConnectionSocket;
   private BufferedReader ftpInputStream;
   private PrintStream ftpOutputStream;
   private InetAddress localHost;
   private boolean loggedIn;
   private String lastResponse;
   private int lastIntResponse;
   private String hostName;
   private int timeout = 50000;
   private boolean connected;
   private String remoteDir;
   private ArrayList ffd;
   private tnvt vt;
   private int recordLength;
   private int recordOutLength;
   private int fileSize;
   private Vector<FTPStatusListener> listeners;
   private FTPStatusEvent status;
   private boolean aborted;
   private char decChar;
   private OutputFilterInterface ofi;
   private Vector members;
   private Thread getThread;

   public FTP5250Prot (tnvt v) {
      vt = v;
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
         if (isConnected()) {
            executeCommand("QUIT");
            ftpOutputStream.close();
            ftpInputStream.close();
            ftpConnectionSocket.close();
            connected = false;
         }
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
      if (lastResponse.toUpperCase().indexOf("OS/400") >= 0) return true;
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

//         System.out.println(tFile.substring(0,memberOffset));
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
      Vector allowsNullFields = null;

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
            else {

               String text = "";

               // first lets check the column headings
               text = data.substring(261,261+20).trim() + " " +
                        data.substring(281,281+20).trim() + " " +
                        data.substring(301,301+20).trim();

               if (text.length() > 0)
                  ffDesc.setFieldName(text);
               else {

                  // WHFLD  Field name text description
                  ffDesc.setFieldName(data.substring(168,168+50).trim());

                  // if the text description is blanks then use the field name
                  if (ffDesc.getFieldName().trim().length() == 0)
                     // WHFLDI  Field name internal
                     ffDesc.setFieldName(data.substring(129,129+10));
               }
            }


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

            // WHNULL Allow NULL Data
            if (data.substring(503,503+1).equals("Y")) {
               if (allowsNullFields == null)
                  allowsNullFields = new Vector(3);
               allowsNullFields.add(ffDesc.getFieldName());
               printFTPInfo("Warning -- File allows null fields!!!");
            }

            // set selected
            ffDesc.setWriteField(true);

            recLength = data.substring(124,124+5);

            ffd.add(ffDesc);
         }

         printFTPInfo("Field Information Transfer complete!");

         if (allowsNullFields != null) {
            JPanel jp = new JPanel();
            jp.setLayout(new BorderLayout());
            JLabel message = new JLabel(LangTool.getString("messages.nullFieldWarning"));
            JTextArea jta = new JTextArea(6,30);
            jta.setEditable(false);
            JScrollPane sp = new JScrollPane(jta);
            String text = new String();

            for (int x = 0; x < allowsNullFields.size(); x++)
               text += allowsNullFields.get(x) + "\n";

            jta.setText(text);

            jp.add(message,BorderLayout.NORTH);
            jp.add(sp,BorderLayout.CENTER);
            JOptionPane.showMessageDialog(null,jp,"Warning",JOptionPane.WARNING_MESSAGE);
         }

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
      members = new Vector(10);

      try {
         socket = createPassiveSocket("RETR " + remoteFile);
         if(socket != null) {
            datainputstream = new DataInputStream(socket.getInputStream());

            byte abyte0[] = new byte[858];

            int c = 0;
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
                     sb.append(vt.getCodePage().ebcdic2uni(abyte0[163 + f] & 0xff));
                  }

                  printFTPInfo(sb + " " + As400Util.packed2int(abyte0,345,5));

                  members.add(new MemberInfo(sb.toString(), As400Util.packed2int(abyte0,345,5)));

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
    * Convenience method to return the file name and member that is being
    *    transferred
    */
   public String getFullFileName(String tFile) {

      int memberOffset = tFile.indexOf(".");
      String file2 = null;
      String member2 = null;

      if (memberOffset > 0) {
         file2 = tFile.substring(0,memberOffset);
         member2 = tFile.substring(memberOffset + 1);
      }
      else {
         file2 = tFile;
      }

      if (members != null) {

         if (member2 == null) {
            MemberInfo mi = (MemberInfo)members.get(0);
            fileSize = mi.getSize();
            status.setFileLength(mi.getSize());
            member2 = mi.getName();
         }
         else {

            Iterator<?> i = members.iterator();
            MemberInfo mi = null;

            while (i.hasNext()) {
               mi = (MemberInfo)i.next();
               if (mi.getName().trim().equalsIgnoreCase(member2.trim())) {
                  fileSize = mi.getSize();
                  status.setFileLength(mi.getSize());
//                  System.out.println(" found member " + mi.getName());
                  break;
               }

            }
         }
      }

      if (member2 != null) return file2.trim() + "." + member2.trim();
      return file2.trim();
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
                     Thread.yield();
         //            if ((c / recordLength) == 200)
         //               aborted = true;
                  }
                  System.out.println(c);
                  if (c == 0) {
                     status.setCurrentRecord(c);
                     fireStatusEvent();
                  }
                  else {
                     if (!aborted)
                        parseResponse();
                  }
                  writeFooter();
//                  parseResponse();
                  printFTPInfo("Transfer complete!");

               }
            }
            catch(InterruptedIOException iioe) {
               printFTPInfo("Interrupted! " + iioe.getMessage());
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

               disconnect();
            }
         }
      };

      getThread = new Thread(getRun);
      getThread.setPriority(2);
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

         if (ftpInputStream == null)
            return "0000 Response Invalid";

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
//      catch (InterruptedIOException iioe) {
//
//         try {
//            ftpInputStream.close();
//         }
//         catch (IOException ieo) {
//
//         }
//         return "0000 Response Invalid";
//
//      }
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

   class MemberInfo {

      private String name;
      private int size;

      MemberInfo(String name, int size) {

         this.name = name;
         this.size = size;

      }

      public String getName() {
         return name;
      }

      public int getSize() {

         return size;
      }



   }
}
