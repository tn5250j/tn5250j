/*
 * @(#)TabClosedListener.java 
 * @author Kenneth J. Pouncey
 * Copyright: Copyright (c) 2001
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this software; see the file COPYING. If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *  
 */
package org.tn5250j.event;

public interface TabClosedListener {

   /**
    * Will be called, when a tab should be closed.
    * 
    * @param tabToBeClosed
    */
   public void onTabClosed(int tabToBeClosed);

}