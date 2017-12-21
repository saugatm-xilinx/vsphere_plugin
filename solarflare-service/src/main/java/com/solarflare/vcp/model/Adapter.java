package com.solarflare.vcp.model;

import java.io.Serializable;
import java.util.List;

public class Adapter implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private String type = "ADAPTER";
	private String id;
	private String versionController;
	private String versionBootROM;
	private String versionUEFIROM;
	private String versionFirmware;
	private FirmewareVersion latestVersion;
	private List<Status> status;
	private boolean isLaterVersionAvailable;
	List<VMNIC> children;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersionController() {
		return versionController;
	}

	public void setVersionController(String versionController) {
		this.versionController = versionController;
	}

	public String getVersionBootROM() {
		return versionBootROM;
	}

	public void setVersionBootROM(String versionBootROM) {
		this.versionBootROM = versionBootROM;
	}

	public String getVersionUEFIROM() {
		return versionUEFIROM;
	}

	public void setVersionUEFIROM(String versionUEFIROM) {
		this.versionUEFIROM = versionUEFIROM;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<VMNIC> getChildren() {
		return children;
	}

	public void setChildren(List<VMNIC> children) {
		this.children = children;
	}

	public String getVersionFirmware() {
		return versionFirmware;
	}

	public void setVersionFirmware(String versionFirmware) {
		this.versionFirmware = versionFirmware;
	}

	public List<Status> getStatus() {
		return status;
	}

	public void setStatus(List<Status> status) {
		this.status = status;
	}

	public FirmewareVersion getLatestVersion() {
		return latestVersion;
	}

	public void setLatestVersion(FirmewareVersion latestVersion) {
		this.latestVersion = latestVersion;
	}

	public boolean isLaterVersionAvailable() {
		return isLaterVersionAvailable;
	}

	public void setLaterVersionAvailable(boolean isLaterVersionAvailable) {
		this.isLaterVersionAvailable = isLaterVersionAvailable;
	}

}
