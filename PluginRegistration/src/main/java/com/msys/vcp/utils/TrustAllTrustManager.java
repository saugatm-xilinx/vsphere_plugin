package com.msys.vcp.utils;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Trust manager that does not validate certificate chains.
 */
public class TrustAllTrustManager implements X509TrustManager, TrustManager {

	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}

	public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
		return true;
	}

	public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
		return true;
	}

	public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
		return;
	}

	public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
			throws CertificateException {
		return;
	}

	/**
	 * Creates a trust manager that does not validate certificate chains
	 *
	 * @throws Exception
	 */
	public static void trustAllHttpsCertificates() throws Exception {
		TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
		TrustManager tm = new TrustAllTrustManager();
		trustAllCerts[0] = tm;
		javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
		javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();
		sslsc.setSessionTimeout(0);
		sc.init(null, trustAllCerts, null);
		javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}

}
