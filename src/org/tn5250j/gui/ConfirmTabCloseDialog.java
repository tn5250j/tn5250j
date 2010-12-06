/**
 * $Id$
 * 
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2009
 * Company:
 * @author: duncanc
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
package org.tn5250j.gui;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Small dialog asking the user to confirm the close tab request
 *
 * @author duncanc
 */
public class ConfirmTabCloseDialog {

	private final static String[] OPTIONS = new String[] { "Close", "Cancel" };

	private final Component parent;

	private JDialog dialog;
	private JOptionPane pane;


	/**
	 * @param parent
	 */
	public ConfirmTabCloseDialog(Component parent) {
		super();
		this.parent = parent;
		initLayout();
	}

	private void initLayout() {
		Object[] messages = new Object[1];
		{
			JPanel srp = new JPanel();
			srp.setLayout(new BorderLayout());
			JLabel jl = new JLabel("Are you sure you want to close this tab?");
			srp.add(jl, BorderLayout.NORTH);
			messages[0] = srp;
		}

		pane = new JOptionPane(messages, // the dialog message array
				JOptionPane.QUESTION_MESSAGE, // message type
				JOptionPane.DEFAULT_OPTION, // option type
				null, // optional icon, use null to use the default icon
				OPTIONS, // options string array, will be made into buttons
				OPTIONS[0]);

		dialog = pane.createDialog(parent, "Confirm Tab Close");

	}

	/**
	 * Shows the dialog and returns the true if the close was confirmed
	 * or false if the operation was canceled.
	 *
	 * @return
	 */
	public boolean show() {
		dialog.setVisible(true);
		if (OPTIONS[0].equals(pane.getValue())) {
			return true;
		}
		return false;
	}

}
