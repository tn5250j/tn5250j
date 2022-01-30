/**
 * Title: SpoolNamePanel.java
 * Copyright:   Copyright (c) 2002
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
package org.tn5250j.spoolfile;

import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;

public class SpoolNameTabPanel extends GridPane implements QueueFilterInterface {
    private final RadioButton all;
    private final RadioButton select;
    private final TextField spoolName;

    public SpoolNameTabPanel() {
        setHgap(5);
        setVgap(5);
        setStyle("-fx-padding: 0.5em 0 0 0;");

        all = new RadioButton("All");
        all.setSelected(true);

        select = new RadioButton("Spool Name");
        select.setSelected(false);

        spoolName = new TextField();
        spoolName.setPrefColumnCount(15);
        spoolName.textProperty().addListener((src, old, value) -> textChanged(spoolName));

        final ToggleGroup bg = new ToggleGroup();
        bg.getToggles().add(all);
        bg.getToggles().add(select);

        getChildren().add(all);

        getChildren().add(select);
        getChildren().add(spoolName);

        setGridConstrains(all, 0, 0, 2);
        setGridConstrains(select, 1, 0, 1);
        setGridConstrains(spoolName, 1, 1, 1);
    }

    private void setGridConstrains(final Node node, final int row, final int column, final int colSpan) {
        GridPane.setColumnIndex(node, column);
        GridPane.setRowIndex(node, row);
        GridPane.setColumnSpan(node, colSpan);
        GridPane.setHalignment(node, HPos.LEFT);
    }

    private void textChanged(final TextField textField) {
        final String text = textField.getText();
        if (text != null && !text.isEmpty()) {
            select.setSelected(true);
        }
    }

    /**
     * Reset to default value(s)
     */
    @Override
    public void reset() {

//      spoolName.setEnabled(false);
        spoolName.setText("");
        all.setSelected(true);

    }

    public String getSpoolName() {
        if (all.isSelected())
            return "";
        else
            return spoolName.getText().trim();
    }

    public void setSpoolName(final String filter) {

        spoolName.setText(filter);
    }
}
