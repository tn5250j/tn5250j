/*
 * Title: MouseAttributesPanel
 * Copyright:   Copyright (c) 2001
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
package org.tn5250j.sessionsettings;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.tn5250j.SessionConfig;
import org.tn5250j.tools.LangTool;

class MouseAttributesPanel extends AttributesPanel {

    private static final long serialVersionUID = 1L;
    private JCheckBox dceCheck;
    private JCheckBox mwCheck;

    MouseAttributesPanel(final SessionConfig config) {
        super(config, "Mouse");
    }

    /**
     * Component initialization
     */
    @Override
    public void initPanel() throws Exception {

        setLayout(new BorderLayout());
        final JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        add(contentPane, BorderLayout.NORTH);

        // define double click as enter
        final JPanel dcep = new JPanel();
        dcep.setBorder(BorderFactory.createTitledBorder(LangTool.getString("sa.doubleClick")));

        dceCheck = new JCheckBox(LangTool.getString("sa.sendEnter"));

        // check if double click sends enter
        dceCheck.setSelected(getStringProperty("doubleClick").equals("Yes"));

        dcep.add(dceCheck);

        // define double click as enter
        final JPanel mwp = new JPanel();
        mwp.setBorder(BorderFactory.createTitledBorder(LangTool.getString("sa.mouseWheel")));

        mwCheck = new JCheckBox(LangTool.getString("sa.activateMW"));

        // check if mouse wheel active
        mwCheck.setSelected(getStringProperty("mouseWheel").equals("Yes"));

        mwp.add(mwCheck);

        contentPane.add(dcep);
        contentPane.add(mwp);

    }

    @Override
    public void applyAttributes() {

        //  double click enter
        if (dceCheck.isSelected()) {
            changes.firePropertyChange(this, "doubleClick",
                    getStringProperty("doubleClick"),
                    "Yes");
            setProperty("doubleClick", "Yes");
        } else {
            changes.firePropertyChange(this, "doubleClick",
                    getStringProperty("doubleClick"),
                    "No");
            setProperty("doubleClick", "No");
        }

        if (mwCheck.isSelected()) {
            changes.firePropertyChange(this, "mouseWheel",
                    getStringProperty("mouseWheel"),
                    "Yes");
            setProperty("mouseWheel", "Yes");
        } else {
            changes.firePropertyChange(this, "mouseWheel",
                    getStringProperty("mouseWheel"),
                    "No");
            setProperty("mouseWheel", "No");
        }

    }
}
