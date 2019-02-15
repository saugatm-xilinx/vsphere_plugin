package com.solarflare.vcp.model;

import java.io.Serializable;

public class FirmwareVersion implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4274221033707581898L;

	private String controller;
	private String bootROM;
	private String uefi;
	private String sucfw;
	private String firmewareFamily;

	public String getController() {
		return controller;
	}

	public void setController(String controller) {
		this.controller = controller;
	}

	public String getBootROM() {
		return bootROM;
	}

	public void setBootROM(String bootROM) {
		this.bootROM = bootROM;
	}

	public String getUefi() {
		return uefi;
	}

	public void setUefi(String uefi) {
		this.uefi = uefi;
	}

	public String getSucfw() {
		return sucfw;
	}

	public void setSucfw(String sucfw) {
		this.sucfw = sucfw;
	}

	public String getFirmewareFamily() {
		return firmewareFamily;
	}

	public void setFirmewareFamily(String firmewareFamily) {
		this.firmewareFamily = firmewareFamily;
	}

	@Override
	public String toString() {
		return "FirmwareVersion [controller=" + controller + ", bootROM=" + bootROM + ", uefi=" + uefi
				+ ", firmewareFamily=" + firmewareFamily + "]";
	}

}
