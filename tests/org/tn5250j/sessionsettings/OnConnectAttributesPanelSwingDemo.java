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
public class OnConnectAttributesPanelSwingDemo {
    public static void main(final String[] args) {
        LangTool.init();
        final OnConnectAttributesPanel panel = new OnConnectAttributesPanel(DevTools.createSessionConfig());
        DevTools.showInFrame(panel);
    }
}
