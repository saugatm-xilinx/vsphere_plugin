package com.solarflare.vcp.model;

import java.io.Serializable;

public class AdapterOverview implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String type= "AdapterOverview";
	private String partNumber;
	private String serialNumber;
	
	
	public String getPartNumber() {
		return partNumber;
	}
	public void setPartNumber(String partNumber) {
		this.partNumber = partNumber;
	}
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	
	@Override
	public String toString() {
		return "AdapterOverview [type=" + type + ", partNumber=" + partNumber + ", serialNumber=" + serialNumber + "]";
	}
	
	
}
