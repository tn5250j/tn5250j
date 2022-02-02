package org.tn5250j.sessionsettings;
/*
 * Title: CursorAttributesPanel
 * Copyright:   Copyright (c) 2001, 2002
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

class CursorAttributesController extends AbstractAttributesController {
    @FXML
    BorderPane view;

    @FXML
    TitledBorderedPane cursorSizePane;
    @FXML
    TitledBorderedPane rullerPane;
    @FXML
    TitledBorderedPane blinkPane;
    @FXML
    TitledBorderedPane offsetFromBottomPane;

    @FXML
    private RadioButton cFull;
    @FXML
    private RadioButton cHalf;
    @FXML
    private RadioButton cLine;

    @FXML
    private RadioButton chNone;
    @FXML
    private RadioButton chHorz;
    @FXML
    private RadioButton chVert;
    @FXML
    private RadioButton chCross;

    @FXML
    private CheckBox rulerFixed;

    @FXML
    private RadioButton blinkYes;
    @FXML
    private RadioButton blinkNo;

    @FXML
    private TextField cursorBottOffset;

    CursorAttributesController(final SessionConfig config) {
        super(config, "Cursor");
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        // define cursor size panel
        cursorSizePane.setTitle(LangTool.getString("sa.crsSize"));

        final ToggleGroup cursorSizeGroup = new ToggleGroup();
        initRadioButton(cFull, cursorSizeGroup, "sa.cFull");
        initRadioButton(cHalf, cursorSizeGroup, "sa.cHalf");
        initRadioButton(cLine, cursorSizeGroup, "sa.cLine");

        //init cursor size
        final String cursorSize = getStringProperty("cursorSize");
        if ("Full".equals(cursorSize)) {
            cFull.setSelected(true);
        } else if ("Half".equals(cursorSize)) {
            cHalf.setSelected(true);
        } else {
            cLine.setSelected(true);
        }

        // define cursor ruler panel
        rullerPane.setTitle(LangTool.getString("sa.crossHair"));

        final ToggleGroup crossHairGroup = new ToggleGroup();
        initRadioButton(chNone, crossHairGroup, "sa.chNone");
        initRadioButton(chHorz, crossHairGroup, "sa.chHorz");
        initRadioButton(chVert, crossHairGroup, "sa.chVert");
        initRadioButton(chCross, crossHairGroup, "sa.chCross");

        final String crossHair = getStringProperty("crossHair");
        if ("Horz".equals(crossHair)) {
            chHorz.setSelected(true);
        } else if ("Vert".equals(crossHair)) {
            chVert.setSelected(true);
        } else if ("Both".equals(crossHair)) {
            chCross.setSelected(true);
        } else {
            chNone.setSelected(true);
        }

        rulerFixed.setText("sa.rulerFixed");
        rulerFixed.setSelected("Yes".equals(getStringProperty("rulerFixed")));

        // define cursor ruler panel
        blinkPane.setTitle(LangTool.getString("sa.blinkCursor"));

        final ToggleGroup blinkGroup = new ToggleGroup();
        initRadioButton(blinkYes, blinkGroup, "sa.blinkYes");
        initRadioButton(blinkNo, blinkGroup, "sa.blinkNo");

        if ("Yes".equals(getStringProperty("cursorBlink"))) {
            blinkYes.setSelected(true);
        } else {
            blinkNo.setSelected(true);
        }

        // define bottom offset panel for cursor
        offsetFromBottomPane.setTitle(LangTool.getString("sa.curBottOffset"));

        cursorBottOffset.setPrefColumnCount(5);
        try {
            final int i = Integer.parseInt(getStringProperty("cursorBottOffset", "0"));
            cursorBottOffset.setText(Integer.toString(i));
        } catch (final NumberFormatException ne) {
            cursorBottOffset.setText("0");
        }
    }

    private void initRadioButton(final RadioButton button, final ToggleGroup group, final String langKey) {
        button.setText(LangTool.getString(langKey));
        button.setToggleGroup(group);
    }

    @Override
    public void applyAttributes() {

        if (cFull.isSelected()) {
            fireStringPropertyChanged("cursorSize", "Full");
        } else if (cHalf.isSelected()) {
            fireStringPropertyChanged("cursorSize", "Half");
        } else if (cLine.isSelected()) {
            fireStringPropertyChanged("cursorSize", "Line");
        }

        if (chNone.isSelected()) {
            fireStringPropertyChanged("crossHair", "None");
        } else if (chHorz.isSelected()) {
            fireStringPropertyChanged("crossHair", "Horz");
        } else if (chVert.isSelected()) {
            fireStringPropertyChanged("crossHair", "Vert");
        } else if (chCross.isSelected()) {
            fireStringPropertyChanged("crossHair", "Both");
        }

        if (rulerFixed.isSelected()) {
            fireStringPropertyChanged("rulerFixed", "No");
        } else {
            fireStringPropertyChanged("rulerFixed", "Yes");
        }

        fireStringPropertyChanged("cursorBottOffset", "cursorBottOffset.getText()");

        if (blinkYes.isSelected()) {
            fireStringPropertyChanged("cursorBlink", "Yes");
        } else {
            fireStringPropertyChanged("cursorBlink", "No");
        }
    }

    @Override
    public Region getView() {
        return view;
    }
}
