package com.msys.solarflare.cim;

public abstract class CIMHost {

	private String	_url	= null;

	public CIMHost() {
		this(null);
	}

	public CIMHost(String url) {
		setUrl(url);
	}

	public String getUrl() {
		return _url;
	}

	public void setUrl(String url) {
		this._url = url;
	}

	public abstract boolean isValid();

}