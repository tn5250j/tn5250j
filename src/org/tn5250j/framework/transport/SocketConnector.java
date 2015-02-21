
/**
 * @(#)SocketConnector.java
 * @author Stephen M. Kennedy
 *
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
package org.tn5250j.framework.transport;

import java.net.Socket;

import org.tn5250j.TN5250jConstants;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

public class SocketConnector {

  String sslType = null;

  TN5250jLogger logger;

  /**
   * Creates a new instance that creates a plain socket by default.
   */
  public SocketConnector() {
  	logger = TN5250jLogFactory.getLogger(getClass());
  }

  /**
   * Set the type of SSL connection to use.  Specify null or an empty string
   * to use a plain socket. 
   * @param type The SSL connection type
   * @see org.tn5250j.framework.transport.SSLConstants
   */
  public void setSSLType(String type) {
    sslType = type;
  }

  /**
   * Create a new client Socket to the given destination and port.  If an SSL
   * socket type has not been specified <i>(by setSSLType(String))</i>, then
   * a plain socket will be created.  Otherwise, a new SSL socket of the 
   * specified type will be created.
   * @param destination
   * @param port
   * @return a new client socket, or null if  
   */
  public Socket createSocket(String destination, int port) {

  	Socket socket = null;
  	Exception ex = null;
  	
      if (sslType == null || sslType.trim().length() == 0 || 
      		sslType.toUpperCase().equals(TN5250jConstants.SSL_TYPE_NONE)) {
        	logger.info("Creating Plain Socket");
        try {
			// Use Socket Constructor!!! SocketFactory for jdk 1.4
			socket = new Socket(destination,port);
		} catch (Exception e) {
			ex = e;
		}
      } else {  //SSL SOCKET

   		logger.info("Creating SSL ["+sslType+"] Socket");      
      
      	SSLInterface sslIf = null;
      	
      	String sslImplClassName = 
      		"org.tn5250j.framework.transport.SSL.SSLImplementation";  
		try {
			Class<?> c = Class.forName(sslImplClassName);
			sslIf = (SSLInterface)c.newInstance();
		} catch (Exception e) {
			ex = new Exception("Failed to create SSLInterface Instance. " +
					"Message is ["+e.getMessage()+"]");
		}
		
      	if (sslIf != null) {
      		sslIf.init(sslType);
      		socket = sslIf.createSSLSocket(destination,port);
      	}
      }

      if (ex != null) {
      	logger.error(ex);
      }
      if (socket == null) {
      	logger.warn("No socket was created");
      }
      return socket;
  }
      
      
}