package com.solarflare.vcp.vim25;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class DisableSecurity {

	/*
	 * Authentication is handled by using a TrustManager and supplying a host
	 * name verifier method. (The host name verifier is declared in the main
	 * function.)
	 *
	 * Do not use this in production code! It is only for samples.
	 */
	private static class TrustAllTrustManager implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {

		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}

		public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}
	}

	public static void trustEveryone() throws NoSuchAlgorithmException, KeyManagementException {
		// Declare a host name verifier that will automatically enable
		// the connection. The host name verifier is invoked during
		// the SSL handshake.
		javax.net.ssl.HostnameVerifier verifier = new HostnameVerifier() {
			public boolean verify(String urlHostName, SSLSession session) {
				return true;
			}
		};
		// Create the trust manager.
		javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
		javax.net.ssl.TrustManager trustManager = new TrustAllTrustManager();
		trustAllCerts[0] = trustManager;

		// Create the SSL context
		javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");

		// Create the session context
		javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();

		// Initialize the contexts; the session context takes the trust manager.
		sslsc.setSessionTimeout(0);
		sc.init(null, trustAllCerts, null);

		// Use the default socket factory to create the socket for the secure
		// connection
		javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		// Set the default host name verifier to enable the connection.
		HttpsURLConnection.setDefaultHostnameVerifier(verifier);
	}

}