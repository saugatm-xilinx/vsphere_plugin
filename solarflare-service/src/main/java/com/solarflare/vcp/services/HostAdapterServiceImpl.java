package com.solarflare.vcp.services;

import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.cim.CIMInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.solarflare.vcp.cim.CIMConstants;
import com.solarflare.vcp.cim.CIMHost;
import com.solarflare.vcp.cim.CIMService;
import com.solarflare.vcp.helper.MetadataHelper;
import com.solarflare.vcp.helper.VCenterHelper;
import com.solarflare.vcp.model.Adapter;
import com.solarflare.vcp.model.Host;
import com.solarflare.vcp.model.NicBootParamInfo;
import com.solarflare.vcp.model.SfFirmware;
import com.solarflare.vcp.vim25.VCenterService;
import com.vmware.vim25.ServiceContent;
import com.vmware.vise.security.ClientSessionEndListener;
import com.vmware.vise.usersession.UserSessionService;

public class HostAdapterServiceImpl implements HostAdapterService, ClientSessionEndListener
{
    private static final Log logger = LogFactory.getLog(HostAdapterServiceImpl.class);

    private UserSessionService userSessionService;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);

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
		try {
			ServiceContent serviceContent = VCenterHelper.getServiceContent(userSessionService, VCenterService.vimPort);
			CIMHost cimHost = service.getCIMHost(serviceContent, hostId);
			// Get Controller SF_SoftwareInstallationService instance
			CIMInstance svc_mcfw_inst = cim.getFirmwareSoftwareInstallationInstance(cimHost);
			// Get BootROM SF_SoftwareInstallationService instance
			 CIMInstance svc_bootrom_inst = cim.getBootROMSoftwareInstallationInstance(cimHost);
			// TODO taken for testing
			//URL fwImagePath = new URL("http://10.101.10.132" + CIMConstants.CONTROLLER_FW_IMAGE_PATH);

			CIMInstance nicInstance = null;
			MetadataHelper metadataHelper = new MetadataHelper();
			for (Adapter adapter : adapterList) {
				nicInstance = cim.getNICCardInstance(cimHost, adapter.getChildren().get(0).getName());
				// get the URL of latest firmware file
				String urlPath = cim.getPluginURL(serviceContent, VCenterService.vimPort, CIMConstants.PLUGIN_KEY);
				URL pluginURL = new URL(urlPath);
				String filePath = null;
				boolean isController = true;
				SfFirmware file = metadataHelper.getMetaDataForAdapter(serviceContent, VCenterService.vimPort, cimHost,
						svc_mcfw_inst, nicInstance, isController);
				if (file != null) {
					filePath =  file.getPath();
				}
				// TODO : check for https certificate warning
				// URL fwImagePath = new
				// URL(pluginURL.getProtocol(),pluginURL.getHost(),pluginURL.getPort(),filePath);
				
				// TODO check version current version and version from file for both controller and BootRom
				URL fwImagePath = new URL("http", pluginURL.getHost(), filePath);
//				FirmwareUpdateThread updateFirmware = new FirmwareUpdateThread(cim, cimHost, fwImagePath, svc_mcfw_inst,
//						nicInstance, "cnt-"+adapter.getId());
//				Thread thread = new Thread(updateFirmware);
//				thread.start();
				
				Runnable workerForController = new FirmwareUpdateThread(cim, cimHost, fwImagePath, svc_mcfw_inst,
						nicInstance, "cnt-"+adapter.getId());
	            executor.execute(workerForController); 
				
				// Update BootROM
				isController = false;
				file = metadataHelper.getMetaDataForAdapter(serviceContent, VCenterService.vimPort, cimHost,
						svc_bootrom_inst, nicInstance, isController);
				if (file != null) {
					filePath = file.getPath();
				}
				// TODO : check for https certificate warning
				// URL fwImagePath = new
				// URL(pluginURL.getProtocol(),pluginURL.getHost(),pluginURL.getPort(),filePath);
				fwImagePath = new URL("http", pluginURL.getHost(), filePath);
							
				Runnable workerForBoot = new FirmwareUpdateThread(cim, cimHost, fwImagePath, svc_bootrom_inst,
						nicInstance, "boot-"+adapter.getId()); 
	            executor.execute(workerForBoot); 
				
			}
		} catch (Exception e) {
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
