package org.tn5250j.transport.SSL;

/*
 * @(#)SSLImplementation.java
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

import java.net.Socket;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLContext;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HandshakeCompletedEvent;

import java.security.KeyStore;
import java.security.SecureRandom;
import org.tn5250j.transport.SSLInterface;
import org.tn5250j.transport.SSLConstants;

public class SSLImplementation implements SSLInterface {

  SSLContext sslctx = null;
  KeyStore ks = null;
  KeyManagerFactory kmf = null;
  SecureRandom prng = null;
  TrustManagerFactory tmf = null;
  TrustManager[] trustManagers = null;
  String sslType = null;

  public SSLImplementation () {

  }

  public SSLImplementation (String sslType) {

    this.sslType = sslType;
  }

  public void setSSLType(String type) {
    sslType = type;
  }

  private void initKeyStore () {
    try {
      ks = KeyStore.getInstance("JKS");
      System.out.println("Loading Keystore...");
      String seperator=System.getProperty("file.separator","/");
      ks.load(new java.io.FileInputStream(System.getProperty("java.home")+
                seperator+"lib"+seperator+"security"+seperator+"cacerts"),
                "changeit".toCharArray());
    }
    catch (Exception e) {
      System.err.println("MySSLFactory: " + e.getMessage());
    }
  }

  private void initKeyManagerFactory() {
    try {
      System.out.println("Initializing KeyManagerFactory...");
      kmf = KeyManagerFactory.getInstance("SunX509");
      kmf.init(ks,"changeit".toCharArray());
    }
    catch (Exception e) {
      System.err.println("MySSLFactory: " + e.getMessage());
    }
  }

  private void initTrustManagers() {
    try {
      System.out.println("Instantiating TrustManager...");
      tmf = TrustManagerFactory.getInstance("SunX509");
      tmf.init(ks);
      trustManagers = tmf.getTrustManagers();
      X509TrustManager myTrustManager = new X509CertificateTrustManager(
          ((X509TrustManager)trustManagers[0]),ks);
      TrustManager[] newTrustManagers = new TrustManager[1];
      newTrustManagers[0] = myTrustManager;
      trustManagers = newTrustManagers;
    }
    catch (Exception e) {
      System.err.println("My5250SocketFactory: initTrustManager: " +
        e.getMessage());
    }
  }

  private void initPrng() {
    System.out.println("Initializing PRNG...");
    SecureRandom prng = new SecureRandom();
    prng.nextInt();
  }

  private void initSSLContext(String type) {
    try {
      System.out.println("Creating and Initializing SSL Context...");
      sslctx = SSLContext.getInstance(type);
      sslctx.init(kmf.getKeyManagers(),trustManagers,prng);
    }
    catch (Exception e) {
      System.err.println("MySSLFactory: " + e.getMessage());
    }

  }

  public Socket createSSLSocket(String destination, int port) {
    try {

      //Using SSL Socket
      initKeyStore();
      initKeyManagerFactory();
      initTrustManagers();
      initPrng();
      System.out.println("Creating Secure Socket");
      if (sslType.equals(SSLConstants.SSL_TYPE_SSLv2)) {
        initSSLContext("SSL");
      }
      else if (sslType.equals(SSLConstants.SSL_TYPE_TLS)) {
        initSSLContext("TLS");
      }
      else {
        System.err.println("SSL Type not Supported");
        return null;
      }
      SSLSocket sslsock = (SSLSocket)sslctx.getSocketFactory().createSocket(destination,port);
        sslsock.addHandshakeCompletedListener(new HandshakeCompletedListener() {
          public void handshakeCompleted(HandshakeCompletedEvent hsce) {
            System.out.println("Handshake Successful: " + hsce.getCipherSuite());
          }
      });
      return sslsock;
    }
    catch (Exception e) {
      System.err.println("MySSLFactory: createSocket: " + e.getMessage());
    }
    return null;
  }
}