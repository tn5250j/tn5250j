/**
 * Title: PrinterThread
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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;

import org.tn5250j.framework.tn5250.Screen5250;

public class PrinterThread extends Thread implements Printable {

   char[] screen;
   char[] screenExtendedAttr;
   char[] screenAttrPlace;
   Screen5250 scr;
   int numCols;
   int numRows;
   Color colorBg;
   Font font;
   SessionPanel session;
   boolean toDefault;
   SessionConfig config;

   public PrinterThread (Screen5250 scr, Font font, int cols, int rows,
                           Color colorBg, boolean toDefaultPrinter, SessionPanel ses) {


      setPriority(1);
      session = ses;
      session.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      config = ses.getSession().getConfiguration();

      int len = scr.getScreenLength();
      screen = new char[len];
      screenExtendedAttr = new char[len];
      screenAttrPlace = new char[len];
      int ret = scr.GetScreen(screen, len, TN5250jConstants.PLANE_TEXT);
      ret = scr.GetScreen(screenExtendedAttr, len, TN5250jConstants.PLANE_EXTENDED);
      ret = scr.GetScreen(screenAttrPlace, len, TN5250jConstants.PLANE_IS_ATTR_PLACE);
      toDefault = toDefaultPrinter;

      numCols = cols;
      numRows = rows;
      this.colorBg = colorBg;
      this.font = font;
   }

   public void run () {
// Toolkit tk = Toolkit.getDefaultToolkit();
//int [][] range = new int[][] {
//new int[] { 1, 1 }
//};
// JobAttributes jobAttributes = new JobAttributes(1, JobAttributes.DefaultSelectionType.ALL, JobAttributes.DestinationType.PRINTER, JobAttributes.DialogType.NONE, "file", 1, 1, JobAttributes.MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_COLLATED_COPIES, range, "HP LaserJet", JobAttributes.SidesType.ONE_SIDED);
//PrintJob job = tk.getPrintJob(null, "Print", jobAttributes, null);
//if (job != null) {
      //--- Create a printerJob object
      PrinterJob printJob = PrinterJob.getPrinterJob ();
      printJob.setJobName("tn5250j");

      // will have to remember this for the next time.
      //   Always set a page format before call setPrintable to
      //   set the orientation.
      PageFormat pf = printJob.defaultPage();
      if (numCols == 132)
         pf.setOrientation(PageFormat.LANDSCAPE);
      else
         pf.setOrientation(PageFormat.PORTRAIT);


      if (numCols != 132) {
         if (config.getStringProperty("print.portWidth").length() != 0 &&
               config.getStringProperty("print.portHeight").length() != 0 &&
               config.getStringProperty("print.portImageWidth").length() != 0 &&
               config.getStringProperty("print.portImageHeight").length() != 0 &&
               config.getStringProperty("print.portImage.X").length() != 0 &&
               config.getStringProperty("print.portImage.Y").length() != 0) {

            Paper paper = pf.getPaper();

            paper.setSize(
               Double.parseDouble(config.getStringProperty("print.portWidth")),
               Double.parseDouble(config.getStringProperty("print.portHeight")));

            paper.setImageableArea(
               Double.parseDouble(config.getStringProperty("print.portImage.X")),
               Double.parseDouble(config.getStringProperty("print.portImage.Y")),
               Double.parseDouble(config.getStringProperty("print.portImageWidth")),
               Double.parseDouble(config.getStringProperty("print.portImageHeight")));
            pf.setPaper(paper);
         }
      }
      else {

         if (config.getStringProperty("print.landWidth").length() != 0 &&
               config.getStringProperty("print.landHeight").length() != 0 &&
               config.getStringProperty("print.landImageWidth").length() != 0 &&
               config.getStringProperty("print.landImageHeight").length() != 0 &&
               config.getStringProperty("print.landImage.X").length() != 0 &&
               config.getStringProperty("print.landImage.Y").length() != 0) {

            Paper paper = pf.getPaper();

            paper.setSize(
               Double.parseDouble(config.getStringProperty("print.landWidth")),
               Double.parseDouble(config.getStringProperty("print.landHeight")));

            paper.setImageableArea(
               Double.parseDouble(config.getStringProperty("print.landImage.X")),
               Double.parseDouble(config.getStringProperty("print.landImage.Y")),
               Double.parseDouble(config.getStringProperty("print.landImageWidth")),
               Double.parseDouble(config.getStringProperty("print.landImageHeight")));
         }
      }

      if (config.getStringProperty("print.font").length() > 0) {

         font = new Font(config.getStringProperty("print.font"),
                           Font.PLAIN, 8);
      }

      //--- Set the printable class to this one since we
      //--- are implementing the Printable interface
      printJob.setPrintable (this,pf);

      // set the cursor back
      session.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));


      //--- Show a print dialog to the user. If the user
      //--- clicks the print button, then print, otherwise
      //--- cancel the print job
      if (printJob.printDialog()) {
         try {
            // we do this because of loosing focus with jdk 1.4.0
            session.requestFocus();
            printJob.print();
         } catch (Exception PrintException) {
            PrintException.printStackTrace();
         }
      }
      else {
         // we do this because of loosing focus with jdk 1.4.0
         session.requestFocus();
      }

      session = null;

      screen = null;
      screenExtendedAttr = null;

   }

   /**
     * Method: print <p>
     *
     * This routine is responsible for rendering a page using
     * the provided parameters. The result will be a screen
     * print of the current screen to the printer graphics object
     *
     * @param g a value of type Graphics
     * @param pageFormat a value of type PageFormat
     * @param page a value of type int
     * @return a value of type int
     */
   public int print (Graphics g, PageFormat pageFormat, int page) {

      Graphics2D g2;

      //--- Validate the page number, we only print the first page
      if (page == 0) {

         //--- Create a graphic2D object and set the default parameters
         g2 = (Graphics2D) g;
         g2.setColor (Color.black);

         //--- Translate the origin to be (0,0)
         g2.translate (pageFormat.getImageableX (), pageFormat.getImageableY ());

         int w = (int)pageFormat.getImageableWidth() / numCols;     // proposed width
         int h = (int)pageFormat.getImageableHeight() / numRows;     // proposed height

         Font k = font;

         LineMetrics l;
         FontRenderContext f = null;

         float j = 1;

         for (; j < 50; j++) {

            // derive the font and obtain the relevent information to compute
            // the width and height
            k = font.deriveFont(j);
            f = new FontRenderContext(k.getTransform(),true,true);
            l = k.getLineMetrics("Wy",f);

            if (
                  (w < (int)k.getStringBounds("W",f).getWidth()) ||
                     h < (int)(k.getStringBounds("y",f).getHeight() +
                           l.getDescent() + l.getLeading())

               )
               break;
         }

         // since we were looking for an overrun of the width or height we need
         // to adjust the font one down to get the last one that fit.
         k = font.deriveFont(--j);
         f = new FontRenderContext(k.getTransform(),true,true);
         l = k.getLineMetrics("Wy",f);

         // set the font of the print job
         g2.setFont(k);

         // get the width and height of the character bounds
         int w1 = (int)k.getStringBounds("W",f).getWidth();
         int h1 = (int)(k.getStringBounds("y",f).getHeight() +
                     l.getDescent() + l.getLeading());
         int x;
         int y;

//         int pos = 0;

         // loop through all the screen characters and print them out.
         for (int m = 0;m < numRows; m++)
            for (int i = 0; i < numCols; i++) {
               x = w1 * i;
               y = h1 * (m + 1);

               // only draw printable characters (in this case >= ' ')
               if (screen[getPos(m,i)] >= ' ' && ((screenExtendedAttr[getPos(m,i)] & TN5250jConstants.EXTENDED_5250_NON_DSP) == 0)) {

                  g2.drawChars(screen, getPos(m,i), 1,x , (int)(y + h1 - (l.getDescent() + l.getLeading())-2));

               }

               // if it is underlined then underline the character
//               if (screen[getPos(m,i)].underLine && !screen[getPos(m,i)].attributePlace)
               if ((screenExtendedAttr[getPos(m,i)] & TN5250jConstants.EXTENDED_5250_UNDERLINE) != 0 &&
                        screenAttrPlace[getPos(m,i)] != 1)
                  g.drawLine(x, (int)(y + (h1 - l.getLeading()-3)), (x + w1), (int)(y + (h1 - l.getLeading())-3));

            }

         return (PAGE_EXISTS);
      }
      else
         return (NO_SUCH_PAGE);
   }

   private int getPos(int row, int col) {

      return (row * numCols) + col;
   }
}