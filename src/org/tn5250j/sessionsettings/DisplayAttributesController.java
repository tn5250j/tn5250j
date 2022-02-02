package org.tn5250j.sessionsettings;
/*
 * Title: DisplayAttributesPanel
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

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

class DisplayAttributesController extends AbstractAttributesController {

    private static final String NO = "No";
    private static final String YES = "Yes";

    @FXML
    BorderPane view;

    @FXML
    TitledBorderedPane columnSeparatorPanel;
    @FXML
    TitledBorderedPane showAttributresPanel;
    @FXML
    TitledBorderedPane guiPanel;

    private final ToggleGroup columnSeparatorButtons = new ToggleGroup();

    @FXML
    RadioButton csHide;
    @FXML
    RadioButton csLine;
    @FXML
    RadioButton csShortLine;
    @FXML
    RadioButton csDot;

    @FXML
    RadioButton saNormal;
    @FXML
    RadioButton saHex;

    private final ToggleGroup showAttributesButtons = new ToggleGroup();

    @FXML
    CheckBox guiCheck;
    @FXML
    CheckBox guiShowUnderline;

    DisplayAttributesController(final SessionConfig config) {
        super(config, "Display");
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        // define column separator panel
        columnSeparatorPanel.setTitle(LangTool.getString("sa.cs"));

        initRadioButton(csHide, columnSeparatorButtons, "sa.csHide", "Hide");
        initRadioButton(csLine, columnSeparatorButtons, "sa.csLine", "Line");
        initRadioButton(csDot, columnSeparatorButtons, "sa.csDot", "Dot");
        initRadioButton(csShortLine, columnSeparatorButtons, "sa.csShortLine", "ShortLine");

        // Group the radio buttons.
        selectToggleByUserData(columnSeparatorButtons, getStringProperty("colSeparator"));

        // define show attributs panel
        showAttributresPanel.setTitle(LangTool.getString("sa.showAttr"));

        initRadioButton(saNormal, showAttributesButtons, "sa.showNormal", "Normal");
        initRadioButton(saHex, showAttributesButtons, "sa.showHex", "Hex");

        selectToggleByUserData(showAttributesButtons, getStringProperty("showAttr"));

        // define gui panel
        guiPanel.setTitle(LangTool.getString("sa.cgp"));

        guiCheck.setText(LangTool.getString("sa.guiCheck"));
        guiCheck.setSelected(YES.equals(getStringProperty("guiInterface")));

        // since this is a new property added then it might not exist in existing
        //    profiles and it should be defaulted to yes.
        guiShowUnderline.setText(LangTool.getString("sa.guiShowUnderline"));;
        guiShowUnderline.setSelected(YES.equals(getStringProperty("guiShowUnderline")));
    }

    private void selectToggleByUserData(final ToggleGroup group, final String userData) {
        if (userData == null) {
            return;
        }

        final ObservableList<Toggle> toggles = group.getToggles();
        for (final Toggle toggle : toggles) {
            if (userData.equals(toggle.getUserData())) {
                group.selectToggle(toggle);
                return;
            }
        }

        group.selectToggle(toggles.get(0));
    }

    private void initRadioButton(final RadioButton button, final ToggleGroup group, final String langKey,
            final String action) {
        button.setText(LangTool.getString(langKey));
        button.setToggleGroup(group);
        button.setUserData(action);
    }

    @Override
    public void applyAttributes() {
        // column separator
        final String columnSeparator = (String) columnSeparatorButtons.getSelectedToggle().getUserData();
        changes.firePropertyChange(this, "colSeparator",
                getStringProperty("colSeparator"), columnSeparator);
        setProperty("colSeparator", columnSeparator);

        final String showAttr = (String) showAttributesButtons.getSelectedToggle().getUserData();
        changes.firePropertyChange(
                this,
                "showAttr",
                getStringProperty("showAttr"),
                showAttr);
        setProperty("showAttr", showAttr);

        final String guiCheckValue = guiCheck.isSelected() ? YES: NO;
        changes.firePropertyChange(
                this,
                "guiInterface",
                getStringProperty("guiInterface"),
                guiCheckValue);
        setProperty("guiInterface", guiCheckValue);

        final String guiShowUnderlineValue = guiShowUnderline.isSelected() ? YES: NO;
        changes.firePropertyChange(
                this, "guiShowUnderline",
                getStringProperty("guiShowUnderline"), guiShowUnderlineValue);
        setProperty("guiShowUnderline", guiShowUnderlineValue);

    }

    @Override
    public Region getView() {
        return view;
    }
}
