/*
 * @(#)WizardPage.java
 * Copyright:    Copyright (c) 2001
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 */
package org.tn5250j.gui;

import org.tn5250j.tools.LangTool;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Class to create and manage a Wizard style framework for you.
 */
public class WizardPage extends BorderPane {

    static public final int NO_BUTTONS = 0x00;
    static public final int PREVIOUS = 0x01;
    static public final int NEXT = 0x02;
    static public final int FINISH = 0x04;
    static public final int CANCEL = 0x08;
    static public final int HELP = 0x10;
    static public final int ALL = PREVIOUS | NEXT | FINISH | CANCEL | HELP;

    protected Button previousButton;
    protected Button nextButton;
    protected Button finishButton;
    protected Button cancelButton;
    protected Button helpButton;

    protected static final int GROUP_SPACING = 10;
    protected static final int MARGIN = 10;
    protected static final int BUTTON_SPACING = 5;

    // Pane returned by getContentPane.  This is the pane the
    // developer adds his/her code to.
    protected BorderPane contentPane;
    private String name;

    public WizardPage() {
        this(ALL);
    }

    public WizardPage(final int button_flags) {
        getStylesheets().add("/application.css");
        getStyleClass().add("opaque");

//      setLayout(new BorderLayout());
//      Box pageBox = Box.createVerticalBox();
        contentPane = new BorderPane();

        // add the pages contentpane to our wizard page
        setCenter(contentPane);

        // lets add some glue here but it still does not stop the separator from
        //  moving up and down.
        //add(Box.createGlue());

        // create the box for the buttons with an x-axis
        final VBox buttonsVbox = new VBox();
        buttonsVbox.setStyle("-fx-padding: 0.5em 0.5em 0.5em 0.5em;");
        buttonsVbox.setAlignment(Pos.BOTTOM_RIGHT);

        final HBox buttonPanel = new HBox();
        buttonPanel.setAlignment(Pos.BOTTOM_RIGHT);
        buttonPanel.setSpacing(5.);
        buttonsVbox.getChildren().add(buttonPanel);

        setBottom(buttonsVbox);

        if ((button_flags & PREVIOUS) != 0) {
            previousButton = createButton("wiz.previous");
            buttonPanel.getChildren().add(previousButton);
        }

        if ((button_flags & NEXT) != 0) {
            nextButton = createButton("wiz.next");
            buttonPanel.getChildren().add(nextButton);
        }

        if ((button_flags & FINISH) != 0) {
            finishButton = createButton("wiz.finish");
            buttonPanel.getChildren().add(finishButton);
        }

        if ((button_flags & CANCEL) != 0) {
            cancelButton = createButton("wiz.cancel");
            buttonPanel.getChildren().add(cancelButton);
        }

        if ((button_flags & HELP) != 0) {
            helpButton = createButton("wiz.help");
            buttonPanel.getChildren().add(helpButton);
        }
    }

    private Button createButton(final String labelKey) {
        final Button button = new Button(LangTool.getString(labelKey));
        // TODO add styling
        return button;
    }

    public Button getNextButton() {
        return nextButton;
    }

    public Button getPreviousButton() {
        return previousButton;
    }

    public Button getFinishButton() {
        return finishButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Button getHelpButton() {
        return helpButton;
    }

    /**
     * Overrides normal getContentPane to provide specially
     * managed area
     */
    public BorderPane getContentPane() {
        return contentPane;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
