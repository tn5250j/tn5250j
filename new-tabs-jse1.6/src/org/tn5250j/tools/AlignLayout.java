package org.tn5250j.tools;
/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.1
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

import java.awt.Container;
import java.awt.Component;
import java.util.Hashtable;

public class AlignLayout extends ENHGridLayout {

   private static final long serialVersionUID = 1L;
protected Hashtable alignment;
   protected Hashtable resize_width, resize_height;
   public static final int TOP = 1;
   public static final int MIDDLE = 4;
   public static final int BOTTOM = 5;

   /**
    * Creates an aligner layout with 2 columns, a variable number of rows,
    * and a gap of 5 pixels.
    */
   public AlignLayout() {
      this(2, 5, 5);
   }

   /**
    * Creates an aligner layout with the specified number of columns and gaps.
    * @param cols the number of columns (should be a multiple of 2)
    * @param hgap the horizontal gap variable
    * @param vgap the vertical gap variable
    * @exception IllegalArgumentException If the rows and columns are invalid.
    */
   public AlignLayout(int cols, int hgap, int vgap) {
      super(VARIABLE, cols, hgap, vgap);
   }

   private int get(Hashtable table, Component comp, int def) {
      Object v = (table != null) ? table.get(""+comp.hashCode()) : null;
      return (v != null) ? ((Integer)v).intValue() : def;
   }

   private boolean get(Hashtable table, Component comp, boolean def) {
      Object v = (table != null) ? table.get(""+comp.hashCode()) : null;
      return (v != null) ? ((Boolean)v).booleanValue() : def;
   }

   /**
    * Gets the vertical position of a label relative to its control.
    * @see #setLabelVerticalAlignment
    */
   public int getLabelVerticalAlignment(Component child) {
      return get(alignment, child, MIDDLE);
   }

   /**
    * Sets the vertical position of a label relative to its control.
    * @param align TOP, MIDDLE (default), or BOTTOM.
    * @exception IllegalArgumentException If an invalid value is set
    */
   public void setLabelVerticalAlignment(Component child, int align) {
      if (alignment == null) alignment = new Hashtable(5);
      alignment.put(""+child.hashCode(), new Integer(align));
   }

   /** Gets the component's RezizeWidth value.
    * @see #setResizeWidth
    */
   public boolean getResizeWidth(Component child) {
      return get(resize_width, child, false);
   }

   /** Sets whether the control should be resized horizontally to its parent's
    * right edge if it is in the last column (default: false).
    */
   public void setResizeWidth(Component child, boolean v) {
      if (resize_width == null) resize_width = new Hashtable(5);
      resize_width.put(""+child.hashCode(), new Boolean(v));
   }

   /** Gets the component's RezizeHeight value.
    * @see #setResizeHeight
    */
   public boolean getResizeHeight(Component child) {
      return get(resize_height, child, false);
   }

   /** Sets whether the control should be resized vertically to the height of the
    * largest component in its row (default: false). This value is ignored for
    * labels (the components in odd columns).
    */
   public void setResizeHeight(Component child, boolean v) {
      if (resize_height == null) resize_height = new Hashtable(5);
      resize_height.put(""+child.hashCode(), new Boolean(v));
   }

   protected boolean isLabel(int col) { return (col % 2) == 0; }

   /**
    * Positions the component.
    * @param pos the component's index in its parents child list
    * @param row,col component's position
    */
   protected void setBounds(int pos, int row, int col, Component comp,
                     int x, int y, int col_width, int row_height) {

      int comp_w = col_width, comp_h = row_height;

      if (isLabel(col) || !getResizeHeight(comp)) {
         comp_h = comp.getPreferredSize().height;
      }

      /* Resize a control to its parent's right edge if its resizeWidth value
       * is true, and it is in the last column
       */
      if (!isLabel(col)) {
         if (col < col_widths.length-1) {
         }
         else if (getResizeWidth(comp)) {
            Container parent = comp.getParent();
            comp_w = parent.getSize().width - parent.getInsets().right - x;
         }
         else {
            comp_w = comp.getPreferredSize().width;
         }

         comp.setBounds(x, y, comp_w, comp_h);

         return;
      }

      int control_h = row_height;
      if (pos < comp.getParent().getComponentCount()-1) {
         Component control = comp.getParent().getComponents()[pos+1];
         if (control != null && !getResizeHeight(control)) {
            control_h = control.getPreferredSize().height;
         }
      }

      switch (getLabelVerticalAlignment(comp)) {
      case MIDDLE:
         y += (control_h - comp_h) / 2;
         break;
      case BOTTOM:
         y += control_h - comp_h;
         break;
      }
      comp.setBounds(x, y, comp_w, comp_h);
   }

}
