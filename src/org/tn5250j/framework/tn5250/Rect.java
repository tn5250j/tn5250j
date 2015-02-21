/**
 * $Id$
 * 
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2009
 * Company:
 * @author: master_jaf
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
package org.tn5250j.framework.tn5250;


/**
 * Simplified rectangle class. Very much similar like java.awt.Rectangle,
 * but we want to decouple the packages ...
 */
public class Rect {

	/* default */ int x;
	/* default */ int y;
	/* default */ int height;
	/* default */ int width;
	
	/**
	 * @param rect
	 */
	public void setBounds(Rect rect) {
		setBounds(rect.x, rect.y, rect.width, rect.height);
	}
	
    /**
     * @param x the new X coordinate for the upper-left corner of this rectangle
     * @param y the new Y coordinate for the upper-left corner of this rectangle
     * @param width the new width for this rectangle
     * @param height the new height for this rectangle
     */
	public void setBounds(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

}
