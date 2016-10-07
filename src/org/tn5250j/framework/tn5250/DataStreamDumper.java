/**
 * Title: Screen5250.java
 * Copyright:   Copyright (c) 2015
 * Company:
 *
 * @author Martin W. Kirst
 * <p/>
 * Description:
 * <p/>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 */
package org.tn5250j.framework.tn5250;

import org.tn5250j.encoding.ICodePage;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class DataStreamDumper {

  private AtomicInteger counter = new AtomicInteger(0);

  private FileOutputStream fw;
  private BufferedOutputStream dw;
  private boolean dumpActive = false;
  private ICodePage codePage;

  private TN5250jLogger log = TN5250jLogFactory.getLogger(this.getClass());


  public void toggleDebug(ICodePage cp) {

    if (codePage == null)
      codePage = cp;

    dumpActive = !dumpActive;
    if (dumpActive) {

      try {
        if (fw == null) {
          fw = new FileOutputStream("log.txt");
          dw = new BufferedOutputStream(fw);
        }
      } catch (FileNotFoundException fnfe) {
        log.warn(fnfe.getMessage());
      }

    } else {

      try {

        if (dw != null)
          dw.close();
        if (fw != null)
          fw.close();
        dw = null;
        fw = null;
        codePage = null;
      } catch (IOException ioe) {

        log.warn(ioe.getMessage());
      }
    }

    log.info("Data Stream output is now " + dumpActive);
  }

  public void dump(byte[] abyte0) {
    if (!dumpActive) {
      return;
    }

    try {

      log.info("\n Buffer Dump of data from AS400: ");
      dw.write("\r\n Buffer Dump of data from AS400: ".getBytes());

      StringBuilder h = new StringBuilder();
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
        char ac = codePage.ebcdic2uni(abyte0[x]);
        if (ac < ' ')
          h.append('.');
        else
          h.append(ac);
        if (x % 4 == 0) {
          System.out.print(" ");
          dw.write((" ").getBytes());

        }

        if (Integer.toHexString(abyte0[x] & 0xff).length() == 1) {
          System.out.print("0" + Integer.toHexString(abyte0[x] & 0xff).toUpperCase());
          dw.write(("0" + Integer.toHexString(abyte0[x] & 0xff).toUpperCase()).getBytes());

        } else {
          System.out.print(Integer.toHexString(abyte0[x] & 0xff).toUpperCase());
          dw.write((Integer.toHexString(abyte0[x] & 0xff).toUpperCase()).getBytes());
        }

      }
      System.out.println();
      dw.write("\r\n".getBytes());

      dw.flush();
    } catch (IOException e) {
      log.warn("Cannot dump from host! Message=" + e.getMessage());
    }

  }

  void dumpRaw(byte[] buffer) {
    try {
      String fname = "dump_" + counter.get() + ".data";
      log.debug("Dumping file: " + fname);
      FileOutputStream fos = new FileOutputStream(fname);
      fos.write(buffer);
      fos.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
