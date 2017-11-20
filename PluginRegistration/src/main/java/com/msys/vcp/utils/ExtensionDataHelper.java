package com.msys.vcp.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.msys.vcp.model.ExtensionData;

public class ExtensionDataHelper {
	private static Logger logger = LoggerFactory.getLogger(ExtensionDataHelper.class);

	private static final String CONF_FILE = "/../registerPlugin.properties";

	public static ExtensionData getDefaultData() throws IOException {
		logger.info("getDefaultData called.");
		Properties props = loadProperties();
		logger.debug("Loaded properties: {}", props);
		if (props == null)
			return null;

		ExtensionData data = new ExtensionData();
		loadProperties(data, props);
		logger.info("default data: {}.", data);
		return data;
	}

	private static void loadProperties(ExtensionData data, Properties props) {
		// create url
		String host = props.getProperty("pluginHost", "");
		int port = Integer.parseInt(props.getProperty("pluginPort", "8443"));

		String defaultPath = "solarflare-vcp/solarflare-" + props.getProperty("version", "1.0.0") + ".zip";

		StringBuilder urlBuilder = new StringBuilder(props.getProperty("pluginScheme", "https")).append("://")
				.append(host).append(":").append(port).append("/").append(props.getProperty("pluginPath", defaultPath));

		// data.setPluginUrl(props.getProperty("pluginUrl", ""));
		data.setPluginUrl(urlBuilder.toString());
		data.setKey(props.getProperty("key", ""));
		data.setName(props.getProperty("name", ""));
		data.setSummary(props.getProperty("summary", ""));
		data.setVersion(props.getProperty("version", ""));
		data.setCompany(props.getProperty("company", ""));
		data.setShowInSolutionManager(Boolean.parseBoolean(props.getProperty("showInSolutionManager", "false")));
		String fingerprint = props.getProperty("serverThumbprint", "");
		String sha1 = null;
		try {
			sha1 = (fingerprint == null || fingerprint.isEmpty()) ? CertThumbprint.getSHAFingerprint(host, port)
					: fingerprint;
		} catch (Exception e) {
			// log error. not able to get server Thumbprint
		}
		data.setServerThumbprint(sha1);
	}

	private static Properties loadProperties() throws IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream input = classLoader.getResourceAsStream(CONF_FILE);

		if (input == null)
			throw new IOException("Error in reading properties file!");

		Properties properties = new Properties();
		properties.load(input);
		return properties;
	}

}
