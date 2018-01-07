package com.solarflare.vcp.model;

public class NetQueue {

	private int netQueueCount;
	private int rss;
	private boolean isMaxNumpCPU;
	
	
	public int getNetQueueCount() {
		return netQueueCount;
	}
	
	public void setNetQueueCount(int netQueueCount) {
		this.netQueueCount = netQueueCount;
	}
	public int getRss() {
		return rss;
	}
	public void setRss(int rss) {
		this.rss = rss;
	}
	public boolean isMaxNumpCPU() {
		return isMaxNumpCPU;
	}
	public void setMaxNumpCPU(boolean isMaxNumpCPU) {
		this.isMaxNumpCPU = isMaxNumpCPU;
	}
	
	
	@Override
	public String toString() {
		return "NetQueue [netQueueCount=" + netQueueCount + ", rss=" + rss + ", isMaxNumpCPU=" + isMaxNumpCPU + "]";
	}
}