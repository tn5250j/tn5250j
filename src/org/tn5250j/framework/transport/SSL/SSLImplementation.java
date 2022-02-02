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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.tn5250j.GlobalConfigure;
import org.tn5250j.framework.transport.SSLInterface;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

/**
 * <p>
 * This class implements the SSLInterface and is used to create SSL socket
 * instances.
 * </p>
 *
 * @author Stephen M. Kennedy <skennedy@tenthpowertech.com>
 */
public class SSLImplementation implements SSLInterface, X509TrustManager {

    SSLContext sslContext = null;

    KeyStore userks = null;
    private String userKsPath;
    private char[] userksPassword = "changeit".toCharArray();

    KeyManagerFactory userkmf = null;

    TrustManagerFactory usertmf = null;

    TrustManager[] userTrustManagers = null;

    X509Certificate[] acceptedIssuers;

    TN5250jLogger logger;

    public SSLImplementation() {
        logger = TN5250jLogFactory.getLogger(getClass());
    }

    @Override
    public void init(final String sslType) {
        try {
            logger.debug("Initializing User KeyStore");
            userKsPath = System.getProperty("user.home") + File.separator
                    + GlobalConfigure.TN5250J_FOLDER + File.separator + "keystore";
            final File userKsFile = new File(userKsPath);
            userks = KeyStore.getInstance(KeyStore.getDefaultType());
            userks.load(userKsFile.exists() ? new FileInputStream(userKsFile)
                    : null, userksPassword);
            logger.debug("Initializing User Key Manager Factory");
            userkmf = KeyManagerFactory.getInstance(KeyManagerFactory
                    .getDefaultAlgorithm());
            userkmf.init(userks, userksPassword);
            logger.debug("Initializing User Trust Manager Factory");
            usertmf = TrustManagerFactory.getInstance(TrustManagerFactory
                    .getDefaultAlgorithm());
            usertmf.init(userks);
            userTrustManagers = usertmf.getTrustManagers();
            logger.debug("Initializing SSL Context");
            sslContext = SSLContext.getInstance(sslType);
            sslContext.init(userkmf.getKeyManagers(), new TrustManager[]{this}, null);
        } catch (final Exception ex) {
            logger.error("Error initializing SSL [" + ex.getMessage() + "]");
        }

    }

    @Override
    public Socket createSSLSocket(final String destination, final int port) {
        if (sslContext == null)
            throw new IllegalStateException("SSL Context Not Initialized");
        SSLSocket socket = null;
        try {
            socket = (SSLSocket) sslContext.getSocketFactory().createSocket(
                    destination, port);
        } catch (final Exception e) {
            logger.error("Error creating ssl socket [" + e.getMessage() + "]");
        }
        return socket;
    }

    // X509TrustManager Methods

    /*
     * (non-Javadoc)
     *
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return acceptedIssuers;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.
     * X509Certificate[], java.lang.String)
     */
    @Override
    public void checkClientTrusted(final X509Certificate[] arg0, final String arg1)
            throws CertificateException {
        throw new SecurityException("checkClientTrusted unsupported");

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.
     * X509Certificate[], java.lang.String)
     */
    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String type)
            throws CertificateException {
        try {
            for (int i = 0; i < userTrustManagers.length; i++) {
                if (userTrustManagers[i] instanceof X509TrustManager) {
                    final X509TrustManager trustManager = (X509TrustManager) userTrustManagers[i];
                    final X509Certificate[] calist = trustManager
                            .getAcceptedIssuers();
                    if (calist.length > 0) {
                        trustManager.checkServerTrusted(chain, type);
                    } else {
                        throw new CertificateException(
                                "Empty list of accepted issuers (a.k.a. root CA list).");
                    }
                }
            }
            return;
        } catch (final CertificateException ce) {
            final X509Certificate cert = chain[0];
            String certInfo = "Version: " + cert.getVersion() + "\n";
            certInfo = certInfo.concat("Serial Number: "
                    + cert.getSerialNumber() + "\n");
            certInfo = certInfo.concat("Signature Algorithm: "
                    + cert.getSigAlgName() + "\n");
            certInfo = certInfo.concat("Issuer: "
                    + cert.getIssuerDN().getName() + "\n");
            certInfo = certInfo.concat("Valid From: " + cert.getNotBefore()
                    + "\n");
            certInfo = certInfo
                    .concat("Valid To: " + cert.getNotAfter() + "\n");
            certInfo = certInfo.concat("Subject DN: "
                    + cert.getSubjectDN().getName() + "\n");
            certInfo = certInfo.concat("Public Key: "
                    + cert.getPublicKey().getFormat() + "\n");

            if (!UiUtils.showYesNoConfirm(certInfo, "Unknown Certificate - Do you accept it?")) {
                throw new java.security.cert.CertificateException(
                        "Certificate Rejected");
            }

            if (UiUtils.showYesNoConfirm("Remember this certificate?", "Save Certificate")) {
                try {
                    userks.setCertificateEntry(cert.getSubjectDN().getName(),
                            cert);
                    userks.store(new FileOutputStream(userKsPath),
                            userksPassword);
                } catch (final Exception e) {
                    logger.error("Error saving certificate [" + e.getMessage()
                            + "]");
                    e.printStackTrace();
                }
            }
        }

    }
}
