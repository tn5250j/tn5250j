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

import java.awt.*;
import java.awt.event.*;
import javax.swing.SwingUtilities;

public abstract class RubberBand {
	private RubberBandCanvasIF canvas;
	private Point startPoint;
	private Point endPoint;
	private boolean eraseSomething = false;
   private boolean isSomethingBounded = false;

	private class MouseHandler extends MouseAdapter
	{
		public void mousePressed(MouseEvent e) {
         if (!SwingUtilities.isRightMouseButton(e) && !isSomethingBounded)
   			start(canvas.translateStart(e.getPoint()));
//         System.out.println("mouse pressed rb");
		}

		public void mouseReleased(MouseEvent e) {
 			erase();
		}

	}

	private class MouseMotionHandler extends MouseMotionAdapter	{

		public void mouseDragged(MouseEvent e) {

			if(!SwingUtilities.isRightMouseButton(e) && getCanvas().canDrawRubberBand(RubberBand.this)) {

				erase();
				stop(canvas.translateEnd(e.getPoint()));
				draw();
				notifyRubberBandCanvas();
			}
		}

	}

   public RubberBand(RubberBandCanvasIF c) {
      super();
      setCanvas(c);
      getCanvas().addMouseListener(new MouseHandler());
      getCanvas().addMouseMotionListener(new MouseMotionHandler());
   }

   protected void draw() {
//      Graphics g = getCanvas().getGraphics();
      Graphics g = getCanvas().getDrawingGraphics();

      if(g != null){
         try {
            if(getCanvas().canDrawRubberBand(this)) {
               g.setXORMode(canvas.getBackground());
               drawRubberBand(g);
               // we have drawn something, set the flag to indicate this
               setEraseSomething(true);
            }
         }
         finally {
            g.dispose();
         }
      }
   }

   protected abstract void drawBoundingShape(Graphics g,int startx, int starty, int width, int height);

   protected void drawRubberBand(Graphics g) {

      if((getEndPoint().x > getStartPoint().x) && (getEndPoint().y > getStartPoint().y)) {
         drawBoundingShape(g,getStartPoint().x,getStartPoint().y,getEndPoint().x-getStartPoint().x,getEndPoint().y-getStartPoint().y);
      }

      else if((getEndPoint().x < getStartPoint().x) && (getEndPoint().y < getStartPoint().y)) {
         drawBoundingShape(g,getEndPoint().x,getEndPoint().y,getStartPoint().x-getEndPoint().x,getStartPoint().y-getEndPoint().y);
      }

      else if((getEndPoint().x > getStartPoint().x) && (getEndPoint().y < getStartPoint().y))  {
         drawBoundingShape(g,getStartPoint().x,getEndPoint().y,getEndPoint().x-getStartPoint().x,getStartPoint().y-getEndPoint().y);
      }

      else if((getEndPoint().x < getStartPoint().x) && (getEndPoint().y > getStartPoint().y)) {
         drawBoundingShape(g,getEndPoint().x,getStartPoint().y,getStartPoint().x-getEndPoint().x,getEndPoint().y-getStartPoint().y);
      }
      isSomethingBounded = true;

   }

   protected void erase()  {

      if(getEraseSomething()) {
         draw();
         setEraseSomething(false);
      }

   }

   public final RubberBandCanvasIF getCanvas()  {
      return this.canvas;
   }

   protected final Point getEndPoint() {
      if(this.endPoint == null) {
         setEndPoint(new Point(0,0));
      }
      return this.endPoint;
   }

   protected final boolean getEraseSomething() {
      return this.eraseSomething;
   }

   protected final Point getStartPoint() {

      if(this.startPoint == null) {
         setStartPoint(new Point(0,0));
      }
      return this.startPoint;

   }

   protected void notifyRubberBandCanvas() {

      int startX, startY, endX, endY;

      if(getStartPoint().x < getEndPoint().x)  {
         startX = getStartPoint().x;
         endX = getEndPoint().x;
      }
      else {
         startX = getEndPoint().x;
         endX = getStartPoint().x;
      }
      if(getStartPoint().y < getEndPoint().y)  {
         startY = getStartPoint().y;
         endY = getEndPoint().y;
      }
      else {
         startY = getEndPoint().y;
         endY = getStartPoint().y;
      }

      getCanvas().areaBounded(this,startX, startY, endX, endY);

   }

   public final void setCanvas(RubberBandCanvasIF c) {
      this.canvas = c;
   }

   protected final void setEndPoint(Point newValue){
      this.endPoint = newValue;
   }

   protected final void setEraseSomething(boolean newValue)  {
      this.eraseSomething = newValue;
   }

   protected final void setStartPoint(Point newValue) {
      this.startPoint = newValue;
      if (startPoint == null)
         endPoint = null;

   }

   protected void start(Point p) {
      setEndPoint(p);
      setStartPoint(p);
//      System.out.println("start " + startPoint + " end " + endPoint);
   }

   protected void stop(Point p) {

      if(p.x < 0) {
         p.x = 0;
      }

      if(p.y < 0) {
         p.y = 0;
      }

      setEndPoint(p);
//      System.out.println("stop " + startPoint + " end " + endPoint);

   }

   protected void reset() {
      erase();
      setStartPoint(null);
      setEndPoint(null);
      isSomethingBounded = false;

   }

   protected final boolean isAreaSelected() {
      return isSomethingBounded;
   }

}