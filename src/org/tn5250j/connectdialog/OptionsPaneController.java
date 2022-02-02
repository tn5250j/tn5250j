/**
 *
 */
package org.tn5250j.connectdialog;

import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import org.tn5250j.gui.TitledBorderedPane;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.tools.LangTool;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class OptionsPaneController implements Initializable {
    private static final String SHOW_CONNECT_DIALOG_OPTION = "emul.showConnectDialog";
    private static final String START_LAST_VIEW_OPTION = "emul.startLastView";
    private static final String HIDE_TAB_BAR_OPTION = "emul.hideTabBar";

    @FXML
    CheckBox hideTabBar;
    @FXML
    RadioButton intTABS;
    @FXML
    CheckBox showMe;
    @FXML
    CheckBox lastView;

    @FXML
    TitledBorderedPane sessionPresentationPanel;
    @FXML
    TitledBorderedPane startupPanel;

    @FXML
    Pane view;

    private final Properties properties;

    public OptionsPaneController() {
        properties = ConfigureFactory.getInstance().getProperties(
                ConfigureFactory.SESSIONS);
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        view.setUserData(this);

        sessionPresentationPanel.setTitle(LangTool.getString("conf.labelPresentation"));
        startupPanel.setTitle(LangTool.getString("ss.labelStartup"));

        // create the checkbox for hiding the tab bar when only one tab exists
        hideTabBar.setText(LangTool.getString("conf.labelHideTabBar"));
        hideTabBar.setSelected("yes".equalsIgnoreCase(properties.getProperty(HIDE_TAB_BAR_OPTION)));
        hideTabBar.selectedProperty().addListener((src, old, value) -> hideTabBar_itemStateChanged(value));

        final ToggleGroup intGroup = new ToggleGroup();
        intTABS.setText(LangTool.getString("conf.labelTABS"));
        intTABS.setToggleGroup(intGroup);
        intTABS.setSelected(true);
        //not listener because just one option
        // intTABS.selectedProperty().addListener((observable, oldValue, value) -> ntTABS_itemStateChanged(value));

        showMe.setText(LangTool.getString("ss.labelShowMe"));
        showMe.setSelected(properties.containsKey(SHOW_CONNECT_DIALOG_OPTION));
        showMe.selectedProperty().addListener((src, old, value) -> showMe_itemStateChanged(value));

        lastView.setText(LangTool.getString("ss.labelLastView"));
        lastView.setSelected(properties.containsKey(START_LAST_VIEW_OPTION));
        lastView.selectedProperty().addListener((src, old, value) -> lastView_itemStateChanged(value));
    }

    private void lastView_itemStateChanged(final Boolean value) {
        updateOption(START_LAST_VIEW_OPTION, "", value);
    }

    private void showMe_itemStateChanged(final Boolean value) {
        updateOption(SHOW_CONNECT_DIALOG_OPTION, "", value);
    }

    private void hideTabBar_itemStateChanged(final boolean value) {
        updateOption(HIDE_TAB_BAR_OPTION, "yes", value);
    }

    private void updateOption(final String key, final String value, final boolean state) {
        if (state) {
            properties.setProperty(key, value);
        } else {
            properties.remove(key);
        }
    }

    void saveState() {
        //nothing
    }
}
