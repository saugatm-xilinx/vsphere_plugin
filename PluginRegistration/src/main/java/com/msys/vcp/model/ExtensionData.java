package com.msys.vcp.model;

public class ExtensionData {

	/**
	 * Extension plugin type
	 */
	private static final String DEFAULT_PLUGIN_TYPE = "vsphere-client-serenity";
	/**
	 * Extension server protocol must match to plugin _url protocol
	 */
	private static final String HTTPS_PROTOCOL = "HTTPS";
	/**
	 * Extension server adminEmail
	 */
	private String email = "noreply@vmware.com";
	private String pluginUrl;
	private String name;
	private String key;
	private String summary;
	private String version;
	private String company;
	private boolean showInSolutionManager;
	private String serverThumbprint;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPluginUrl() {
		return pluginUrl;
	}

	public void setPluginUrl(String pluginUrl) {
		this.pluginUrl = pluginUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public boolean isShowInSolutionManager() {
		return showInSolutionManager;
	}

	public void setShowInSolutionManager(boolean showInSolutionManager) {
		this.showInSolutionManager = showInSolutionManager;
	}

	public String getServerThumbprint() {
		return serverThumbprint;
	}

	public void setServerThumbprint(String serverThumbprint) {
		this.serverThumbprint = serverThumbprint;
	}

	public static String getDefaultPluginType() {
		return DEFAULT_PLUGIN_TYPE;
	}

	public static String getHttpsProtocol() {
		return HTTPS_PROTOCOL;
	}

}
