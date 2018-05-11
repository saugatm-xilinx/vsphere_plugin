package com.solarflare.vcp.model;

public class Overlay {
	
	private boolean vxlanOffloadEnable;
	private boolean geneveOffloadEnable;
	
	
	public boolean isVxlanOffloadEnable() {
		return vxlanOffloadEnable;
	}


	public void setVxlanOffloadEnable(boolean vxlanOffloadEnable) {
		this.vxlanOffloadEnable = vxlanOffloadEnable;
	}


	public boolean isGeneveOffloadEnable() {
		return geneveOffloadEnable;
	}


	public void setGeneveOffloadEnable(boolean geneveOffloadEnable) {
		this.geneveOffloadEnable = geneveOffloadEnable;
	}


	@Override
	public String toString() {
		return "Overlay [vxlanOffloadEnable=" + vxlanOffloadEnable + ", geneveOffloadEnable=" + geneveOffloadEnable
				+ "]";
	}
	
	

}
