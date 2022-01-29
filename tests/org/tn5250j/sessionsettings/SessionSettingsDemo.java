/**
 *
 */
package org.tn5250j.sessionsettings;

import org.tn5250j.SessionConfig;
import org.tn5250j.SessionGuiAdapter;
import org.tn5250j.gui.SwingToFxUtils;
import org.tn5250j.tools.LangTool;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SessionSettingsDemo extends Application {
    public static void main(final String[] args) {
        LangTool.init();
        SwingToFxUtils.initFx();

        launch();
    }

    @Override
    public void start(final Stage frame) throws Exception {
        final SessionGuiAdapter gui = new SessionGuiAdapter();
        SessionGuiAdapter.showWindowWithMe(frame, gui);

        final String system = "127.0.0.1";

        final SessionSettings settings = new SessionSettings(frame,
                new SessionConfig(system, system));

        settings.showIt();
    }
}
