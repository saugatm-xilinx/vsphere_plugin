package com.solarflare.vcp.cim;

public abstract class CIMHost
{

    private String _url = null;
    private String hostId;
    private String clientId;

    public CIMHost()
    {
        this(null);
    }

    public CIMHost(String url)
    {
        setUrl(url);
    }

    public String getUrl()
    {
        return _url;
    }

    public void setUrl(String url)
    {
        this._url = url;
    }

    public abstract boolean isValid();

    public String getHostId() {
		return hostId;
	}

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

}
