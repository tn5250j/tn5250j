/**
 *
 * <p>Title: ScreenOIAListener</p>
 * <p>Description: Main interface to draw the graphical image of the screen</p>
 * <p>Copyright: Copyright (c) 2000 - 2002</p>
 * <p>
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
 * </p>
 * @author Kenneth J. Pouncey
 * @version 0.5
 */

package org.tn5250j.event;

import org.tn5250j.ScreenOIA;

public interface ScreenOIAListener {


   public void onOIAChanged(ScreenOIA oia);

}