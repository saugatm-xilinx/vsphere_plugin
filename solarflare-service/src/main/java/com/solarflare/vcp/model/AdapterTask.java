package com.solarflare.vcp.model;

public class AdapterTask {

	private String adapterId;
	private Status bootROM;
	private Status controller;
	private Status uefiROM;
	
	public String getAdapterId() {
		return adapterId;
	}

	public void setAdapterId(String adapterId) {
		this.adapterId = adapterId;
	}

	public Status getBootROM() {
		return bootROM;
	}

	public void setBootROM(Status bootROM) {
		this.bootROM = bootROM;
	}

	public Status getController() {
		return controller;
	}

	public void setController(Status controller) {
		this.controller = controller;
	}

	public Status getUefiROM() {
		return uefiROM;
	}

	public void setUefiROM(Status uefiROM) {
		this.uefiROM = uefiROM;
	}

}
