/**
 * Title: GuiGraphicBuffer.java
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

import java.awt.image.*;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.font.*;

import org.tn5250j.event.ScreenOIAListener;
import org.tn5250j.event.ScreenListener;
import org.tn5250j.tools.logging.*;
import org.tn5250j.tools.GUIGraphicsUtils;

public class GuiGraphicBuffer implements ScreenOIAListener, ScreenListener {

   private BufferedImage bi;
   private final Object lock = new Object();
   private Line2D separatorLine = new Line2D.Float();
   private Rectangle2D tArea; // text area
   private Rectangle2D aArea; // all screen area
   private Rectangle2D cArea; // command line area
   private Rectangle2D sArea; // status area
   private Rectangle2D pArea; // position area (cursor etc..)
   private Rectangle2D mArea; // message area
   private Rectangle2D iArea; // insert indicator
   private Rectangle2D kbArea; // keybuffer indicator
   private Rectangle2D scriptArea; // script indicator
   private int width;
   private int height;
   private Rectangle2D cursor = new Rectangle2D.Float();
   public final static byte STATUS_SYSTEM       = 1;
   public final static byte STATUS_ERROR_CODE   = 2;
   public final static byte STATUS_VALUE_ON     = 1;
   public final static byte STATUS_VALUE_OFF    = 2;
   private final static String xSystem = "X - System";
   private final static String xError = "X - II";
   private int crossRow;
   private int crossCol;
   private Rectangle crossRect = new Rectangle();
   protected int offTop = 0;   // offset from top
   protected int offLeft = 0;  // offset from left
   private boolean resized = false;
   private boolean antialiased;
   private Graphics2D gg2d;
   private Screen5250 screen;

   private TN5250jLogger log = TN5250jLogFactory.getLogger ("GFX");

   public GuiGraphicBuffer (Screen5250 screen) {

      this.screen = screen;
      screen.getOIA().addOIAListener(this);
      screen.addScreenListener(this);
      tArea = new Rectangle2D.Float();
      cArea = new Rectangle2D.Float();
      aArea = new Rectangle2D.Float();
      sArea = new Rectangle2D.Float();
      pArea = new Rectangle2D.Float();
      mArea = new Rectangle2D.Float();
      iArea = new Rectangle2D.Float();
      kbArea = new Rectangle2D.Float();
      scriptArea = new Rectangle2D.Float();

   }

   public void resize(int width, int height) {

      if (bi.getWidth() != width || bi.getHeight()  != height) {
         synchronized (lock) {
            bi = null;
            bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
            this.width = width;
            this.height = height;
            resized = true;
            // tell waiting threads to wake up
            lock.notifyAll();
         }
      }

   }

   public BufferedImage getImageBuffer(int width, int height) {


      synchronized (lock) {
         if (bi == null || bi.getWidth() != width || bi.getHeight() != height) {
            // allocate a buffer Image with appropriate size
            bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
            this.width = width;
            this.height = height;
            resized = true;
         }
         // tell waiting threads to wake up
         lock.notifyAll();
      }
      return bi;
   }

   /**
    * Draw the operator information area
    */
      public Graphics2D drawOIA () {

      	int numRows = screen.getRows();
      	int numCols = screen.getCols();

         Graphics2D g2d;

         // get ourselves a global pointer to the graphics
         g2d = getDrawingArea();

         if (antialiased)
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                  RenderingHints.VALUE_ANTIALIAS_ON);
         g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
               RenderingHints.VALUE_COLOR_RENDER_SPEED);
         g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
               RenderingHints.VALUE_RENDER_SPEED);
         g2d.setFont(screen.font);


         g2d.setColor(screen.colorBg);
         g2d.fillRect(0,0,bi.getWidth(null),bi.getHeight(null));
         tArea.setRect(0,0,bi.getWidth(null),(screen.fmHeight * (numRows)));
         cArea.setRect(0,screen.fmHeight * (numRows + 1),bi.getWidth(null),screen.fmHeight * (numRows + 1));
         aArea.setRect(0,0,bi.getWidth(null),bi.getHeight(null));
         sArea.setRect(screen.fmWidth * 9,screen.fmHeight * (numRows + 1),screen.fmWidth * 20,screen.fmHeight);
         pArea.setRect(bi.getWidth(null) - screen.fmWidth * 6,screen.fmHeight * (numRows + 1),screen.fmWidth * 6,screen.fmHeight);
         mArea.setRect((float)(sArea.getX()+ sArea.getWidth()) + screen.fmWidth + screen.fmWidth,
               screen.fmHeight * (numRows + 1),
               screen.fmWidth + screen.fmWidth,
               screen.fmHeight);
         kbArea.setRect((float)(sArea.getX()+ sArea.getWidth()) + (20 * screen.fmWidth),
               screen.fmHeight * (numRows + 1),
               screen.fmWidth + screen.fmWidth,
               screen.fmHeight);
         scriptArea.setRect((float)(sArea.getX()+ sArea.getWidth()) + (16 * screen.fmWidth),
               screen.fmHeight * (numRows + 1),
               screen.fmWidth + screen.fmWidth,
               screen.fmHeight);
//         cArea = new Rectangle2D.Float(0,fmHeight * (numRows + 1),bi.getWidth(null),fmHeight * (numRows + 1));
//         aArea = new Rectangle2D.Float(0,0,bi.getWidth(null),bi.getHeight(null));
//         sArea = new Rectangle2D.Float(fmWidth * 9,fmHeight * (numRows + 1),fmWidth * 20,fmHeight);
//         pArea = new Rectangle2D.Float(bi.getWidth(null) - fmWidth * 6,fmHeight * (numRows + 1),fmWidth * 6,fmHeight);
//         mArea = new Rectangle2D.Float((float)(sArea.getX()+ sArea.getWidth()) + fmWidth + fmWidth,
//                                       fmHeight * (numRows + 1),
//                                       fmWidth + fmWidth,
//                                       fmHeight);

         separatorLine.setLine(0,
               (screen.fmHeight * (numRows + 1)) - (screen.fmHeight / 2),
               bi.getWidth(null),
               (screen.fmHeight * (numRows + 1)) - (screen.fmHeight / 2));

         g2d.setColor(screen.colorBlue);
         g2d.draw(separatorLine);
         gg2d = g2d;
      return g2d;
   }


   public void drawCursor()   {
//      public void drawCursor(int row, int col)   {
//                           int fmWidth, int fmHeight,
//                           boolean insertMode, int crossHair,
//                           boolean rulerFixed,
//                           int cursorSize, Color colorCursor,
//                           Color colorBg,Color colorWhite,
//                           Font font,int botOffset) {

      	int row = screen.getRow(screen.getLastPos());
      	int col = screen.getCol(screen.getLastPos());

      	int fmHeight = screen.fmHeight;
      	int fmWidth = screen.fmWidth;
      	int botOffset = screen.cursorBottOffset;
      	int cursorSize = screen.cursorSize;
      	boolean insertMode = screen.insertMode;
      	boolean rulerFixed = screen.rulerFixed;
      	int crossHair = screen.crossHair;

         Graphics2D g2 = getDrawingArea();

         switch (screen.cursorSize) {
            case 0:
               cursor.setRect(
                     fmWidth * (col),
                           (fmHeight * (row + 1)) - botOffset,
                           fmWidth,
                           1
                           );
               break;
            case 1:
               cursor.setRect(
                     fmWidth * (col),
                           (fmHeight * (row + 1) - fmHeight / 2),
                           fmWidth,
                           (fmHeight / 2) - botOffset
                           );
               break;
            case 2:
               cursor.setRect(
                     fmWidth * (col),
                           (fmHeight * row),
                           fmWidth,
                           fmHeight - botOffset
                           );
               break;
         }

         if (insertMode && cursorSize != 1) {
               cursor.setRect(
                     fmWidth * (col),
                           (fmHeight * (row + 1) - fmHeight / 2),
                           fmWidth,
                           (fmHeight / 2) - botOffset
                           );
         }

         Rectangle r = cursor.getBounds();
         r.setSize(r.width,r.height);

         g2.setColor(screen.colorCursor);
         g2.setXORMode(screen.colorBg);

         g2.fill(cursor);

         screen.updateImage(r);

         if (!rulerFixed) {
            crossRow = row;
            crossCol = col;
            crossRect.setBounds(r);
         }
         else {
            if (crossHair == 0) {
               crossRow = row;
               crossCol = col;
               crossRect.setBounds(r);
            }
         }

         switch (crossHair) {
            case 1:  // horizontal
               g2.drawLine(0,(fmHeight * (crossRow + 1))- botOffset,
                           bi.getWidth(null),
                           (fmHeight * (crossRow + 1))- botOffset);
               screen.updateImage(0,fmHeight * (crossRow + 1)- botOffset,
                              bi.getWidth(null),1);
               break;
            case 2:  // vertical
               g2.drawLine(crossRect.x,0,crossRect.x,bi.getHeight(null) - fmHeight - fmHeight);
               screen.updateImage(crossRect.x,0,1,bi.getHeight(null) - fmHeight - fmHeight);
               break;

            case 3:  // horizontal & vertical
               g2.drawLine(0,(fmHeight * (crossRow + 1))- botOffset,
                           bi.getWidth(null),
                           (fmHeight * (crossRow + 1))- botOffset);
               g2.drawLine(crossRect.x,0,crossRect.x,bi.getHeight(null) - fmHeight - fmHeight);
               screen.updateImage(0,fmHeight * (crossRow + 1)- botOffset,
                              bi.getWidth(null),1);
               screen.updateImage(crossRect.x,0,1,bi.getHeight(null) - fmHeight - fmHeight);
               break;
         }

         g2.dispose();
         g2 = getWritingArea(screen.font);
         g2.setPaint(screen.colorBg);

         g2.fill(pArea);
         g2.setColor(screen.colorWhite);

         g2.drawString((row + 1) + "/" + (col + 1)
                        ,(float)pArea.getX(),
                        (float)pArea.getY() + fmHeight);
         screen.updateImage(pArea.getBounds());
         g2.dispose();

   }

   private void drawScriptRunning(Color color) {

      Graphics2D g2d;

      // get ourselves a global pointer to the graphics
      g2d = (Graphics2D)bi.getGraphics();

      g2d.setColor(color);

      // set the points for the polygon
      int[] xs = {(int)scriptArea.getX(),
                  (int)scriptArea.getX(),
                  (int)scriptArea.getX() + (int)(scriptArea.getWidth())};
      int[] ys = {(int)scriptArea.getY(),
                  (int)scriptArea.getY() + (int)scriptArea.getHeight(),
                  (int)scriptArea.getY() + (int)(scriptArea.getHeight() / 2)};

      // now lets drawit
      g2d.fillPolygon(xs,ys,3);
      g2d.setClip(scriptArea);
//      drawImageBuffer(g2d,(int)scriptArea.getX(),
//                        (int)scriptArea.getY(),
//                        (int)scriptArea.getWidth(),(int)scriptArea.getHeight());

      // get rid of the pointers
      g2d.dispose();


   }

   private void eraseScriptRunning(Color color) {

      Graphics2D g2d;

      // get ourselves a global pointer to the graphics
      g2d = (Graphics2D)bi.getGraphics();

      g2d.setColor(color);
      g2d.fill(scriptArea);
      g2d.dispose();


   }

   /**
    * Returns a pointer to the graphics area that we can draw on
    */
   public Graphics2D getDrawingArea() {

      try {
         synchronized (lock) {
            // wait until there is something to read
            while (bi == null) {
               log.debug(" bi = null ");
               lock.wait();
            }
            // we have the lock and state we're seeking
            Graphics2D g2;

            g2 = bi.createGraphics();
            // tell waiting threads to wake up
            lock.notifyAll();
            return g2;
         }
      }
      catch (InterruptedException ie) {
         log.warn("getDrawingarea : " + ie.getMessage());
         return null;
      }
   }

   public synchronized void drawImageBuffer(Graphics2D gg2d,int x, int y, int width, int height) {

       /**
        * @todo this is a hack and should be fixed at the root of the problem
        */
      if (gg2d == null) {
         log.debug(" we got a null graphic object ");
         return;
      }

      synchronized (lock) {
         gg2d.drawImage(bi.getSubimage(x,y,width,height),null,x + offLeft,y+ offTop);
         // tell waiting threads to wake up
         lock.notifyAll();
      }

   }

   public synchronized void drawImageBuffer(Graphics2D gg2d) {

       /**
        * @todo this is a hack and should be fixed at the root of the problem
        */
      if (gg2d == null) {
		 log.debug(" we got a null graphic object ");
         return;
      }

      synchronized (lock) {

//         // lets calculate the offsets
//         Rectangle r = gg2d.getClipBounds();
//         offLeft = (r.width - width) / 2;
//         offTop = (r.height - height) / 2;
//         resized = false;
//         if (offLeft < 0)
//            offLeft = 0;
//         if (offTop <0 )
//            offTop = 0;
         gg2d.drawImage(bi,null,offLeft,offTop);
         // tell waiting threads to wake up
         lock.notifyAll();
      }


   }

  /**
    * Returns a pointer to the graphics area that we can write on
    */
   public Graphics2D getWritingArea(Font font) {

      Graphics2D g2;
      // we could be in the middle of creating the graphics because of the
      //    threads, resizing etc....   so lets wait until we have one.
      //    If this causes problems we should implement a thresh-hold of sorts
      //    to keep an infinate loop from occurring.  So far not problems
      try {
         synchronized (lock) {
            // wait until there is something to read
            while (bi == null) {
               log.debug( " bi = null wa ");
               lock.wait();
            }
               // we have the lock and state we're seeking

            g2 = bi.createGraphics();

            if (g2 != null) {
               if (antialiased)
                  g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
               g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                     RenderingHints.VALUE_COLOR_RENDER_SPEED);
               g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                     RenderingHints.VALUE_RENDER_SPEED);
               g2.setFont(font);
            }

            // tell waiting threads to wake up
            lock.notifyAll();
            return g2;

         }
      }
      catch (InterruptedException ie) {
         log.warn("getWritingarea : " + ie.getMessage());
         return null;
      }
   }

   public void setUseAntialias(boolean antialiased) {
      this.antialiased = antialiased;
   }

   private void setStatus(ScreenOIA oia) {

      int attr = oia.getLevel();
      int value = oia.getInputInhibited();
      String s = oia.getInhibitedText();
      Graphics2D g2d = getWritingArea(screen.font);
//      log.info(attr + ", " + value + ", " + s);
      if (g2d == null)
         return;

      try {
         g2d.setColor(screen.colorBg);
         g2d.fill(sArea);

         float Y = ((int)sArea.getY() + screen.fmHeight)- (screen.lm.getLeading() + screen.lm.getDescent());

         switch (attr) {

            case ScreenOIA.OIA_LEVEL_INPUT_INHIBITED:
               if (value == ScreenOIA.INPUTINHIBITED_SYSTEM_WAIT) {
                  g2d.setColor(screen.colorWhite);

                  if (s != null)
                     g2d.drawString(s,(float)sArea.getX(),Y);
                  else
                     g2d.drawString(xSystem,(float)sArea.getX(),Y);
               }
               break;
            case ScreenOIA.OIA_LEVEL_INPUT_ERROR:
               if (value == ScreenOIA.INPUTINHIBITED_SYSTEM_WAIT) {
                  g2d.setColor(screen.colorRed);

                  if (s != null)
                     g2d.drawString(s,(float)sArea.getX(),Y);
                  else
                     g2d.drawString(xError,(float)sArea.getX(),Y);

               }
               break;

         }
   		screen.updateImage(sArea.getBounds());
         g2d.dispose();
      }
      catch (Exception e) {

         log.warn(" gui graphics setStatus " + e.getMessage());

      }
   }

   public void onScreenChanged(int which, int sr, int sc, int er, int ec) {

//      log.info("screen updated -> " +  sr + ", " + sc + ", " + er + ", " + ec);

//		workR.setBounds(sr, sc, ec, er);

		int rows = er - sr;
		int cols = 0;
		int lc = 0;
      int lenScreen = screen.getScreenLength();
		int lr = screen.getPos(sr,sc);
      int numCols = screen.getCols();

		while (rows-- >= 0) {
			cols = ec - sc;
			lc = lr;
			while (cols-- >= 0) {
				if (lc >= 0 && lc < lenScreen) {
//					drawChar(gg2d,screen.screen[lc],screen.getRow(lc),screen.getCol(lc));
					drawChar(gg2d,lc,screen.getRow(lc),screen.getCol(lc));
               lc++;
				}
			}
			lr += numCols;
		}
//      screen.dumpScreen();
   }

   public void onOIAChanged(ScreenOIA changedOIA) {

      switch (changedOIA.getLevel()) {

         case ScreenOIA.OIA_LEVEL_KEYS_BUFFERED:
            if (changedOIA.isKeysBuffered()) {
               Graphics2D g2d = getWritingArea(screen.font);
               float Y = (screen.fmHeight * (screen.getRows() + 2))
                     - (screen.lm.getLeading() + screen.lm.getDescent());
               g2d.setColor(screen.colorYellow);
               g2d.drawString("KB", (float) kbArea.getX(), Y);
               screen.updateImage(kbArea.getBounds());
               g2d.dispose();
            }
            else {

               Graphics2D g2d = getWritingArea(screen.font);

               g2d.setColor(screen.colorBg);
               g2d.fill(kbArea);
               screen.updateImage(kbArea.getBounds());
               g2d.dispose();


            }
            break;
         case ScreenOIA.OIA_LEVEL_MESSAGE_LIGHT_OFF:
            Graphics2D g2d = getWritingArea(screen.font);

            g2d.setColor(screen.colorBg);
            g2d.fill(mArea);
            screen.updateImage(mArea.getBounds());
            g2d.dispose();
            break;
         case ScreenOIA.OIA_LEVEL_MESSAGE_LIGHT_ON:
            g2d = getWritingArea(screen.font);
            float Y = (screen.fmHeight * (screen.getRows() + 2))
                  - (screen.lm.getLeading() + screen.lm.getDescent());
            g2d.setColor(screen.colorBlue);
            g2d.drawString("MW", (float) mArea.getX(), Y);
            screen.updateImage(mArea.getBounds());
            g2d.dispose();
            break;
         case ScreenOIA.OIA_LEVEL_SCRIPT:
            if (changedOIA.isScriptActive()) {
               drawScriptRunning(screen.colorGreen);
               screen.updateImage(scriptArea.getBounds());
            }
            else {
               eraseScriptRunning(screen.colorBg);
               screen.updateImage(scriptArea.getBounds());

            }
            break;
         case ScreenOIA.OIA_LEVEL_INPUT_INHIBITED:
         case ScreenOIA.OIA_LEVEL_NOT_INHIBITED:
         case ScreenOIA.OIA_LEVEL_INPUT_ERROR:
            setStatus(changedOIA);
            break;

      }
   }

   /**
    * get the
    */
   public Rectangle2D getTextArea () {
      return tArea;
   }

   public Rectangle2D getScreenArea () {
      return aArea;
   }

   public Rectangle2D getCommandLineArea () {
      return cArea;
   }

   public Rectangle2D getStatusArea () {
      return sArea;
   }

   public Rectangle2D getPositionArea () {
      return pArea;
   }

   public Rectangle2D getMessageArea () {
      return mArea;
   }

   public Rectangle2D getInsertIndicatorArea () {
      return iArea;
   }

   public Rectangle2D getKBIndicatorArea () {
      return kbArea;
   }

   public Rectangle2D getScriptIndicatorArea () {
      return scriptArea;
   }

   public int getWidth() {

      synchronized (lock) {
         // tell waiting threads to wake up
         lock.notifyAll();
         return bi.getWidth();
      }

   }
   public int getHeight() {

      synchronized (lock) {
         // tell waiting threads to wake up
         lock.notifyAll();
         return bi.getHeight();
      }
   }
   public int getWidth(ImageObserver io) {

      synchronized (lock) {
         // tell waiting threads to wake up
         lock.notifyAll();
         return bi.getWidth(io);
      }
   }

   public int getHeight(ImageObserver io) {

      synchronized (lock) {
         // tell waiting threads to wake up
         lock.notifyAll();
         return bi.getHeight(io);
      }
   }

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

   public final void drawChar(Graphics2D g, ScreenChar sc, int row, int col) {

      Screen5250 s = screen;

      Color fg = sc.fg;
      Color bg = sc.bg;

      cArea.setRect((s.fmWidth*col),s.fmHeight * row,s.fmWidth,s.fmHeight);
      int x = s.fmWidth * col;
      int y = s.fmHeight * row;
      int cy = (int)(y + s.fmHeight - (s.lm.getDescent() + s.lm.getLeading()));

//      int x = sc.x;
//      int y = sc.y;
      int attr = sc.attr;
//      int cy = sc.cy;

      if (sc.attributePlace && s.isShowHex()) {
//      if ((sc.sChar[0] == 0x20 || sc.sChar[0] == 0x0 || nonDisplay) && s.isShowHex()) {
         Font f = g.getFont();

         Font k = f.deriveFont(f.getSize2D()/2);
         g.setFont(k);
         g.setColor(s.colorHexAttr);
         char[] a = Integer.toHexString(attr).toCharArray();
         g.drawChars(a, 0, 1, x, y + (int)(s.fmHeight /2));
         g.drawChars(a, 1, 1, x+(int)(s.fmWidth/2),
            (int)(y + s.fmHeight - (s.lm.getDescent() + s.lm.getLeading())-2));
         g.setFont(f);
//return;
      }

      if(!sc.nonDisplay && !sc.attributePlace) {

         if (!sc.useGui) {
            g.setColor(bg);
            g.fill(sc.cArea);
         }
         else {

            if (bg == s.colorBg && sc.whichGui >= FIELD_LEFT && sc.whichGui <= FIELD_ONE)
               g.setColor(s.colorGUIField);
            else
               g.setColor(bg);

            g.fill(sc.cArea);

         }

         if (sc.useGui && (sc.whichGui < FIELD_LEFT)) {
            int w = 0;

            g.setColor(fg);

            switch (sc.whichGui) {

               case UPPER_LEFT:
                  if (sc.sChar[0] == '.') {
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
                  if (sc.sChar[0] == '.') {

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
                  if (sc.sChar[0] == '.') {
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
                  if (sc.sChar[0] == ':') {
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
                  if (sc.sChar[0] == ':') {
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
                  if (sc.sChar[0] == ':') {

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
                  if (sc.sChar[0] == '.') {

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
                  if (sc.sChar[0] == ':') {
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
            if (sc.sChar[0] != 0x0) {
            // use this until we define colors for gui stuff
               if ((sc.useGui && sc.whichGui < BUTTON_LEFT) && (fg == s.colorGUIField))

                  g.setColor(Color.black);
               else
                  g.setColor(fg);

                  try {
                     if (sc.useGui)

                        if (sc.sChar[0] == 0x1C)
                           g.drawChars(sc.dupChar, 0, 1, x+1, cy -2);
                        else
                           g.drawChars(sc.sChar, 0, 1, x+1, cy -2);
                     else
                        if (sc.sChar[0] == 0x1C)
                           g.drawChars(sc.dupChar, 0, 1, x, cy -2);
                        else
                           g.drawChars(sc.sChar, 0, 1, x, cy -2);
                  }
                  catch (IllegalArgumentException iae) {
                     System.out.println(" ScreenChar iae " + iae.getMessage());

                  }
            }
            if(sc.underLine ) {

               if (!sc.useGui || s.guiShowUnderline) {
                  g.setColor(fg);
//                  g.drawLine(x, cy -2, (int)(x + s.fmWidth), cy -2);
//                  g.drawLine(x, (int)(y + (s.fmHeight - s.lm.getLeading()-5)), (int)(x + s.fmWidth), (int)(y + (s.fmHeight - s.lm.getLeading())-5));
                  g.drawLine(x, (int)(y + (s.fmHeight - (s.lm.getLeading() + s.lm.getDescent()))), (int)(x + s.fmWidth), (int)(y + (s.fmHeight -(s.lm.getLeading() + s.lm.getDescent()))));

               }
            }

            if(sc.colSep) {
               g.setColor(s.colorSep);
               switch (s.getColSepLine()) {
                  case 0:  // line
                     g.drawLine(x, y, x, y + s.fmHeight - 1);
                     g.drawLine(x + s.fmWidth - 1, y, x + s.fmWidth - 1, y + s.fmHeight);
                     break;
                  case 1:  // short line
                     g.drawLine(x,  y + s.fmHeight - (int)s.lm.getLeading()-4, x, y + s.fmHeight);
                     g.drawLine(x + s.fmWidth - 1, y + s.fmHeight - (int)s.lm.getLeading()-4, x + s.fmWidth - 1, y + s.fmHeight);
                     break;
                  case 2:  // dot
                     g.drawLine(x,  y + s.fmHeight - (int)s.lm.getLeading()-3, x, y + s.fmHeight - (int)s.lm.getLeading()-4);
                     g.drawLine(x + s.fmWidth - 1, y + s.fmHeight - (int)s.lm.getLeading()-3, x + s.fmWidth - 1, y + s.fmHeight - (int)s.lm.getLeading()-4);
                     break;
                  case 3:  // hide
                     break;
               }
            }
         }
      }

      if (sc.useGui & (sc.whichGui >= FIELD_LEFT)) {
            int w = 0;

            switch (sc.whichGui) {

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

   Rectangle csArea = new Rectangle();
   char sChar[] = new char[1];
   // Dup Character array for display output
   public static final transient char[] dupChar = {'*'};
   public final void drawChar(Graphics2D g, int pos, int row, int col) {

      Screen5250 s = screen;
      ScreenPlanes planes = s.planes;

      int attr = planes.getCharAttr(pos);
      sChar[0] = planes.getChar(pos);
      setCharAttr(attr);
      boolean attributePlace = planes.isAttributePlace(pos);
      boolean useGui = planes.isUseGui(pos);
      int whichGui = planes.getWhichGUI(pos);

      csArea.setRect((s.fmWidth*col),s.fmHeight * row,s.fmWidth,s.fmHeight);

      int x = s.fmWidth * col;
      int y = s.fmHeight * row;
      int cy = (int)(y + s.fmHeight - (s.lm.getDescent() + s.lm.getLeading()));

//      int x = sc.x;
//      int y = sc.y;
//      int cy = sc.cy;

      if (attributePlace && s.isShowHex()) {
//      if ((sc.sChar[0] == 0x20 || sc.sChar[0] == 0x0 || nonDisplay) && s.isShowHex()) {
         Font f = g.getFont();

         Font k = f.deriveFont(f.getSize2D()/2);
         g.setFont(k);
         g.setColor(s.colorHexAttr);
         char[] a = Integer.toHexString(attr).toCharArray();
         g.drawChars(a, 0, 1, x, y + (int)(s.fmHeight /2));
         g.drawChars(a, 1, 1, x+(int)(s.fmWidth/2),
            (int)(y + s.fmHeight - (s.lm.getDescent() + s.lm.getLeading())-2));
         g.setFont(f);
//return;
      }

      if(!nonDisplay && !attributePlace) {

         if (!useGui) {
            g.setColor(bg);
            g.fill(csArea);
         }
         else {

            if (bg == s.colorBg && whichGui >= FIELD_LEFT && whichGui <= FIELD_ONE)
               g.setColor(s.colorGUIField);
            else
               g.setColor(bg);

            g.fill(csArea);

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

                        if (sChar[0] == 0x1C)
                           g.drawChars(dupChar, 0, 1, x+1, cy -2);
                        else
                           g.drawChars(sChar, 0, 1, x+1, cy -2);
                     else
                        if (sChar[0] == 0x1C)
                           g.drawChars(dupChar, 0, 1, x, cy -2);
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
//                  g.drawLine(x, (int)(y + (s.fmHeight - s.lm.getLeading()-5)), (int)(x + s.fmWidth), (int)(y + (s.fmHeight - s.lm.getLeading())-5));
                  g.drawLine(x, (int)(y + (s.fmHeight - (s.lm.getLeading() + s.lm.getDescent()))), (int)(x + s.fmWidth), (int)(y + (s.fmHeight -(s.lm.getLeading() + s.lm.getDescent()))));

               }
            }

            if(colSep) {
               g.setColor(s.colorSep);
               switch (s.getColSepLine()) {
                  case 0:  // line
                     g.drawLine(x, y, x, y + s.fmHeight - 1);
                     g.drawLine(x + s.fmWidth - 1, y, x + s.fmWidth - 1, y + s.fmHeight);
                     break;
                  case 1:  // short line
                     g.drawLine(x,  y + s.fmHeight - (int)s.lm.getLeading()-4, x, y + s.fmHeight);
                     g.drawLine(x + s.fmWidth - 1, y + s.fmHeight - (int)s.lm.getLeading()-4, x + s.fmWidth - 1, y + s.fmHeight);
                     break;
                  case 2:  // dot
                     g.drawLine(x,  y + s.fmHeight - (int)s.lm.getLeading()-3, x, y + s.fmHeight - (int)s.lm.getLeading()-4);
                     g.drawLine(x + s.fmWidth - 1, y + s.fmHeight - (int)s.lm.getLeading()-3, x + s.fmWidth - 1, y + s.fmHeight - (int)s.lm.getLeading()-4);
                     break;
                  case 3:  // hide
                     break;
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

   boolean      colSep = false;
   boolean   underLine = false;
   boolean   nonDisplay = false;
   Color fg;
   Color bg;

   private void setCharAttr(int attr) {

      Screen5250 s = screen;
      colSep = false;
      underLine = false;
      nonDisplay = false;

      switch(attr) {
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
//            colSep = true;
            underLine = true;
            break;

         case 53:
            fg = s.colorBg;
            bg = s.colorTurq;
//            colSep = true;
            underLine = true;
            break;

         case 54:
            fg = s.colorYellow;
            bg = s.colorBg;
//            colSep = true;
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

         case 63:  // nondisplay
            nonDisplay = true;
            break;
         default:
            fg = s.colorYellow;
            break;

      }

   }


}