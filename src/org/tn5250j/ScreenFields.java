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

import java.io.ByteArrayOutputStream;
import org.tn5250j.tools.CodePage;

public class ScreenFields implements TN5250jConstants {

   private ScreenField[] screenFields;
   private ScreenField currentField;
   private ScreenField saveCurrent;
   private int sizeFields;
   private boolean cpfExists;
   private int nextField;
   private int fieldIds;
   private Screen5250 screen;
   private boolean masterMDT;

   public ScreenFields(Screen5250 s) {

      screen = s;
      screenFields = new ScreenField[256];

   }

   public void clearFFT() {

      sizeFields = nextField = fieldIds = 0;
      cpfExists = false;   // clear the cursor progression fields flag
      currentField = null;
      masterMDT = false;

   }

   public boolean existsAtPos(int lastPos) {

      ScreenField sf = null;

      // from 14.6.12 for Start of Field Order 5940 function manual
      //  examine the format table for an entry that begins at the current
      //  starting address plus 1.
      for (int x = 0;x < sizeFields; x++) {
         sf = screenFields[x];

         if (lastPos == sf.startPos()) {
            currentField = sf;
            return true;
         }

      }

      return false;
   }

   public boolean isMasterMDT() {
      return masterMDT;
   }

   public boolean isCurrentField() {
      return currentField == null;
   }

   public boolean isCurrentFieldFER() {
      return currentField.isFER();
   }

   public boolean isCurrentFieldToUpper() {
      return currentField.isToUpper();
   }

   public boolean isCurrentFieldBypassField() {
      return currentField.isBypassField();
   }

   public boolean isCurrentFieldAutoEnter() {
      return currentField.isAutoEnter();
   }

   public boolean withinCurrentField(int pos) {
      return currentField.withinField(pos);
   }

   public boolean isCurrentFieldContinued() {
      return currentField.isContinued();
   }

   public boolean isCurrentFieldContinuedFirst() {
      return currentField.isContinuedFirst();
   }

   public boolean isCurrentFieldContinuedMiddle() {
      return currentField.isContinuedMiddle();
   }

   public boolean isCurrentFieldContinuedLast() {
      return currentField.isContinuedLast();
   }

   public void saveCurrentField() {
      saveCurrent = currentField;
   }

   public void restoreCurrentField() {
      currentField = saveCurrent;
   }

   public void setCurrentField(ScreenField sf) {
      currentField = sf;
   }

   public void setCurrentFieldMDT() {
      currentField.setMDT();
      masterMDT = true;
   }

   public void setCurrentFieldFFWs(int ffw1, int ffw2) {

      masterMDT = currentField.setFFWs(ffw1,ffw2);

   }


   public ScreenField setField(int attr, int row, int col, int len, int ffw1,
                           int ffw2, int fcw1, int fcw2) {

      ScreenField sf = null;
      screenFields[nextField] = new ScreenField(screen);
      screenFields[nextField].setField(attr,row,col,len,ffw1,ffw2,fcw1,fcw2);
      sf = screenFields[nextField++];
      sizeFields++;

      // set the field id if it is not a bypass field
      // this is used for cursor progression
      if (!sf.isBypassField())
         sf.setFieldId(++fieldIds);

      // check if the cursor progression field flag should be set.
      if ((fcw1 & 0x88) == 0x88)
         cpfExists = true;

      if (currentField != null) {
         currentField.next = sf;
         sf.prev = currentField;
      }

      currentField = sf;

      // check if the Modified Data Tag was set while creating the field
      masterMDT = currentField.mdt;
      return currentField;

   }

   public ScreenField getField(int index) {

      return screenFields[index];
   }

   public ScreenField getCurrentField() {
      return currentField;
   }

   public int getCurrentFieldPos() {
      return currentField.getCurrentPos();
   }

   public int getCurrentFieldShift() {
      return currentField.getFieldShift();
   }

   public String getCurrentFieldText() {

      return currentField.getText();
   }

   public int getSize() {

      return sizeFields;
   }

   public boolean isInField(int pos) {
      return isInField(pos,true);
   }

   public boolean isInField() {
      return isInField(screen.getLastPos(),true);
   }

   public boolean isInField(int pos, boolean chgToField) {

      ScreenField sf;

      for (int x = 0;x < sizeFields; x++) {
         sf = screenFields[x];

         if (sf.withinField(pos)) {
            if (chgToField) {
               currentField = sf;
            }
            return true;
         }
      }
      return false;

   }

   public void gotoFieldNext() {

      // sanity check - we where getting null pointers after a restore of screen
      //   and cursor was not positioned on a field when returned
      //   *** Note *** to myself
      //   maybe this is fixed I will have to check this some time
      int lastPos = screen.getLastPos();

      if (currentField == null && (sizeFields != 0) && !isInField(lastPos,true)) {
         int pos = lastPos;
         screen.setCursorOff();
         screen.advancePos();
         lastPos = screen.getLastPos();
         while (!isInField() && pos != lastPos) {
            screen.advancePos();
         }
         screen.setCursorOn();
      }

      // if we are still null do nothing
      if (currentField == null)
         return;

      ScreenField sf = currentField;

      if (!sf.withinField(lastPos)) {
         screen.setCursorOff();

         if (sizeFields > 0) {

            // lets get the current position so we can test if we have looped
            //    the screen and not found a valid field.
            int pos = lastPos;
            int savPos = lastPos;
            boolean done = false;
            do {
               screen.advancePos();
               lastPos = screen.getLastPos();
               if (isInField(lastPos)
                   || pos==lastPos) {
                  if (!currentField.isBypassField()) {
                     screen.gotoField(currentField);
                     done = true;
                  }
               }
            }  while ( !done && lastPos != savPos);
         }
         screen.setCursorOn();

      }
      else {
         if (!cpfExists) {
            do {

               sf = sf.next;
            }
            while ( sf != null && sf.isBypassField());

         }
         else {
            int f = 0;
            int cp = sf.getCursorProgression();
            ScreenField sf1 = null;
            boolean found = false;
            while (!found && f < sizeFields) {

               sf1 = screenFields[f++];
               if (sf1.getFieldId() == cp)
                  found = true;
            }
            if (found)
               sf = sf1;
            else {
               do {
                  sf = sf.next;
               }
               while ( sf != null && sf.isBypassField());

            }
            sf1 = null;
         }

         if (sf == null)
            screen.gotoField(1);
         else {
            currentField = sf;
            screen.gotoField(currentField);
         }

      }
   }

   public void gotoFieldPrev() {

      ScreenField sf = currentField;
      int lastPos = screen.getLastPos();

      if (!sf.withinField(lastPos)) {
         screen.setCursorOff();

         if (sizeFields > 0) {
            // lets get the current position so we can test if we have looped
            //    the screen and not found a valid field.
            int pos = lastPos;
            int savPos = lastPos;
            boolean done = false;

            do {
               screen.changePos(-1);
               lastPos = screen.getLastPos();

               if (isInField(lastPos)
                  || (pos == lastPos)) {

                  if (!currentField.isBypassField()) {
                     screen.gotoField(currentField);
                     done = true;
                  }
               }
            } while ( !done && lastPos != savPos);
         }
         screen.setCursorOn();

      }
      else {

         if (sf.startPos() == lastPos) {
            if (!cpfExists) {

               do {
                  sf = sf.prev;
               }
               while ( sf != null && sf.isBypassField());
            }
            else {

               int f = 0;
               int cp = sf.getFieldId();
               ScreenField sf1 = null;
               boolean found = false;
               while (!found && f < sizeFields) {

                  sf1 = screenFields[f++];
                  if (sf1.getCursorProgression() == cp)
                     found = true;
               }
               if (found)
                  sf = sf1;
               else {
                  do {
                     sf = sf.prev;
                  }
                  while ( sf != null && sf.isBypassField());
               }
               sf1 = null;
            }
         }

         if (sf == null) {
            int size = sizeFields;
            sf = screenFields[size - 1];

            while (sf.isBypassField() && size-- > 0) {
               sf = screenFields[size];

            }
         }
         currentField = sf;
         screen.gotoField(currentField);
      }
   }

   protected void readFormatTable(ByteArrayOutputStream baosp,int readType,
                                    CodePage codePage) {

      ScreenField sf;
      boolean isSigned = false;
      char c;

      if (masterMDT) {

         StringBuffer sb = new StringBuffer();
         for (int x = 0; x < sizeFields; x++) {
            isSigned = false;

            sf = screenFields[x];

            if (sf.mdt || (readType == CMD_READ_INPUT_FIELDS)) {

               sb.setLength(0);
               sb.append(sf.getText());


               if (readType == CMD_READ_MDT_FIELDS ||
                     readType == CMD_READ_MDT_IMMEDIATE_ALT) {
                  int len = sb.length() - 1;

                  // we strip out all '\u0020' and less
                  while (len >= 0 &&
                     (sb.charAt(len) <= ' ' || sb.charAt(len) >= '\uff20' )) {

                     sb.deleteCharAt(len--);
                  }

               }

//               System.out.println("field " + sf.toString());
//               System.out.println(">" + sb.toString() + "<");

//               System.out.println(" field is all nulls");
               if (sf.isSignedNumeric() && sb.charAt(sb.length() - 1) == '-') {
                  isSigned = true;
                  sb.setLength(sb.length() - 1);
               }

               int len3 = sb.length();
               if (len3 > 0 || (readType == CMD_READ_MDT_FIELDS ||
                       readType == CMD_READ_MDT_IMMEDIATE_ALT)) {

                  if ((readType == CMD_READ_MDT_FIELDS ||
                       readType == CMD_READ_MDT_IMMEDIATE_ALT)) {

                     baosp.write(17);   // start of field data
                     baosp.write(sf.startRow()+1);
                     baosp.write(sf.startCol()+1);
                  }

//                  int len = sb.length();
                  for (int k = 0; k < len3; k++) {
                     c = sb.charAt(k);
                     // here we have to check for special instances of the
                     //    characters in the string field.  Attribute bytes
                     //    are encoded with an offset of \uff00
                     //    This is a hack !!!!!!!!!!!
                     //    See ScreenField object for a description
                     if (c < ' ' || c >= '\uff20') {

                        // if it is an offset attribute byte we just pass
                        //    it straight on to the output stream
                        if (c >= '\uff20' && c <= '\uff3f') {
                           baosp.write(c - '\uff00');
                        }
                        else
                           baosp.write(codePage.getEBCDIC(' '));
                     }
                     else {
                        if (isSigned && k == len3 - 1) {
                           baosp.write(0xd0 | (0x0f & c));
                        }
                        else
                           baosp.write(codePage.uni2ebcdic(c));

                     }
                  }
               }
            }
         }
      }
   }

}