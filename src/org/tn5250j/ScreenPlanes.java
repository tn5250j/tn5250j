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

public class ScreenPlanes implements TN5250jConstants {

   Screen5250 scr;
   int screenSize;
   int numRows;
   int numCols;
   int errorLineNum;

   private char char0 = 0;
   private static final int initAttr = 32;
   private static final char initChar = 0;

   protected char[] screen;   // text plane
   protected char[] screenAttr;   // attribute plane
   protected char[] screenGUI;   // gui plane
   protected char[] screenIsAttr;
   protected char[] fieldExtended;
   protected char[] screenField;
   protected char[] screenColor;   // color plane
   protected char[] screenExtended;   // extended plane
   protected boolean[] screenIsChanged;

   protected char[] errorLine;
   protected char[] errorLineAttr;
   protected char[] errorLineIsAttr;

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
      screenAttr = new char[screenSize];
      screenIsAttr = new char[screenSize];
      screenGUI = new char[screenSize];
      screenColor = new char[screenSize];
      screenExtended = new char[screenSize];
      fieldExtended = new char[screenSize];
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
         errorLineAttr = new char[numCols];
         errorLineIsAttr = new char[numCols];

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
            setScreenCharAndAttr(r+x,errorLine[x],errorLineAttr[x],
                  					(errorLineIsAttr[x] == '1' ? true : false));
			}
			errorLine = null;
			errorLineAttr = null;
         errorLineIsAttr = null;
		}
	}

   protected boolean isErrorLineSaved() {
      return errorLine == null ? false : true;
   }

   protected void setScreenCharAndAttr(int pos, char c, int attr, boolean isAttr) {

      screen[pos] = c;
      screenAttr[pos] = (char)attr;
      disperseAttribute(pos,attr);
      screenIsAttr[pos] = (isAttr ? (char)1 : (char)0);
      screenGUI[pos] = initChar;

   }

   protected void setScreenAttr(int pos, int attr, boolean isAttr) {

      screenAttr[pos] = (char)attr;
      screenIsAttr[pos] = isAttr ? (char)1 : (char)0;
      disperseAttribute(pos,attr);
      screenGUI[pos] = initChar;

   }

   protected void setScreenAttr(int pos, int attr) {

      screenAttr[pos] = (char)attr;
      screenGUI[pos] = initChar;
      disperseAttribute(pos,attr);

   }

   protected final void setChar(int pos, char c) {
      screenIsChanged[pos] = screen[pos] == c ? false : true;
      //      if (isChanged)
      //         System.out.println(sChar[0] + " - " + c);
      screen[pos] = c;
      if (screenIsAttr[pos] == 1)
         setScreenCharAndAttr(pos,c,32,false);

   }

   protected final char getChar(int pos) {
      return screen[pos];
   }

   protected final int getCharAttr(int pos) {
      return screenAttr[pos];
   }

   protected final boolean isAttributePlace(int pos) {
      return screenIsAttr[pos] == 1 ? true : false;
   }

   public final void setUseGUI(int pos, int which) {

      screenIsChanged[pos] = screenGUI[pos] == which ? false : true;

      screenGUI[pos] = (char)which;
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

   /**
    * Return the data associated with the plane that is passed.
    *
    * @param from Position from which to start
    * @param to Position to end
    * @param plane From which plane to obtain the data
    * @return Character array containing the data requested
    */
   protected synchronized char[] getPlaneData(int from, int to, int plane) {

      int len = to - from;

      char[] planeChars = new char[len + 1];

      switch (plane) {
         case PLANE_TEXT:
            System.arraycopy(screen, from, planeChars, 0, len);
            break;
         case PLANE_ATTR:
            System.arraycopy(screenAttr, from, planeChars, 0, len);
            break;
         case PLANE_COLOR:
            System.arraycopy(screenColor, from, planeChars, 0, len);
            break;
         case PLANE_EXTENDED:
            System.arraycopy(screenExtended, from, planeChars, 0, len);
            break;
         case PLANE_EXTENDED_GRAPHIC:
            System.arraycopy(screenGUI, from, planeChars, 0, len);
            break;
         case PLANE_EXTENDED_FIELD:
            System.arraycopy(screenField, from, planeChars, 0, len);
            break;
         case PLANE_IS_ATTR_PLACE:
            System.arraycopy(screenIsAttr, from, planeChars, 0, len);
            break;
         default:
            System.arraycopy(screen, from, planeChars, 0, len);

      }
      return planeChars;

   }

   /**
    * Converts a linear presentation space position to its corresponding row.
    *
    * @param pos The position to be converted
    * @return The row which corresponds to the position given
    * @throws OhioException
    */
//   public int convertPosToRow(int pos) throws OhioException {
   public int convertPosToRow(int pos) {

      return (pos / numCols) + 1;

   }

   /**
    * Converts a linear presentation space position to its corresponding column.
    *
    * @param pos The position to be converted
    * @return The column which corresponds to the position given
    * @throws OhioException
    */
//   public int convertPosToColumn(int pos) throws OhioException {
   public int convertPosToColumn(int pos) {

      return (pos % numCols) + 1;

   }

   /**
    *
    * Converts a row and column coordinate to its corresponding linear position.
    *
    * @param row - The row of the coordinate
    * @param col - The column of the coordinate
    * @return The linear position which corresponds to the coordinate given.
    * @throws OhioException
    */
//   public int convertRowColToPos(int row, int col) throws OhioException {
   public int convertRowColToPos(int row, int col) {


      return (row - 1) * numCols + col -1;

   }


   private void fillExtendedFieldPlane(char[] plane,int start,int len) {

      for (int x = 0; x < len; x++ ) {
         plane[x] = (char)fieldExtended[start + x];
      }


   }

   /**
    * <p>
    *  GetScreen retrieves the various planes associated with the presentation
    *  space. The data is returned as a linear array of character values in the
    *  array provided. The array is not terminated by a null character except
    *  when data is retrieved from the text plane, in which case a single null
    *  character is appended.
    *  </p>
    *  <p>
    *  The application must supply a buffer for the returned data and the length
    *  of the buffer. Data is returned starting from the beginning of the
    *  presentation space and continuing until the buffer is full or the entire
    *  plane has been copied. For text plane data, the buffer must include one
    *  extra position for the terminating null character.
    *  <p>
    *
    * @param buffer
    * @param bufferLength
    * @param plane
    * @return The number of characters copied to the buffer
    * @throws OhioException
    */
   public synchronized int GetScreen(char buffer[], int bufferLength, int plane)
//                                       throws OhioException {
                                       {
      return GetScreen(buffer,bufferLength,0,screenSize,plane);

   }

   /**
    * <p>
    *  GetScreen retrieves the various planes associated with the presentation
    *  space. The data is returned as a linear array of character values in the
    *  array provided. The array is not terminated by a null character except
    *  when data is retrieved from the text plane, in which case a single null
    *  character is appended.
    * </p>
    * <p>
    * The application must supply a buffer for the returned data and the length
    * of the buffer. Data is returned starting from the given position and
    * continuing until the specified number of characters have been copied, the
    * buffer is full or the entire plane has been copied. For text plane data,
    * the buffer must include one extra position for the terminating null character.
    * </p>
    *
    * @param buffer
    * @param bufferLength
    * @param from
    * @param length
    * @param plane
    * @return The number of characters copied to the buffer
    * @throws OhioException
    */
   public synchronized int GetScreen(char buffer[], int bufferLength, int from,
                                    int length, int plane)
//                                    throws OhioException {
                                    {
//      if(buffer == null)
//         throw new OhioException(sessionVT.getSessionConfiguration(),
//                     OhioScreen.class.getName(), "osohio.screen.ohio00300", 1);
      if(buffer == null)
         return 0;

      int min = Math.min(Math.min(buffer.length, bufferLength), screenSize);
      if ((from + min) > screenSize) {
         min = screenSize - from;
      }

      char[] pd = getPlaneData(from,from + min,plane);
      if(pd != null) {
         System.arraycopy(pd, 0, buffer, 0, min);
         return pd.length;
      }

      return 0;
   }

   /**
    * <p>
    *  GetScreen retrieves the various planes associated with the presentation
    *  space. The data is returned as a linear array of character values in the
    *  array provided. The array is not terminated by a null character except
    *  when data is retrieved from the text plane, in which case a single null
    *  character is appended.
    *  </p>
    *  <p>
    *  The application must supply a buffer for the returned data and the length
    *  of the buffer. Data is returned starting from the given coordinates and
    *  continuing until the specified number of characters have been copied,
    *  the buffer is full, or the entire plane has been copied. For text plane
    *  data, the buffer must include one extra position for the terminating null
    *  character.
    *  </p>
    *
    * @param buffer
    * @param bufferLength
    * @param row
    * @param col
    * @param length
    * @param plane
    * @return The number of characters copied to the buffer.
    * @throws OhioException
    */
   public synchronized int GetScreen(char buffer[], int bufferLength, int row,
                                       int col, int length, int plane)
//                                       throws OhioException {
                                       {
      // Call GetScreen function after converting row and column to
      // a position.
      return GetScreen(buffer,bufferLength, convertRowColToPos(row,col),
                           length, plane);
   }

   /**
    * <p>
    *  GetScreenRect retrieves data from the various planes associated with the
    *  presentation space. The data is returned as a linear array of character
    *  values in the buffer provided.
    *  </p>
    *
    * <p>
    * The application supplies two positions that represent opposing corners of
    * a rectangle within the presentation space. The starting and ending
    * positions can have any spatial relationship to each other. The data
    * returned starts from the row containing the upper-most point to the row
    * containing the lower-most point, and from the left-most column to the
    * right-most column.
    * </p>
    * <p>
    * The specified buffer must be at least large enough to contain the number
    * of characters in the rectangle. If the buffer is too small, no data is
    * copied and zero is returned by the method. Otherwise, the method returns
    * the number of characters copied.
    * </p>
    *
    * @param buffer
    * @param bufferLength
    * @param startPos
    * @param endPos
    * @param plane
    * @return The number of characters copied to the buffer
    * @throws OhioException
    */
   protected int GetScreenRect(char buffer[], int bufferLength,
                                             int startPos, int endPos, int plane)
//                                             throws OhioException {
                                             {
      // We will use the row,col routine here because it is easier to use
      // row colum than it is for position since I wrote the other first and
      // am to lazy to implement it here
      // Maybe it would be faster to do it the other way?
      int startRow = convertPosToRow(startPos);
      int startCol = convertPosToColumn(startPos);
      int endRow = convertPosToRow(endPos);
      int endCol = convertPosToColumn(endPos);
      return GetScreenRect(buffer, bufferLength, startRow, startCol,
                                 endRow, endCol, plane);

   }

   /**
    * <p>
    *  GetScreenRect retrieves data from the various planes associated with the
    *  presentation space. The data is returned as a linear array of character
    *  values in the buffer provided. The buffer is not terminated by a null
    *  character.
    * </p>
    * <p>
    * The application supplies two coordinates that represent opposing corners
    * of a rectangle within the presentation space. The starting and ending
    * coordinates can have any spatial relationship to each other. The data
    * returned starts from the row containing the upper-most point to the row
    * containing the lower-most point, and from the left-most column to the
    * right-most column.
    * </p>
    * <p>
    * The specified buffer must be at least large enough to contain the number
    * of characters in the rectangle. If the buffer is too small, no data is
    * copied and zero is returned by the method. Otherwise, the method returns
    * the number of characters copied.
    * </p>
    *
    * @param buffer
    * @param bufferLength
    * @param startRow
    * @param startCol
    * @param endRow
    * @param endCol
    * @param plane
    * @return The number characters copied to the buffer
    * @throws OhioException
    */
   protected int GetScreenRect(char buffer[], int bufferLength,
                                             int startRow, int startCol,
                                             int endRow, int endCol, int plane)
//                                             throws OhioException {
                                             {
      // number of bytes obtained
      int numBytes = 0;

      // lets check the row range.  If they are reversed then we need to
      // place them in the correct order.
      if(startRow > endRow) {
         int r = startRow;
         startRow = endRow;
         endRow = r;
      }
      // lets check the column range.  If they are reversed then we need to
      // place them in the correct order.
      if(startCol > endCol) {
         int c = startCol;
         startCol = endCol;
         endCol = c;
      }
      int numCols = (endCol - startCol) + 1;
      int numRows = (endRow - startRow) + 1;

      // lets make sure it is within the bounds of the character array passed
      //  if not the return as zero bytes where read as per documentation.
      if(numCols * numRows <= bufferLength) {

         // make sure it is one larger.  I guess for other languanges to
         // reference like in C which is terminated by a zero byte at the end
         // of strings.
         char cb[] = new char[numCols + 1];
         int charOffset = 0;
         int bytes = 0;

         // now let's loop through and get the screen information for
         //  each row;
         for(int row = startRow; row <= endRow;) {
             if((bytes = GetScreen(cb, cb.length, row, startCol, numCols, plane)) != 0) {
                 System.arraycopy(cb, 0, buffer, charOffset, numCols);
             }
             row++;
             charOffset += numCols;
             // make sure we count the number of bytes returned
             numBytes += bytes;
         }

      }

      return numBytes;
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

   public final void disperseAttribute(int pos, int attr) {

//      Screen5250 s = scr;
//
//      boolean colSep = false;
//      boolean underLine = false;
//      boolean nonDisplay = false;
//      java.awt.Color fg;
//      java.awt.Color bg;

      char c = 0;
      char cs = 0;
      char ul = 0;
      char nd = 0;

      if(attr == 0)
         return;

      switch(attr) {
         case 32: // green normal
            c = (COLOR_BG_BLACK << 8 & 0xff00) |
            (COLOR_FG_GREEN & 0xff);
            break;

         case 33: // green/revers
            c = (COLOR_BG_GREEN << 8 & 0xff00) |
            (COLOR_FG_BLACK & 0xff);
            break;

         case 34: // white normal
            c = (COLOR_BG_BLACK << 8 & 0xff00) |
            (COLOR_FG_WHITE & 0xff);
            break;

         case 35: // white/reverse
            c = (COLOR_BG_WHITE << 8 & 0xff00) |
            (COLOR_FG_BLACK & 0xff);
            break;

         case 36: // green/underline
            c = (COLOR_BG_BLACK << 8 & 0xff00) |
            (COLOR_FG_GREEN & 0xff);
            ul = EXTENDED_5250_UNDERLINE;
            break;

         case 37: // green/reverse/underline
            c = (COLOR_BG_GREEN << 8 & 0xff00) |
            (COLOR_FG_BLACK & 0xff);
            ul = EXTENDED_5250_UNDERLINE;
            break;

         case 38: // white/underline
            c = (COLOR_BG_BLACK << 8 & 0xff00) |
            (COLOR_FG_WHITE & 0xff);
            ul = EXTENDED_5250_UNDERLINE;
            break;

         case 39:
            nd = EXTENDED_5250_NON_DSP;
            break;

         case 40:
         case 42: // red/normal
            c = (COLOR_BG_BLACK << 8 & 0xff00) |
            (COLOR_FG_RED & 0xff);
            break;

         case 41:
         case 43: // red/reverse
            c = (COLOR_BG_RED << 8 & 0xff00) |
            (COLOR_FG_BLACK & 0xff);
            break;

         case 44:
         case 46: // red/underline
            c = (COLOR_BG_BLACK << 8 & 0xff00) |
            (COLOR_FG_RED & 0xff);
            ul = EXTENDED_5250_UNDERLINE;
            break;

         case 45: // red/reverse/underline
            c = ( COLOR_BG_RED << 8 & 0xff00) |
            ( COLOR_FG_BLACK & 0xff);
            ul = EXTENDED_5250_UNDERLINE;
            break;

         case 47:
            nd = EXTENDED_5250_NON_DSP;
            break;

         case 48:
            c = (COLOR_BG_BLACK << 8 & 0xff00) |
            (COLOR_FG_CYAN & 0xff);
            cs = EXTENDED_5250_COL_SEP;
            break;

         case 49:
            c = (COLOR_BG_CYAN << 8 & 0xff00) |
            (COLOR_FG_BLACK & 0xff);
            cs = EXTENDED_5250_COL_SEP;
            break;

         case 50:
            c = (COLOR_BG_BLACK << 8 & 0xff00) |
            (COLOR_FG_YELLOW & 0xff);
            cs = EXTENDED_5250_COL_SEP;
            break;

         case 51:
            c = (COLOR_BG_YELLOW << 8 & 0xff00) |
            (COLOR_FG_BLACK & 0xff);
            cs = EXTENDED_5250_COL_SEP;
            break;

         case 52:
            c = ( COLOR_BG_BLACK << 8 & 0xff00) |
            ( COLOR_FG_CYAN & 0xff);
//            colSep = true;
            ul = EXTENDED_5250_UNDERLINE;
            break;

         case 53:
            c = ( COLOR_BG_CYAN << 8 & 0xff00) |
            ( COLOR_FG_BLACK & 0xff);
//            colSep = true;
            ul = EXTENDED_5250_UNDERLINE;
            break;

         case 54:
            c = ( COLOR_BG_BLACK << 8 & 0xff00) |
            ( COLOR_FG_YELLOW & 0xff);
//            colSep = true;
            ul = EXTENDED_5250_UNDERLINE;
            break;

         case 55:
            nd = EXTENDED_5250_NON_DSP;
            break;

         case 56: // pink
            c = ( COLOR_BG_BLACK << 8 & 0xff00) |
            ( COLOR_FG_MAGENTA & 0xff);
            break;

         case 57: // pink/reverse
            c = ( COLOR_BG_MAGENTA << 8 & 0xff00) |
            ( COLOR_FG_BLACK & 0xff);
            break;

         case 58: // blue/reverse
            c = ( COLOR_BG_BLACK << 8 & 0xff00) |
            ( COLOR_FG_BLUE & 0xff);
            break;

         case 59: // blue
            c = ( COLOR_BG_BLUE << 8 & 0xff00) |
            ( COLOR_FG_BLACK & 0xff);
            break;

         case 60: // pink/underline
            c = ( COLOR_BG_BLACK << 8 & 0xff00) |
            ( COLOR_FG_MAGENTA & 0xff);
            ul = EXTENDED_5250_UNDERLINE;
            break;

         case 61: // pink/reverse/underline
            c = ( COLOR_BG_MAGENTA << 8 & 0xff00) |
            ( COLOR_FG_BLACK & 0xff);
            ul = EXTENDED_5250_UNDERLINE;
            break;

         case 62: // blue/underline
            c = ( COLOR_BG_BLACK << 8 & 0xff00) |
            ( COLOR_FG_BLUE & 0xff);
            ul = EXTENDED_5250_UNDERLINE;
            break;

         case 63:  // nondisplay
            nd = EXTENDED_5250_NON_DSP;
            break;
         default:
            c = ( COLOR_BG_BLACK << 8 & 0xff00) |
            ( COLOR_FG_YELLOW & 0xff);
            break;

      }

      screenColor[pos] = c;
      screenExtended[pos] = (char)(ul | cs | nd);
   }

   protected boolean checkHotSpots () {

   	Screen5250 s = scr;
   	int lenScreen = scr.getScreenLength();
   	boolean hs = false;
	   boolean retHS = false;
	   StringBuffer hsMore = s.getHSMore();
	   StringBuffer hsBottom = s.getHSBottom();
	//   Rectangle2D mArea = new Rectangle2D.Float(0,0,0,0);
	//   Rectangle2D mwArea = new Rectangle2D.Float(0,0,0,0);
	//   ArrayList mArray = new ArrayList(10);

	      for (int x = 0; x < lenScreen; x++) {

	         hs =false;
	         if (s.isInField(x,false))
	            continue;

	         // First check for PF keys
	         if (x > 0 && screen[x] == 'F') {
	            if (screen[x + 1] >= '0' &&
	                     screen[x + 1] <= '9' &&
	                      screen[x - 1] <= ' ' &&
	                      (screenExtended[x] & EXTENDED_5250_NON_DSP) == 0) {

	               if (screen[x + 2] >= '0' &&
	                     screen[x + 2] <= '9' &&
	                      (screen[x + 3] == '=' ||
	                       screen[x + 3] == '-' ||
	                       screen[x + 3] == '/') )
	                  hs = true;
	               else
	                  if (   screen[x + 2] == '=' ||
	                         screen[x + 3] == '-' ||
	                         screen[x + 3] == '/')
	                     hs = true;

	               if (hs) {
	                  screenGUI[x] = BUTTON_LEFT;

	                  int ns = 0;
	                  int row = x / numCols;
	                  while (ns < 2 && ++x / numCols == row) {
	                     if (screen[x] <= ' ')
	                        ns++;
	                     else
	                        ns = 0;
	                     if (ns <2)
	                        screenGUI[x] = BUTTON_MIDDLE;

	                  }

	                  // now lets go back and take out gui's that do not belong
	                  while (screen[--x] <= ' ') {
	                     screenGUI[x] = NO_GUI;
	                  }
	                  screenGUI[x] = BUTTON_RIGHT;

	               }
	            }
	         }

	         // now lets check for menus
	         if (!hs && x > 0 && x < lenScreen - 2 &&
	               screen[x] == '.' &&
	               screenGUI[x] == NO_GUI &&
	               (screenExtended[x] & EXTENDED_5250_UNDERLINE) == 0 &&
	               (screenExtended[x] & EXTENDED_5250_NON_DSP) == 0
	            ) {

	            int os = 0;
	            if ((os = isOption(screen,x,lenScreen,2,3,'.') )> 0) {
	               hs = true;

	               int stop = x;
	               int ns = 0;
	               int row = stop / numCols;

	               while (++stop / numCols == row &&
	                            (screen[stop] >= ' ' ||
	                             screen[stop] == 0x0) ) {

	                  if (screen[stop] <= ' ') {
	                     ns++;
	                  }
	                  else
	                     ns = 0;

	                  if (screen[stop] == '.') {
	                     int io = 0;
	                     if ((io = isOption(screen,stop,lenScreen,2,3,'.')) > 0) {

	                        stop = io;
	                        break;
	                     }
	                  }

	                  if (ns > 3)
	                     break;
	               }

	               screenGUI[++os] = BUTTON_LEFT;
	               s.setDirty(os);

	               while (++os < stop) {
	                  screenGUI[os] = BUTTON_MIDDLE;
	                  s.setDirty(os);
	               }

	               // now lets go back and take out gui's that do not belong
	               while (screen[--stop] <= ' ') {
	                  screenGUI[stop] = NO_GUI;
	                  s.setDirty(stop);
	               }
	               screenGUI[stop] = BUTTON_RIGHT;
	               s.setDirty(stop);

	            }
	         }

	         // now lets check for options.
	         if (!hs && x > 0 && x < lenScreen - 2 &&
	               screen[x] == '=' &&
	               screenGUI[x] == NO_GUI &&
	               (screenExtended[x] & EXTENDED_5250_UNDERLINE) == 0 &&
	               (screenExtended[x] & EXTENDED_5250_NON_DSP) == 0
	            ) {

	            int os = 0;
	            if ((os = isOption(screen,x,lenScreen,2,2,'=') )> 0) {
	               hs = true;

	               int stop = x;
	               int ns = 0;
	               int row = stop / numCols;

	               while (++stop / numCols == row &&
	                            screen[stop] >= ' ') {

	                  if (screen[stop] == ' ') {
	                     ns++;
	                  }
	                  else
	                     ns = 0;

	                  if (screen[stop] == '=') {
	                     int io = 0;
	                     if ((io = isOption(screen,stop,lenScreen,2,2,'=')) > 0) {

	                        stop = io;
	                        break;
	                     }
	                  }

	                  if (ns > 2)
	                     break;
	               }

	               screenGUI[++os] = BUTTON_LEFT;
	               s.setDirty(os);

	               while (++os < stop) {
	                  screenGUI[os] = BUTTON_MIDDLE;
	                  s.setDirty(os);

	               }

	               // now lets go back and take out gui's that do not belong
	               while (screen[--stop] <= ' ') {
	                  screenGUI[stop] = NO_GUI;
	                  s.setDirty(stop);

	               }
	               screenGUI[stop] = BUTTON_RIGHT;
	               s.setDirty(stop);
	            }
	         }

	         // now lets check for More... .

	         if (!hs && x > 2 && x < lenScreen - hsMore.length() &&
	               screen[x] == hsMore.charAt(0) &&
	               screen[x - 1] <= ' ' &&
	               screen[x - 2] <= ' ' &&
	               screenGUI[x] == NO_GUI &&
	               (screenExtended[x] & EXTENDED_5250_NON_DSP) == 0
	            ) {

	            boolean mFlag = true;
	            int ms = hsMore.length();
	            int mc = 0;
	            while (++mc < ms) {
	               if (screen[x+mc] != hsMore.charAt(mc)) {
	                  mFlag = false;
	                  break;
	               }

	            }

	            if (mFlag) {
	               hs = true;

	               screenGUI[x] = BUTTON_LEFT_DN;

	               while (--ms > 0) {
	                  screenGUI[++x] = BUTTON_MIDDLE_DN;

	               }
	               screenGUI[x] = BUTTON_RIGHT_DN;
	            }
	         }

	         // now lets check for Bottom .
	         if (!hs && x > 2 && x < lenScreen - hsBottom.length() &&
	               screen[x] == hsBottom.charAt(0) &&
	               screen[x - 1]  <= ' ' &&
	               screen[x - 2] <= ' ' &&
	               screenGUI[x] == NO_GUI &&
	               (screenExtended[x] & EXTENDED_5250_NON_DSP) == 0
	            ) {

	            boolean mFlag = true;
	            int bs = hsBottom.length();
	            int bc = 0;
	            while (++bc < bs) {
	               if (screen[x+bc] != hsBottom.charAt(bc)) {
	                  mFlag = false;
	                  break;
	               }

	            }

	            if (mFlag) {
	               hs = true;

	               screenGUI[x] = BUTTON_LEFT_UP;

	               while (--bs > 0) {
	                  screenGUI[++x] = BUTTON_MIDDLE_UP;

	               }
	               screenGUI[x] = BUTTON_RIGHT_UP;
	            }
	         }

	         // now lets check for HTTP:// .
	         if (!hs && x > 0 && x < lenScreen - 7 &&
	               Character.toLowerCase(screen[x]) == 'h' &&
	               screen[x - 1] <= ' ' &&
	               screenGUI[x] == NO_GUI &&
	               (screenExtended[x] & EXTENDED_5250_NON_DSP) == 0
	            ) {

	            if (Character.toLowerCase(screen[x+1]) == 't' &&
	                  Character.toLowerCase(screen[x+2]) == 't' &&
	                  Character.toLowerCase(screen[x+3]) == 'p' &&
	                  screen[x+4] == ':' &&
	                  screen[x+5] == '/' &&
	                  screen[x+6] == '/' ) {

	               hs = true;

	               screenGUI[x] = BUTTON_LEFT_EB;

	               while (screen[++x] > ' ') {
	                  screenGUI[x] = BUTTON_MIDDLE_EB;

	               }
	               screenGUI[--x] = BUTTON_RIGHT_EB;
	            }
	         }
	         if (!retHS && hs)
	            retHS = true;

	      }


	//      int pos = 0;
	//
	//      mArea.setRect(0,0,0,0);
	//      mwArea.setRect(0,0,0,0);
	//      for (int k = 0; k < numCols;k++) {
	////         System.out.println(k);
	//         pos =k;
	//         boolean gui = false;
	//         for (int j=0; j < 19; j++) {
	//            if (screen[pos].whichGui != NO_GUI)
	////                  System.out.print(screen[pos].getChar());
	//
	//                  mwArea.setRect(screen[pos].x,
	//                                    screen[pos].y,
	//                                    screen[pos].x,
	//                                    screen[pos].y);
	//
	//                  if (mArea.getWidth() == 0) {
	//                     mArea.setRect(mwArea);
	//                  }
	//                  else {
	//                     double x1 = Math.min(mArea.getX(), mwArea.getX());
	//                     double x2 = Math.max(mArea.getWidth(), mwArea.getWidth());
	//                     double y1 = Math.min(mArea.getY(), mwArea.getY());
	//                     double y2 = Math.max(mArea.getHeight(), mwArea.getHeight());
	//                     mArea.setRect(x1, y1, x2, y2);
	//
	//                  }
	//            pos += numCols;
	//         }
	//      }
	//
	//      if (mwArea.getWidth() != 0) {
	//         System.out.println("Mennu area is " +
	//                              s.getRow(s.getRowColFromPoint((int)mArea.getX(),(int)mArea.getY())) + "," +
	//                              s.getCol(s.getRowColFromPoint((int)mArea.getX(),(int)mArea.getY())) + "," +
	//                              s.getRow(s.getRowColFromPoint(
	//                                       (int)mArea.getWidth(),
	//                                       (int)mArea.getHeight())) + "," +
	//                              s.getCol(s.getRowColFromPoint(
	//                                       (int)mArea.getWidth(),
	//                                       (int)mArea.getHeight()) ));
	//      }


      return retHS;
	}

	private int isOption(char[] screen,
	                              int x,
	                              int lenScreen,
	                              int numPref,
	                              int numSuff,
	                              char suff) {
	   boolean hs =true;
	   int sp = x;
	   int os = 0;
	   // check to the left for option
	   while (--sp >=0 &&  screen[sp] <= ' ' ) {

	      if (x - sp > numPref || screen[sp] == suff||
	               screen[sp] == '.' ||
	               screen[sp] == '*') {
	         hs =false;
	//         System.out.println(" hs1 false " + screen[sp].getChar() + " " + sp);
	         break;
	      }
	   }

	   // now lets check for how long the option is it has to be numPref or less
	   os = sp;
	   while (hs && --os > 0 && screen[os] > ' ' ) {
	//      System.out.println(" hs2 length " + (sp-os) + " " + screen[os].getChar());

	      if (sp - os >= numPref || screen[os] == suff ||
	               screen[os] == '.' ||
	               screen[os] == '*') {
	         hs = false;
	//         System.out.println(" hs2 false at " + (sp-os) + " " + screen[os].getChar());
	         break;
	      }
	   }
	   if (sp - os > 1 && !Character.isDigit(screen[os+1])) {
	      hs = false;
	   }

	   sp = x;

	   if (Character.isDigit(screen[sp+1]))
	      hs = false;
	   // now lets make sure there are no more than numSuff spaces after option
	   while (hs && (++sp < lenScreen && screen[sp] <= ' '
	                  || screen[sp] == suff )) {
	      if (sp - x >= numSuff || screen[sp] == suff ||
	                  screen[sp] == '.' ||
	               screen[sp] == '*') {
	         hs =false;
	//         System.out.println(" hs3 false at " + sp + " " + screen[sp].getChar());
	         break;
	      }
	   }
	   if (hs && !Character.isLetterOrDigit(screen[sp]))
	      hs = false;
	   if (hs) {
	      return os;
	   }
	   return -1;
	}

}