package org.tn5250j.tools;
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
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import org.tn5250j.*;

public class GUIHotSpots {

   public static boolean checkHotSpots(Screen5250 s,
                                          ScreenChar[] screen,
                                          int numRows,
                                          int numCols,
                                          int lenScreen,
                                          int fmWidth,
                                          int fmHeight) {

      boolean hs = false;
      boolean retHS = false;
      StringBuffer hsMore = s.getHSMore();
      StringBuffer hsBottom = s.getHSBottom();
//      Rectangle2D mArea = new Rectangle2D.Float(0,0,0,0);
//      Rectangle2D mwArea = new Rectangle2D.Float(0,0,0,0);
//      ArrayList mArray = new ArrayList(10);

         for (int x = 0; x < lenScreen; x++) {

            hs =false;
            if (s.isInField(x,false))
               continue;

            // First check for PF keys
            if (x > 0 && screen[x].getChar() == 'F') {
               if (screen[x + 1].getChar() >= '0' &&
                        screen[x + 1].getChar() <= '9' &&
                         screen[x - 1].getChar() <= ' ' &&
                         !screen[x].nonDisplay) {

                  if (screen[x + 2].getChar() >= '0' &&
                        screen[x + 2].getChar() <= '9' &&
                         (screen[x + 3].getChar() == '=' ||
                          screen[x + 3].getChar() == '-' ||
                          screen[x + 3].getChar() == '/') )
                     hs = true;
                  else
                     if (   screen[x + 2].getChar() == '=' ||
                            screen[x + 3].getChar() == '-' ||
                            screen[x + 3].getChar() == '/')
                        hs = true;

                  if (hs) {
                     screen[x].setUseGUI(ScreenChar.BUTTON_LEFT);

                     int ns = 0;
                     int row = x / numCols;
                     while (ns < 2 && ++x / numCols == row) {
                        if (screen[x].getChar() <= ' ')
                           ns++;
                        else
                           ns = 0;
                        if (ns <2)
                           screen[x].setUseGUI(ScreenChar.BUTTON_MIDDLE);

                     }

                     // now lets go back and take out gui's that do not belong
                     while (screen[--x].getChar() <= ' ') {
                        screen[x].setUseGUI(ScreenChar.NO_GUI);
                     }
                     screen[x].setUseGUI(ScreenChar.BUTTON_RIGHT);

                  }
               }
            }

            // now lets check for menus
            if (!hs && x > 0 && x < lenScreen - 2 &&
                  screen[x].getChar() == '.' &&
                  screen[x].getWhichGUI() == ScreenChar.NO_GUI &&
                  !screen[x].underLine &&
                  !screen[x].nonDisplay
               ) {

               int os = 0;
               if ((os = isOption(screen,x,lenScreen,2,3,'.') )> 0) {
                  hs = true;

                  int stop = x;
                  int ns = 0;
                  int row = stop / numCols;

                  while (++stop / numCols == row &&
                               (screen[stop].getChar() >= ' ' ||
                                screen[stop].getChar() == 0x0) ) {

                     if (screen[stop].getChar() <= ' ') {
                        ns++;
                     }
                     else
                        ns = 0;

                     if (screen[stop].getChar() == '.') {
                        int io = 0;
                        if ((io = isOption(screen,stop,lenScreen,2,3,'.')) > 0) {

                           stop = io;
                           break;
                        }
                     }

                     if (ns > 3)
                        break;
                  }

                  screen[++os].setUseGUI(ScreenChar.BUTTON_LEFT);
                  s.setDirty(os);

                  while (++os < stop) {
                     screen[os].setUseGUI(ScreenChar.BUTTON_MIDDLE);
                     s.setDirty(os);
                  }

                  // now lets go back and take out gui's that do not belong
                  while (screen[--stop].getChar() <= ' ') {
                     screen[stop].setUseGUI(ScreenChar.NO_GUI);
                     s.setDirty(stop);
                  }
                  screen[stop].setUseGUI(ScreenChar.BUTTON_RIGHT);
                  s.setDirty(stop);

               }
            }

            // now lets check for options.
            if (!hs && x > 0 && x < lenScreen - 2 &&
                  screen[x].getChar() == '=' &&
                  screen[x].getWhichGUI() == ScreenChar.NO_GUI &&
                  !screen[x].underLine &&
                  !screen[x].nonDisplay
               ) {

               int os = 0;
               if ((os = isOption(screen,x,lenScreen,2,2,'=') )> 0) {
                  hs = true;

                  int stop = x;
                  int ns = 0;
                  int row = stop / numCols;

                  while (++stop / numCols == row &&
                               screen[stop].getChar() >= ' ') {

                     if (screen[stop].getChar() == ' ') {
                        ns++;
                     }
                     else
                        ns = 0;

                     if (screen[stop].getChar() == '=') {
                        int io = 0;
                        if ((io = isOption(screen,stop,lenScreen,2,2,'=')) > 0) {

                           stop = io;
                           break;
                        }
                     }

                     if (ns > 2)
                        break;
                  }

                  screen[++os].setUseGUI(ScreenChar.BUTTON_LEFT);
                  s.setDirty(os);

                  while (++os < stop) {
                     screen[os].setUseGUI(ScreenChar.BUTTON_MIDDLE);
                     s.setDirty(os);

                  }

                  // now lets go back and take out gui's that do not belong
                  while (screen[--stop].getChar() <= ' ') {
                     screen[stop].setUseGUI(ScreenChar.NO_GUI);
                     s.setDirty(stop);

                  }
                  screen[stop].setUseGUI(ScreenChar.BUTTON_RIGHT);
                  s.setDirty(stop);
               }
            }

            // now lets check for More... .

            if (!hs && x > 2 && x < lenScreen - hsMore.length() &&
                  screen[x].getChar() == hsMore.charAt(0) &&
                  screen[x - 1].getChar() <= ' ' &&
                  screen[x - 2].getChar() <= ' ' &&
                  screen[x].getWhichGUI() == ScreenChar.NO_GUI &&
                  !screen[x].nonDisplay
               ) {

               boolean mFlag = true;
               int ms = hsMore.length();
               int mc = 0;
               while (++mc < ms) {
                  if (screen[x+mc].getChar() != hsMore.charAt(mc)) {
                     mFlag = false;
                     break;
                  }

               }

               if (mFlag) {
                  hs = true;

                  screen[x].setUseGUI(ScreenChar.BUTTON_LEFT_DN);

                  while (--ms > 0) {
                     screen[++x].setUseGUI(ScreenChar.BUTTON_MIDDLE_DN);

                  }
                  screen[x].setUseGUI(ScreenChar.BUTTON_RIGHT_DN);
               }
            }

            // now lets check for Bottom .
            if (!hs && x > 2 && x < lenScreen - hsBottom.length() &&
                  screen[x].getChar() == hsBottom.charAt(0) &&
                  screen[x - 1].getChar() <= ' ' &&
                  screen[x - 2].getChar() <= ' ' &&
                  screen[x].getWhichGUI() == ScreenChar.NO_GUI &&
                  !screen[x].nonDisplay
               ) {

               boolean mFlag = true;
               int bs = hsBottom.length();
               int bc = 0;
               while (++bc < bs) {
                  if (screen[x+bc].getChar() != hsBottom.charAt(bc)) {
                     mFlag = false;
                     break;
                  }

               }

               if (mFlag) {
                  hs = true;

                  screen[x].setUseGUI(ScreenChar.BUTTON_LEFT_UP);

                  while (--bs > 0) {
                     screen[++x].setUseGUI(ScreenChar.BUTTON_MIDDLE_UP);

                  }
                  screen[x].setUseGUI(ScreenChar.BUTTON_RIGHT_UP);
               }
            }

            // now lets check for HTTP:// .
            if (!hs && x > 0 && x < lenScreen - 7 &&
                  Character.toLowerCase(screen[x].getChar()) == 'h' &&
                  screen[x - 1].getChar() <= ' ' &&
                  screen[x].getWhichGUI() == ScreenChar.NO_GUI &&
                  !screen[x].nonDisplay
               ) {

               if (Character.toLowerCase(screen[x+1].getChar()) == 't' &&
                     Character.toLowerCase(screen[x+2].getChar()) == 't' &&
                     Character.toLowerCase(screen[x+3].getChar()) == 'p' &&
                     screen[x+4].getChar() == ':' &&
                     screen[x+5].getChar() == '/' &&
                     screen[x+6].getChar() == '/' ) {

                  hs = true;

                  screen[x].setUseGUI(ScreenChar.BUTTON_LEFT_EB);

                  while (screen[++x].getChar() > ' ') {
                     screen[x].setUseGUI(ScreenChar.BUTTON_MIDDLE_EB);

                  }
                  screen[--x].setUseGUI(ScreenChar.BUTTON_RIGHT_EB);
               }
            }
            if (!retHS && hs)
               retHS = true;

         }
//         int pos = 0;
//
//         mArea.setRect(0,0,0,0);
//         mwArea.setRect(0,0,0,0);
//         for (int k = 0; k < numCols;k++) {
//   //         System.out.println(k);
//            pos =k;
//            boolean gui = false;
//            for (int j=0; j < 19; j++) {
//               if (screen[pos].whichGui != ScreenChar.NO_GUI)
//   //                  System.out.print(screen[pos].getChar());
//
//                     mwArea.setRect(screen[pos].x,
//                                       screen[pos].y,
//                                       screen[pos].x,
//                                       screen[pos].y);
//
//                     if (mArea.getWidth() == 0) {
//                        mArea.setRect(mwArea);
//                     }
//                     else {
//                        double x1 = Math.min(mArea.getX(), mwArea.getX());
//                        double x2 = Math.max(mArea.getWidth(), mwArea.getWidth());
//                        double y1 = Math.min(mArea.getY(), mwArea.getY());
//                        double y2 = Math.max(mArea.getHeight(), mwArea.getHeight());
//                        mArea.setRect(x1, y1, x2, y2);
//
//                     }
//               pos += numCols;
//            }
//         }
//
//         if (mwArea.getWidth() != 0) {
//            System.out.println("Mennu area is " +
//                                 s.getRow(s.getRowColFromPoint((int)mArea.getX(),(int)mArea.getY())) + "," +
//                                 s.getCol(s.getRowColFromPoint((int)mArea.getX(),(int)mArea.getY())) + "," +
//                                 s.getRow(s.getRowColFromPoint(
//                                          (int)mArea.getWidth(),
//                                          (int)mArea.getHeight())) + "," +
//                                 s.getCol(s.getRowColFromPoint(
//                                          (int)mArea.getWidth(),
//                                          (int)mArea.getHeight()) ));
//         }


      return retHS;
   }

   private static int isOption(ScreenChar[] screen,
                                 int x,
                                 int lenScreen,
                                 int numPref,
                                 int numSuff,
                                 char suff) {
      boolean hs =true;
      int sp = x;
      int os = 0;
      // check to the left for option
      while (--sp >=0 &&  screen[sp].getChar() <= ' ' ) {

         if (x - sp > numPref || screen[sp].getChar() == suff||
                  screen[sp].getChar() == '.' ||
                  screen[sp].getChar() == '*') {
            hs =false;
//            System.out.println(" hs1 false " + screen[sp].getChar() + " " + sp);
            break;
         }
      }

      // now lets check for how long the option is it has to be numPref or less
      os = sp;
      while (hs && --os > 0 && screen[os].getChar() > ' ' ) {
//         System.out.println(" hs2 length " + (sp-os) + " " + screen[os].getChar());

         if (sp - os >= numPref || screen[os].getChar() == suff ||
                  screen[os].getChar() == '.' ||
                  screen[os].getChar() == '*') {
            hs = false;
//            System.out.println(" hs2 false at " + (sp-os) + " " + screen[os].getChar());
            break;
         }
      }
      if (sp - os > 1 && !Character.isDigit(screen[os+1].getChar())) {
         hs = false;
      }

      sp = x;

      if (Character.isDigit(screen[sp+1].getChar()))
         hs = false;
      // now lets make sure there are no more than numSuff spaces after option
      while (hs && (++sp < lenScreen && screen[sp].getChar() <= ' '
                     || screen[sp].getChar() == suff )) {
         if (sp - x >= numSuff || screen[sp].getChar() == suff ||
                     screen[sp].getChar() == '.' ||
                  screen[sp].getChar() == '*') {
            hs =false;
//            System.out.println(" hs3 false at " + sp + " " + screen[sp].getChar());
            break;
         }
      }
      if (hs && !Character.isLetterOrDigit(screen[sp].getChar()))
         hs = false;
      if (hs) {
         return os;
      }
      return -1;
   }

}