/**
 *
 */
package org.tn5250j;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.swing.JFrame;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DevTools {
    public static SessionBean createSessionBean() throws Exception {
        final Session5250 session = createSession();
        return createSessionBean(session);
    }

    public static Session5250 createSession() {
        final String system = "127.0.0.1"; // TODO: your IP/hostname

        final SessionConfig config = new SessionConfig(system, system);
        config.setProperty("font", "Lucida Sans Typewriter Regular"); // example config

        return new Session5250(new Properties(), system, system, config);
    }

    public static SessionBean createSessionBean(final Session5250 session) throws UnknownHostException {
        final SessionBean sessionBean = new SessionBean(session);

        sessionBean.setHostName("127.0.0.1");
        sessionBean.setCodePage("Cp273");
        sessionBean.setNoSaveConfigFile();
        sessionBean.setScreenSize("27x132");
        sessionBean.setDeviceName("devname");

        return sessionBean;
    }

    public static JFrame createClosableFrame(final String title, final Runnable closingListener) {
        final JFrame frame = new JFrame(title);

        final Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        final int w = size.width * 2 / 3;
        final int h = size.height * 2 / 3;

        frame.setLocation((size.width - w) / 2, (size.height - h) / 2);
        frame.setSize(w, h);

        frame.addWindowListener(
            new WindowAdapter() {
                @Override
                public void windowClosing(final WindowEvent e) {
                    closingListener.run();
                }
            }
        );

        return frame;
    }
}
