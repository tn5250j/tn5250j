package org.tn5250j.connectdialog;

import java.net.URL;
import java.util.ResourceBundle;

import org.tn5250j.TN5250jConstants;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.tools.LangTool;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ConnectionDialogController implements Initializable {
    @FXML
    Tab connectionsTab;
    @FXML
    Tab optionsTab;
    @FXML
    Tab loggingTab;
    @FXML
    Tab optionsAccessTab;
    @FXML
    Tab externalTab;
    @FXML
    Tab aboutTab;

    @FXML
    Label versionLabel;

    @FXML
    Button applyButton;
    @FXML
    Button cancelButton;
    @FXML
    Button connectButton;

    @FXML
    Pane loggingPane;
    @FXML
    Pane connectionsPane;
    @FXML
    Pane optionsPane;
    @FXML
    Pane optionsAccessPane;
    @FXML
    Pane externalProgramsPane;

    private LoggingPaneController loggingController;
    private ConnectionsPaneController connectionsController;
    private OptionsPaneController optionsController;
    private OptionsAccessPaneController optionsAccessController;
    private ExternalProgramsController externalProgramsController;

    private String connectKey;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        connectionsTab.setText(LangTool.getString("ss.labelConnections"));
        optionsTab.setText(LangTool.getString("ss.labelOptions1"));
        loggingTab.setText(LangTool.getString("ss.labelLogging"));
        optionsAccessTab.setText(LangTool.getString("ss.labelOptions2"));
        externalTab.setText(LangTool.getString("ss.labelExternal"));

        versionLabel.setText("Version: " + TN5250jConstants.VERSION_INFO);

        addOptButton(connectButton, "ss.optConnect", "CONNECT", false);
        addOptButton(applyButton, "ss.optApply", "APPLY", true);
        addOptButton(cancelButton, "ss.optCancel", "DONE", true);

        this.loggingController = (LoggingPaneController) loggingPane.getUserData();
        this.connectionsController = (ConnectionsPaneController) connectionsPane.getUserData();
        this.optionsController = (OptionsPaneController) optionsPane.getUserData();
        this.optionsAccessController = (OptionsAccessPaneController) optionsAccessPane.getUserData();
        this.externalProgramsController = (ExternalProgramsController) externalProgramsPane.getUserData();

        connectionsController.tableSelectedIndexProperty().addListener((src, old, value) -> updateConnecButton());
        connectionsTab.selectedProperty().addListener((src, old, value) -> updateConnecButton());

        connectionsPane.addEventHandler(ConnectEvent.TYPE, e -> doActionConnect());
        externalProgramsPane.addEventHandler(ConnectEvent.TYPE, e -> doActionConnect());

        setUpCloseListener();

        updateConnecButton();
    }

    private void setUpCloseListener() {
        UiUtils.setUpCloseListener(connectionsTab.getTabPane().sceneProperty(), e -> onClose());
    }

    private void addOptButton(final Button btn, final String title, final String opt, final boolean enabled) {
        UiUtils.addOptButton(btn, title, e -> invokeAction(opt)).setDisable(!enabled);
    }

    private void updateConnecButton() {
        connectButton.setDisable(!connectionsTab.isSelected() || connectionsController.tableSelectedIndexProperty().get() < 0);
    }

    private void doActionConnect() {
        if (connectionsController.getSelectedSessionName() == null) {
            return;
        }

        onSuccess();
        closeDialog();
    }
    private void onSuccess() {
        connectKey = connectionsController.getSelectedSessionName();
        saveProps();
    }

    private void onClose() {
        connectKey = null;
        saveProps();
    }

    /**
     * @return system name of selected session
     */
    public String getConnectKey() {
        return connectKey;
    }

    private void saveProps() {
        this.connectionsController.saveState();
        this.optionsController.saveState();
        this.loggingController.saveState();
        this.optionsAccessController.saveState();
        this.externalProgramsController.saveState();
    }

    private void invokeAction(final String action) {
        if ("DONE".equals(action)) {
            onSuccess();
            closeDialog();
        } else if ("CONNECT".equals(action)) {
            doActionConnect();
        } else if ("APPLY".equals(action)) {
            saveProps();
        }
    }

    private void closeDialog() {
        UiUtils.closeMe(connectionsTab.getTabPane().getScene());
    }
}
