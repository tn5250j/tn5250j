/**
 *
 */
package org.tn5250j.spoolfile;

import org.tn5250j.SessionGuiAdapter;
import org.tn5250j.encoding.builtin.CCSID1025;
import org.tn5250j.framework.tn5250.tnvt;
import org.tn5250j.tools.LangTool;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SpoolExporterDemo extends Application {
    public static void main(final String[] args) {
        LangTool.init();

        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        final CCSID1025 cp = new CCSID1025();
        cp.init();

        final SessionGuiAdapter gui = new SessionGuiAdapter();
        final tnvt tvt = new tnvt(gui.getSession(), gui.getScreen(), true, true);

        final SpoolExporter dialog = new SpoolExporter(tvt, gui);
        dialog.setVisible(true);
    }
}
