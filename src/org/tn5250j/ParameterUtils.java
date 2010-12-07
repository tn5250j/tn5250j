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

import org.tn5250j.gui.model.EmulSession;
import org.tn5250j.gui.model.SslType;

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
		if (Configure.isSpecified(PARAM_PORT, args)) {
			session.setPort(Integer.parseInt(Configure.getParm(PARAM_PORT, args)));
		}
		if (Configure.isSpecified(PARAM_CODEPAGE, args)){
			session.setCodepage(Configure.getParm(PARAM_CODEPAGE, args));
		}
		if (Configure.isSpecified(PARAM_DEVNAME, args)){
			session.setDevName(Configure.getParm(PARAM_DEVNAME, args));
		}
		if (Configure.isSpecified(PARAM_PROXY_PORT, args)) {
			session.setProxyPort(Integer.parseInt(Configure.getParm(PARAM_PROXY_PORT, args)));
		}
		if (Configure.isSpecified(PARAM_SSLTYPE, args)) {
			SslType sslt = SslType.valueOf(Configure.getParm(PARAM_SSLTYPE, args).toUpperCase());
			if (sslt!=null) {
				session.setSslType(sslt);
			} else {
				session.setSslType(SslType.NONE);
			}
		}
		if (Configure.isSpecified(PARAM_CONFIGFILE, args)){
			session.setConfigFile(Configure.getParm(PARAM_CONFIGFILE, args));
		}
		if (Configure.isSpecified(PARAM_PROXY_HOST, args)){
			session.setProxyHost(Configure.getParm(PARAM_PROXY_HOST, args));
		}
		session.setEnhancedMode(Configure.isSpecified(PARAM_EXT_MODE, args));
		session.setUseSysNameAsDescription(Configure.isSpecified(PARAM_USE_SYSNAME_DESCR, args));
		session.setUseWidth132(Configure.isSpecified(PARAM_USE_WIDTH132, args));
		session.setMonitorSessionStart(Configure.isSpecified(PARAM_MONITOR_START, args));
		session.setOpenNewJvm(Configure.isSpecified(PARAM_NEWJVM, args));
		session.setOpenNewFrame(Configure.isSpecified(PARAM_NEWFRAME, args));
		session.setUsePcAsDevName(Configure.isSpecified(PARAM_USEPCDEVNAME, args));
		session.setSendKeepAlive(Configure.isSpecified(PARAM_HEARTBEAT, args));
		session.setUseProxy(Configure.isSpecified(PARAM_USEPROXY, args));
		return session;
	}

}
