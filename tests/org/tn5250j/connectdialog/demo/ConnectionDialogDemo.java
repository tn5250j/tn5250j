package org.tn5250j.connectdialog.demo;

import org.tn5250j.gui.UiUtils;
import org.tn5250j.tools.LangTool;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ConnectionDialogDemo extends Application {

    public static void main(final String[] args) throws Exception {
        LangTool.init();
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        setUserAgentStylesheet(STYLESHEET_MODENA);
        UiUtils.showDialog(null, "/fxml/ConnectionDialog.fxml", "Connection dialog demo", null);
    }
}
