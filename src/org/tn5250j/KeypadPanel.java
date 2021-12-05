package org.tn5250j;
/*
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
 * Company:
 *
 * @author Kenneth J. Pouncey
 * @version 0.4
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

import static org.tn5250j.tools.LangTool.getString;

import java.util.function.Consumer;

import org.tn5250j.keyboard.KeyMnemonic;
import org.tn5250j.keyboard.KeyMnemonicResolver;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;

class KeypadPanel extends BorderPane {

    private static final int MIN_FONT_SIZE = 3;
    private static final int NO_OF_BUTTONS_PER_ROW = 15;

    private final KeyMnemonicResolver keyMnemonicResolver = new KeyMnemonicResolver();
    private final SessionConfig.SessionConfiguration configuration;

    private Button[] buttons;

    KeypadPanel(final SessionConfig.SessionConfiguration sessionConfiguration) {
        this.configuration = sessionConfiguration;

        setBorder(Border.EMPTY);
        reInitializeButtons(configuration.getKeypadMnemonics());

        final ChangeListener<Number> sizeListener = (src, old, value) -> sizeChanged();
        this.widthProperty().addListener(sizeListener);
        this.heightProperty().addListener(sizeListener);

        //set font from config
        final Button b = buttons[0];
        final int size = Math.max(Math.round(configuration.getKeypadFontSize()), MIN_FONT_SIZE);

        setButtonsFont(new Font(b.getFont().getName(), size));
    }

    private void sizeChanged() {
        updateButtonFontSize(configuration.getKeypadFontSize());
    }

    void reInitializeButtons(final KeyMnemonic[] keyMnemonics) {
        getChildren().clear();

        this.buttons = createButtonsFromMnemonics(keyMnemonics);
        final GridPane grid = createButtonsPane(buttons);
        setCenter(grid);
    }

    private Button[] createButtonsFromMnemonics(final KeyMnemonic[] keyMnemonics) {
        final Insets minimalBorder = new Insets(2, 3, 3, 3);

        final Button[] buttons = new Button[keyMnemonics.length];
        for (int i = 0; i < keyMnemonics.length; i++) {
            buttons[i] = createButton(keyMnemonics[i], minimalBorder);
        }

        return buttons;
    }

    private GridPane createButtonsPane(final Button[] buttons) {
        final GridPane grid = new GridPane();

        final int columns = NO_OF_BUTTONS_PER_ROW;
        final int rows = (buttons.length + columns - 1) / columns;

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                final int index = row * columns + column;
                if (index >= buttons.length) {
                    break;
                }

                //add button or empty component
                final Button button = buttons[index];

                GridPane.setFillHeight(button, true);
                GridPane.setFillWidth(button, true);
                GridPane.setVgrow(button, Priority.ALWAYS);
                GridPane.setHgrow(button, Priority.ALWAYS);

                grid.add(button, column, row);
            }
        }
        return grid;
    }

    //TODO very complex implementation. Possible latter
    void updateButtonFontSize(final float fontSize) {
        final Button b = buttons[0];
        final String fontName = b.getFont().getName();
        final int size = Math.max(Math.round(fontSize), MIN_FONT_SIZE);

        setButtonsFont(new Font(fontName, size));
//
//        while (true) {
//            setButtonsFont(new Font(fontName, size));
//
//            if (!isTruncated()) {
//                break;
//            }
//            System.out.println(">>>>>");
//
//            size--;
//            if (size < MIN_FONT_SIZE) {
//                break;
//            }
//
//        }
    }

//    private boolean isTruncated() {
//        for (final Button button : buttons) {
//            for (final Node n : button.getChildrenUnmodifiable()) {
//                if (n instanceof Text && ((Text) n).getText().endsWith("...")) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    private void setButtonsFont(final Font font) {
        for (final Button b : buttons) {
            b.setFont(font);
        }
    }

    private Button createButton(final KeyMnemonic mnemonic, final Insets minimalBorder) {
        final String text = getString("KP_" + mnemonic.name(), keyMnemonicResolver.getDescription(mnemonic));
        return createButton(text, mnemonic.mnemonic);
    }

    private static Button createButton(final String text, final String mnemonic) {
        final Button b = new Button(text);
        b.setMaxSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        b.setScaleShape(true);
        b.setUserData(mnemonic);
        return b;
    }

    void addActionListener(final Consumer<String> actionlistener) {
        for (final Button button : buttons) {
            button.setOnAction(toActionListener(actionlistener));
        }
    }

    private EventHandler<ActionEvent> toActionListener(final Consumer<String> consumer) {
        return e -> {
            final Button button = (Button) e.getSource();
            final String action = (String) button.getUserData();
            consumer.accept(action);
        };
    }
}
