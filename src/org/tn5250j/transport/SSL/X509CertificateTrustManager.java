package org.tn5250j.transport.SSL;

/*
 * @(#)X509CertificateTrustManager.java
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
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import javax.swing.JOptionPane;

public class X509CertificateTrustManager implements X509TrustManager {

  KeyStore ks = null;
  X509TrustManager trustManager = null;

  public X509CertificateTrustManager(X509TrustManager manager, KeyStore keyStore) {
    trustManager = manager;
    ks = keyStore;
  }

  public void checkClientTrusted(X509Certificate[] chain, String type) throws java.security.cert.CertificateException {

  }
  public void checkServerTrusted(X509Certificate[] chain, String type) throws java.security.cert.CertificateException {
    try {
      trustManager.checkServerTrusted(chain,type);
      return;
    }
    catch (CertificateException ce) {
      X509Certificate cert = chain[0];
      String certInfo = "Version: " + cert.getVersion() + "\n";
      certInfo = certInfo.concat("Serial Number: " + cert.getSerialNumber()+"\n");
      certInfo = certInfo.concat("Signature Algorithm: " + cert.getSigAlgName()+"\n");
      certInfo = certInfo.concat("Issuer: " + cert.getIssuerDN().getName()+"\n");
      certInfo = certInfo.concat("Valid From: " + cert.getNotBefore()+"\n");
      certInfo = certInfo.concat("Valid To: " + cert.getNotAfter()+"\n");
      certInfo = certInfo.concat("Subject DN: " + cert.getSubjectDN().getName()+"\n");
      certInfo = certInfo.concat("Public Key: " + cert.getPublicKey().getFormat()+"\n");

      int accept = JOptionPane.showConfirmDialog(null,certInfo,
                  "Accept Certificate",javax.swing.JOptionPane.YES_NO_OPTION);
      if (accept != JOptionPane.YES_OPTION) {
        throw new java.security.cert.CertificateException("Certificate Not Accepted");
      }
    }
  }

  public X509Certificate[] getAcceptedIssuers() {
    return trustManager.getAcceptedIssuers();
  }
}