/*
 * $Id$
 * <p>
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2009
 * Company:
 *
 * @author: duncanc
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
package org.tn5250j.sessionsettings;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.tn5250j.SessionConfig;
import org.tn5250j.tools.LangTool;

class TabAttributesPanel extends AbstractAttributesPanelSwing {

    private static final long serialVersionUID = 1L;
    private JCheckBox tabCloseCheck;

    TabAttributesPanel(final SessionConfig config) {
        super(config, "Tabs");
    }

    // Component initialization
    @Override
    public void initPanel() throws Exception {

        setLayout(new BorderLayout());
        final JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        add(contentPane, BorderLayout.NORTH);

        // Define close tab confirmation panel
        final JPanel tabConfirm = new JPanel();
        tabConfirm.setBorder(BorderFactory.createTitledBorder(LangTool.getString("sa.titleTabOptions")));

        tabCloseCheck = new JCheckBox(LangTool.getString("sa.confirmTabClose"));

        // Check if tab close confirmation is to be checked
        tabCloseCheck.setSelected(getStringProperty("confirmTabClose").equals("Yes"));

        tabConfirm.add(tabCloseCheck);

        contentPane.add(tabConfirm);

    }

    @Override
    public void applyAttributes() {

        String value = "";

        if (tabCloseCheck.isSelected()) {
            value = "Yes";
        } else {
            value = "No";
        }

        changes.firePropertyChange(this, "confirmTabClose", getStringProperty("confirmTabClose"), value);

        setProperty("confirmTabClose", value);

    }

}
