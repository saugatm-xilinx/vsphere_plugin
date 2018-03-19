package com.solarflare.vcp.security.model;

public class SignedData_Version implements SignedData{
	
	/* SignedData structure version */
	short[] data = { 0x03 };
	int valueSize = 1;
	
	public short[] getData() {
		return data;
	}
	public void setData(short[] data) {
		this.data = data;
	}
	public int getValueSize() {
		return valueSize;
	}
	public void setValueSize(int valueSize) {
		this.valueSize = valueSize;
	}
	
	
}
