/**
 * $Id$
 * 
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2009
 * Company:
 * @author: maki
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
package org.tn5250j.encoding;

/**
 * Just a workaround to make {@link ToolboxCodePage} methods public available.
 * 
 * @author maki
 */
public class ToolboxCodePageProvider {

	/**
	 * Just a workaround to make {@link ToolboxCodePage#getCodePage(String)} methods public available.
	 * 
	 * @param encoding
	 * @return
	 */
	public final static CodePage getCodePage(String encoding) {
		return ToolboxCodePage.getCodePage(encoding);
	}
	
}
