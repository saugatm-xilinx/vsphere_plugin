package com.solarflare.vcp.model;

import java.io.Serializable;
import java.util.List;

public class Host implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7893231025735798101L;
	private String type = "HOST";
	private String id;
	private String name;
	List<Adapter> children;
	private int adapterCount;
	private int portCount;
	private String driverVersion;
	private String cimProviderVersion;

	public List<Adapter> getChildren() {
		return children;
	}

	public void setChildren(List<Adapter> children) {
		this.children = children;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

//	@Override
//	public String toString() {
//		return "Host [type=" + type + ", id=" + id + ", name=" + name + ", adpaters=" + children + "]";
//	}
//
//	@Override
//	public int compareTo(Host o) {
//		return this.name.compareTo(o.getName());
//	}

	public int getAdapterCount() {
		return adapterCount;
	}

	public void setAdapterCount(int adapterCount) {
		this.adapterCount = adapterCount;
	}

	public int getPortCount() {
		return portCount;
	}

	public void setPortCount(int portCount) {
		this.portCount = portCount;
	}

	public String getDriverVersion() {
		return driverVersion;
	}

	public void setDriverVersion(String driverVersion) {
		this.driverVersion = driverVersion;
	}

	public String getCimProviderVersion() {
		return cimProviderVersion;
	}

	public void setCimProviderVersion(String cimProviderVersion) {
		this.cimProviderVersion = cimProviderVersion;
	}

//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((id == null) ? 0 : id.hashCode());
//		result = prime * result + ((name == null) ? 0 : name.hashCode());
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		Host other = (Host) obj;
//		if (id == null) {
//			if (other.id != null)
//				return false;
//		} else if (!id.equals(other.id))
//			return false;
//		if (name == null) {
//			if (other.name != null)
//				return false;
//		} else if (!name.equals(other.name))
//			return false;
//		return true;
//	}

}