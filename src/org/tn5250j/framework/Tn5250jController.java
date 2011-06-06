/**Copyright (C) 2004 Seagull Software
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*
*@author bvansomeren (bvansomeren@seagull.nl)
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

import javax.swing.JFrame;

import org.tn5250j.GlobalConfigure;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.SessionPanel;
import org.tn5250j.Session5250;
import org.tn5250j.framework.common.SessionManager;
import org.tn5250j.TN5250jConstants;
import org.tn5250j.framework.tn5250.tnvt;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;
import org.tn5250j.framework.common.Sessions;


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
		String maindir = System.getProperty("user.dir");
		extensionDir = new File(maindir + File.separatorChar + "ext");
		log.info("plugin directory is: " + extensionDir.getAbsolutePath());
		if (!extensionDir.exists()) {
			log.warn("Plugin path '"+extensionDir.getAbsolutePath()+"' does not exist. No plugins will be loaded.");
		}
		this.setDaemon(true);
		eventList = new ArrayList<Tn5250jEvent>();
		listeners = new ArrayList<Tn5250jListener>();
		Tn5250jController.current = this;
		log.info("Tn5250j plugin manager created");
		manager = SessionManager.instance();
		Sessions ses = manager.getSessions();
		log.debug("Sessions:" + ses.getCount());
		sesprops =
			((GlobalConfigure) ConfigureFactory.getInstance()).getProperties(
				GlobalConfigure.SESSIONS);
		log.debug("Session configuration: " + sesprops.toString());
		this.start();
	}

	private void loadExt() {
		if (this.extensionDir.exists()) {
			File[] exts = extensionDir.listFiles();
			for (int x = 0; x < exts.length; x++) {
				if (exts[x].isDirectory()) {
					String jarName =
						exts[x].getAbsolutePath()
						+ File.separatorChar
						+ exts[x].getName()
						+ ".jar";
					File jarFile = new File(jarName);
					if (jarFile.exists()) {
						Properties config = loadConfig(jarFile);
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

	private void load(File jar, String name, Properties config) {
		URL[] urls = new URL[1];

		try {
			urls[0] = jar.toURI().toURL();
			log.info("Loading jar: " + jar.toURI().toURL());
		} catch (MalformedURLException e) {
			log.warn("The URL was malformed");
		}
		URLClassLoader loader = new URLClassLoader(urls);
		try {
			Class<?> ext = loader.loadClass(name);
			try {
				Tn5250jListener module = (Tn5250jListener) ext.newInstance();
				listeners.add(module);
				ModuleThread thrd =
					new ModuleThread(jar.getParentFile(), module, config);
				thrd.start();

			} catch (InstantiationException e2) {
				log.warn("Error instantiating class " + name);
			} catch (IllegalAccessException e2) {
				log.warn(
					"The class "
						+ name
						+ " gives no access to the constructor");
			} catch (ClassCastException e2) {
				log.warn(
					"Main module class does not derive from Tn5250jListener");
			}

		} catch (ClassNotFoundException e1) {
			log.warn(
				"Extension could not be loaded, class: " + name + " not found");
		}
	}

	private Properties loadConfig(File jar) {
		JarFile jarfile = null;
		Properties config = null;
		try {
			jarfile = new JarFile(jar);
			JarEntry configfile = jarfile.getJarEntry("config.properties");
			InputStream stream = jarfile.getInputStream(configfile);
			config = new Properties();
			config.load(stream);
		} catch (IOException e) {
			log.warn("Failure trying to load configuration");
		}
		return config;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
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
				} catch (InterruptedException e) {
					log.debug("Intertupted exception");
				}
			}
		}
		//log.info("Tn5250j plugin manager stopped");
	}

	private void broadcastEvent(final Tn5250jEvent event) {
		Iterator<Tn5250jListener> listenerIt = listeners.iterator();
		while (listenerIt.hasNext()) {
			Tn5250jListener listener = listenerIt.next();
			listener.actionPerformed(event);
		}
	}

	public void handleEvent(Tn5250jEvent e) {
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

	public void createSession(Screen5250 screen, tnvt vt, SessionPanel ses) {
		final Tn5250jSession session = new Tn5250jSession(screen, vt, ses);
		Iterator<Tn5250jListener> listenerIt = listeners.iterator();
		log.info("New session created and received");
		while (listenerIt.hasNext()) {
			Tn5250jListener listener = listenerIt.next();
			listener.sessionCreated(session);
		}
	}

	private class ModuleThread extends Thread {
		File dir;
		Tn5250jListener mod;
		Properties config;
		public ModuleThread(
			File directory,
			Tn5250jListener module,
			Properties config) {
			dir = directory;
			mod = module;
			this.config = config;
			this.setDaemon(true);
			this.setName(module.getName());
		}

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

	protected Properties getPropertiesForSession(String session) {
		return null;
	}

	public Screen5250 startSession(String name) {
		JFrame frame = new JFrame();
		String args[] = new String[15];
		parseArgs((String) sesprops.get(name), args);
		Properties fin = convertToProps(args);
		Session5250 newses = manager.openSession(fin, null, name);
      SessionPanel newGui = new SessionPanel(newses);
		frame.getContentPane().add(newGui);
		frame.setBounds(50, 50, 960, 700);
		frame.setVisible(true);
		newses.connect();
		return newses.getScreen();
	}

	public List<String> getSessions() {
		Enumeration<Object> e = sesprops.keys();
		ArrayList<String> list = new ArrayList<String>();
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

	protected void parseArgs(String theStringList, String[] s) {
		int x = 0;
		StringTokenizer tokenizer = new StringTokenizer(theStringList, " ");
		while (tokenizer.hasMoreTokens()) {
			s[x++] = tokenizer.nextToken();
		}
	}

	protected Properties convertToProps(String args[]) {
		Properties sesProps = new Properties();

		String session = args[0];

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
			} catch (UnknownHostException uhe) {
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

	boolean isSpecified(String parm, String[] args) {

		if (args == null)
			return false;

		for (int x = 0; x < args.length; x++) {

			if (args[x] != null && args[x].equals(parm))
				return true;

		}
		return false;
	}

	private String getParm(String parm, String[] args) {

		for (int x = 0; x < args.length; x++) {

			if (args[x].equals(parm))
				return args[x + 1];

		}
		return null;
	}

}
