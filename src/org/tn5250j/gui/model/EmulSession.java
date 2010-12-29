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

import org.tn5250j.Session5250;

public class EmulSession implements Cloneable {

	private String name = "name";
	private String host = "127.0.0.1";
	private boolean defaultCon = false;
	
	private boolean openNewFrame = false;
	private boolean monitorSessionStart = false;
	private boolean openNewJvm = false;
	
	private int port = 23;
	private String devName;
	private boolean usePcAsDevName = false;
	private SslType sslType = SslType.NONE;
	private boolean sendKeepAlive = true;
	
	private String configFile;
	private boolean useWidth132 = true;
	private String codepage = "1140";
	private boolean useAs400Toolbox = false;
	private boolean enhancedMode = true;
	private boolean useSysNameAsDescription = true;
	
	private boolean useProxy = false;
	private String proxyHost;
	private int proxyPort = Session5250.DEFAULT_PROXY_PORT;
	
	public EmulSession() {
		// allow default constructor
	}
	
	public EmulSession(String name, String host, boolean defaultCon) {
		super();
		this.name = name;
		this.host = host;
		this.defaultCon = defaultCon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public boolean isDefaultCon() {
		return defaultCon;
	}

	public void setDefaultCon(boolean defaultCon) {
		this.defaultCon = defaultCon;
	}

	public boolean isOpenNewFrame() {
		return openNewFrame;
	}

	public void setOpenNewFrame(boolean openNewFrame) {
		this.openNewFrame = openNewFrame;
	}

	public boolean isMonitorSessionStart() {
		return monitorSessionStart;
	}

	public void setMonitorSessionStart(boolean monitorSessionStart) {
		this.monitorSessionStart = monitorSessionStart;
	}

	public boolean isOpenNewJvm() {
		return openNewJvm;
	}

	public void setOpenNewJvm(boolean openNewJvm) {
		this.openNewJvm = openNewJvm;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getDevName() {
		return devName;
	}

	public void setDevName(String devName) {
		this.devName = devName;
	}

	public boolean isUsePcAsDevName() {
		return usePcAsDevName;
	}

	public void setUsePcAsDevName(boolean usePcAsDevName) {
		this.usePcAsDevName = usePcAsDevName;
	}

	public SslType getSslType() {
		return sslType;
	}

	public void setSslType(SslType sslType) {
		this.sslType = sslType;
	}

	public boolean isSendKeepAlive() {
		return sendKeepAlive;
	}

	public void setSendKeepAlive(boolean sendKeepAlive) {
		this.sendKeepAlive = sendKeepAlive;
	}

	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public boolean isUseWidth132() {
		return useWidth132;
	}

	public void setUseWidth132(boolean useWidth132) {
		this.useWidth132 = useWidth132;
	}

	public String getCodepage() {
		return codepage;
	}

	public void setCodepage(String codepage) {
		this.codepage = codepage;
	}

	public boolean isUseAs400Toolbox() {
		return useAs400Toolbox;
	}

	public void setUseAs400Toolbox(boolean useAs400Toolbox) {
		this.useAs400Toolbox = useAs400Toolbox;
	}

	public boolean isEnhancedMode() {
		return enhancedMode;
	}

	public void setEnhancedMode(boolean enhancedMode) {
		this.enhancedMode = enhancedMode;
	}

	public boolean isUseSysNameAsDescription() {
		return useSysNameAsDescription;
	}

	public void setUseSysNameAsDescription(boolean useSysNameAsDescription) {
		this.useSysNameAsDescription = useSysNameAsDescription;
	}

	public boolean isUseProxy() {
		return useProxy;
	}

	public void setUseProxy(boolean useProxy) {
		this.useProxy = useProxy;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

}
