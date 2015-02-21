/**
 * <p>Title: ScreenOIA.java</p>
 * <p>Description: Main interface to control Operator information area screen</p>
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
package org.tn5250j.framework.tn5250;

import java.util.Vector;

import org.tn5250j.event.ScreenOIAListener;

/**
 * The operator information area of a host session. This area is used to provide
 * status information regarding the state of the host session and location of
 * the cursor.  A ScreenOIA object can be obtained using the GetOIA() method on
 * an instance of Screen5250.
 *
 *
 */
public class ScreenOIA
  {
   // OIA_LEVEL
   public static final int OIA_LEVEL_INPUT_INHIBITED   =  1;
   public static final int OIA_LEVEL_NOT_INHIBITED   =  2;
   public static final int OIA_LEVEL_MESSAGE_LIGHT_ON  =  3;
   public static final int OIA_LEVEL_MESSAGE_LIGHT_OFF  =  4;
   public static final int OIA_LEVEL_AUDIBLE_BELL  =  5;
   public static final int OIA_LEVEL_INSERT_MODE  =  6;
   public static final int OIA_LEVEL_KEYBOARD  =  7;
   public static final int OIA_LEVEL_CLEAR_SCREEN  =  8;
   public static final int OIA_LEVEL_SCREEN_SIZE  =  9;
   public static final int OIA_LEVEL_INPUT_ERROR   =  10;
   public static final int OIA_LEVEL_KEYS_BUFFERED   =  11;
   public static final int OIA_LEVEL_SCRIPT   =  12;

   // INPUTINHIBITED
   public static final int INPUTINHIBITED_NOTINHIBITED   =  0;
   public static final int INPUTINHIBITED_SYSTEM_WAIT   =  1;
   public static final int INPUTINHIBITED_COMMCHECK   =  2;
   public static final int INPUTINHIBITED_PROGCHECK   =  3;
   public static final int INPUTINHIBITED_MACHINECHECK   =  4;
   public static final int INPUTINHIBITED_OTHER   =  5;

   public ScreenOIA (Screen5250 screen) {

      source = screen;

   }

   public boolean isInsertMode() {

      return insertMode;
   }

   protected void setInsertMode(boolean mode) {

      level = OIA_LEVEL_INSERT_MODE;
      insertMode = mode;
      fireOIAChanged(ScreenOIAListener.OIA_CHANGED_INSERT_MODE);
   }

   public int getCommCheckCode() {

      return commCheck;
   }

   public int getInputInhibited() {

      return inputInhibited;
   }

   public int getMachineCheckCode() {

      return machineCheck;
   }

   public int getOwner() {
      return owner;
   }

   public int getProgCheckCode() {
      return 0;
   }

	/**
	 * Is the keyboard locked or not
	 *
	 * @return locked or not
	 */
   public boolean isKeyBoardLocked() {
      return locked;
   }

   public boolean isKeysBuffered() {
      return keysBuffered;
   }

   public void setKeysBuffered(boolean kb) {
      level = OIA_LEVEL_KEYS_BUFFERED;
      boolean oldKB = keysBuffered;
      keysBuffered = kb;
      if (keysBuffered != oldKB)
         fireOIAChanged(ScreenOIAListener.OIA_CHANGED_KEYS_BUFFERED);
   }

   protected void setKeyBoardLocked(boolean lockIt) {
      level = OIA_LEVEL_KEYBOARD;
      boolean oldLocked = locked;
      locked = lockIt;
		if (!lockIt) {

			if (isKeysBuffered()) {
				source.sendKeys("");
			}
		}

      if (locked != oldLocked)
         fireOIAChanged(ScreenOIAListener.OIA_CHANGED_KEYBOARD_LOCKED);
   }

   public boolean isMessageWait() {
      return messageWait;
   }

   protected void setMessageLightOn() {
      level = OIA_LEVEL_MESSAGE_LIGHT_ON;
      messageWait = true;
      fireOIAChanged(ScreenOIAListener.OIA_CHANGED_MESSAGELIGHT);
   }

   protected void setMessageLightOff() {
      level = OIA_LEVEL_MESSAGE_LIGHT_OFF;
      messageWait = false;
      fireOIAChanged(ScreenOIAListener.OIA_CHANGED_MESSAGELIGHT);
   }

   public void setScriptActive(boolean running) {
      level = OIA_LEVEL_SCRIPT;
      scriptRunning = running;
      fireOIAChanged(ScreenOIAListener.OIA_CHANGED_SCRIPT);
   }

   public boolean isScriptActive() {
      return scriptRunning;
   }

   public void setAudibleBell() {
      level = OIA_LEVEL_AUDIBLE_BELL;
      fireOIAChanged(ScreenOIAListener.OIA_CHANGED_BELL);
   }

   protected void clearScreen() {
      level = OIA_LEVEL_CLEAR_SCREEN;
      fireOIAChanged(ScreenOIAListener.OIA_CHANGED_CLEAR_SCREEN);
   }

   /**
    * Add a ScreenOIAListener to the listener list.
    *
    * @param listener  The ScreenOIAListener to be added
    */
   public void addOIAListener(ScreenOIAListener listener) {

      if (listeners == null) {
          listeners = new java.util.Vector<ScreenOIAListener>(3);
      }
      listeners.addElement(listener);

   }

   /**
    * Remove a iOhioSessionListener from the listener list.
    *
    * @param listener  The iOhioSessionListener to be removed
    */
   public void removeOIAListener(ScreenOIAListener listener) {

      if (listeners == null) {
          return;
      }
      listeners.removeElement(listener);
   }

   // object methods
   public Screen5250 getSource() {

      return source;
   }


   public void setSource(Screen5250 screen) {

      source = screen;

   }

   public void setOwner(int newOwner) {

      owner = newOwner;

   }

   public int getLevel() {

      return level;
   }

   public String getInhibitedText() {
      return inhibitedText;
   }

   public void setInputInhibited(int inhibit , int whatCode) {
      setInputInhibited(inhibit, whatCode, null);
   }

   public void setInputInhibited(int inhibit , int whatCode, String message) {

      inputInhibited = inhibit;
      level = OIA_LEVEL_INPUT_INHIBITED;
      inhibitedText = message;

//      if (saveInhibit != inhibit || saveInhibitLevel != whatCode) {
         switch(inhibit) {

            case INPUTINHIBITED_COMMCHECK :
               commCheck = whatCode;
               break;
            case INPUTINHIBITED_PROGCHECK :
//               progCheck = whatCode; // never used
               break;
            case INPUTINHIBITED_MACHINECHECK :
               machineCheck = whatCode;
               break;
            case INPUTINHIBITED_SYSTEM_WAIT :
               level = whatCode;
               break;
            case INPUTINHIBITED_NOTINHIBITED :
               level = whatCode;
               break;
         }

         fireOIAChanged(ScreenOIAListener.OIA_CHANGED_INPUTINHIBITED);
//      }
   }

   /**
    * Notify all registered listeners of the onOIAChanged event.
    *
    */
   private void fireOIAChanged(int change) {

      if (listeners != null) {
         int size = listeners.size();
         for (int i = 0; i < size; i++) {
            ScreenOIAListener target =
                    listeners.elementAt(i);
            target.onOIAChanged(this, change);
         }
      }
   }

   private Vector<ScreenOIAListener> listeners = null;
   private boolean insertMode;
   private boolean locked;
   private boolean keysBuffered;
   private int owner = 0;
   private int level = 0;
   private Screen5250 source = null;
   private int commCheck = 0;
   private int machineCheck = 0;
   private boolean messageWait;
   private boolean scriptRunning;
   private int inputInhibited = INPUTINHIBITED_NOTINHIBITED;
   private String inhibitedText;

}
