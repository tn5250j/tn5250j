package org.tn5250j;

import java.awt.print.*;
import java.awt.*;
import javax.swing.*;
import java.awt.font.*;



public class PrinterThread extends Thread implements Printable {

   ScreenChar[] screen;
   int numCols;
   int numRows;
   Color colorBg;
   Font font;

   public PrinterThread (ScreenChar[] screen, Font font, int cols, int rows,
                           Color colorBg) {
      setPriority(1);
      this.screen = screen;
      numCols = cols;
      numRows = rows;
      this.colorBg = colorBg;
      this.font = font;
   }

   public void run () {

      //--- Create a printerJob object
      PrinterJob printJob = PrinterJob.getPrinterJob ();
      printJob.setJobName("tn5250j");

      //--- Set the printable class to this one since we
      //--- are implementing the Printable interface
      printJob.setPrintable (this);


      //--- Show a print dialog to the user. If the user
      //--- clicks the print button, then print, otherwise
      //--- cancel the print job
      if (printJob.printDialog()) {
         try {
            printJob.print();
         } catch (Exception PrintException) {
            PrintException.printStackTrace();
         }
      }

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
         g2.setColor (colorBg);

         //--- Translate the origin to be (0,0)
         g2.translate (pageFormat.getImageableX (), pageFormat.getImageableY ());

         int w = (int)pageFormat.getImageableWidth() / numCols;     // proposed width
         int h = (int)pageFormat.getImageableHeight() / numRows;     // proposed height

         Font k = new Font("Courier New",Font.PLAIN,8);

         LineMetrics l;
         FontRenderContext f = null;

         float j = 1;

         for (; j < 36; j++) {

            // derive the font and obtain the relevent information to compute
            // the width and height
            k = font.deriveFont(j);
            f = new FontRenderContext(k.getTransform(),true,true);
            l = k.getLineMetrics("Wy",f);

            if (
                  (w < (int)k.getStringBounds("W",f).getWidth() + 1) ||
                     h < (int)(k.getStringBounds("g",f).getHeight() +
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
         int w1 = (int)k.getStringBounds("W",f).getWidth() + 1;
         int h1 = (int)(k.getStringBounds("g",f).getHeight() +
                     l.getDescent() + l.getLeading());
         int x;
         int y;

         // loop through all the screen characters and print them out.
         for (int m = 0;m < numRows; m++)
            for (int i = 0; i < numCols; i++) {
               x = w1 * i;
               y = h1 * m;

               // only draw printable characters (in this case >= ' ')
               if (screen[getPos(m,i)].getChar() >= ' ' && !screen[getPos(m,i)].nonDisplay) {

                  g2.drawChars(screen[getPos(m,i)].sChar, 0, 1,x , (int)(y + h1 - (l.getDescent() + l.getLeading())-2));

               }
               // if it is underlined then underline the character
               if (screen[getPos(m,i)].underLine && !screen[getPos(m,i)].attributePlace)
                  g.drawLine(x, (int)(y + (h1 - l.getLeading()-3)), (int)(x + w1), (int)(y + (h1 - l.getLeading())-3));

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