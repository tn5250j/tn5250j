/*
 *
 * <p>Title: SessionBean</p>
 * <p>Description: TN5250 Session as a bean with auto signon features</p>
 * <p>Copyright: Copyright (c) 2000 - 2004</p>
 * <p>
 *
 * @author Luc Gorrens
 * @version 1.0
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 * </p>
 */

package org.tn5250j;

import static org.tn5250j.TN5250jConstants.SCREEN_SIZE_24X80_STR;
import static org.tn5250j.TN5250jConstants.SCREEN_SIZE_27X132_STR;
import static org.tn5250j.TN5250jConstants.SESSION_CODE_PAGE;
import static org.tn5250j.TN5250jConstants.SESSION_DEVICE_NAME;
import static org.tn5250j.TN5250jConstants.SESSION_HOST;
import static org.tn5250j.TN5250jConstants.SESSION_HOST_PORT;
import static org.tn5250j.TN5250jConstants.SESSION_LOCALE;
import static org.tn5250j.TN5250jConstants.SESSION_SCREEN_SIZE;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Properties;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.tn5250j.keyboard.KeyMnemonic;


public class SessionBean extends SessionPanel {
    //      // ===========================================================================
//      //                      C o n s t r u c t o r s
//      // ===========================================================================
    public SessionBean(final String configurationResource,
                       final String sessionName) {
        this(new Properties(),
                configurationResource,
                sessionName);
    }


    public SessionBean(final Properties sessionProperties,
                       final String configurationResource,
                       final String sessionName) {

//   Session52520 session = new Session5250(sessionProperties, null, sessionName, new SessionConfig(configurationResource, sessionName));
        this(new Session5250(sessionProperties, null, sessionName,
                new SessionConfig(configurationResource, sessionName)));
        //    super(sessionProperties, null, sessionName, new SessionConfig(configurationResource, sessionName));
//    this.sessionProperties = sessionProperties;
//    this.sessionProperties.put(SESSION_LOCALE, Locale.getDefault());
//    this.getConfiguration().addSessionConfigListener(this);
    }

    public SessionBean(final Session5250 session) {

        super(session);
        this.sessionProperties = session.sesProps;
        this.sessionProperties.put(SESSION_LOCALE, Locale.getDefault());
        this.getSession().getConfiguration().addSessionConfigListener(this);
    }

    // ===========================================================================
    //               E m u l a t o r   I m p l e m e n t a t i o n
    // ===========================================================================
    public void setHostName(final String hostName)
            throws UnknownHostException, IllegalStateException {
        failIfConnected();

        //props.put(SESSION_HOST, hostName);
        this.setIPAddress(InetAddress.getByName(hostName).getHostAddress());
    }


    public void setIPAddress(final String ipAddress)
            throws IllegalStateException {
        failIfConnected();

        if (isSignificant(ipAddress))
            sessionProperties.put(SESSION_HOST, ipAddress);
    }


    public void setIPPort(final int ipPort) {
        failIfConnected();

        if (ipPort < 0)
            throw new IllegalArgumentException("An IP port must be greate or equal to 0!");

        sessionProperties.put(SESSION_HOST_PORT, Integer.toString(ipPort));
    }


    public void setCodePage(String charEncoding) {
        failIfConnected();

        if (isSignificant(charEncoding)) {
            if (charEncoding.startsWith("Cp"))
                charEncoding = charEncoding.substring(2);
            sessionProperties.put(SESSION_CODE_PAGE, charEncoding);
        }
    }

    public void setDeviceName(final String deviceName) {
        failIfConnected();

        if (isSignificant(deviceName))
            sessionProperties.put(SESSION_DEVICE_NAME, deviceName);
    }


    public void setScreenSize(final String screenSize) {
        failIfConnected();

        if ("27x132".equals(screenSize))
            sessionProperties.put(SESSION_SCREEN_SIZE, SCREEN_SIZE_27X132_STR);
        else
            sessionProperties.put(SESSION_SCREEN_SIZE, SCREEN_SIZE_24X80_STR);
    }


    public void setSignonEmbedded(final boolean embed) {
        failIfConnected();

        this.embeddedSignon = embed;
    }


    public void setSignonUser(final String user) {
        failIfConnected();
        failIfNot10(user);

        this.user = user;
    }


    public void setSignonPassword(final String password) {
        failIfConnected();
        failIfNot10(password);

        this.password = password;
    }


    public void setSignonLibrary(final String library) {
        failIfConnected();
        failIfNot10(library);

        this.library = library;
    }


    public void setSignonMenu(final String menu) {
        failIfConnected();
        failIfNot10(menu);

        this.menu = menu;
    }


    public void setSignonProgram(final String program) {
        failIfConnected();
        failIfNot10(program);

        this.program = program;
    }


    public void setAfterSignonMacro(final String macro) {
        failIfConnected();

        this.afterSignon = macro;
    }


    public void setInitialCommand(final String command) {
        failIfConnected();

        this.initialCommand = command;
    }


    public void setVisibilityInterval(final int interval) {
        failIfConnected();

        this.visibilityInterval = interval;

//    if (this.visibilityInterval <= 0)
//      this.setVisible(true);
//    else
//      this.setVisible(false);
    }


    @Override
    public void connect() {
        failIfConnected();

        if (!isSignificant(this.user)) {
            connectSimple();
        } else {
            if (this.embeddedSignon)
                connectEmbedded();
            else
                connectSimulated();

            final Runnable runnable = new Runnable() {
                int tryConnection;

                @Override
                public void run() {
                    if ((tryConnection++ < 30) &&     //If it is still not connected after 3 seconds,
                            //stop with trying
                            (isVtConnected() == false)) {
                        try {
                            Thread.sleep(100);
                        } catch (final InterruptedException ex) {
                            ;
                        }
                        SwingUtilities.invokeLater(this);
                    } else {
                        doAfterSignon();
                        doInitialCommand();
                        doVisibility();
                    }
                }
            };
            runnable.run();
        }
    }

    public void signoff() {
        if (session.getVT() != null) {
            if (this.isVtConnected())
                this.session.getVT().systemRequest("90");
        }
    }

    public void systemRequest(final String srCode) {
        this.session.getVT().systemRequest(srCode);
    }

    //============================================================================
    //            P r i v a t e   M e t h o d s   a n d   F i e l d s
    //============================================================================
    private void connectSimple() {
        super.connect();
    }

    private void connectEmbedded() {
        if (isSignificant(user))
            sessionProperties.put("SESSION_CONNECT_USER", user);

        if (password != null)
            sessionProperties.put("SESSION_CONNECT_PASSWORD", password);

        if (isSignificant(program))
            sessionProperties.put("SESSION_CONNECT_PROGRAM", program);

        if (isSignificant(menu))
            sessionProperties.put("SESSION_CONNECT_MENU", menu);

        if (isSignificant(library))
            sessionProperties.put("SESSION_CONNECT_LIBRARY", library);

        super.connect();
    }


    private void connectSimulated() {
        final StringBuilder sb = new StringBuilder();

        if (isSignificant(user))
            sb.append(user);
        if (!isFieldLength(user))
            sb.append(KeyMnemonic.TAB.mnemonic);

        if (isSignificant(password))
            sb.append(password);
        if (!isFieldLength(password))
            sb.append(KeyMnemonic.TAB.mnemonic);

        // First we test if we have something signicant to send.
        // If so, we presume we have a standard IBM login screen!
        if (isSignificant(program) || isSignificant(menu) || isSignificant(library)) {
            if (isSignificant(program))
                sb.append(program);
            if (!isFieldLength(program))
                sb.append(KeyMnemonic.TAB.mnemonic);

            if (isSignificant(menu))
                sb.append(menu);
            if (!isFieldLength(menu))
                sb.append(KeyMnemonic.TAB.mnemonic);

            if (isSignificant(library))
                sb.append(library);
        }
        sb.append(KeyMnemonic.ENTER.mnemonic);

        super.connect();

        this.getScreen().sendKeys(sb.toString());
    }


    private void doAfterSignon() {
        if (isSignificant(afterSignon))
            this.getScreen().sendKeys(afterSignon);
    }


    private void doInitialCommand() {
        if (isSignificant(initialCommand)) {
            this.getScreen().sendKeys(initialCommand + KeyMnemonic.ENTER.mnemonic);
        }
    }


    private void doVisibility() {
        if (!isVisible() && (visibilityInterval > 0)) {
            final Timer t = new Timer(visibilityInterval, new DoVisible());
            t.setRepeats(false);
            t.start();
        } else if (!isVisible()) {
            new DoVisible().run();
        }
    }


    private boolean isFieldLength(final String param) {
        return ((param != null) && (param.length() == 10));
    }

    private void failIfConnected() {
        if ((session.getVT() != null) && (isVtConnected()))
            throw new IllegalStateException("Cannot change property after being connected!");
    }


    private void failIfNot10(final String param) {
        if ((param != null) && (param.length() > 10))
            throw new IllegalArgumentException("The length of the parameter cannot exceed 10 positions!");
    }


    private Properties sessionProperties;

    private boolean embeddedSignon;
    private String user;
    private String password;
    private String library;
    private String menu;
    private String program;
    private String initialCommand;
    private String afterSignon;
    private int visibilityInterval;

    //============================================================================
    //                    U t i l i t y   M e t h o d s
    //============================================================================
    private static boolean isSignificant(final String param) {
        if ((param != null) && (param.length() != 0))
            return true;

        return false;
    }

    public void setNoSaveConfigFile() {
        this.sesConfig.removeProperty("saveme");
    }

    private class DoVisible
            implements ActionListener, Runnable {
        @Override
        public void actionPerformed(final ActionEvent event) {
            SwingUtilities.invokeLater(this);
        }

        @Override
        public void run() {
            SessionBean.this.setVisible(true);
            SessionBean.this.resizeMe();
            SessionBean.this.requestFocus();
        }
    }
}
