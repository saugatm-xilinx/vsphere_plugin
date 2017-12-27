package com.solarflare.vcp.model;

public class Overlay {
	
	private boolean isVxlanOffloadEnable;
	private boolean isGeneveOffloadEnable;
	
	public boolean isVxlanOffloadEnable() {
		return isVxlanOffloadEnable;
	}
	public void setVxlanOffloadEnable(boolean isVxlanOffloadEnable) {
		this.isVxlanOffloadEnable = isVxlanOffloadEnable;
	}
	public boolean isGeneveOffloadEnable() {
		return isGeneveOffloadEnable;
	}
	public void setGeneveOffloadEnable(boolean isGeneveOffloadEnable) {
		this.isGeneveOffloadEnable = isGeneveOffloadEnable;
	}
	@Override
	public String toString() {
		return "Overlay [isVxlanOffloadEnable=" + isVxlanOffloadEnable + ", isGeneveOffloadEnable="
				+ isGeneveOffloadEnable + "]";
	}
	
	

}
