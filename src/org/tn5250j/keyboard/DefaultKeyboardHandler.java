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

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

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
    public KeyStroke getKeyStroke(final String accelKey) {
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

        final char c = e.getKeyChar();
        final int keyCode = e.getKeyCode();
        final int modifiers = e.getModifiers();

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
        if (keyCode == KeyEvent.VK_PREVIOUS_CANDIDATE) {

            keyCodeString += " previous candidate ";

        }

        if (keyCode == KeyEvent.VK_DEAD_ABOVEDOT ||
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

        isString = "isKeys = isActionKey (" + e.isActionKey() + ")" +
                " isAltDown (" + e.isAltDown() + ")" +
                " isAltGraphDown (" + e.isAltGraphDown() + ")" +
                " isAltGraphDownLinux (" + isAltGr + ")" +
                " isControlDown (" + e.isControlDown() + ")" +
                " isMetaDown (" + e.isMetaDown() + ")" +
                " isShiftDown (" + e.isShiftDown() + ")";


        final String newline = "\n";
        System.out.println(s + newline
                + "    " + charString + newline
                + "    " + keyCodeString + newline
                + "    " + modString + newline
                + "    " + isString + newline);

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

        switch (evt.getID()) {
            case KeyEvent.KEY_TYPED:
                processVTKeyTyped(evt);
                break;
            case KeyEvent.KEY_PRESSED:
                processVTKeyPressed(evt);
                break;
            case KeyEvent.KEY_RELEASED:
                processVTKeyReleased(evt);
                break;
        }

    }

    private void processVTKeyPressed(final KeyEvent e) {


        keyProcessed = true;
        final int keyCode = e.getKeyCode();

        if (isLinux && keyCode == KeyEvent.VK_ALT_GRAPH) {

            isAltGr = true;
        }

        if (keyCode == KeyEvent.VK_CAPS_LOCK ||
                keyCode == KeyEvent.VK_SHIFT ||
                keyCode == KeyEvent.VK_ALT ||
                keyCode == KeyEvent.VK_ALT_GRAPH
        ) {

            return;
        }

        final KeyStroke ks = KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiers(), false);

        if (emulatorAction(ks, e)) {

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
                    session.getGUI().doKeyBoundArea(e, lastKeyStroke);
                }
            }
        } else
            keyProcessed = false;

        if (keyProcessed)
            e.consume();

    }

    private void processVTKeyTyped(final KeyEvent e) {

        final char kc = e.getKeyChar();
//      displayInfo(e,"Typed processed " + keyProcessed);
        // Hack to make german umlauts work under Linux
        // The problem is that these umlauts don't generate a keyPressed event
        // and so keyProcessed is true (even if is hasn't been processed)
        // so we check if it's a letter (with or without shift) and skip return
        if (isLinux) {

            if (!((Character.isLetter(kc) || kc == '\u20AC') && (e.getModifiers() == 0
                    || e.getModifiers() == KeyEvent.SHIFT_MASK))) {

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

    private void processVTKeyReleased(final KeyEvent e) {


        if (isLinux && e.getKeyCode() == KeyEvent.VK_ALT_GRAPH) {

            isAltGr = false;
        }

        if (Character.isISOControl(e.getKeyChar()) || keyProcessed || e.isConsumed())
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
