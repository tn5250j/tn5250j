package org.tn5250j.framework.transport.SSL;

/*
 * @(#)SSLImplementation.java
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

import java.net.Socket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLContext;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManagerFactory;

import java.security.KeyStore;
import java.security.SecureRandom;
import org.tn5250j.framework.transport.SSLInterface;

import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

/**
 * <p>
 * This class implements the SSLInterface and is used to create SSL socket
 * instances.
 * </p>
 * <p>
 * This class uses an X509CertificateTrustManager instance to perform
 * certificate validation during handshaking.
 * </p> 
 * @author Stephen M. Kennedy <skennedy@tenthpowertech.com>
 *
 */
public class SSLImplementation implements SSLInterface {

  SSLContext sslctx = null;
  KeyStore ks = null;
  KeyManagerFactory kmf = null;
  SecureRandom prng = null;
  TrustManagerFactory tmf = null;
  TrustManager[] trustManagers = null;
  String sslType = null;

  private char[] keystorePassword = "changeit".toCharArray();
  
  TN5250jLogger logger;
  
  public SSLImplementation () {
  	logger = TN5250jLogFactory.getLogger(getClass());
  }

  public SSLImplementation (String sslType) {
  	this();
    this.sslType = sslType;
  }

  public void setSSLType(String type) {
    sslType = type;
  }

  /**
   * Initialize the keystore where certificates are loaded from and stored to.
   *
   */
  private void initKeyStore () {
    try {
      ks = KeyStore.getInstance(KeyStore.getDefaultType());
      logger.debug("Loading Keystore...");
      
      String seperator=System.getProperty("file.separator","/");
      
      ks.load(new java.io.FileInputStream(System.getProperty("java.home")+
                seperator+"lib"+seperator+"security"+seperator+"cacerts"),
                keystorePassword);
      
    }
    catch (Exception e) {
    	logger.error("Failed Initializing Keystore ["+e.getMessage()+"]");
    }
  }

  
  /**
   * Initialize the key manager factory  
   *
   */
  private void initKeyManagerFactory() {
    try {
    	logger.debug("Initializing KeyManagerFactory...");
      kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(ks,keystorePassword);
    }
    catch (Exception e) {
    	logger.error("Failed initializing Key Manager Factory ["+e.getMessage()+"]");
    }
  }

  /**
   * Initialize the trust managers
   *
   */
  private void initTrustManagers() {
    try {
    	logger.debug("Instantiating TrustManager...");
      tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmf.init(ks);
      trustManagers = tmf.getTrustManagers();
      X509TrustManager myTrustManager = 
      	new X509CertificateTrustManager(trustManagers,ks);
      TrustManager[] newTrustManagers = new TrustManager[1];
      newTrustManagers[0] = myTrustManager;
      trustManagers = newTrustManagers;
    }
    catch (Exception e) {
    	logger.error("Failed initializing Trust Managers ["+e.getMessage()+"]");
    }
  }

  
  private void initPrng() {
    logger.debug("Initializing PRNG...");
    SecureRandom prng = new SecureRandom();
    prng.nextInt();
  }

  private void initSSLContext(String type) {
    try {
    	logger.debug("Creating and Initializing SSL Context...");
      sslctx = SSLContext.getInstance(type);
      sslctx.init(kmf.getKeyManagers(),trustManagers,prng);
    }
    catch (Exception e) {
    	logger.error("Failed initializing SSL Context ["+e.getMessage()+"]");
    }

  }

  public Socket createSSLSocket(String destination, int port) {
  	SSLSocket socket = null;
    try {
      //Using SSL Socket
      initKeyStore();
      initKeyManagerFactory();
      initTrustManagers();
      initPrng();
      initSSLContext(sslType);
      socket = (SSLSocket)sslctx.getSocketFactory().createSocket(destination,port);
    }
    catch (Exception e) {
    	logger.error("Error creating ssl socket ["+e.getMessage()+"]");
    }
    return socket;
  }
}