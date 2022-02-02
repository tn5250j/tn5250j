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

import java.net.URL;
import java.util.ResourceBundle;

import org.tn5250j.SessionConfig;
import org.tn5250j.gui.TitledBorderedPane;
import org.tn5250j.tools.LangTool;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

class TabAttributesController extends AbstractAttributesController {
    @FXML
    BorderPane view;

    @FXML
    TitledBorderedPane tabOptionsPane;

    @FXML
    CheckBox tabCloseCheck;

    TabAttributesController(final SessionConfig config) {
        super(config, "Tabs");
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        tabOptionsPane.setTitle(LangTool.getString("sa.titleTabOptions"));

        tabCloseCheck.setText(LangTool.getString("sa.confirmTabClose"));
        // Check if tab close confirmation is to be checked
        tabCloseCheck.setSelected(getStringProperty("confirmTabClose").equals("Yes"));
    }

    @Override
    public void applyAttributes() {
        if (tabCloseCheck.isSelected()) {
            fireStringPropertyChanged("confirmTabClose", "Yes");
        } else {
            fireStringPropertyChanged("confirmTabClose", "No");
        }
    }

    @Override
    public Region getView() {
        return view;
    }
}
