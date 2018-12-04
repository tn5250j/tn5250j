/*
 * Title: ScreenListener.java
 * Copyright:   Copyright (c) 2001
 * Company:
 *
 * @author Kenneth J. Pouncey
 * @version 0.5
 * <p>
 * Description:
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 */
package org.tn5250j.api.screen;

import org.tn5250j.framework.tn5250.Screen5250;

public interface ScreenListener {

  void onScreenChanged(int inUpdate, int startRow, int startCol, int endRow, int endCol);

  void onScreenSizeChanged(int rows, int cols);

  void onKeysSent(final Screen5250 screen, final String keys);

  void onCursorMoved(final Screen5250 screen, final int pos);

}
