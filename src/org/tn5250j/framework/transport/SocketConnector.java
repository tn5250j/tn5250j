
/*
 * @(#)SocketConnector.java
 * @author Steve Kennedy
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

public class SocketConnector {

  String sslType = null;


  public SocketConnector() {

    sslType = System.getProperty(SSLConstants.SSL_TYPE,SSLConstants.SSL_TYPE_NONE);
  }

  public void setSSLType(String type) {
    sslType = type;
  }

  public Socket createSocket(String destination, int port) {
    try {

      if (sslType.equals(SSLConstants.SSL_TYPE_NONE)) {
        System.out.println("Creating Socket");
        // for jdk 1.4
//        return SocketFactory.getDefault().createSocket(destination,port);
        return new Socket(destination,port);
      }

      //Using SSL Socket
      SSLInterface o = null;
      try {
         Class c = Class.forName("org.tn5250j.transport.SSL.SSLImplementation");
         o = (SSLInterface)c.newInstance();
         o.setSSLType(sslType);
         return o.createSSLSocket(destination,port);
      }
      catch (Exception e) {
         System.err.println(e);
      }
    }
    catch (Exception e) {
      System.err.println("SocketConnector: createSocket: " + e.getMessage());
    }
    return null;
  }
}