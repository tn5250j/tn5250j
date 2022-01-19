/**
 *
 */
package org.tn5250j.sessionsettings;

import org.tn5250j.DevTools;
import org.tn5250j.tools.LangTool;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DisplayAttributesPanelSwingDemo {
    public static void main(final String[] args) {
        LangTool.init();
        final DisplayAttributesPanelSwing panel = new DisplayAttributesPanelSwing(DevTools.createSessionConfig());
        DevTools.showInFrame(panel);
    }
}
