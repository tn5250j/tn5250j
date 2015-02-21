package org.tn5250j.tools.filters;

/*
 * @(#)iOhioSession.java
 * Copyright:    Copyright (c) 2001
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

import java.io.*;
import java.util.ArrayList;

public class HTMLOutputFilter implements OutputFilterInterface {

   PrintStream fout = null;

   // create instance of file for output
   public void createFileInstance(String fileName) throws
                              FileNotFoundException {
      fout = new PrintStream(new FileOutputStream(fileName));
   }

   /**
    * Write the html header of the output file
    */
   public void parseFields(byte[] cByte, ArrayList ffd, StringBuffer rb) {

      FileFieldDef f;

      // write out the html record information for each field that is selected

      rb.append ("<TR>");
      rb.append('\n');
      for (int x = 0; x < ffd.size(); x++) {
         f = (FileFieldDef)ffd.get(x);
         if (f.isWriteField()) {
            rb.append ("<TD>");
            rb.append(f.parseData(cByte));
            rb.append ("</TD>");
            rb.append ('\n');
         }
      }

      rb.append ("</TR>");
      rb.append ('\n');

      fout.println(rb);

   }
   /**
    * Write the html header of the output file
    */
   public void writeHeader(String fileName, String host,
                                 ArrayList ffd, char decChar) {

      try {

         fout.write ("<HTML>".getBytes());
         fout.write ("\n".getBytes());
         fout.write ("<HEAD>".getBytes());
         fout.write ('\n');
         String out = "<TITLE>" + host + " - " +
                       fileName + "</TITLE>";
         fout.write (out.getBytes());
         fout.write ('\n');

         fout.write ("   <META http-equiv=".getBytes());
         fout.write ('\"');
         fout.write ("GENERATOR".getBytes());
         fout.write ('\"');
         fout.write (" content".getBytes());
         fout.write ('\"');
         fout.write ("tn5250j Data Transfer".getBytes());
         fout.write ('\"');
         fout.write (">".getBytes());
         fout.write ('\n');

         fout.write ("   <META http-equiv=".getBytes());
         fout.write ('\"');
         fout.write ("Content-Type".getBytes());
         fout.write ('\"');
         fout.write (" content=".getBytes());
         fout.write ('\"');
         fout.write ("text/html;charset=windows-1252".getBytes());
         fout.write ('\"');
         fout.write (">".getBytes());
         fout.write ('\n');

         fout.write ("</HEAD>".getBytes());
         fout.write ('\n');

         fout.write ("<BODY>".getBytes());
         fout.write ('\n');

         fout.write ("<TABLE BORDER>".getBytes());
         fout.write ('\n');

         fout.write ("<TR ALIGN=center>".getBytes());
         fout.write ('\n');

         FileFieldDef f;

         //  loop through each of the fields and write out the field name for
         //    each selected field
         for (int x = 0; x < ffd.size(); x++) {
            f = (FileFieldDef)ffd.get(x);
            if (f.isWriteField()) {
               out = "<TH>" + f.getFieldName() + "</TH>";
               fout.write (out.getBytes());
               fout.write ('\n');
            }
         }

         fout.write ("</TR>".getBytes());
         fout.write ('\n');

      }
      catch (IOException ioe) {

//         printFTPInfo(" error writing header " + ioe.getMessage());
      }
   }


   /**
    * write the footer of the html output
    */
   public void writeFooter(ArrayList ffd) {

      try {

        fout.write ("</TABLE>".getBytes());
        fout.write ('\n');
        fout.write ("</BODY>".getBytes());
        fout.write ('\n');
        fout.write ("</HTML>".getBytes());
        fout.write ('\n');

        fout.flush();
        fout.close();

      }
      catch (IOException ioex) {
         System.out.println(ioex.getMessage());
      }


   }

   public boolean isCustomizable() {
      return false;
   }

   public void setCustomProperties() {

   }

}