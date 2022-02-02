package org.tn5250j.sessionsettings;
/*
 * Title: SignoffAttributesPanel
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

import java.net.URL;
import java.util.ResourceBundle;

import org.tn5250j.SessionConfig;
import org.tn5250j.gui.TitledBorderedPane;
import org.tn5250j.tools.LangTool;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

class OnConnectAttributesController extends AbstractAttributesController {
    @FXML
    BorderPane view;

    @FXML
    TitledBorderedPane titledPane;
    @FXML
    TextField connectMacro;

    OnConnectAttributesController(final SessionConfig config) {
        super(config, "OnConnect");
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        // define onConnect macro to run
        titledPane.setTitle(LangTool.getString("sa.connectMacro"));
        connectMacro.setText(getStringProperty("connectMacro"));
    }

    @Override
    public void applyAttributes() {
        fireStringPropertyChanged("connectMacro", connectMacro.getText());
    }

    @Override
    public Region getView() {
        return view;
    }
}
