/*
 * Title: ColorAttributesPanel
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
import java.util.Objects;
import java.util.ResourceBundle;

import org.tn5250j.SessionConfig;
import org.tn5250j.gui.TitledBorderedPane;
import org.tn5250j.tools.LangTool;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

class FontAttributesController extends AbstractAttributesController {
    @FXML
    BorderPane view;

    @FXML
    TitledBorderedPane fontPanel;
    @FXML
    ComboBox<String> fontsList;
    @FXML
    CheckBox useAntialias;

    @FXML
    TitledBorderedPane scalePanel;

    @FXML
    Label verticalScaleLabel;
    @FXML
    TextField verticalScale;
    @FXML
    Label horizontalScaleLabel;
    @FXML
    TextField horizontalScale;
    @FXML
    Label pointSizeLabel;
    @FXML
    TextField pointSize;

    FontAttributesController(final SessionConfig config) {
        super(config, "Fonts");
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

        fontPanel.setTitle(LangTool.getString("sa.font"));
        // fonts

        final String font = getStringProperty("font");

        for (final String fontName: Font.getFontNames()) {
            if (fontName.indexOf('.') < 0) {
                fontsList.getItems().add(fontName);
            }
        }

        fontsList.setValue(font);

        useAntialias.setText(LangTool.getString("sa.useAntialias"));
        useAntialias.setSelected(getBooleanProperty("useAntialias", true));

        scalePanel.setTitle(LangTool.getString("sa.scaleLabel"));

        pointSizeLabel.setText(LangTool.getString("sa.fixedPointSize"));
        horizontalScaleLabel.setText(LangTool.getString("sa.horScaleLabel"));
        verticalScaleLabel.setText(LangTool.getString("sa.vertScaleLabel"));

        if (hasProperty("fontScaleWidth"))
            horizontalScale.setText(getStringProperty("fontScaleWidth"));
        if (hasProperty("fontScaleHeight"))
            verticalScale.setText(getStringProperty("fontScaleHeight"));
        if (hasProperty("fontPointSize"))
            pointSize.setText(getStringProperty("fontPointSize"));
    }

    @Override
    public void applyAttributes() {
        if (!Objects.equals(getStringProperty("font"), fontsList.getValue())) {
            fireStringPropertyChanged("font", fontsList.getValue());
        }

        if (useAntialias.isSelected()) {
            fireStringPropertyChanged("useAntialias", "Yes");
        } else {
            fireStringPropertyChanged("useAntialias", "No");
        }

        fireStringPropertyChanged("fontScaleHeight", verticalScale.getText());
        fireStringPropertyChanged("fontScaleWidth", horizontalScale.getText());
        fireStringPropertyChanged("fontPointSize", pointSize.getText());
    }

    @Override
    public Region getView() {
        return view;
    }
}
