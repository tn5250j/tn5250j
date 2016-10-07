/*
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

import org.tn5250j.SessionPanel;
import org.tn5250j.keyboard.KeyMapper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import static org.tn5250j.keyboard.KeyMnemonic.OPEN_NEW;

/**
 * New Session emulator action to open new sessions
 */
public class NewSessionAction extends EmulatorAction {

  private static final long serialVersionUID = 1L;

  public NewSessionAction(SessionPanel session, KeyMapper keyMap) {
    super(session,
        OPEN_NEW.mnemonic,
        KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_MASK),
        keyMap);
  }

  public void actionPerformed(ActionEvent e) {
    session.startNewSession();
  }
}
