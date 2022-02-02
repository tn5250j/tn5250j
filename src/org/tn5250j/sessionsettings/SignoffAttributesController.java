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
package org.tn5250j.sessionsettings;

import java.net.URL;
import java.util.ResourceBundle;

import org.tn5250j.SessionConfig;
import org.tn5250j.framework.tn5250.Rect;
import org.tn5250j.gui.TitledBorderedPane;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.tools.LangTool;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

class SignoffAttributesController extends AbstractAttributesController {
    @FXML
    BorderPane view;

    @FXML
    TitledBorderedPane parametersPane;
    @FXML
    CheckBox signoffCheck;

    @FXML
    TitledBorderedPane regionPane;
    @FXML
    Label fromRowLabel;
    @FXML
    TextField fromRow;
    @FXML
    Label fromColLabel;
    @FXML
    TextField fromCol;
    @FXML
    Label toRowLabel;
    @FXML
    TextField toRow;
    @FXML
    Label toColLabel;
    @FXML
    TextField toCol;

    SignoffAttributesController(final SessionConfig config) {
        super(config, "Signoff");
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        // define signoff confirmation panel
        parametersPane.setTitle(LangTool.getString("sa.titleSignoff"));

        signoffCheck.setText(LangTool.getString("sa.confirmSignoff"));
        signoffCheck.selectedProperty().addListener((src, old, value) -> signOffSelectionChanged(value));
        // check if signoff confirmation is to be checked
        final boolean signOffChecked = "Yes".equals(getStringProperty("confirmSignoff"));
        signoffCheck.setSelected(signOffChecked);

        regionPane.setTitle(LangTool.getString("sa.titleSignoffRegion"));

        fromRowLabel.setText(LangTool.getString("sa.fromRow"));
        fromColLabel.setText(LangTool.getString("sa.fromColumn"));
        toRowLabel.setText(LangTool.getString("sa.toRow"));
        toColLabel.setText(LangTool.getString("sa.toColumn"));

        loadRegion();
        signOffSelectionChanged(signOffChecked);
    }

    private void signOffSelectionChanged(final Boolean value) {
        final boolean disable = !Boolean.TRUE.equals(value);
        fromRow.setDisable(disable);
        fromCol.setDisable(disable);
        toRow.setDisable(disable);
        toCol.setDisable(disable);
    }

    private void loadRegion() {

        final Rect region = UiUtils.toRect(getRectangleProperty("signOnRegion"));

        if (region.x == 0)
            fromRow.setText("1");
        else
            fromRow.setText(Integer.toString(region.x));

        if (region.y == 0)
            fromCol.setText("1");
        else
            fromCol.setText(Integer.toString(region.y));

        if (region.width == 0)
            toRow.setText("24");
        else
            toRow.setText(Integer.toString(region.width));

        if (region.height == 0)
            toCol.setText("80");
        else
            toCol.setText(Integer.toString(region.height));

    }

    @Override
    public void applyAttributes() {

        if (signoffCheck.isSelected()) {
            fireStringPropertyChanged("confirmSignoff", "Yes");
        } else {
            fireStringPropertyChanged("confirmSignoff", "No");
        }

        final int x = Integer.parseInt(fromRow.getText());
        final int y = Integer.parseInt(fromCol.getText());
        final int width = Integer.parseInt(toRow.getText());
        final int height = Integer.parseInt(toCol.getText());

        setRectangleProperty("signOnRegion", new Rectangle2D(Math.max(0, x), Math.max(0, y),
                Math.min(24, width), Math.min(80, height)));
    }

    @Override
    public Region getView() {
        return view;
    }
}
