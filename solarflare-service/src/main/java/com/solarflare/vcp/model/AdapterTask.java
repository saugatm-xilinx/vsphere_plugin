package com.solarflare.vcp.model;

public class AdapterTask {

	private String adapterId;
	private Status bootROM;
	private Status controller;
	private Status uefiROM;
	private Status sucfw;
	
	public Status getSucfw() {
		return sucfw;
	}

	public void setSucfw(Status sucfw) {
		this.sucfw = sucfw;
	}

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

	@Override
	public String toString() {
		return "AdapterTask [adapterId=" + adapterId + ", bootROM=" + bootROM + ", controller=" + controller
				+ ", uefiROM=" + uefiROM + ", sucfw=" + sucfw + "]";
	}

	
}
