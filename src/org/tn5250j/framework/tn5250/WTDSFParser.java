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
package org.tn5250j.framework.tn5250;

import static org.tn5250j.TN5250jConstants.BOTTOM;
import static org.tn5250j.TN5250jConstants.GUI_LEFT;
import static org.tn5250j.TN5250jConstants.GUI_RIGHT;
import static org.tn5250j.TN5250jConstants.LOWER_LEFT;
import static org.tn5250j.TN5250jConstants.LOWER_RIGHT;
import static org.tn5250j.TN5250jConstants.NO_GUI;
import static org.tn5250j.TN5250jConstants.NR_REQUEST_ERROR;
import static org.tn5250j.TN5250jConstants.UPPER;
import static org.tn5250j.TN5250jConstants.UPPER_LEFT;
import static org.tn5250j.TN5250jConstants.UPPER_RIGHT;

import java.util.ArrayList;
import java.util.List;

import org.tn5250j.encoding.ICodePage;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

/**
 *
 * Write To Display Structured Field:
 *
 * This module will parse the structured field information for enhanced
 * emulation mode.
 *
 */
public class WTDSFParser {

   private Screen5250 screen52;
   private tnvt vt;
   private ICodePage codePage;
   int pos;
   byte[] segment;
   int length;
   boolean error;
   boolean guiStructsExist;

   private TN5250jLogger log = TN5250jLogFactory.getLogger(this.getClass());

   private final List<Window> guiStructs = new ArrayList<Window>(3);
   private final List<ChoiceField> choices = new ArrayList<ChoiceField>(3);


   WTDSFParser (tnvt vt) {

      this.vt = vt;
      screen52 = vt.screen52;
      codePage = vt.codePage;

   }

	protected class ChoiceField {

		int x;
		int y;
      int row;
      int col;
		int width;
		int height;
		char mnemonic;
		int fieldId;
      int selectIndex;

      ChoiceField(int row, int col, int fldRow, int fldCol) {
         x = row;
         y = col;
         row = fldRow;
         col = fldCol;
      }
	}

   protected class Window {

      byte[] window;
      int pos;

      Window(byte[] seg, int pos) {

         //log.info("window created at " + pos);
         window = seg;
         this.pos = pos;
         guiStructsExist = true;
      }

   }

	protected void addChoiceField(int row,int col,int fldRow, int fldCol, String text) {

		ChoiceField cf = new ChoiceField(row,col, fldRow, fldCol);
		cf.fieldId = screen52.getScreenFields().getCurrentField().getFieldId();
		choices.add(cf);

	}

   protected boolean isGuisExists () {

      return guiStructsExist;

   }

   protected byte[] getSegmentAtPos(int pos) {
      int len = guiStructs.size();
      for (int x = 0; x < len; x++) {
         Window w = guiStructs.get(x);
         if (w.pos == pos)
            return w.window;
      }

      return null;

   }

   protected void clearGuiStructs() {

      guiStructs.clear();
   }

   protected boolean parseWriteToDisplayStructuredField(byte[] seg) {

//      bk = vt.bk;

      error = false;
      boolean done = false;
      boolean windowDefined = false;
//      int nextone;
      pos = 0;
      segment = seg;

//      try {
         length = (( segment[pos++] & 0xff )<< 8 | (segment[pos++] & 0xff));

         while (!done) {
            int s =    segment[pos++] & 0xff;
            switch (s) {

               case 0xD9:     // Class Type 0xD9 - Create Window

                  switch (segment[pos++]) {
                     case 0x50:      // Define Selection Field

                        defineSelectionField(length);
                        done = true;
                        break;
                     case 0x51:      // Create Window

                        guiStructs.add(new Window(segment, screen52.getLastPos()));

                        boolean cr = false;
                        int rows = 0;
                        int cols = 0;
                        // pull down not supported yet
                        if ((segment[pos++] & 0x80) == 0x80)
                           cr = true;  // restrict cursor
                        pos++; // get reserved field pos 6
                        pos++; // get reserved field pos 7
                        rows = segment[pos++]; // get window depth rows pos 8
                        cols = segment[pos++]; // get window width cols pos 9
                        length -= 9;
                        if (length == 0) {
                           done = true;
//                           System.out.println("Create Window");
//                           System.out.println("   restrict cursor " + cr);
//                           System.out.println(" Depth = " + rows + " Width = " + cols);
//                           screen52.createWindow(rows,cols,1,true,32,58,
                           createWindow(rows,cols,1,true,32,58,
                                             '.',
                                             '.',
                                             '.',
                                             ':',
                                             ':',
                                             ':',
                                             '.',
                                             ':');
                           windowDefined = true;
                           break;
                        }

                        // pos 10 is Minor Structure
                        int ml = 0;
                        int type = 0;
                        int lastPos = screen52.getLastPos();
//                        if (cr)
//                           screen52.setPendingInsert(true,
//                                          screen52.getCurrentRow(),
//                                          screen52.getCurrentCol());
                        int mAttr = 0;
                        int cAttr = 0;

                        while (length > 0) {

                           // get minor length
                           ml = ( segment[pos++] & 0xff );
                           length -= ml;

                           // only normal windows are supported at this time
                           type = segment[pos++];

                           switch (type) {

                              case 0x01 : // Border presentation
                                 boolean gui = false;
                                 if ((segment[pos++] & 0x80) == 0x80)
                                    gui = true;
                                 mAttr = segment[pos++];
                                 cAttr = segment[pos++];

                                 char ul = '.';
                                 char upper = '.';
                                 char ur = '.';
                                 char left = ':';
                                 char right = ':';
                                 char ll = ':';
                                 char bottom = '.';
                                 char lr = ':';

                                 // if minor length is greater than 5 then
                                 //    the border characters are specified
                                 if (ml > 5) {
                                    ul = codePage.ebcdic2uni(segment[pos++]);
//                                    ul = getASCIIChar(segment[pos++]);
                                    if (ul == 0)
                                       ul = '.';

                                    upper = codePage.ebcdic2uni(segment[pos++]);
//                                    upper = getASCIIChar(segment[pos++]);
                                    if (upper == 0)
                                       upper = '.';

                                    ur = codePage.ebcdic2uni(segment[pos++]);
//                                    ur = getASCIIChar(segment[pos++]);
                                    if (ur == 0)
                                       ur = '.';

                                    left = codePage.ebcdic2uni(segment[pos++]);
//                                    left = getASCIIChar(segment[pos++]);
                                    if (left == 0)
                                       left = ':';

                                    right = codePage.ebcdic2uni(segment[pos++]);
//                                    right = getASCIIChar(segment[pos++]);
                                    if (right == 0)
                                       right = ':';

                                    ll = codePage.ebcdic2uni(segment[pos++]);
//                                    ll = getASCIIChar(segment[pos++]);
                                    if (ll == 0)
                                       ll = ':';

                                    bottom = codePage.ebcdic2uni(segment[pos++]);
//                                    bottom = getASCIIChar(segment[pos++]);
                                    if (bottom == 0)
                                       bottom = '.';

                                    lr = codePage.ebcdic2uni(segment[pos++]);
//                                    lr = getASCIIChar(segment[pos++]);
                                    if (lr == 0)
                                       lr = ':';
                                 }

//                                 System.out.println("Create Window");
//                                 System.out.println("   restrict cursor " + cr);
//                                 System.out.println("   Depth = " + rows + " Width = " + cols);
//                                 System.out.println("   type = " + type + " gui = " + gui);
//                                 System.out.println("   mono attr = " + mAttr + " color attr = " + cAttr);
//                                 System.out.println("   ul = " + ul + " upper = " + upper +
//                                                         " ur = " + ur +
//                                                         " left = " + left +
//                                                         " right = " + right +
//                                                         " ll = " + ll +
//                                                         " bottom = " + bottom +
//                                                         " lr = " + lr
//                                                         );
//                                 screen52.createWindow(rows,cols,type,gui,mAttr,cAttr,
                                       createWindow(rows,cols,type,gui,mAttr,cAttr,
                                                      ul,
                                                      upper,
                                                      ur,
                                                      left,
                                                      right,
                                                      ll,
                                                      bottom,
                                                      lr);
                                 windowDefined = true;
                              break;
         //
         //  The following shows the input for window with a title
         //
         //      +0000 019A12A0 00000400 00020411 00200107  .?.?..?...?..?.
         //      +0010 00000018 00000011 06131500 37D95180  ........?.?..R??
         //      +0020 00000A24 0D018023 23404040 40404040  ..??..???
         //      +0030 40211000 000000D7 C2C1D9C4 C5D4D67A   \uFFFD.....PBARDEMO:
         //      +0040 40D79996 879985A2 A2408281 99408485   Progress bar de
         //      +0050 94961108 1520D5A4 94828599 40968640  mo.???Number of
         //      +0060 8595A399 8985A24B 4B4B4B4B 4B7A2011  entries......:?.
         //      +0070 082E2040 404040F5 F0F06BF0 F0F02011  ?.?    500,000?.
         //      +0080 091520C3 A4999985 95A34085 95A399A8  \uFFFD??Current entry
         //      +0090 4095A494 8285994B 4B4B7A20 11092E20   number...:?.\uFFFD.?
         //      +00A0 40404040 4040F56B F0F0F020 110A1520        5,000?.???
         //      +00B0 D9859481 89958995 87408595 A3998985  Remaining entrie
         //      +00C0 A24B4B4B 4B4B4B7A 20110A2E 20404040  s......:?.?.?
         //      +00D0 40F4F9F5 6BF0F0F0 20110C15 20E2A381   495,000?..??Sta
         //      +00E0 99A340A3 8994854B 4B4B4B4B 4B4B4B4B  rt time.........
         //      +00F0 4B4B4B4B 7A20110C 2F2040F7 7AF5F37A  ....:?...? 7:53:

                              case 0x10 : // Window title/footer
                                 if (!windowDefined) {
//                                    screen52.createWindow(rows,cols,1,true,32,58,
                                    guiStructs.add(new Window(segment, screen52.getLastPos()));
                                          createWindow(rows,cols,1,true,32,58,
                                                         '.',
                                                         '.',
                                                         '.',
                                                         ':',
                                                         ':',
                                                         ':',
                                                         '.',
                                                         ':');
                                    windowDefined = true;
                                 }

                                 byte orientation = segment[pos++];
                                 mAttr = segment[pos++];
                                 cAttr = segment[pos++];

                                 //reserved
                                 pos++;
                                 ml -= 6;

                                 StringBuffer hfBuffer = new StringBuffer(ml);
                                 while (ml-- > 0) {
                                    //LDC - 13/02/2003 - Convert it to unicode
                                    hfBuffer.append(codePage.ebcdic2uni(segment[pos++]));
//                                    hfBuffer.append(getASCIIChar(segment[pos++]));

                                 }

                                 log.debug(
                                    " orientation " + Integer.toBinaryString(orientation) +
                                    " mAttr " + mAttr +
                                    " cAttr " + cAttr +
                                    " Header/Footer " + hfBuffer);
                                 screen52.writeWindowTitle(lastPos,
                                                            rows,
                                                            cols,
                                                            orientation,
                                                            mAttr,
                                                            cAttr,
                                                            hfBuffer);
                                 break;
                              default:
                            	  log.warn("Invalid Window minor structure");
                              length = 0;
                              done = true;
                           }

                        }

                        done = true;

                        break;

                     case 0x53:      // Scroll Bar
                        int sblen = 15;
                        byte sbflag = segment[pos++];  // flag position 5

                        pos++;  // reserved position 6

                        // position 7,8
                        int totalRowScrollable =  (( segment[pos++] & 0xff )<< 8
                                                | (segment[pos++] & 0xff));

                        // position 9,10
                        int totalColScrollable =  (( segment[pos++] & 0xff )<< 8
                                                | (segment[pos++] & 0xff));

                        // position 11,12
                        int sliderRowPos =  (( segment[pos++] & 0xff )<< 8
                                                | (segment[pos++] & 0xff));

                        // position 13,14
                        int sliderColPos =  (( segment[pos++] & 0xff )<< 8
                                                | (segment[pos++] & 0xff));

                        // position 15
                        int sliderRC = segment[pos++];

                        screen52.createScrollBar(sbflag,totalRowScrollable,
                                                   totalColScrollable,
                                                   sliderRowPos,
                                                   sliderColPos,
                                                   sliderRC);
                        length -= 15;

                        done = true;

                        break;

                     case 0x5B:      // Remove GUI ScrollBar field

                        pos++; // reserved must be set to off pos 5
                        pos++; // reserved must be set to zero pos 6

                        done = true;
                        break;

                     case 0x5F:      // Remove All GUI Constructs
                        log.info("remove all gui contructs");
                        clearGuiStructs();
                        guiStructsExist = false;
                        int len = 4;
                        int d = 0;
                        length -= s;
                        while (--len > 0)
                           d = segment[pos++];
//                        if (length > 0) {
//                           len = (segment[pos++] & 0xff )<< 8;
//
//                           while (--len > 0)
//                              d = segment[pos++];
//                        }

                        screen52.clearGuiStuff();
                        // per 14.6.13.4 documentation we should clear the
                        //    format table after this command
                        screen52.clearTable();
                        done = true;
                        break;
                     case 0x59:	// remove gui window
                        log.info(" remove window at " + screen52.getCurrentPos());
                        done = true;
                        break;

                     case 0x60:      // Erase/Draw Grid Lines - not supported
                                    // do not know what they are
                                    // as of 03/11/2002 we should not be getting
                                    // this anymore but I will leave it here
                                    //  just in case.
//                        System.out.println("erase/draw grid lines " + length);
                        len = 6;
                        d = 0;
                        length -= 9;
                        while (--len > 0)
                           d = segment[pos++];
                           if (length > 0) {
                              len = (segment[pos++] & 0xff )<< 8;

                           while (--len > 0) {
                              d = segment[pos++];
                           }
                        }
                        done = true;
                        break;
                     default:
                        vt.sendNegResponse(NR_REQUEST_ERROR,0x03,0x01,0x01,"invalid wtd structured field sub command "
                                                   + ( pos - 1));
//                                                   + bk.getByteOffset(-1));
                        error = true;
                        break;
                  }
                  break;

               default:
                  vt.sendNegResponse(NR_REQUEST_ERROR,0x03,0x01,0x01,
                              "invalid wtd structured field command "
                               + (pos - 1));
//                               + bk.getByteOffset(-1));
                  error = true;
                  break;
            }

            if (error)
               done = true;

         }
//      }
//      catch (Exception e) {};

      return error;

   }

	/**
	 * Creates a window on the screen
	 *
	 * @param depth
	 * @param width
	 * @param type
	 * @param gui
	 * @param monoAttr
	 * @param colorAttr
	 * @param ul
	 * @param upper
	 * @param ur
	 * @param left
	 * @param right
	 * @param ll
	 * @param bottom
	 * @param lr
	 */
	protected void createWindow(int depth, int width, int type, boolean gui,
			int monoAttr, int colorAttr, int ul, int upper, int ur, int left,
			int right, int ll, int bottom, int lr) {

	   int lastPos = screen52.getLastPos();
	   int numCols = screen52.getColumns();

		int c = screen52.getCol(lastPos);
		int w = 0;
		width++;

		w = width;
		char initChar = Screen5250.initChar;
		int initAttr = Screen5250.initAttr;

		// set leading attribute byte
		screen52.setScreenCharAndAttr(initChar, initAttr, true);

		// set upper left
		if (gui) {
			screen52.setScreenCharAndAttr((char) ul, colorAttr, UPPER_LEFT, false);
		}
		else {
			screen52.setScreenCharAndAttr((char) ul, colorAttr, false);
		}

		// draw top row
		while (w-- >= 0) {
			if (gui) {
				screen52.setScreenCharAndAttr((char) upper, colorAttr, UPPER,false);
			}
			else {
				screen52.setScreenCharAndAttr((char) upper, colorAttr, false);
			}
		}

		// set upper right
		if (gui) {
			screen52.setScreenCharAndAttr((char) ur, colorAttr, UPPER_RIGHT, false);
		}
		else {
			screen52.setScreenCharAndAttr((char) ur, colorAttr, false);

		}

		// set ending attribute byte
		screen52.setScreenCharAndAttr(initChar, initAttr, true);

		lastPos = ((screen52.getRow(lastPos) + 1) * numCols) + c;
		screen52.goto_XY(lastPos);

		// now handle body of window
		while (depth-- > 0) {

			// set leading attribute byte
			screen52.setScreenCharAndAttr(initChar, initAttr, true);
			// set left
			if (gui) {
				screen52.setScreenCharAndAttr((char) left, colorAttr, GUI_LEFT, false);
			}
			else {
				screen52.setScreenCharAndAttr((char) left, colorAttr, false);

			}

			w = width - 2;
		   screen52.setScreenCharAndAttr(initChar, initAttr, NO_GUI, true);
			// fill it in
			while (w-- >= 0) {
//			   if (!planes.isUseGui(screen52.getLastPos()))
			   screen52.setScreenCharAndAttr(initChar, initAttr, NO_GUI, false);
			}
			screen52.setScreenCharAndAttr(initChar, initAttr, NO_GUI, true);

			// set right
			if (gui) {
				screen52.setScreenCharAndAttr((char) right, colorAttr, GUI_RIGHT, false);

			}
			else {
				screen52.setScreenCharAndAttr((char) right, colorAttr, false);

			}

			screen52.setScreenCharAndAttr(initChar, initAttr, true);

			lastPos = ((screen52.getRow(lastPos) + 1) * numCols) + c;
			screen52.goto_XY(lastPos);
		}

		// set leading attribute byte
		screen52.setScreenCharAndAttr(initChar, initAttr, true);
		if (gui) {
			screen52.setScreenCharAndAttr((char) ll, colorAttr, LOWER_LEFT, false);

		}
		else {
			screen52.setScreenCharAndAttr((char) ll, colorAttr, false);

		}
		w = width;

		// draw bottom row
		while (w-- >= 0) {
			if (gui) {
				screen52.setScreenCharAndAttr((char) bottom, colorAttr, BOTTOM, false);
			}
			else {
				screen52.setScreenCharAndAttr((char) bottom, colorAttr, false);

			}
		}

		// set lower right
		if (gui) {
			screen52.setScreenCharAndAttr((char) lr, colorAttr, LOWER_RIGHT, false);
		}
		else {
			screen52.setScreenCharAndAttr((char) lr, colorAttr, false);
		}

		// set ending attribute byte
		screen52.setScreenCharAndAttr(initChar, initAttr, true);

	}

/* *** NEVER USED LOCALLY ************************************************** */
//	private void clearWindowBody(ScreenPlanes planes, int startPos, int depth, int width) {
//
//	   int lastPos = startPos;
//		char initChar = Screen5250.initChar;
//		int initAttr = Screen5250.initAttr;
//
//		// now handle body of window
//		while (depth-- > 0) {
//
//			// set leading attribute byte
////				planes.setScreenCharAndAttr(lastPos,initChar, initAttr, true);
////				setDirty(lastPos);
////				advancePos();
////
////				// set left
////				planes.setScreenCharAndAttr(lastPos, (char) left, colorAttr, false);
////
////				if (gui) {
////					planes.setUseGUI(lastPos,GUI_LEFT);
////				}
////				setDirty(lastPos);
////				advancePos();
//
//			int w = width;
//			// fill it in
//			while (w-- >= 0) {
////				screen[lastPos].setCharAndAttr(initChar, initAttr, true);
//				planes.setScreenCharAndAttr(lastPos,initChar, initAttr, true);
////				screen[lastPos].setUseGUI(NO_GUI);
//				planes.setUseGUI(lastPos,NO_GUI);
////				setDirty(lastPos);
//				lastPos++;
//				advancePos();
//			}
//
////				// set right
////	//			screen[lastPos].setCharAndAttr((char) right, colorAttr, false);
////				planes.setScreenCharAndAttr(lastPos,(char) right, colorAttr, false);
////				if (gui) {
////	//				screen[lastPos].setUseGUI(RIGHT);
////					planes.setUseGUI(lastPos,GUI_RIGHT);
////				}
////
////				setDirty(lastPos);
////				advancePos();
////
////				// set ending attribute byte
////	//			screen[lastPos].setCharAndAttr(initChar, initAttr, true);
////				planes.setScreenCharAndAttr(lastPos,initChar, initAttr, true);
////				setDirty(lastPos);
//
//			lastPos = startPos;
//		}
//
//	}

/* *** NEVER USED LOCALLY ************************************************** */
//	private void setDirty(int pos) {
//
//	   screen52.setDirty(pos);
//
//	}

/* *** NEVER USED LOCALLY ************************************************** */
//	private void advancePos() {
//
//	   screen52.advancePos();
//	}

   private void defineSelectionField(int majLen) {

      //   0030:  20 00 2C 3E 00 00 00 69 12 A0 00 00 04 00 00 03  .,>...i........
      //   0040:  04 40 04 11 00 28 01 07 00 00 00 19 00 00 04 11 .@...(..........
      //   0050:  14 19 15 00 48 D9 50 00 60 00 11 01 84 84 00 00 ....H.P.`.......
      //   0060:  05 03 01 01 00 00 00 13 01 E0 00 21 00 21 00 3B ...........!.!.;
      //   0070:  22 20 20 20 20 3A 24 20 20 3A 0B 10 08 00 E0 00 "    :$  :......
      //   0080:  D6 95 85 40 40 0B 10 08 00 E0 00 E3 A6 96 40 40 ...@@.........@@
      //   0090:  0B 10 08 00 E0 00 E3 88 99 85 85 04 52 00 00 FF ............R...
      //   00A0:  EF                                              .
      try {
         int flag1 = segment[pos++];    // Flag byte 1 - byte 5
         int flag2 = segment[pos++];    // Flag byte 2 - byte 6
         int flag3 = segment[pos++];    // Flag byte 3 - byte 7
         int typeSelection = segment[pos++];    // Type of selection Field - byte 8

         // GUI Device Characteristics:
         //    This byte is used if the target device is a GUI PWS or a GUI-like
         //    NWS.  If neigher of these WS are the targets, this byte is ignored
         int guiDevice = segment[pos++];    // byte 9
         int withMnemonic = segment[pos++];    //  byte 10
         int noMnemonic = segment[pos++];    // byte 11

         pos++;    // Reserved - byte 12
         pos++;    // Reserved - byte 13

         int cols = segment[pos++];    // Text Size - byte 14
         int rows = segment[pos++];    // Rows - byte 15

         int maxColChoice = segment[pos++];    // byte 16 num of column choices
         int padding = segment[pos++];    // byte 17
         int numSepChar = segment[pos++];    // byte 18
         int ctySepChar = segment[pos++];    // byte 19
         int cancelAID = segment[pos++];    // byte 20

         int cnt = 0;
         int minLen = 0;
         majLen -= 21;
         log.debug(" row: " + screen52.getCurrentRow()
                              + " col: " + screen52.getCurrentCol()
                              + " type " + typeSelection
                              + " gui " + guiDevice
                              + " withMnemonic " + Integer.toHexString(withMnemonic & 0xf0)
                              + " noMnemonic " + Integer.toHexString(noMnemonic & 0xf0)
                              + " noMnemonic " + Integer.toBinaryString(noMnemonic)
                              + " noMnemonicType " + Integer.toBinaryString((noMnemonic & 0xf0))
                              + " noMnemonicSel " + Integer.toBinaryString((noMnemonic & 0x0f))
                              + " maxcols " + maxColChoice
                              + " cols " + cols
                              + " rows " + rows);
         int rowCtr = 0;
         int colCtr = 0;
         int chcRowStart = screen52.getCurrentRow();
         int chcColStart = screen52.getCurrentCol();
         int chcRow = chcRowStart;
         int chcCol = chcColStart;
         int chcPos = screen52.getPos(chcRow-1,chcCol);

// client access
//0000   00 04 ac 9e b9 35 00 01 02 32 bb 4e 08 00 45 00  .....5...2.N..E.
//0010   00 3c 4f 8e 40 00 80 06 00 00 c1 a8 33 58 c1 a8  .<O.@.......3X..
//0020   33 01 09 e4 00 17 5b bf b7 a4 c3 41 43 d1 50 18  3.....[....AC.P.
//0030   fc de e9 d8 00 00 00 12 12 a0 00 00 04 00 80 03  ................
//0040   16 18 f1 11 14 1a 00 22 ff ef                    ......."..

         int colAvail = 0x20;
         int colSelAvail = 0x20;
         int fld = 0;

         do {
            minLen = segment[pos++];    // Minor Length byte 21

            int minType = segment[pos++];    // Minor Type

            switch (minType) {

               case 0x01:  // Choice Presentation Display

                  // flag
                  int flagCP1 = segment[pos++];

                  pos++; // mon select cursor avail emphasis - byte4
                  colSelAvail = segment[pos++];  // -byte 5

                  pos++; // mon select cursor - byte 6
                  int colSelCur = segment[pos++];  // -byte 7

                  pos++; // mon select cursor not avail emphasis - byte 8
                  int colSelNotAvail = segment[pos++];  // -byte 9

                  pos++; // mon avail emphasis - byte 10
                  colAvail = segment[pos++];  // -byte 11

                  pos++; // mon select emphasis - byte 12
                  int colSel = segment[pos++];  // -byte 13

                  pos++; // mon not avail emphasis - byte 14
                  int colNotAvail = segment[pos++];  // -byte 15

                  pos++; // mon indicator emphasis - byte 16
                  int colInd = segment[pos++];  // -byte 17

                  pos++; // mon indicator not avail emphasis - byte 18
                  int colNotAvailInd = segment[pos++];  // -byte 19

                  break;

               case 0x10:  // Choice Text minor structure

                  cnt = 5;
                  int flagCT1 = segment[pos++];
                  int flagCT2 = segment[pos++];
                  int flagCT3 = segment[pos++];
                  int mnemOffset = 0;
                  boolean aid = false;
                  boolean selected = false;

                  // is in selected state
                  if ((flagCT1 & 0x40) == 0x40) {
                	  log.debug(" selected ");
                     selected = true;
                  }

                  //System.out.println(Integer.toBinaryString((flagCT1 & 0xf0)));
                  // is mnemonic offset specified
                  if ((flagCT1 & 0x08) == 8) {
                	 log.debug(" mnemOffset " + mnemOffset);
                     mnemOffset = segment[pos++];
                     cnt++;
                  }

                  // is aid key specified
                  if ((flagCT1 & 0x04) == 4) {

                     aid = true;
                     log.debug(" aidKey " + aid);
//                     cnt++;
                  }

                  // is single digit number specified
                  if ((flagCT1 & 0x01) == 0x01) {
                	 log.debug(" single digit " );
                     pos++;
                     cnt++;
                  }

                  // is double digint number specified
                  if ((flagCT1 & 0x02) == 0x02) {
                	 log.debug(" double digit " );

                     pos++;
                     cnt++;
                  }

                  String s = "";
                  byte byte0 = 0;
                  fld++;

                  screen52.setCursor(chcRowStart,chcColStart);

                  // we do not add a selection if it is marked as unavailable
                  if ((flagCT1 & 0x80) != 0x80) {
                     screen52.addField(0x26,1,0,0,0,0);
                     screen52.getScreenFields().getCurrentField().setFieldChar('.');
                     screen52.getScreenFields().getCurrentField().setSelectionFieldInfo(17,
                                 fld,
                                 chcPos);
                     screen52.setCursor(chcRowStart,chcColStart + 3);

                     for (;cnt < minLen; cnt++) {

                        byte0 = segment[pos++];
                        s += vt.codePage.ebcdic2uni(byte0);
                        screen52.setChar(vt.codePage.ebcdic2uni(byte0));

                     }

                     addChoiceField(chcRowStart,chcColStart,chcRow,chcCol,s);
                  }

//         screen52.getScreenFields().getCurrentField().setMDT();

                  log.debug(s + " selected " + selected);
//                  chcRowStart;
                  //maxColChoice
                  colCtr++;
//                  rowCtr++;
                  if (colCtr >= maxColChoice) {

                     rowCtr++;
                     colCtr=0;
                     chcColStart = chcCol;
                     chcRowStart = chcRow + rowCtr;
                     if (rowCtr > rows) {
                        chcRowStart = chcRow;
                        rowCtr = 0;
                        chcColStart = chcColStart + 3 + cols + padding;
                     }
                  }
                  else {
                     chcColStart = chcColStart + padding + cols + 3;
//
                  }

                  break;
               default:
                  for (cnt = 2;cnt < minLen; cnt++) {

                     pos++;
                  }

            }

            majLen -= minLen;

         }  while (majLen > 0);
      }
      catch (Exception exc) {
    	 log.warn(" defineSelectionField :", exc);
         exc.printStackTrace();
      }
   }

   // negotiating commands
//   private static final byte IAC = (byte)-1; // 255  FF
//   private static final byte DONT = (byte)-2; //254  FE
//   private static final byte DO = (byte)-3; //253    FD
//   private static final byte WONT = (byte)-4; //252  FC
//   private static final byte WILL = (byte)-5; //251  FB
//   private static final byte SB = (byte)-6; //250 Sub Begin  FA
//   private static final byte SE = (byte)-16; //240 Sub End   F0
//   private static final byte EOR = (byte)-17; //239 End of Record  EF
//   private static final byte TERMINAL_TYPE = (byte)24;     // 18
//   private static final byte OPT_END_OF_RECORD = (byte)25;  // 19
//   private static final byte TRANSMIT_BINARY = (byte)0;     // 0
//   private static final byte QUAL_IS = (byte)0;             // 0
//   private static final byte TIMING_MARK = (byte)6;         // 6
//   private static final byte NEW_ENVIRONMENT = (byte)39;         // 27
//   private static final byte IS = (byte)0;         // 0
//   private static final byte SEND = (byte)1;         // 1
//   private static final byte INFO = (byte)2;         // 2
//   private static final byte VAR = (byte)0;         // 0
//   private static final byte VALUE = (byte)1;         // 1
//   private static final byte NEGOTIATE_ESC = (byte)2;         // 2
//   private static final byte USERVAR = (byte)3;         // 3

   // miscellaneous
//   private static final byte ESC = 0x04; // 04
//   private static final char char0 = 0;

//   private static final byte CMD_READ_IMMEDIATE_ALT = (byte)0x83; // 131


}