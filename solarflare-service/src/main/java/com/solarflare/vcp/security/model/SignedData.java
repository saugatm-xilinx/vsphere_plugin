package com.solarflare.vcp.security.model;

public interface SignedData {

	public short[] getData(); 
	public void setData(short[] data) ;
	public int getValueSize(); 
	public void setValueSize(int valueSize) ;
}
