/**
 * Copyright (C) 2004 Seagull Software
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * @author bvansomeren (bvansomeren@seagull.nl)
 */
package org.tn5250j.framework;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.tn5250j.GlobalConfigure;
import org.tn5250j.Session5250;
import org.tn5250j.SessionGui;
import org.tn5250j.SessionPanel;
import org.tn5250j.TN5250jConstants;
import org.tn5250j.framework.common.SessionManager;
import org.tn5250j.framework.common.Sessions;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.tnvt;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

import javafx.scene.Scene;
import javafx.stage.Stage;


public class Tn5250jController extends Thread {
    private File extensionDir;
    private TN5250jLogger log = TN5250jLogFactory.getLogger(this.getClass());
    //private URLClassLoader loader = new URLClassLoader(null, this.getClass().getClassLoader());
    private List<Tn5250jEvent> eventList;
    private List<Tn5250jListener> listeners;
    private SessionManager manager;
    Properties sesprops;
    private static Tn5250jController current;

    private Tn5250jController() {
        final String maindir = System.getProperty("user.dir");
        extensionDir = new File(maindir + File.separatorChar + "ext");
        log.info("plugin directory is: " + extensionDir.getAbsolutePath());
        if (!extensionDir.exists()) {
            log.warn("Plugin path '" + extensionDir.getAbsolutePath() + "' does not exist. No plugins will be loaded.");
        }
        this.setDaemon(true);
        eventList = new ArrayList<Tn5250jEvent>();
        listeners = new ArrayList<Tn5250jListener>();
        Tn5250jController.current = this;
        log.info("Tn5250j plugin manager created");
        manager = SessionManager.instance();
        final Sessions ses = manager.getSessions();
        log.debug("Sessions:" + ses.getCount());
        sesprops =
                ((GlobalConfigure) ConfigureFactory.getInstance()).getProperties(
                        GlobalConfigure.SESSIONS);
        log.debug("Session configuration: " + sesprops.toString());
        this.start();
    }

    private void loadExt() {
        if (this.extensionDir.exists()) {
            final File[] exts = extensionDir.listFiles();
            for (int x = 0; x < exts.length; x++) {
                if (exts[x].isDirectory()) {
                    final String jarName =
                            exts[x].getAbsolutePath()
                                    + File.separatorChar
                                    + exts[x].getName()
                                    + ".jar";
                    final File jarFile = new File(jarName);
                    if (jarFile.exists()) {
                        final Properties config = loadConfig(jarFile);
                        load(jarFile, config.getProperty("mainentry"), config);
                    } else {
                        log.warn(
                                "extension could not be loaded as the jar was not found: "
                                        + jarName);
                    }
                }
            }
        }
    }

    private void load(final File jar, final String name, final Properties config) {
        final URL[] urls = new URL[1];

        try {
            urls[0] = jar.toURI().toURL();
            log.info("Loading jar: " + jar.toURI().toURL());
        } catch (final MalformedURLException e) {
            log.warn("The URL was malformed");
        }
        final URLClassLoader loader = new URLClassLoader(urls);
        try {
            final Class<?> ext = loader.loadClass(name);
            try {
                final Tn5250jListener module = (Tn5250jListener) ext.newInstance();
                listeners.add(module);
                final ModuleThread thrd =
                        new ModuleThread(jar.getParentFile(), module, config);
                thrd.start();

            } catch (final InstantiationException e2) {
                log.warn("Error instantiating class " + name);
            } catch (final IllegalAccessException e2) {
                log.warn(
                        "The class "
                                + name
                                + " gives no access to the constructor");
            } catch (final ClassCastException e2) {
                log.warn(
                        "Main module class does not derive from Tn5250jListener");
            }

        } catch (final ClassNotFoundException e1) {
            log.warn(
                    "Extension could not be loaded, class: " + name + " not found");
        }
    }

    private Properties loadConfig(final File jar) {
        JarFile jarfile = null;
        Properties config = null;
        try {
            jarfile = new JarFile(jar);
            final JarEntry configfile = jarfile.getJarEntry("config.properties");
            final InputStream stream = jarfile.getInputStream(configfile);
            config = new Properties();
            config.load(stream);
        } catch (final IOException e) {
            log.warn("Failure trying to load configuration");
        }
        return config;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        log.info("Tn5250j plugin manager started");
        loadExt();
        while (true) { //keep on running forever as we are a daemon
            synchronized (eventList) {
                while (!eventList.isEmpty()) {
                    final Tn5250jEvent event =
                            eventList.remove(0);
                    broadcastEvent(event);
                }
                try {
                    eventList.wait();
                } catch (final InterruptedException e) {
                    log.debug("Intertupted exception");
                }
            }
        }
        //log.info("Tn5250j plugin manager stopped");
    }

    private void broadcastEvent(final Tn5250jEvent event) {
        final Iterator<Tn5250jListener> listenerIt = listeners.iterator();
        while (listenerIt.hasNext()) {
            final Tn5250jListener listener = listenerIt.next();
            listener.actionPerformed(event);
        }
    }

    public void handleEvent(final Tn5250jEvent e) {
        log.debug("Received event: " + e.getClass().toString());
        if (e instanceof Tn5250jKeyEvents) {
            log.debug("Keys: " + ((Tn5250jKeyEvents) e).getKeystrokes());
        }
        eventList.add(e);
        synchronized (eventList) {
            eventList.notify();
        }
    }

    public static Tn5250jController getCurrent() {
        if (current == null) {
            current = new Tn5250jController();
        }
        return current;
    }

    public void createSession(final Screen5250 screen, final tnvt vt, final SessionGui ses) {
        final Tn5250jSession session = new Tn5250jSession(screen, vt, ses);
        final Iterator<Tn5250jListener> listenerIt = listeners.iterator();
        log.info("New session created and received");
        while (listenerIt.hasNext()) {
            final Tn5250jListener listener = listenerIt.next();
            listener.sessionCreated(session);
        }
    }

    private class ModuleThread extends Thread {
        File dir;
        Tn5250jListener mod;
        Properties config;

        public ModuleThread(
                final File directory,
                final Tn5250jListener module,
                final Properties config) {
            dir = directory;
            mod = module;
            this.config = config;
            this.setDaemon(true);
            this.setName(module.getName());
        }

        @Override
        public void run() {
            mod.init(dir, config);
            mod.setController(Tn5250jController.getCurrent());
            log.info("module initialized");
            mod.run();
            log.info("module stopped");
            mod.destroy();
            log.info("module destroyed");
        }

    }

    protected Properties getPropertiesForSession(final String session) {
        return null;
    }

    public Screen5250 startSession(final String name) {
        final String args[] = new String[15];
        parseArgs((String) sesprops.get(name), args);
        final Properties fin = convertToProps(args);

        final Session5250 newses = manager.openSession(fin, null, name);

        UiUtils.runInFxAndWait(() -> {
            final SessionPanel gui = new SessionPanel(newses);

            final Stage stage = new Stage();
            stage.setScene(new Scene(gui));
            return gui;
        });

        newses.connect();
        return newses.getScreen();
    }

    public List<String> getSessions() {
        final Enumeration<Object> e = sesprops.keys();
        final ArrayList<String> list = new ArrayList<String>();
        String ses = null;
        //This has the nasty tendency to grab data it isn't suposed to grab.
        //please fix
        while (e.hasMoreElements()) {
            ses = (String) e.nextElement();
            if (!ses.startsWith("emul.")) {
                list.add(ses);
            }
        }
        log.error(list.toString());
        return list;
    }

    protected void parseArgs(final String theStringList, final String[] s) {
        int x = 0;
        final StringTokenizer tokenizer = new StringTokenizer(theStringList, " ");
        while (tokenizer.hasMoreTokens()) {
            s[x++] = tokenizer.nextToken();
        }
    }

    protected Properties convertToProps(final String args[]) {
        final Properties sesProps = new Properties();

        final String session = args[0];

        // Start loading properties
        sesProps.put(TN5250jConstants.SESSION_HOST, session);

        if (isSpecified("-e", args))
            sesProps.put(TN5250jConstants.SESSION_TN_ENHANCED, "1");

        if (isSpecified("-p", args)) {
            sesProps.put(TN5250jConstants.SESSION_HOST_PORT, getParm("-p", args));
        }

//		if (isSpecified("-f", args)) {
//			String propFileName = getParm("-f", args);
//		}

        if (isSpecified("-cp", args))
            sesProps.put(TN5250jConstants.SESSION_CODE_PAGE, getParm("-cp", args));

        if (isSpecified("-gui", args))
            sesProps.put(TN5250jConstants.SESSION_USE_GUI, "1");

        if (isSpecified("-t", args))
            sesProps.put(TN5250jConstants.SESSION_TERM_NAME_SYSTEM, "1");

        if (isSpecified("-132", args))
            sesProps.put(TN5250jConstants.SESSION_SCREEN_SIZE, TN5250jConstants.SCREEN_SIZE_27X132_STR);
        else
            sesProps.put(TN5250jConstants.SESSION_SCREEN_SIZE, TN5250jConstants.SCREEN_SIZE_24X80_STR);

        // are we to use a socks proxy
        if (isSpecified("-usp", args)) {

            // socks proxy host argument
            if (isSpecified("-sph", args)) {
                sesProps.put(TN5250jConstants.SESSION_PROXY_HOST, getParm("-sph", args));
            }

            // socks proxy port argument
            if (isSpecified("-spp", args))
                sesProps.put(TN5250jConstants.SESSION_PROXY_PORT, getParm("-spp", args));
        }

        // are we to use a ssl and if we are what type
        if (isSpecified("-sslType", args)) {

            sesProps.put(TN5250jConstants.SSL_TYPE, getParm("-sslType", args));
        }

        // check if device name is specified
        if (isSpecified("-dn=hostname", args)) {
            String dnParam;

            // use IP address as device name
            try {
                dnParam = InetAddress.getLocalHost().getHostName();
            } catch (final UnknownHostException uhe) {
                dnParam = "UNKNOWN_HOST";
            }

            sesProps.put(TN5250jConstants.SESSION_DEVICE_NAME, dnParam);
        } else if (isSpecified("-dn", args)) {

            sesProps.put(TN5250jConstants.SESSION_DEVICE_NAME, getParm("-dn", args));
        }

        if (isSpecified("-hb", args))
            sesProps.put(TN5250jConstants.SESSION_HEART_BEAT, "1");

        return sesProps;
    }

    boolean isSpecified(final String parm, final String[] args) {

        if (args == null)
            return false;

        for (int x = 0; x < args.length; x++) {

            if (args[x] != null && args[x].equals(parm))
                return true;

        }
        return false;
    }

    private String getParm(final String parm, final String[] args) {

        for (int x = 0; x < args.length; x++) {

            if (args[x].equals(parm))
                return args[x + 1];

        }
        return null;
    }

}
