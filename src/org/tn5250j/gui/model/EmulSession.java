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
import org.tn5250j.framework.transport.SslType;

public class EmulSession implements Cloneable {

	public static final int DEFAULT_PORT = 23;
	public static final int DEFAULT_SSL_PORT = 992;
	
	private String name = "name";
	private String host = "127.0.0.1";
	private boolean defaultCon = false;
	
	private boolean openNewFrame = false;
	private boolean monitorSessionStart = false;
	private boolean openNewJvm = false;
	
	private int port = DEFAULT_PORT;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((codepage == null) ? 0 : codepage.hashCode());
		result = prime * result
				+ ((configFile == null) ? 0 : configFile.hashCode());
		result = prime * result + (defaultCon ? 1231 : 1237);
		result = prime * result + ((devName == null) ? 0 : devName.hashCode());
		result = prime * result + (enhancedMode ? 1231 : 1237);
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + (monitorSessionStart ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (openNewFrame ? 1231 : 1237);
		result = prime * result + (openNewJvm ? 1231 : 1237);
		result = prime * result + port;
		result = prime * result
				+ ((proxyHost == null) ? 0 : proxyHost.hashCode());
		result = prime * result + proxyPort;
		result = prime * result + (sendKeepAlive ? 1231 : 1237);
		result = prime * result + ((sslType == null) ? 0 : sslType.hashCode());
		result = prime * result + (useAs400Toolbox ? 1231 : 1237);
		result = prime * result + (usePcAsDevName ? 1231 : 1237);
		result = prime * result + (useProxy ? 1231 : 1237);
		result = prime * result + (useSysNameAsDescription ? 1231 : 1237);
		result = prime * result + (useWidth132 ? 1231 : 1237);
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EmulSession other = (EmulSession) obj;
		if (codepage == null) {
			if (other.codepage != null)
				return false;
		} else if (!codepage.equals(other.codepage))
			return false;
		if (configFile == null) {
			if (other.configFile != null)
				return false;
		} else if (!configFile.equals(other.configFile))
			return false;
		if (defaultCon != other.defaultCon)
			return false;
		if (devName == null) {
			if (other.devName != null)
				return false;
		} else if (!devName.equals(other.devName))
			return false;
		if (enhancedMode != other.enhancedMode)
			return false;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (monitorSessionStart != other.monitorSessionStart)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (openNewFrame != other.openNewFrame)
			return false;
		if (openNewJvm != other.openNewJvm)
			return false;
		if (port != other.port)
			return false;
		if (proxyHost == null) {
			if (other.proxyHost != null)
				return false;
		} else if (!proxyHost.equals(other.proxyHost))
			return false;
		if (proxyPort != other.proxyPort)
			return false;
		if (sendKeepAlive != other.sendKeepAlive)
			return false;
		if (sslType != other.sslType)
			return false;
		if (useAs400Toolbox != other.useAs400Toolbox)
			return false;
		if (usePcAsDevName != other.usePcAsDevName)
			return false;
		if (useProxy != other.useProxy)
			return false;
		if (useSysNameAsDescription != other.useSysNameAsDescription)
			return false;
		if (useWidth132 != other.useWidth132)
			return false;
		return true;
	}
	
}
