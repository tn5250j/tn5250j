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

import static org.tn5250j.ParameterUtils.PARAM_MONITOR_START;
import static org.tn5250j.ParameterUtils.PARAM_NEWJVM;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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
import org.tn5250j.framework.tn5250.Rect;
import org.tn5250j.gui.SessionsDialog;
import org.tn5250j.gui.TN5250jSplashScreen;
import org.tn5250j.gui.model.EmulConfig;
import org.tn5250j.gui.model.EmulSessionProfile;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.tools.LangTool;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

public class My5250 implements BootListener,SessionListener, EmulatorActionListener {

	private static final String PARAM_LOCALE = "-L";
	private static final String EMUL_LOCALE = "emul.locale";
	private static final String EMUL_FRAME0 = "emul.frame0";
	private static final String EMUL_SCRIPT_CLASS_PATH = "emul.scriptClassPath";
	private static final String EMUL_START_LAST_VIEW = "emul.startLastView";
	private static final String EMUL_VIEW = "emul.view";
	private static final String EMUL_DEFAULT = "emul.default";
	
	private static final String PARAM_START_SESSION = "-s";
	
	private final static List<Gui5250Frame> frames = new ArrayList<Gui5250Frame>(5);
	
	private Gui5250Frame main5250Frame;
//	private String[] sessionArgs = null;
//	private static Properties sessionProperties = new Properties();
	private static BootStrapper strapper = null;
	private final EmulConfig emulConfig = new EmulConfig();
	private SessionManager manager;
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
		//System.out.println(" boot options received " + bootEvent.getNewSessionOptions());

		// If the options are not equal to the string 'null' then we have
		//    boot options
		if (!bootEvent.getNewSessionOptions().equals("null")) {
			// check if a session parameter is specified on the command line
			String[] args = new String[TN5250jConstants.NUM_PARMS];
			parseArgs(bootEvent.getNewSessionOptions(), args);


			if (isSpecified(PARAM_START_SESSION,args)) {

				final String sd = getParm(PARAM_START_SESSION,args);
//				if (sessionProperties.containsKey(sd)) {
//					parseArgs(sessionProperties.getProperty(sd), args);
//					final String[] args2 = args;
//					final String sd2 = sd;
//					SwingUtilities.invokeLater(
//							new Runnable () {
//								public void run() {
//									newSession(sd2,args2);
//
//								}
//							}
//					);
//				}
				if (emulConfig.hasSession(sd)) {
					SwingUtilities.invokeLater(
							new Runnable () {
								public void run() {
									newSession(emulConfig.getSessionByName(sd));
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

		if (!isSpecified(PARAM_NEWJVM,args)) {

			if (!checkBootStrapper(args)) {

				// if we did not find a running instance and the -d options is
				//    specified start up the bootstrap deamon to allow checking
				//    for running instances
				if (isSpecified(PARAM_MONITOR_START,args)) {
					strapper = new BootStrapper();

					strapper.start();
				}
			}
			else {

				System.exit(0);
			}
		}

		My5250 m = new My5250();
		m.run(args);

	}

	private void run(String[] args) {
		if (strapper != null)
			strapper.addBootListener(this);

		if (args.length > 0) {

			if (isSpecified("-width",args) ||
					isSpecified("-height",args)) {
				int width = main5250Frame.getWidth();
				int height = main5250Frame.getHeight();

				if (isSpecified("-width",args)) {
					width = Integer.parseInt(getParm("-width",args));
				}
				if (isSpecified("-height",args)) {
					height = Integer.parseInt(getParm("-height",args));
				}
				main5250Frame.setSize(width,height);
				main5250Frame.centerFrame();

			}

			/**
			 * @todo this crap needs to be rewritten it is a mess
			 */
			if (args[0].startsWith("-")) {

				// check if a session parameter is specified on the command line
				if (isSpecified(PARAM_START_SESSION,args)) {

					final String sd = getParm(PARAM_START_SESSION,args);
//					if (sessionProperties.containsKey(sd)) {
//						sessionProperties.setProperty("emul.default",sd);
//					}
//					else {
//						args = null;
//					}
					if (emulConfig.hasSession(sd)) {
						emulConfig.setDefaultSess(emulConfig.getSessionByName(sd));
					}

				}

				// check if a locale parameter is specified on the command line
				if (isSpecified(PARAM_LOCALE,args)) {
					emulConfig.setLocale(parseLocale(getParm(PARAM_LOCALE,args)));
					setDefaultLocale();
				}
				LangTool.init();

//				if (isSpecified(PARAM_START_SESSION,args))
//					sessionArgs = args;
//				else
//					sessionArgs = null;
				//            }
			}
			else {

				LangTool.init();
//				sessionArgs = args;
			}
		}
		else {
			LangTool.init();
//			sessionArgs = null;
		}

//		if (sessionArgs == null && sessionProperties.containsKey(EMUL_VIEW) &&
//				sessionProperties.containsKey(EMUL_START_LAST_VIEW)) {
//			String[] sargs = new String[TN5250jConstants.NUM_PARMS];
//			parseArgs(sessionProperties.getProperty(EMUL_VIEW), sargs);
//			sessionArgs = sargs;
//		}
//
//		if (sessionArgs != null) {
//
//			// BEGIN
//			// 2001/09/19 natural computing MR
//			List<String> os400_sessions = new ArrayList<String>();
//			List<String> session_params = new ArrayList<String>();
//
//			for (int x = 0; x < sessionArgs.length; x++) {
//
//				final String sessarg = sessionArgs[x];
//				if (sessarg != null) {
//					if (sessarg.equals(PARAM_START_SESSION)) {
//						x++;
//						if (sessionProperties.containsKey(sessarg)) {
//							os400_sessions.add(sessarg);
//						}else{
//							x--;
//							session_params.add(sessarg);
//						}
//					}else{
//						session_params.add(sessarg);
//					}
//				}
//			}
//
//			for (int x = 0; x < session_params.size(); x++)
//
//				sessionArgs[x] = session_params.get(x).toString();
//
//			//m.startNewSession(); // XXX: should be enabled, see bug report ...
//
//			// shouldn't we be starting x at 0?
//			for (int x = 0; x < os400_sessions.size(); x++ ) {
//				String sel = os400_sessions.get(x).toString();
//
//				if (!frame1.isVisible()) {
//					splash.updateProgress(++step);
//					splash.setVisible(false);
//					frame1.setVisible(true);
//					frame1.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//				}
//
//				sessionArgs = new String[TN5250jConstants.NUM_PARMS];
//				My5250.parseArgs(sessionProperties.getProperty(sel),sessionArgs);
//				newSession(sel,sessionArgs);
//			}
//			// 2001/09/19 natural computing MR
//			// END
//		}
//		else {
//			startNewSession();
//		}
		if (emulConfig.isStartLastView() && emulConfig.getViews().length > 0) {
			for (String view : emulConfig.getViews()) {
				newSession(emulConfig.getSessionByName(view));
			}
		} else {
			if (emulConfig.getDefaultSess() != null) {
				newSession(emulConfig.getDefaultSess());
			} else {
				final EmulSessionProfile newsession = showModalConnectSession();
				if (newsession != null) {
					newSession(newsession);
				} else {
					closingDown(main5250Frame);
				}
			}
		}

	}


	private void setDefaultLocale () {

//		if (sessionProperties.containsKey(EMUL_LOCALE)) {
//			Locale.setDefault(parseLocal(sessionProperties.getProperty(EMUL_LOCALE)));
//		}
		
		Locale.setDefault(emulConfig.getLocale());

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

//	private EmulSession getDefaultSession() {
//		return emulConfig.getDefaultSess();
//	}

//	private static String getDefaultSession() {
//
////		if (sessionProperties.containsKey("emul.default")) {
////			return sessionProperties.getProperty("emul.default");
////		}
////		return null;
//	}

//	private static void startFrameType() {
//
//		if (sessionProperties.containsKey("emul.interface")) {
//			String s = sessionProperties.getProperty("emul.interface");
//			if (s.equalsIgnoreCase("MDI"))
//				useMDIFrames = true;
//
//		}
//	}

	private void startNewSession() {
		final EmulSessionProfile newsession = showModalConnectSession();
		if (newsession != null) {
			newSession(newsession);
		}
	}

	private void startDuplicateSession(SessionPanel ses) {

		if (ses == null) {
			Sessions sess = manager.getSessions();
			for (int x = 0; x < sess.getCount(); x++) {

				if ((sess.item(x).getGUI()).isVisible()) {

					ses = sess.item(x).getGUI();
					break;
				}
			}
		}
//
//		String selArgs = sessionProperties.getProperty(ses.getSessionName());
//		sessionArgs = new String[TN5250jConstants.NUM_PARMS];
//		parseArgs(selArgs, sessionArgs);
//
//		newSession(ses.getSessionName(),sessionArgs);
		EmulSessionProfile emulsess = emulConfig.getSessionByName(ses.getSessionName());
		if (emulsess != null) {
			newSession(emulsess);
		} else {
			log.error("Couldn't find session " + ses.getSessionName());
		}
	}
	
	/**
	 * Displays connection dialog and returns the chosen {@link EmulSessionProfile} config.
	 * 
	 * @return null, if nothing was select (or canceled)
	 */
	private EmulSessionProfile showModalConnectSession() {
		splash.setVisible(false);
		SessionsDialog sessdlg = new SessionsDialog(main5250Frame, LangTool.getString("ss.title"), emulConfig);
		if (emulConfig.getDimension() != null) {
			// try to center the connect dialog over the app's main window,
			// even it's not visible yet
			final int x1 = main5250Frame.getX();
			final int y1 = main5250Frame.getY();
			final int w1 = main5250Frame.getWidth();
			final int h1 = main5250Frame.getHeight();
			final int h2 = sessdlg.getHeight();
			final int w2 = sessdlg.getWidth();
			final int x2 = x1 + w1/2 - w2/2;
			final int y2 = y1 + h1/2 - h2/2;
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			// don't display out of the screen
			sessdlg.setLocation(Math.max(Math.min(x2, screenSize.width), 0), Math.max(Math.min(y2, screenSize.height), 0));
		}
		if (sessdlg.showModal()) {
			EmulSessionProfile sess = sessdlg.getSelectedSession();
			if (sess != null) {
				return sess;
			}
		}
		return null;
	}

	/**
	 * @param emulSessionProfile
	 * @see #newSession(String, String[])
	 */
	private synchronized void newSession(EmulSessionProfile emulSessionProfile) {
		String propFileName = emulSessionProfile.getConfigFile();
		if (emulSessionProfile.isUsePcAsDevName()){
			String dnParam;

			// use IP address as device name
			try{
				dnParam = InetAddress.getLocalHost().getHostName();
			}
			catch(UnknownHostException uhe){
				dnParam = "UNKNOWN_HOST";
			}
			emulSessionProfile.setDevName(dnParam);
		}
		int sessionCount = manager.getSessions().getCount();

		Session5250 s2 = manager.openSession(emulSessionProfile, propFileName, emulSessionProfile.getName());
		SessionPanel sessiongui = new SessionPanel(s2);

		if (!main5250Frame.isVisible()) {
			splash.updateProgress(++step);

			// Here we check if this is the first session created in the system.
			//  We have to create a frame on initialization for use in other scenarios
			//  so if this is the first session being added in the system then we
			//  use the frame that is created and skip the part of creating a new
			//  view which would increment the count and leave us with an unused
			//  frame.
			if (emulSessionProfile.isOpenNewFrame() && sessionCount > 0) {
				newView();
			}
			splash.setVisible(false);
			main5250Frame.setVisible(true);
			main5250Frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		else {
			if (emulSessionProfile.isOpenNewFrame()) {
				splash.updateProgress(++step);
				newView();
				splash.setVisible(false);
				main5250Frame.setVisible(true);
				main5250Frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

			}
		}
// XXX: should be checked?
//		if (isSpecified(PARAM_USE_SYSNAME_DESCR,args))
//		if (emulSession.isUseSysNameAsDescription())
//			frame1.addSessionView(sel, sessiongui);
//		else
		main5250Frame.addSessionView(emulSessionProfile.getName(), sessiongui);

		sessiongui.connect();

		sessiongui.addEmulatorActionListener(this);
	}
	
	/**
	 * @param sel
	 * @param args
	 * @deprecated
	 * @see {@link #newSession(EmulSessionProfile)}
	 */
	@Deprecated
	private synchronized void newSession(String sel,String[] args) {
		throw new UnsupportedOperationException("not working any more!");
	}

	private void newView() {
		main5250Frame = new Gui5250Frame();
		main5250Frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closingDown(main5250Frame);
			}
		});

		main5250Frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// restore size and position, if available, else center on screen
		Rect r = emulConfig.getDimension();
		if (r == null) {
			r = emulConfig.defaultDimension();
			//Center the window
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension frameSize = main5250Frame.getSize();
			if (frameSize.height > screenSize.height) {
				frameSize.height = screenSize.height;
			}
			if (frameSize.width > screenSize.width) {
				frameSize.width = screenSize.width;
			}
			r.x = (screenSize.width - frameSize.width) / 2;
			r.y = (screenSize.height - frameSize.height) / 2;
		}
		main5250Frame.setLocation(r.x, r.y);
		main5250Frame.setSize(r.width, r.height);
		
		frames.add(main5250Frame);
	}

	private void closingDown(SessionPanel targetSession) {

		closingDown(getParentView(targetSession));
	}

	protected void closingDown(Gui5250Frame view) {

		SessionPanel jf = null;
		Sessions sess = manager.getSessions();

		if (log.isDebugEnabled()) {
			log.debug("number of active sessions we have " + sess.getCount());
		}

		if (view.getSessionViewCount() > 0) {
			String views[] = new String[0];
			views = new String[view.getSessionViewCount()];
			int counter = 0;
			while (view.getSessionViewCount() > 0) {
				
				jf = view.getSessionAt(0);
				
//			views += "-s " + jf.getSessionName() + " ";
				views[counter++] = jf.getSessionName();
				
				log.info("session found and closing down");
				view.removeSessionView(jf);
				manager.closeSession(jf);
				log.info("disconnecting socket");
				log.info("socket closed");
				jf = null;
				
			}
			if (log.isInfoEnabled()) {
				log.info("view settings " + Arrays.toString(views));
			}
			emulConfig.setViews(views);
		}

//		sessionProperties.setProperty("emul.frame" + view.getFrameSequence(),
//				view.getX() + "," +
//				view.getY() + "," +
//				view.getWidth() + "," +
//				view.getHeight());

		frames.remove(view);
		view.dispose();

		if (log.isDebugEnabled()) {
			log.debug("number of active sessions we have after shutting down " + sess.getCount());
		}

		if (sess.getCount() == 0) {

			// sessionProperties.setProperty(EMUL_WIDTH,Integer.toString(view.getWidth()));
			// sessionProperties.setProperty(EMUL_HEIGHT,Integer.toString(view.getHeight()));
//			emulConfig.setWidth(view.getWidth());
//			emulConfig.setHeight(view.getHeight());
			Rect r = new Rect();
			r.setBounds(view.getX(), view.getY(), view.getWidth(), view.getHeight());
			emulConfig.setDimension(r);
			
			// sessionProperties.setProperty(EMUL_VIEW,views);

			// placing the eventually modified session properties.
			Properties sessionProperties = ConfigureFactory.getInstance().getProperties(ConfigureFactory.SESSIONS);
			final Iterator<Object> keysit = sessionProperties.keySet().iterator();
			while (keysit.hasNext()) {
				String key = keysit.next().toString();
				if (!key.startsWith("emul.")) {
					keysit.remove(); // first, delete all session properties
				}
			}
			for (EmulSessionProfile es : emulConfig.getProfiles()) {
				sessionProperties.put(es.getName(), ParameterUtils.safeEmulSessionToString(es));
			}
			
			// save position and size ...
			sessionProperties.put(EMUL_FRAME0, emulConfig.getDimension().toString());

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

	protected void closeSession(SessionPanel targetSession) {

		Gui5250Frame f = getParentView(targetSession);
		if (f == null)
			return;
		int tabs = f.getSessionViewCount();
		Sessions sessions = manager.getSessions();

		if (tabs > 1) {

			if ((sessions.item(targetSession.getSession())) != null) {

				f.removeSessionView(targetSession);
				manager.closeSession(targetSession);
				targetSession = null;

			}
		}
		else {
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

	private static Locale parseLocale(String localString) {
		int x = 0;
		String[] s = {"","",""};
		StringTokenizer tokenizer = new StringTokenizer(localString, "_");
		while (tokenizer.hasMoreTokens()) {
			s[x++] = tokenizer.nextToken();
		}
		return new Locale(s[0],s[1],s[2]);
	}

	private void loadSessions() {

		Properties sessionProperties = ConfigureFactory.getInstance().getProperties(ConfigureFactory.SESSIONS);

		// first load every session
		for (Object key : sessionProperties.keySet()) {
			String k = key.toString();
			if (!k.startsWith("emul.")) {
				EmulSessionProfile session = ParameterUtils.loadSessionFromArguments(sessionProperties.getProperty(k));
				session.setName(k);
				emulConfig.addSession(session);
			}
		}
		// then load emul config parameters
		for (Object key : sessionProperties.keySet()) {
			String k = key.toString();
			final String val = sessionProperties.getProperty(k).trim();
			if (k.equals(EMUL_DEFAULT)) {
				// FIXME: what does it mean?
			}
			if (k.equals(EMUL_START_LAST_VIEW)) {
				emulConfig.setStartLastView(true);
			}
			if (k.equals(EMUL_VIEW)) {
				List<String> views = new ArrayList<String>();
				String emulview = val;
				int idxstart = 0;
				int idxend = emulview.indexOf(PARAM_START_SESSION, idxstart);
				for (; idxend > -1; idxend=emulview.indexOf(PARAM_START_SESSION, idxstart)) {
					String sessname = emulview.substring(idxstart, idxend).trim();
					if (sessname.length() > 0) {
						views.add(sessname);
					}
					idxstart = idxend+PARAM_START_SESSION.length();
				}
				if (idxstart + PARAM_START_SESSION.length() < emulview.length()) {
					String sessname = emulview.substring(idxstart+PARAM_START_SESSION.length() -1).trim();
					if (sessname.length() > 0) {
						views.add(sessname);
					}
				}
				emulConfig.setViews(views.toArray(new String[views.size()]));
			}
			if (k.equals(EMUL_SCRIPT_CLASS_PATH)) {
				emulConfig.setJarClassPaths(val);
			}
			if (k.equals(EMUL_LOCALE)) {
				emulConfig.setLocale(parseLocale(val));
			}
			if (k.equals(EMUL_FRAME0)) {
				try {
					final Rect r = Rect.fromString(val);
					emulConfig.setDimension(r);
				} catch (Exception e) {
					log.warn("couldn't parse dimension information. Expected 'x,y,w,h'; Actual '"+val+"'");
				}
			}
		}
	}

	public void onSessionChanged(SessionChangeEvent changeEvent) {

		Session5250 ses5250 = (Session5250)changeEvent.getSource();
		SessionPanel ses = ses5250.getGUI();

		switch (changeEvent.getState()) {
		case TN5250jConstants.STATE_REMOVE:
			closeSession(ses);
			break;
		}
	}

	public void onEmulatorAction(EmulatorActionEvent actionEvent) {

		SessionPanel ses = (SessionPanel)actionEvent.getSource();

		switch (actionEvent.getAction()) {
		case EmulatorActionEvent.CLOSE_SESSION:
			closeSession(ses);
			break;
		case EmulatorActionEvent.CLOSE_EMULATOR:
			closingDown(ses);
			break;
		case EmulatorActionEvent.START_NEW_SESSION:
			startNewSession();
			break;
		case EmulatorActionEvent.START_DUPLICATE:
			startDuplicateSession(ses);
			break;
		}
	}

	private Gui5250Frame getParentView(SessionPanel session) {

		Gui5250Frame f = null;

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

//		 if (sessionProperties.containsKey(EMUL_SCRIPT_CLASS_PATH)) {
//			 jarClassPaths += File.pathSeparator + sessionProperties.getProperty(EMUL_SCRIPT_CLASS_PATH);
//		 }
		 jarClassPaths += File.pathSeparator + emulConfig.getJarClassPaths();

		 System.setProperty("python.path",jarClassPaths);

		 splash.updateProgress(++step);

	 }
}
