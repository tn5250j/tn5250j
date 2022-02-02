package org.tn5250j.sessionsettings;
/*
 * Title: KeypadAttributesPanel
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

import static org.tn5250j.SessionConfig.KEYPAD_FONT_SIZE_DEFAULT_VALUE;
import static org.tn5250j.SessionConfig.YES;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Consumer;

import org.tn5250j.SessionConfig;
import org.tn5250j.gui.TitledBorderedPane;
import org.tn5250j.keyboard.KeyMnemonic;
import org.tn5250j.keyboard.KeyMnemonicResolver;
import org.tn5250j.tools.LangTool;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import javafx.util.StringConverter;

class KeypadAttributesController extends AbstractAttributesController {
    @FXML
    BorderPane view;

    @FXML
    TitledBorderedPane enableKeyPadPanel;
    @FXML
    CheckBox keyPadEnable;
    @FXML
    Label fontSizeLabel;
    @FXML
    TextField fontSize;

    @FXML
    TitledBorderedPane visibleButtonsPanel;
    @FXML
    ListView<KeyMnemonic> availableButtonsList;
    @FXML
    ListView<KeyMnemonic> configuredButtonsList;

    @FXML
    Button toRightButton;
    @FXML
    Button toLeftButton;
    @FXML
    Button moveUpButton;
    @FXML
    Button moveDownButton;

    @FXML
    Button resetButton;

    KeypadAttributesController(final SessionConfig config) {
        super(config, "KP");
    }

    @Override
    public void applyAttributes() {
        fireStringPropertyChanged(SessionConfig.CONFIG_KEYPAD_ENABLED, keyPadEnable.isSelected() ? YES : SessionConfig.NO);
        fireStringPropertyChanged(SessionConfig.CONFIG_KEYPAD_FONT_SIZE, ensureValidFloatAsString(fontSize.getText()));
        changes.setKeypadMnemonicsAndFireChangeEvent(getConfiguredKeypadMnemonics());
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        initEnableKeyPadPanel();

        initCustomButtonConfigurationPanel();

        //init
        keyPadSelectionChanged(keyPadEnable.isSelected());
    }

    private void initEnableKeyPadPanel() {
        //enable key pad
        enableKeyPadPanel.setTitle(LangTool.getString("sa.kpp"));

        keyPadEnable.setText(LangTool.getString("sa.kpCheck"));
        keyPadEnable.setSelected(YES.equals(getStringProperty(SessionConfig.CONFIG_KEYPAD_ENABLED)));

        keyPadEnable.selectedProperty().addListener((src, old, value) -> keyPadSelectionChanged(value));

        //fontSize = new JTextField(Float.toString(KEYPAD_FONT_SIZE_DEFAULT_VALUE), 5);
        if (hasProperty(SessionConfig.CONFIG_KEYPAD_FONT_SIZE)) {
            fontSize.setText(getStringProperty(SessionConfig.CONFIG_KEYPAD_FONT_SIZE));
        }
        fontSizeLabel.setText(LangTool.getString("spool.labelOptsFontSize"));
    }

    private void initCustomButtonConfigurationPanel() {
        visibleButtonsPanel.setTitle(LangTool.getString("sa.kpVisibleButtons"));

        final KeyMnemonic[] configuredMnemonics = this.changes.getConfig().getKeypadMnemonics();
        final KeyMnemonic[] availableMnemonics = getAvailableAndNotYetConfiguredMnemonics(configuredMnemonics);

        availableButtonsList.getItems().addAll(Arrays.asList(availableMnemonics));
        availableButtonsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        configuredButtonsList.setCellFactory(mnemonicCellFactory);

        configuredButtonsList.getItems().addAll(Arrays.asList(configuredMnemonics));
        configuredButtonsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        configuredButtonsList.setCellFactory(mnemonicCellFactory);

        clearSelectionIfOtherSelected(availableButtonsList, configuredButtonsList);
        clearSelectionIfOtherSelected(configuredButtonsList, availableButtonsList);

        toRightButton.setOnAction(e -> moveFromTo(availableButtonsList, configuredButtonsList));
        setEnabledOnSelection(toRightButton, availableButtonsList);

        toLeftButton.setOnAction(e -> moveFromTo(configuredButtonsList, availableButtonsList));
        setEnabledOnSelection(toLeftButton, configuredButtonsList);

        moveUpButton.setOnAction(e -> moveUp());
        setupMoveEnablement(moveUpButton, this::updateMoveUpEnablement);

        moveDownButton.setOnAction(e -> moveDown());
        setupMoveEnablement(moveDownButton, this::updateMoveDownEnablement);

        resetButton.setText(LangTool.getString("sa.kpResetDefaults"));
        resetButton.setOnAction(e -> resetModelsToDefaultValues());

        toRightButton.setDisable(true);
        toLeftButton.setDisable(true);
        moveUpButton.setDisable(true);
        moveDownButton.setDisable(true);
    }

    private void setupMoveEnablement(final Button btn, final Consumer<ListView<KeyMnemonic>> consumer) {
        final ObservableList<KeyMnemonic> availableItems = availableButtonsList.getSelectionModel().getSelectedItems();
        final ObservableList<KeyMnemonic> configredItems = configuredButtonsList.getSelectionModel().getSelectedItems();

        final ListChangeListener<KeyMnemonic> listener = e -> {
            if (!availableItems.isEmpty() && !configredItems.isEmpty()) {
                //just ignore it is possible switching enablement from one to other list
                return;
            }
            if (availableItems.isEmpty() && configredItems.isEmpty()) {
                btn.setDisable(true);
                return;
            }

            consumer.accept(availableItems.isEmpty() ? configuredButtonsList : availableButtonsList);
        };

        availableItems.addListener(listener);
        configredItems.addListener(listener);
    }

    private void updateMoveUpEnablement(final ListView<KeyMnemonic> list) {
        final List<Integer> indexes = new LinkedList<>(list.getSelectionModel().getSelectedIndices());
        Collections.sort(indexes);

        moveUpButton.setDisable(indexes.get(0) < 1);
    }
    private void updateMoveDownEnablement(final ListView<KeyMnemonic> list) {
        final List<Integer> indexes = new LinkedList<>(list.getSelectionModel().getSelectedIndices());
        Collections.sort(indexes);

        moveDownButton.setDisable(indexes.get(indexes.size() -1) >= list.getItems().size() - 1);
    }

    private void clearSelectionIfOtherSelected(final ListView<KeyMnemonic> observable, final ListView<KeyMnemonic> observer) {
        final ListChangeListener<KeyMnemonic> listener = e -> {
            final MultipleSelectionModel<KeyMnemonic> selectionModel = observer.getSelectionModel();
            if (!e.getList().isEmpty()) {
                selectionModel.clearSelection();
            }
        };

        observable.getSelectionModel().getSelectedItems().addListener(listener);
    }

    private void setEnabledOnSelection(final Button button, final ListView<KeyMnemonic> list) {
        final ListChangeListener<KeyMnemonic> listener = e -> {
            button.setDisable(e.getList().isEmpty());
        };

        list.getSelectionModel().getSelectedItems().addListener(listener);
    }

    private void resetModelsToDefaultValues() {
        final ObservableList<KeyMnemonic> configuredModel = configuredButtonsList.getItems();
        configuredModel.clear();
        for (final KeyMnemonic mnemonic : changes.getConfig().getDefaultKeypadMnemonics()) {
            configuredModel.add(mnemonic);
        }

        final ObservableList<KeyMnemonic> availableModel = availableButtonsList.getItems();
        availableModel.clear();
        for (final KeyMnemonic mnemonic : getAvailableAndNotYetConfiguredMnemonics(changes.getConfig().getDefaultKeypadMnemonics())) {
            availableModel.add(mnemonic);
        }
    }

    private void moveUp() {
        final ObservableList<KeyMnemonic> items = configuredButtonsList.getSelectionModel().getSelectedItems();
        final int pos = items.indexOf(items.get(0));

        if (pos > 0) {
            moveSelectedToPosition(configuredButtonsList, pos - 1);
        }
    }

    private void moveDown() {
        final ObservableList<KeyMnemonic> items = configuredButtonsList.getSelectionModel().getSelectedItems();
        final int lastIndex = items.size() - 1;
        final int pos = items.indexOf(items.get(lastIndex));

        if (pos < lastIndex) {
            moveSelectedToPosition(configuredButtonsList, pos - items.size() + 2);
        }
    }

    private void moveSelectedToPosition(final ListView<KeyMnemonic> list, final int pos) {
        final List<KeyMnemonic> items = new LinkedList<>(list.getSelectionModel().getSelectedItems());
        list.getItems().removeAll(items);

        Collections.reverse(items);

        for (final KeyMnemonic keyMnemonic : items) {
            list.getItems().add(pos, keyMnemonic);
        }
    }

    private void moveFromTo(final ListView<KeyMnemonic> sourceList, final ListView<KeyMnemonic> destinationList) {
        final List<KeyMnemonic> selectedItems = new LinkedList<>(sourceList.getSelectionModel().getSelectedItems());
        sourceList.getItems().removeAll(selectedItems);
        destinationList.getItems().addAll(selectedItems);
        sourceList.getSelectionModel().clearSelection();
    }

    private KeyMnemonic[] getAvailableAndNotYetConfiguredMnemonics(final KeyMnemonic[] excludedMnemonics) {
        final List<KeyMnemonic> result = new ArrayList<KeyMnemonic>();
        final Set<KeyMnemonic> alreadyConfigured = new HashSet<KeyMnemonic>();
        Collections.addAll(alreadyConfigured, excludedMnemonics);

        for (final KeyMnemonic mnemonic : KeyMnemonic.values()) {
            if (!alreadyConfigured.contains(mnemonic)) result.add(mnemonic);
        }
        Collections.sort(result, new KeypadMnemonicDescriptionComparator());

        return result.toArray(new KeyMnemonic[result.size()]);
    }

    private KeyMnemonic[] getConfiguredKeypadMnemonics() {
        final ObservableList<KeyMnemonic> items = configuredButtonsList.getItems();
        return items.toArray(new KeyMnemonic[items.size()]);
    }

    private String ensureValidFloatAsString(final String value) {
        if (value != null) {
            try {
                final float v = Float.parseFloat(value.trim());
                return Float.toString(v);
            } catch (final NumberFormatException e) {
                // nothing, just save default
            }
        }
        return Float.toString(KEYPAD_FONT_SIZE_DEFAULT_VALUE);
    }

    private static class KeypadMnemonicDescriptionComparator implements Comparator<KeyMnemonic> {
        private final KeyMnemonicResolver resolver = new KeyMnemonicResolver();

        @Override
        public int compare(final KeyMnemonic mnemonic1, final KeyMnemonic mnemonic2) {
            return resolver.getDescription(mnemonic1).compareToIgnoreCase(resolver.getDescription(mnemonic2));
        }
    }

    private void keyPadSelectionChanged(final Boolean value) {
        fontSize.setDisable(!Boolean.FALSE.equals(value));
    }

    @Override
    public BorderPane getView() {
        return view;
    }

    private static Callback<ListView<KeyMnemonic>, ListCell<KeyMnemonic>> mnemonicCellFactory
            = new Callback<ListView<KeyMnemonic>, ListCell<KeyMnemonic>>() {

        private final KeyMnemonicResolver keyMnemonicResolver = new KeyMnemonicResolver();

        @Override
        public ListCell<KeyMnemonic> call(final ListView<KeyMnemonic> list) {
            final TextFieldListCell<KeyMnemonic> cell = new TextFieldListCell<KeyMnemonic>();
            cell.setConverter(new StringConverter<KeyMnemonic>() {
                @Override
                public String toString(final KeyMnemonic value) {
                    return keyMnemonicResolver.getDescription(value);
                }
                @Override
                public KeyMnemonic fromString(final String string) {
                    // not editable
                    return cell.getItem();
                }
            });
            return cell;
        }
    } ;
}
