/**
 * Title: KeyGetter13
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

/**
 * This class extends label so that we can display text as well as capture
 * the key stroke(s) to assign to keys.  This is version 1.3 of JDK
 */
public class KeyGetter13 extends KeyGetterInterface {

   public KeyGetter13() {
      super();
      System.out.println(" keygetter initialized ");
   }

   /*
    * We have to jump through some hoops to avoid
    * trying to print non-printing characters
    * such as Shift.  (Not only do they not print,
    * but if you put them in a String, the characters
    * afterward won't show up in the text area.)
    */
   private void displayInfo(KeyEvent e){

      String charString, keyCodeString, modString, tmpString,isString,
               locString,typeStr;

      switch(e.getID()) {
         case KeyEvent.KEY_PRESSED:
            typeStr = "KEY_PRESSED";
            break;
         case KeyEvent.KEY_RELEASED:
            typeStr = "KEY_RELEASED";
            break;
         case KeyEvent.KEY_TYPED:
            typeStr = "KEY_TYPED";
            break;
         default:
            typeStr = "unknown type";
      }

      char c = e.getKeyChar();
      int keyCode = e.getKeyCode();
      int modifiers = e.getModifiers();

      if (Character.isISOControl(c)) {
         charString = "key character = "
                    + "(an unprintable control character)";
      } else {
         charString = "key character = '"
                    + c + "'";
      }

      keyCodeString = "key code = " + keyCode
                     + " ("
                     + KeyEvent.getKeyText(keyCode)
                     + ")";
      if(keyCode == KeyEvent.VK_PREVIOUS_CANDIDATE) {

         keyCodeString += " previous candidate ";

      }

      if(keyCode == KeyEvent.VK_DEAD_ABOVEDOT ||
            keyCode == KeyEvent.VK_DEAD_ABOVERING ||
            keyCode == KeyEvent.VK_DEAD_ACUTE ||
            keyCode == KeyEvent.VK_DEAD_BREVE ||
            keyCode == KeyEvent.VK_DEAD_CIRCUMFLEX

         ) {

         keyCodeString += " dead key ";

      }

      modString = "modifiers = " + modifiers;
      tmpString = KeyEvent.getKeyModifiersText(modifiers);
      if (tmpString.length() > 0) {
         modString += " (" + tmpString + ")";
      } else {
         modString += " (no modifiers)";
      }

      locString = "location = (UNKNOWN)";


      isString = "isKeys = isActionKey (" + e.isActionKey() + ")" +
                      " isAltDown (" + e.isAltDown() + ")" +
                      " isAltGraphDown (" + e.isAltGraphDown() + ")" +
                      " isAltGraphDownLinux (" + isAltGr + ")" +
                      " isControlDown (" + e.isControlDown() + ")" +
                      " isMetaDown (" + e.isMetaDown() + ")" +
                      " isShiftDown (" + e.isShiftDown() + ")";


      String newline = "\n";
      System.out.println(typeStr + newline
                        + "    " + charString + newline
                        + "    " + keyCodeString + newline
                        + "    " + modString + newline
                        + "    " + locString + newline
                        + "    " + isString + newline);

    }

   void processVTKeyPressed(KeyEvent e){

      displayInfo(e);
      int keyCode = e.getKeyCode();

      if (isLinux && keyCode == e.VK_ALT_GRAPH) {

         isAltGr = true;
      }

      // be careful with the control key
      if (keyCode == e.VK_UNDEFINED ||
            keyCode == e.VK_CAPS_LOCK ||
            keyCode == e.VK_SHIFT ||
            keyCode == e.VK_ALT ||
            keyCode == e.VK_ALT_GRAPH ||
            keyCode == e.VK_CONTROL
         ) {

         return;
      }

      // be careful with the control key !!!!!!
      if (!e.isAltDown() ||
         !e.isShiftDown() ||
         !e.isControlDown() ||
         keyCode != KeyEvent.VK_CONTROL &&  // be careful about removing this line
         !e.isActionKey()) {

//            if (keyCode == KeyEvent.VK_ESCAPE ||
//               keyCode == KeyEvent.VK_CONTROL ||
//               keyCode == KeyEvent.VK_BACK_SPACE) {
//               displayInfo(e,"Pressed added");
            keyevent = e;
            dialog.setVisible(false);
            dialog.dispose();
//            }
      }


   }

   void processVTKeyTyped(KeyEvent e){

       displayInfo(e);
      int keycode = e.getKeyCode();
      if (e.isAltDown() ||
         e.isShiftDown() ||
         e.isControlDown() ||
         e.isActionKey() ||
         keycode == KeyEvent.VK_CONTROL) {

         keyevent = e;

         dialog.setVisible(false);
         dialog.dispose();
      }

   }

   void processVTKeyReleased(KeyEvent e){
         displayInfo(e);
      if (isLinux && e.getKeyCode() == e.VK_ALT_GRAPH) {

         isAltGr = false;
      }
      int keycode = e.getKeyCode();
      if (e.isAltDown() ||
         e.isShiftDown() ||
         e.isControlDown() ||
         e.isActionKey() ||
         keycode == KeyEvent.VK_CONTROL) {


         keyevent = e;

         dialog.setVisible(false);
         dialog.dispose();
      }
  }

}
