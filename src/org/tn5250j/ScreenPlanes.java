/**
 * Title: ScreenPlanes.java
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
package org.tn5250j;

public class ScreenPlanes {

   Screen5250 scr;
   int screenSize;
   int numRows;
   int numCols;
   int errorLineNum;

   private char char0 = 0;
   private static final int initAttr = 32;
   private static final char initChar = 0;

   protected char[] screen;   // text plane
   protected int[] screenAttr;   // attribute plane
   protected int[] screenGUI;   // gui plane
   protected boolean[] screenIsAttr;
   protected int[] fieldExtended;
   protected int[] screenField;
   protected int[] screenColor;   // color plane
   protected boolean[] screenIsChanged;

   protected char[] errorLine;
   protected int[] errorLineAttr;
   protected boolean[] errorLineIsAttr;

   public ScreenPlanes(Screen5250 s5250, int size) {

      scr = s5250;
      setSize(size);
   }

   protected void setSize(int newSize) {

      int oldRows = numRows;
      screenSize = newSize;

      numCols = 80;
      switch (newSize) {
         case 24:
            numRows = 24;
            break;
         case 27:
            numRows = 27;
            numCols = 132;
         break;

      }

      // this is used here when size changes
      setErrorLine(numRows);

      screenSize = numRows * numCols;
      screen = new char[screenSize];
      screenAttr = new int[screenSize];
      screenIsAttr = new boolean[screenSize];
      screenGUI = new int[screenSize];
      fieldExtended = new int[screenSize];
      screenIsChanged = new boolean[screenSize];

      initalizePlanes();
//      if (numRows != oldRows)
//         fireScreenSizeChanged();
   }

   protected void setErrorLine (int line) {

      // * NOTE * for developers I have changed the send qry to pass different
      //    parameters to the host so check setsize for setting error line as well.
      //
      if (line == 0 || line > numRows)
         errorLineNum = numRows;
      else
         errorLineNum = line;
   }

	/**
	 * Returns the current error line number
	 *
	 * @return current error line number
	 */
	protected int getErrorLine() {
		return errorLineNum;
	}

   protected void saveErrorLine() {

      // if there is already an error line saved then do not save it again
      //  This signifies that there was a previous error and the original error
      //  line was not restored yet.
      if (errorLine == null) {
         errorLine = new char[numCols];
         errorLineAttr = new int[numCols];
         errorLineIsAttr = new boolean[numCols];

         int r = scr.getPos(errorLineNum-1,0);

         for (int x = 0;x < numCols; x++) {
            errorLine[x] = screen[r+x];
            errorLineAttr[x] = screenAttr[r+x];
            errorLineIsAttr[x] = screenIsAttr[r+x];
         }
      }
   }

	/**
	 * Restores the error line characters from the save buffer.
	 *
	 * @see #saveErrorLine()
	 */
	protected void restoreErrorLine() {

		if (errorLine != null) {
			int r = scr.getPos(errorLineNum - 1, 0);

			for (int x = 0; x < numCols - 1; x++) {
//				screen[r + x].setCharAndAttr(errorLine[x].getChar(),
//						errorLine[x].getCharAttr(), false);
            this.setScreenCharAndAttr(r+x,errorLine[x],errorLineAttr[x],errorLineIsAttr[x]);
			}
			errorLine = null;
			errorLineAttr = null;
         errorLineIsAttr = null;
		}
	}

   protected boolean isErrorLineSaved() {
      return errorLine == null ? true : false;
   }

   protected void setScreenCharAndAttr(int pos, char c, int attr, boolean isAttr) {

      screen[pos] = c;
      screenAttr[pos] = attr;
      screenIsAttr[pos] = isAttr;
      screenGUI[pos] = initChar;

   }

   protected void setScreenAttr(int pos, int attr, boolean isAttr) {

      screenAttr[pos] = attr;
      screenIsAttr[pos] = isAttr;
      screenGUI[pos] = initChar;

   }

   protected void setScreenAttr(int pos, int attr) {

      screenAttr[pos] = attr;
      screenGUI[pos] = initChar;

   }

   protected final void setChar(int pos, char c) {
      screenIsChanged[pos] = screen[pos] == c ? false : true;
      //      if (isChanged)
      //         System.out.println(sChar[0] + " - " + c);
      screen[pos] = c;
      if (screenIsAttr[pos])
         setScreenCharAndAttr(pos,c,32,false);

   }

   protected final char getChar(int pos) {
      return screen[pos];
   }

   protected final int getCharAttr(int pos) {
      return screenAttr[pos];
   }

   protected final boolean isAttributePlace(int pos) {
      return screenIsAttr[pos];
   }

   public final void setUseGUI(int pos, int which) {

      screenIsChanged[pos] = screenGUI[pos] == which ? false : true;

      screenGUI[pos] = which;
//      if (which == NO_GUI) {
//        screenGUI[pos] = ;
//      }
//      else {
//        screenGUI[pos] = true;
//      }
   }

   protected void initalizePlanes () {
      for (int y = 0;y < screenSize; y++) {

         setScreenCharAndAttr(y,initChar,initAttr,false);
         screenGUI[y] = NO_GUI;
         fieldExtended[y] = initChar;
         screenIsChanged[y] = false;

      }
   }

   protected final int getWhichGUI(int pos) {

      return screenGUI[pos];
   }

   protected final boolean isChanged(int pos) {
      return screenIsChanged[pos];
   }

   protected final boolean isUseGui(int pos) {
      return screenGUI[pos] == NO_GUI ? false : true;
   }

//      public final char getChar() {
//           return sChar[0];
//      }
//
//      public final int getCharAttr() {
//           return attr;
//      }
//
//      public final boolean isAttributePlace() {
//         return attributePlace;
//      }
//
//      public final void setChar(char c) {
//         isChanged = sChar[0] == c ? false : true;
//   //      if (isChanged)
//   //         System.out.println(sChar[0] + " - " + c);
//         sChar[0] = c;
//         if (attributePlace)
//            setCharAndAttr(c,32,false);
//
//      }
//
//      public final void setUseGUI(int which) {
//
//         isChanged = whichGui == which ? false : true;
//
//         whichGui = which;
//         if (which == NO_GUI) {
//           useGui = false;
//         }
//         else {
//           useGui = true;
//         }
//      }
//
//      public final int getWhichGUI() {
//
//         return whichGui;
//      }
//
//
//      public final void setCharAndAttr(char c, int a, boolean ap) {
//
//         isChanged = sChar[0] == c ? false : true;
//   //      if (isChanged)
//   //         System.out.println(sChar[0] + " - " + c);
//
//         sChar[0] = c;
//   //         useGui = false;
//   //         whichGui = NO_GUI;
//
//         if(attr != a)
//             setAttribute(a);
//
//         if(ap) {
//            attributePlace = true;
//            useGui = false;
//            whichGui = NO_GUI;
//
//         }
//         else
//            attributePlace = false;
//      }
//
//      public final void setRowCol(int row, int col) {
//
//         cArea.setRect((s.fmWidth*col),s.fmHeight * row,s.fmWidth,s.fmHeight);
//         x = s.fmWidth * col;
//         y = s.fmHeight * row;
//         cy = (int)(y + s.fmHeight - (s.lm.getDescent() + s.lm.getLeading()));
//      }
//
//      public final void setAttribute(int i) {
//
//         colSep = false;
//         underLine = false;
//         nonDisplay = false;
//
//         isChanged = attr == i ? false : true;
//
//         attr = i;
//
//         if(i == 0)
//            return;
//         switch(i) {
//            case 32: // green normal
//               fg = s.colorGreen;
//               bg = s.colorBg;
//               break;
//
//            case 33: // green/revers
//               fg = s.colorBg;
//               bg = s.colorGreen;
//               break;
//
//            case 34: // white normal
//               fg = s.colorWhite;
//               bg = s.colorBg;
//               break;
//
//            case 35: // white/reverse
//               fg = s.colorBg;
//               bg = s.colorWhite;
//               break;
//
//            case 36: // green/underline
//               fg = s.colorGreen;
//               bg = s.colorBg;
//               underLine = true;
//               break;
//
//            case 37: // green/reverse/underline
//               fg = s.colorBg;
//               bg = s.colorGreen;
//               underLine = true;
//               break;
//
//            case 38: // white/underline
//               fg = s.colorWhite;
//               bg = s.colorBg;
//               underLine = true;
//               break;
//
//            case 39:
//               nonDisplay = true;
//               break;
//
//            case 40:
//            case 42: // red/normal
//               fg = s.colorRed;
//               bg = s.colorBg;
//               break;
//
//            case 41:
//            case 43: // red/reverse
//               fg = s.colorBg;
//               bg = s.colorRed;
//               break;
//
//            case 44:
//            case 46: // red/underline
//               fg = s.colorRed;
//               bg = s.colorBg;
//               underLine = true;
//               break;
//
//            case 45: // red/reverse/underline
//               fg = s.colorBg;
//               bg = s.colorRed;
//               underLine = true;
//               break;
//
//            case 47:
//               nonDisplay = true;
//               break;
//
//            case 48:
//               fg = s.colorTurq;
//               bg = s.colorBg;
//               colSep = true;
//               break;
//
//            case 49:
//               fg = s.colorBg;
//               bg = s.colorTurq;
//               colSep = true;
//               break;
//
//            case 50:
//               fg = s.colorYellow;
//               bg = s.colorBg;
//               colSep = true;
//               break;
//
//            case 51:
//               fg = s.colorBg;
//               bg = s.colorYellow;
//               colSep = true;
//               break;
//
//            case 52:
//               fg = s.colorTurq;
//               bg = s.colorBg;
//   //            colSep = true;
//               underLine = true;
//               break;
//
//            case 53:
//               fg = s.colorBg;
//               bg = s.colorTurq;
//   //            colSep = true;
//               underLine = true;
//               break;
//
//            case 54:
//               fg = s.colorYellow;
//               bg = s.colorBg;
//   //            colSep = true;
//               underLine = true;
//               break;
//
//            case 55:
//               nonDisplay = true;
//               break;
//
//            case 56: // pink
//               fg = s.colorPink;
//               bg = s.colorBg;
//               break;
//
//            case 57: // pink/reverse
//               fg = s.colorBg;
//               bg = s.colorPink;
//               break;
//
//            case 58: // blue/reverse
//               fg = s.colorBlue;
//               bg = s.colorBg;
//               break;
//
//            case 59: // blue
//               fg = s.colorBg;
//               bg = s.colorBlue;
//               break;
//
//            case 60: // pink/underline
//               fg = s.colorPink;
//               bg = s.colorBg;
//               underLine = true;
//               break;
//
//            case 61: // pink/reverse/underline
//               fg = s.colorBg;
//               bg = s.colorPink;
//               underLine = true;
//               break;
//
//            case 62: // blue/underline
//               fg = s.colorBlue;
//               bg = s.colorBg;
//               underLine = true;
//               break;
//
//            case 63:  // nondisplay
//               nonDisplay = true;
//               break;
//            default:
//               fg = s.colorYellow;
//               break;
//
//         }
//      }
//
//      public final boolean isChanged() {
//         return isChanged;
//      }
//
//      public final String toString() {
//
//         return "x >" + x + "< y >" + y + "< char >" + sChar[0]
//                  + "< char hex >" + Integer.toHexString(sChar[0]) + "< attr >" + attr
//                  + "< attribute >" + isAttributePlace() + "< isNonDisplayable >"
//                  + nonDisplay + "< underline >" + underLine + "< colSep >" + colSep
//                  + "< backGround >" + bg + "< foreGround >" + fg ;
//
//      }

   public static final int NO_GUI = 0;
   public static final int UPPER_LEFT = 1;
   public static final int UPPER = 2;
   public static final int UPPER_RIGHT = 3;
   public static final int LEFT = 4;
   public static final int RIGHT = 5;
   public static final int LOWER_LEFT = 6;
   public static final int BOTTOM = 7;
   public static final int LOWER_RIGHT = 8;
   public static final int FIELD_LEFT = 9;
   public static final int FIELD_RIGHT = 10;
   public static final int FIELD_MIDDLE = 11;
   public static final int FIELD_ONE = 12;
   public static final int BUTTON_LEFT = 13;
   public static final int BUTTON_RIGHT = 14;
   public static final int BUTTON_MIDDLE = 15;
   public static final int BUTTON_ONE = 16;
   public static final int BUTTON_LEFT_UP = 17;
   public static final int BUTTON_RIGHT_UP = 18;
   public static final int BUTTON_MIDDLE_UP = 19;
   public static final int BUTTON_ONE_UP = 20;
   public static final int BUTTON_LEFT_DN = 21;
   public static final int BUTTON_RIGHT_DN = 22;
   public static final int BUTTON_MIDDLE_DN = 23;
   public static final int BUTTON_ONE_DN = 24;
   public static final int BUTTON_LEFT_EB = 25;
   public static final int BUTTON_RIGHT_EB = 26;
   public static final int BUTTON_MIDDLE_EB = 27;
   public static final int BUTTON_SB_UP = 28;
   public static final int BUTTON_SB_DN = 29;
   public static final int BUTTON_SB_GUIDE = 30;
   public static final int BUTTON_SB_THUMB = 31;
   public static final int BUTTON_LAST = 31;

}