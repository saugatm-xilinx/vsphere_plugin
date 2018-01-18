package com.solarflare.firmware.model;

import java.io.Serializable;

public class BinaryFiles implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3013944344069991065L;
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