package com.solarflare.vcp.security.model;

public class PKCS7_Data implements SignedData{
	
	/* OID 1.2.840.113549.1.7.1 */
	short[] data = { 0x2A, 0x86, 0x48, 0x86, 0xF7, 0x0D, 0x01, 0x07, 0x01 };
	int valueSize = 9;
	
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
