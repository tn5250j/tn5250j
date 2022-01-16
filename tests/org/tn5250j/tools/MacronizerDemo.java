/**
 *
 */
package org.tn5250j.tools;

import org.tn5250j.SessionGuiAdapter;
import org.tn5250j.gui.SwingToFxUtils;

import javafx.application.Platform;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MacronizerDemo {
    public static void main(final String[] args) {
        SwingToFxUtils.initFx();
        Platform.runLater(() -> launch());
    }

    private static void launch() {
        Macronizer.init();
        final SessionGuiAdapter gui = new SessionGuiAdapter();
        SessionGuiAdapter.showWindowWithMe(gui);

        Macronizer.showRunScriptDialog(gui);
    }
}
