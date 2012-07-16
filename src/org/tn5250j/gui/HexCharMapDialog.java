/**
 * $Id$
 *
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2009
 * Company:
 * @author: master_jaf
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
import java.text.CollationKey;
import java.text.Collator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

import org.tn5250j.encoding.ICodePage;
import org.tn5250j.tools.LangTool;

/**
 * Shows a dialog, containing all HEX values and their corresponding chars.
 */
public class HexCharMapDialog {

	private final DefaultListModel hexListModel;
	private final JList hexList;
	private final Component parent;

	public HexCharMapDialog(Component parent, ICodePage codepage) {
		assert codepage != null : new IllegalArgumentException("A codepage is needed!");

		this.parent = parent;

		// we will use a collator here so that we can take advantage of the locales
		Collator collator = Collator.getInstance();
		CollationKey key = null;

		Set<CollationKey> set = new TreeSet<CollationKey>();
		StringBuilder sb = new StringBuilder();
		for (int x =0;x < 256; x++) {
			char ac = codepage.ebcdic2uni(x);
			if (!Character.isISOControl(ac)) {
				sb.setLength(0);
				if (Integer.toHexString(ac).length() == 1){
					sb.append("0x0" + Integer.toHexString(ac).toUpperCase());
				}
				else {
					sb.append("0x" + Integer.toHexString(ac).toUpperCase());
				}

				sb.append(" - " + ac);
				key = collator.getCollationKey(sb.toString());

				set.add(key);
			}
		}

		Iterator<?> iterator = set.iterator();
		hexListModel = new DefaultListModel();
		while (iterator.hasNext()) {
			CollationKey keyc = (CollationKey)iterator.next();
			hexListModel.addElement(keyc.getSourceString());
		}

		hexList = new JList(hexListModel);

		hexList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		hexList.setSelectedIndex(0);

	}

	/**
	 * Displays the dialog
	 * 
	 * @return a String, containing the selected character OR null, if nothing was selected
	 */
	public String showModal() {

		final JScrollPane listScrollPane = new JScrollPane(hexList);
		listScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		listScrollPane.setSize(40,100);

		final JPanel srp = new JPanel();
		srp.setLayout(new BorderLayout());
		srp.add(listScrollPane,BorderLayout.CENTER);

		final String[] options = {LangTool.getString("hm.optInsert"), LangTool.getString("hm.optCancel")};

		int result = JOptionPane.showOptionDialog(
				parent,   // the parent that the dialog blocks
				new Object[] {srp},                // the dialog message array
				LangTool.getString("hm.title"),    // the title of the dialog window
				JOptionPane.DEFAULT_OPTION,        // option type
				JOptionPane.INFORMATION_MESSAGE,      // message type
				null,                              // optional icon, use null to use the default icon
				options,                           // options string array, will be made into buttons//
				options[0]                         // option that should be made into a default button
		);

		if (result == 0) {
			final String selval = (String) hexList.getSelectedValue();
			return selval.substring(selval.length()-1);
		}
		return null;
	}

}
