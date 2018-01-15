package com.solarflare.vcp.model;

public class Debugging {

	private boolean driver;
	private boolean utils;
	private boolean mgmt;
	private boolean uplink;
	private boolean transmit;
	private boolean receive;
	private boolean hardware;
	private boolean eventQueue;
	private boolean rss;
	private boolean port;
	private boolean interrupt;
	private boolean commonCode;
	public boolean isDriver() {
		return driver;
	}
	public void setDriver(boolean driver) {
		this.driver = driver;
	}
	public boolean isUtils() {
		return utils;
	}
	public void setUtils(boolean utils) {
		this.utils = utils;
	}
	public boolean isMgmt() {
		return mgmt;
	}
	public void setMgmt(boolean mgmt) {
		this.mgmt = mgmt;
	}
	public boolean isUplink() {
		return uplink;
	}
	public void setUplink(boolean uplink) {
		this.uplink = uplink;
	}
	public boolean isTransmit() {
		return transmit;
	}
	public void setTransmit(boolean transmit) {
		this.transmit = transmit;
	}
	public boolean isReceive() {
		return receive;
	}
	public void setReceive(boolean receive) {
		this.receive = receive;
	}
	public boolean isHardware() {
		return hardware;
	}
	public void setHardware(boolean hardware) {
		this.hardware = hardware;
	}
	public boolean isEventQueue() {
		return eventQueue;
	}
	public void setEventQueue(boolean eventQueue) {
		this.eventQueue = eventQueue;
	}
	public boolean isRss() {
		return rss;
	}
	public void setRss(boolean rss) {
		this.rss = rss;
	}
	public boolean isPort() {
		return port;
	}
	public void setPort(boolean port) {
		this.port = port;
	}
	public boolean isInterrupt() {
		return interrupt;
	}
	public void setInterrupt(boolean interrupt) {
		this.interrupt = interrupt;
	}
	public boolean isCommonCode() {
		return commonCode;
	}
	public void setCommonCode(boolean commonCode) {
		this.commonCode = commonCode;
	}
	@Override
	public String toString() {
		return "Debugging [driver=" + driver + ", utils=" + utils + ", mgmt=" + mgmt + ", uplink=" + uplink
				+ ", transmit=" + transmit + ", receive=" + receive + ", hardware=" + hardware + ", eventQueue="
				+ eventQueue + ", rss=" + rss + ", port=" + port + ", interrupt=" + interrupt + ", commonCode="
				+ commonCode + "]";
	}
	
	
}
