/*
 * @(#)TN5250jTabbedPane.java Copyright: Copyright (c) 2001
 * @author Kenneth J. Pouncey
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this software; see the file COPYING. If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *  
 */
package org.tn5250j.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.plaf.basic.BasicTabbedPaneUI;

import org.tn5250j.event.TabClosedListener;

/**
 * 
 * This class provides an instance of the a JTabbedPane that paints a closable
 * (X) on each tab when the cursor enters a tab.
 * 
 */
public class TN5250jTabbedPane extends JTabbedPane implements MouseListener,
																					MouseMotionListener{

   // the currectly drawn tab that an X is drawn on
   int tabNumber;
   // the region that the X is drawn in the tab
   Rectangle closeRect;
   // closable tab listener.
   private Vector closeListeners;   
   
   public TN5250jTabbedPane() {
      super();
      this.setUI(new MyBasicTabbedPaneUI());
      addMouseListener(this);
      addMouseMotionListener(this);
   }

   public void mouseClicked(MouseEvent e) {
      int tabNumber = getUI().tabForCoordinate(this, e.getX(), e.getY());
      if (tabNumber < 0)
         return;
      if (closeRect.contains(e.getX(), e.getY())) {
         //the tab is being closed
         fireTabClosed(tabNumber);
         
      }
   }

   public void mouseDragged(MouseEvent e) {
      
   }
   
   public void mouseMoved(MouseEvent e) {
      
      int stabNumber = getUI().tabForCoordinate(this, e.getX(), e.getY());
      
      if (stabNumber >= 0) {
         
         if (stabNumber != tabNumber) {
            
            repaint();
         }
         
         tabNumber = stabNumber;

         Rectangle tabRect = getUI().getTabBounds(this,tabNumber);
      
//         System.out.println(tabNumber + " " + tabRect.x
//               								+ " " + tabRect.y
//               								+ " " + tabRect.width
//               								+ " " + tabRect.height);
         
         Graphics g = this.getGraphics();
         closeRect = new Rectangle(tabRect.x + tabRect.width - 18,tabRect.y,16,16);

         if (closeRect.contains(e.getX(), e.getY()))
            paintClosableX(g,tabRect.x + tabRect.width - 18,tabRect.y,Color.red);
         else
            paintClosableX(g,tabRect.x + tabRect.width - 18,tabRect.y,Color.black);
         
      }
      
      else {
         repaint();
      }
   }

   private void paintClosableX(Graphics g, int x, int y, Color x_color) {
      
      int x_pos = x;
      int y_pos = y;

      Color col = g.getColor();

      g.setColor(x_color);
      
      int y_p = y + 2;
      g.drawLine(x + 1, y_p, x + 12, y_p);
      g.drawLine(x + 1, y_p + 13, x + 12, y_p + 13);
      g.drawLine(x, y_p + 1, x, y_p + 12);
      g.drawLine(x + 13, y_p + 1, x + 13, y_p + 12);
      g.drawLine(x + 3, y_p + 3, x + 10, y_p + 10);
      g.drawLine(x + 3, y_p + 4, x + 9, y_p + 10);
      g.drawLine(x + 4, y_p + 3, x + 10, y_p + 9);
      g.drawLine(x + 10, y_p + 3, x + 3, y_p + 10);
      g.drawLine(x + 10, y_p + 4, x + 4, y_p + 10);
      g.drawLine(x + 9, y_p + 3, x + 3, y_p + 9);
      g.setColor(col);
      
   }
   public void mouseEntered(MouseEvent e) {

      
   }

   public void mouseExited(MouseEvent e) {
      tabNumber = -1;
      repaint();

   }

   public void mousePressed(MouseEvent e) {
   }

   public void mouseReleased(MouseEvent e) {

   }

   /**
    * Add a TabClosedListener to the listener list.
    *
    * @param listener  The TabClosedListener to be added
    */
   public synchronized void addtabCloseListener(TabClosedListener listener) {

      if (closeListeners == null) {
          closeListeners = new java.util.Vector(3);
      }
      closeListeners.addElement(listener);

   }

   /**
    * Remove a TabClosedListener from the listener list.
    *
    * @param listener  The TabClosedListener to be removed
    */
   public synchronized void removeTabCloseListener(TabClosedListener listener) {
      if (closeListeners == null) {
          return;
      }
      closeListeners.removeElement(listener);

   }
   
   public void fireTabClosed(int tabToClose) {
      if (closeListeners != null) {
         int size = closeListeners.size();
         for (int i = 0; i < size; i++) {
            TabClosedListener target =
                    (TabClosedListener)closeListeners.elementAt(i);
            target.tabClosed(tabToClose);
         }
      }
      
      
   }
   
   /**
    * 
    * This makes sure the icon and label are left justified in the tabs
    */
   class MyBasicTabbedPaneUI extends BasicTabbedPaneUI {

      // not supported right now but maybe in the future.  Right now text is
      // on the right and the icon is on the left with both left justified in
      // in the tab.  Not changeble.
      
      private int horizontalTextPosition = SwingUtilities.RIGHT;

      public MyBasicTabbedPaneUI() {
      }

      public MyBasicTabbedPaneUI(int horTextPosition) {
         horizontalTextPosition = horTextPosition;
      }

      protected void layoutLabel(int tabPlacement, FontMetrics metrics,
            int tabIndex, String title, Icon icon, Rectangle tabRect,
            Rectangle iconRect, Rectangle textRect, boolean isSelected) {

         textRect.x = 0; 
         textRect.y = 0;
         iconRect.x = 0; 
         iconRect.y = 0;
         
         SwingUtilities.layoutCompoundLabel((JComponent) tabPane, metrics,
               title, icon, SwingUtilities.CENTER, SwingUtilities.LEFT,
               SwingUtilities.CENTER, horizontalTextPosition, tabRect,
               iconRect, textRect, textIconGap + 2);

      }
   }

}