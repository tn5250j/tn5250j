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
import javax.swing.JPanel;

import org.tn5250j.event.ScreenOIAListener;
import org.tn5250j.event.ScreenListener;
import org.tn5250j.tools.logging.*;
import org.tn5250j.tools.GUIGraphicsUtils;

public class GuiGraphicBuffer implements ScreenOIAListener, ScreenListener, TN5250jConstants {

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
   private Data updateRect;
   private int columnWidth;
   private int rowHeight;
	LineMetrics lm;
	Font font;
   
   private TN5250jLogger log = TN5250jLogFactory.getLogger ("GFX");

   public GuiGraphicBuffer (Screen5250 screen) {

      this.screen = screen;
      
      columnWidth = screen.fmWidth;
      rowHeight = screen.fmHeight;
      font = screen.font;
      lm = screen.lm;
      
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
            columnWidth = screen.fmWidth;
            rowHeight = screen.fmHeight;
            font = screen.font;
            lm = screen.lm;
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
         g2d.setFont(font);


         g2d.setColor(screen.colorBg);
         g2d.fillRect(0,0,bi.getWidth(null),bi.getHeight(null));
         tArea.setRect(0,0,bi.getWidth(null),(rowHeight * (numRows)));
         cArea.setRect(0,rowHeight * (numRows + 1),bi.getWidth(null),rowHeight * (numRows + 1));
         aArea.setRect(0,0,bi.getWidth(null),bi.getHeight(null));
         sArea.setRect(columnWidth * 9,rowHeight * (numRows + 1),columnWidth * 20,rowHeight);
         pArea.setRect(bi.getWidth(null) - columnWidth * 6,rowHeight * (numRows + 1),columnWidth * 6,rowHeight);
         mArea.setRect((float)(sArea.getX()+ sArea.getWidth()) + columnWidth + columnWidth,
               rowHeight * (numRows + 1),
               columnWidth + columnWidth,
               rowHeight);
         kbArea.setRect((float)(sArea.getX()+ sArea.getWidth()) + (20 * columnWidth),
               rowHeight * (numRows + 1),
               columnWidth + columnWidth,
               rowHeight);
         scriptArea.setRect((float)(sArea.getX()+ sArea.getWidth()) + (16 * columnWidth),
               rowHeight * (numRows + 1),
               columnWidth + columnWidth,
               rowHeight);
//         cArea = new Rectangle2D.Float(0,fmHeight * (numRows + 1),bi.getWidth(null),fmHeight * (numRows + 1));
//         aArea = new Rectangle2D.Float(0,0,bi.getWidth(null),bi.getHeight(null));
//         sArea = new Rectangle2D.Float(fmWidth * 9,fmHeight * (numRows + 1),fmWidth * 20,fmHeight);
//         pArea = new Rectangle2D.Float(bi.getWidth(null) - fmWidth * 6,fmHeight * (numRows + 1),fmWidth * 6,fmHeight);
//         mArea = new Rectangle2D.Float((float)(sArea.getX()+ sArea.getWidth()) + fmWidth + fmWidth,
//                                       fmHeight * (numRows + 1),
//                                       fmWidth + fmWidth,
//                                       fmHeight);

         separatorLine.setLine(0,
               (rowHeight * (numRows + 1)) - (rowHeight / 2),
               bi.getWidth(null),
               (rowHeight * (numRows + 1)) - (rowHeight / 2));

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

      	int botOffset = screen.cursorBottOffset;
      	int cursorSize = screen.cursorSize;
      	boolean insertMode = screen.insertMode;
      	boolean rulerFixed = screen.rulerFixed;
      	int crossHair = screen.crossHair;

         Graphics2D g2 = getDrawingArea();

         switch (screen.cursorSize) {
            case 0:
               cursor.setRect(
                     columnWidth * (col),
                           (rowHeight * (row + 1)) - botOffset,
                           columnWidth,
                           1
                           );
               break;
            case 1:
               cursor.setRect(
                     columnWidth * (col),
                           (rowHeight * (row + 1) - rowHeight / 2),
                           columnWidth,
                           (rowHeight / 2) - botOffset
                           );
               break;
            case 2:
               cursor.setRect(
                     columnWidth * (col),
                           (rowHeight * row),
                           columnWidth,
                           rowHeight - botOffset
                           );
               break;
         }

         if (insertMode && cursorSize != 1) {
               cursor.setRect(
                     columnWidth * (col),
                           (rowHeight * (row + 1) - rowHeight / 2),
                           columnWidth,
                           (rowHeight / 2) - botOffset
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
               g2.drawLine(0,(rowHeight * (crossRow + 1))- botOffset,
                           bi.getWidth(null),
                           (rowHeight * (crossRow + 1))- botOffset);
               screen.updateImage(0,rowHeight * (crossRow + 1)- botOffset,
                              bi.getWidth(null),1);
               break;
            case 2:  // vertical
               g2.drawLine(crossRect.x,0,crossRect.x,bi.getHeight(null) - rowHeight - rowHeight);
               screen.updateImage(crossRect.x,0,1,bi.getHeight(null) - rowHeight - rowHeight);
               break;

            case 3:  // horizontal & vertical
               g2.drawLine(0,(rowHeight * (crossRow + 1))- botOffset,
                           bi.getWidth(null),
                           (rowHeight * (crossRow + 1))- botOffset);
               g2.drawLine(crossRect.x,0,crossRect.x,bi.getHeight(null) - rowHeight - rowHeight);
               screen.updateImage(0,rowHeight * (crossRow + 1)- botOffset,
                              bi.getWidth(null),1);
               screen.updateImage(crossRect.x,0,1,bi.getHeight(null) - rowHeight - rowHeight);
               break;
         }

         g2.dispose();
         g2 = getWritingArea(screen.font);
         g2.setPaint(screen.colorBg);

         g2.fill(pArea);
         g2.setColor(screen.colorWhite);

         g2.drawString((row + 1) + "/" + (col + 1)
                        ,(float)pArea.getX(),
                        (float)pArea.getY() + rowHeight);
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

         float Y = ((int)sArea.getY() + rowHeight)- (lm.getLeading() + lm.getDescent());

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


      updateRect = new Data (sr,sc,er,ec);

      int pos = 0;
		while (rows-- >= 0) {
			cols = ec - sc;
			lc = lr;
			while (cols-- >= 0) {
				if (lc >= 0 && lc < lenScreen) {
					drawChar(gg2d,pos++,screen.getRow(lc),screen.getCol(lc));
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
               float Y = (rowHeight * (screen.getRows() + 2))
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
            float Y = (rowHeight * (screen.getRows() + 2))
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


   protected Data fillData(int startRow, int startCol, int endRow, int endCol) {

      return new Data(startRow, startCol, endRow, endCol );

   }

   Rectangle csArea = new Rectangle();
   char sChar[] = new char[1];

   protected class Data {


      public Data(char[] text, char[] attr, char[] color, char[] extended, char[] graphic) {
         this.text = text;
         this.color = color;
         this.extended = extended;
         this.graphic = graphic;
         this.attr = attr;
         this.field = null;
      }

      public Data(int startRow, int startCol, int endRow, int endCol) {
         startRow++;
         startCol++;
         endRow++;
         endCol++;
         int size = ((endCol - startCol) + 1) * ((endRow - startRow) +1);
         text = new char[size];
         attr = new char[size];
         color = new char[size];
         extended =new char[size];
         graphic = new char[size];
         field = null;
         screen.GetScreenRect(text, size, startRow, startCol, endRow, endCol, PLANE_TEXT);
         screen.GetScreenRect(attr, size, startRow, startCol, endRow, endCol, PLANE_ATTR);
         screen.GetScreenRect(color, size, startRow, startCol, endRow, endCol, PLANE_COLOR);
         screen.GetScreenRect(extended, size, startRow, startCol, endRow, endCol, PLANE_EXTENDED);
         screen.GetScreenRect(graphic, size, startRow, startCol, endRow, endCol, PLANE_EXTENDED_GRAPHIC);
      }

      public char[] text;
      public char[] attr;
      public char[] color;
      public char[] extended;
      public final char[] graphic;
      public final char[] field;
   }

   public final Rectangle modelToView(int row, int col)
   {
     return modelToView(row, col, new Rectangle());
   }

   public final Rectangle modelToView(int row, int col, Rectangle r) {
      
      // right now row and column is 1,1 offset based.  This will need
      //   to be changed to 0,0 offset based by subtracting 1 from them
      //   when the screen is being passed this way 
//     r.x      =  (col - 1) * columnWidth;
//     r.y      =  (row - 1) * rowHeight;
     r.x      =  col * columnWidth;
     r.y      =  row * rowHeight;
     r.width  = columnWidth;
     r.height = rowHeight;
     return r;
   }

   // Dup Character array for display output
   public static final transient char[] dupChar = {'*'};
   public final void drawChar(Graphics2D g, int pos, int row, int col) {

      Screen5250 s = screen;
      ScreenPlanes planes = s.planes;

      int attr = updateRect.attr[pos];
      sChar[0] = updateRect.text[pos];
      setDrawAttr(pos);
      boolean attributePlace = planes.isAttributePlace(s.getPos(row,col));
      int whichGui = updateRect.graphic[pos];
      boolean useGui = whichGui == 0 ? false : true;

      csArea = modelToView(row, col, csArea);
      

      int x = csArea.x;
      int y = csArea.y;
      int cy = (int)(y + rowHeight - (s.lm.getDescent() + s.lm.getLeading()));

      if (attributePlace && s.isShowHex()) {
//      if ((sc.sChar[0] == 0x20 || sc.sChar[0] == 0x0 || nonDisplay) && s.isShowHex()) {
         Font f = g.getFont();

         Font k = f.deriveFont(f.getSize2D()/2);
         g.setFont(k);
         g.setColor(s.colorHexAttr);
         char[] a = Integer.toHexString(attr).toCharArray();
         g.drawChars(a, 0, 1, x, y + (int)(rowHeight /2));
         g.drawChars(a, 1, 1, x+(int)(columnWidth/2),
            (int)(y + rowHeight - (s.lm.getDescent() + s.lm.getLeading())-2));
         g.setFont(f);
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
                                             x,y,columnWidth,rowHeight);

                     }
                     else {

                        GUIGraphicsUtils.drawWinUpperLeft(g,
                                             GUIGraphicsUtils.WINDOW_NORMAL,
                                             fg,
                                             x,y,columnWidth,rowHeight);

                     }
                  }
               break;
               case UPPER:
                  if (sChar[0] == '.') {

                     if (s.isUsingGuiInterface()) {
                        GUIGraphicsUtils.drawWinUpper(g,
                                             GUIGraphicsUtils.WINDOW_GRAPHIC,
                                             s.colorBlue,
                                             x,y,columnWidth,rowHeight);


                     }
                     else {

                        GUIGraphicsUtils.drawWinUpper(g,
                                             GUIGraphicsUtils.WINDOW_NORMAL,
                                             fg,
                                             x,y,columnWidth,rowHeight);
                     }
                  }
               break;
               case UPPER_RIGHT:
                  if (sChar[0] == '.') {
                     if (s.isUsingGuiInterface()) {

                        GUIGraphicsUtils.drawWinUpperRight(g,
                                             GUIGraphicsUtils.WINDOW_GRAPHIC,
                                             s.colorBlue,
                                             x,y,columnWidth,rowHeight);


                     }
                     else {

                        GUIGraphicsUtils.drawWinUpperRight(g,
                                             GUIGraphicsUtils.WINDOW_NORMAL,
                                             fg,
                                             x,y,columnWidth,rowHeight);

                     }
                  }
               break;
               case GUI_LEFT:
                  if (sChar[0] == ':') {
                     if (s.isUsingGuiInterface()) {
                        GUIGraphicsUtils.drawWinLeft(g,
                                             GUIGraphicsUtils.WINDOW_GRAPHIC,
                                             bg,
                                             x,y,columnWidth,rowHeight);


                     }
                     else {

                        GUIGraphicsUtils.drawWinLeft(g,
                                             GUIGraphicsUtils.WINDOW_NORMAL,
                                             fg,
                                             x,y,columnWidth,rowHeight);

                        g.drawLine(x + columnWidth / 2,
                                    y,
                                    x + columnWidth / 2,
                                    y + rowHeight);
                     }
                  }
               break;
               case GUI_RIGHT:
                  if (sChar[0] == ':') {
                     if (s.isUsingGuiInterface()) {
                        GUIGraphicsUtils.drawWinRight(g,
                                             GUIGraphicsUtils.WINDOW_GRAPHIC,
                                             bg,
                                             x,y,columnWidth,rowHeight);


                     }
                     else {
                        GUIGraphicsUtils.drawWinRight(g,
                                             GUIGraphicsUtils.WINDOW_NORMAL,
                                             fg,
                                             x,y,columnWidth,rowHeight);

                     }
                  }
               break;
               case LOWER_LEFT:
                  if (sChar[0] == ':') {

                     if (s.isUsingGuiInterface()) {

                        GUIGraphicsUtils.drawWinLowerLeft(g,
                                             GUIGraphicsUtils.WINDOW_GRAPHIC,
                                             bg,
                                             x,y,columnWidth,rowHeight);


                     }
                     else {

                        GUIGraphicsUtils.drawWinLowerLeft(g,
                                             GUIGraphicsUtils.WINDOW_NORMAL,
                                             fg,
                                             x,y,columnWidth,rowHeight);
                     }
                  }
               break;
               case BOTTOM:
                  if (sChar[0] == '.') {

                     if (s.isUsingGuiInterface()) {


                        GUIGraphicsUtils.drawWinBottom(g,
                                             GUIGraphicsUtils.WINDOW_GRAPHIC,
                                             bg,
                                             x,y,columnWidth,rowHeight);


                     }
                     else {

                        GUIGraphicsUtils.drawWinBottom(g,
                                             GUIGraphicsUtils.WINDOW_NORMAL,
                                             fg,
                                             x,y,columnWidth,rowHeight);
                     }
                  }
               break;

               case LOWER_RIGHT:
                  if (sChar[0] == ':') {
                     if (s.isUsingGuiInterface()) {

                        GUIGraphicsUtils.drawWinLowerRight(g,
                                             GUIGraphicsUtils.WINDOW_GRAPHIC,
                                             bg,
                                             x,y,columnWidth,rowHeight);

                     }
                     else {

                        GUIGraphicsUtils.drawWinLowerRight(g,
                                             GUIGraphicsUtils.WINDOW_NORMAL,
                                             fg,
                                             x,y,columnWidth,rowHeight);

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
//                  g.drawLine(x, cy -2, (int)(x + columnWidth), cy -2);
//                  g.drawLine(x, (int)(y + (rowHeight - s.lm.getLeading()-5)), (int)(x + columnWidth), (int)(y + (rowHeight - s.lm.getLeading())-5));
                  g.drawLine(x, (int)(y + (rowHeight - (s.lm.getLeading() + s.lm.getDescent()))), (int)(x + columnWidth), (int)(y + (rowHeight -(s.lm.getLeading() + s.lm.getDescent()))));

               }
            }

            if(colSep) {
               g.setColor(s.colorSep);
               switch (s.getColSepLine()) {
                  case 0:  // line
                     g.drawLine(x, y, x, y + rowHeight - 1);
                     g.drawLine(x + columnWidth - 1, y, x + columnWidth - 1, y + rowHeight);
                     break;
                  case 1:  // short line
                     g.drawLine(x,  y + rowHeight - (int)s.lm.getLeading()-4, x, y + rowHeight);
                     g.drawLine(x + columnWidth - 1, y + rowHeight - (int)s.lm.getLeading()-4, x + columnWidth - 1, y + rowHeight);
                     break;
                  case 2:  // dot
                     g.drawLine(x,  y + rowHeight - (int)s.lm.getLeading()-3, x, y + rowHeight - (int)s.lm.getLeading()-4);
                     g.drawLine(x + columnWidth - 1, y + rowHeight - (int)s.lm.getLeading()-3, x + columnWidth - 1, y + rowHeight - (int)s.lm.getLeading()-4);
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
                                             columnWidth,rowHeight);

               break;
               case FIELD_MIDDLE:
                  GUIGraphicsUtils.draw3DMiddle(g, GUIGraphicsUtils.INSET, x,y,
                                             columnWidth,rowHeight);
               break;
               case FIELD_RIGHT:
                  GUIGraphicsUtils.draw3DRight(g, GUIGraphicsUtils.INSET, x,y,
                                             columnWidth,rowHeight);
               break;

               case FIELD_ONE:
                  GUIGraphicsUtils.draw3DOne(g, GUIGraphicsUtils.INSET, x,y,
                                             columnWidth,rowHeight);

               break;

               case BUTTON_LEFT:
               case BUTTON_LEFT_UP:
               case BUTTON_LEFT_DN:
               case BUTTON_LEFT_EB:

                  GUIGraphicsUtils.draw3DLeft(g, GUIGraphicsUtils.RAISED, x,y,
                                             columnWidth,rowHeight);

                  break;

               case BUTTON_MIDDLE:
               case BUTTON_MIDDLE_UP:
               case BUTTON_MIDDLE_DN:
               case BUTTON_MIDDLE_EB:

                  GUIGraphicsUtils.draw3DMiddle(g, GUIGraphicsUtils.RAISED, x,y,
                                             columnWidth,rowHeight);
                  break;

               case BUTTON_RIGHT:
               case BUTTON_RIGHT_UP:
               case BUTTON_RIGHT_DN:
               case BUTTON_RIGHT_EB:

                  GUIGraphicsUtils.draw3DRight(g, GUIGraphicsUtils.RAISED, x,y,
                                             columnWidth,rowHeight);

               break;

               // scroll bar
               case BUTTON_SB_UP:
                  GUIGraphicsUtils.drawScrollBar(g, GUIGraphicsUtils.RAISED, 1, x,y,
                                             columnWidth,rowHeight,
                                             s.colorWhite,s.colorBg);
                  break;

               // scroll bar
               case BUTTON_SB_DN:

                  GUIGraphicsUtils.drawScrollBar(g, GUIGraphicsUtils.RAISED, 2, x,y,
                                             columnWidth,rowHeight,
                                             s.colorWhite,s.colorBg);


                  break;
               // scroll bar
               case BUTTON_SB_GUIDE:

                  GUIGraphicsUtils.drawScrollBar(g, GUIGraphicsUtils.INSET, 0,x,y,
                                             columnWidth,rowHeight,
                                             s.colorWhite,s.colorBg);


                  break;

               // scroll bar
               case BUTTON_SB_THUMB:

                  GUIGraphicsUtils.drawScrollBar(g, GUIGraphicsUtils.INSET, 3,x,y,
                                             columnWidth,rowHeight,
                                             s.colorWhite,s.colorBg);


                  break;

            }
         }

   }

   protected Color getColor(char color, boolean background) {
      int c = 0;
      if (background)
         // background
         c = (color & 0xff00) >> 8;
      else
         // foreground
         c = color & 0x00ff;

      switch (c) {
         case COLOR_FG_BLACK:
            return screen.colorBg;
         case COLOR_FG_GREEN:
            return screen.colorGreen;
         case COLOR_FG_BLUE:
            return screen.colorBlue;
         case COLOR_FG_RED:
            return screen.colorRed;
         case COLOR_FG_YELLOW:
            return screen.colorYellow;
         case COLOR_FG_CYAN:
            return screen.colorTurq;
         case COLOR_FG_WHITE:
            return screen.colorWhite;
         case COLOR_FG_MAGENTA:
            return screen.colorPink;
         default:
           return Color.orange;
      }
   }

   boolean      colSep = false;
   boolean   underLine = false;
   boolean   nonDisplay = false;
   Color fg;
   Color bg;

   private void setDrawAttr(int pos) {

      Screen5250 s = screen;
      colSep = false;
      underLine = false;
      nonDisplay = false;

      fg = getColor(updateRect.color[pos],false);
      bg = getColor(updateRect.color[pos],true);
      underLine = (updateRect.extended[pos] & EXTENDED_5250_UNDERLINE) != 0;
      colSep = (updateRect.extended[pos] & EXTENDED_5250_COL_SEP) != 0;
      nonDisplay = (updateRect.extended[pos] & EXTENDED_5250_NON_DSP) != 0;
      
//	      switch(pos) {
//	         case 32: // green normal
//	            fg = s.colorGreen;
//	            bg = s.colorBg;
//	            break;
//	
//	         case 33: // green/revers
//	            fg = s.colorBg;
//	            bg = s.colorGreen;
//	            break;
//	
//	         case 34: // white normal
//	            fg = s.colorWhite;
//	            bg = s.colorBg;
//	            break;
//	
//	         case 35: // white/reverse
//	            fg = s.colorBg;
//	            bg = s.colorWhite;
//	            break;
//	
//	         case 36: // green/underline
//	            fg = s.colorGreen;
//	            bg = s.colorBg;
//	            underLine = true;
//	            break;
//	
//	         case 37: // green/reverse/underline
//	            fg = s.colorBg;
//	            bg = s.colorGreen;
//	            underLine = true;
//	            break;
//	
//	         case 38: // white/underline
//	            fg = s.colorWhite;
//	            bg = s.colorBg;
//	            underLine = true;
//	            break;
//	
//	         case 39:
//	            nonDisplay = true;
//	            break;
//	
//	         case 40:
//	         case 42: // red/normal
//	            fg = s.colorRed;
//	            bg = s.colorBg;
//	            break;
//	
//	         case 41:
//	         case 43: // red/reverse
//	            fg = s.colorBg;
//	            bg = s.colorRed;
//	            break;
//	
//	         case 44:
//	         case 46: // red/underline
//	            fg = s.colorRed;
//	            bg = s.colorBg;
//	            underLine = true;
//	            break;
//	
//	         case 45: // red/reverse/underline
//	            fg = s.colorBg;
//	            bg = s.colorRed;
//	            underLine = true;
//	            break;
//	
//	         case 47:
//	            nonDisplay = true;
//	            break;
//	
//	         case 48:
//	            fg = s.colorTurq;
//	            bg = s.colorBg;
//	            colSep = true;
//	            break;
//	
//	         case 49:
//	            fg = s.colorBg;
//	            bg = s.colorTurq;
//	            colSep = true;
//	            break;
//	
//	         case 50:
//	            fg = s.colorYellow;
//	            bg = s.colorBg;
//	            colSep = true;
//	            break;
//	
//	         case 51:
//	            fg = s.colorBg;
//	            bg = s.colorYellow;
//	            colSep = true;
//	            break;
//	
//	         case 52:
//	            fg = s.colorTurq;
//	            bg = s.colorBg;
//	//            colSep = true;
//	            underLine = true;
//	            break;
//	
//	         case 53:
//	            fg = s.colorBg;
//	            bg = s.colorTurq;
//	//            colSep = true;
//	            underLine = true;
//	            break;
//	
//	         case 54:
//	            fg = s.colorYellow;
//	            bg = s.colorBg;
//	//            colSep = true;
//	            underLine = true;
//	            break;
//	
//	         case 55:
//	            nonDisplay = true;
//	            break;
//	
//	         case 56: // pink
//	            fg = s.colorPink;
//	            bg = s.colorBg;
//	            break;
//	
//	         case 57: // pink/reverse
//	            fg = s.colorBg;
//	            bg = s.colorPink;
//	            break;
//	
//	         case 58: // blue/reverse
//	            fg = s.colorBlue;
//	            bg = s.colorBg;
//	            break;
//	
//	         case 59: // blue
//	            fg = s.colorBg;
//	            bg = s.colorBlue;
//	            break;
//	
//	         case 60: // pink/underline
//	            fg = s.colorPink;
//	            bg = s.colorBg;
//	            underLine = true;
//	            break;
//	
//	         case 61: // pink/reverse/underline
//	            fg = s.colorBg;
//	            bg = s.colorPink;
//	            underLine = true;
//	            break;
//	
//	         case 62: // blue/underline
//	            fg = s.colorBlue;
//	            bg = s.colorBg;
//	            underLine = true;
//	            break;
//	
//	         case 63:  // nondisplay
//	            nonDisplay = true;
//	            break;
//	         default:
//	            fg = s.colorYellow;
//	            break;

      

   }


}