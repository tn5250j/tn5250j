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

import java.net.URL;
import java.util.ResourceBundle;

import org.tn5250j.SessionConfig;
import org.tn5250j.gui.TitledBorderedPane;
import org.tn5250j.tools.LangTool;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;

class MouseAttributesController extends AbstractAttributesController {
    @FXML
    BorderPane view;

    @FXML
    TitledBorderedPane doubleClickPanel;
    @FXML
    CheckBox dceCheck;

    @FXML
    TitledBorderedPane mouseWheelPanel;
    @FXML
    CheckBox mwCheck;

    MouseAttributesController(final SessionConfig config) {
        super(config, "Mouse");
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        // define double click as enter
        doubleClickPanel.setTitle(LangTool.getString("sa.doubleClick"));


        dceCheck.setText(LangTool.getString("sa.sendEnter"));
        dceCheck.setSelected("Yes".equals(getStringProperty("doubleClick")));

        // define double click as enter
        mouseWheelPanel.setTitle(LangTool.getString("sa.mouseWheel"));

        mwCheck.setText(LangTool.getString("sa.activateMW"));
        mwCheck.setSelected("Yes".equals(getStringProperty("mouseWheel")));
    }

    @Override
    public void applyAttributes() {
        //  double click enter
        if (dceCheck.isSelected()) {
            fireStringPropertyChanged("doubleClick", "Yes");
        } else {
            fireStringPropertyChanged("doubleClick", "No");
        }

        if (mwCheck.isSelected()) {
            fireStringPropertyChanged("mouseWheel", "Yes");
        } else {
            fireStringPropertyChanged("mouseWheel", "No");
        }
    }

    @Override
    public BorderPane getView() {
        return view;
    }
}
