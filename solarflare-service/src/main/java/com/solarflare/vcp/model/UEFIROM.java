package com.solarflare.vcp.model;

import java.util.List;

public class UEFIROM {
	private List<SfFirmware> files;

	public List<SfFirmware> getFiles() {
		return files;
	}

	public void setFiles(List<SfFirmware> files) {
		this.files = files;
	}

	@Override
	public String toString() {
		return "UEFIROM [files=" + files + "]";
	}

}