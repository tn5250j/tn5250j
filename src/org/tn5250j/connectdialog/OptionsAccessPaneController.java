/**
 *
 */
package org.tn5250j.connectdialog;

import java.net.URL;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.interfaces.OptionAccessFactory;
import org.tn5250j.keyboard.KeyMnemonicResolver;
import org.tn5250j.tools.DESSHA1;
import org.tn5250j.tools.LangTool;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class OptionsAccessPaneController implements Initializable {

    private static final TN5250jLogger LOG = TN5250jLogFactory.getLogger(OptionsAccessPaneController.class);

    private static final String CRYPTO_KEY = "tn5205j";
    private static final String ACCESS_DIGEST_PROPERTY = "emul.accessDigest";

    private static final String SELECT_ITEM = ">";
    private static final String SELECT_ALL = ">>|";
    private static final String DESELECT_ITEM = "<";
    private static final String DESELECT_ALL = "|<<";

    @FXML
    Button setPassButton;
    @FXML
    PasswordField password;

    @FXML
    ListView<MnemonicItem> sourceList;
    @FXML
    ListView<MnemonicItem> restrictedList;

    @FXML
    Button selectItemButton;
    @FXML
    Button selectAllButton;
    @FXML
    Button deselectItemButton;
    @FXML
    Button deselectAllButton;

    @FXML
    Label sourceListTitle;
    @FXML
    Label restrictedListTitle;

    @FXML
    Pane view;

    private final Properties properties;

    public OptionsAccessPaneController() {
        properties = ConfigureFactory.getInstance().getProperties(
                ConfigureFactory.SESSIONS);
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        view.setUserData(this);
        setPassButton.setText(LangTool.getString("ss.labelSetPass"));
        setPassButton.setDisable(isAccessDigestUsed());
        setPassButton.setOnAction(e -> setPassword());

        sourceListTitle.setText(LangTool.getString("ss.labelActive"));
        restrictedListTitle.setText(LangTool.getString("ss.labelRestricted"));

        initButton(selectItemButton, SELECT_ITEM, "oaa.AddSelected", this::selectItem);
        initButton(selectAllButton, SELECT_ALL, "oaa.AddAll", this::selectAll);
        initButton(deselectItemButton, DESELECT_ITEM, "oaa.RemoveSelected", this::deselectItem);
        initButton(deselectAllButton, DESELECT_ALL, "oaa.RemoveAll", this::deselectAll);

        password.textProperty().addListener((src, old, value) -> passwordTextChanged());

        loadOptions();

        //source list
        initListView(sourceList, this::sourceListMouseClicked);
        initListView(restrictedList, this::targetListMouseClicked);

        sortLists();
        setAccessOptionsEnabled(!isAccessDigestUsed());
    }

    private void initButton(final Button button, final String text, final String toolTipKey, final Runnable listener) {
        button.setText(text);
        button.setTooltip(new Tooltip(LangTool.getString(toolTipKey)));
        button.setOnAction(e -> listener.run());
        button.setDisable(true);
    }

    private void initListView(final ListView<MnemonicItem> list, final EventHandler<MouseEvent> listener) {
        list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        final ListChangeListener<MnemonicItem> selectionListener = c -> optionsSelectionChanged();
        list.getSelectionModel().getSelectedItems().addListener(selectionListener);
        list.addEventFilter(MouseEvent.MOUSE_CLICKED, listener);
        list.setEditable(false);

        list.setCellFactory(cb -> new MnemonicListViewItem());
    }

    private void loadOptions() {
        final KeyMnemonicResolver keyMnemonicResolver = new KeyMnemonicResolver();
        final String[] options = keyMnemonicResolver.getMnemonicsSorted();

        for (int i = 0; i < options.length; i++) {
            final String option = options[i];
            final MnemonicItem item = new MnemonicItem(option, LangTool.getString("key." + option));
            if (!OptionAccessFactory.getInstance().isValidOption(option)) {
                restrictedList.getItems().add(item);
            } else {
                sourceList.getItems().add(item);
            }
        }
    }

    private void sourceListMouseClicked(final MouseEvent e) {
        if (e.getClickCount() > 1) {
            selectItem();
        }
    }
    private void targetListMouseClicked(final MouseEvent e) {
        if (e.getClickCount() > 1) {
            deselectItem();
        }
    }

    private boolean isAccessDigestUsed() {
        return properties.getProperty(ACCESS_DIGEST_PROPERTY) != null;
    }

    private void setPassword() {
        final String pwd = password.getText();
        if (pwd != null && pwd.length() > 0) {
            try {
                final DESSHA1 sha = new DESSHA1();
                properties.setProperty(ACCESS_DIGEST_PROPERTY, sha.digest(new String(pwd), CRYPTO_KEY));
            } catch (final Exception ex) {
            }
        }
    }

    private void passwordTextChanged() {
        final String pwd = password.getText();
        if (pwd != null && pwd.length() > 0) {
            doSomethingEntered();
        } else {
            doNothingEntered();
        }
    }

    private void doSomethingEntered() {
        if (isAccessDigestUsed()) {
            try {
                final DESSHA1 sha = new DESSHA1();
                if (properties.getProperty("emul.accessDigest").equals(sha.digest(password.getText(), CRYPTO_KEY))) {
                    setAccessOptionsEnabled(true);
                    setPassButton.setDisable(false);
                }
            } catch (final Exception ex) {
                LOG.warn(ex.getMessage(), ex);
            }
        }
    }

    private void doNothingEntered() {
        setAccessOptionsEnabled(false);
        setPassButton.setDisable(true);
    }

    private void setAccessOptionsEnabled(final boolean recommended) {
        sourceList.setDisable(!recommended);
        restrictedList.setDisable(!recommended);

        updateButtons(recommended);
    }

    private void sortLists() {
        sourceList.getItems().sort(Comparator.naturalOrder());
        restrictedList.getItems().sort(Comparator.naturalOrder());
    }

    /*
     * Enables (or disables) the buttons.
     */
    private void updateButtons(final boolean recommended) {
        if (!recommended) {
            selectItemButton.setDisable(true);
            selectAllButton.setDisable(true);
            deselectItemButton.setDisable(true);
            deselectAllButton.setDisable(true);
        } else {
            selectItemButton.setDisable(sourceList.getSelectionModel().isEmpty());
            selectAllButton.setDisable(sourceList.getItems().isEmpty());
            deselectItemButton.setDisable(restrictedList.getSelectionModel().isEmpty());
            deselectAllButton.setDisable(restrictedList.getItems().isEmpty());
        }
    }

    private void optionsSelectionChanged() {
        updateButtons(true);
    }

    /**
     * Moves the items selected in the left list to the right list.
     */
    private void selectItem() {
        move(sourceList, restrictedList, sourceList.getSelectionModel().getSelectedItems());
    }

    /**
     * Moves all items from the left list to the right list.
     */
    private void selectAll() {
        move(sourceList, restrictedList, sourceList.getItems());
    }

    /**
     * Moves the items selected in the right list to the left list.
     */
    private void deselectItem() {
        move(restrictedList, sourceList, restrictedList.getSelectionModel().getSelectedItems());
    }

    /**
     * Moves all items from the right list to the left list.
     */
    private void deselectAll() {
        move(restrictedList, sourceList, restrictedList.getItems());
    }

    private void move(final ListView<MnemonicItem> from, final ListView<MnemonicItem> to,
            final List<MnemonicItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        final List<MnemonicItem> toMove = new LinkedList<>(items);
        from.getItems().removeAll(toMove);
        to.getItems().addAll(toMove);

        restrictedList.getItems().sort(Comparator.naturalOrder());
        sourceList.getItems().sort(Comparator.naturalOrder());
        updateButtons(true);
    }

    void saveState() {
        final StringBuilder sb = new StringBuilder();
        for (final MnemonicItem item : this.restrictedList.getItems()) {
            if (sb.length() > 0) {
                sb.append(';');
            }
            sb.append(item.getName());
        }
        properties.setProperty("emul.restricted", sb.toString());
        OptionAccessFactory.getInstance().reload();
    }
}
