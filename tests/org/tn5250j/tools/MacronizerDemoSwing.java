/**
 *
 */
package org.tn5250j.tools;

import javax.swing.SwingUtilities;

import org.tn5250j.SessionGuiAdapterSwing;
import org.tn5250j.gui.SwingToFxUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MacronizerDemoSwing {
    public static void main(final String[] args) {
        SwingToFxUtils.initFx();
        SwingUtilities.invokeLater(() -> launch());
    }

    private static void launch() {
        final SessionGuiAdapterSwing gui = new SessionGuiAdapterSwing();
        SessionGuiAdapterSwing.showWindowWithMe(gui);

        Macronizer.showRunScriptDialog(gui);
    }
}
