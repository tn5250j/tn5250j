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

import java.util.*;
import java.text.*;
import java.net.Socket;
import java.net.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.lang.reflect.*;
import org.tn5250j.tools.CodePage;

public final class tnvt implements Runnable, TN5250jConstants {

   Socket sock;
   BufferedInputStream bin;
   BufferedOutputStream bout;
   DataStreamQueue dsq;
   Stream5250 bk;
   DataStreamProducer producer;
   private Screen5250 screen52;
   private boolean waitingForInput;
   boolean invited;
   private boolean dumpBytes = false;
   private boolean negotiated = false;
   private Thread me;
   private Thread pthread;
   private int readType;
   private boolean enhanced = true;
   private Session controller;
   private boolean cursorOn = false;
   private String session = "";
   private int port = 23;
   private boolean connected = false;
   private boolean support132 = true;
   ByteArrayOutputStream baosp = null;
   ByteArrayOutputStream baosrsp = null;
   byte[] saveStream;
   private boolean proxySet = false;
   private String proxyHost = null;
   private String proxyPort = "1080";
   private int devSeq = -1;
   private String devName;
   private String devNameUsed;

   private boolean[] dataIncluded;

   private CodePage codePage;

   private FileOutputStream fw;
   private BufferedOutputStream dw;

   tnvt (Screen5250 screen52) {

      this(screen52,false,false);

   }

   tnvt (Screen5250 screen52, boolean type, boolean support132) {

      enhanced = type;
      this.support132 = support132;
      setCodePage("37");
      this.screen52 = screen52;
      dataIncluded = new boolean[24];
      baosp = new ByteArrayOutputStream();
      baosrsp = new ByteArrayOutputStream();
   }

   public String getHostName () {

      return session;
   }

   public void setController(Session c) {

      controller = c;
   }

   public void setDeviceName(String name) {

      devName = name;

   }

   public String getDeviceName() {
      return devName;
   }

   public String getAllocatedDeviceName() {
      return devNameUsed;
   }

   public boolean isConnected() {


      return connected;
   }

   public final boolean connect() {
      return connect(session,port);

   }

   public final void setProxy(String proxyHost, String proxyPort) {

      this.proxyHost=proxyHost;
      this.proxyPort = proxyPort;
      proxySet = true;

      Properties systemProperties = System.getProperties();
      systemProperties.put("socksProxySet","true");
      systemProperties.put("socksProxyHost",proxyHost);
      systemProperties.put("socksProxyPort",proxyPort);

      System.setProperties(systemProperties);

   }

   public final boolean connect(String s, int port) {

      try {
         session = s;
         this.port = port;


         try {
            screen52.setStatus(screen52.STATUS_SYSTEM,screen52.STATUS_VALUE_ON,"X - Connecting");
         }
         catch (Exception exc) {
            System.out.println("setStatus(ON) " + exc.getMessage());

         }

         sock = new Socket(s, port);

         if (sock == null)
            System.out.println("I did not get a socket");
         connected = true;
         // used for JDK1.3
//         sock.setKeepAlive(true);
         sock.setTcpNoDelay(true);
         sock.setSoLinger(false,0);
         InputStream in = sock.getInputStream();
         OutputStream out = sock.getOutputStream();

         bin = new BufferedInputStream(in, 8192);
         bout = new BufferedOutputStream(out);

         byte abyte0[];
         while(negotiate(abyte0 = readNegotiations())) ;
         negotiated = true;
         try {
            screen52.setCursorOff();
         }
         catch (Exception excc) {
            System.out.println("setCursorOff " + excc.getMessage());

         };

         controller.fireSessionChanged(TN5250jConstants.STATE_CONNECTED);

         dsq = new DataStreamQueue();
         producer = new DataStreamProducer(this,bin,dsq,abyte0);
         pthread = new Thread(producer);
//         pthread.setPriority(pthread.MIN_PRIORITY);
         pthread.setPriority(pthread.NORM_PRIORITY/2);
         pthread.start();

         try {
            screen52.setStatus(screen52.STATUS_SYSTEM,screen52.STATUS_VALUE_OFF,null);
         }
         catch (Exception exc) {
            System.out.println("setStatus(OFF) " + exc.getMessage());

         }

         me = new Thread(this);
         me.start();


      }
      catch(Exception exception) {
         if (exception.getMessage() == null)
            exception.printStackTrace();
         System.out.println("connect() " + exception.getMessage());

         if (sock == null)
            System.out.println("I did not get a socket");

         disconnect();
         return false;
      }
      return true;

   }

   public final boolean disconnect() {

      if (me != null && me.isAlive()) {
         me.interrupt();
         pthread.interrupt();
      }

      screen52.setStatus(screen52.STATUS_SYSTEM,screen52.STATUS_VALUE_ON,"X - Disconnected");
      screen52.setKeyboardLocked(false);

      try {
         if (bin != null)
            bin.close();
         if (bout != null)
            bout.close();
         if (sock != null) {
            System.out.println("Closing socket");
            sock.close();
         }
         connected = false;
         controller.fireSessionChanged(TN5250jConstants.STATE_DISCONNECTED);

      }
      catch(Exception exception) {
         System.out.println(exception.getMessage());
         connected = false;
         devSeq = -1;
         return false;

      }
      devSeq = -1;
      return true;
   }

   private final ByteArrayOutputStream appendByteStream(byte abyte0[]) {
      ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
      for(int i = 0; i < abyte0.length; i++)
      {
         bytearrayoutputstream.write(abyte0[i]);
         if(abyte0[i] == -1)
             bytearrayoutputstream.write(-1);
      }

      return bytearrayoutputstream;
   }

    private final byte[] readNegotiations()
        throws IOException {
        int i = bin.read();
        if(i < 0) {
            throw new IOException("Connection closed.");
        }
        else {
            int j = bin.available();
            byte abyte0[] = new byte[j + 1];
            abyte0[0] = (byte)i;
            bin.read(abyte0, 1, j);
            return abyte0;
        }
    }

    private final void writeByte(byte abyte0[])
        throws IOException {

        bout.write(abyte0);
        bout.flush();
    }

    private final void writeByte(byte byte0)
        throws IOException {

        bout.write(byte0);
        bout.flush();
    }

   private final void readImmediate(int readType) {

      if (screen52.isStatusErrorCode()) {
         screen52.restoreErrorLine();
         screen52.setStatus(screen52.STATUS_ERROR_CODE,screen52.STATUS_VALUE_OFF,null);
      }

      if (!enhanced) {
         screen52.setCursorOff();
      }
         screen52.setStatus(screen52.STATUS_SYSTEM,screen52.STATUS_VALUE_ON,null);

      screen52.setKeyboardLocked(true);
      invited = false;

      screen52.getScreenFields().readFormatTable(baosp,readType,codePage);

      try {

        writeGDS(0, 3, baosp.toByteArray());
      }
      catch (IOException ioe) {

        System.out.println(ioe.getMessage());
        baosp.reset();
      }
      baosp.reset();

   }

   public final boolean sendAidKey(int aid) {

      if (screen52.isStatusErrorCode()) {
         screen52.restoreErrorLine();
         screen52.setStatus(screen52.STATUS_ERROR_CODE,screen52.STATUS_VALUE_OFF,null);
      }

      if (!enhanced) {
         screen52.setCursorOff();
      }
         screen52.setStatus(screen52.STATUS_SYSTEM,screen52.STATUS_VALUE_ON,null);

      screen52.setKeyboardLocked(true);
      invited = false;
      baosp.write(screen52.getCurrentRow());
      baosp.write(screen52.getCurrentCol());
      baosp.write(aid);

      if (dataIncluded(aid))
         screen52.getScreenFields().readFormatTable(baosp,readType,codePage);

      try {

        writeGDS(0, 3, baosp.toByteArray());
      }
      catch (IOException ioe) {

        System.out.println(ioe.getMessage());
        baosp.reset();
        return false;
      }
      baosp.reset();
      return true;

   }

   private boolean dataIncluded(int aid) {

      switch (aid) {

         case PF1:
            return !dataIncluded[0];
         case PF2:
            return !dataIncluded[1];
         case PF3:
            return !dataIncluded[2];
         case PF4:
            return !dataIncluded[3];
         case PF5:
            return !dataIncluded[4];
         case PF6:
            return !dataIncluded[5];
         case PF7:
            return !dataIncluded[6];
         case PF8:
            return !dataIncluded[7];
         case PF9:
            return !dataIncluded[8];
         case PF10:
            return !dataIncluded[9];
         case PF11:
            return !dataIncluded[10];
         case PF12:
            return !dataIncluded[11];
         case PF13:
            return !dataIncluded[12];
         case PF14:
            return !dataIncluded[13];
         case PF15:
            return !dataIncluded[14];
         case PF16:
            return !dataIncluded[15];
         case PF17:
            return !dataIncluded[16];
         case PF18:
            return !dataIncluded[17];
         case PF19:
            return !dataIncluded[18];
         case PF20:
            return !dataIncluded[19];
         case PF21:
            return !dataIncluded[20];
         case PF22:
            return !dataIncluded[21];
         case PF23:
            return !dataIncluded[22];
         case PF24:
            return !dataIncluded[23];

         default:
            return true;

      }

   }

   /**
    * Help request -
    *
    *
    *    See notes inside method
    */
   public final void sendHelpRequest() {

      // Client sends header           000D12A0000004000003####F3FFEF
      //       operation code 3
      //       row - first ##
      //       column - second ##
      //       F3 - Help Aid Key
//      System.out.println("Help request sent");
      baosp.write(screen52.getCurrentRow());
      baosp.write(screen52.getCurrentCol());
      baosp.write(AID_HELP);

      try {
        writeGDS(0, 3, baosp.toByteArray());
      }
      catch (IOException ioe) {

        System.out.println(ioe.getMessage());
      }
      baosp.reset();
   }

   /**
    * Attention Key -
    *
    *
    *    See notes inside method
    */
   public final void sendAttentionKey() {

      // Client sends header           000A12A000004400000FFEF
      //    0x40 -> 01000000
      //
      // flags
      // bit 0 - ERR
      // bit 1 - ATN Attention
      // bits 2-4   - reserved
      // bit 5 -  SRQ system request
      // bit 6 - TRQ Test request key
      // bit 7 - HLP

//      System.out.println("Attention key sent");

      try {
        writeGDS(0x40, 0, null);
      }
      catch (IOException ioe) {

        System.out.println(ioe.getMessage());
      }
   }

   public final void systemRequest() {
      systemRequest(' ');
   }

   /**
    * System request - taken from the rfc1205 - 5250 Telnet interface
    *    section 4.3
    *
    *    See notes inside method
    */
   public final void systemRequest(char sr) {


      if (sr == ' ') {
         JPanel srp = new JPanel();
         srp.setLayout(new BorderLayout());
         JLabel jl = new JLabel("Enter alternate job");
         JTextField sro = new JTextField();
         srp.add(jl,BorderLayout.NORTH);
         srp.add(sro,BorderLayout.CENTER);
         Object[]      message = new Object[1];
         message[0] = srp;
         String[] options = {"SysReq","Cancel"};

         int result = 0;
            result = JOptionPane.showOptionDialog(
                null,                               // the parent that the dialog blocks
                message,                           // the dialog message array
                "System Request",                  // the title of the dialog window
                JOptionPane.DEFAULT_OPTION,        // option type
                JOptionPane.QUESTION_MESSAGE,      // message type
                null,                              // optional icon, use null to use the default icon
                options,                           // options string array, will be made into buttons//
                options[0]                         // option that should be made into a default button
            );

            switch(result) {
               case 0: // Send SysReq
                  // from rfc1205 section 4.3
                  // Client sends header with the           000A12A0000004040000FFEF
                  // System Request bit set.
                  //
                  // if we wanted to send an option with it we would need to send
                  //    it at the end such as the following
                  //
                  // byte abyte0[] = new byte[1];    or number of bytes in option
                  // abyte0[0] = getEBCDIC(option);

                  System.out.println("SYSRQS sent");

                  // send option along with system request
                  if (sro.getText().length() > 0) {
                     for (int x = 0; x < sro.getText().length(); x++) {
   //                     System.out.println(sro.getText().charAt(x));
                        baosp.write(getEBCDIC(sro.getText().charAt(x)));
                     }

                     try {
                       writeGDS(4, 0, baosp.toByteArray());
                     }
                     catch (IOException ioe) {

                       System.out.println(ioe.getMessage());
                     }
                     baosp.reset();
                  }
                  else {    // no option sent with system request


                     try {
                       writeGDS(4, 0, null);
                     }
                     catch (IOException ioe) {

                       System.out.println(ioe.getMessage());
                     }



                  }

                  break;
               case 1: // Cancel
      //		      System.out.println("Cancel");
                  break;
               default:
                  break;
            }
         controller.requestFocus();
      }
      else {

         baosp.write(getEBCDIC(sr));

         try {
            writeGDS(4, 0, baosp.toByteArray());
         }
         catch (IOException ioe) {
            baosp.reset();
            System.out.println(ioe.getMessage());
         }
         baosp.reset();
      }
   }


   /**
    * Cancel Invite - taken from the rfc1205 - 5250 Telnet interface
    *    section 4.3
    *
    *    See notes inside method
    */
   public final void cancelInvite() {

      screen52.setStatus(screen52.STATUS_SYSTEM,screen52.STATUS_VALUE_ON,null);

      // from rfc1205 section 4.3
      // Server: Sends header with the          000A12A0 00000400 000AFFEF
      // Opcode = Cancel Invite.


      // Client: sends header with the          000A12A0 00000400 000AFFEF
      // Opcode = Cancel Invite to
      // indicate that the work station is
      // no longer invited.
      try {
        writeGDS(0, 10, null);
      }
      catch (IOException ioe) {

        System.out.println(ioe.getMessage());
      }

   }

   public final void hostPrint(int aid) {

      if (screen52.isStatusErrorCode()) {
         screen52.restoreErrorLine();
         screen52.setStatus(screen52.STATUS_ERROR_CODE,screen52.STATUS_VALUE_OFF,null);
      }

      screen52.setCursorOff();
      screen52.setStatus(screen52.STATUS_SYSTEM,screen52.STATUS_VALUE_ON,null);
      // From client access ip capture
      // it seems to use an operation code of 3 and 4
      // also note that the flag field that says reserved is being sent as well
      // with a value of 0x80
      //
      // I have tried with not setting these flags and sending with 3 or 1
      // there is no effect and I still get a host print screen.  Go figure
      //0000:  000D 12A0 0000 0400 8003 1407 F6FFEF
      //0000:  000D 12A0 0000 0400 8001 110E F6FFEF
      //
      // Client sends header           000D12A0000004000003####F6FFEF
      //       operation code 3
      //       row - first ##
      //       column - second ##
      //       F6 - Print Aid Key

      baosp.write(screen52.getCurrentRow());
      baosp.write(screen52.getCurrentCol());
      baosp.write(AID_PRINT);                 // aid key

      try {
        writeGDS(0, 3, baosp.toByteArray());
      }
      catch (IOException ioe) {

        System.out.println(ioe.getMessage());
      }
      baosp.reset();
   }

   protected final void toggleDebug () {

      dumpBytes = !dumpBytes;
      if (dumpBytes) {

         try {
            if (fw == null) {
               fw = new FileOutputStream("log.txt");
               dw = new BufferedOutputStream(fw);
            }
         }
         catch (FileNotFoundException fnfe) {
            System.out.println(fnfe.getMessage());
         }

      }
      else {

         try {

            if (dw != null)
               dw.close();
            if (fw != null)
               fw.close();
            dw = null;
            fw = null;

         }
         catch(IOException ioe) {

            System.out.println(ioe.getMessage());
         }
      }

      System.out.println("Data Stream output is now " + dumpBytes);
   }

   // write gerneral data stream
   public final void writeGDS(int flags, int opcode, byte abyte0[])
      throws IOException {

      // Added to fix for JDK 1.4 this was null coming from another method.
      //  There was a weird keyRelease event coming from another panel when
      //  using a key instead of the mouse to select button.
      //  The other method was fixed as well but this check should be here anyway.
      if (bout == null)
         return;

      int length;
      if(abyte0 != null)
         length = abyte0.length + 10;
      else
         length = 10;

      // refer to rfc1205 - 5250 Telnet interface
      // Section 3.  Data Stream Format

      // Logical Record Length   -  16 bits
      baosrsp.write(length >> 8);       // Length LL
      baosrsp.write(length & 0xff);     //        LL

      // Record Type -  16 bits
      // It should always be set to '12A0'X to indicate the
      // General Data Stream (GDS) record type.
      baosrsp.write(18);          // 0x12
      baosrsp.write(160);         // 0xA0

      // the next 16 bits are not used
      baosrsp.write(0);           // 0x00
      baosrsp.write(0);           // 0x00

      //  The second part is meant to be variable in length
      //  currently this portion is 4 octets long (1 byte or 8 bits for us ;-O)
      baosrsp.write(4);           // 0x04

      baosrsp.write(flags);       // flags
                                                // bit 0 - ERR
                                                // bit 1 - ATN Attention
                                                // bits 2-4   - reserved
                                                // bit 5 -  SRQ system request
                                                // bit 6 - TRQ Test request key
                                                // bit 7 - HLP
      baosrsp.write(0);           // reserved - set to 0x00
      baosrsp.write(opcode);      // opcode

      if(abyte0 != null)
         baosrsp.write(abyte0, 0, abyte0.length);

      baosrsp = appendByteStream(baosrsp.toByteArray());

      // make sure we indicate no more to be sent
      baosrsp.write(IAC);
      baosrsp.write(EOR);

      baosrsp.writeTo(bout);

//        byte[] b = new byte[baosrsp.size()];
//        b = baosrsp.toByteArray();
//      dump(b);
      bout.flush();
//      baos = null;
      baosrsp.reset();
   }

   public final int getOpCode() {

      return bk.getOpCode();
   }

   private final void sendNotify() throws IOException {

      writeGDS(0, 0, null);
   }

   private final void setInvited() {

//      System.out.println("invited");
      if (!screen52.isStatusErrorCode())
         screen52.setStatus(screen52.STATUS_SYSTEM,screen52.STATUS_VALUE_OFF,null);

      invited = true;
   }

   public void run () {

      while (true) {

         try {
            bk = (Stream5250)dsq.get();
         }
         catch (InterruptedException ie) {

            System.out.println(" ie " + ie.getMessage());
         }

         // lets play nicely with the others on the playground
         me.yield();

         pthread.yield();

         invited = false;
         screen52.setCursorOff();

//      System.out.println("operation code: " + bk.getOpCode());
         switch (bk.getOpCode()) {
            case 00:
//               System.out.println("No operation");
               break;
            case 1:
//               System.out.println("Invite Operation");
               parseIncoming();
               screen52.setKeyboardLocked(false);
               cursorOn = true;
               setInvited();
               break;
            case 2:
//               System.out.println("Output Only");
               parseIncoming();
//               System.out.println(screen52.dirty);
               screen52.updateDirty();

   //            invited = true;

               break;
            case 3:
//               System.out.println("Put/Get Operation");
               parseIncoming();
//               inviteIt =true;
               setInvited();
               break;
            case 4:
//            System.out.println("Save Screen Operation");
               parseIncoming();
               break;

            case 5:
//               System.out.println("Restore Screen Operation");
               parseIncoming();
               break;
            case 6:
//               System.out.println("Read Immediate");
               sendAidKey(0);
               break;
            case 7:
//               System.out.println("Reserved");
               break;
            case 8:
//               System.out.println("Read Screen Operation");
               try {
                  readScreen();
               }
               catch (IOException ex) {

               }
               break;

            case 9:
//               System.out.println("Reserved");
               break;

            case 10:
//               System.out.println("Cancel Invite Operation");
               cancelInvite();
               break;

            case 11:
//               System.out.println("Turn on message light");
               screen52.setMessageLightOn();
               screen52.setCursorOn();

               break;
            case 12:
//               System.out.println("Turn off Message light");
               screen52.setMessageLightOff();
               screen52.setCursorOn();

               break;
            default:
               break;
         }

         if (screen52.isUsingGuiInterface())
            screen52.drawFields();

//      if (screen52.screen[0][1].getChar() == '#' &&
//         screen52.screen[0][2].getChar() == '!')
//         execCmd();
//      else {

         if (screen52.isHotSpots()) {
            screen52.checkHotSpots();
         }

         try {
            screen52.updateDirty();
         }
         catch (Exception exd ) {
            System.out.println(" tnvt.run: " + exd.getMessage());
         }

         if (cursorOn && !screen52.isKeyboardLocked()) {
            screen52.setCursorOn();
            cursorOn = false;
         }
         // lets play nicely with the others on the playground
         me.yield();
         pthread.yield();


      }
   }

//      private final void execCmd() {
//         String name = "";
//         String argString = "";
//
//         StringBuffer sb = new StringBuffer();
//         sb.append(screen52.screen[0][3].getChar());
//         sb.append(screen52.screen[0][4].getChar());
//         sb.append(screen52.screen[0][5].getChar());
//         sb.append(screen52.screen[0][6].getChar());
//
//         System.out.println("command = " + sb);
//         int x = 8;
//         sb.setLength(0);
//         while (screen52.screen[0][x].getChar() > ' ') {
//            sb.append(screen52.screen[0][x].getChar());
//            x++;
//         }
//         name = sb.toString();
//         System.out.println("name = " + name);
//
//         sb.setLength(0);
//         x++;
//         while (screen52.screen[0][x].getChar() >= ' ') {
//            sb.append(screen52.screen[0][x].getChar());
//            x++;
//         }
//         argString = sb.toString();
//         System.out.println("args = " + argString);
//
//         sendAidKey(AID_ENTER);
//
//         try {
//
//            Class c = Class.forName(name);
//            String args1[] = {argString};
//            String args2[] = {};
//
//            Method m = c.getMethod("main",
//            new Class[] { args1.getClass() });
//            m.setAccessible(true);
//            int mods = m.getModifiers();
//            if (m.getReturnType() !=
//                   void.class || !Modifier.isStatic(mods) ||
//                  !Modifier.isPublic(mods)) {
//
//                     throw new NoSuchMethodException("main");
//                  }
//            try {
//               if (argString.length() > 0)
//                  m.invoke(null, new Object[] { args1 });
//               else
//                  m.invoke(null, new Object[] { args2 });
//            }
//            catch (IllegalAccessException e) {
//                 // This should not happen, as we have
//                 // disabled access checks
//                  System.out.println("iae " + e.getMessage());
//
//            }
//         }
//         catch (ClassNotFoundException cnfe) {
//            System.out.println("cnfe " + cnfe.getMessage());
//         }
//         catch (NoSuchMethodException nsmf) {
//            System.out.println("nsmf " + nsmf.getMessage());
//         }
//         catch (InvocationTargetException ite) {
//            System.out.println("ite " + ite.getMessage());
//         }
//   //      catch (IllegalAccessException iae) {
//   //         System.out.println("iae " + iae.getMessage());
//   //      }
//   //      catch (InstantiationException ie) {
//   //         System.out.println("ie " + ie.getMessage());
//   //      }
//   //      try {
//   //
//   //         Runtime rt = Runtime.getRuntime();
//   //         Process proc = rt.exec("notepad");
//   //         int exitVal = proc.exitValue();
//   //      }
//   //      catch (Throwable t) {
//   //
//   //         t.printStackTrace();
//   //      }
//      }

   private final void readScreen()
        throws IOException {

      int rows = screen52.getRows();
      int cols = screen52.getCols();
      boolean att = false;
      int off = 0;
      byte abyte0[] = new byte[ rows * cols];
      fillScreenArray(abyte0, rows, cols);
      writeGDS(0, 0, abyte0);
      abyte0 = null;
   }

   private final void fillScreenArray(byte[] sa, int rows, int cols) {

      int la = 32;
      int sac = 0;
      int len = rows * cols;
      for (int y = 0;y < len; y++)  {  // save the screen data

         if(screen52.screen[y].isAttributePlace()) {
            la = screen52.screen[y].getCharAttr();
            sa[sac++] = (byte)la;
         }
         else {
            if (screen52.screen[y].getCharAttr() != la) {
               la = screen52.screen[y].getCharAttr();
               sac--;
               sa[sac++] = (byte)la;
            }
            sa[sac++] = (byte)getEBCDIC(screen52.screen[y].getChar());
         }
      }
   }

   public final void saveScreen()
        throws IOException  {

      ByteArrayOutputStream sc = new ByteArrayOutputStream();
      sc.write(4);
      sc.write(0x12);  // 18
      sc.write(0);  // 18
      sc.write(0);  // 18

      sc.write((byte)screen52.getRows());   // store the current size
      sc.write((byte)screen52.getCols());   //    ""

      int cp = screen52.getCurrentPos(); // save off current position
      sc.write((byte)(cp >> 8 & 0xff));          //    ""
      sc.write((byte)(cp & 0xff));               //    ""

      sc.write((byte)(screen52.homePos >> 8 & 0xff));       // save home pos
      sc.write((byte)(screen52.homePos & 0xff));            //    ""

      int rows = screen52.getRows();   // store the current size
      int cols = screen52.getCols();   //    ""
      byte[] sa = new byte[rows * cols];
      fillScreenArray(sa, rows, cols);

      sc.write(sa);
      sa = null;
      int sizeFields = screen52.getScreenFields().getSize();
      sc.write((byte)(sizeFields >> 8 & 0xff));          //    ""
      sc.write((byte)(sizeFields & 0xff));               //    ""

      if (sizeFields > 0) {
         int x = 0;
         int s = screen52.getScreenFields().getSize();
         ScreenField sf = null;
         while (x < s) {
            sf = screen52.getScreenFields().getField(x);
            sc.write((byte)sf.getAttr());          // attribute
            int sp = sf.startPos();
            sc.write((byte)(sp >> 8 & 0xff));          //    ""
            sc.write((byte)(sp & 0xff));               //    ""
            if (sf.mdt)
               sc.write((byte)1);
            else
               sc.write((byte)0);
            sc.write((byte)(sf.getLength() >> 8 & 0xff));          //    ""
            sc.write((byte)(sf.getLength() & 0xff));               //    ""
            sc.write((byte)sf.getFFW1() & 0xff);
            sc.write((byte)sf.getFFW2() & 0xff);
            sc.write((byte)sf.getFCW1() & 0xff);
            sc.write((byte)sf.getFCW2() & 0xff);
//            System.out.println("Saved ");
//            System.out.println(sf.toString());

            x++;
         }
         sf = null;
      }

      screen52.getScreenFields().setCurrentField(null);    // set it to null for GC ?

      screen52.clearTable();
      try {
         writeGDS(0, 3, sc.toByteArray());
      }
      catch (IOException ioe) {

        System.out.println(ioe.getMessage());
      }

      sc = null;
//      System.out.println("Save Screen end ");
   }

   /**
    */
   public final void restoreScreen()
        throws IOException   {
      int which = 0;
      try {
//         System.out.println("Restore ");

         bk.getNextByte();
         bk.getNextByte();

         int rows = bk.getNextByte() & 0xff;
         int cols = bk.getNextByte() & 0xff;
         int pos = bk.getNextByte() << 8 & 0xff00;
         pos |= bk.getNextByte() & 0xff;
         int hPos = bk.getNextByte() << 8 & 0xff00;
         hPos |= bk.getNextByte() & 0xff;
         if (rows != screen52.getRows())
            screen52.setRowsCols(rows,cols);
         screen52.clearAll();    // initialize what we currenty have
         int b = 32;
         int la = 32;
         int len = rows * cols;
         for (int y = 0;y < len; y++) {

            b = bk.getNextByte();
            if (isAttribute(b)) {
               screen52.screen[y].setCharAndAttr(
                           screen52.screen[y].getChar(),
                           b,
                           true);
               la = b;

            }
            else {

               screen52.screen[y].setCharAndAttr(
                              getASCIIChar(b),
                              la,
                              false);
            }
         }

         int numFields = bk.getNextByte() << 8 & 0xff00;
         numFields |= bk.getNextByte() & 0xff;
//         System.out.println("number of fields " + numFields);

         if (numFields > 0) {
            int x = 0;
            int attr = 0;
            int fPos = 0;
            int fLen = 0;
            int ffw1 = 0;
            int ffw2 = 0;
            int fcw1 = 0;
            int fcw2 = 0;
            boolean mdt = false;

            ScreenField sf = null;
            while (x < numFields) {

               attr = bk.getNextByte();
               fPos = bk.getNextByte() << 8 & 0xff00;
               fPos |= bk.getNextByte() & 0xff;
               if (bk.getNextByte() == 1)
                  mdt = true;
               else
                  mdt = false;
               fLen = bk.getNextByte() << 8 & 0xff00;
               fLen |= bk.getNextByte() & 0xff;
               ffw1 = bk.getNextByte();
               ffw2 = bk.getNextByte();
               fcw1 = bk.getNextByte();
               fcw2 = bk.getNextByte();

               sf = screen52.getScreenFields().setField(attr,
                              screen52.getRow(fPos),
                              screen52.getCol(fPos),
                              fLen,
                              ffw1,
                              ffw2,
                              fcw1,
                              fcw2);

               if (mdt)
                  sf.setMDT();

//               System.out.println("/nRestored ");
//               System.out.println(sf.toString());
//
               x++;
            }
         }

         screen52.restoreScreen();  // display the screen
         screen52.homePos = hPos;
         screen52.goto_XY(pos);
         screen52.isInField();
         if (screen52.isUsingGuiInterface())
            screen52.drawFields();
      }
      catch (Exception e) {
         System.out.println("error restoring screen " + which + " with " + e.getMessage());
      }
   }

    public final boolean waitingForInput () {

      return waitingForInput;
    }

   private void parseIncoming() {

      boolean controlChars = false;
      byte control0;
      byte control1;
      boolean done = false;
      boolean error = false;

      try {
         while (bk.hasNext() && !done) {
            byte b = bk.getNextByte();

            switch (b) {
               case 0:
               case 1:
                  break;
               case CMD_SAVE_SCREEN:   // 0x02 2 Save Screen
               case 3:   // 0x03 3 Save Partial Screen
                  saveScreen();
                  break;

               case ESC:    // ESCAPE
                  break;
               case 7:    // audible bell
                  Toolkit.getDefaultToolkit().beep();
                  bk.getNextByte();
                  bk.getNextByte();
                  break;
               case CMD_WRITE_TO_DISPLAY:    // 0x11 17 write to display
                  error = writeToDisplay(true);
                  break;
               case CMD_RESTORE_SCREEN:   // 0x12 18 Restore Screen
               case 13:   // 0x13 19 Restore Partial Screen
                  restoreScreen();
                  break;

               case CMD_CLEAR_UNIT_ALTERNATE:    // 0x20 32 clear unit alternate
                  int param = bk.getNextByte();
                  if (param != 0) {
//                     System.out.println(" clear unit alternate error " + Integer.toHexString(param));
                     sendNegResponse(NR_REQUEST_ERROR,03,01,05,
                                    " clear unit alternate not supported");
                     done = true;
                  }
                  else {
                     if (screen52.getRows() != 27)
                        screen52.setRowsCols(27,132);
                     screen52.clearAll();

                  }
                  break;

               case CMD_WRITE_ERROR_CODE:   // 0x21 33 Write Error Code
                  writeErrorCode();
                  error = writeToDisplay(false);
                  break;
               case CMD_WRITE_ERROR_CODE_TO_WINDOW:   // 0x22 34
                                                      // Write Error Code to window
                  writeErrorCodeToWindow();
                  error = writeToDisplay(false);
                  break;

               case CMD_READ_SCREEN_IMMEDIATE:    // 0x62  98
               case CMD_READ_SCREEN_TO_PRINT:    // 0x66  102 read screen to print
                  readScreen();
                  break;

               case CMD_CLEAR_UNIT:    // 64 0x40 clear unit
                  if (screen52.getRows() != 24)
                     screen52.setRowsCols(24,80);
                  screen52.clearAll();
                  break;

               case CMD_CLEAR_FORMAT_TABLE:  // 80 0x50 Clear format table
                  screen52.clearTable();
                  break;

               case CMD_READ_INPUT_FIELDS:      //0x42 66 read input fields
               case CMD_READ_MDT_FIELDS:    // 0x52 82 read MDT Fields
                  bk.getNextByte();
                  bk.getNextByte();
                  readType = b;
                  screen52.goHome();
//                  screen52.setCursorOn();
                  waitingForInput = true;
                  screen52.setKeyboardLocked(false);
                  break;
               case CMD_READ_MDT_IMMEDIATE_ALT: // 0x53 83
                  readType = b;
//                  screen52.goHome();
//                  waitingForInput = true;
//                  screen52.setKeyboardLocked(false);
                  readImmediate(readType);
                  break;
               case CMD_WRITE_STRUCTURED_FIELD:   // 243 0xF3 -13 Write structured field
                  writeStructuredField();
                  break;

               default:
                  done = true;
                  sendNegResponse(NR_REQUEST_ERROR,03,01,01,"parseIncoming");
                  break;
            }

            if (error)
               done = true;
         }
      }
      catch (Exception exc) {System.out.println("incoming " + exc.getMessage());};
   }

   /**
    * This routine handles sending negative responses back to the host.
    *
    *    You can find a description of the types of responses to be sent back
    *    by looking at section 12.4 of the 5250 Functions Reference manual
    *
    *
    */
   private void sendNegResponse(int cat, int modifier , int uByte1, int uByte2, String from) {

      try {

         int os = bk.getByteOffset(-1) & 0xf0;
         int cp = (bk.getCurrentPos()-1);
         System.out.println("invalid " + from + " command " +
                              os +
                              " at pos " +
                               cp);
      }
      catch (Exception e) {

        System.out.println("Send Negative Response error " +  e.getMessage());
      }


      baosp.write(cat);
      baosp.write(modifier);
      baosp.write(uByte1);
      baosp.write(uByte2);

      try {
        writeGDS(128, 0, baosp.toByteArray());
      }
      catch (IOException ioe) {

        System.out.println(ioe.getMessage());
      }
      baosp.reset();

   }

   public void sendNegResponse2(int ec) {


      baosp.write(0x00);
      baosp.write(ec);

      try {
        writeGDS(1, 0, baosp.toByteArray());
      }
      catch (IOException ioe) {

        System.out.println(ioe.getMessage());
      }

      baosp.reset();
   }

   private boolean writeToDisplay(boolean controlsExist) {

      int pos = 0;
      boolean error=false;
      boolean done=false;
      int attr;
      byte nextOne;
      byte control0 = 0;
      byte control1 = 0;
      int saRows = screen52.getRows();
      int saCols = screen52.getCols();

      try {
         if (controlsExist) {
            control0 = bk.getNextByte();
            control1 = bk.getNextByte();
            processCC0(control0);
         }
         while (bk.hasNext() && !done) {
//            pos = bk.getCurrentPos();

//            int rowc = screen52.getCurrentRow();
//            int colc = screen52.getCurrentCol();

            switch (bk.getNextByte()) {

               case 1:     // SOH - Start of Header Order

                  error = processSOH();

                  break;
               case 02:    // RA - Repeat to address

                  int row = screen52.getCurrentRow();
                  int col = screen52.getCurrentCol();

                  int toRow = bk.getNextByte();
                  int toCol = bk.getNextByte() & 0xff;
                  if (toRow >= row) {
                     int repeat = bk.getNextByte();

                     // a little intelligence here I hope
                     if (row == 1 &&
                        col == 2 &&
                        toRow == screen52.getRows() &&
                        toCol == screen52.getCols())

                        screen52.clearScreen();
                     else {
                        if (repeat != 0)
                           repeat = getASCIIChar(repeat);

                        int times = ((toRow * screen52.getCols()) + toCol) -
                                 ((row * screen52.getCols()) + col);
                        while (times-- >= 0) {
                           screen52.setChar(repeat);
                        }

                     }
                  }
                  else {
                     sendNegResponse(NR_REQUEST_ERROR,0x05,0x01,0x23," RA invalid");
                     error =true;
                  }
                  break;

               case 03:    // EA - Erase to address
                  int EArow = screen52.getCurrentRow();
                  int EAcol = screen52.getCurrentCol();

                  int toEARow = bk.getNextByte();
                  int toEACol = bk.getNextByte() & 0xff;
                  int EALength = bk.getNextByte()  & 0xff;
                  while (--EALength > 0) {

                     bk.getNextByte();


                  }
                  char EAAttr = (char)0;

                  // a little intelligence here I hope
                  if (EArow == 1 &&
                     EAcol == 2 &&
                     toEARow == screen52.getRows() &&
                     toEACol == screen52.getCols())

                     screen52.clearScreen();
                  else {
                     int times = ((toEARow * screen52.getCols()) + toEACol) -
                              ((EArow * screen52.getCols()) + EAcol);
                     while (times-- >= 0) {
                        screen52.setChar(EAAttr);
                     }
                  }
                  break;
               case 04:    // Command - Escape
                  done = true;
                  break;

               case 16:    // TD - Transparent Data
                  bk.getNextByte();
                  int j = bk.getNextByte();  // length
                  while (j-- > 0)
                     bk.getNextByte();
                  break;

               case 17:    // SBA - set buffer address order (row column)
                  int saRow = bk.getNextByte();
                  int saCol = bk.getNextByte() & 0xff;
                  // make sure it is in bounds
                  if (saRow >= 0 &&
                        saRow <= screen52.getRows() &&
                        saCol >= 0 &&
                        saCol <= screen52.getCols()) {
                     screen52.goto_XY(saRow,saCol); // now set screen position for output

                  }
                  else {

                     sendNegResponse(NR_REQUEST_ERROR,0x05,0x01,0x22,"invalid row/col order" +
                                                                     " saRow = " +
                                                                     saRow +
                                                                     " saRows = " +
                                                                     screen52.getRows() +
                                                                     " saCol = " + saCol);

                     error = true;

                  }
                  break;

               case 18:    // WEA - Extended Attribute
                  bk.getNextByte();
                  bk.getNextByte();
                  break;

               case 19:    // IC - Insert Cursor
               case 20:    // MC - Move Cursor
                  int icX = bk.getNextByte();
                  int icY = bk.getNextByte() & 0xff;
                  if (icX >= 0 &&
                        icX <= saRows &&
                        icY >= 0 &&
                        icY <= saCols)

//                     System.out.println(" IC " + icX + " " + icY);
                     screen52.setPendingInsert(true,icX,icY);
                  else {
                     sendNegResponse(NR_REQUEST_ERROR,0x05,0x01,0x22," IC/IM position invalid ");
                     error = true;
                  }

                  break;

               case 21:    // WTDSF - Write To Display Structured Field order
                  error = writeToDisplayStructuredField();
                  break;

               case 29:    // SF - Start of Field
                  int fcw1 = 0;
                  int fcw2 = 0;
                  int ffw1 = 0;
                  int ffw0 = bk.getNextByte();   // FFW

                  if (!isAttribute(ffw0)) {
                     ffw1 = bk.getNextByte();   // FFW 1

                     fcw1 = bk.getNextByte();   // check for field control word

                     if (!isAttribute(fcw1)) {
                        fcw2 = bk.getNextByte();   // FCW 2
                        attr = bk.getNextByte();   // attribute field

                     }
                     else {
                        attr = fcw1;      // attribute of field
                        fcw1 = 0;
                     }
                  }
                  else {
                     attr = ffw0;
                  }

                  int fLength = (bk.getNextByte() & 0xff) << 8 | bk.getNextByte() & 0xff;
                  screen52.addField(attr,fLength, ffw0,ffw1,fcw1,fcw2);
                  break;

               default:    // all others must be output to screen
                  byte byte0 = bk.getByteOffset(-1);
                  if (isAttribute(byte0)) {
                     screen52.setAttr(byte0);
                  }
                  else {
                     if (!screen52.isStatusErrorCode()) {
                        if (!isData(byte0)) {
//                           if (byte0 == 255) {
//                              sendNegResponse(NR_REQUEST_ERROR,0x05,0x01,0x42,
//                              " Attempt to send FF to screen");
//                           }
//                           else

                           screen52.setChar(byte0);
                        }
                        else
//                           screen52.setChar(getASCIIChar(byte0));
                           screen52.setChar(codePage.ebcdic2uni(byte0));
                     }
                     else {
                        if (byte0 == 0)
                           screen52.setChar(byte0);
                        else
                           screen52.setChar(getASCIIChar(byte0));
                     }
                  }

                  break;
            }

            if (error)
               done = true;
         }
      }

      catch (Exception e) {
         System.out.println("write to display " + e.getMessage());
      };

      processCC1(control1);

      return error;

   }

   private boolean processSOH() throws Exception {

      int l = bk.getNextByte();  // length
//      System.out.println(" byte 0 " + l);

      if (l > 0 && l <= 7) {
         bk.getNextByte(); // flag byte 2
         bk.getNextByte(); // reserved
         bk.getNextByte(); // resequence fields
         screen52.setErrorLine(bk.getNextByte()); // error row

         int byte1 = 0;
         if (l >= 5) {
            byte1 = bk.getNextByte();
            dataIncluded[23] = (byte1 & 0x80) == 0x80;
            dataIncluded[22] = (byte1 & 0x40) == 0x40;
            dataIncluded[21] = (byte1 & 0x20) == 0x20;
            dataIncluded[20] = (byte1 & 0x10) == 0x10;
            dataIncluded[19] = (byte1 & 0x8) == 0x8;
            dataIncluded[18] = (byte1 & 0x4) == 0x4;
            dataIncluded[17] = (byte1 & 0x2) == 0x2;
            dataIncluded[16] = (byte1 & 0x1)  == 0x1;
         }

         if (l >= 6) {
            byte1 = bk.getNextByte();
            dataIncluded[15] = (byte1 & 0x80) == 0x80;
            dataIncluded[14] = (byte1 & 0x40) == 0x40;
            dataIncluded[13] = (byte1 & 0x20) == 0x20;
            dataIncluded[12] = (byte1 & 0x10) == 0x10;
            dataIncluded[11] = (byte1 & 0x8) == 0x8;
            dataIncluded[10] = (byte1 & 0x4) == 0x4;
            dataIncluded[9] = (byte1 & 0x2) == 0x2;
            dataIncluded[8] = (byte1 & 0x1) == 0x1;
         }

         if (l >= 7) {
            byte1 = bk.getNextByte();
            dataIncluded[7] = (byte1 & 0x80) == 0x80;
            dataIncluded[6] = (byte1 & 0x40) == 0x40;
            dataIncluded[5] = (byte1 & 0x20) == 0x20;
            dataIncluded[4] = (byte1 & 0x10) == 0x10;
            dataIncluded[3] = (byte1 & 0x8) == 0x8;
            dataIncluded[2] = (byte1 & 0x4) == 0x4;
            dataIncluded[1] = (byte1 & 0x2) == 0x2;
            dataIncluded[0] = (byte1 & 0x1) == 0x1;
         }

//         if (l >= 5)
//            System.out.println(" byte 5 " + Integer.toBinaryString(bk.getNextByte()));
//         if (l >= 6)
//            System.out.println(" byte 6 " + Integer.toBinaryString(bk.getNextByte()));
//         if (l == 7)
//            System.out.println(" byte 7 " + Integer.toBinaryString(bk.getNextByte()));

         screen52.clearTable();
         return false;
      }
      else {
         sendNegResponse(NR_REQUEST_ERROR,0x05,0x01,0x2B,"invalid SOH length");
         return true;
      }

   }

   private void processCC0 (byte byte0) {
//      System.out.println(" Control byte0 " + Integer.toBinaryString(byte0 & 0xff));
      boolean lockKeyboard = true;
      boolean resetMDT=false;
      boolean resetMDTAll = false;
      boolean nullMDT = false;
      boolean nullAll = false;

      // Bits 3 to 6 are reserved and should be set to '0000'
      // 0xE0 = '11100000' - only the first 3 bits are tested
      if ((byte0 & 0xE0) == 0x00) {
         lockKeyboard = false;
      }

      // '00100000' = 0x20 /32 -- just lock keyboard
      // '01000000' = 0x40 /64
      // '01100000' = 0x60 /96
      // '10000000' = 0x80 /128
      // '10100000' = 0xA0 /160
      // '11000000' = 0xC0 /192
      // '11100000' = 0xE0 /224

      switch (byte0 & 0xE0) {

         case 0x40:
            resetMDT = true;
            break;
         case 0x60:
            resetMDTAll = true;
            break;
         case 0x80:
            nullMDT = true;
            break;
         case 0xA0:
            resetMDT = true;
            nullAll = true;
            break;
         case 0xC0:
            resetMDT = true;
            nullMDT = true;
            break;

         case 0xE0:
            resetMDTAll = true;
            nullAll = true;
            break;


      }

      if (lockKeyboard) {
         screen52.setKeyboardLocked(true);
      }

      if (resetMDT ||
            resetMDTAll ||
            nullMDT ||
            nullAll) {
         ScreenField sf;

         int f = screen52.getScreenFields().getSize();
         for (int x = 0; x < f; x++) {
            sf = screen52.getScreenFields().getField(x);

            if (!sf.isBypassField()) {
               if ((nullMDT && sf.mdt) || nullAll ) {
                  sf.setFieldChar((char)0x0);
                  screen52.drawField(sf);
               }
            }
            if (resetMDTAll || (resetMDT && !sf.isBypassField()))
                  sf.resetMDT();

         }
         sf = null;
      }

   }

   private void processCC1 (byte byte1) {
//      System.out.println(" Control byte1 " + Integer.toBinaryString(byte1 & 0xff));

      if ((byte1 & 0x04) == 0x04) {
         Toolkit.getDefaultToolkit().beep();
      }
      if ((byte1 & 0x02) == 0x02) {
         screen52.setMessageLightOff();
      }
      if ((byte1 & 0x01) == 0x01) {
         screen52.setMessageLightOn();
      }
      if ((byte1 & 0x01) == 0x01 && (byte1 & 0x02) == 0x02) {
         screen52.setMessageLightOn();
      }

      // reset blinking cursor seems to control whether to set or not set the
      // the cursor position.  No documentation for this just testing and
      // looking at the bit settings of this field.  This was a pain in the ass!
      //
      // if it is off '0' then keep existing cursor positioning information
      // if it is on '1' then reset the cursor positioning information
      // *** Note *** unless we receive bit 4 on at the same time
      // this seems to work so far
      if ((byte1 & 0x20) == 0x20 && (byte1 & 0x08) == 0x00) {
         screen52.setPendingInsert(false,1,1);
      }

      // in enhanced mode we sometimes only receive bit 6 turned on which
      // is reset blinking cursor
      if ((byte1 & 0x20) == 0x20 && enhanced) {
         cursorOn = true;
      }

      if (!screen52.isStatusErrorCode() && (byte1 & 0x08) == 0x08) {

//         screen52.setStatus(screen52.STATUS_SYSTEM,screen52.STATUS_VALUE_OFF,null);
         cursorOn = true;
      }

      if ((byte1 & 0x20) == 0x20 && (byte1 & 0x08) == 0x00) {
         screen52.setPendingInsert(false,1,1);
      }

   }

   private boolean isAttribute(int byte0) {
      int byte1 = byte0 & 0xff;
      return (byte1 & 0xe0) == 0x20;
   }

   private boolean isData(int byte0) {
      int byte1 = byte0 & 0xff;
      // here it should always be less than 255
      if (byte1 >= 64 && byte1 < 255)

         return true;
      else
         return false;

   }

   private boolean writeToDisplayStructuredField() {

      boolean error = false;
      boolean done = false;
      int nextone;
      try {
         int length = (( bk.getNextByte() & 0xff )<< 8 | (bk.getNextByte() & 0xff));

         while (!done) {
            int s =    bk.getNextByte() & 0xff;
            switch (s) {

               case 0xD9:     // Class Type 0xD9 - Create Window

                  switch (bk.getNextByte()) {
//                     case 0x50:      // Define Selection Field
//
//                        defineSelectionField(length);
//                        done = true;
//                        break;
                     case 0x51:      // Create Window

                        boolean cr = false;
                        int rows = 0;
                        int cols = 0;
                        // pull down not supported yet
                        if ((bk.getNextByte() & 0x80) == 0x80)
                           cr = true;
                        bk.getNextByte(); // get reserved field pos 6
                        bk.getNextByte(); // get reserved field pos 7
                        rows = bk.getNextByte(); // get window depth rows pos 8
                        cols = bk.getNextByte(); // get window width cols pos 9
                        length -= 9;
                        if (length == 0) {
                           done = true;
//                           System.out.println("Create Window");
//                           System.out.println("   restrict cursor " + cr);
//                           System.out.println(" Depth = " + rows + " Width = " + cols);
                           screen52.createWindow(rows,cols,1,true,32,58,
                                             '.',
                                             '.',
                                             '.',
                                             ':',
                                             ':',
                                             ':',
                                             '.',
                                             ':');

                           break;
                        }

                        // pos 10 is Minor Structure
                        int ml = 0;
                        int type = 0;
                        int lastPos = screen52.getLastPos();
                        int mAttr = 0;
                        int cAttr = 0;

                        while (length > 0) {

                           // get minor length
                           ml = ( bk.getNextByte() & 0xff );
                           length -= ml;

                           // only normal windows are supported at this time
                           type = bk.getNextByte();

                           switch (type) {

                              case 0x01 : // Border presentation
                                 boolean gui = false;
                                 if ((bk.getNextByte() & 0x80) == 0x80)
                                    gui = true;
                                 mAttr = bk.getNextByte();
                                 cAttr = bk.getNextByte();

                                 char ul = '.';
                                 char upper = '.';
                                 char ur = '.';
                                 char left = ':';
                                 char right = ':';
                                 char ll = ':';
                                 char bottom = '.';
                                 char lr = ':';

                                 // if minor length is greater than 5 then
                                 //    the border characters are specified
                                 if (ml > 5) {
                                    ul = getASCIIChar(bk.getNextByte());
                                    if (ul == 0)
                                       ul = '.';

                                    upper = getASCIIChar(bk.getNextByte());
                                    if (upper == 0)
                                       upper = '.';

                                    ur = getASCIIChar(bk.getNextByte());
                                    if (ur == 0)
                                       ur = '.';

                                    left = getASCIIChar(bk.getNextByte());
                                    if (left == 0)
                                       left = ':';

                                    right = getASCIIChar(bk.getNextByte());
                                    if (right == 0)
                                       right = ':';

                                    ll = getASCIIChar(bk.getNextByte());
                                    if (ll == 0)
                                       ll = ':';

                                    bottom = getASCIIChar(bk.getNextByte());
                                    if (bottom == 0)
                                       bottom = '.';

                                    lr = getASCIIChar(bk.getNextByte());
                                    if (lr == 0)
                                       lr = ':';
                                 }

//                                 System.out.println("Create Window");
//                                 System.out.println("   restrict cursor " + cr);
//                                 System.out.println("   Depth = " + rows + " Width = " + cols);
//                                 System.out.println("   type = " + type + " gui = " + gui);
//                                 System.out.println("   mono attr = " + mAttr + " color attr = " + cAttr);
//                                 System.out.println("   ul = " + ul + " upper = " + upper +
//                                                         " ur = " + ur +
//                                                         " left = " + left +
//                                                         " right = " + right +
//                                                         " ll = " + ll +
//                                                         " bottom = " + bottom +
//                                                         " lr = " + lr
//                                                         );
                                 screen52.createWindow(rows,cols,type,gui,mAttr,cAttr,
                                                      ul,
                                                      upper,
                                                      ur,
                                                      left,
                                                      right,
                                                      ll,
                                                      bottom,
                                                      lr);
                              break;
         //
         //  The following shows the input for window with a title
         //
         //      +0000 019A12A0 00000400 00020411 00200107  .ª.µ..œ...œ..€.
         //      +0010 00000018 00000011 06131500 37D95180  ........†.…..RéØ
         //      +0020 00000A24 0D018023 23404040 40404040  ..Ž„..Øƒƒ
         //      +0030 40211000 000000D7 C2C1D9C4 C5D4D67A   \uFFFD.....PBARDEMO:
         //      +0040 40D79996 879985A2 A2408281 99408485   Progress bar de
         //      +0050 94961108 1520D5A4 94828599 40968640  mo.—…€Number of
         //      +0060 8595A399 8985A24B 4B4B4B4B 4B7A2011  entries......:€.
         //      +0070 082E2040 404040F5 F0F06BF0 F0F02011  —.€    500,000€.
         //      +0080 091520C3 A4999985 95A34085 95A399A8  \uFFFD…€Current entry
         //      +0090 4095A494 8285994B 4B4B7A20 11092E20   number...:€.\uFFFD.€
         //      +00A0 40404040 4040F56B F0F0F020 110A1520        5,000€.Ž…€
         //      +00B0 D9859481 89958995 87408595 A3998985  Remaining entrie
         //      +00C0 A24B4B4B 4B4B4B7A 20110A2E 20404040  s......:€.Ž.€
         //      +00D0 40F4F9F5 6BF0F0F0 20110C15 20E2A381   495,000€..…€Sta
         //      +00E0 99A340A3 8994854B 4B4B4B4B 4B4B4B4B  rt time.........
         //      +00F0 4B4B4B4B 7A20110C 2F2040F7 7AF5F37A  ....:€...€ 7:53:

                              case 0x10 : // Window title/footer
                                 byte orientation = bk.getNextByte();
                                 mAttr = bk.getNextByte();
                                 cAttr = bk.getNextByte();

                                 //reserved
                                 bk.getNextByte();
                                 ml -= 6;

                                 StringBuffer hfBuffer = new StringBuffer(ml);
                                 while (ml-- > 0) {
                                    hfBuffer.append(getASCIIChar(bk.getNextByte()));

                                 }

                                 System.out.println(
                                    " orientation " + Integer.toBinaryString(orientation) +
                                    " mAttr " + mAttr +
                                    " cAttr " + cAttr +
                                    " Header/Footer " + hfBuffer);
                                 screen52.writeWindowTitle(lastPos,
                                                            rows,
                                                            cols,
                                                            orientation,
                                                            mAttr,
                                                            cAttr,
                                                            hfBuffer);
                                 break;
                              default:
                              System.out.println("Invalid Window minor structure");
                              length = 0;
                              done = true;
                           }

                        }

                        done = true;

                        break;

                     case 0x53:      // Scroll Bar
                        int sblen = 15;
                        byte sbflag = bk.getNextByte();  // flag position 5

                        bk.getNextByte();  // reserved position 6

                        // position 7,8
                        int totalRowScrollable =  (( bk.getNextByte() & 0xff )<< 8
                                                | (bk.getNextByte() & 0xff));

                        // position 9,10
                        int totalColScrollable =  (( bk.getNextByte() & 0xff )<< 8
                                                | (bk.getNextByte() & 0xff));

                        // position 11,12
                        int sliderRowPos =  (( bk.getNextByte() & 0xff )<< 8
                                                | (bk.getNextByte() & 0xff));

                        // position 13,14
                        int sliderColPos =  (( bk.getNextByte() & 0xff )<< 8
                                                | (bk.getNextByte() & 0xff));

                        // position 15
                        int sliderRC = bk.getNextByte();

                        screen52.createScrollBar(sbflag,totalRowScrollable,
                                                   totalColScrollable,
                                                   sliderRowPos,
                                                   sliderColPos,
                                                   sliderRC);
                        length -= 15;

                        done = true;

                        break;

                     case 0x5B:      // Remove GUI ScrollBar field

                        bk.getNextByte(); // reserved must be set to off pos 5
                        bk.getNextByte(); // reserved must be set to zero pos 6

                        done = true;
                        break;

                     case 0x5F:      // Remove All GUI Constructs
//                        System.out.println("remove all gui contructs");
                        int len = 4;
                        int d = 0;
                        length -= s;
                        while (--len > 0)
                           d = bk.getNextByte();
//                        if (length > 0) {
//                           len = (bk.getNextByte() & 0xff )<< 8;
//
//                           while (--len > 0)
//                              d = bk.getNextByte();
//                        }

                        // per 14.6.13.4 documentation we should clear the
                        //    format table after this command
                        screen52.clearTable();
                        done = true;
                        break;

                     case 0x60:      // Erase/Draw Grid Lines - not supported
                                    // do not know what they are
                                    // as of 03/11/2002 we should not be getting
                                    // this anymore but I will leave it here
                                    //  just in case.
//                        System.out.println("erase/draw grid lines " + length);
                        len = 6;
                        d = 0;
                        length -= 9;
                        while (--len > 0)
                           d = bk.getNextByte();
                           if (length > 0) {
                              len = (bk.getNextByte() & 0xff )<< 8;

                           while (--len > 0) {
                              d = bk.getNextByte();
                           }
                        }
                        done = true;
                        break;
                     default:
                        sendNegResponse(NR_REQUEST_ERROR,0x03,0x01,0x01,"invalid wtd structured field sub command "
                                                   + bk.getByteOffset(-1));
                        error = true;
                        break;
                  }
                  break;

               default:
                  sendNegResponse(NR_REQUEST_ERROR,0x03,0x01,0x01,
                              "invalid wtd structured field command "
                               + bk.getByteOffset(-1));
                  error = true;
                  break;
            }

            if (error)
               done = true;

         }
      }
      catch (Exception e) {};

      return error;

   }

   private void defineSelectionField(int majLen) {

      //   0030:  20 00 2C 3E 00 00 00 69 12 A0 00 00 04 00 00 03  .,>...i........
      //   0040:  04 40 04 11 00 28 01 07 00 00 00 19 00 00 04 11 .@...(..........
      //   0050:  14 19 15 00 48 D9 50 00 60 00 11 01 84 84 00 00 ....H.P.`.......
      //   0060:  05 03 01 01 00 00 00 13 01 E0 00 21 00 21 00 3B ...........!.!.;
      //   0070:  22 20 20 20 20 3A 24 20 20 3A 0B 10 08 00 E0 00 "    :$  :......
      //   0080:  D6 95 85 40 40 0B 10 08 00 E0 00 E3 A6 96 40 40 ...@@.........@@
      //   0090:  0B 10 08 00 E0 00 E3 88 99 85 85 04 52 00 00 FF ............R...
      //   00A0:  EF                                              .
      try {
         int flag1 = bk.getNextByte();    // Flag byte 1 - byte 5
         int flag2 = bk.getNextByte();    // Flag byte 2 - byte 6
         int flag3 = bk.getNextByte();    // Flag byte 3 - byte 7
         int typeSelection = bk.getNextByte();    // Type of selection Field - byte 8

         // GUI Device Characteristics:
         //    This byte is used if the target device is a GUI PWS or a GUI-like
         //    NWS.  If neigher of these WS are the targets, this byte is ignored
         int guiDevice = bk.getNextByte();    // byte 9
         int withMnemonic = bk.getNextByte();    //  byte 10
         int noMnemonic = bk.getNextByte();    // byte 11

         bk.getNextByte();    // Reserved - byte 12
         bk.getNextByte();    // Reserved - byte 13

         int cols = bk.getNextByte();    // Text Size - byte 14
         int rows = bk.getNextByte();    // Rows - byte 15

         int maxColChoice = bk.getNextByte();    // byte 16
         int padding = bk.getNextByte();    // byte 17
         int numSepChar = bk.getNextByte();    // byte 18
         int ctySepChar = bk.getNextByte();    // byte 19
         int cancelAID = bk.getNextByte();    // byte 20

         int cnt = 0;
         int minLen = 0;
         majLen -= 21;
         System.out.println(" row: " + screen52.getCurrentRow()
                              + " col: " + screen52.getCurrentCol()
                              + " type " + typeSelection
                              + " gui " + guiDevice
                              + "withMnemonic " + withMnemonic
                              + " cols " + cols
                              + " rows " + rows);
         do {
            minLen = bk.getNextByte();    // Minor Length byte 21

            int minType = bk.getNextByte();    // Minor Type

            switch (minType) {

               case 0x01:  // Choice Presentation Display

                  // flag
                  int flagCP1 = bk.getNextByte();

                  bk.getNextByte(); // mon select cursor avail emphasis - byte4
                  int colSelAvail = bk.getNextByte();  // -byte 5

                  bk.getNextByte(); // mon select cursor - byte 6
                  int colSelCur = bk.getNextByte();  // -byte 7

                  bk.getNextByte(); // mon select cursor not avail emphasis - byte 8
                  int colSelNotAvail = bk.getNextByte();  // -byte 9

                  bk.getNextByte(); // mon avail emphasis - byte 10
                  int colAvail = bk.getNextByte();  // -byte 11

                  bk.getNextByte(); // mon select emphasis - byte 12
                  int colSel = bk.getNextByte();  // -byte 13

                  bk.getNextByte(); // mon not avail emphasis - byte 14
                  int colNotAvail = bk.getNextByte();  // -byte 15

                  bk.getNextByte(); // mon indicator emphasis - byte 16
                  int colInd = bk.getNextByte();  // -byte 17

                  bk.getNextByte(); // mon indicator not avail emphasis - byte 18
                  int colNotAvailInd = bk.getNextByte();  // -byte 19

                  break;

               case 0x10:  // Choice Text minor structure

                  cnt = 5;
                  int flagCT1 = bk.getNextByte();
                  int flagCT2 = bk.getNextByte();
                  int flagCT3 = bk.getNextByte();
                  int mnemOffset = 0;
                  boolean aid = false;

                  // is mnemonic offset specified
                  if ((flagCT1 & 0x08) == 8) {
                     System.out.println(" mnemOffset " + mnemOffset);
                     mnemOffset = bk.getNextByte();
                     cnt++;
                  }

                  // is aid key specified
                  if ((flagCT1 & 0x04) == 4) {

                     aid = true;
                     System.out.println(" aidKey " + aid);
//                     cnt++;
                  }

                  // is single digit number specified
                  if ((flagCT1 & 0x01) == 0x01) {
                     System.out.println(" single digit " );
                     bk.getNextByte();
                     cnt++;
                  }

                  // is double digint number specified
                  if ((flagCT1 & 0x02) == 0x02) {
                     System.out.println(" double digit " );

                     bk.getNextByte();
                     cnt++;
                  }

                  String s = "";
                  byte byte0 = 0;
                  for (;cnt < minLen; cnt++) {

                     byte0 = bk.getNextByte();
                     s += ebcdic2uni(byte0);
                     screen52.setChar(ebcdic2uni(byte0));

                  }
                  System.out.println(s);
                  break;
               default:
                  for (cnt = 2;cnt < minLen; cnt++) {

                     bk.getNextByte();
                  }

            }

            majLen -= minLen;

         }  while (majLen > 0);
      }
      catch (Exception exc) {
         System.out.println(" defineSelectionField :" + exc.getMessage());
         exc.printStackTrace();
      }
   }

   private void writeStructuredField() {

      boolean done = false;
      int nextone;
      try {
         int length = (( bk.getNextByte() & 0xff )<< 8 | (bk.getNextByte() & 0xff));
         while (bk.hasNext() && !done) {
            switch (bk.getNextByte()) {

               case -39:     // SOH - Start of Header Order

                  switch (bk.getNextByte()) {
                     case 112:      // 5250 Query
                        bk.getNextByte(); // get null required field
                        sendQueryResponse();
                        break;
                     default:
                        System.out.println("invalid structured field sub command " + bk.getByteOffset(-1));
                        break;
                  }
                  break;
               default:
                  System.out.println("invalid structured field command " + bk.getByteOffset(-1));
                  break;
            }
         }
      }
      catch (Exception e) {};

   }

   private final void writeErrorCode() throws Exception {
      screen52.goto_XY(screen52.getErrorLine(),1); // Skip the control byte
      screen52.saveErrorLine();
      screen52.setStatus(screen52.STATUS_ERROR_CODE,screen52.STATUS_VALUE_ON,null);
      cursorOn = true;

   }

   private final void writeErrorCodeToWindow() throws Exception {
      int fromCol = bk.getNextByte() & 0xff;  // from column
      int toCol = bk.getNextByte() & 0xff;  // to column
      screen52.goto_XY(screen52.getErrorLine(),fromCol); // Skip the control byte
      screen52.saveErrorLine();
      screen52.setStatus(screen52.STATUS_ERROR_CODE,screen52.STATUS_VALUE_ON,null);
      cursorOn = true;

   }

   /**
    * Method sendQueryResponse
    *
    * The query command is used to obtain information about the capabilities
    * of the 5250 display.
    *
    * The Query command must follow an Escape (0x04) and Write Structured
    * Field command (0xF3).
    *
    * This section is modeled after the rfc1205 - 5250 Telnet Interface section
    * 5.3
    */
   public final void sendQueryResponse()
         throws IOException {

      System.out.println("sending query response");
      byte abyte0[] = new byte[64];
      abyte0[0] = 0;       // Cursor Row/column (set to zero)
      abyte0[1] = 0;       //           ""
      abyte0[2] = -120;    // X'88' inbound write structure Field aid
      if (enhanced == true) {
         abyte0[3] = 0;       // 0x003D (61) length of query response
         abyte0[4] = 64;      //       ""  see note below ?????????
      }
      else {
         abyte0[3] = 0;       // 0x003A (58) length of query response
         abyte0[4] = 58;      //       ""
                              //  the length between 58 and 64 seems to cause
                              //  different formatting codes to be sent from
                              //  the host ???????????????? why ???????
                              //    Well the why can be found in the manual if
                              //       read a little more ;-)
      }
      abyte0[5] = -39;     // command class 0xD9
      abyte0[6] = 112;     // Command type query 0x70
      abyte0[7] = -128;    // 0x80 Flag byte
      abyte0[8] = 6;       // Controller Hardware Class
      abyte0[9] = 0;       // 0x0600 - Other WSF or another 5250 Emulator
      abyte0[10] = 1;      // Controller Code Level
      abyte0[11] = 1;      //    Version 1 Rel 1.0
      abyte0[12] = 0;      //       ""

      abyte0[13] = 0;      // 13 - 28 are reserved so set to 0x00
      abyte0[14] = 0;      //       ""
      abyte0[15] = 0;      //       ""
      abyte0[16] = 0;      //       ""
      abyte0[17] = 0;      //       ""
      abyte0[18] = 0;      //       ""
      abyte0[19] = 0;      //       ""
      abyte0[20] = 0;      //       ""
      abyte0[21] = 0;      //       ""
      abyte0[22] = 0;      //       ""
      abyte0[23] = 0;      //       ""
      abyte0[24] = 0;      //       ""
      abyte0[25] = 0;      //       ""
      abyte0[26] = 0;      //       ""
      abyte0[27] = 0;      //       ""
      abyte0[28] = 0;      //       ""
      abyte0[29] = 1;      // Device type - 0x01 5250 Emulator
      abyte0[30] = getEBCDIC('5');  // Device type character
      abyte0[31] = getEBCDIC('2');  //          ""
      abyte0[32] = getEBCDIC('5');  //          ""
      abyte0[33] = getEBCDIC('1');  //          ""
      abyte0[34] = getEBCDIC('0');  //          ""
      abyte0[35] = getEBCDIC('1');  //          ""
      abyte0[36] = getEBCDIC('1');  //          ""

      abyte0[37] = 2;      // Keyboard Id - 0x02 Standard Keyboard
      abyte0[38] = 0;      // extended keyboard id
      abyte0[39] = 0;      // reserved

      abyte0[40] = 0;      // 40 - 43 Display Serial Number
      abyte0[41] = 36;     //
      abyte0[42] = 36;     //
      abyte0[43] = 0;      //

      abyte0[44] = 1;      // Maximum number of display fields - 256
      abyte0[45] = 0;      // 0x0100
      abyte0[46] = 0;      // 46 -48 Reserved set to 0x00
      abyte0[47] = 0;
      abyte0[48] = 0;
      abyte0[49] = 1;      // 49 - 53 Controller Display Capability
      abyte0[50] = 16;     //      see rfc - tired of typing :-)
      abyte0[51] = 0;      //          ""
      abyte0[52] = 0;      //          ""

      //  53
      //    Bit 0-2: B'000'   -  no graphics capability
      //             B'001'   - 5292-2 style graphics
      //    Bit 3-7: B '00000' = reserved (it seems for Client access)

      if (enhanced == true) {
         abyte0[53] = 0x5E;      //  0x5E turns on ehnhanced mode
         System.out.println("enhanced options");
      }
      else
         abyte0[53] = 0x0;      //  0x0 is normal emulation

      abyte0[54] = 24;      // 54 - 60 Reserved set to 0x00
                            //  54 - I found out is used for enhanced user
                            //       interface level 3.  Bit 4 allows headers
                            //       and footers for windows
      abyte0[55] = 0;
      abyte0[56] = 0;
      abyte0[57] = 0;
      abyte0[58] = 0;
      abyte0[59] = 0;
      abyte0[60] = 0;
      abyte0[61] = 0;      // gridlines are not supported
      abyte0[62] = 0;      // gridlines are not supported
      abyte0[63] = 0;
      writeGDS(0, 0, abyte0); // now tell them about us
      abyte0 = null;

   }

   protected final boolean negotiate(byte abyte0[]) throws IOException {
      int i = 0;


      // from server negotiations
      if(abyte0[i] == IAC) { // -1

         while(i < abyte0.length && abyte0[i++] == -1)
            switch(abyte0[i++]) {

               // we will not worry about what it WONT do
               case WONT:            // -4
               default:
                 break;

               case DO: //-3

                  switch(abyte0[i]) {
                     case TERMINAL_TYPE: // 24
                        baosp.write(IAC);
                        baosp.write(WILL);
                        baosp.write(TERMINAL_TYPE);
                        writeByte(baosp.toByteArray());
                        baosp.reset();

                        break;

                    case OPT_END_OF_RECORD: // 25

                        baosp.write(IAC);
                        baosp.write(WILL);
                        baosp.write(OPT_END_OF_RECORD);
                        writeByte(baosp.toByteArray());
                        baosp.reset();
                        break;

                    case TRANSMIT_BINARY: // 0

                        baosp.write(IAC);
                        baosp.write(WILL);
                        baosp.write(TRANSMIT_BINARY);
                        writeByte(baosp.toByteArray());
                        baosp.reset();

                        break;

                    case TIMING_MARK: // 6   rfc860
//                        System.out.println("Timing Mark Received and notifying " +
//                        "the server that we will not do it");
                        baosp.write(IAC);
                        baosp.write(WONT);
                        baosp.write(TIMING_MARK);
                        writeByte(baosp.toByteArray());
                        baosp.reset();

                        break;

                    case NEW_ENVIRONMENT: // 39 rfc1572
                        if (devName == null) {
                           baosp.write(IAC);
                           baosp.write(WONT);
                           baosp.write(NEW_ENVIRONMENT);
                           writeByte(baosp.toByteArray());
                           baosp.reset();

                        }
                        else {
                           System.out.println(devName);
                           baosp.write(IAC);
                           baosp.write(WILL);
                           baosp.write(NEW_ENVIRONMENT);
                           writeByte(baosp.toByteArray());
                           baosp.reset();

                        }
                        break;

                    default:  // every thing else we will not do at this time
                        baosp.write(IAC);
                        baosp.write(WONT);
                        baosp.write(abyte0[i]); // either
                        writeByte(baosp.toByteArray());
                        baosp.reset();

                        break;
                 }

                 i++;
                 break;

               case WILL:

                 switch(abyte0[i]) {
                    case OPT_END_OF_RECORD: // 25
                        baosp.write(IAC);
                        baosp.write(DO);
                        baosp.write(OPT_END_OF_RECORD);
                        writeByte(baosp.toByteArray());
                        baosp.reset();

                        break;

                    case TRANSMIT_BINARY: // '\0'
                        baosp.write(IAC);
                        baosp.write(DO);
                        baosp.write(TRANSMIT_BINARY);
                        writeByte(baosp.toByteArray());
                        baosp.reset();

                        break;
                 }
                 i++;
                 break;

               case SB: // -6

                  if(abyte0[i] == NEW_ENVIRONMENT && abyte0[i + 1] == 1) {
                     negNewEnvironment();

                     i++;
                  }

                  if(abyte0[i] == TERMINAL_TYPE && abyte0[i + 1] == 1) {
                     baosp.write(IAC);
                     baosp.write(SB);
                     baosp.write(TERMINAL_TYPE);
                     baosp.write(QUAL_IS);
                     if(!support132)
                         baosp.write((new String("IBM-3179-2")).getBytes());
                     else
                         baosp.write((new String("IBM-3477-FC")).getBytes());
                     baosp.write(IAC);
                     baosp.write(SE);
                     writeByte(baosp.toByteArray());
                     baosp.reset();

                     i++;
                  }
                  i++;
                  break;
            }
            return true;
      }
      else {
         return false;
      }
   }

   /**
    * Negotiate new environment string for device name
    */
   private void negNewEnvironment()  throws IOException {


      baosp.write(IAC);
      baosp.write(SB);
      baosp.write(NEW_ENVIRONMENT);
      baosp.write(IS);
      baosp.write(USERVAR);

      baosp.write((new String("DEVNAME")).getBytes());

      baosp.write(VALUE);

      baosp.write(negDeviceName().getBytes());

      baosp.write(IAC);
      baosp.write(SE);
      writeByte(baosp.toByteArray());
      baosp.reset();

   }

   /**
    * This will negotiate a device name with controller.
    *    if the sequence is less than zero then it will send the device name
    *    as specified.  On each unsuccessful attempt a sequential number is
    *    appended until we find one or the controller says no way.
    */
   private String negDeviceName() {

      if (devSeq++ == -1) {
         devNameUsed = devName;
         return devName;
      }
      else {
         StringBuffer sb = new StringBuffer(devName + devSeq);
         int ei = 1;
         while (sb.length() > 10) {

            sb.setLength(0);
            sb.append(devName.substring(0,devName.length() - ei++));
            sb.append(devSeq);

         }
         devNameUsed = sb.toString();
         return devNameUsed;
      }
   }

   public final void setCodePage(String cp) {

      if (this.codePage == null) {
         codePage = new CodePage(cp);
      }
      else {

         codePage.setCodePage(cp);

      }

   }

   public final CodePage getCodePage() {

      return codePage;
   }

   public final Dimension getPreferredSize() {
      return screen52.getPreferredSize();
   }

   public byte getEBCDIC(int index) {
      return codePage.getEBCDIC(index);

   }

   public char getEBCDICChar(int index) {
      return codePage.getEBCDICChar(index);

   }

   public byte getASCII(int index) {
      return codePage.getASCII(index);

   }

   public char getASCIIChar(int index) {
      return codePage.getASCIIChar(index);
   }

   public char ebcdic2uni(int index) {
      return codePage.ebcdic2uni(index);

   }

   public byte uni2ebcdic(char index) {
      return codePage.uni2ebcdic(index);

   }

   public void dumpScreen () {

      for (int y = 0;y < screen52.getRows(); y++) {
         System.out.print("row :" + (y + 1) + " ");

         for (int x = 0; x < screen52.getCols(); x++) {
            System.out.println("row " + (y + 1) + " col " + (x + 1) + " " + screen52.screen[y * x].toString());

         }
      }
   }

   public void dump (byte[] abyte0) {
      try {

         System.out.print("\n Buffer Dump of data from AS400: ");
         dw.write("\r\n Buffer Dump of data from AS400: ".getBytes());

         StringBuffer h = new StringBuffer();
         for (int x = 0; x < abyte0.length; x++) {
            if (x % 16 == 0) {
               System.out.println("  " + h.toString());
               dw.write(("  " + h.toString() + "\r\n").getBytes());

               h.setLength(0);
               h.append("+0000");
               h.setLength(5 - Integer.toHexString(x).length());
               h.append(Integer.toHexString(x).toUpperCase());

               System.out.print(h.toString());
               dw.write(h.toString().getBytes());

               h.setLength(0);
            }
            char ac = getASCIIChar(abyte0[x]);
            if (ac < ' ')
               h.append('.');
            else
               h.append(ac);
            if (x % 4 == 0) {
               System.out.print(" ");
               dw.write((" ").getBytes());

            }

            if (Integer.toHexString(abyte0[x] & 0xff).length() == 1){
               System.out.print("0" + Integer.toHexString(abyte0[x] & 0xff).toUpperCase());
               dw.write(("0" + Integer.toHexString(abyte0[x] & 0xff).toUpperCase()).getBytes());

            }
            else {
               System.out.print(Integer.toHexString(abyte0[x] & 0xff).toUpperCase());
               dw.write((Integer.toHexString(abyte0[x] & 0xff).toUpperCase()).getBytes());
            }

         }
         System.out.println();
         dw.write("\r\n".getBytes());

         dw.flush();
//         dw.close();
      }
      catch(EOFException _ex) { }
      catch(Exception _ex) {
         System.out.println("Cannot dump from host\n\r");
      }

   }

   public void dumpBytes() {
      byte shit[] = bk.buffer;
      for (int i = 0;i < shit.length;i++)
         System.out.println(i + ">" + shit[i] + "< - ascii - >" + getASCIIChar(shit[i]) + "<");
   }

   public void dumpHexBytes(byte[] abyte) {
      byte shit[] = abyte;
      for (int i = 0;i < shit.length;i++)
         System.out.println(i + ">" + shit[i] + "< hex >" + Integer.toHexString((shit[i] & 0xff)));
   }

   // negotiating commands
   private static final byte IAC = (byte)-1; // 255  FF
   private static final byte DONT = (byte)-2; //254  FE
   private static final byte DO = (byte)-3; //253    FD
   private static final byte WONT = (byte)-4; //252  FC
   private static final byte WILL = (byte)-5; //251  FB
   private static final byte SB = (byte)-6; //250 Sub Begin  FA
   private static final byte SE = (byte)-16; //240 Sub End   F0
   private static final byte EOR = (byte)-17; //239 End of Record  EF
   private static final byte TERMINAL_TYPE = (byte)24;     // 18
   private static final byte OPT_END_OF_RECORD = (byte)25;  // 19
   private static final byte TRANSMIT_BINARY = (byte)0;     // 0
   private static final byte QUAL_IS = (byte)0;             // 0
   private static final byte TIMING_MARK = (byte)6;         // 6
   private static final byte NEW_ENVIRONMENT = (byte)39;         // 27
   private static final byte IS = (byte)0;         // 0
   private static final byte SEND = (byte)1;         // 1
   private static final byte INFO = (byte)2;         // 2
   private static final byte VAR = (byte)0;         // 0
   private static final byte VALUE = (byte)1;         // 1
   private static final byte NEGOTIATE_ESC = (byte)2;         // 2
   private static final byte USERVAR = (byte)3;         // 3

   // miscellaneous
   private static final byte ESC = 0x04; // 04
   private static final char char0 = 0;

//   private static final byte CMD_READ_IMMEDIATE_ALT = (byte)0x83; // 131


}