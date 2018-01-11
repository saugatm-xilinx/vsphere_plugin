package com.solarflare.vcp.model;

import java.io.Serializable;

public class HostConfiguration implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3255818571331912468L;
	
	private NetQueue netQueue;
	private Debugging debuggingMask;
	private Overlay overlay;
	public NetQueue getNetQueue() {
		return netQueue;
	}
	public void setNetQueue(NetQueue netQueue) {
		this.netQueue = netQueue;
	}
	
	public Overlay getOverlay() {
		return overlay;
	}
	public void setOverlay(Overlay overlay) {
		this.overlay = overlay;
	}
	@Override
	public String toString() {
		return "HostConfiguration [netQueue=" + netQueue + ", debugging=" + debuggingMask + ", overlay=" + overlay + "]";
	}
	public Debugging getDebuggingMask() {
		return debuggingMask;
	}
	public void setDebuggingMask(Debugging debuggingMask) {
		this.debuggingMask = debuggingMask;
	}
	
	
}
