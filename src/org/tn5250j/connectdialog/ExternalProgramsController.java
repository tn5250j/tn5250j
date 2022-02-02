/**
 *
 */
package org.tn5250j.connectdialog;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;
import java.util.ResourceBundle;

import org.tn5250j.ExternalProgramConfig;
import org.tn5250j.gui.TitledBorderedPane;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.tools.LangTool;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ExternalProgramsController implements Initializable {

    private static final String EXTERNAL_PROGRAM_FXML = "/fxml/ExternalProgramDialog.fxml";

    private static final String PROTOCOL_MAILTO_PROPERTY = "emul.protocol.mailto";
    private static final String PROTOCOL_HTTP_PROPERTY = "emul.protocol.http";

    @FXML
    TitledBorderedPane externalPrograms;
    @FXML
    TitledBorderedPane cExternalPrograms;

    @FXML
    Label browserLabel;
    @FXML
    TextField browser;
    @FXML
    Button selectBrowser;

    @FXML
    Label mailerLabel;
    @FXML
    TextField mailer;
    @FXML
    Button selectMailer;

    @FXML
    TableView<ExternalProgram> externals;

    @FXML
    Button cEditButton;
    @FXML
    Button cRemoveButton;
    @FXML
    Button cAddButton;

    @FXML
    Pane view;

    private final Properties properties;
    private final ExternalProgramConfig externalProgramConfig;

    public ExternalProgramsController() {
        properties = ConfigureFactory.getInstance().getProperties(
                ConfigureFactory.SESSIONS);
        externalProgramConfig = ExternalProgramConfig.getInstance();
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        view.setUserData(this);

        externalPrograms.setTitle(LangTool.getString("external.title"));
        browserLabel.setText(LangTool.getString("external.http"));
        if (properties.containsKey(PROTOCOL_HTTP_PROPERTY)) {
            browser.setText(properties.getProperty(PROTOCOL_HTTP_PROPERTY));
        }

        mailerLabel.setText(LangTool.getString("external.mailto"));
        if (properties.containsKey(PROTOCOL_MAILTO_PROPERTY)) {
            mailer.setText(properties.getProperty(PROTOCOL_MAILTO_PROPERTY));
        }

        final TableColumn<ExternalProgram, String> nameColumn = createTableColumn(
                "name", "customized.name");
        final TableColumn<ExternalProgram, String> windowsColumn = createTableColumn(
                "wCommand", "customized.window");
        final TableColumn<ExternalProgram, String> linuxColumn = createTableColumn(
                "uCommand", "customized.unix");

        externals.setEditable(false);
        externals.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        externals.getColumns().addAll(Arrays.asList(nameColumn, windowsColumn, linuxColumn));

        externals.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        resetSorted();
        externals.addEventHandler(MouseEvent.MOUSE_CLICKED, this::mouseClickedOnTable);
        final ListChangeListener<ExternalProgram> listener = ch -> externalsSelectionChanged();
        externals.getSelectionModel().getSelectedItems().addListener(listener);

        // Setup panels
        cExternalPrograms.setTitle(LangTool.getString("customized.title"));

        // add the option buttons
        addOptButton(cAddButton, "ss.optAdd", "cADD", true);
        addOptButton(cRemoveButton, "ss.optDelete", "cREMOVE", false);
        addOptButton(cEditButton, "ss.optEdit", "cEDIT", false);

        addOpenFileListener(selectBrowser, browser, "external.title");
        addOpenFileListener(selectMailer, mailer, "external.mailto");
    }

    private <T> TableColumn<ExternalProgram, T> createTableColumn(
            final String propertyName, final String title) {
        final TableColumn<ExternalProgram, T> colC = new TableColumn<>(LangTool.getString(title));
        colC.setEditable(false);
        colC.setCellValueFactory(new PropertyValueFactory<ExternalProgram, T>(propertyName));
        return colC;
    }

    private void addOpenFileListener(final Button button, final TextField target, final String titleKey) {
        button.setOnAction(e -> {
            final FileChooser chooser = new FileChooser();
            chooser.setTitle(LangTool.getString(titleKey));

            final String old = target.getText();
            if (old != null && old.length() > 0) {
                chooser.setInitialFileName(old);
            }

            final File file = chooser.showOpenDialog(view.getScene().getWindow());
            if (file != null) {
                target.setText(file.getAbsolutePath());
            }
        });
    }

    private void addOptButton(final Button button, final String labelKey, final String action, final boolean enabled) {
        UiUtils.addOptButton(button, labelKey, e -> buttonClicked(action)).setDisable(!enabled);
    }

    private void buttonClicked(final String action) {
        if ("cADD".equals(action)) {
            showDialog(null);
            resetSorted();
            externals.requestFocus();
        } else if ("cEDIT".equals(action)) {
            final ExternalProgram selected = externals.getSelectionModel().getSelectedItem();
            showDialog(selected);
            resetSorted();
            externals.requestFocus();
        } else if ("cREMOVE".equals(action)) {
            removeExternalProgram();
            externalsSelectionChanged();
        }
    }

    private void showDialog(final ExternalProgram p) {
        final String title = LangTool.getString(p == null ? "customized.addEntryTitle" : "customized.editEntryTitle");
        UiUtils.showDialog(
                view.getScene().getWindow(),
                UiUtils.createLoader(EXTERNAL_PROGRAM_FXML),
                title,
                c -> ((ExternalProgramDialogController) c).setProgram(p));
    }

    private void removeExternalProgram() {
        final ExternalProgram item = externals.getSelectionModel().getSelectedItem();
        if (item == null) {
            return;
        }

        externals.getItems().remove(item);
        externalProgramConfig.remove(item.getName());
    }

    private void mouseClickedOnTable(final MouseEvent e) {
        if (e.getClickCount() == 2) {
            view.fireEvent(new ConnectEvent());
        }
    }

    private void externalsSelectionChanged() {
        final boolean isSelectionEmpty = externals.getSelectionModel().isEmpty();
        cEditButton.setDisable(isSelectionEmpty);
        cRemoveButton.setDisable(isSelectionEmpty);
    }

    void saveState() {
        externalProgramConfig.save();
        setProperty(PROTOCOL_HTTP_PROPERTY, browser.getText());
        setProperty(PROTOCOL_MAILTO_PROPERTY, mailer.getText());
    }

    private void setProperty(final String key, final String origin) {
        final String value = origin == null ? null : origin.trim();
        if (value != null && value.length() > 0) {
            properties.setProperty(key, value);
        } else {
            properties.remove(key);
        }
    }

    private void resetSorted() {
        externals.getItems().clear();
        externals.getItems().addAll(externalProgramConfig.getPrograms());
        externals.sort();
    }
}
