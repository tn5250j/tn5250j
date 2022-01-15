/*
 * Title: JumpNextAction.java
 * Copyright:   Copyright (c) 2001,2002
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.5
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
package org.tn5250j.keyboard.actions;

import static org.tn5250j.keyboard.KeyMnemonic.PASTE;

import org.tn5250j.SessionGui;
import org.tn5250j.keyboard.KeyMapper;

import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * Paste from the clipboard
 */
public class PasteAction extends EmulatorAction {
    public PasteAction(final SessionGui sessionGui, final KeyMapper keyMap) {
        super(sessionGui,
                PASTE.mnemonic,
                new KeyCodeCombination(KeyCode.V, KeyCombination.ALT_DOWN),
                keyMap);
    }

    @Override
    public void handle() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString()) {
            final String content = clipboard.getString();
            session.getScreen().pasteText(content, false);
        }
    }
}
