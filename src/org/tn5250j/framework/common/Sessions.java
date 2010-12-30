/*
 * @(#)Sessions.java
 * Copyright:    Copyright (c) 2001
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.tn5250j.Session5250;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;


/**
 * Contains a collection of Session objects. This list is a static snapshot
 * of the list of Session objects available at the time of the snapshot.
 */
public class Sessions {

	public static final int DEFAULT_DELAY_MILLISEC = 15*1000;

	private static final AtomicInteger SEQUENCECOUNTER = new AtomicInteger(1);
	
	private final List<Session5250> sessions = new ArrayList<Session5250>(5);
	private final HeartBeatTask heartbeattask = new HeartBeatTask();
	private final TN5250jLogger log = TN5250jLogFactory.getLogger (this.getClass());

	private Timer heartBeater;

	protected void addSession(Session5250 newSession) {
		sessions.add(newSession);
		log.debug("adding Session: "+newSession.getSessionName());
		if (newSession.isSendKeepAlive() && heartBeater == null) {
			heartBeater = new Timer("heartbeater-"+SEQUENCECOUNTER.getAndIncrement(), true);
			heartBeater.scheduleAtFixedRate(heartbeattask, DEFAULT_DELAY_MILLISEC, DEFAULT_DELAY_MILLISEC);
		}
	}

	protected void removeSession(Session5250 session) {
		if (session != null) {
			log.debug("Removing session: " + session.getSessionName());
			if (session.isConnected()) {
				session.disconnect();
			}
			sessions.remove(session);
		}
	}

	protected void removeSession(String sessionName) {
		log.debug("Remove session by name: "+sessionName);
		removeSession(item(sessionName));
	}

	protected void removeSession(int index) {
		log.debug("Remove session by index: "+index);
		removeSession(item(index));
	}

	public int getCount() {
		return sessions.size();
	}

	public Session5250 item(int index) {
		return sessions.get(index);
	}

	public Session5250 item(String sessionName) {
		Session5250 s = null;
		int x = 0;
		while (x < sessions.size()) {
			s = sessions.get(x);
			if (s.getSessionName().equals(sessionName)) {
				return s;
			}
			x++;
		}
		return null;
	}

	public Session5250 item(Session5250 sessionObject) {
		Session5250 s = null;
		int x = 0;
		while (x < sessions.size()) {
			s = sessions.get(x);
			if (s.equals(sessionObject)) {
				return s;
			}
			x++;
		}
		return null;
	}

	/**
	 * @return A copy of the current sessions list.
	 */
	public List<Session5250> getSessionsList() {
		List<Session5250> newS = new ArrayList<Session5250>(sessions.size());
		for (int x = 0; x < sessions.size(); x++) {
			newS.add(sessions.get(x));
		}
		return newS;
	}

	/*
	 * ========================================================================
	 */
	
	/**
	 * Actually doing the heart beat (ping) towards the host. 
	 */
	private final class HeartBeatTask extends TimerTask {

		@Override
		public void run() {
			for (Session5250 session : getSessionsList()) {
				try {
					if (session.isConnected() && session.isSendKeepAlive()) {
						session.getVT().sendHeartBeat();
						if (log.isDebugEnabled()) {
							log.debug("sent heartbeat to " +  session.getSessionName());
						}
					}
				}
				catch (Exception ex) {
					log.warn(ex.getMessage());
				}
			}
		}
		
	}
}