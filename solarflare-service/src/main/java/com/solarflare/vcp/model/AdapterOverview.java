package com.solarflare.vcp.model;

import java.io.Serializable;

public class AdapterOverview implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String type= "AdapterOverview";
	private String name;
	private String portNumber;
	private String serialNumber;
	private String pciExpressLinkSpeed;
	private String pciExpressBusWidth;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
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
	public String getPciExpressLinkSpeed() {
		return pciExpressLinkSpeed;
	}
	public void setPciExpressLinkSpeed(String pciExpressLinkSpeed) {
		this.pciExpressLinkSpeed = pciExpressLinkSpeed;
	}
	public String getPciExpressBusWidth() {
		return pciExpressBusWidth;
	}
	public void setPciExpressBusWidth(String pciExpressBusWidth) {
		this.pciExpressBusWidth = pciExpressBusWidth;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	
}
