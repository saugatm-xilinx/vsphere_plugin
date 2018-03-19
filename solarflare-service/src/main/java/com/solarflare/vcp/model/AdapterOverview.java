package com.solarflare.vcp.model;

import java.io.Serializable;

public class AdapterOverview implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String type= "AdapterOverview";
	private String portNumber;
	private String serialNumber;
	
	
	public String getPortNumber() {
		return portNumber;
	}
	public void setPortNumber(String portNumber) {
		this.portNumber = portNumber;
	}
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	
	@Override
	public String toString() {
		return "AdapterOverview [type=" + type + ", portNumber=" + portNumber + ", serialNumber=" + serialNumber + "]";
	}
	
	
}
