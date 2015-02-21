/**
 * Title: KeyStroker
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
 * project instead of KeyEvents. Uses getKeyLocation for 1.4 and greater.
 */
public class KeyStroker {

   protected int keyCode;
   protected boolean isShiftDown;
   protected boolean isControlDown;
   protected boolean isAltDown;
   protected boolean isAltGrDown;
   protected int location;
   protected int hashCode;

   public static final String altSuffix = ".alt2";


   // literals copied from KeyEvent of JDK Version 1.4.0

    /**
     * A constant indicating that the keyLocation is indeterminate
     * or not relevant.
     * KEY_TYPED events do not have a keyLocation; this value
     * is used instead.
     * @since 1.4
     */
    public static final int KEY_LOCATION_UNKNOWN  = 0;

    /**
     * A constant indicating that the key pressed or released
     * is not distinguished as the left or right version of a key,
     * and did not originate on the numeric keypad (or did not
     * originate with a virtual key corresponding to the numeric
     * keypad).
     * @since 1.4
     */
    public static final int KEY_LOCATION_STANDARD = 1;

    /**
     * A constant indicating that the key pressed or released is in
     * the left key location (there is more than one possible location
     * for this key).  Example: the left shift key.
     * @since 1.4
     */
    public static final int KEY_LOCATION_LEFT     = 2;

    /**
     * A constant indicating that the key pressed or released is in
     * the right key location (there is more than one possible location
     * for this key).  Example: the right shift key.
     * @since 1.4
     */
    public static final int KEY_LOCATION_RIGHT    = 3;

    /**
     * A constant indicating that the key event originated on the
     * numeric keypad or with a virtual key corresponding to the
     * numeric keypad.
     * @since 1.4
     */
    public static final int KEY_LOCATION_NUMPAD   = 4;

   public KeyStroker(KeyEvent ke) {


      this.keyCode = ke.getKeyCode();
      this.isShiftDown = ke.isShiftDown();
      this.isControlDown = ke.isControlDown();
      this.isAltDown = ke.isAltDown();
      this.isAltGrDown = ke.isAltGraphDown();
      this.location = ke.getKeyLocation();

      hashCode = keyCode +
                  (isShiftDown ? 1 : 0) +
                  (isControlDown ? 1 : 0) +
                  (isAltDown ? 1 : 0) +
                  (isAltGrDown ? 1 : 0) +
                  location;

   }

   public KeyStroker(KeyEvent ke, boolean isAltGrDown) {


      this.keyCode = ke.getKeyCode();
      this.isShiftDown = ke.isShiftDown();
      this.isControlDown = ke.isControlDown();
      this.isAltDown = ke.isAltDown();
      this.isAltGrDown = isAltGrDown;
      this.location = ke.getKeyLocation();

      hashCode = keyCode +
                  (isShiftDown ? 1 : 0) +
                  (isControlDown ? 1 : 0) +
                  (isAltDown ? 1 : 0) +
                  (isAltGrDown ? 1 : 0) +
                  location;

   }

   public KeyStroker(int keyCode,
                           boolean isShiftDown,
                           boolean isControlDown,
                           boolean isAltDown,
                           boolean isAltGrDown,
                           int location) {

      this.keyCode = keyCode;
      this.isShiftDown = isShiftDown;
      this.isControlDown = isControlDown;
      this.isAltDown = isAltDown;
      this.isAltGrDown = isAltGrDown;
      this.location = location;

      hashCode = keyCode +
                  (isShiftDown ? 1 : 0) +
                  (isControlDown ? 1 : 0) +
                  (isAltDown ? 1 : 0) +
                  (isAltGrDown ? 1 : 0) +
                  location;
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

   public int hashCode() {
      return hashCode;
   }

   public boolean isShiftDown () {

      return isShiftDown;
   }
   public boolean isControlDown () {

      return isControlDown;
   }
   public boolean isAltDown () {

      return isAltDown;
   }

   public boolean isAltGrDown () {

      return isAltGrDown;
   }

   public int getLocation() {
      return location;
   }

   public boolean equals(KeyEvent ke) {

      return (keyCode == ke.getKeyCode() &&
             isShiftDown == ke.isShiftDown() &&
             isControlDown == ke.isControlDown() &&
             isAltDown == ke.isAltDown() &&
             isAltGrDown == ke.isAltGraphDown() &&
             location == ke.getKeyLocation());
   }

   public boolean equals(Object obj) {

     KeyStroker ks = (KeyStroker)obj;

     return ks.keyCode == keyCode &&
            ks.isShiftDown == isShiftDown &&
            ks.isControlDown == isControlDown &&
            ks.isAltDown == isAltDown &&
            ks.isAltGrDown == isAltGrDown &&
            ks.location == location;
   }

   public boolean equals(KeyEvent ke,boolean altGrDown) {

      return (keyCode == ke.getKeyCode() &&
             isShiftDown == ke.isShiftDown() &&
             isControlDown == ke.isControlDown() &&
             isAltDown == ke.isAltDown() &&
             isAltGrDown == altGrDown &&
             location == ke.getKeyLocation());
   }

   public boolean equals(Object obj,boolean altGrDown) {
     KeyStroker ks = (KeyStroker)obj;

     return ks.keyCode == keyCode &&
            ks.isShiftDown == isShiftDown &&
            ks.isControlDown == isControlDown &&
            ks.isAltDown == isAltDown &&
            ks.isAltGrDown == altGrDown &&
            ks.location == location;
   }

   public String toString() {

      return new String(keyCode + "," +
                        (isShiftDown ? "true":"false") + "," +
                        (isControlDown ? "true":"false") + "," +
                        (isAltDown ? "true":"false")  + "," +
                        (isAltGrDown ? "true":"false") + "," +
                        location);
   }

   public String getKeyStrokeDesc() {

      return (isShiftDown ? "Shift + ":"") +
                        (isControlDown ? "Ctrl + ":"") +
                        (isAltDown ? "Alt + ":"") +
                        (isAltGrDown ? "Alt-Gr + ":"") +
                        KeyEvent.getKeyText(keyCode);
   }

   public int getKeyCode() {
      return keyCode;
   }

}

