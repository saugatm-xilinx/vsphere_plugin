package com.solarflare.vcp.model;

import java.io.Serializable;

public class FirmewareVersion implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4274221033707581898L;

	private String controlerVersion;
	private String bootROMVersion;
	private String uefiVersion;
	private String firmewareFamilyVersion;

	public String getControlerVersion() {
		return controlerVersion;
	}

	public void setControlerVersion(String controlerVersion) {
		this.controlerVersion = controlerVersion;
	}

	public String getBootROMVersion() {
		return bootROMVersion;
	}

	public void setBootROMVersion(String bootROMVersion) {
		this.bootROMVersion = bootROMVersion;
	}

	public String getUefiVersion() {
		return uefiVersion;
	}

	public void setUefiVersion(String uefiVersion) {
		this.uefiVersion = uefiVersion;
	}

	public String getFirmewareFamilyVersion() {
		return firmewareFamilyVersion;
	}

	public void setFirmewareFamilyVersion(String firmewareFamilyVersion) {
		this.firmewareFamilyVersion = firmewareFamilyVersion;
	}

	@Override
	public String toString() {
		return "FirmewareVersion [controlerVersion=" + controlerVersion + ", bootROMVersion=" + bootROMVersion
				+ ", uefiVersion=" + uefiVersion + ", firmewareFamilyVersion=" + firmewareFamilyVersion + "]";
	}

}
