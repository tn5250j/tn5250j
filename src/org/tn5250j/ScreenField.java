package org.tn5250j;
/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.4
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.*;
import java.io.*;
import java.awt.event.*;
import java.awt.*;

import java.text.*;

public class ScreenField {

   public ScreenField(Screen5250 s) {

      this.s = s;

   }

   public ScreenField setField(int attr, int len, int ffw1, int ffw2,
                     int fcw1, int fcw2) {
      return setField(attr,
               s.getCurrentRow() - 1,
               s.getCurrentCol() - 1,
               len,
               ffw1,
               ffw2,
               fcw1,
               fcw2);
   }

   public ScreenField setField(int attr, int row, int col, int len, int ffw1, int ffw2,
                     int fcw1, int fcw2) {

//      startRow = row;
//      startCol = col;
      startPos = (row * s.getCols()) + col;
      endPos = startPos + length -1;
      cursorProg = 0;
      fieldId = 0;
      length = len;
      endPos = startPos + length -1;
      this.attr = attr;
      setFFWs(ffw1,ffw2);
      setFCWs(fcw1,fcw2);

      next = null;
      prev = null;

      return this;

   }

   public int getAttr(){
      return attr;
   }

   public int getLength(){
      return length;
   }

   public boolean setFFWs(int ffw1, int ffw2) {

      this.ffw1 = ffw1;
      this.ffw2 = ffw2;
      mdt = (ffw1 & 0x8 ) == 0x8;
//      if (mdt)
//         s.masterMDT = true;
      return mdt;
   }


   public int getFFW1(){
      return ffw1;
   }
   public int getFFW2(){
      return ffw2;
   }

   public void setFCWs(int fcw1, int fcw2) {

      this.fcw1 = fcw1;
      this.fcw2 = fcw2;

//      if ((fcw1 & 0x88) == 0x88) {
      if (fcw1 == 0x88) {

         cursorProg = fcw2;
      }
   }

   public int getFCW1(){
      return fcw1;
   }

   public int getFCW2(){
      return fcw2;
   }

   public int getFieldLength(){
      return length;
   }

   public int getCursorProgression() {
      return cursorProg;
   }

   public int getFieldId() {
      return fieldId;
   }

   public void setFieldId(int fi) {
      fieldId = fi;
   }

   public int getCursorRow() {

      return cursorPos / s.getCols();
   }

   public int getCursorCol() {

      return cursorPos % s.getCols();
   }

   public void changePos(int i) {

      cursorPos += i;

   }

   public String getText() {


      StringBuffer text = new StringBuffer();
      getKeyPos(endPos);
      int x = length;
      text.setLength(x);
      int nc = s.getCols();
      while (x-- > 0) {

         // here we manipulate the unicode characters a little for attributes
         //    that are imbedded in input fields.  We will offset them by unicode
         //    \uff00.   All routines that process these fields will have to
         //    return them to their proper offsets.
         //    example:
         //    if we read an attribute byte of 32 for normal display the unicode
         //       character for this is \u0020 and the unicode character for
         //       a space is also \u0020 thus the offset.
         if (s.screen[cursorPos].attributePlace) {
            text.setCharAt(x,(char)('\uff00' + s.screen[cursorPos].attr));
         }
         else
            text.setCharAt(x,s.screen[cursorPos].getChar());
         changePos(-1);

      }

      // Since only the mdt of the first continued field is set we will get
      //    the text of the next continued field if we are dealing with continued
      //    fields.  See routine setMDT for the whys of this.  This is only
      //    executed if this is the first field of a continued field.
      if (isContinued() && isContinuedFirst()) {
         ScreenField sf = this;
         do {
            sf = sf.next;
            text.append(sf.getText());
         }
         while (!sf.isContinuedLast());

         sf = null;
      }

      return text.toString();

   }

   public void setFieldChar(char c) {

      int x = length;
      cursorPos = startPos;
      while (x-- > 0) {
         s.screen[cursorPos].setChar(c);
         changePos(1);
      }
   }

   public void resetMDT() {
      mdt = false;

   }

   public void setMDT() {

      //  get the first field of a continued edit field if it is continued
      if (isContinued() && !isContinuedFirst()) {
         ScreenField sf = prev;
         while (sf.isContinued() && !sf.isContinuedFirst()) {

            sf = sf.prev;

         }
         sf.setMDT();
         sf = null;
      }
      else
         mdt = true;

   }

   public boolean isBypassField() {

      return (ffw1 & 0x20) == 0x20;

   }

   public int getAdjustment () {

      return (ffw2 & 0x7);
   }

   // is field exit required
   public boolean isFER () {

      return (ffw2 & 0x40) == 0x40;
   }

   // is field manditory enter
   public boolean isMandatoryEnter() {

      return (ffw2 & 0x8) == 0x8;

   }

   public boolean isToUpper() {

      return (ffw2 & 0x20) == 0x20;

   }

   // bits 5 - 7
   public int getFieldShift () {

      return (ffw1 & 0x7);

   }

   public boolean isAutoEnter() {

      return (ffw2 & 0x80) == 0x80;

   }

   public boolean isSignedNumeric () {

      return (getFieldShift() == 7);

   }

   public boolean isContinued() {

      return (fcw1 & 0x86) == 0x86 && (fcw2 >= 1 && fcw2 <= 3) ;

   }

   public boolean isContinuedFirst() {

      return (fcw1 & 0x86) == 0x86 && (fcw2 == 1);

   }

   public boolean isContinuedMiddle() {

      return (fcw1 & 0x86) == 0x86 && (fcw2 == 3);

   }

   public boolean isContinuedLast() {

      return (fcw1 & 0x86) == 0x86 && (fcw2 == 2);

   }

   public int getKeyPos(int row1, int col1) {

      int x = ((row1 * s.getCols()) + col1);
      int y = x - startPos();
      cursorPos = x;

      return y;
   }

   public int getKeyPos(int pos) {

      int y = pos - startPos();
      cursorPos = pos;

      return y;
   }

   public int getCurrentPos() {

      return cursorPos;
   }

   public boolean withinField (int pos) {

      if (pos >= startPos && pos <= endPos)
            return true;
      return false;

   }

   public int startPos() {

      return startPos;
   }

   public int startRow() {

      return startPos / s.getCols();

   }

   public int startCol() {

      return startPos % s.getCols();

   }

   public int endPos() {

      return endPos;

   }

   public String toString() {
      int fcw = (fcw1 & 0xff) << 8 | fcw2 & 0xff;
      return "startRow = " + startRow() + " startCol = " + startCol() +
            " length = " + length + " ffw1 = (0x" + Integer.toHexString(ffw1) +
            ") ffw2 = (0x" + Integer.toHexString(ffw2) +
            ") fcw1 = (0x" + Integer.toHexString(fcw1) +
            ") fcw2 = (0x" + Integer.toHexString(fcw2) +
            ") fcw = (" + Integer.toBinaryString(fcw) +
            ") fcw hex = (0x" + Integer.toHexString(fcw) +
            ") is bypass field = " + isBypassField() +
            ") is autoenter = " + isAutoEnter() +
            ") is manditoryenter = " + isMandatoryEnter() +
            " continued edit field = " + isContinued() +
            " first continued edit field = " + isContinuedFirst() +
            " middle continued edit field = " + isContinuedMiddle() +
            " last continued edit field = " + isContinuedLast() +
            " mdt = " + mdt;
   }

   int startPos = 0;
   int endPos = 0;
   boolean mdt = false;
   int attr = 0;
   int length = 0;
   int ffw1 = 0;
   int ffw2 = 0;
   int fcw1 = 0;
   int fcw2 = 0;
   int cursorPos = 0;
   Screen5250 s;
   int cursorProg = 0;
   int fieldId = 0;
   ScreenField next = null;
   ScreenField prev = null;

}