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
package org.tn5250j.settings;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.tn5250j.SessionConfig;
import org.tn5250j.tools.LangTool;

public class TabAttributesPanel extends AttributesPanel {

	private static final long serialVersionUID = 1L;
	JCheckBox tabCloseCheck;

	public TabAttributesPanel(SessionConfig config) {
		super(config,"Tabs");
	}

	// Component initialization
	public void initPanel() throws Exception {

		setLayout(new BorderLayout());
		contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));
		add(contentPane,BorderLayout.NORTH);

		// Define close tab confirmation panel
		JPanel tabConfirm = new JPanel();
		tabConfirm.setBorder(BorderFactory.createTitledBorder(LangTool.getString("sa.titleTabOptions")));

		tabCloseCheck = new JCheckBox(LangTool.getString("sa.confirmTabClose"));

		// Check if tab close confirmation is to be checked
		tabCloseCheck.setSelected(getStringProperty("confirmTabClose").equals("Yes"));

		tabConfirm.add(tabCloseCheck);

		contentPane.add(tabConfirm);

	}

	public void save() {

	}

	public void applyAttributes() {

		String value = "";

		if (tabCloseCheck.isSelected()) {
			value = "Yes";
		} else {
			value = "No";
		}

		changes.firePropertyChange(this, "confirmTabClose", getStringProperty("confirmTabClose"), value);

		setProperty("confirmTabClose","Yes");

	}

}

