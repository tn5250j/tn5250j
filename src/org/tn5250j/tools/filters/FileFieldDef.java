package org.tn5250j.tools.filters;

/*
 * @(#)FileFieldDef.java
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
import org.tn5250j.framework.tn5250.tnvt;

 /**
 * Class representing the File Field Definition of a file
 */
public class FileFieldDef {

   private int startOffset;
   private int bufferLength;
   private int length;
   private int numDigits;
   private int decPos;
   private char type;
   private String txtDesc;
   private String fieldName;
   private String data;
   private boolean writeField;
   private char decChar;
   private boolean translateIt;
   private tnvt vt;
   private StringBuffer sbdata;

   public FileFieldDef(tnvt v,char dec) {
      decChar = dec;
      vt = v;
      translateIt = true;
   }

   public String parseData(byte[] cByte) {

      if (!translateIt) {
         return sbdata.toString();
      }

      StringBuffer sb = new StringBuffer(bufferLength);

      int end = startOffset + length - 1;

      switch (type) {

         case 'P':   // Packed decimal format

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
            // Here we interrogate the the DIGIT portion for the sign
            //    0x0f = positive   -> 0x0f | 0x0d = 0x0f
            //    0x0d = negative   -> 0x0d | 0x0d = 0x0d
            if ((byteD | 0x0d) == 0x0d)
               sb.insert(0,'-');
            else
               sb.insert(0,'+');

            break;

         case 'S':   // Signed decimal format

            // example field of buffer length 5 with decimal precision 0
            //    output length is 5
            //
            //    each byte of the buffer contains a digit.  The zone portion
            //    contain F for the EBCDIC number not 3 for ASCII, the digit
            //    portion contains the number, the last byte of the field
            //    contains the last digit in the DIGIT portion and the sign is
            //    contained in the ZONE portion.
            //
            //    The number 12345 would be represented as follows:
            //    byte 1 of 5 -> F1
            //    byte 2 of 5 -> F2
            //    byte 3 of 5 -> F3
            //    byte 4 of 5 -> F4
            //    byte 5 of 5 -> F5 The F in the zone portion signifies positive
            //
            //    The number -12345 would be represented as follows:
            //    byte 1 of 5 -> F1
            //    byte 2 of 5 -> F2
            //    byte 3 of 5 -> F3
            //    byte 4 of 5 -> F4
            //    byte 5 of 5 -> D5 The D in the zone portion signifies negative


            for (int f = startOffset-1;f < end; f++) {
               // we only take the digit portion of the byte
               sb.append((cByte[f] & 0x0f));
            }

            // Here we interrogate the the ZONE portion for the sign
            //    0xf0 = positive   -> 0xf5 & 0xf0 = 0xf0
            //    0xd0 = negative   -> 0xd5 & 0xf0 = 0xd0
            if ((cByte[end - 1] & 0xf0) == 0xd0)
               sb.insert(0,'-');
            else
               sb.insert(0,'+');
            break;

         default:

            for (int f = startOffset-1;f < end; f++) {
               sb.append(vt.getCodePage().ebcdic2uni(cByte[f] & 0xff));
            }

      }

      if (decPos > 0) {

         int o = sb.length();
         sb.insert(o - decPos,decChar);

      }

      data = sb.toString();
      return data;
   }

   public void setFieldData(String fd) {
      if (sbdata == null)
         sbdata = new StringBuffer(length);
      sbdata.setLength(0);
      sbdata.append(fd);
   }
   public String toString() {
      return fieldName + " " +
               startOffset + " " +
               length + " " +
               getBufferOutLength() + " " +
               numDigits + " " +
               decPos + " " +
               type + " " +
               txtDesc;
   }

   public char getFieldType() {
      return type;
   }

   public int getPrecision() {
      return decPos;
   }

   public String getFieldName () {

      return fieldName;
   }

   public void setFieldName (String name) {

      fieldName = name;
   }

   public void setStartOffset (String pos) {

      startOffset = Integer.parseInt(pos);
   }

   public int getBufferOutLength() {
      return bufferLength;
   }

   public int getFieldLength () {
      return length;

   }

   public void setFieldLength (String len) {

      length = Integer.parseInt(len);

   }

   public void setNumDigits (String num) {

      numDigits = Integer.parseInt(num);

   }

   public void setDecPositions (String dec) {

      decPos = Integer.parseInt(dec);

   }

   public void setFieldType (String fType) {

      type = fType.charAt(0);

      if (type == 'P')
         bufferLength = (length * 2) - 1;
      else
         bufferLength = length;
   }

   public void setFieldText (String text) {

      txtDesc = text;

   }

   public void setNeedsTranslation(boolean translate) {
      translateIt = translate;
   }

   public boolean isWriteField () {
      return writeField;
   }

   public void setWriteField(boolean value) {

      writeField = value;

   }

}