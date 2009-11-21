package org.tn5250j.gui;

/**
 * Title: TN5250SplashScreen.java
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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Window;
import java.awt.Cursor;
import javax.swing.ImageIcon;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.MediaTracker;
import java.awt.Canvas;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Color;

import org.tn5250j.tools.GUIGraphicsUtils;

/**
 * Uses an Icon or the location of an image to create an application's introductory screen.
 */
public class TN5250jSplashScreen extends Canvas {

   private static final long serialVersionUID = 1L;
protected Window dialog = null;
   protected Frame f = null;
   protected Image image;
   private Image offScreenBuffer;
   private Graphics offScreenBufferGraphics;
   private int steps;
   private int progress;
   private Object lock = new Object();

   /**
    * Creates a splash screen given the location of the image.
    * The image location is the package path of the image and must be in
    * the classpath. For example, if an image was located in
    * /test/examples/image.gif, and the classpath specified contains /test,
    * the constructor should be passed "/examples/image.gif".
    */
   public TN5250jSplashScreen(String image_location) {

      initialize(GUIGraphicsUtils.createImageIcon(image_location));

   }

   /**
    * Creates a splash screen given an Icon image.
    */
   public TN5250jSplashScreen(ImageIcon image) {
      initialize(image);
   }

   /**
    * Creates the Splash screen window and configures it.
    */
   protected void initialize(ImageIcon iimage) {

      image = iimage.getImage();
      // if no image, return
      if (image == null) {
         throw new IllegalArgumentException("Image specified is invalid.");
      }
//      System.out.println(" here in splash ");

      MediaTracker tracker = new MediaTracker(this);
      tracker.addImage(image,0);

      try {
         tracker.waitForAll();
      }
      catch(Exception e) {
         System.out.println(e.getMessage());
      }

      // create dialog window
      f = new Frame();
      dialog = new Window(f);
      dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      Dimension s = new Dimension(image.getWidth(this) + 2,
         image.getHeight(this) + 2);
      setSize(s);
      dialog.setLayout(new BorderLayout());
      dialog.setSize(s);

      dialog.add(this,BorderLayout.CENTER);
      dialog.pack();

      // position splash screen
      Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
      int x = (screen.width - s.width)/2;
      if (x < 0) {
         x = 0;
      }

      int y = (screen.height - s.height)/2;
      if (y < 0) {
         y = 0;
      }

      dialog.setLocation(x, y);
      dialog.validate();

   }

   public void setSteps(int step) {
      steps = step;
   }

   public synchronized void updateProgress(int prog) {

      if (dialog == null || f == null) {
         return;
      }

      progress = prog;
      repaint();

      // wait for it to be painted to ensure progress is updated
      // continuously
      try {
         wait();
      }
      catch(InterruptedException ie) {
         System.out.println(" updateProgress " + ie.getMessage()  );
      }
   }

   public void update(Graphics g) {
      paint(g);
   }

   public synchronized void paint(Graphics g) {

      int inset = 5;
      int height = 14;

      Dimension size = getSize();
//      System.out.println(" here in paint ");
      // create the offscreen buffer if it does not exist
      if(offScreenBuffer == null) {
         offScreenBuffer = createImage(size.width,size.height);
         offScreenBufferGraphics = offScreenBuffer.getGraphics();
      }

      // draw the splash image
      offScreenBufferGraphics.drawImage(image,1,1,this);

      // create a raised border around image
      offScreenBufferGraphics.setColor(new Color(204,204,255));
      offScreenBufferGraphics.draw3DRect(0,0,size.width - 1,size.height - 1,true);

      // fill in progress area
      offScreenBufferGraphics.setColor(new Color(204,204,255).darker());
      offScreenBufferGraphics.fill3DRect(inset - 1,
                              image.getHeight(this) - (height +2),
                              image.getWidth(this) - (inset *2),
                              height + 1,
                              false);

      // draw progress
      offScreenBufferGraphics.setColor(new Color(204,204,255));
      offScreenBufferGraphics.fillRect(inset,
                              image.getHeight(this) - (height +1),
                              ((image.getWidth(this) - (inset * 2)) / steps) * progress,
                              height);

      // now lets show the world
      g.drawImage(offScreenBuffer,0,0,this);

      notify();
//      System.out.println(" here after paint ");
   }

   /**
    * This method will show or hide the splash screen.  Once the splash
    * screen is hidden, the splash screen window will be disposed. This means
    * the splash screen cannot become visible again.
    */
   public void setVisible(boolean show) {
      if (show == true && dialog != null && f != null && !dialog.isVisible()) {
         dialog.setVisible(true);
      }
      else {

         if (dialog != null) {
            updateProgress(steps + 1);
            dialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            dialog.dispose();
         }
         if (f != null) {
            f.dispose();
         }
         dialog = null;
         f = null;
      }
   }

}
