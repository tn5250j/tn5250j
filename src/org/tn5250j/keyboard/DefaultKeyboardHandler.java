/*
 * Title: DefaultKeyboardHandler
 * Copyright:   Copyright (c) 2001, 2002
 * Company:
 *
 * @author Kenneth J. Pouncey
 * @version 0.5
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 */

package org.tn5250j.keyboard;

import org.tn5250j.Session5250;
import org.tn5250j.SessionGui;
import org.tn5250j.keyboard.actions.AttributesAction;
import org.tn5250j.keyboard.actions.CloseAction;
import org.tn5250j.keyboard.actions.CopyAction;
import org.tn5250j.keyboard.actions.DebugAction;
import org.tn5250j.keyboard.actions.DispMsgsAction;
import org.tn5250j.keyboard.actions.EmailAction;
import org.tn5250j.keyboard.actions.GuiAction;
import org.tn5250j.keyboard.actions.HotspotsAction;
import org.tn5250j.keyboard.actions.JumpNextAction;
import org.tn5250j.keyboard.actions.JumpPrevAction;
import org.tn5250j.keyboard.actions.NewSessionAction;
import org.tn5250j.keyboard.actions.OpenSameAction;
import org.tn5250j.keyboard.actions.PasteAction;
import org.tn5250j.keyboard.actions.PrintAction;
import org.tn5250j.keyboard.actions.QuickEmailAction;
import org.tn5250j.keyboard.actions.RulerAction;
import org.tn5250j.keyboard.actions.RunScriptAction;
import org.tn5250j.keyboard.actions.SpoolWorkAction;
import org.tn5250j.keyboard.actions.ToggleConnectionAction;
import org.tn5250j.keyboard.actions.TransferAction;

import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;

/**
 * The default keyboard input handler.
 */
class DefaultKeyboardHandler extends KeyboardHandler {

    /**
     * Creates a new keyboard handler
     *
     * @param session The session to which the keys should be sent
     */
    DefaultKeyboardHandler(final Session5250 session) {
        super(session);
    }

    @Override
    public boolean isKeyStrokeDefined(final String accelKey) {

        return KeyMapper.isKeyStrokeDefined(accelKey);
    }

    @Override
    public KeyCodeCombination getKeyStroke(final String accelKey) {
        return KeyMapper.getKeyStroke(accelKey);
    }

    /*
     * We have to jump through some hoops to avoid
     * trying to print non-printing characters
     * such as Shift.  (Not only do they not print,
     * but if you put them in a String, the characters
     * afterward won't show up in the text area.)
     */
    protected void displayInfo(final KeyEvent e, final String s) {
        String charString, keyCodeString, modString, tmpString, isString;

        final KeyCode keyCode = e.getCode();
        final char c = e.getCharacter().charAt(0);

        if (Character.isISOControl(c)) {
            charString = "key character = "
                    + "(an unprintable control character)";
        } else {
            charString = "key character = '"
                    + c + "'";
        }

        keyCodeString = "key code = " + keyCode
                + " ("
                + e.getText()
                + ")";
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

        modString = "modifiers = " + KeyStrokeHelper.getModifiersFlag(e);
        tmpString = KeyStrokeHelper.getKeyModifiersText(e);
        if (tmpString.length() > 0) {
            modString += " (" + tmpString + ")";
        } else {
            modString += " (no modifiers)";
        }

        isString = "isKeys = isActionKey (" + KeyStrokeHelper.isActionKey(e) + ")" +
                " isAltDown (" + e.isAltDown() + ")" +
                // " isAltGraphDown (" + e.isAltGraphDown() + ")" + // not supported in FX
                " isAltGraphDownLinux (" + isAltGr + ")" +
                " isControlDown (" + e.isControlDown() + ")" +
                " isMetaDown (" + e.isMetaDown() + ")" +
                " isShiftDown (" + e.isShiftDown() + ")";

        System.out.println(s + "\n"
                + "    " + charString + "\n"
                + "    " + keyCodeString + "\n"
                + "    " + modString + "\n"
                + "    " + isString + "\n");
    }

    /**
     * This is here for keybindings using the swing input map - the preferred
     * way to use the keyboard.
     */
    @Override
    void initKeyBindings() {

        if (session.getGUI() == null)
            return;

        final SessionGui sessionGui = session.getGUI();

        new NewSessionAction(sessionGui, keyMap);
        new ToggleConnectionAction(sessionGui, keyMap);
        new JumpNextAction(sessionGui, keyMap);
        new JumpPrevAction(sessionGui, keyMap);
        new HotspotsAction(sessionGui, keyMap);
        new GuiAction(sessionGui, keyMap);
        new DispMsgsAction(sessionGui, keyMap);
        new AttributesAction(sessionGui, keyMap);
        new PrintAction(sessionGui, keyMap);
        new RulerAction(sessionGui, keyMap);
        new CloseAction(sessionGui, keyMap);
        new TransferAction(sessionGui, keyMap);
        new EmailAction(sessionGui, keyMap);
        new RunScriptAction(sessionGui, keyMap);
        new DebugAction(sessionGui, keyMap);
        new CopyAction(sessionGui, keyMap);
        new PasteAction(sessionGui, keyMap);
        new SpoolWorkAction(sessionGui, keyMap);
        new QuickEmailAction(sessionGui, keyMap);
        new OpenSameAction(sessionGui, keyMap);

    }

    /**
     * Forwards key events directly to the input handler.
     * This is slightly faster than using a KeyListener
     * because some Swing overhead is avoided.
     */
    @Override
    public void processKeyEvent(final KeyEvent evt) {

        if (evt.isConsumed())
            return;

        final EventType<KeyEvent> type = evt.getEventType();

        if (type == KeyEvent.KEY_TYPED) {
            processVTKeyTyped(evt);
        } else if (type == KeyEvent.KEY_PRESSED) {
            processVTKeyPressed(evt);
        } else if (type == KeyEvent.KEY_RELEASED) {
            processVTKeyReleased(evt);
        }
    }

    private void processVTKeyPressed(final KeyEvent e) {


        keyProcessed = true;
        final KeyCode keyCode = e.getCode();

        if (isLinux && keyCode == KeyCode.ALT_GRAPH) {

            isAltGr = true;
        }

        if (keyCode == KeyCode.CAPS ||
                keyCode == KeyCode.SHIFT ||
                keyCode == KeyCode.ALT ||
                keyCode == KeyCode.ALT_GRAPH
        ) {

            return;
        }

        if (emulatorAction(e)) {
            return;
        }

        if (isLinux)
            lastKeyStroke = KeyMapper.getKeyStrokeText(e, isAltGr);
        else
            lastKeyStroke = KeyMapper.getKeyStrokeText(e);

        if (lastKeyStroke != null && !lastKeyStroke.equals("null")) {

            if (lastKeyStroke.startsWith("[") || lastKeyStroke.length() == 1) {

                screen.sendKeys(lastKeyStroke);
                if (recording)
                    recordBuffer.append(lastKeyStroke);
            } else {
                session.getGUI().executeMacro(lastKeyStroke);
            }
            if (lastKeyStroke.startsWith("[mark")) {
                if (lastKeyStroke.equals("[markleft]") ||
                        lastKeyStroke.equals("[markright]") ||
                        lastKeyStroke.equals("[markup]") ||
                        lastKeyStroke.equals("[markdown]")) {
                    session.getGUI().doKeyBoundArea(lastKeyStroke);
                }
            }
        } else
            keyProcessed = false;

        if (keyProcessed)
            e.consume();

    }

    private void processVTKeyTyped(final KeyEvent e) {

        final char kc = getKeyChar(e);
//      displayInfo(e,"Typed processed " + keyProcessed);
        // Hack to make german umlauts work under Linux
        // The problem is that these umlauts don't generate a keyPressed event
        // and so keyProcessed is true (even if is hasn't been processed)
        // so we check if it's a letter (with or without shift) and skip return
        if (isLinux) {

            if (!((Character.isLetter(kc) || kc == '\u20AC') && (!hasModifiers(e)
                    || e.isShiftDown()))) {

                if (Character.isISOControl(kc) || keyProcessed) {
                    return;
                }
            }
        } else {
            if (Character.isISOControl(kc) || keyProcessed) {
                return;
            }
        }
        if (!session.isConnected())
            return;
        screen.sendKeys(Character.toString(kc));
        if (recording)
            recordBuffer.append(kc);
        keyProcessed = true;
        e.consume();
    }

    private char getKeyChar(final KeyEvent e) {
        return e.getCharacter().charAt(0);
    }

    private boolean hasModifiers(final KeyEvent e) {
        return e.isAltDown()
                || e.isControlDown()
                || e.isMetaDown()
                || e.isShiftDown()
                || e.isShortcutDown();
    }

    private void processVTKeyReleased(final KeyEvent e) {

        if (isLinux && e.getCode() == KeyCode.ALT_GRAPH) {
            isAltGr = false;
        }

        if (Character.isISOControl(getKeyChar(e)) || keyProcessed || e.isConsumed())
            return;

        final String s = KeyMapper.getKeyStrokeText(e);

        if (s != null) {

            if (s.startsWith("[")) {
                screen.sendKeys(s);
                if (recording)
                    recordBuffer.append(s);
            } else
                session.getGUI().executeMacro(s);

        } else
            keyProcessed = false;

        if (keyProcessed)
            e.consume();
    }
}
