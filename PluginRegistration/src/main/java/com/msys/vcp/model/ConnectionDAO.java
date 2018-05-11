package com.msys.vcp.model;

import java.net.URL;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConnectionDAO {
	private static Logger logger = LoggerFactory.getLogger(ConnectionDAO.class);
	// full url like https://vc-host:8443/sdk
	private String url;
	private String username;
	private String password;

	public String getUrl() {
		return url;
	}

	public ConnectionDAO() {
	}

	public ConnectionDAO(String url, String username, String password) {
		this.url = setValidUrl(url);
		this.username = username;
		this.password = password;
	}

	private String setValidUrl(String url) {
		if (!url.startsWith("https://"))
			url = "https://" + url;
		if (!url.endsWith("/sdk"))
			url = url + "/sdk";

		return url;
	}

	/*
	 * url ex: https:vcenter:443/sdk
	 */
	public void setUrl(String url) {
		this.url = setValidUrl(url);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public static void main(String[] args) throws Exception {
		ConnectionDAO c = new ConnectionDAO();
		c.setUrl("https://192.168.102.198:443/sdk");
		System.out.println("validating..");
		boolean res = c.validateURL();
		System.out.println(res);
	}

	public boolean validateURL() throws Exception {
		logger.info("validating url...");
		// validate url
		URL _url = new URL(url);
		// check for multicast or loopback
		java.net.InetAddress address = address(_url.getHost());

		if (address.isMulticastAddress()) {
			logger.error("Invalid host. {} is a multicast address.", _url.getHost());
			throw new Exception("Multicast address can not be used. Provide correct hostname or ip address.");
		}
		if (address.isLoopbackAddress()) {
			logger.error("Invalid host. {} is a loopback address.", _url.getHost());
			throw new Exception("Loopback address can not be used. Provide correct hostname or ip address.");
		}
		// ping host
		if (!address.isReachable(5000)) {
			logger.error("{} unreachable.", _url.getHost());
			throw new Exception(_url.getHost() + " is NOT reachable!");
		}
		return true;
	}

	@Override
	public String toString() {
		return "Connection [url=" + url + ", username=" + username + ", password=*******]";
	}

	public java.net.InetAddress address(final String value) throws UnknownHostException {
		return java.net.InetAddress.getByName(value);
	}

}
