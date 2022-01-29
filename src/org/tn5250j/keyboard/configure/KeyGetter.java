/**
 * Title: KeyGetter
 * Copyright:   Copyright (c) 2001, 2002, 2003
 * Company:
 *
 * @author Kenneth J. Pouncey
 * @version 0.1
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
 * MERCHANTABILreITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 */
package org.tn5250j.keyboard.configure;

import org.tn5250j.keyboard.KeyStrokeHelper;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

import javafx.event.EventType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * This class extends label so that we can display text as well as capture
 * the key stroke(s) to assign to keys.
 */
public class KeyGetter extends Label {
    private static final transient TN5250jLogger LOG = TN5250jLogFactory.getLogger(KeyGetter.class);

    KeyEvent keyevent;
    boolean isAltGr;
    Dialog<ButtonType> dialog;

    public void setDialog(final Dialog<ButtonType> dialog) {

        this.dialog = dialog;

    }

    public KeyGetter() {
        super();

        setOnKeyPressed(this::processVTKeyPressed);
        setOnKeyReleased(this::processVTKeyReleased);
        setOnKeyTyped(this::processVTKeyTyped);
    }

    /*
     * We have to jump through some hoops to avoid
     * trying to print non-printing characters
     * such as Shift.  (Not only do they not print,
     * but if you put them in a String, the characters
     * afterward won't show up in the text area.)
     */
    private void displayInfo(final KeyEvent e) {

        String charString, keyCodeString, modString, tmpString, isString;
        String typeStr;

        final EventType<KeyEvent> type = e.getEventType();
        if (type == KeyEvent.KEY_TYPED) {
            typeStr = "KEY_PRESSED";
        } else if (type == KeyEvent.KEY_PRESSED) {
            typeStr = "KEY_RELEASED";
        } else if (type == KeyEvent.KEY_RELEASED) {
            typeStr = "KEY_TYPED";
        } else {
            typeStr = "unknown type";
        }

        final char c = e.getCharacter().charAt(0);
        final KeyCode keyCode = e.getCode();
        final int modifiers = KeyStrokeHelper.getModifiersFlag(e);

        if (Character.isISOControl(c)) {
            charString = "key character = "
                    + "(an unprintable control character)";
        } else {
            charString = "key character = '"
                    + c + "'";
        }

        keyCodeString = "key code = " + keyCode + " (" + e.getText() + ")";
        if (keyCode == KeyCode.PREVIOUS_CANDIDATE) {
            keyCodeString += " previous candidate ";
        }

        if (keyCode == KeyCode.DEAD_ABOVEDOT ||
                keyCode == KeyCode.DEAD_ABOVERING ||
                keyCode == KeyCode.DEAD_ACUTE ||
                keyCode == KeyCode.DEAD_BREVE ||
                keyCode == KeyCode.DEAD_CIRCUMFLEX

        ) {

            keyCodeString += " dead key ";

        }

        modString = "modifiers = " + modifiers;
        tmpString = KeyStrokeHelper.getKeyModifiersText(e);
        if (tmpString.length() > 0) {
            modString += " (" + tmpString + ")";
        } else {
            modString += " (no modifiers)";
        }

        isString = "isKeys = isActionKey (" + KeyStrokeHelper.isActionKey(e) + ")" +
                " isAltDown (" + e.isAltDown() + ")" +
                " isAltGraphDownLinux (" + isAltGr + ")" +
                " isControlDown (" + e.isControlDown() + ")" +
                " isMetaDown (" + e.isMetaDown() + ")" +
                " isShiftDown (" + e.isShiftDown() + ")";


        if (LOG.isDebugEnabled()) {
            LOG.debug(typeStr + "\n"
                    + "    " + charString + "\n"
                    + "    " + keyCodeString + "\n"
                    + "    " + modString + "\n"
                    + "    " + isString + "\n");
        }

    }

    void processVTKeyPressed(final KeyEvent e) {

        displayInfo(e);
        final KeyCode keyCode = e.getCode();

        if (keyCode == KeyCode.ALT_GRAPH) {

            isAltGr = true;
        }

        // be careful with the control key
        if (keyCode == KeyCode.UNDEFINED ||
                keyCode == KeyCode.CAPS ||
                keyCode == KeyCode.SHIFT ||
                keyCode == KeyCode.ALT ||
                keyCode == KeyCode.ALT_GRAPH ||
                keyCode == KeyCode.CONTROL
        ) {

            return;
        }

        // be careful with the control key !!!!!!
        if (!e.isAltDown() ||
                !e.isShiftDown() ||
                !e.isControlDown() ||
                keyCode != KeyCode.CONTROL &&  // be careful about removing this line
                        !KeyStrokeHelper.isActionKey(e)) {

//            if (keyCode == KeyEvent.VK_ESCAPE ||
//               keyCode == KeyEvent.VK_CONTROL ||
//               keyCode == KeyEvent.VK_BACK_SPACE) {
//               displayInfo(e,"Pressed added");
            keyevent = e;
            dialog.close();
        }
    }

    void processVTKeyTyped(final KeyEvent e) {

        displayInfo(e);
        final KeyCode keycode = e.getCode();
        if (e.isAltDown() ||
                e.isShiftDown() ||
                e.isControlDown() ||
                KeyStrokeHelper.isActionKey(e) ||
                keycode == KeyCode.CONTROL) {

            keyevent = e;
            dialog.close();
        }
    }

    void processVTKeyReleased(final KeyEvent e) {
        displayInfo(e);
        if (e.getCode() == KeyCode.ALT_GRAPH) {

            isAltGr = false;
        }
        final KeyCode keycode = e.getCode();
        if (e.isAltDown() ||
                e.isShiftDown() ||
                e.isControlDown() ||
                KeyStrokeHelper.isActionKey(e) ||
                keycode == KeyCode.CONTROL) {


            keyevent = e;
            dialog.close();
        }
    }
}
