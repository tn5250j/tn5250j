package org.tn5250j;

import java.io.*;
import java.net.*;

public class DataStreamProducer implements Runnable {

   private BufferedInputStream bin;
   private ByteArrayOutputStream baosin;
   private Thread me;
   private byte[] saveStream;
   private DataStreamQueue dsq;
   private tnvt vt;
//   private boolean waitingForInput;
//   private boolean invited;
   private byte[] abyte2;

   public DataStreamProducer(tnvt vt, BufferedInputStream in, DataStreamQueue queue, byte[] init) {
      bin = in;
      this.vt = vt;
      baosin = new ByteArrayOutputStream();
      dsq = queue;
      abyte2 = init;
   }

   public void setInputStream(ByteArrayOutputStream is) {

      baosin = is;

   }

   public void setQueue( DataStreamQueue queue) {

      dsq = queue;

   }

   public final void run() {

      boolean done = false;

      me = Thread.currentThread();

      // load the first response screen
      try {
         loadStream(abyte2, 0);
      }
      catch (IOException ioef) {
         System.out.println(" run() " + ioef.getMessage());
      }
      while (!done) {
         try {

//            waitingForInput = false;

            byte[] abyte0 = readIncoming();

            loadStream(abyte0, 0);

         }
         catch (SocketException se) {
            System.out.println(se.getMessage());
            done = true;
            vt.disconnect();
         }

         catch (IOException ioe) {

//              System.out.println(ioe.getMessage());
//           invited = true;
           if (me.isInterrupted())
              done = true;

         }
         catch (Exception ex) {

           System.out.println(ex.getMessage());
//           invited = true;
           if (me.isInterrupted())
              done = true;

         }
      }
    }

   private final void loadStream(byte abyte0[], int i)
     throws IOException {

      int j = 0;
      int size = 0;
      if (saveStream == null) {
         j = (abyte0[i] & 0xff) << 8 | abyte0[i + 1] & 0xff;
         size = abyte0.length;
      }
      else {
         size = saveStream.length + abyte0.length;
         byte[] inter = new byte[size];
         System.arraycopy(saveStream, 0, inter, 0, saveStream.length);
         System.arraycopy(abyte0, 0, inter, saveStream.length, abyte0.length);
         abyte0 = new byte[size];
         System.arraycopy(inter, 0, abyte0, 0, size);
         saveStream = null;
         inter = null;
         j = (abyte0[i] & 0xff) << 8 | abyte0[i + 1] & 0xff;
//         System.out.println("partial stream found");
      }

      if (j > size) {
         saveStream = new byte[abyte0.length];
         System.arraycopy(abyte0, 0, saveStream, 0, abyte0.length);
//         System.out.println("partial stream saved");
      }
      else {
         byte abyte1[];
         try {
            abyte1 = new byte[j + 2];

            System.arraycopy(abyte0, i, abyte1, 0, j + 2);
            dsq.put(new Stream5250(abyte1));
            if(abyte0.length > abyte1.length + i)
                loadStream(abyte0, i + j + 2);
         }
         catch (Exception ex) {

           System.out.println("load stream error " + ex.getMessage());
   //        ex.printStackTrace();
   //        dump(abyte0);

         }
      }
   }

   public final byte[] readIncoming()
        throws IOException {

      boolean done = false;
      boolean negotiate = false;

      baosin.reset();
      int j = -1;
      int i = 0;

      while(!done) {
         i = bin.read();

         // We use the values instead of the static values IAC and EOR
         //    because they are defined as bytes.
         //
         // The > if(i != 255 || j != 255)  < is a hack for the double FF FF's
         // that are being returned.  I do not know why this is like this and
         // can not find any documentation for it.  It is also being returned
         // on my Client Access tcp dump as well so they are handling it.
         //
         // my5250
         // 0000:  00 50 DA 44 C8 45 42 00 00 00 00 24 08 00 45 00 .P.D.EB....$..E.
         // 0010:  04 2A BC F9 00 00 40 06 D0 27 C1 A8 33 04 C1 A8 .*....@..'..3...
         // 0020:  33 58 00 17 04 18 6F A2 83 CB 00 1E D1 BA 50 18 3X....o.......P.
         // 0030:  20 00 8A 9A 00 00 03 FF FF 12 A0 00 00 04 00 00  ...............
         // --------------------------- || || -------------------------------------
         // 0040:  03 04 40 04 11 00 20 01 07 00 00 00 18 00 00 10 ..@... .........

         if(j == 255 && i == 255) {
            j = -1;
            continue;
         }
         else {
            baosin.write(i);
            // check for end of record EOR and IAC  - FFEF
            if(j == 255 && i == 239)
               done = true;

            // This is to check for the TELNET TIMING MARK OPTION
            // rfc860 explains this in more detail.  When we receive it
            // we will negotiate with the server by sending a WONT'T TIMING-MARK
            // This will let the server know that we processed the information
            // and are just waiting for the user to enter some data so keep the
            // socket alive.   This is more or less a AYT (ARE YOU THERE) or not.
            if(i == 253 && j == 255) {
               done = true;
               negotiate = true;
            }
            j = i;
         }
     }

     // after the initial negotiation we might get other options such as
     //    timing marks ??????????????  do we ???????????? look at telnet spec
     // yes we do. rfc860 explains about timing marks.
         if (negotiate) {
            // get the negotiation option
            baosin.write(bin.read());
            vt.negotiate(baosin.toByteArray());
         }

//         if (dumpBytes) {
//            dump(baosin.toByteArray());
//         }

        return baosin.toByteArray();
    }

//      public void dump (byte[] abyte0) {
//         try {
//
//            System.out.print("\n Buffer Dump of data from AS400: ");
//            dw.write("\r\n Buffer Dump of data from AS400: ".getBytes());
//
//            StringBuffer h = new StringBuffer();
//            for (int x = 0; x < abyte0.length; x++) {
//               if (x % 16 == 0) {
//                  System.out.println("  " + h.toString());
//                  dw.write(("  " + h.toString() + "\r\n").getBytes());
//
//                  h.setLength(0);
//                  h.append("+0000");
//                  h.setLength(5 - Integer.toHexString(x).length());
//                  h.append(Integer.toHexString(x).toUpperCase());
//
//                  System.out.print(h.toString());
//                  dw.write(h.toString().getBytes());
//
//                  h.setLength(0);
//               }
//               char ac = getASCIIChar(abyte0[x]);
//               if (ac < ' ')
//                  h.append('.');
//               else
//                  h.append(ac);
//               if (x % 4 == 0) {
//                  System.out.print(" ");
//                  dw.write((" ").getBytes());
//
//               }
//
//               if (Integer.toHexString(abyte0[x] & 0xff).length() == 1){
//                  System.out.print("0" + Integer.toHexString(abyte0[x] & 0xff).toUpperCase());
//                  dw.write(("0" + Integer.toHexString(abyte0[x] & 0xff).toUpperCase()).getBytes());
//
//               }
//               else {
//                  System.out.print(Integer.toHexString(abyte0[x] & 0xff).toUpperCase());
//                  dw.write((Integer.toHexString(abyte0[x] & 0xff).toUpperCase()).getBytes());
//               }
//
//            }
//            System.out.println();
//            dw.write("\r\n".getBytes());
//
//            dw.flush();
//   //         dw.close();
//         }
//         catch(EOFException _ex) { }
//         catch(Exception _ex) {
//            System.out.println("Cannot dump from host\n\r");
//         }
//
//      }

}