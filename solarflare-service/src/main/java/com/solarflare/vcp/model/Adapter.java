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
	List<VMNIC> children;
	private String deviceId;
	private String subSystemDeviceId;
	private String vendorId;
	private String subSystemVendorId;
	// Caching type and subType in adapter 
	private int controllerType;
	private int controllerSubType;
	private int bootROMType;
	private int bootROMSubType;
	private int uefiROMType;
	private int uefiROMSubType;
	

	public int getControllerType() {
		return controllerType;
	}

	public void setControllerType(int controllerType) {
		this.controllerType = controllerType;
	}

	public int getControllerSubType() {
		return controllerSubType;
	}

	public void setControllerSubType(int controllerSubType) {
		this.controllerSubType = controllerSubType;
	}

	public int getBootROMType() {
		return bootROMType;
	}

	public void setBootROMType(int bootROMType) {
		this.bootROMType = bootROMType;
	}

	public int getBootROMSubType() {
		return bootROMSubType;
	}

	public void setBootROMSubType(int bootROMSubType) {
		this.bootROMSubType = bootROMSubType;
	}

	public int getUefiROMType() {
		return uefiROMType;
	}

	public void setUefiROMType(int uefiROMType) {
		this.uefiROMType = uefiROMType;
	}

	public int getUefiROMSubType() {
		return uefiROMSubType;
	}

	public void setUefiROMSubType(int uefiROMSubType) {
		this.uefiROMSubType = uefiROMSubType;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getSubSystemDeviceId() {
		return subSystemDeviceId;
	}

	public void setSubSystemDeviceId(String subSystemDeviceId) {
		this.subSystemDeviceId = subSystemDeviceId;
	}

	public String getVendorId() {
		return vendorId;
	}

	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}

	public String getSubSystemVendorId() {
		return subSystemVendorId;
	}

	public void setSubSystemVendorId(String subSystemVendorId) {
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

	public FirmwareVersion getLatestVersion() {
		return latestVersion;
	}

	public void setLatestVersion(FirmwareVersion latestVersion) {
		this.latestVersion = latestVersion;
	}

	@Override
	public String toString() {
		return "Adapter [name=" + name + ", type=" + type + ", id=" + id + ", versionController=" + versionController
				+ ", versionBootROM=" + versionBootROM + ", versionUEFIROM=" + versionUEFIROM + ", versionFirmware="
				+ versionFirmware + ", latestVersion=" + latestVersion + ", children=" + children + ", deviceId="
				+ deviceId + ", subSystemDeviceId=" + subSystemDeviceId + ", vendorId=" + vendorId
				+ ", subSystemVendorId=" + subSystemVendorId + ", controllerType=" + controllerType
				+ ", controllerSubType=" + controllerSubType + ", bootROMType=" + bootROMType + ", bootROMSubType="
				+ bootROMSubType + ", uefiROMType=" + uefiROMType + ", uefiROMSubType=" + uefiROMSubType + "]";
	}

}
