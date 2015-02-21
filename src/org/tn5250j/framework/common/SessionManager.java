/*
 * @(#)SessionManager.java
 * Copyright:    Copyright (c) 2001 - 2004
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
package org.tn5250j.framework.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.tn5250j.Session5250;
import org.tn5250j.SessionConfig;
import org.tn5250j.SessionPanel;
import org.tn5250j.TN5250jConstants;
import org.tn5250j.interfaces.SessionManagerInterface;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;


/**
 * The SessionManager is the central repository for access to all sessions.
 * The SessionManager contains a list of all Session objects available.
 */
public class SessionManager implements SessionManagerInterface {

	static private Sessions sessions;
	static private List<SessionConfig> configs;

	private TN5250jLogger log = TN5250jLogFactory.getLogger (this.getClass());
	/**
	 * A handle to the unique SessionManager class
	 */
	static private SessionManager _instance;

	/**
	 * The constructor is made protected to allow overriding.
	 */
	protected SessionManager() {
		if (_instance == null) {
			// initialize the settings information
			initialize();
			// set our instance to this one.
			_instance = this;
		}
	}

	/**
	 *
	 * @return The unique instance of this class.
	 */
	static public SessionManager instance() {

		if (_instance == null) {
			_instance = new SessionManager();
		}
		return _instance;

	}

	private void initialize() {
		log.info("New session Manager initialized");
		sessions = new Sessions();
		configs = new ArrayList<SessionConfig>();

	}

	@Override
	public Sessions getSessions() {
		return sessions;
	}

	@Override
	public void closeSession(SessionPanel sesspanel) {

		sesspanel.closeDown();
		sessions.removeSession((sesspanel).getSession());

	}

	@Override
	public synchronized Session5250 openSession(Properties sesProps, String configurationResource
			, String sessionName) {

		if(sessionName == null)
			sesProps.put(TN5250jConstants.SESSION_TERM_NAME,sesProps.getProperty(TN5250jConstants.SESSION_HOST));
		else
			sesProps.put(TN5250jConstants.SESSION_TERM_NAME,sessionName);

		if (configurationResource == null) configurationResource = "";

		sesProps.put(TN5250jConstants.SESSION_CONFIG_RESOURCE, configurationResource);

		SessionConfig useConfig = null;
		for (SessionConfig conf : configs) {
			if (conf.getSessionName().equals(sessionName)) {
				useConfig = conf;
			}
		}

		if (useConfig == null) {

			useConfig = new SessionConfig(configurationResource,sessionName);
			configs.add(useConfig);
		}

		Session5250 newSession = new Session5250(sesProps,configurationResource,
				sessionName,useConfig);
		sessions.addSession(newSession);
		return newSession;

	}

}