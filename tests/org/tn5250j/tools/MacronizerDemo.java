/**
 *
 */
package org.tn5250j.tools;

import org.tn5250j.SessionGuiAdapter;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MacronizerDemo extends Application {
    public static void main(final String[] args) {
        Macronizer.init();
        launch();
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        final SessionGuiAdapter gui = new SessionGuiAdapter();
        SessionGuiAdapter.showWindowWithMe(primaryStage, gui);

        Macronizer.showRunScriptDialog(gui);
    }
}
