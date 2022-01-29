/**
 * Title: EmulatorAction.java
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

import org.tn5250j.SessionGui;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.interfaces.OptionAccessFactory;
import org.tn5250j.keyboard.KeyMapper;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCodeCombination;

/**
 * Base class for all emulator actions
 */
public abstract class EmulatorAction implements EventHandler<ActionEvent> {
    // content pane to be used if needed by subclasses
    protected SessionGui session;
    private final String name;

    public EmulatorAction(final SessionGui session, final String name) {
        this.name = name;
        this.session = session;
    }

    public EmulatorAction(final SessionGui session, final String name, final KeyCodeCombination ks, final KeyMapper keyMap) {

        this(session, name);

        setKeyStroke(name, ks, keyMap);
    }

    protected void setKeyStroke(final String action, KeyCodeCombination ks, final KeyMapper keyMap) {

        if (OptionAccessFactory.getInstance().isRestrictedOption(action))
            return;

        if (KeyMapper.isKeyStrokeDefined(action)) {
            ks = KeyMapper.getKeyStroke(action);
        }

        this.session.addKeyAction(ks, this);

        // check for alternate
        if (KeyMapper.isKeyStrokeDefined(action + ".alt2")) {
            ks = KeyMapper.getKeyStroke(action + ".alt2");
            this.session.addKeyAction(ks, this);
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public void handle(final ActionEvent e) {
        UiUtils.runInFx(() -> {
            handle();
            return null;
        });
    }

    protected abstract void handle();
}
