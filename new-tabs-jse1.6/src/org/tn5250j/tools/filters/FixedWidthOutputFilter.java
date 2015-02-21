package org.tn5250j.tools.filters;

/*
 * @(#)FixedWidthOutputFilter.java
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

public class FixedWidthOutputFilter implements OutputFilterInterface {

   PrintStream fout = null;
   StringBuffer sb = new StringBuffer();

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
      for (int x = 0; x < ffd.size(); x++) {
         f = (FileFieldDef)ffd.get(x);
         if (f.isWriteField()) {


            switch (f.getFieldType()) {

               case 'P':
               case 'S':
                  rb.append(getFixedLength(cByte,f));
                  break;
               default:
                  rb.append(getFixedLength(cByte,f));
                  break;

            }
         }
      }

      fout.println(rb);

   }

   /**
    * Return the field in fixed width field.
    *
    * @param cByte
    * @param f
    * @return
    */
   private String getFixedLength(byte[] cByte,FileFieldDef f) {

      sb.setLength(0);

      switch (f.getFieldType()) {

         case 'P':
         case 'S':
            sb.append(f.parseData(cByte));
            formatNumeric(sb);
            while (sb.length() < f.getFieldLength())
               sb.insert(0,' ');
            break;
         default:
            sb.append(f.parseData(cByte));
            while (sb.length() < f.getFieldLength())
               sb.append(' ');
            break;

      }

      return sb.toString();


   }

   private void formatNumeric(StringBuffer sb) {

      if (sb.length() == 0) {
         sb.append('0');
         return;
      }

      int len = sb.length();
      int counter = 0;
      boolean done = false;
      boolean neg = false;

      while (!done && counter < len) {

         switch (sb.charAt(counter)) {

            case '0':
            case '+':
            case ' ':
               sb.setCharAt(counter,' ');
               break;
            case '-' :
               sb.setCharAt(counter,' ');
               neg = true;
               break;
            default :
               done = true;
               break;
         }

         if (!done)
            counter++;
      }

      if (counter > 0)
         counter--;

      if (neg)
         sb.setCharAt(counter,'-');

      if (sb.length() == 0) {
         sb.append('0');
      }

   }

   /**
    * Write the html header of the output file
    */
   public void writeHeader(String fileName, String host,
                                 ArrayList ffd, char decChar) {

//      FileFieldDef f;
//      StringBuffer sb = new StringBuffer();
//      //  loop through each of the fields and write out the field name for
//      //    each selected field
//      for (int x = 0; x < ffd.size(); x++) {
//         f = (FileFieldDef)ffd.get(x);
//         if (f.isWriteField()) {
//            sb.append(f.getFieldName());
//         }
//      }
//
//      fout.println (sb.toString().toCharArray());
   }


   /**
    * write the footer of the html output
    */
   public void writeFooter(ArrayList ffd) {

        fout.flush();
        fout.close();

   }

   public boolean isCustomizable() {
      return false;
   }

   public void setCustomProperties() {

   }

}