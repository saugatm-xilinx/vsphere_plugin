package com.solarflare.vcp.model;

public class NicBootParamInfo {
	private String hostId;
	private String nicId;
	private String bootImage;
	private String linkSpeed;
	private String linkUpDelayTime;
	private String bannerDelayTime;
	private String bootSkipDelayTime;
	private String bootType;
	private String physicalFunPerPort;
	private String msiXinterruptLimit;
	private String vfMsiXinterruptLimit;
	private String numOfVirtualFunctions;
	private String firmwareVariant;
	private String insecureFilters;
	private String vLANTags;
	private String switchMode;

	public String getBootImage() {
		return bootImage;
	}

	public void setBootImage(String bootImage) {
		this.bootImage = bootImage;
	}

	public String getLinkSpeed() {
		return linkSpeed;
	}

	public void setLinkSpeed(String linkSpeed) {
		this.linkSpeed = linkSpeed;
	}

	public String getLinkUpDelayTime() {
		return linkUpDelayTime;
	}

	public void setLinkUpDelayTime(String linkUpDelayTime) {
		this.linkUpDelayTime = linkUpDelayTime;
	}

	public String getBannerDelayTime() {
		return bannerDelayTime;
	}

	public void setBannerDelayTime(String bannerDelayTime) {
		this.bannerDelayTime = bannerDelayTime;
	}

	public String getBootSkipDelayTime() {
		return bootSkipDelayTime;
	}

	public void setBootSkipDelayTime(String bootSkipDelayTime) {
		this.bootSkipDelayTime = bootSkipDelayTime;
	}

	public String getBootType() {
		return bootType;
	}

	public void setBootType(String bootType) {
		this.bootType = bootType;
	}

	public String getPhysicalFunPerPort() {
		return physicalFunPerPort;
	}

	public void setPhysicalFunPerPort(String physicalFunPerPort) {
		this.physicalFunPerPort = physicalFunPerPort;
	}

	public String getMsiXinterruptLimit() {
		return msiXinterruptLimit;
	}

	public void setMsiXinterruptLimit(String msiXinterruptLimit) {
		this.msiXinterruptLimit = msiXinterruptLimit;
	}

	public String getNumOfVirtualFunctions() {
		return numOfVirtualFunctions;
	}

	public void setNumOfVirtualFunctions(String numOfVirtualFunctions) {
		this.numOfVirtualFunctions = numOfVirtualFunctions;
	}

	public String getFirmwareVariant() {
		return firmwareVariant;
	}

	public void setFirmwareVariant(String firmwareVariant) {
		this.firmwareVariant = firmwareVariant;
	}

	public String getInsecureFilters() {
		return insecureFilters;
	}

	public void setInsecureFilters(String insecureFilters) {
		this.insecureFilters = insecureFilters;
	}

	public String getvLANTags() {
		return vLANTags;
	}

	public void setvLANTags(String vLANTags) {
		this.vLANTags = vLANTags;
	}

	public String getSwitchMode() {
		return switchMode;
	}

	public void setSwitchMode(String switchMode) {
		this.switchMode = switchMode;
	}

	public String getHostId() {
		return hostId;
	}

	public void setHostId(String hostId) {
		this.hostId = hostId;
	}

	public String getNicId() {
		return nicId;
	}

	public void setNicId(String nicId) {
		this.nicId = nicId;
	}

	@Override
	public String toString() {
		return "NicBootParamInfo [bootImage=" + bootImage + ", linkSpeed=" + linkSpeed + ", linkUpDelayTime="
				+ linkUpDelayTime + ", bannerDelayTime=" + bannerDelayTime + ", bootSkipDelayTime=" + bootSkipDelayTime
				+ ", bootType=" + bootType + ", physicalFunPerPort=" + physicalFunPerPort + ", msiXinterruptLimit="
				+ msiXinterruptLimit + ", numOfVirtualFunctions=" + numOfVirtualFunctions + ", firmwareVariant="
				+ firmwareVariant + ", insecureFilters=" + insecureFilters + ", vLANTags=" + vLANTags + ", switchMode="
				+ switchMode + "]";
	}

	public String getVfMsiXinterruptLimit() {
		return vfMsiXinterruptLimit;
	}

	public void setVfMsiXinterruptLimit(String vfMsiXinterruptLimit) {
		this.vfMsiXinterruptLimit = vfMsiXinterruptLimit;
	}

}
