package com.solarflare.firmware.model;

public class FileHeader {

	private int magic;
	private int version;
	private int type;
	private int subtype;
	private int codeSize;
	private int size;

	private int controllerVersionMin;
	private int controllerVersionMax;

	private short codeVersion_a;
	private short codeVersion_b;
	private short codeVersion_c;
	private short codeVersion_d;

	public int getMagic() {
		return magic;
	}

	public void setMagic(int magic) {
		this.magic = magic;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getSubtype() {
		return subtype;
	}

	public void setSubtype(int subtype) {
		this.subtype = subtype;
	}

	public int getCodeSize() {
		return codeSize;
	}

	public void setCodeSize(int codeSize) {
		this.codeSize = codeSize;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getControllerVersionMin() {
		return controllerVersionMin;
	}

	public void setControllerVersionMin(int controllerVersionMin) {
		this.controllerVersionMin = controllerVersionMin;
	}

	public int getControllerVersionMax() {
		return controllerVersionMax;
	}

	public void setControllerVersionMax(int controllerVersionMax) {
		this.controllerVersionMax = controllerVersionMax;
	}

	public short getCodeVersion_a() {
		return codeVersion_a;
	}

	public void setCodeVersion_a(short codeVersion_a) {
		this.codeVersion_a = codeVersion_a;
	}

	public short getCodeVersion_b() {
		return codeVersion_b;
	}

	public void setCodeVersion_b(short codeVersion_b) {
		this.codeVersion_b = codeVersion_b;
	}

	public short getCodeVersion_c() {
		return codeVersion_c;
	}

	public void setCodeVersion_c(short codeVersion_c) {
		this.codeVersion_c = codeVersion_c;
	}

	public short getCodeVersion_d() {
		return codeVersion_d;
	}

	public void setCodeVersion_d(short codeVersion_d) {
		this.codeVersion_d = codeVersion_d;
	}

	public String getVersionString() {
		StringBuffer version = new StringBuffer();
		version.append(codeVersion_a);
		version.append(".");
		version.append(codeVersion_b);
		version.append(".");
		version.append(codeVersion_c);
		version.append(".");
		version.append(codeVersion_d);

		return version.toString();
	}

	@Override
	public String toString() {
		return "FileHeader [magic=" + magic + ", version=" + version + ", type=" + type + ", subtype=" + subtype
				+ ", codeSize=" + codeSize + ", size=" + size + ", controllerVersionMin=" + controllerVersionMin
				+ ", controllerVersionMax=" + controllerVersionMax + ", codeVersion_a=" + codeVersion_a
				+ ", codeVersion_b=" + codeVersion_b + ", codeVersion_c=" + codeVersion_c + ", codeVersion_d="
				+ codeVersion_d + "]";
	}
}
