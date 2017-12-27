package com.solarflare.vcp.model;

public class Debugging {

	private boolean isMaskDriver;
	private boolean isMaskUtils;
	private boolean isMaskMgmt;
	private boolean isMaskUplink;
	private boolean isMaskTransmit;
	private boolean isMaskReceive;
	private boolean isMaskHardware;
	private boolean isMaskEventQueue;
	private boolean isMaskRSS;
	private boolean isMaskPort;
	private boolean isMaskIntrupt;
	private boolean isMaskCommonCode;
	public boolean isMaskDriver() {
		return isMaskDriver;
	}
	public void setMaskDriver(boolean isMaskDriver) {
		this.isMaskDriver = isMaskDriver;
	}
	public boolean isMaskUtils() {
		return isMaskUtils;
	}
	public void setMaskUtils(boolean isMaskUtils) {
		this.isMaskUtils = isMaskUtils;
	}
	public boolean isMaskMgmt() {
		return isMaskMgmt;
	}
	public void setMaskMgmt(boolean isMaskMgmt) {
		this.isMaskMgmt = isMaskMgmt;
	}
	public boolean isMaskUplink() {
		return isMaskUplink;
	}
	public void setMaskUplink(boolean isMaskUplink) {
		this.isMaskUplink = isMaskUplink;
	}
	public boolean isMaskTransmit() {
		return isMaskTransmit;
	}
	public void setMaskTransmit(boolean isMaskTransmit) {
		this.isMaskTransmit = isMaskTransmit;
	}
	public boolean isMaskReceive() {
		return isMaskReceive;
	}
	public void setMaskReceive(boolean isMaskReceive) {
		this.isMaskReceive = isMaskReceive;
	}
	public boolean isMaskHardware() {
		return isMaskHardware;
	}
	public void setMaskHardware(boolean isMaskHardware) {
		this.isMaskHardware = isMaskHardware;
	}
	public boolean isMaskEventQueue() {
		return isMaskEventQueue;
	}
	public void setMaskEventQueue(boolean isMaskEventQueue) {
		this.isMaskEventQueue = isMaskEventQueue;
	}
	public boolean isMaskRSS() {
		return isMaskRSS;
	}
	public void setMaskRSS(boolean isMaskRSS) {
		this.isMaskRSS = isMaskRSS;
	}
	public boolean isMaskPort() {
		return isMaskPort;
	}
	public void setMaskPort(boolean isMaskPort) {
		this.isMaskPort = isMaskPort;
	}
	public boolean isMaskIntrupt() {
		return isMaskIntrupt;
	}
	public void setMaskIntrupt(boolean isMaskIntrupt) {
		this.isMaskIntrupt = isMaskIntrupt;
	}
	public boolean isMaskCommonCode() {
		return isMaskCommonCode;
	}
	public void setMaskCommonCode(boolean isMaskCommonCode) {
		this.isMaskCommonCode = isMaskCommonCode;
	}
	@Override
	public String toString() {
		return "Debugging [isMaskDriver=" + isMaskDriver + ", isMaskUtils=" + isMaskUtils + ", isMaskMgmt=" + isMaskMgmt
				+ ", isMaskUplink=" + isMaskUplink + ", isMaskTransmit=" + isMaskTransmit + ", isMaskReceive="
				+ isMaskReceive + ", isMaskHardware=" + isMaskHardware + ", isMaskEventQueue=" + isMaskEventQueue
				+ ", isMaskRSS=" + isMaskRSS + ", isMaskPort=" + isMaskPort + ", isMaskIntrupt=" + isMaskIntrupt
				+ ", isMaskCommonCode=" + isMaskCommonCode + "]";
	}

	
	
}
