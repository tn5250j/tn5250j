/**
 *
 */
package org.tn5250j.sessionsettings;

import org.tn5250j.DevTools;
import org.tn5250j.tools.LangTool;

import javafx.application.Application;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class KeypadAttributesPaneDemo extends Application {
    public static void main(final String[] args) {
        LangTool.init();
        launch(args);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        final KeypadAttributesController controller = new KeypadAttributesController(DevTools.createSessionConfig());
        final String template = "/fxml/KeypadAttributesPane.fxml";
        final ButtonType result = DevTools.showInDialog(controller, template);
        if (result == ButtonType.OK) {
            controller.applyAttributes();
        }
    }
}
