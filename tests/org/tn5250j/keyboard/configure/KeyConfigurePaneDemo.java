/**
 *
 */
package org.tn5250j.keyboard.configure;

import org.tn5250j.DevTools;
import org.tn5250j.encoding.builtin.CCSID1025;
import org.tn5250j.tools.LangTool;

import javafx.application.Application;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class KeyConfigurePaneDemo  extends Application {
    public static void main(final String[] args) {
        LangTool.init();
        launch(args);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        final CCSID1025 cp = new CCSID1025();
        cp.init();

        final KeyConfigureController controller = new KeyConfigureController(null, cp);
        final String template = "/fxml/KeyConfigurePane.fxml";
        final Dialog<ButtonType> dialog = DevTools.createDialog(controller, template);

        dialog.setTitle(LangTool.getString("key.title"));
        dialog.show();
    }
}
