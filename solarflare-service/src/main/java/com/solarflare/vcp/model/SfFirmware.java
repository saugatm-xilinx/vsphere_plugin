package com.solarflare.vcp.model;

public class SfFirmware {

	private String name;
	private String type;
	private String subtype;
	private String versionString;
	private String path;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSubtype() {
		return subtype;
	}

	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	public String getVersionString() {
		return versionString;
	}

	public void setVersionString(String versionString) {
		this.versionString = versionString;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return "File [name=" + name + ", type=" + type + ", subtype=" + subtype + ", versionString=" + versionString
				+ ", path=" + path + "]";
	}
}