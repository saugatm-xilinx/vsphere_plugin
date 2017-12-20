package com.solarflare.vcp.model;

import java.io.Serializable;
import java.util.List;

public class CustomUpdateRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6407645016404725398L;
	private String url;
	private String base64Data;
	private List<Adapter> adapters;
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getBase64Data() {
		return base64Data;
	}
	public void setBase64Data(String base64Data) {
		this.base64Data = base64Data;
	}
	public List<Adapter> getAdapters() {
		return adapters;
	}
	public void setAdapters(List<Adapter> adapters) {
		this.adapters = adapters;
	}
	
	
	
}
