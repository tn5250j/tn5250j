/**
 * Title: KeyStroker14
 * Copyright:   Copyright (c) 2001, 2002, 2003
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.1
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
package org.tn5250j.keyboard;

import java.awt.event.KeyEvent;

/**
 * This class is basically a wrapper for KeyEvent that is used internally to the
 * project instead of KeyEvents.  Uses getKeyLocation for 1.4 and greater.
 *
 */
public class KeyStroker14 extends KeyStroker {


   public KeyStroker14(KeyEvent ke) {

      super(ke);

      location = ke.getKeyLocation();
      hashCode = keyCode +
                  (isShiftDown ? 1 : 0) +
                  (isControlDown ? 1 : 0) +
                  (isAltDown ? 1 : 0) +
                  (isAltGrDown ? 1 : 0) +
                  location;

   }

   public KeyStroker14(KeyEvent ke, boolean isAltGrDown) {

      super(ke,isAltGrDown);
      this.location = ke.getKeyLocation();

      hashCode = keyCode +
                  (isShiftDown ? 1 : 0) +
                  (isControlDown ? 1 : 0) +
                  (isAltDown ? 1 : 0) +
                  (isAltGrDown ? 1 : 0) +
                  location;

   }

   public KeyStroker14(int keyCode,
                           boolean isShiftDown,
                           boolean isControlDown,
                           boolean isAltDown,
                           boolean isAltGrDown,
                           int location) {

      super(keyCode, isShiftDown,isControlDown,isAltDown,isAltGrDown,location);
   }

   public void setAttributes(KeyEvent ke,boolean isAltGr) {

      keyCode = ke.getKeyCode();
      isShiftDown = ke.isShiftDown();
      isControlDown = ke.isControlDown();
      isAltDown = ke.isAltDown();
      isAltGrDown = isAltGr;
      location = ke.getKeyLocation();

      hashCode = keyCode +
                  (isShiftDown ? 1 : 0) +
                  (isControlDown ? 1 : 0) +
                  (isAltDown ? 1 : 0) +
                  (isAltGrDown ? 1 : 0) +
                  location;
   }

   public boolean equals(KeyEvent ke) {

      return (keyCode == ke.getKeyCode() &&
             isShiftDown == ke.isShiftDown() &&
             isControlDown == ke.isControlDown() &&
             isAltDown == ke.isAltDown() &&
             isAltGrDown == ke.isAltGraphDown() &&
             location == ke.getKeyLocation());
   }

   public boolean equals(KeyEvent ke,boolean altGrDown) {

      return (keyCode == ke.getKeyCode() &&
             isShiftDown == ke.isShiftDown() &&
             isControlDown == ke.isControlDown() &&
             isAltDown == ke.isAltDown() &&
             isAltGrDown == altGrDown &&
             location == ke.getKeyLocation());
   }

}

