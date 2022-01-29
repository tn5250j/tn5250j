/*
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

import java.util.Objects;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * This class is basically a wrapper for KeyEvent that is used internally to the
 * project instead of KeyEvents. Uses getKeyLocation for 1.4 and greater.
 */
public class KeyStroker {

    protected int location;
    private KeyCode keyCode;
    private boolean isShiftDown;
    private boolean isControlDown;
    private boolean isAltDown;
    private boolean isAltGrDown;
    private int hashCode;

    public static final String altSuffix = ".alt2";


    // literals copied from KeyEvent of JDK Version 1.4.0

    /**
     * A constant indicating that the keyLocation is indeterminate
     * or not relevant.
     * KEY_TYPED events do not have a keyLocation; this value
     * is used instead.
     *
     * @since 1.4
     */
    public static final int KEY_LOCATION_UNKNOWN = 0;

    /**
     * A constant indicating that the key pressed or released
     * is not distinguished as the left or right version of a key,
     * and did not originate on the numeric keypad (or did not
     * originate with a virtual key corresponding to the numeric
     * keypad).
     *
     * @since 1.4
     */
    public static final int KEY_LOCATION_STANDARD = 1;

    /**
     * A constant indicating that the key pressed or released is in
     * the left key location (there is more than one possible location
     * for this key).  Example: the left shift key.
     *
     * @since 1.4
     */
    public static final int KEY_LOCATION_LEFT = 2;

    /**
     * A constant indicating that the key pressed or released is in
     * the right key location (there is more than one possible location
     * for this key).  Example: the right shift key.
     *
     * @since 1.4
     */
    public static final int KEY_LOCATION_RIGHT = 3;

    /**
     * A constant indicating that the key event originated on the
     * numeric keypad or with a virtual key corresponding to the
     * numeric keypad.
     *
     * @since 1.4
     */
    public static final int KEY_LOCATION_NUMPAD = 4;

    public KeyStroker(final KeyEvent ke) {
        this(ke, ke.getCode() == KeyCode.ALT_GRAPH);
    }

    public KeyStroker(final KeyEvent ke, final boolean isAltGrDown) {
        this(ke.getCode(),
            ke.isShiftDown(),
            ke.isControlDown(),
            ke.isAltDown(),
            isAltGrDown,
            KEY_LOCATION_STANDARD); // key location is not supported in  FX key event
    }

    public KeyStroker(final int keyCode,
            final boolean isShiftDown,
            final boolean isControlDown,
            final boolean isAltDown,
            final boolean isAltGrDown,
            final int location) {
        this(KeyStrokeHelper.getCode(keyCode), isShiftDown, isControlDown, isAltDown, isAltGrDown, location);
    }
    public KeyStroker(final KeyCode keyCode,
                      final boolean isShiftDown,
                      final boolean isControlDown,
                      final boolean isAltDown,
                      final boolean isAltGrDown,
                      final int location) {
        setAttributes(keyCode, isShiftDown, isControlDown, isAltDown, isAltGrDown, location);
    }

    public void setAttributes(final KeyEvent ke, final boolean isAltGr) {
        setAttributes(ke.getCode(), ke.isShiftDown(), ke.isControlDown(), ke.isAltDown(),
                isAltGr, KEY_LOCATION_STANDARD);
    }

    private void setAttributes(final KeyCode keyCode, final boolean isShiftDown, final boolean isControlDown,
            final boolean isAltDown, final boolean isAltGrDown, final int location) {
        this.keyCode = keyCode;
        this.isShiftDown = isShiftDown;
        this.isControlDown = isControlDown;
        this.isAltDown = isAltDown;
        this.isAltGrDown = isAltGrDown;
        this.location = location;

        hashCode = Objects.hash(
                keyCode,
                isShiftDown,
                isControlDown,
                isAltDown,
                isAltGrDown,
                location);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public boolean isShiftDown() {

        return isShiftDown;
    }

    public boolean isControlDown() {

        return isControlDown;
    }

    public boolean isAltDown() {

        return isAltDown;
    }

    public boolean isAltGrDown() {

        return isAltGrDown;
    }

    public int getLocation() {
        return location;
    }

    public boolean equals(final KeyEvent ke) {
        return equals(ke, ke.getCode() == KeyCode.ALT_GRAPH);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof KeyStroker) {
            final KeyStroker ks = (KeyStroker) obj;
            return equals(ks, ks.isAltDown);
        }

        return false;
    }

    public boolean equals(final KeyEvent ke, final boolean altGrDown) {

        return (keyCode == ke.getCode() &&
                isShiftDown == ke.isShiftDown() &&
                isControlDown == ke.isControlDown() &&
                isAltDown == ke.isAltDown() &&
                isAltGrDown == altGrDown &&
                location == KEY_LOCATION_STANDARD);
    }

    public boolean equals(final KeyStroker ks, final boolean altGrDown) {
        return ks.keyCode == keyCode &&
                ks.isShiftDown == isShiftDown &&
                ks.isControlDown == isControlDown &&
                ks.isAltDown == isAltDown &&
                ks.isAltGrDown == altGrDown &&
                ks.location == location;
    }

    @Override
    public String toString() {

        return new String(keyCode + "," +
                (isShiftDown ? "true" : "false") + "," +
                (isControlDown ? "true" : "false") + "," +
                (isAltDown ? "true" : "false") + "," +
                (isAltGrDown ? "true" : "false") + "," +
                location);
    }

    public String getKeyStrokeDesc() {

        return (isShiftDown ? "Shift + " : "") +
                (isControlDown ? "Ctrl + " : "") +
                (isAltDown ? "Alt + " : "") +
                (isAltGrDown ? "Alt-Gr + " : "") +
                keyCode.getName();
    }

    public KeyCode getKeyCode() {
        return keyCode;
    }
}
