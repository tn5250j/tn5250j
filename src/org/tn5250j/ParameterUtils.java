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
package org.tn5250j;

import org.tn5250j.framework.transport.SslType;
import org.tn5250j.gui.model.EmulSession;

/**
 * @author maki
 *
 */
public class ParameterUtils {

	public static final String PARAM_PORT = "-p";
	public static final String PARAM_CONFIGFILE = "-f";
	public static final String PARAM_HEARTBEAT = "-hb";
	public static final String PARAM_USEPCDEVNAME = "-dn=hostname";
	public static final String PARAM_NEWFRAME = "-noembed";
	public static final String PARAM_NEWJVM = "-nc";
	public static final String PARAM_MONITOR_START = "-d";
	public static final String PARAM_SSLTYPE = "-sslType";
	public static final String PARAM_USEPROXY = "-usp";
	public static final String PARAM_PROXY_HOST = "-sph";
	public static final String PARAM_PROXY_PORT = "-spp";
	public static final String PARAM_DEVNAME = "-dn";
	public static final String PARAM_USE_WIDTH132 = "-132";
	public static final String PARAM_USE_SYSNAME_DESCR = "-t";
	public static final String PARAM_CODEPAGE = "-cp";
	public static final String PARAM_EXT_MODE = "-e";

	/**
	 * @param arguments
	 * @return
	 */
	public static EmulSession loadSessionFromArguments(String[] args) {
		EmulSession session = new EmulSession();
		session.setHost(args[0]);
		if (isSpecified(PARAM_PORT, args)) {
			session.setPort(Integer.parseInt(Configure.getParm(PARAM_PORT, args)));
		}
		if (isSpecified(PARAM_CODEPAGE, args)){
			session.setCodepage(Configure.getParm(PARAM_CODEPAGE, args));
		}
		if (isSpecified(PARAM_DEVNAME, args)){
			session.setDevName(Configure.getParm(PARAM_DEVNAME, args));
		}
		if (isSpecified(PARAM_PROXY_PORT, args)) {
			session.setProxyPort(Integer.parseInt(Configure.getParm(PARAM_PROXY_PORT, args)));
		}
		if (isSpecified(PARAM_SSLTYPE, args)) {
			SslType sslt = SslType.valueOf(Configure.getParm(PARAM_SSLTYPE, args).toUpperCase());
			if (sslt!=null) {
				session.setSslType(sslt);
			} else {
				session.setSslType(SslType.NONE);
			}
		}
		if (isSpecified(PARAM_CONFIGFILE, args)){
			session.setConfigFile(Configure.getParm(PARAM_CONFIGFILE, args));
		}
		if (isSpecified(PARAM_PROXY_HOST, args)){
			session.setProxyHost(Configure.getParm(PARAM_PROXY_HOST, args));
		}
		session.setEnhancedMode(isSpecified(PARAM_EXT_MODE, args));
		session.setUseSysNameAsDescription(isSpecified(PARAM_USE_SYSNAME_DESCR, args));
		session.setUseWidth132(isSpecified(PARAM_USE_WIDTH132, args));
		session.setMonitorSessionStart(isSpecified(PARAM_MONITOR_START, args));
		session.setOpenNewJvm(isSpecified(PARAM_NEWJVM, args));
		session.setOpenNewFrame(isSpecified(PARAM_NEWFRAME, args));
		session.setUsePcAsDevName(isSpecified(PARAM_USEPCDEVNAME, args));
		session.setSendKeepAlive(isSpecified(PARAM_HEARTBEAT, args));
		session.setUseProxy(isSpecified(PARAM_USEPROXY, args));
		return session;
	}

	/**
	 * @param param
	 * @param arguments
	 * @return true if the arguments contain the given param
	 */
	public final static boolean isSpecified(String param, String[] arguments) {
		for (int x = 0; x < arguments.length; x++) {
			if (arguments[x] != null && arguments[x].equals(param))
				return true;
		}
		return false;
	}
}
