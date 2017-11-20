/* Copyright (c) 2016-2017 VMware, Inc. All rights reserved. */

package com.msys.solarflare.helper;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Host name verifier by thumbprint
 */
public class ThumbprintHostNameVerifier implements HostnameVerifier {

   @Override
   public boolean verify(String host, SSLSession session) {
      try {
         Certificate[] certificates = session.getPeerCertificates();
         verify(host, (X509Certificate) certificates[0]);
         return true;
      } catch (SSLException e) {
         return false;
      }
   }


   private void verify(String host, X509Certificate cert) throws SSLException {
      try {
         ThumbprintTrustManager.checkThumbprint(cert);
      } catch(CertificateException e){
         throw new SSLException(e.getMessage());
      }
   }
}
