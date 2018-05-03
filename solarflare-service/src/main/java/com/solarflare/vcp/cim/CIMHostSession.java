package com.solarflare.vcp.cim;

public class CIMHostSession extends CIMHost
{
    private String _sessionId = null;

    public CIMHostSession()
    {
        this(null, null);
    }

    public CIMHostSession(String url, String sessionId)
    {
        super(url);
        setSessionId(sessionId);
    }

    public String getSessionId()
    {
        return _sessionId;
    }

    public void setSessionId(String sessionId)
    {
        _sessionId = sessionId;
    }

    @Override
    public boolean isValid()
    {
        return getUrl() != null && getSessionId() != null;
    }
}
