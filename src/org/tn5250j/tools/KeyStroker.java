package org.tn5250j.tools;

/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
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
import java.awt.event.KeyEvent;

public class KeyStroker {

   private int keyCode;
   private boolean isShiftDown;
   private boolean isControlDown;
   private boolean isAltDown;
   private boolean isAltGrDown;
   private int hashCode;

   public KeyStroker(int keyCode,
                           boolean isShiftDown,
                           boolean isControlDown,
                           boolean isAltDown,
                           boolean isAltGrDown) {

      this.keyCode = keyCode;
      this.isShiftDown = isShiftDown;
      this.isControlDown = isControlDown;
      this.isAltDown = isAltDown;
      this.isAltGrDown = isAltGrDown;

      hashCode = keyCode +
                  (isShiftDown ? 1 : 0) +
                  (isControlDown ? 1 : 0) +
                  (isAltDown ? 1 : 0) +
                  (isAltGrDown ? 1 : 0);
   }

   public final void setAttributes(int keyCode,
                           boolean isShiftDown,
                           boolean isControlDown,
                           boolean isAltDown,
                           boolean isAltGrDown) {

      this.keyCode = keyCode;
      this.isShiftDown = isShiftDown;
      this.isControlDown = isControlDown;
      this.isAltDown = isAltDown;
      this.isAltGrDown = isAltGrDown;
      hashCode = keyCode +
                  (isShiftDown ? 1 : 0) +
                  (isControlDown ? 1 : 0) +
                  (isAltDown ? 1 : 0);

   }

   public final void setAttributes(KeyEvent ke) {

      keyCode = ke.getKeyCode();
      isShiftDown = ke.isShiftDown();
      isControlDown = ke.isControlDown();
      isAltDown = ke.isAltDown();
      hashCode = keyCode +
                  (isShiftDown ? 1 : 0) +
                  (isControlDown ? 1 : 0) +
                  (isAltDown ? 1 : 0) +
                  (isAltGrDown ? 1 : 0);
   }

   public final void setAttributes(KeyEvent ke,boolean isAltGr) {

      keyCode = ke.getKeyCode();
      isShiftDown = ke.isShiftDown();
      isControlDown = ke.isControlDown();
      isAltDown = ke.isAltDown();
      isAltGrDown = isAltGr;
      hashCode = keyCode +
                  (isShiftDown ? 1 : 0) +
                  (isControlDown ? 1 : 0) +
                  (isAltDown ? 1 : 0) +
                  (isAltGrDown ? 1 : 0);
   }

   public final int hashCode() {
      return hashCode;
   }

   public final boolean isShiftDown () {

      return isShiftDown;
   }
   public final boolean isControlDown () {

      return isControlDown;
   }
   public final boolean isAltDown () {

      return isAltDown;
   }
   public final boolean isAltGrDown () {

      return isAltGrDown;
   }

   public boolean equals(KeyEvent ke) {

      return (keyCode == ke.getKeyCode() &&
             isShiftDown == ke.isShiftDown() &&
             isControlDown == ke.isControlDown() &&
             isAltDown == ke.isAltDown() &&
             isAltGrDown == ke.isAltGraphDown());
   }

   public boolean equals(Object obj) {
     KeyStroker ks = (KeyStroker)obj;

     return ks.keyCode == keyCode &&
            ks.isShiftDown == isShiftDown &&
            ks.isControlDown == isControlDown &&
            ks.isAltDown == isAltDown &&
            ks.isAltGrDown == isAltGrDown;
   }

   public boolean equals(KeyEvent ke,boolean altGrDown) {

      return (keyCode == ke.getKeyCode() &&
             isShiftDown == ke.isShiftDown() &&
             isControlDown == ke.isControlDown() &&
             isAltDown == ke.isAltDown() &&
             isAltGrDown == altGrDown);
   }

   public boolean equals(Object obj,boolean altGrDown) {
     KeyStroker ks = (KeyStroker)obj;

     return ks.keyCode == keyCode &&
            ks.isShiftDown == isShiftDown &&
            ks.isControlDown == isControlDown &&
            ks.isAltDown == isAltDown &&
            ks.isAltGrDown == altGrDown;
   }

   public String toString() {
      return new String(keyCode + "," +
                        (isShiftDown ? "true":"false") + "," +
                        (isControlDown ? "true":"false") + "," +
                        (isAltDown ? "true":"false")  + "," +
                        (isAltGrDown ? "true":"false"));
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

