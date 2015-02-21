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
import java.awt.GridLayout;
import java.awt.Container;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;

/**
 * <code>ENHGridLayout</code> is an improved subclass of <code>GridLayout</code>.
 * It lays out a grid of rows and columns based on the attributes of the
 * individual rows and columns. <code>ENHGridLayout</code>
 * uses the widest element in a column to set the width of that
 * column, and the tallest element in a row to set the height of
 * that row.
 */
public class ENHGridLayout extends GridLayout {

   private static final long serialVersionUID = 1L;

/** The horiztonal gap between items. */
   protected int hgap;

   /** The vertical gap between items. */
   protected int vgap;

   /** The number of rows in the layout, as set by the user.
    * This number may not correspond exactly to the number of
    * rows in the layout.
    */
   protected int rows;

   /** The number of columns in the layout, as set by the user.
    * This number may not correspond exactly to the number of
    * columns in the layout.
    */
   protected int cols;

   /** Array of row heights.
    * It is accurate only after a call to getGridSizes()
    */
   protected int row_heights[] = new int[0];

   /** Array of column widths.
    * It is accurate only after a call to getGridSizes()
    */
   protected int col_widths[] = new int[0];

   public final static int VARIABLE = 0;

   /**
    * Creates a grid layout with the specified number of rows and columns.
    * @param rows the number of rows in the layout
    * @param cols the number of columns in the layout
    */
   public ENHGridLayout(int rows, int cols) {
      this(rows, cols, 0, 0);
   }

   /**
    * Creates a grid layout with the specified rows, columns,
    * horizontal gap, and vertical gap.
    * @param rows the rows; VARIABLE (0) means 'any number.'
    * @param cols the columns; VARIABLE (0) means 'any number.'
    * Only one of 'rows' and 'cols' can be VARIABLE, not both.
    * @param hgap the horizontal gap variable
    * @param vgap the vertical gap variable
    * @exception IllegalArgumentException If the rows and columns are invalid.
    */
   public ENHGridLayout(int rows, int cols, int hgap, int vgap) {
      super(rows, cols, hgap, vgap);
      this.rows = rows;
      this.cols = cols;
      this.hgap = hgap;
      this.vgap = vgap;
   }

   /**
    * Traverses the children and determines row heights and column widths.
    * @param parent the component which needs to be laid out
    * @param min if true, the minimum size is used. Otherwise, the preferred size
    * is used.
    */
   protected void getGridSizes(Container parent, boolean min) {
      int ncomponents = parent.getComponentCount();
      if (ncomponents == 0) return;
      int nrows = rows, ncols = cols;
      if (nrows > 0)
          ncols = (ncomponents + nrows - 1) / nrows;
      else
          nrows = (ncomponents + ncols - 1) / ncols;

      row_heights = new int[nrows];
      col_widths = new int[ncols];

      for (int i = 0; i < ncomponents; i++) {
          Component comp = parent.getComponent(i);
         Dimension d = min ? comp.getMinimumSize() :
                      comp.getPreferredSize();

         int row = i / ncols;
         if (d.height > row_heights[row])
            row_heights[row] = d.height;

         int col = i % ncols;
         if (d.width > col_widths[col])
            col_widths[col] = d.width;
      }
   }

   /**
    * Sums the items of an array
    */
   final int sum(int[] array) {
      if (array == null) return 0;
      int s = 0;
      for (int i = 0; i < array.length; i++)
         s += array[i];
      return s;
   }

   /**
    * Calculates the preferred size for this layout.
    * @param parent the component which needs to be laid out
    */
   public Dimension preferredLayoutSize(Container parent) {

      Insets insets = parent.getInsets();
      getGridSizes(parent, false);
      return new Dimension(insets.left + insets.right + sum(col_widths)
                      + (col_widths.length+1)*hgap,
                      insets.top + insets.bottom + sum(row_heights)
                      + (row_heights.length+1)*vgap);
   }

   /**
    * Returns the minimum dimensions needed to layout the components
    * contained in the specified panel.
    * @param parent the component which needs to be laid out
    */
   public Dimension minimumLayoutSize(Container parent) {

      Insets insets = parent.getInsets();
      getGridSizes(parent, true);
      return new Dimension(insets.left + insets.right + sum(col_widths)
                      + (col_widths.length+1)*hgap,
                      insets.top + insets.bottom + sum(row_heights)
                      + (row_heights.length+1)*vgap);
   }

   /**
    * Positions the component.
    * @param pos the component's index in its parents child list
    * @param row,col component's position
    */
   protected void setBounds(int pos, int row, int col,
                     Component comp, int x, int y, int w, int h) {

      comp.setBounds(x, y, w, h);

   }

   /**
    * Performs the layout of the children.
    * It calculates the number of actual rows and columns
    * based on the user's settings, retrieves row height and column
    * width information, then moves all the children to the appropriate places.
    * @param parent the specified component being laid out
    * @see #reshape
    */
   public void layoutContainer(Container parent) {
      int ncomponents = parent.getComponentCount();
      if (ncomponents == 0) {
         return;
      }
      Insets insets = parent.getInsets();

      getGridSizes(parent, false);
      int nrows = rows, ncols = cols;

      if (nrows > 0)
          ncols = (ncomponents + nrows - 1) / nrows;
      else
          nrows = (ncomponents + ncols - 1) / ncols;

      Dimension psize = parent.getSize();
      for (int col = 0, x = insets.left+hgap; col < ncols; col++) {
          for (int row = 0, y = insets.top+vgap; row < nrows; row++) {
            int i = row*ncols + col;
            if (i < ncomponents) {
               int w = Math.max(0, Math.min(col_widths[col],
                                     psize.width-insets.right-x));
               int h = Math.max(0, Math.min(row_heights[row],
                                     psize.height-insets.bottom-y));
               setBounds(i, row, col, parent.getComponent(i), x, y, w, h);
            }
            y += row_heights[row] + vgap;
          }
         x += col_widths[col] + hgap;
      }
   }

}




