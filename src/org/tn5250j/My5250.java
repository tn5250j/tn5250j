/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.4
 *
 * Description:
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
 *
 */
package org.tn5250j;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.tn5250j.event.BootEvent;
import org.tn5250j.event.BootListener;
import org.tn5250j.event.EmulatorActionEvent;
import org.tn5250j.event.EmulatorActionListener;
import org.tn5250j.event.SessionChangeEvent;
import org.tn5250j.event.SessionListener;
import org.tn5250j.framework.Tn5250jController;
import org.tn5250j.framework.common.SessionManager;
import org.tn5250j.framework.common.Sessions;
import org.tn5250j.gui.TN5250jSplashScreen;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.interfaces.GUIViewInterface;
import org.tn5250j.tools.LangTool;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

public class My5250 implements BootListener,SessionListener,
EmulatorActionListener {

	private GUIViewInterface frame1;
	private String[] sessionArgs = null;
	private static Properties sessions = new Properties();
	private static BootStrapper strapper = null;
	private SessionManager manager;
	private static List<GUIViewInterface> frames;
	private TN5250jSplashScreen splash;
	private int step;

	private TN5250jLogger log = TN5250jLogFactory.getLogger(this.getClass());

	private My5250 () {

		splash = new TN5250jSplashScreen("tn5250jSplash.jpg");
		splash.setSteps(5);
		splash.setVisible(true);

		loadLookAndFeel();


		loadSessions();
		splash.updateProgress(++step);

		initJarPaths();

		initScripting();

		// sets the starting frame type.  At this time there are tabs which is
		//    default and Multiple Document Interface.
//		startFrameType();

		frames = new ArrayList<GUIViewInterface>();

		newView();

		setDefaultLocale();
		manager = SessionManager.instance();
		splash.updateProgress(++step);
		Tn5250jController.getCurrent();
	}


	/**
	 * we only want to try and load the Nimbus look and feel if it is not
	 * for the MAC operating system.
	 */
	private void loadLookAndFeel() {
		try  {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		}
		catch(Exception e) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ex) {
				// we don't care. Cause this should always work.
			}
		}
	}

	/**
	 * Check if there are any other instances of tn5250j running
	 */
	static private boolean checkBootStrapper (String[] args) {

		try {
			Socket boot = new Socket("localhost", 3036);

			PrintWriter out = new PrintWriter(boot.getOutputStream(), true);

			// parse args into a string to send to the other instance of
			//    tn5250j
			String opts = null;
			for (int x = 0;x < args.length; x++) {
				if (opts != null)
					opts += args[x] + " ";
				else
					opts = args[x] + " ";
			}
			out.println(opts);
			out.flush();
			out.close();
			boot.close();
			return true;

		}
		catch (UnknownHostException e) {
			// TODO: Should be logged @ DEBUG level
			//         System.err.println("localhost not known.");
		}
		catch (IOException e) {
			// TODO: Should be logged @ DEBUG level
			//         System.err.println("No other instances of tn5250j running.");
		}

		return false;
	}

	public void bootOptionsReceived(BootEvent bootEvent) {
		log.info(" boot options received " + bootEvent.getNewSessionOptions());

		// reload setting, to ensure correct bootstraps
		ConfigureFactory.getInstance().reloadSettings();

		// If the options are not equal to the string 'null' then we have
		//    boot options
		if (!bootEvent.getNewSessionOptions().equals("null")) {
			// check if a session parameter is specified on the command line
			String[] args = new String[TN5250jConstants.NUM_PARMS];
			parseArgs(bootEvent.getNewSessionOptions(), args);


			if (isSpecified("-s",args)) {

				String sd = getParm("-s",args);
				if (sessions.containsKey(sd)) {
					parseArgs(sessions.getProperty(sd), args);
					final String[] args2 = args;
					final String sd2 = sd;
					SwingUtilities.invokeLater(
							new Runnable () {
								public void run() {
									newSession(sd2,args2);

								}
							}
					);
				}
			}
			else {

				if (args[0].startsWith("-")) {
					SwingUtilities.invokeLater(
							new Runnable () {
								public void run() {
									startNewSession();

								}
							}
					);
				}
				else {
					final String[] args2 = args;
					final String sd2 = args[0];
					SwingUtilities.invokeLater(
							new Runnable () {
								public void run() {
									newSession(sd2,args2);

								}
							}
					);
				}
			}
		}
		else {
			SwingUtilities.invokeLater(
					new Runnable () {
						public void run() {
							startNewSession();

						}
					}
			);
		}
	}

	static public void main(String[] args) {

//		if (isSpecified("-MDI",args)) {
//			useMDIFrames = true;
//		}

		if (!isSpecified("-nc",args)) {

			if (!checkBootStrapper(args)) {

				// if we did not find a running instance and the -d options is
				//    specified start up the bootstrap daemon to allow checking
				//    for running instances
				if (isSpecified("-d",args)) {
					strapper = new BootStrapper();

					strapper.start();
				}
			}
			else {

				System.exit(0);
			}
		}

		My5250 m = new My5250();

		if (strapper != null)
			strapper.addBootListener(m);

		if (args.length > 0) {

			if (isSpecified("-width",args) ||
					isSpecified("-height",args)) {
				int width = m.frame1.getWidth();
				int height = m.frame1.getHeight();

				if (isSpecified("-width",args)) {
					width = Integer.parseInt(My5250.getParm("-width",args));
				}
				if (isSpecified("-height",args)) {
					height = Integer.parseInt(My5250.getParm("-height",args));
				}

				m.frame1.setSize(width,height);
				m.frame1.centerFrame();


			}

			/**
			 * @todo this crap needs to be rewritten it is a mess
			 */
			if (args[0].startsWith("-")) {

				// check if a session parameter is specified on the command line
				if (isSpecified("-s",args)) {

					String sd = getParm("-s",args);
					if (sessions.containsKey(sd)) {
						sessions.setProperty("emul.default",sd);
					}
					else {
						args = null;
					}

				}

				// check if a locale parameter is specified on the command line
				if (isSpecified("-L",args)) {
					Locale.setDefault(parseLocal(getParm("-L",args)));
				}
				LangTool.init();

				if (isSpecified("-s",args))
					m.sessionArgs = args;
				else
					m.sessionArgs = null;
				//            }
			}
			else {

				LangTool.init();
				m.sessionArgs = args;
			}
		}
		else {
			LangTool.init();
			m.sessionArgs = null;
		}

		if (m.sessionArgs == null && sessions.containsKey("emul.view") &&
				sessions.containsKey("emul.startLastView")) {
			String[] sargs = new String[TN5250jConstants.NUM_PARMS];
			parseArgs(sessions.getProperty("emul.view"), sargs);
			m.sessionArgs = sargs;
		}

		if (m.sessionArgs != null) {

			// BEGIN
			// 2001/09/19 natural computing MR
			List<String> os400_sessions = new ArrayList<String>();
			List<String> session_params = new ArrayList<String>();

			for (int x = 0; x < args.length; x++) {
				if (args[x].equals("-s")) {
					x++;
					if (args[x] != null && sessions.containsKey(args[x])) {
						os400_sessions.add(args[x]);
					}else{
						x--;
						session_params.add(args[x]);
					}
				}else{
					session_params.add(args[x]);
				}

			}

			for (int x = 0; x < session_params.size(); x++) {
				m.sessionArgs[x] = session_params.get(x).toString();
			}

			m.startNewSession();

			// start from 1, cause 0 equals the default session (implizit nr 0)
			for (int x = 1; x < os400_sessions.size(); x++ ) {
				String sel = os400_sessions.get(x).toString();

				if (!m.frame1.isVisible()) {
					m.splash.updateProgress(++m.step);
					m.splash.setVisible(false);
					m.frame1.setVisible(true);
					m.frame1.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}

				m.sessionArgs = new String[TN5250jConstants.NUM_PARMS];
				My5250.parseArgs(sessions.getProperty(sel),m.sessionArgs);
				m.newSession(sel,m.sessionArgs);
			}
			// 2001/09/19 natural computing MR
			// END
		}
		else {
			m.startNewSession();
		}

	}

	private void setDefaultLocale () {

		if (sessions.containsKey("emul.locale")) {
			Locale.setDefault(parseLocal(sessions.getProperty("emul.locale")));
		}

	}

	static private String getParm(String parm, String[] args) {

		for (int x = 0; x < args.length; x++) {

			if (args[x].equals(parm))
				return args[x+1];

		}
		return null;
	}

	private static boolean isSpecified(String parm, String[] args) {

		if (args == null)
			return false;

		for (int x = 0; x < args.length; x++) {

			if (args[x] != null && args[x].equals(parm))
				return true;

		}
		return false;
	}

	private static String getDefaultSession() {

		if (sessions.containsKey("emul.default")) {
			return sessions.getProperty("emul.default");
		}
		return null;
	}

//	private static void startFrameType() {
//
////		if (sessions.containsKey("emul.interface")) {
////			String s = sessions.getProperty("emul.interface");
////			if (s.equalsIgnoreCase("MDI"))
////				useMDIFrames = true;
////
////		}
//	}

	private void startNewSession() {

		String sel = "";

		if (sessionArgs != null && !sessionArgs[0].startsWith("-"))
			sel = sessionArgs[0];
		else {
			sel = getDefaultSession();
		}

		Sessions sess = manager.getSessions();

		if (sel != null && sess.getCount() == 0
				&& sessions.containsKey(sel)){
			sessionArgs = new String[TN5250jConstants.NUM_PARMS];
			parseArgs(sessions.getProperty(sel), sessionArgs);
		}

		if (sessionArgs == null  || sess.getCount() > 0
				|| sessions.containsKey("emul.showConnectDialog")) {

			sel = getConnectSession();

			if (sel != null) {
				String selArgs = sessions.getProperty(sel);
				sessionArgs = new String[TN5250jConstants.NUM_PARMS];
				parseArgs(selArgs, sessionArgs);

				newSession(sel,sessionArgs);
			}
			else {
				if (sess.getCount() == 0)
					System.exit(0);
			}

		}
		else {

			newSession(sel,sessionArgs);

		}
	}

	private void startDuplicateSession(SessionPanel ses) {

		loadSessions();
		if (ses == null) {
			Sessions sess = manager.getSessions();
			for (int x = 0; x < sess.getCount(); x++) {

				if ((sess.item(x).getGUI()).isVisible()) {

					ses = sess.item(x).getGUI();
					break;
				}
			}
		}

		String selArgs = sessions.getProperty(ses.getSessionName());
		sessionArgs = new String[TN5250jConstants.NUM_PARMS];
		parseArgs(selArgs, sessionArgs);

		newSession(ses.getSessionName(),sessionArgs);
	}

	private String getConnectSession () {

		splash.setVisible(false);
		ConnectDialog sc = new ConnectDialog(frame1,LangTool.getString("ss.title"),sessions);

		// load the new session information from the session property file
		loadSessions();
		return sc.getConnectKey();
	}

	private synchronized void newSession(String sel,String[] args) {

		Properties sesProps = new Properties();

		String propFileName = null;
		String session = args[0];

		// Start loading properties
		sesProps.put(TN5250jConstants.SESSION_HOST,session);

		if (isSpecified("-e",args))
			sesProps.put(TN5250jConstants.SESSION_TN_ENHANCED,"1");

		if (isSpecified("-p",args)) {
			sesProps.put(TN5250jConstants.SESSION_HOST_PORT,getParm("-p",args));
		}

		if (isSpecified("-f",args))
			propFileName = getParm("-f",args);

		if (isSpecified("-cp",args))
			sesProps.put(TN5250jConstants.SESSION_CODE_PAGE ,getParm("-cp",args));

		if (isSpecified("-gui",args))
			sesProps.put(TN5250jConstants.SESSION_USE_GUI,"1");

		if (isSpecified("-t", args))
			sesProps.put(TN5250jConstants.SESSION_TERM_NAME_SYSTEM, "1");

		if (isSpecified("-132",args))
			sesProps.put(TN5250jConstants.SESSION_SCREEN_SIZE,TN5250jConstants.SCREEN_SIZE_27X132_STR);
		else
			sesProps.put(TN5250jConstants.SESSION_SCREEN_SIZE,TN5250jConstants.SCREEN_SIZE_24X80_STR);

		// are we to use a socks proxy
		if (isSpecified("-usp",args)) {

			// socks proxy host argument
			if (isSpecified("-sph",args)) {
				sesProps.put(TN5250jConstants.SESSION_PROXY_HOST ,getParm("-sph",args));
			}

			// socks proxy port argument
			if (isSpecified("-spp",args))
				sesProps.put(TN5250jConstants.SESSION_PROXY_PORT ,getParm("-spp",args));
		}

		// are we to use a ssl and if we are what type
		if (isSpecified("-sslType",args)) {

			sesProps.put(TN5250jConstants.SSL_TYPE,getParm("-sslType",args));
		}


		// check if device name is specified
		if (isSpecified("-dn=hostname",args)){
			String dnParam;

			// use IP address as device name
			try{
				dnParam = InetAddress.getLocalHost().getHostName();
			}
			catch(UnknownHostException uhe){
				dnParam = "UNKNOWN_HOST";
			}

			sesProps.put(TN5250jConstants.SESSION_DEVICE_NAME ,dnParam);
		}
		else if (isSpecified("-dn",args)){

			sesProps.put(TN5250jConstants.SESSION_DEVICE_NAME ,getParm("-dn",args));
		}

		if (isSpecified("-hb",args))
			sesProps.put(TN5250jConstants.SESSION_HEART_BEAT,"1");

		int sessionCount = manager.getSessions().getCount();

		Session5250 s2 = manager.openSession(sesProps,propFileName,sel);
		SessionPanel s = new SessionPanel(s2);


		if (!frame1.isVisible()) {
			splash.updateProgress(++step);

			// Here we check if this is the first session created in the system.
			//  We have to create a frame on initialization for use in other scenarios
			//  so if this is the first session being added in the system then we
			//  use the frame that is created and skip the part of creating a new
			//  view which would increment the count and leave us with an unused
			//  frame.
			if (isSpecified("-noembed",args) && sessionCount > 0) {
				newView();
			}
			splash.setVisible(false);
			frame1.setVisible(true);
			frame1.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		else {
			if (isSpecified("-noembed",args)) {
				splash.updateProgress(++step);
				newView();
				splash.setVisible(false);
				frame1.setVisible(true);
				frame1.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

			}
		}

		if (isSpecified("-t",args))
			frame1.addSessionView(sel,s);
		else
			frame1.addSessionView(session,s);

		s.connect();

		s.addEmulatorActionListener(this);
	}

	private void newView() {

		// we will now to default the frame size to take over the whole screen
		//    this is per unanimous vote of the user base
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		int width = screenSize.width;
		int height = screenSize.height;

		if (sessions.containsKey("emul.width"))
			width = Integer.parseInt(sessions.getProperty("emul.width"));
		if (sessions.containsKey("emul.height"))
			height = Integer.parseInt(sessions.getProperty("emul.height"));

		frame1 = new Gui5250Frame(this);

		frame1.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		if (sessions.containsKey("emul.frame" + frame1.getFrameSequence())) {

			String location = sessions.getProperty("emul.frame" + frame1.getFrameSequence());
			//         System.out.println(location + " seq > " + frame1.getFrameSequence() );
			restoreFrame(frame1,location);
		}
		else {
			frame1.setSize(width,height);
			frame1.centerFrame();
		}

		frames.add(frame1);

	}

	private void restoreFrame(GUIViewInterface frame,String location) {

		StringTokenizer tokenizer = new StringTokenizer(location, ",");
		int x = Integer.parseInt(tokenizer.nextToken());
		int y = Integer.parseInt(tokenizer.nextToken());
		int width = Integer.parseInt(tokenizer.nextToken());
		int height = Integer.parseInt(tokenizer.nextToken());

		frame.setLocation(x,y);
		frame.setSize(width,height);
	}

	/**
	 * @param view
	 */
	protected void closingDown(GUIViewInterface view) {

		Sessions sess = manager.getSessions();

		if (log.isDebugEnabled()) {
			log.debug("number of active sessions we have " + sess.getCount());
		}

		// preserve sessions for next boot
		String views = "";
		while (view.getSessionViewCount() > 0) {
			SessionPanel sesspanel = view.getSessionAt(0);
			views += "-s " + sesspanel.getSessionName() + " ";
			closeSessionInternal(sesspanel);
		}

		sessions.setProperty("emul.frame" + view.getFrameSequence(),
				view.getX() + "," +
				view.getY() + "," +
				view.getWidth() + "," +
				view.getHeight());

		frames.remove(view);
		view.dispose();

		if (log.isDebugEnabled()) {
			log.debug("number of active sessions we have after shutting down " + sess.getCount());
		}

		log.info("view settings " + views);
		if (sess.getCount() == 0) {

			sessions.setProperty("emul.width",Integer.toString(view.getWidth()));
			sessions.setProperty("emul.height",Integer.toString(view.getHeight()));

			sessions.setProperty("emul.view",views);

			// save off the session settings before closing down
			ConfigureFactory.getInstance().saveSettings(ConfigureFactory.SESSIONS,
					ConfigureFactory.SESSIONS,
					"------ Defaults --------");
			if (strapper != null) {
				strapper.interrupt();
			}
			System.exit(0);
		}


	}

	/**
	 * Really closes the tab/session
	 * @param sesspanel
	 */
	protected void closeSessionInternal(SessionPanel sesspanel) {
		GUIViewInterface f = getParentView(sesspanel);
		if (f == null) {
			return;
		}
		Sessions sessions = manager.getSessions();
		if ((sessions.item(sesspanel.getSession())) != null) {
			f.removeSessionView(sesspanel);
			manager.closeSession(sesspanel);
		}
		if (manager.getSessions().getCount() < 1) {
			closingDown(f);
		}
	}

	private static void parseArgs(String theStringList, String[] s) {
		int x = 0;
		StringTokenizer tokenizer = new StringTokenizer(theStringList, " ");
		while (tokenizer.hasMoreTokens()) {
			s[x++] = tokenizer.nextToken();
		}
	}

	private static Locale parseLocal(String localString) {
		int x = 0;
		String[] s = {"","",""};
		StringTokenizer tokenizer = new StringTokenizer(localString, "_");
		while (tokenizer.hasMoreTokens()) {
			s[x++] = tokenizer.nextToken();
		}
		return new Locale(s[0],s[1],s[2]);
	}

	private static void loadSessions() {

		sessions = (ConfigureFactory.getInstance()).getProperties(
				ConfigureFactory.SESSIONS);
	}

	public void onSessionChanged(SessionChangeEvent changeEvent) {

		Session5250 ses5250 = (Session5250)changeEvent.getSource();
		SessionPanel ses = ses5250.getGUI();

		switch (changeEvent.getState()) {
		case TN5250jConstants.STATE_REMOVE:
			closeSessionInternal(ses);
			break;
		}
	}

	public void onEmulatorAction(EmulatorActionEvent actionEvent) {

		SessionPanel ses = (SessionPanel)actionEvent.getSource();

		switch (actionEvent.getAction()) {
		case EmulatorActionEvent.CLOSE_SESSION:
			closeSessionInternal(ses);
			break;
		case EmulatorActionEvent.CLOSE_EMULATOR:
			throw new UnsupportedOperationException("Not yet implemented!");
		case EmulatorActionEvent.START_NEW_SESSION:
			startNewSession();
			break;
		case EmulatorActionEvent.START_DUPLICATE:
			startDuplicateSession(ses);
			break;
		}
	}

	private GUIViewInterface getParentView(SessionPanel session) {

		GUIViewInterface f = null;

		for (int x = 0; x < frames.size(); x++) {
			f = frames.get(x);
			if (f.containsSession(session))
				return f;
		}

		return null;

	}

	/**
	 * Initializes the scripting environment if the jython interpreter exists
	 * in the classpath
	 */
	 private void initScripting() {

		 try {
			 Class.forName("org.tn5250j.scripting.JPythonInterpreterDriver");
		 }
		 catch (java.lang.NoClassDefFoundError ncdfe) {
			 log.warn("Information Message: Can not find scripting support"
					 + " files, scripting will not be available: "
					 + "Failed to load interpreter drivers " + ncdfe);
		 }
		 catch (Exception ex) {
			 log.warn("Information Message: Can not find scripting support"
					 + " files, scripting will not be available: "
					 + "Failed to load interpreter drivers " + ex);
		 }

		 splash.updateProgress(++step);

	 }

	 /**
	  * Sets the jar path for the available jars.
	  * Sets the python.path system variable to make the jython jar available
	  * to scripting process.
	  *
	  * This needs to be rewritten to loop through and obtain all jars in the
	  * user directory.  Maybe also additional paths to search.
	  */
	 private void initJarPaths() {

		 String jarClassPaths = System.getProperty("python.path")
		 + File.pathSeparator + "jython.jar"
		 + File.pathSeparator + "jythonlib.jar"
		 + File.pathSeparator + "jt400.jar"
		 + File.pathSeparator + "itext.jar";

		 if (sessions.containsKey("emul.scriptClassPath")) {
			 jarClassPaths += File.pathSeparator + sessions.getProperty("emul.scriptClassPath");
		 }

		 System.setProperty("python.path",jarClassPaths);

		 splash.updateProgress(++step);

	 }
}
