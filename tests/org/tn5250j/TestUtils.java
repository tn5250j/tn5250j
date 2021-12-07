/**
 *
 */
package org.tn5250j;

import java.net.UnknownHostException;
import java.util.Properties;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TestUtils {
    public static SessionBean createSessionBean() throws Exception {
        final Session5250 session = createSession();
        return createSessionBean(session);
    }

    public static Session5250 createSession() {
        final String system = "127.0.0.1"; // TODO: your IP/hostname

        final SessionConfig config = new SessionConfig(system, system);
        config.setProperty("font", "Lucida Sans Typewriter Regular"); // example config

        final Session5250 session = new Session5250(new Properties(), system, system, config);
        return session;
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
}
