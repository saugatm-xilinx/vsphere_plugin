package com.msys.solarflare.model;

import java.io.Serializable;
import java.util.List;

public class Adapter implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private String type = "ADAPTER";
	private String id;
	private String versionController;
	private String versionBootROM;
	private String versionUEFIROM;
	List<VMNIC> children;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersionController() {
		return versionController;
	}

	public void setVersionController(String versionController) {
		this.versionController = versionController;
	}

	public String getVersionBootROM() {
		return versionBootROM;
	}

	public void setVersionBootROM(String versionBootROM) {
		this.versionBootROM = versionBootROM;
	}

	public String getVersionUEFIROM() {
		return versionUEFIROM;
	}

	public void setVersionUEFIROM(String versionUEFIROM) {
		this.versionUEFIROM = versionUEFIROM;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Adapter [name=" + name + ", type=" + type + ", id=" + id + ", versionController=" + versionController
				+ ", versionBootROM=" + versionBootROM + ", versionUEFIROM=" + versionUEFIROM + ", vmnics=" + children
				+ "]";
	}

	public List<VMNIC> getChildren() {
		return children;
	}

	public void setChildren(List<VMNIC> children) {
		this.children = children;
	}

}
