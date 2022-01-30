package org.tn5250j.spoolfile;
/**
 * Title: OutputQueueTabPanel.java
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

import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;

public class OutputQueueTabPanel extends GridPane implements QueueFilterInterface {
    RadioButton all;
    RadioButton select;
    TextField queue;
    TextField library;

    public OutputQueueTabPanel() {
        setHgap(5);
        setVgap(5);
        setStyle("-fx-padding: 0.5em 0 0 0;");

        all = new RadioButton("All");
        all.setSelected(true);

        select = new RadioButton("Select Output Queue");

        library = new TextField();
        library.setPrefColumnCount(10);
        library.textProperty().addListener((src, old, value) -> textChanged(library));

        queue = new TextField();
        queue.setPrefColumnCount(10);
        queue.textProperty().addListener((src, old, value) -> textChanged(queue));

        final ToggleGroup bg = new ToggleGroup();
        bg.getToggles().add(all);
        bg.getToggles().add(select);

        getChildren().add(all);
        getChildren().add(select);
        getChildren().add(queue);

        final Label queueLabel = new Label("Output queue library");
        getChildren().add(queueLabel);
        getChildren().add(library);


        setGridConstrains(all, 0, 0, 2);

        setGridConstrains(select, 1, 0, 1);
        setGridConstrains(queue, 1, 1, 1);

        setGridConstrains(queueLabel, 2, 0, 1);
        setGridConstrains(library, 2, 1, 1);
    }

    private void setGridConstrains(final Node node, final int row, final int column, final int colSpan) {
        GridPane.setColumnIndex(node, column);
        GridPane.setRowIndex(node, row);
        GridPane.setColumnSpan(node, colSpan);
        GridPane.setHalignment(node, HPos.LEFT);
    }

    /**
     * Reset to default value(s)
     */
    @Override
    public void reset() {

        library.setText("");
        queue.setText("");
        all.setSelected(true);

    }

    private void textChanged(final TextField textField) {
        final String text = textField.getText();
        if (text != null && !text.isEmpty()) {
            select.setSelected(true);
        }
    }

    public String getQueue() {
        if (all.isSelected())
            return "%ALL%";
        else
            return queue.getText().trim();
    }

    public String getLibrary() {

        if (all.isSelected())
            return "%ALL%";
        else
            return library.getText().trim();

    }
}
