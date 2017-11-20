package com.msys.solarflare.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.msys.solarflare.model.VMNIC;
import com.msys.solarflare.vim25.DisableSecurity;
import com.vmware.vim25.HostPciDevice;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.VimPortType;
import com.vmware.vise.usersession.ServerInfo;
import com.vmware.vise.usersession.UserSession;
import com.vmware.vise.usersession.UserSessionService;

public class VCenterHelper
{
    private static final Log logger = LogFactory.getLog(VCenterHelper.class);
    private static final String SERVICE_INSTANCE = "ServiceInstance";

    public static ServiceContent getServiceContent(UserSessionService usersessionService, VimPortType _vimPort) throws Exception
    {
        
        ServiceContent serviceContent = null;
        ServerInfo serverInfoObject = getServerInfoObject(usersessionService, null);
        setThumbprint(serverInfoObject);
        String sessionCookie = serverInfoObject.sessionCookie;
        String serviceUrl = serverInfoObject.serviceUrl;

        List<String> values = new ArrayList<String>();
        values.add("vmware_soap_session=" + sessionCookie);
        Map<String, List<String>> reqHeadrs = new HashMap<String, List<String>>();
        reqHeadrs.put("Cookie", values);

        Map<String, Object> reqContext = ((BindingProvider) _vimPort).getRequestContext();
        reqContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceUrl);
        reqContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
        reqContext.put(MessageContext.HTTP_REQUEST_HEADERS, reqHeadrs);

        final ManagedObjectReference svcInstanceRef = new ManagedObjectReference();
        svcInstanceRef.setType(SERVICE_INSTANCE);
        svcInstanceRef.setValue(SERVICE_INSTANCE);
        try
        {
            // TODO: Used for development only
            DisableSecurity.trustEveryone();
            serviceContent = _vimPort.retrieveServiceContent(svcInstanceRef);
        }
        catch (Exception e)
        {
            logger.error("getServiceContent error: " + e.getMessage());
            throw e;
        }
        return serviceContent;
    }

    private static void setThumbprint(ServerInfo sinfo)
    {
        String thumbprint = sinfo.thumbprint;
        if (thumbprint != null)
        {
            thumbprint = thumbprint.replaceAll(":", "").toLowerCase();
            ThumbprintTrustManager.addThumbprint(thumbprint);
        }
    }

    public static void mergeHwNicData(List<VMNIC> pnicData, Map<String, HostPciDevice> hwData)
    {
        if (pnicData == null || hwData == null)
            return;

        for (VMNIC nic : pnicData)
        {
            HostPciDevice pciDecice = hwData.get(nic.getPciId());
            if (pciDecice == null)
                continue;
            nic.setPciFunction(Byte.toString(pciDecice.getFunction()));
            nic.setPciBusNumber(Byte.toString(pciDecice.getBus()));

            nic.setDeviceName(pciDecice.getDeviceName());
            nic.setDeviceId(Short.toString(pciDecice.getDeviceId()));
            nic.setSubSystemDeviceId(Short.toString(pciDecice.getSubDeviceId()));

            nic.setVendorId(Short.toString(pciDecice.getVendorId()));
            nic.setSubSystemVendorId(Short.toString(pciDecice.getSubVendorId()));
            nic.setVendorName(pciDecice.getVendorName());
        }

    }

    private static ServerInfo getServerInfoObject(UserSessionService usersessionService, String serverGuid)
    {
        UserSession userSession = usersessionService.getUserSession();
        // TODO currently we consider we have only one vcenter server
        return userSession.serversInfo[0];

        // for (ServerInfo sinfo : userSession.serversInfo)
        // {
        // if (sinfo.serviceGuid.equalsIgnoreCase(serverGuid))
        // {
        // return sinfo;
        // }
        // }
        // return null;
    }
}
