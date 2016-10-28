package org.tn5250j.event;

import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.ScreenField;

/*
 * @(#)SessionKeysListener.java
 *
 * Interface to support adding listeners for key events to the 5250 session 
 *
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

public interface SessionKeysListener {

	public void keysSent(final Screen5250 screen, final String keys);
	
	public void fieldStringSet(final Screen5250 screen, final ScreenField field, final String keys);
	
	public void cursorMoved(final Screen5250 screen, final int pos);
}
