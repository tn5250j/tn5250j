/*
 * Title: TransferAction.java
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

import org.tn5250j.SessionPanel;
import org.tn5250j.keyboard.KeyMapper;
import org.tn5250j.tools.XTFRFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import static org.tn5250j.keyboard.KeyMnemonic.FILE_TRANSFER;

/**
 * Display session attributes
 */
public class TransferAction extends EmulatorAction {

  private static final long serialVersionUID = 1L;

  public TransferAction(SessionPanel session, KeyMapper keyMap) {
    super(session,
        FILE_TRANSFER.mnemonic,
        KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.ALT_MASK),
        keyMap);
  }

  public void actionPerformed(ActionEvent e) {
    new XTFRFile((Frame) SwingUtilities.getRoot(session),
        session.getVT(), session);
  }
}
