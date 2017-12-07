package com.solarflare.vcp.model;

public class BinaryFiles {

	private Controller controller;
	private BootROM bootROM;

	public Controller getController() {
		return controller;
	}

	public void setController(Controller controller) {
		this.controller = controller;
	}

	public BootROM getBootROM() {
		return bootROM;
	}

	public void setBootROM(BootROM bootROM) {
		this.bootROM = bootROM;
	}

	@Override
	public String toString() {
		return "BinaryFiles [controller=" + controller + ", bootROM=" + bootROM + "]";
	}
}