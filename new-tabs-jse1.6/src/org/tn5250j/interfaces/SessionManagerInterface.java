/*
 * @(#)SessionManagerInterface.java
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
package org.tn5250j.interfaces;

import java.util.Properties;

import org.tn5250j.Session5250;
import org.tn5250j.SessionPanel;
import org.tn5250j.framework.common.Sessions;

public interface SessionManagerInterface {

	/**
	 * @return
	 */
	public abstract Sessions getSessions();

	/**
	 * @param sessionObject
	 */
	public abstract void closeSession(SessionPanel sessionObject);

	/**
	 * @param props
	 * @param configurationResource
	 * @param sessionName
	 * @return
	 */
	public abstract Session5250 openSession(Properties props, String configurationResource, String sessionName);

}