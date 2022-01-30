/**
 *
 */
package org.tn5250j.spoolfile;

import org.tn5250j.SessionGuiAdapterSwing;
import org.tn5250j.framework.tn5250.tnvt;
import org.tn5250j.spoolfile.SpoolExporterSwing;
import org.tn5250j.tools.LangTool;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SpoolExporterSwingDemo {
    public static void main(final String[] args) {
        LangTool.init();

        final SessionGuiAdapterSwing gui = new SessionGuiAdapterSwing();
        final tnvt tvt = new tnvt(gui.getSession(), gui.getScreen(), true, true);
        final SpoolExporterSwing panel = new SpoolExporterSwing(tvt, gui);
        panel.setVisible(true);
    }
}
