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

import java.util.StringTokenizer;


/**
 * Simplified rectangle class. Very much similar like java.awt.Rectangle,
 * but we want to decouple the packages ...
 */
public class Rect {

	private static final String SEPARATOR = ",";

	public int x;
	public int y;
	public int height;
	public int width;
	
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Integer.toString(x) + SEPARATOR 
				+ Integer.toString(y) + SEPARATOR
				+ Integer.toString(width) + SEPARATOR
				+ Integer.toString(height);
	}

	/**
	 * @param s
	 * @return
	 */
	public static final Rect fromString(String s) {
		if (s == null) throw new IllegalArgumentException("The string is not allowed to be null");
		Rect r = new Rect();
		StringTokenizer tokenizer = new StringTokenizer(s, SEPARATOR);
		int x = Integer.parseInt(tokenizer.nextToken().trim());
		int y = Integer.parseInt(tokenizer.nextToken().trim());
		int width = Integer.parseInt(tokenizer.nextToken().trim());
		int height = Integer.parseInt(tokenizer.nextToken().trim());
		r.setBounds(x, y, width, height);
		return r;
	}

}
