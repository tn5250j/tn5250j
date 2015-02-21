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

package org.tn5250j.tools;

import java.awt.LayoutManager2;
import java.io.Serializable;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Insets;

/**
 * Fixed Center layout.
 */
public class FixedCenterLayout implements LayoutManager2, Serializable {

    private static final long serialVersionUID = 1L;
	protected int hgap;
    protected Component west;
    protected Component east;
    protected Component center;

    /**
     * Constructs a new layout with no gap between components.
     */
    public FixedCenterLayout() {
        this(0);
    }

    /**
     * Constructs a layout with the specified gaps between components.
     */
    public FixedCenterLayout(int hgap) {
        this.hgap = hgap;
    }

    /**
     * Returns the horizontal gap between components.
     */
    public int getHgap() {
        return hgap;
    }

    /**
     * Sets the horizontal gap between components.
     */
    public void setHgap(int hgap) {
        this.hgap = hgap;
    }

    /**
     * Adds the specified component to the layout.
     */
    public void addLayoutComponent(Component comp, Object constraints) {
        synchronized (comp.getTreeLock()) {
            if ((constraints == null) || (constraints instanceof String)) {
                addLayoutComponent((String)constraints, comp);
            } else {
                throw new IllegalArgumentException("Cannot add to layout: constraint must be a string or null");
            }
        }
    }

    /**
     * We are forced to support it by <code>LayoutManager</code>.
     */
    public void addLayoutComponent(String name, Component comp) {
        synchronized (comp.getTreeLock()) {
            if (name == null || BorderLayout.CENTER.equals(name)) {
                center = comp;
            } else if (BorderLayout.EAST.equals(name)) {
                east = comp;
            } else if (BorderLayout.WEST.equals(name)) {
                west = comp;
            } else {
                throw new IllegalArgumentException("cannot add to layout: unknown constraint: " + name);
            }
        }
    }

    /**
     * Removes the specified component from this layout.
     */
    public void removeLayoutComponent(Component component) {
        synchronized (component.getTreeLock()) {
            if (component == center) {
                center = null;
            } else if (component == east) {
                east = null;
            } else if (component == west) {
                west = null;
            }
        }
    }

    /**
     * Determines the minimum size of the target.
     */
    public Dimension minimumLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {
            Dimension d = new Dimension(0, 0);

            addMinimumSize(d, east);
            addMinimumSize(d, west);
            addMinimumSize(d, center);

            Insets insets = target.getInsets();
            d.width += insets.left + insets.right;
            d.height += insets.top + insets.bottom;

            return d;
        }
    }

    /**
     * Determines the preferred size of the target.
     */
    public Dimension preferredLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {
            Dimension d = new Dimension(0, 0);

            addPreferredSize(d, east);
            addPreferredSize(d, west);
            addPreferredSize(d, center);

            Insets insets = target.getInsets();
            d.width += insets.left + insets.right;
            d.height += insets.top + insets.bottom;

            return d;
        }
    }

    /**
     * Determines the maximum size of the target.
     */
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Returns the alignment along the x axis.
     */
    public float getLayoutAlignmentX(Container parent) {
        return 0.5f;
    }

    /**
     * Returns the alignment along the y axis.
     */
    public float getLayoutAlignmentY(Container parent) {
        return 0.5f;
    }

    /**
     * Invalidates the layout, indicating that if the layout manager
     * has cached information it should be discarded.
     */
    public void invalidateLayout(Container target) {
    }

    /**
     * Lays out the target argument using this layout.
     */
    public void layoutContainer(Container target) {
        synchronized (target.getTreeLock()) {
            Insets insets = target.getInsets();
            int top = insets.top;
            //	int bottom = target.getHeight() - insets.bottom;
            int bottom = target.getBounds().height - insets.bottom;
            int left = insets.left;
            //	int right = target.getWidth() - insets.right;
            int right = target.getBounds().width - insets.right;

            int leftCenter = (right-left)/2;
            int rightCenter = leftCenter;

            if (center != null) {
                Dimension d = center.getPreferredSize();
                leftCenter = (right-left-d.width)/2;
                rightCenter = leftCenter+d.width;
                center.setBounds(leftCenter, top, d.width, bottom-top);
            }
            if (west != null) {
                west.setBounds(left, top, leftCenter-left-hgap, bottom-top);
            }
            if (east != null) {
                east.setBounds(rightCenter+hgap, top, right-rightCenter-2*hgap, bottom-top);
            }
        }
    }

    private void addMinimumSize(Dimension d, Component c) {
        if (c != null) {
            addSize(d, c.getMinimumSize());
        }
    }

    private void addPreferredSize(Dimension d, Component c) {
        if (c != null) {
            addSize(d, c.getPreferredSize());
        }
    }

    private void addSize(Dimension d, Dimension size) {
        d.width += size.width + hgap;
        d.height = Math.max(size.height, d.height);
    }

    /**
     * Returns a string representation of the state of this layout.
     */
    public String toString() {
        return getClass().getName() + "[hgap=" + hgap + "]";

    }

} // FixedCenterLayout
