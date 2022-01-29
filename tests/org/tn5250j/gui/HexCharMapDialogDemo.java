/**
 *
 */
package org.tn5250j.gui;

import org.tn5250j.encoding.builtin.CCSID1025;
import org.tn5250j.tools.LangTool;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class HexCharMapDialogDemo  extends Application {
    public static void main(final String[] args) {
        LangTool.init();

        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        final CCSID1025 cp = new CCSID1025();
        cp.init();
        final HexCharMapDialog dialog = new HexCharMapDialog(cp);
        System.out.println(dialog.showModal());
    }
}
