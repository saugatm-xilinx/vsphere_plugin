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
	private FirmwareVersion latestVersion;
	private List<Status> status;
	private boolean isLaterVersionAvailable;
	List<VMNIC> children;
	private String deviceId;
	private String subSystemDeviceId;
	private String vendorId;
	private String subSystemVendorId;

	
	
	 public String getDeviceId()
	    {
	        return deviceId;
	    }

	    public void setDeviceId(String deviceId)
	    {
	        this.deviceId = deviceId;
	    }

	    public String getSubSystemDeviceId()
	    {
	        return subSystemDeviceId;
	    }

	    public void setSubSystemDeviceId(String subSystemDeviceId)
	    {
	        this.subSystemDeviceId = subSystemDeviceId;
	    }

	
	    public String getVendorId()
	    {
	        return vendorId;
	    }

	    public void setVendorId(String vendorId)
	    {
	        this.vendorId = vendorId;
	    }

	    public String getSubSystemVendorId()
	    {
	        return subSystemVendorId;
	    }

	    public void setSubSystemVendorId(String subSystemVendorId)
	    {
	        this.subSystemVendorId = subSystemVendorId;
	    }

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

	public FirmwareVersion getLatestVersion() {
		return latestVersion;
	}

	public void setLatestVersion(FirmwareVersion latestVersion) {
		this.latestVersion = latestVersion;
	}

	public boolean isLaterVersionAvailable() {
		return isLaterVersionAvailable;
	}

	public void setLaterVersionAvailable(boolean isLaterVersionAvailable) {
		this.isLaterVersionAvailable = isLaterVersionAvailable;
	}

	@Override
	public String toString() {
		return "Adapter [name=" + name + ", type=" + type + ", id=" + id + ", versionController=" + versionController
				+ ", versionBootROM=" + versionBootROM + ", versionUEFIROM=" + versionUEFIROM + ", versionFirmware="
				+ versionFirmware + ", latestVersion=" + latestVersion + ", status=" + status
				+ ", isLaterVersionAvailable=" + isLaterVersionAvailable + ", children=" + children + ", deviceId="
				+ deviceId + ", subSystemDeviceId=" + subSystemDeviceId + ", vendorId=" + vendorId
				+ ", subSystemVendorId=" + subSystemVendorId + "]";
	}

	
}
