/**
 * Title: SessionConfigEvent
 * Copyright:   Copyright (c) 2002
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

package org.tn5250j.event;

import java.beans.PropertyChangeEvent;

public class SessionConfigEvent extends PropertyChangeEvent {


   private static final long serialVersionUID = 1L;

/**
   * Constructs a new <code>SessionConfigChangeEvent</code>.
   *
   * @param source  The bean that fired the event.
   * @param propertyName  The programmatic name of the property
   *		that was changed.
   * @param oldValue  The old value of the property.
   * @param newValue  The new value of the property.
   */
   public SessionConfigEvent(Object source, String propertyName,
              Object oldValue, Object newValue) {

      super(source, propertyName, oldValue, newValue);

   }

}
