/**
 *
 */
package org.tn5250j.connectdialog;

import static org.tn5250j.gui.UiUtils.addOptButton;

import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.prefs.Preferences;

import org.tn5250j.gui.UiUtils;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.tools.LangTool;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ConnectionsPaneController implements Initializable {

    private static final String EMUL_DEFAULT_PROPERTY = "emul.default";
    private static final String USER_PREF_LAST_SESSION = "last_session";
    private static final String EDIT_ACTION = "EDIT";
    private static final String REMOVE_ACTION = "REMOVE";
    private static final String ADD_ACTION = "ADD";

    @FXML
    Button addButton;
    @FXML
    Button removeButton;
    @FXML
    Button editButton;

    @FXML
    Pane view;

    @FXML
    private TableView<SessionsDataModel> table;

    private final ObservableList<SessionsDataModel> data = FXCollections.observableArrayList();

    private final Properties properties;

    public ConnectionsPaneController() {
        properties = ConfigureFactory.getInstance().getProperties(
                ConfigureFactory.SESSIONS);
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        view.setUserData(this);

        addOptButton(addButton, "ss.optAdd", actionListener(ADD_ACTION)).setDisable(false);
        addOptButton(removeButton, "ss.optDelete", actionListener(REMOVE_ACTION)).setDisable(false);
        addOptButton(editButton, "ss.optEdit", actionListener(EDIT_ACTION)).setDisable(false);

        final TableColumn<SessionsDataModel, String> colA = createTableColumn(
                model -> model.getNameProperty(), "conf.tableColA", 250);
        final TableColumn<SessionsDataModel, String> colB = createTableColumn(
                model -> model.getHostProperty(), "conf.tableColB", 250);
        final TableColumn<SessionsDataModel, Boolean> colC = createDefaultSessionColumn(
                "conf.tableColC", 65);

        table.setEditable(true);
        table.getColumns().addAll(Arrays.asList(colA, colB, colC));

        // get an instance of our table model
        parseConnections();

        table.setItems(data);
        table.sort();
        table.getSelectionModel().selectionModeProperty().set(SelectionMode.SINGLE);

        table.addEventHandler(KeyEvent.KEY_RELEASED, this::tableKeyReleased);
        table.addEventHandler(MouseEvent.MOUSE_CLICKED, this::tableMouseClicked);
        tableSelectedIndexProperty().addListener(this::tableSelectionChanged);

        setDefaultSelection();
    }

    private void setDefaultSelection() {
        SessionsDataModel selected = data.stream().filter(m -> Boolean.TRUE.equals(m.getDeflt())).findAny().orElse(null);
        if (selected == null) {
            final Preferences userpref = Preferences.userNodeForPackage(SessionsDataModel.class);
            final String lastSelected = userpref.get(USER_PREF_LAST_SESSION, null);
            if (lastSelected != null) {
                selected = data.stream().filter(m -> m.getName().equals(lastSelected)).findAny().orElse(null);
            }
        }
        if (selected != null) {
            table.getSelectionModel().select(selected);
        }
    }

    void saveState() {
        final SessionsDataModel selectedItem = getSelectedItem();
        if (selectedItem != null) {
            final Preferences userpref = Preferences.userNodeForPackage(SessionsDataModel.class);
            userpref.put(USER_PREF_LAST_SESSION, selectedItem.getName());
        }

        ConfigureFactory.getInstance().saveSettings(ConfigureFactory.SESSIONS, "------ Session Information --------");
    }

    private void tableMouseClicked(final MouseEvent e) {
        if (e.getClickCount() > 1) {
            this.view.fireEvent(new ConnectEvent());
        }
    }

    private void tableKeyReleased(final KeyEvent e) {
        if (KeyCode.ENTER == e.getCode()) {
            this.view.fireEvent(new ConnectEvent());
        }
    }

    ReadOnlyIntegerProperty tableSelectedIndexProperty() {
        return table.getSelectionModel().selectedIndexProperty();
    }

    private void tableSelectionChanged(final ObservableValue<? extends Number> observable,
            final Number oldValue, final Number newValue) {
        final boolean isEmpty = newValue == null || newValue.intValue() == -1;
        editButton.setDisable(isEmpty);
        removeButton.setDisable(isEmpty);
    }

    private TableColumn<SessionsDataModel, String> createTableColumn(
            final Function<SessionsDataModel, ObservableValue<String>> accessor,
            final String title, final int prefSize) {
        final TableColumn<SessionsDataModel, String> col = new TableColumn<>(LangTool.getString(title));
        col.setPrefWidth(prefSize);
        col.setEditable(false);
        col.setCellValueFactory(t -> accessor.apply(t.getValue()));
        return col;
    }

    private TableColumn<SessionsDataModel, Boolean> createDefaultSessionColumn(
            final String title, final int prefSize) {
        final TableColumn<SessionsDataModel, Boolean> col = new TableColumn<>(LangTool.getString(title));
        col.setPrefWidth(prefSize);
        col.setEditable(true);
        col.setCellValueFactory(new PropertyValueFactory<>("deflt"));
        col.setCellFactory(c -> createTableCell());
        return col;
    }

    private CheckBoxTableCell<SessionsDataModel, Boolean> createTableCell() {
        final Callback<Integer, ObservableValue<Boolean>> callback = e -> {
            final SessionsDataModel model = data.get(e);
            return model == null ? null : model.getDefltProperty();
        };
        return new CheckBoxTableCell<>(callback);
    }

    private void defaultSessionPropertyChanged(final String sessionName) {
        properties.setProperty(EMUL_DEFAULT_PROPERTY, sessionName);
        for (final SessionsDataModel model : data) {
            if (!model.getName().equals(sessionName)) {
                model.setDeflt(false);
            }
        }
    }

    private void parseConnections() {
        final String defaultSessionName = properties.getProperty(EMUL_DEFAULT_PROPERTY, "");

        final Enumeration<Object> e = properties.keys();
        while (e.hasMoreElements()) {
            final String ses = (String) e.nextElement();
            if (!ses.startsWith("emul.")) {
                final String[] args = EditSessionDialogController.parseArgs(properties.getProperty(ses));
                final boolean deflt = ses.equals(defaultSessionName);

                final SessionsDataModel model = new SessionsDataModel(ses, args[0], deflt);
                model.setDefaultStateConsumer(this::defaultSessionPropertyChanged);
                data.add(model);
            }
        }
    }

    private EventHandler<ActionEvent> actionListener(final String action) {
        return e -> invokeAction(action);
    }

    private void invokeAction(final String action) {
        if (ADD_ACTION.equals(action)) {
            final String systemName = showSessionDialog(null);
            reloadConnections();
            final int index = indexOfSession(systemName);
            if (index > -1) {
                table.getSelectionModel().select(index);
            }
            table.requestFocus();

        } else if (REMOVE_ACTION.equals(action)) {
            final SessionsDataModel model = getSelectedItem();
            if (model != null) {
                removeEntry(model);
            }
            editButton.setDisable(true);
            removeButton.setDisable(true);
        } else if (EDIT_ACTION.equals(action)) {
            final SessionsDataModel model = getSelectedItem();
            if (model != null) {
                showSessionDialog(model.getName());
                reloadConnections();
                table.requestFocus();
            }
        }
    }

    /**
     * @param name system name.
     */
    private String showSessionDialog(final String name) {
        final String title = (name == null)
                ? LangTool.getString("conf.addEntryATitle")
                : LangTool.getString("conf.addEntryETitle");

        final AtomicReference<EditSessionDialogController> ref = new AtomicReference<>();
        UiUtils.showDialog(view.getScene().getWindow(), "/fxml/EditSessionDialog.fxml", title,
                c -> {
                    ref.set((EditSessionDialogController) c);
                    ref.get().setSystemName(name);
                });

        return ref.get().getSystemName();
    }

    private SessionsDataModel getSelectedItem() {
        return table.getSelectionModel().selectedItemProperty().get();
    }

    private void reloadConnections() {
        data.clear();
        parseConnections();
    }

    private void removeEntry(final SessionsDataModel model) {
        properties.remove(model.getName());
        reloadConnections();
    }

    private int indexOfSession(final String systemName) {
        int index = 0;
        for (final SessionsDataModel model : data) {
            if (model.getName().equals(systemName))  {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * @return system name of selected session.
     */
    public String getSelectedSessionName() {
        final SessionsDataModel item = getSelectedItem();
        return item == null ? null : item.getName();
    }
}
