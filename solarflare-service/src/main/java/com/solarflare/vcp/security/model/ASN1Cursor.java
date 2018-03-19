package com.solarflare.vcp.security.model;

import java.util.Arrays;

public class ASN1Cursor {

	short[] buffer;
	int bufferIndex;
	int bufferLength;
	int tag;
	int headerSize;
	int valueSize;
	
	
	public int getBufferIndex() {
		return bufferIndex;
	}
	public void setBufferIndex(int bufferIndex) {
		this.bufferIndex = bufferIndex;
	}
	public short[] getBuffer() {
		return buffer;
	}
	public void setBuffer(short[] buffer) {
		this.buffer = buffer;
	}
	public int getLength() {
		return bufferLength;
	}
	public void setLength(int length) {
		this.bufferLength = length;
	}
	public int getTag() {
		return tag;
	}
	public void setTag(int tag) {
		this.tag = tag;
	}
	public int getHeaderSize() {
		return headerSize;
	}
	public void setHeaderSize(int headerSize) {
		this.headerSize = headerSize;
	}
	public int getValueSize() {
		return valueSize;
	}
	public void setValueSize(int valueSize) {
		this.valueSize = valueSize;
	}
	@Override
	public String toString() {
		return "ASN1Cursor [buffer=" + Arrays.toString(buffer) + ", bufferIndex=" + bufferIndex + ", length=" + bufferLength
				+ ", tag=" + tag + ", headerSize=" + headerSize + ", valueSize=" + valueSize + "]";
	}
	
	
	
	
}
