package com.solarflare.vcp.model;

import java.io.Serializable;

public class HostConfiguration implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3255818571331912468L;
	private int netQueueCount;
	private int netQueueRSS;
	private boolean isMasNumberOfCPU;
	private boolean isDebudggingMaskDriver;
	private boolean isDebudggingMaskUtils;
	private boolean isDebudggingMaskMgmt;
	private boolean isDebudggingMaskUplink;
	private boolean isDebudggingMaskTransmit;
	private boolean isDebudggingMaskReceive;
	private boolean isDebudggingMaskHardware;
	private boolean isDebudggingMaskEventQueue;
	private boolean isDebudggingMaskRSS;
	private boolean isDebudggingMaskPort;
	private boolean isDebudggingMaskIntrupt;
	private boolean isDebudggingMaskCommonCode;
	private boolean isOverlayVxlanOffloadEnable;
	private boolean isOverlayGeneveOffloadEnable;

	public int getNetQueueCount() {
		return netQueueCount;
	}

	public void setNetQueueCount(int netQueueCount) {
		this.netQueueCount = netQueueCount;
	}

	public int getNetQueueRSS() {
		return netQueueRSS;
	}

	public void setNetQueueRSS(int netQueueRSS) {
		this.netQueueRSS = netQueueRSS;
	}

	public boolean isMasNumberOfCPU() {
		return isMasNumberOfCPU;
	}

	public void setMasNumberOfCPU(boolean isMasNumberOfCPU) {
		this.isMasNumberOfCPU = isMasNumberOfCPU;
	}

	public boolean isDebudggingMaskDriver() {
		return isDebudggingMaskDriver;
	}

	public void setDebudggingMaskDriver(boolean isDebudggingMaskDriver) {
		this.isDebudggingMaskDriver = isDebudggingMaskDriver;
	}

	public boolean isDebudggingMaskUtils() {
		return isDebudggingMaskUtils;
	}

	public void setDebudggingMaskUtils(boolean isDebudggingMaskUtils) {
		this.isDebudggingMaskUtils = isDebudggingMaskUtils;
	}

	public boolean isDebudggingMaskMgmt() {
		return isDebudggingMaskMgmt;
	}

	public void setDebudggingMaskMgmt(boolean isDebudggingMaskMgmt) {
		this.isDebudggingMaskMgmt = isDebudggingMaskMgmt;
	}

	public boolean isDebudggingMaskUplink() {
		return isDebudggingMaskUplink;
	}

	public void setDebudggingMaskUplink(boolean isDebudggingMaskUplink) {
		this.isDebudggingMaskUplink = isDebudggingMaskUplink;
	}

	public boolean isDebudggingMaskTransmit() {
		return isDebudggingMaskTransmit;
	}

	public void setDebudggingMaskTransmit(boolean isDebudggingMaskTransmit) {
		this.isDebudggingMaskTransmit = isDebudggingMaskTransmit;
	}

	public boolean isDebudggingMaskReceive() {
		return isDebudggingMaskReceive;
	}

	public void setDebudggingMaskReceive(boolean isDebudggingMaskReceive) {
		this.isDebudggingMaskReceive = isDebudggingMaskReceive;
	}

	public boolean isDebudggingMaskHardware() {
		return isDebudggingMaskHardware;
	}

	public void setDebudggingMaskHardware(boolean isDebudggingMaskHardware) {
		this.isDebudggingMaskHardware = isDebudggingMaskHardware;
	}

	public boolean isDebudggingMaskEventQueue() {
		return isDebudggingMaskEventQueue;
	}

	public void setDebudggingMaskEventQueue(boolean isDebudggingMaskEventQueue) {
		this.isDebudggingMaskEventQueue = isDebudggingMaskEventQueue;
	}

	public boolean isDebudggingMaskRSS() {
		return isDebudggingMaskRSS;
	}

	public void setDebudggingMaskRSS(boolean isDebudggingMaskRSS) {
		this.isDebudggingMaskRSS = isDebudggingMaskRSS;
	}

	public boolean isDebudggingMaskPort() {
		return isDebudggingMaskPort;
	}

	public void setDebudggingMaskPort(boolean isDebudggingMaskPort) {
		this.isDebudggingMaskPort = isDebudggingMaskPort;
	}

	public boolean isDebudggingMaskIntrupt() {
		return isDebudggingMaskIntrupt;
	}

	public void setDebudggingMaskIntrupt(boolean isDebudggingMaskIntrupt) {
		this.isDebudggingMaskIntrupt = isDebudggingMaskIntrupt;
	}

	public boolean isDebudggingMaskCommonCode() {
		return isDebudggingMaskCommonCode;
	}

	public void setDebudggingMaskCommonCode(boolean isDebudggingMaskCommonCode) {
		this.isDebudggingMaskCommonCode = isDebudggingMaskCommonCode;
	}

	

	@Override
	public String toString() {
		return "HostConfiguration [netQueueCount=" + netQueueCount + ", netQueueRSS=" + netQueueRSS
				+ ", isMasNumberOfCPU=" + isMasNumberOfCPU + ", isDebudggingMaskDriver=" + isDebudggingMaskDriver
				+ ", isDebudggingMaskUtils=" + isDebudggingMaskUtils + ", isDebudggingMaskMgmt=" + isDebudggingMaskMgmt
				+ ", isDebudggingMaskUplink=" + isDebudggingMaskUplink + ", isDebudggingMaskTransmit="
				+ isDebudggingMaskTransmit + ", isDebudggingMaskReceive=" + isDebudggingMaskReceive
				+ ", isDebudggingMaskHardware=" + isDebudggingMaskHardware + ", isDebudggingMaskEventQueue="
				+ isDebudggingMaskEventQueue + ", isDebudggingMaskRSS=" + isDebudggingMaskRSS
				+ ", isDebudggingMaskPort=" + isDebudggingMaskPort + ", isDebudggingMaskIntrupt="
				+ isDebudggingMaskIntrupt + ", isDebudggingMaskCommonCode=" + isDebudggingMaskCommonCode
				+ ", isOverlayVxlanOffload=" + isOverlayVxlanOffloadEnable + ", isOverlayGeneveOffload="
				+ isOverlayGeneveOffloadEnable + "]";
	}

	public boolean isOverlayVxlanOffloadEnable() {
		return isOverlayVxlanOffloadEnable;
	}

	public void setOverlayVxlanOffloadEnable(boolean isOverlayVxlanOffloadEnable) {
		this.isOverlayVxlanOffloadEnable = isOverlayVxlanOffloadEnable;
	}

	public boolean isOverlayGeneveOffloadEnable() {
		return isOverlayGeneveOffloadEnable;
	}

	public void setOverlayGeneveOffloadEnable(boolean isOverlayGeneveOffloadEnable) {
		this.isOverlayGeneveOffloadEnable = isOverlayGeneveOffloadEnable;
	}

}
