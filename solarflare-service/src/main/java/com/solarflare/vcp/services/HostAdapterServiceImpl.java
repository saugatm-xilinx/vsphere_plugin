package com.solarflare.vcp.services;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.solarflare.vcp.model.Adapter;
import com.solarflare.vcp.model.Host;
import com.solarflare.vcp.model.NicBootParamInfo;
import com.solarflare.vcp.vim25.VCenterService;
import com.vmware.vise.security.ClientSessionEndListener;
import com.vmware.vise.usersession.UserSessionService;

public class HostAdapterServiceImpl implements HostAdapterService, ClientSessionEndListener
{
    private static final Log logger = LogFactory.getLog(HostAdapterServiceImpl.class);

    private UserSessionService userSessionService;

    VCenterService service = new VCenterService();

    @Autowired
    public HostAdapterServiceImpl(UserSessionService session)
    {
        userSessionService = session;
    }

    @Override
    public List<Host> getHostList() throws Exception
    {
        List<Host> hostList = null;
        try
        {
            hostList = service.getHostsList(userSessionService);
        }
        catch (Exception e)
        {
            throw e;
        }
        return hostList;
    }

    @Override
    public Host getHostById(String hostId) throws Exception
    {
        Host host = null;
        try
        {
            host = service.getHostById(userSessionService, hostId);
        }
        catch (Exception e)
        {
            throw e;
        }
        return host;

    }

    @Override
    public boolean uploadFile(String file) throws Exception
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void sessionEnded(String clientId)
    {
        logger.info("Logging out client session - " + clientId);

        // Clean up all session specific resources.
        // Logout from any session specific services.

    }

    @Override
    public List<Adapter> getHostAdapters(String hostId) throws Exception
    {

        List<Adapter> adapters = service.getAdapters(userSessionService, hostId);
        return adapters;

    }

    @Override
    public NicBootParamInfo getNicParamInfo(String hostId, String nicId) throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

}
