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
package org.tn5250j;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.tn5250j.framework.tn5250.Screen5250;

/**
 * Session Scroller to allow the use of the mouse wheel to move the list on the
 * screen up and down.
 */
public class SessionScroller implements MouseWheelListener {

	private Screen5250 screen = null;;

	public void addMouseWheelListener(SessionPanel ses) {
		this.screen = ses.getScreen();
		ses.addMouseWheelListener(this);
	}

	public void removeMouseWheelListener(SessionPanel ses) {
		this.screen = null;
		ses.removeMouseWheelListener(this);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (this.screen != null) {
			int notches = e.getWheelRotation();
			if (notches < 0) {
				screen.sendKeys(TN5250jConstants.MNEMONIC_PAGE_UP);
			} else {
				screen.sendKeys(TN5250jConstants.MNEMONIC_PAGE_DOWN);
			}
		}
	}

}
