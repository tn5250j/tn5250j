/**
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

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.KeyStroke;

import org.tn5250j.SessionPanel;
import org.tn5250j.TN5250jConstants;
import org.tn5250j.keyboard.KeyMapper;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

/**
 * Paste from the clipboard
 */
public class PasteAction extends EmulatorAction {

	private static final long serialVersionUID = 1L;
	
	private final TN5250jLogger log = TN5250jLogFactory.getLogger(this.getClass());

	public PasteAction(SessionPanel session, KeyMapper keyMap) {
		super(session,
				TN5250jConstants.MNEMONIC_PASTE,
				KeyStroke.getKeyStroke(KeyEvent.VK_V,KeyEvent.ALT_MASK),
				keyMap);

	}

	public void actionPerformed(ActionEvent event) {
		try {
			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			final Transferable transferable = cb.getContents(this);
			if (transferable != null) {
				final String content = (String) transferable.getTransferData(DataFlavor.stringFlavor);
				session.getScreen().pasteText(content, false);
			}
		} catch (HeadlessException e1) {
			log.debug("HeadlessException", e1);
		} catch (UnsupportedFlavorException e1) {
			log.debug("the requested data flavor is not supported", e1);
		} catch (IOException e1) {
			log.debug("data is no longer available in the requested flavor", e1);
		}
	}
	
}