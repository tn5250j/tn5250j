/*
 * Title: JumpPrevAction.java
 * Copyright:   Copyright (c) 2001,2002
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
package org.tn5250j.keyboard.actions;

import static org.tn5250j.keyboard.KeyMnemonic.JUMP_PREV;

import org.tn5250j.SessionGui;
import org.tn5250j.keyboard.KeyMapper;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * Jump to the Previous session action
 */
public class JumpPrevAction extends EmulatorAction {
    public JumpPrevAction(final SessionGui session, final KeyMapper keyMap) {
        super(session,
                JUMP_PREV.mnemonic,
                new KeyCodeCombination(KeyCode.PAGE_DOWN, KeyCombination.ALT_DOWN),
                keyMap);
    }

    @Override
    public void handle() {
        session.prevSession();
    }
}
