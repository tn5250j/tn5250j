/**
 * $Id$
 * 
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2009
 * Company:
 * @author: master_jaf
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
package org.tn5250j.gui.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EmulConfig {

	private List<EmulSession> sessions = new ArrayList<EmulSession>();
	private EmulSession defaultSess;
	private String jarClassPaths = "";
	private int width = 700;
	private int height = 500;
	private String[] views = new String[0];
	private Locale locale = Locale.getDefault();
	private boolean startLastView = false;
	private boolean showConnectDialog = true;
	
	/**
	 * @param connection
	 */
	public void addSession(EmulSession connection) {
		if (connection != null) {
			sessions.add(connection);
		}
	}
	
	/**
	 * @return
	 */
	public int getSessionCount() {
		return sessions.size();
	}
	
	/**
	 * @param connection
	 */
	public void removeSession(EmulSession connection) {
		if (connection != null) {
			sessions.remove(connection);
		}
	}

	/**
	 * @return
	 */
	public List<EmulSession> getSessions() {
		return sessions;
	}

	/**
	 * @return
	 */
	public EmulSession getDefaultSess() {
		return defaultSess;
	}

	/**
	 * @param defaultSess
	 */
	public void setDefaultSess(EmulSession defaultSess) {
		this.defaultSess = defaultSess;
	}
	
	/**
	 * @param sessionname
	 * @return
	 */
	public boolean hasSession(String sessionname) {
		return getSessionByName(sessionname) != null;
	}
	
	/**
	 * @param name
	 * @return
	 */
	public EmulSession getSessionByName(String name) {
		for (EmulSession es : sessions) {
			if (es.getName().equals(name)) {
				return es;
			}
		}
		return null;
	}

	/**
	 * @return
	 */
	public String getJarClassPaths() {
		return jarClassPaths;
	}

	/**
	 * @param jarClassPaths
	 */
	public void setJarClassPaths(String jarClassPaths) {
		this.jarClassPaths = jarClassPaths;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return the views
	 */
	public String[] getViews() {
		return views;
	}

	/**
	 * @param views the views to set
	 */
	public void setViews(String[] views) {
		this.views = views;
	}

	/**
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param locale the locale to set
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * @return the startLastView
	 */
	public boolean isStartLastView() {
		return startLastView;
	}

	/**
	 * @param startLastView the startLastView to set
	 */
	public void setStartLastView(boolean startLastView) {
		this.startLastView = startLastView;
	}

	/**
	 * @return the showConnectDialog
	 */
	public boolean isShowConnectDialog() {
		return showConnectDialog;
	}

	/**
	 * @param showConnectDialog the showConnectDialog to set
	 */
	public void setShowConnectDialog(boolean showConnectDialog) {
		this.showConnectDialog = showConnectDialog;
	}

}
