/**
 * Title: KeyGetterInterface
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
 * MERCHANTABILreITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 */
package org.tn5250j.keyboard.configure;

import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import javax.swing.JLabel;
import javax.swing.JDialog;

import org.tn5250j.tools.system.OperatingSystem;

/**
 * This class is not really an interface but an class that extends label so that
 * we can display text as well as capture the key stroke(s) to assign to keys.
 *
 * The extending classes must override the key capture methods:
 *
 *    abstract private void processVTKeyPressed(KeyEvent e);
 *    abstract private void processVTKeyTyped(KeyEvent e);
 *    abstract private void processVTKeyReleased(KeyEvent e);
 *
 */
public abstract class KeyGetterInterface extends JLabel {

   private static final long serialVersionUID = 1L;
KeyEvent keyevent;
   boolean isAltGr;
   boolean isLinux;
   JDialog dialog;

   public KeyGetterInterface() {
      super();

      if (OperatingSystem.isUnix() && !OperatingSystem.isMacOS()) {
         isLinux = true;
      }

      addKeyListener(new KeyAdapter() {

            public void keyTyped(KeyEvent e) {
                  processVTKeyTyped(e);

            }

            public void keyPressed(KeyEvent ke) {

               processVTKeyPressed(ke);
            }

            public void keyReleased(KeyEvent e) {

               processVTKeyReleased(e);

            }

      });

   }

   public void setDialog(JDialog dialog) {

      this.dialog = dialog;

   }

   public boolean isFocusTraversable () {
      return true;
   }

   /**
    * Override to inform focus manager that component is managing focus changes.
    * This is to capture the tab and shift+tab keys.
    */
   public boolean isManagingFocus() {
      return true;
   }

   abstract void processVTKeyPressed(KeyEvent e);

   abstract void processVTKeyTyped(KeyEvent e);

   abstract void processVTKeyReleased(KeyEvent e);
}
