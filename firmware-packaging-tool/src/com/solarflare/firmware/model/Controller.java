package com.solarflare.firmware.model;

import java.util.List;

public class Controller {

	private List<SfFirmware> files;

	public List<SfFirmware> getFiles() {
		return files;
	}

	public void setFiles(List<SfFirmware> files) {
		this.files = files;
	}

	@Override
	public String toString() {
		return "Controller [files=" + files + "]";
	}

}