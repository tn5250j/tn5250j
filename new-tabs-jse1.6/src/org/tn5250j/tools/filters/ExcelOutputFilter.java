package org.tn5250j.tools.filters;

/*
 * @(#)ExcelOutputFilter.java
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
import java.util.Vector;

public class ExcelOutputFilter implements OutputFilterInterface {

   DataOutputStream fout = null;
//   BufferedOutputStream fout;
   int row = 0;
   StringBuffer sb;
   Vector formats;

   // create instance of file for output
   public void createFileInstance(String fileName) throws
                              FileNotFoundException {
      try {
         fout = new DataOutputStream(new FileOutputStream(fileName));
      }
      catch (Exception e) {
         System.out.println("create file " + e.getMessage());
      }

      // initialize work variables
      row = 0;
      sb = new StringBuffer();
      formats = null;
   }

   /**
    * Write the html header of the output file
    */
   public void parseFields(byte[] cByte, ArrayList ffd, StringBuffer rb) {

      FileFieldDef f;

      // write out the html record information for each field that is selected

      int col = 0;
      row++;

      int fmt = 0;
      for (int x = 0; x < ffd.size(); x++) {
         f = (FileFieldDef)ffd.get(x);
         if (f.isWriteField()) {
            switch (f.getFieldType()) {

               case 'P':
               case 'S':
                  writeDouble(f.parseData(cByte),col++,f.getPrecision());
                  break;
               default:
                  writeLabel(f.parseData(cByte),col++);
                  break;

            }
         }
      }

   }


   private void writeLabel(String label, int col) {

      int wLen = label.length();

      try {
         writeShort(0x04);
         writeShort(8 + wLen);
         writeShort(row);
         writeShort(col);
         fout.write(0x00);
         fout.write(0x00);
         fout.write(0x00);
         fout.writeByte(wLen);
         fout.writeBytes(label);
      }

      catch(IOException ioe) {

         System.out.println("write label: " + ioe.getMessage());
      }

   }

/* *** NEVER USED ********************************************************** */
//   private void writeDouble(String string, int col) {
//
//
//      writeDouble(string, col,0);
//
//   }

   private void writeDouble(String string, int col, int fmtCode) {

      try {
         writeShort(0x03);
         writeShort(0x0F);
         writeShort(row);
         writeShort(col);
//         fout.write(0x00);
//         fout.writeShort(fmtCode);
//--------------
         fout.writeByte(0x00);
//         fout.writeByte(0x00);
         fout.writeByte(fmtCode);
         fout.writeByte(formats.indexOf(Integer.toString(fmtCode)));
//         fout.writeByte(0x00);
//         fout.writeByte(0x00);
//         fout.writeByte(fmtCode);
//         fout.writeByte(0x00);
//         fout.writeByte(0x03);
//         fout.writeShort(fmtCode);
         double d = Double.parseDouble(string);
         writeLong(Double.doubleToLongBits(d));

      }

      catch(IOException ioe) {

         System.out.println("write double: " + ioe.getMessage());
      }

   }

   private void writeFormat(int len, int decPos, int fmtCode) {

      if (formats == null) {
         formats = new Vector();
      }

      sb.setLength(0);
      if (decPos > 0) {
         for (int x = decPos; x > 0; x--) {

            sb.insert(0,'0');

         }

         sb.insert(0,"#0.");
      }
      else {
         sb.append("#0");
      }


      int fLen = sb.length();

      if (!formats.contains(Integer.toString(decPos))) {

         formats.add(Integer.toString(decPos));

      try {
         writeShort(0x001E);
         writeShort(1 + fLen);
//         writeShort(fmtCode);
         fout.writeByte(fLen);
//         fout.writeByte(0x0);
         fout.writeBytes(sb.toString());
      }

      catch(IOException ioe) {

         System.out.println("write label: " + ioe.getMessage());
      }
      }
   }

   /**
   * Writes an eight-byte <code>long</code> to the underlying output stream
   * in little endian order, low byte first, high byte last
   *
   * @param      l   the <code>long</code> to be written.
   * @exception  IOException  if the underlying stream throws an IOException.
   */
   public void writeLong(long l) throws IOException {

       fout.write((int) l & 0xFF);
       fout.write((int) (l >>> 8) & 0xFF);
       fout.write((int) (l >>> 16) & 0xFF);
       fout.write((int) (l >>> 24) & 0xFF);
       fout.write((int) (l >>> 32) & 0xFF);
       fout.write((int) (l >>> 40) & 0xFF);
       fout.write((int) (l >>> 48) & 0xFF);
       fout.write((int) (l >>> 56) & 0xFF);

   }

  /**
   * Writes a two byte <code>short</code> to the underlying output stream in
   * little endian order, low byte first.
   *
   * @param      s   the <code>short</code> to be written.
   * @exception  IOException  if the underlying stream throws an IOException.
   */
   public void writeShort(int s) throws IOException {

      fout.write(s & 0xFF);
      fout.write((s >>> 8) & 0xFF);
   }

   /**
    * Write the html header of the output file
    */
   public void writeHeader(String fileName, String host,
                                 ArrayList ffd, char decChar) {

      final byte[] bof = {0x09,0x08,0x06,0x00,0x00,0x00,0x10,0x00,0x00,0x00};

      final byte[] dimension = {0x00,0x02,0x0A,0x00,0x00,0x00,0x64,0x00,
                                 0x00,0x00,0x64,0x00,0x00,0x00};

      try {

         fout.write(bof);
         fout.write(dimension);
         FileFieldDef f;


         //  loop through each of the fields and write out the format of
         //     the cell for numeric values.
         int c = 0;

         for (int x = 0; x < ffd.size(); x++) {
            f = (FileFieldDef)ffd.get(x);
            if (f.isWriteField()) {

               switch (f.getFieldType()) {

                  case 'P':
                  case 'S':
                     writeFormat(f.getFieldLength(),f.getPrecision(),++c);
                     break;
                  default:
                     break;

               }
            }
         }

         //  loop through each of the fields and write out the field name for
         //    each selected field
         c = 0;
         for (int x = 0; x < ffd.size(); x++) {
            f = (FileFieldDef)ffd.get(x);
            if (f.isWriteField()) {
               writeLabel(f.getFieldName(),c++);
            }
         }


      }
      catch (IOException ioe) {

//      catch (Exception e) {
         System.out.println("header " + ioe.getMessage());
//      }
//         printFTPInfo(" error writing header " + ioe.getMessage());
      }
   }


   /**
    * write the footer of the html output
    */
   public void writeFooter(ArrayList ffd) {

      try {

         fout.write(0x0a);
         fout.write(0x00);
         fout.write(0x00);
         fout.write(0x00);
         fout.flush();
         fout.close();

      }
      catch (IOException ioex) {
         System.out.println("write footer: " + ioex.getMessage());
      }


   }

   public boolean isCustomizable() {
      return false;
   }

   public void setCustomProperties() {

   }

}