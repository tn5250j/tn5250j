/**
 * $Id: ColumnSeparator.java 1101 2011-01-17 23:20:39Z master_jaf $
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
package org.tn5250j.settings;

/**
 * Types of separator line style
 * 
 * @author master_jaf
 */
public enum ColumnSeparator {

	Hide, Dot, Line, ShortLine;
	
	/**
	 * searches the enumeration for the given name, case insensitive
	 *  
	 * @param name
	 * @return the corresponding enum value OR default value, if name not matches 
	 */
	public static ColumnSeparator getFromName(String name) {
		ColumnSeparator result = DEFAULT;
		if (name == null) return result; 
		for (ColumnSeparator sep : ColumnSeparator.values()) {
			if (name.equalsIgnoreCase(sep.toString())) {
				return sep;
			}
		}
		return result;
	}
	
	/**
	 * default Line
	 */
	public static ColumnSeparator DEFAULT = Hide;
}
