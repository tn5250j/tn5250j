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
import java.awt.font.*;
import java.awt.Font;
import org.tn5250j.tools.GUIGraphicsUtils;

public class ScreenChar {


   public ScreenChar(Screen5250 s5250) {
      s = s5250;
      sChar= new char[1];
      cArea = new Rectangle2D.Float(0,0,0,0);
      useGui = false;
      whichGui = NO_GUI;
   }

   public final char getChar() {
        return sChar[0];
   }

   public final int getCharAttr() {
        return attr;
   }

   public final boolean isAttributePlace() {
//      return (attributePlace || (sChar[0] == ' ' && !underLine && !colSep && bg.equals(s.colorBg)));
      return attributePlace;
   }

   public final void setChar(char c) {
      isChanged = sChar[0] == c ? false : true;
//      if (isChanged)
//         System.out.println(sChar[0] + " - " + c);
      sChar[0] = c;
      if (attributePlace)
         setCharAndAttr(c,32,false);

   }

   public final void setUseGUI(int which) {

      isChanged = whichGui == which ? false : true;

      whichGui = which;
      if (which == NO_GUI) {
        useGui = false;
      }
      else {
        useGui = true;
      }
   }

   public final int getWhichGUI() {

      return whichGui;
   }


   public final void setCharAndAttr(char c, int a, boolean ap) {

      isChanged = sChar[0] == c ? false : true;
//      if (isChanged)
//         System.out.println(sChar[0] + " - " + c);

      sChar[0] = c;
//         useGui = false;
//         whichGui = NO_GUI;

      if(attr != a)
          setAttribute(a);

      if(ap) {
         attributePlace = true;
         useGui = false;
         whichGui = NO_GUI;

      }
      else
         attributePlace = false;
   }

   public final void setRowCol(int row, int col) {

      cArea.setRect((s.fmWidth*col),s.fmHeight * row,s.fmWidth,s.fmHeight);
      x = s.fmWidth * col;
      y = s.fmHeight * row;
      cy = (int)(y + s.fmHeight - (s.lm.getDescent() + s.lm.getLeading()));
   }

   public final void setAttribute(int i) {

      colSep = false;
      underLine = false;
      nonDisplay = false;

      isChanged = attr == i ? false : true;

      attr = i;

      if(i == 0)
         return;
      switch(i) {
         case 32: // green normal
            fg = s.colorGreen;
            bg = s.colorBg;
            break;

         case 33: // green/revers
            fg = s.colorBg;
            bg = s.colorGreen;
            break;

         case 34: // white normal
            fg = s.colorWhite;
            bg = s.colorBg;
            break;

         case 35: // white/reverse
            fg = s.colorBg;
            bg = s.colorWhite;
            break;

         case 36: // green/underline
            fg = s.colorGreen;
            bg = s.colorBg;
            underLine = true;
            break;

         case 37: // green/reverse/underline
            fg = s.colorBg;
            bg = s.colorGreen;
            underLine = true;
            break;

         case 38: // white/underline
            fg = s.colorWhite;
            bg = s.colorBg;
            underLine = true;
            break;

         case 39:
            nonDisplay = true;
            break;

         case 40:
         case 42: // red/normal
            fg = s.colorRed;
            bg = s.colorBg;
            break;

         case 41:
         case 43: // red/reverse
            fg = s.colorBg;
            bg = s.colorRed;
            break;

         case 44:
         case 46: // red/underline
            fg = s.colorRed;
            bg = s.colorBg;
            underLine = true;
            break;

         case 45: // red/reverse/underline
            fg = s.colorBg;
            bg = s.colorRed;
            underLine = true;
            break;

         case 47:
            nonDisplay = true;
            break;

         case 48:
            fg = s.colorTurq;
            bg = s.colorBg;
            colSep = true;
            break;

         case 49:
            fg = s.colorBg;
            bg = s.colorTurq;
            colSep = true;
            break;

         case 50:
            fg = s.colorYellow;
            bg = s.colorBg;
            colSep = true;
            break;

         case 51:
            fg = s.colorBg;
            bg = s.colorYellow;
            colSep = true;
            break;

         case 52:
            fg = s.colorTurq;
            bg = s.colorBg;
            colSep = true;
            underLine = true;
            break;

         case 53:
            fg = s.colorBg;
            bg = s.colorTurq;
            colSep = true;
            underLine = true;
            break;

         case 54:
            fg = s.colorYellow;
            bg = s.colorBg;
            colSep = true;
            underLine = true;
            break;

         case 55:
            nonDisplay = true;
            break;

         case 56: // pink
            fg = s.colorPink;
            bg = s.colorBg;
            break;

         case 57: // pink/reverse
            fg = s.colorBg;
            bg = s.colorPink;
            break;

         case 58: // blue/reverse
            fg = s.colorBlue;
            bg = s.colorBg;
            break;

         case 59: // blue
            fg = s.colorBg;
            bg = s.colorBlue;
            break;

         case 60: // pink/underline
            fg = s.colorPink;
            bg = s.colorBg;
            underLine = true;
            break;

         case 61: // pink/reverse/underline
            fg = s.colorBg;
            bg = s.colorPink;
            underLine = true;
            break;

         case 62: // blue/underline
            fg = s.colorBlue;
            bg = s.colorBg;
            underLine = true;
            break;

         case 63:
            nonDisplay = true;
            break;
         default:
            fg = s.colorYellow;
            break;

      }
   }

   public final void drawChar(Graphics2D g) {

      if (attributePlace && s.getShowHex()) {
         Font f = g.getFont();

         Font k = f.deriveFont(f.getSize2D()/2);
         g.setFont(k);
         g.setColor(s.colorHexAttr);
         char[] a = Integer.toHexString(attr).toCharArray();
         g.drawChars(a, 0, 1, x, y + (int)(s.fmHeight /2));
         g.drawChars(a, 1, 1, x+(int)(s.fmWidth/2),
            (int)(y + s.fmHeight - (s.lm.getDescent() + s.lm.getLeading())-2));
         g.setFont(f);
      }

      if(!nonDisplay && !attributePlace) {

         if (!useGui) {
            g.setColor(bg);
            g.fill(cArea);
         }
         else {

            if (bg == s.colorBg && whichGui >= FIELD_LEFT && whichGui <= FIELD_ONE)
               g.setColor(s.colorGUIField);
            else
               g.setColor(bg);

            g.fill(cArea);

         }

         if (useGui && (whichGui < FIELD_LEFT)) {
            int w = 0;

            g.setColor(fg);

            switch (whichGui) {

               case UPPER_LEFT:
                  if (sChar[0] == '.') {
                     if (s.isUsingGuiInterface()) {
                        GUIGraphicsUtils.drawWinUpperLeft(g,
                                             GUIGraphicsUtils.WINDOW_GRAPHIC,
                                             s.colorBlue,
                                             x,y,s.fmWidth,s.fmHeight);

                     }
                     else {

                        GUIGraphicsUtils.drawWinUpperLeft(g,
                                             GUIGraphicsUtils.WINDOW_NORMAL,
                                             fg,
                                             x,y,s.fmWidth,s.fmHeight);

                     }
                  }
               break;
               case UPPER:
                  if (sChar[0] == '.') {

                     if (s.isUsingGuiInterface()) {
                        GUIGraphicsUtils.drawWinUpper(g,
                                             GUIGraphicsUtils.WINDOW_GRAPHIC,
                                             s.colorBlue,
                                             x,y,s.fmWidth,s.fmHeight);


                     }
                     else {

                        GUIGraphicsUtils.drawWinUpper(g,
                                             GUIGraphicsUtils.WINDOW_NORMAL,
                                             fg,
                                             x,y,s.fmWidth,s.fmHeight);
                     }
                  }
               break;
               case UPPER_RIGHT:
                  if (sChar[0] == '.') {
                     if (s.isUsingGuiInterface()) {

                        GUIGraphicsUtils.drawWinUpperRight(g,
                                             GUIGraphicsUtils.WINDOW_GRAPHIC,
                                             s.colorBlue,
                                             x,y,s.fmWidth,s.fmHeight);


                     }
                     else {

                        GUIGraphicsUtils.drawWinUpperRight(g,
                                             GUIGraphicsUtils.WINDOW_NORMAL,
                                             fg,
                                             x,y,s.fmWidth,s.fmHeight);

                     }
                  }
               break;
               case LEFT:
                  if (sChar[0] == ':') {
                     if (s.isUsingGuiInterface()) {
                        GUIGraphicsUtils.drawWinLeft(g,
                                             GUIGraphicsUtils.WINDOW_GRAPHIC,
                                             bg,
                                             x,y,s.fmWidth,s.fmHeight);


                     }
                     else {

                        GUIGraphicsUtils.drawWinLeft(g,
                                             GUIGraphicsUtils.WINDOW_NORMAL,
                                             fg,
                                             x,y,s.fmWidth,s.fmHeight);

                        g.drawLine(x + s.fmWidth / 2,
                                    y,
                                    x + s.fmWidth / 2,
                                    y + s.fmHeight);
                     }
                  }
               break;
               case RIGHT:
                  if (sChar[0] == ':') {
                     if (s.isUsingGuiInterface()) {
                        GUIGraphicsUtils.drawWinRight(g,
                                             GUIGraphicsUtils.WINDOW_GRAPHIC,
                                             bg,
                                             x,y,s.fmWidth,s.fmHeight);


                     }
                     else {
                        GUIGraphicsUtils.drawWinRight(g,
                                             GUIGraphicsUtils.WINDOW_NORMAL,
                                             fg,
                                             x,y,s.fmWidth,s.fmHeight);

                     }
                  }
               break;
               case LOWER_LEFT:
                  if (sChar[0] == ':') {

                     if (s.isUsingGuiInterface()) {

                        GUIGraphicsUtils.drawWinLowerLeft(g,
                                             GUIGraphicsUtils.WINDOW_GRAPHIC,
                                             bg,
                                             x,y,s.fmWidth,s.fmHeight);


                     }
                     else {

                        GUIGraphicsUtils.drawWinLowerLeft(g,
                                             GUIGraphicsUtils.WINDOW_NORMAL,
                                             fg,
                                             x,y,s.fmWidth,s.fmHeight);
                     }
                  }
               break;
               case BOTTOM:
                  if (sChar[0] == '.') {

                     if (s.isUsingGuiInterface()) {


                        GUIGraphicsUtils.drawWinBottom(g,
                                             GUIGraphicsUtils.WINDOW_GRAPHIC,
                                             bg,
                                             x,y,s.fmWidth,s.fmHeight);


                     }
                     else {

                        GUIGraphicsUtils.drawWinBottom(g,
                                             GUIGraphicsUtils.WINDOW_NORMAL,
                                             fg,
                                             x,y,s.fmWidth,s.fmHeight);
                     }
                  }
               break;

               case LOWER_RIGHT:
                  if (sChar[0] == ':') {
                     if (s.isUsingGuiInterface()) {

                        GUIGraphicsUtils.drawWinLowerRight(g,
                                             GUIGraphicsUtils.WINDOW_GRAPHIC,
                                             bg,
                                             x,y,s.fmWidth,s.fmHeight);

                     }
                     else {

                        GUIGraphicsUtils.drawWinLowerRight(g,
                                             GUIGraphicsUtils.WINDOW_NORMAL,
                                             fg,
                                             x,y,s.fmWidth,s.fmHeight);

                     }
                  }
               break;

            }
         }

         else {
            if (sChar[0] != 0x0) {
            // use this until we define colors for gui stuff
               if ((useGui && whichGui < BUTTON_LEFT) && (fg == s.colorGUIField))

                  g.setColor(Color.black);
               else
                  g.setColor(fg);

                  try {
                  if (useGui)

                     g.drawChars(sChar, 0, 1, x+1, cy -2);
                  else
                     g.drawChars(sChar, 0, 1, x, cy -2);
                  }
                  catch (IllegalArgumentException iae) {
                     System.out.println(" ScreenChar iae " + iae.getMessage());

                  }
            }
            if(underLine ) {

               if (!useGui || s.guiShowUnderline) {
                  g.setColor(fg);
//                  g.drawLine(x, cy -2, (int)(x + s.fmWidth), cy -2);
                  g.drawLine(x, (int)(y + (s.fmHeight - s.lm.getLeading()-5)), (int)(x + s.fmWidth), (int)(y + (s.fmHeight - s.lm.getLeading())-5));

               }
            }

            if(colSep) {
               if(s.getColSepLine()) {
                  g.setColor(fg);
                  g.drawLine(x, y, x, y + s.fmHeight - 1);
                  g.drawLine(x + s.fmWidth - 1, y, x + s.fmWidth - 1, y + s.fmHeight);
               }
               else {
                  g.setColor(s.colorSep);
                  g.drawLine(x,  y + s.fmHeight - (int)s.lm.getLeading()-4, x, y + s.fmHeight);
                  g.drawLine(x + s.fmWidth - 1, y + s.fmHeight - (int)s.lm.getLeading()-4, x + s.fmWidth - 1, y + s.fmHeight);
               }
            }
         }
      }

      if (useGui & (whichGui >= FIELD_LEFT)) {
            int w = 0;

            switch (whichGui) {

               case FIELD_LEFT:
                  GUIGraphicsUtils.draw3DLeft(g, GUIGraphicsUtils.INSET, x,y,
                                             s.fmWidth,s.fmHeight);

               break;
               case FIELD_MIDDLE:
                  GUIGraphicsUtils.draw3DMiddle(g, GUIGraphicsUtils.INSET, x,y,
                                             s.fmWidth,s.fmHeight);
               break;
               case FIELD_RIGHT:
                  GUIGraphicsUtils.draw3DRight(g, GUIGraphicsUtils.INSET, x,y,
                                             s.fmWidth,s.fmHeight);
               break;

               case FIELD_ONE:
                  GUIGraphicsUtils.draw3DOne(g, GUIGraphicsUtils.INSET, x,y,
                                             s.fmWidth,s.fmHeight);

               break;

               case BUTTON_LEFT:
               case BUTTON_LEFT_UP:
               case BUTTON_LEFT_DN:
               case BUTTON_LEFT_EB:

                  GUIGraphicsUtils.draw3DLeft(g, GUIGraphicsUtils.RAISED, x,y,
                                             s.fmWidth,s.fmHeight);

                  break;

               case BUTTON_MIDDLE:
               case BUTTON_MIDDLE_UP:
               case BUTTON_MIDDLE_DN:
               case BUTTON_MIDDLE_EB:

                  GUIGraphicsUtils.draw3DMiddle(g, GUIGraphicsUtils.RAISED, x,y,
                                             s.fmWidth,s.fmHeight);
                  break;

               case BUTTON_RIGHT:
               case BUTTON_RIGHT_UP:
               case BUTTON_RIGHT_DN:
               case BUTTON_RIGHT_EB:

                  GUIGraphicsUtils.draw3DRight(g, GUIGraphicsUtils.RAISED, x,y,
                                             s.fmWidth,s.fmHeight);

               break;

               // scroll bar
               case BUTTON_SB_UP:
                  GUIGraphicsUtils.drawScrollBar(g, GUIGraphicsUtils.RAISED, 1, x,y,
                                             s.fmWidth,s.fmHeight,
                                             s.colorWhite,s.colorBg);
                  break;

               // scroll bar
               case BUTTON_SB_DN:

                  GUIGraphicsUtils.drawScrollBar(g, GUIGraphicsUtils.RAISED, 2, x,y,
                                             s.fmWidth,s.fmHeight,
                                             s.colorWhite,s.colorBg);


                  break;
               // scroll bar
               case BUTTON_SB_GUIDE:

                  GUIGraphicsUtils.drawScrollBar(g, GUIGraphicsUtils.INSET, 0,x,y,
                                             s.fmWidth,s.fmHeight,
                                             s.colorWhite,s.colorBg);


                  break;

               // scroll bar
               case BUTTON_SB_THUMB:

                  GUIGraphicsUtils.drawScrollBar(g, GUIGraphicsUtils.INSET, 3,x,y,
                                             s.fmWidth,s.fmHeight,
                                             s.colorWhite,s.colorBg);


                  break;

            }
         }

   }

   public final boolean isChanged() {
      return isChanged;
   }

   public final String toString() {

      return "x >" + x + "< y >" + y + "< char >" + sChar[0]
               + "< char hex >" + Integer.toHexString(sChar[0]) + "< attr >" + attr
               + "< attribute >" + isAttributePlace() + "< isNonDisplayable >"
               + nonDisplay + "< underline >" + underLine + "< colSep >" + colSep
               + "< backGround >" + bg + "< foreGround >" + fg ;

   }

   public int x;
   public int y;
   public int cy;
   public char sChar[];
   public int attr;
   public Color fg;
   public Color bg;
   public boolean underLine;
   public boolean nonDisplay;
   public boolean colSep;
   public boolean attributePlace;
   public boolean useGui;
   public boolean showGuiUnderline = true;
   public int whichGui;
   public final Screen5250 s;
   private boolean isChanged = true;
   private Rectangle2D cArea; // character area
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