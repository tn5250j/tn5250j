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

import java.awt.image.*;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.Color;
import java.awt.Rectangle;
import javax.swing.SwingUtilities;
import java.awt.Font;
import java.awt.font.*;

public class GuiGraphicBuffer {

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

   public GuiGraphicBuffer () {

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
         }
         // tell waiting threads to wake up
         lock.notifyAll();
      }
      return bi;
   }

   /**
    * Draw the operator information area
    */
   public Graphics2D drawOIA (int fmWidth,
                           int fmHeight,
                           int numRows,
                           int numCols,
                           Font font,
                           Color colorBg,
                           Color colorBlue
                           ) {

         Graphics2D g2d;

         // get ourselves a global pointer to the graphics
//         g2d = (Graphics2D)bi.getGraphics();
         g2d = getDrawingArea();

         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
               RenderingHints.VALUE_ANTIALIAS_ON);
         g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
               RenderingHints.VALUE_COLOR_RENDER_SPEED);
         g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
               RenderingHints.VALUE_RENDER_SPEED);
         g2d.setFont(font);


         g2d.setColor(colorBg);
         g2d.fillRect(0,0,bi.getWidth(null),bi.getHeight(null));
         tArea.setRect(0,0,bi.getWidth(null),(fmHeight * (numRows)));
         cArea.setRect(0,fmHeight * (numRows + 1),bi.getWidth(null),fmHeight * (numRows + 1));
         aArea.setRect(0,0,bi.getWidth(null),bi.getHeight(null));
         sArea.setRect(fmWidth * 9,fmHeight * (numRows + 1),fmWidth * 20,fmHeight);
         pArea.setRect(bi.getWidth(null) - fmWidth * 6,fmHeight * (numRows + 1),fmWidth * 6,fmHeight);
         mArea.setRect((float)(sArea.getX()+ sArea.getWidth()) + fmWidth + fmWidth,
                                       fmHeight * (numRows + 1),
                                       fmWidth + fmWidth,
                                       fmHeight);
         kbArea.setRect((float)(sArea.getX()+ sArea.getWidth()) + (20 * fmWidth),
                                       fmHeight * (numRows + 1),
                                       fmWidth + fmWidth,
                                       fmHeight);
         scriptArea.setRect((float)(sArea.getX()+ sArea.getWidth()) + (16 * fmWidth),
                                       fmHeight * (numRows + 1),
                                       fmWidth + fmWidth,
                                       fmHeight);
//         cArea = new Rectangle2D.Float(0,fmHeight * (numRows + 1),bi.getWidth(null),fmHeight * (numRows + 1));
//         aArea = new Rectangle2D.Float(0,0,bi.getWidth(null),bi.getHeight(null));
//         sArea = new Rectangle2D.Float(fmWidth * 9,fmHeight * (numRows + 1),fmWidth * 20,fmHeight);
//         pArea = new Rectangle2D.Float(bi.getWidth(null) - fmWidth * 6,fmHeight * (numRows + 1),fmWidth * 6,fmHeight);
//         mArea = new Rectangle2D.Float((float)(sArea.getX()+ sArea.getWidth()) + fmWidth + fmWidth,
//                                       fmHeight * (numRows + 1),
//                                       fmWidth + fmWidth,
//                                       fmHeight);

         separatorLine.setLine(0,
               (fmHeight * (numRows + 1)) - (fmHeight / 2),
               bi.getWidth(null),
               (fmHeight * (numRows + 1)) - (fmHeight / 2));

         g2d.setColor(colorBlue);
         g2d.draw(separatorLine);

      return g2d;
   }


   public void drawCursor(Screen5250 s,int row, int col,
                           int fmWidth, int fmHeight,
                           boolean insertMode, int crossHair,
                           int cursorSize, Color colorCursor,
                           Color colorBg,Color colorWhite,
                           Font font,int botOffset) {

         Graphics2D g2 = getDrawingArea();

         switch (cursorSize) {
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
         g2.setColor(colorCursor);
         g2.setXORMode(colorBg);

         g2.fill(cursor);
         s.updateImage(r);

         switch (crossHair) {
            case 1:  // horizontal
               g2.drawLine(0,(fmHeight * (row + 1))- botOffset,bi.getWidth(null),(fmHeight * (row + 1))- botOffset);
               s.updateImage(0,fmHeight * (row + 1)- botOffset,bi.getWidth(null),1);
               break;
            case 2:  // vertical
               g2.drawLine(r.x,0,r.x,bi.getHeight(null) - fmHeight - fmHeight);
               s.updateImage(r.x,0,1,bi.getHeight(null) - fmHeight - fmHeight);
               break;
            case 3:  // horizontal & vertical
               g2.drawLine(0,(fmHeight * (row + 1))- botOffset,bi.getWidth(null),(fmHeight * (row + 1))- botOffset);
               g2.drawLine(r.x,0,r.x,bi.getHeight(null) - fmHeight - fmHeight);
               s.updateImage(0,(fmHeight * (row + 1)) - botOffset,bi.getWidth(null),1);
               s.updateImage(r.x,0,1,bi.getHeight(null) - fmHeight - fmHeight);
               break;
         }
         g2.dispose();
         g2 = getWritingArea(font);
         g2.setPaint(colorBg);

         g2.fill(pArea);
         g2.setColor(colorWhite);

         g2.drawString((row + 1) + "/" + (col + 1)
                        ,(float)pArea.getX(),
                        (float)pArea.getY() + fmHeight);
         s.updateImage(pArea.getBounds());
         g2.dispose();

   }

   public void drawScriptRunning(Color color) {

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

   public void eraseScriptRunning(Color color) {

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
               System.out.println(" bi = null ");
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
         System.out.println("getDrawingarea : " + ie.getMessage());
         return null;
      }
   }

   public synchronized void drawImageBuffer(Graphics2D gg2d,int x, int y, int width, int height) {

       /**
        * @todo this is a hack and should be fixed at the root of the problem
        */
      if (gg2d == null) {
         System.out.println(" we got a null graphic object ");
         return;
      }

      //synchronized (lock) {
         gg2d.drawImage(bi.getSubimage(x,y,width,height),null,x,y);
         // tell waiting threads to wake up
         //lock.notifyAll();
      //}

   }

   public synchronized void drawImageBuffer(Graphics2D gg2d) {

       /**
        * @todo this is a hack and should be fixed at the root of the problem
        */
      if (gg2d == null) {
         System.out.println(" we got a null graphic object ");
         return;
      }

      //synchronized (lock) {
         gg2d.drawImage(bi,null,0,0);
         // tell waiting threads to wake up
         //lock.notifyAll();
      //}


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
               System.out.println( " bi = null wa ");
               lock.wait();
            }
               // we have the lock and state we're seeking

            g2 = bi.createGraphics();

            if (g2 != null) {
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
         System.out.println("getWritingarea : " + ie.getMessage());
         return null;
      }
   }

   public void setStatus(byte attr,byte value,String s,
                           int fmWidth,
                           int fmHeight,
                           LineMetrics lm,
                           Font font,
                           Color colorBg,
                           Color colorRed,
                           Color colorWhite) {

      Graphics2D g2d = getWritingArea(font);

      if (g2d == null)
         return;

      try {
         g2d.setColor(colorBg);
         g2d.fill(sArea);

         float Y = ((int)sArea.getY() + fmHeight)- (lm.getLeading() + lm.getDescent());
         switch (attr) {

            case STATUS_SYSTEM:
               if (value == STATUS_VALUE_ON) {
                  g2d.setColor(colorWhite);

                  if (s != null)
                     g2d.drawString(s,(float)sArea.getX(),Y);
                  else
                     g2d.drawString(xSystem,(float)sArea.getX(),Y);
               }
               break;
            case STATUS_ERROR_CODE:
               if (value == STATUS_VALUE_ON) {
                  g2d.setColor(colorRed);

                  if (s != null)
                     g2d.drawString(s,(float)sArea.getX(),Y);
                  else
                     g2d.drawString(xError,(float)sArea.getX(),Y);

               }
               break;

         }
         g2d.dispose();
      }
      catch (Exception e) {

         System.out.println(" gui graphics setStatus " + e.getMessage());

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
}