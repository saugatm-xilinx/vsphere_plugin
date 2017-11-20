package com.msys.vcp.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class CertThumbprint {

	public static void main(String[] args) throws Exception {
		System.out.println(getSHAFingerprint("localhost", 8443));
		// F4:74:EB:21:11:88:25:AC:DB:7F:1A:6C:F3:05:85:04:BB:F1:EB:52
		// f4:74:eb:21:11:88:25:ac:db:7f:1a:6c:f3:05:85:04:bb:f1:eb:52
		// F4:74:EB:21:11:88:25:AC:DB:7F:1A:6C:F3:05:85:04:BB:F1:EB:52
		System.out.println("done!!!");

	}

	public static String getSHAFingerprint(String host, int port) throws Exception {
		HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String urlHostName, SSLSession session) {
				return true;
			}
		};
		TrustAllTrustManager.trustAllHttpsCertificates();
		HttpsURLConnection.setDefaultHostnameVerifier(hv);

		SSLSocketFactory sf = HttpsURLConnection.getDefaultSSLSocketFactory();

		SSLSocket socket = (SSLSocket) sf.createSocket(host, port);

		return getSHA1(socket.getSession());

	}

	private static String getThumbprint(X509Certificate cert) throws CertificateException {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] certBytes = cert.getEncoded();
			byte[] bytes = md.digest(certBytes);

			StringBuilder builder = new StringBuilder();
			for (byte b : bytes) {
				String hex = Integer.toHexString(0xFF & b);
				if (hex.length() == 1) {
					builder.append("0");
				}
				builder.append(hex).append(":");
			}

			return builder.substring(0, builder.length() - 1);
		} catch (NoSuchAlgorithmException ex) {
			return null;
		}
	}

	public static String getSHA1(SSLSession session) throws CertificateException, SSLPeerUnverifiedException {
		Certificate[] certificates = session.getPeerCertificates();
		return getThumbprint((X509Certificate) certificates[0]);

	}
}
