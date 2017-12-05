package com.solarflare.vcp.services;

import java.net.URL;
import java.util.Collection;
import java.util.List;

import javax.cim.CIMInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.solarflare.vcp.cim.CIMConstants;
import com.solarflare.vcp.cim.CIMHost;
import com.solarflare.vcp.cim.CIMService;
import com.solarflare.vcp.helper.VCenterHelper;
import com.solarflare.vcp.model.Adapter;
import com.solarflare.vcp.model.Host;
import com.solarflare.vcp.model.NicBootParamInfo;
import com.solarflare.vcp.vim25.VCenterService;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.VimPortType;
import com.vmware.vise.security.ClientSessionEndListener;
import com.vmware.vise.usersession.UserSessionService;

public class HostAdapterServiceImpl implements HostAdapterService, ClientSessionEndListener
{
    private static final Log logger = LogFactory.getLog(HostAdapterServiceImpl.class);

    private UserSessionService userSessionService;

    VCenterService service = new VCenterService();
    CIMService cim = new CIMService();

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
    public boolean updateFirmwareToLatest(List<Adapter> adapterList, String hostId) throws Exception
    {
        logger.info("Start updating firmware adapter");
        try
        {
            ServiceContent serviceContent = VCenterHelper.getServiceContent(userSessionService, VCenterService.vimPort);

           CIMHost cimHost = service.getCIMHost(serviceContent, hostId);
           Collection<CIMInstance> instances = cim.getAllInstances(cimHost, CIMConstants.CIM_NAMESPACE, CIMConstants.SF_SOFTWARE_INSTALLATION_SERVICE);
           // Get Controller SF_SoftwareInstallationService instance
           CIMInstance svc_mcfw_inst = cim.getFirmwareSoftwareInstallationInstance(instances);

           // Get BootROM SF_SoftwareInstallationService instance
          // CIMInstance svc_bootrom_inst = cim.getBootROMSoftwareInstallationInstance(instances);
           URL fwImagePath = new URL("http://10.101.10.132"+CIMConstants.CONTROLLER_FW_IMAGE_PATH);
            for (Adapter adapter : adapterList)
            {
                logger.debug("Updating Contoller firmware for adapter : "+adapter.getName());
                CIMInstance nicInstance = cim.getNICCardInstance(cimHost, adapter.getChildren().get(0).getName());
                cim.updateFirmwareFromURL(svc_mcfw_inst.getObjectPath(), cimHost, nicInstance, fwImagePath);
                logger.debug("Contoller firmware update for adapter '" + adapter.getName() + "' is done.");
            }
        }
        catch (Exception e)
        {
            throw e;
        }
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

    @Override
    public boolean validateTypeAndSubTupe(String file, boolean isLocal) throws Exception
    {
        return false;
    }

}
