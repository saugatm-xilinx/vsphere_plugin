package com.solarflare.vcp.vim25;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.cim.CIMInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.solarflare.vcp.cim.CIMConstants;
import com.solarflare.vcp.cim.CIMHost;
import com.solarflare.vcp.cim.CIMHostSession;
import com.solarflare.vcp.cim.CIMService;
import com.solarflare.vcp.helper.VCenterHelper;
import com.solarflare.vcp.model.Adapter;
import com.solarflare.vcp.model.Host;
import com.solarflare.vcp.model.VMNIC;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.HostConfigInfo;
import com.vmware.vim25.HostConfigManager;
import com.vmware.vim25.HostHardwareInfo;
import com.vmware.vim25.HostNetworkInfo;
import com.vmware.vim25.HostPciDevice;
import com.vmware.vim25.HostServiceTicket;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PhysicalNic;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.SelectionSpec;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.SoftwarePackage;
import com.vmware.vim25.TraversalSpec;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VimService;
import com.vmware.vise.usersession.UserSessionService;

public class VCenterService
{
    private static final Log logger = LogFactory.getLog(VCenterService.class);
    public static final String SOLARFLARE = "Solarflare";
    public static final String SLF = "SLF";
    public static final String CIM = "cim";
    public static final String SFC = "sfc";

    public static VimPortType vimPort = initializeVimPort();

    CIMService cim = new CIMService();

    private static VimPortType initializeVimPort()
    {
        VimService vimService = new VimService();
        return vimService.getVimPort();
    }

    private String getCimProviderVersion(List<SoftwarePackage> softPac) throws Exception
    {
        String cimProvider = null;
        try
        {
            for (SoftwarePackage sf : softPac)
            {
                List<String> vendors = Arrays.asList(new String[]
                { SLF, SOLARFLARE });
                if (vendors.contains(sf.getVendor()) && sf.getName().contains(CIM))
                {
                    cimProvider = sf.getVersion();
                    break;
                }
            }
        }
        catch (Exception e1)
        {
            throw e1;
        }
        return cimProvider;
    }

    private List<ObjectContent> getHostObjs(PropertySpec hostPropSpec, VimPortType vimPort, ServiceContent serviceContent,
            ManagedObjectReference rootFolderRef) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg
    {
        logger.info("Getting Host Objects");
        TraversalSpec datacenterHostTraversalSpec = new TraversalSpec();
        datacenterHostTraversalSpec.setName("datacenterHostTraversalSpec");
        datacenterHostTraversalSpec.setType("Datacenter");
        datacenterHostTraversalSpec.setPath("hostFolder");
        datacenterHostTraversalSpec.setSkip(false);
        SelectionSpec sSpecF = new SelectionSpec();
        sSpecF.setName("folderTraversalSpec");
        datacenterHostTraversalSpec.getSelectSet().add(sSpecF);

        TraversalSpec folderTraversalSpec = new TraversalSpec();
        folderTraversalSpec.setName("folderTraversalSpec");
        folderTraversalSpec.setType("Folder");
        folderTraversalSpec.setPath("childEntity");
        folderTraversalSpec.setSkip(false);
        folderTraversalSpec.getSelectSet().add(sSpecF);
        folderTraversalSpec.getSelectSet().add(datacenterHostTraversalSpec);

        TraversalSpec computeResourceHostTraversalSpec = new TraversalSpec();
        computeResourceHostTraversalSpec.setName("computeResourceHostTraversalSpec");
        computeResourceHostTraversalSpec.setType("ComputeResource");
        computeResourceHostTraversalSpec.setPath("host");
        computeResourceHostTraversalSpec.setSkip(false);
        folderTraversalSpec.getSelectSet().add(computeResourceHostTraversalSpec);

        ObjectSpec oSpec = new ObjectSpec();
        oSpec.setObj(rootFolderRef);
        oSpec.setSkip(false);
        oSpec.getSelectSet().add(folderTraversalSpec);

        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getPropSet().add(hostPropSpec);
        propertyFilterSpec.getObjectSet().add(oSpec);

        List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>();
        listpfs.add(propertyFilterSpec);
        List<ObjectContent> listObjContent = retrievePropertiesAllObjects(vimPort, serviceContent.getPropertyCollector(),
                listpfs);

        return listObjContent;
    }

    public static List<ObjectContent> retrievePropertiesAllObjects(VimPortType vimPort, ManagedObjectReference propCollectorRef,
            List<PropertyFilterSpec> listpfs) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg
    {

        RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();
        List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();
        RetrieveResult rslts = vimPort.retrievePropertiesEx(propCollectorRef, listpfs, propObjectRetrieveOpts);
        if (rslts != null && rslts.getObjects() != null && !rslts.getObjects().isEmpty())
        {
            listobjcontent.addAll(rslts.getObjects());
        }
        String token = null;
        if (rslts != null && rslts.getToken() != null)
        {
            token = rslts.getToken();
        }
        while (token != null && !token.isEmpty())
        {
            rslts = vimPort.continueRetrievePropertiesEx(propCollectorRef, token);
            token = null;
            if (rslts != null)
            {
                token = rslts.getToken();
                if (rslts.getObjects() != null && !rslts.getObjects().isEmpty())
                {
                    listobjcontent.addAll(rslts.getObjects());
                }
            }
        }

        return listobjcontent;
    }

    public List<Host> getHostsList(UserSessionService userSessionService) throws Exception
    {
        logger.info("Getting Host Objects");
        ServiceContent serviceContent = VCenterHelper.getServiceContent(userSessionService, vimPort);
        List<Host> hosts = new ArrayList<>();
        /**
         * Get All Host object
         */
        // -------------Set property specs --------------
        PropertySpec hostPropSpec = new PropertySpec();
        hostPropSpec.setType("HostSystem");
        hostPropSpec.setAll(false);
        hostPropSpec.getPathSet().add("name");
        hostPropSpec.getPathSet().add("config");
        hostPropSpec.getPathSet().add("hardware");
        // -----------------------------------------------
        List<ObjectContent> listObjects = getHostObjs(hostPropSpec, vimPort, serviceContent, serviceContent.getRootFolder());
        List<VMNIC> nicList = null;
        Map<String, HostPciDevice> hwData = null;
        if (listObjects != null)
            for (ObjectContent oc : listObjects)
            {
                Host host = new Host();
                host.setId(oc.getObj().getValue());
                List<DynamicProperty> dpsHost = oc.getPropSet();
                for (DynamicProperty dp : dpsHost)
                {
                    if (dp.getName().equals("name"))
                    {
                        host.setName(dp.getVal().toString());
                    }
                    if (dp.getName().equals("config"))
                    {
                        nicList = getPNicInfo(dp);
                    }
                    if (dp.getName().equals("hardware"))
                    {
                        hwData = getHardwareInfo(dp);
                    }
                }
                List<Adapter> adapters = new ArrayList<>();
                Map<String, List<VMNIC>> nicGrp = mergPNicAndHWData(nicList, hwData);
                for (String key : nicGrp.keySet())
                {
                    Adapter adapter = new Adapter();
                    adapter.setName(key);
                    adapter.setId(key);
                    adapter.setChildren(nicGrp.get(key));
                    adapters.add(adapter);
                }
                host.setChildren(adapters);
                hosts.add(host);
            }
        return hosts;
    }

    private Map<String, HostPciDevice> getHardwareInfo(DynamicProperty dp)
    {
        Map<String, HostPciDevice> map = new HashMap<>();
        HostHardwareInfo hwInfo = (HostHardwareInfo) dp.getVal();
        if (hwInfo != null)
        {
            List<HostPciDevice> pciDecices = hwInfo.getPciDevice();
            if (pciDecices != null && pciDecices.size() > 0)
            {
                for (HostPciDevice pciDecice : pciDecices)
                {
                    if ("Solarflare".equalsIgnoreCase(pciDecice.getVendorName()))
                        map.put(pciDecice.getId(), pciDecice);
                }
            }
        }
        return map;
    }

    private List<VMNIC> getPNicInfo(DynamicProperty dp)
    {
        List<VMNIC> nics = new ArrayList<>();
        HostConfigInfo configInfo = (HostConfigInfo) dp.getVal();
        if (configInfo != null)
        {
            HostNetworkInfo networkInfo = configInfo.getNetwork();
            if (networkInfo != null)
            {
                List<PhysicalNic> pnic = networkInfo.getPnic();
                if (pnic != null && pnic.size() > 0)
                {
                    for (PhysicalNic physicalNic : pnic)
                    {
                        if (SFC.equalsIgnoreCase(physicalNic.getDriver()))
                        {
                            VMNIC pnicInfo = new VMNIC();

                            String pnicKey = physicalNic.getKey();
                            pnicInfo.setId(pnicKey);

                            String pnicDevice = physicalNic.getDevice();
                            pnicInfo.setName(pnicDevice);

                            pnicInfo.setPciId(physicalNic.getPci());
                            nics.add(pnicInfo);
                        }
                    }
                }
            }
        }
        return nics;
    }

    private String getDriverVersion(List<SoftwarePackage> sfs) throws Exception
    {
        String cimProvider = null;
        try
        {
            for (SoftwarePackage sf : sfs)
            {
                List<String> vendors = Arrays.asList(new String[]
                { "SLF", "Solarflare" });
                if (vendors.contains(sf.getVendor()) && sf.getName().contains("net"))
                {
                    cimProvider = sf.getVersion();
                    break;
                }

            }
        }
        catch (Exception e1)
        {
            throw e1;
        }
        return cimProvider;
    }

    /**
     * 
     * @param hostId
     * @return
     * @throws Exception
     */
    public Host getHostById(UserSessionService userSession, String hostId) throws Exception
    {
        logger.info("Getting Host Object by Host Id : " + hostId);
        Host host = null;
        ServiceContent serviceContent = VCenterHelper.getServiceContent(userSession, vimPort);
        Map<String, HostPciDevice> hwData = null;
        List<VMNIC> nicList = null;
        try
        {
            ManagedObjectReference rootFolder = serviceContent.getRootFolder();
            rootFolder.setType("HostSystem");
            rootFolder.setValue(hostId);
            // -------------Set property specs --------------
            PropertySpec hostPropSpec = new PropertySpec();
            hostPropSpec.setType("HostSystem");
            hostPropSpec.setAll(false);
            hostPropSpec.getPathSet().add("name");
            hostPropSpec.getPathSet().add("config");
            hostPropSpec.getPathSet().add("hardware");
            hostPropSpec.getPathSet().add("configManager");
            // -----------------------------------------------
            List<ObjectContent> objectList = getHostObjs(hostPropSpec, vimPort, serviceContent, rootFolder);
            if (objectList != null)
            {
                host = new Host();
                for (ObjectContent oc : objectList)
                {
                    host.setId(oc.getObj().getValue());
                    List<DynamicProperty> dpsHost = oc.getPropSet();
                    for (DynamicProperty dp : dpsHost)
                    {
                        if (dp.getName().equals("name"))
                        {
                            host.setName(dp.getVal().toString());
                        }
                        else if (dp.getName().equals("config"))
                        {
                            nicList = getPNicInfo(dp);
                            host.setPortCount(nicList.size());
                        }
                        else if (dp.getName().equals("hardware"))
                        {
                            hwData = getHardwareInfo(dp);
                        }
                        else if (dp.getName().equals("configManager"))
                        {
                            HostConfigManager hcm = (HostConfigManager) dp.getVal();
                            ManagedObjectReference icmMor = hcm.getImageConfigManager();
                            List<SoftwarePackage> softwarePackage = vimPort.fetchSoftwarePackages(icmMor);
                            host.setCimProviderVersion(getCimProviderVersion(softwarePackage));
                            host.setDriverVersion(getDriverVersion(softwarePackage));
                        }
                    }
                    List<Adapter> adapters = new ArrayList<>();
                    Map<String, List<VMNIC>> nicGrp = mergPNicAndHWData(nicList, hwData);
                    for (String key : nicGrp.keySet())
                    {
                        Adapter adapter = new Adapter();
                        adapter.setName(key);
                        adapter.setId(key);
                        adapter.setChildren(nicGrp.get(key));
                        adapters.add(adapter);
                    }
                    host.setAdapterCount(adapters.size());
                }
            }
        }
        catch (Exception e)
        {
            throw e;
        }
        return host;
    }

    private Map<String, List<VMNIC>> mergPNicAndHWData(List<VMNIC> pnicData, Map<String, HostPciDevice> hwData) throws Exception
    {
        Map<String, List<VMNIC>> nicGrp = new HashMap<>();
        if (pnicData != null && hwData != null)
        {
            for (VMNIC nic : pnicData)
            {
                HostPciDevice pciDecice = hwData.get(nic.getPciId());
                if (pciDecice == null)
                    continue;

                nic.setDeviceName(pciDecice.getDeviceName());
                nic.setDeviceId(Short.toString(pciDecice.getDeviceId()));
                nic.setVendorId(Short.toString(pciDecice.getVendorId()));
                nic.setVendorName(pciDecice.getVendorName());
                String adapterName = nic.getVendorName() + " " + nic.getDeviceName();
                if (nicGrp.get(adapterName) == null)
                {
                    List<VMNIC> niclist = new ArrayList<>();

                    niclist.add(nic);
                    nicGrp.put(adapterName, niclist);
                }
                else
                {
                    nicGrp.get(adapterName).add(nic);
                }

            }

//             for (String key : nicGrp.keySet())
//             {
//             Adapter adapter = new Adapter();
//             adapter.setName(key);
//             adapter.setId(key);
//             adapter.setChildren(nicGrp.get(key));
//             adapters.add(adapter);
//             }
        }
        return nicGrp;
    }

    public CIMHost getCIMHost(ServiceContent serviceContent, String hostId) throws Exception
    {
        logger.info("Getting CIM host object for a host : " + hostId);
        ManagedObjectReference rootFolder = serviceContent.getRootFolder();
        CIMHost cimHost = null;
        String hostName = null;
        rootFolder.setType("HostSystem");
        rootFolder.setValue(hostId);
        // -------------Set property specs --------------
        PropertySpec hostPropSpec = new PropertySpec();
        hostPropSpec.setType("HostSystem");
        hostPropSpec.setAll(false);
        hostPropSpec.getPathSet().add("name");
        // -----------------------------------------------
        List<ObjectContent> objectList = getHostObjs(hostPropSpec, vimPort, serviceContent, rootFolder);
        List<DynamicProperty> dpsHost = null;
        if (objectList != null)
        {
            ObjectContent objectContent = objectList.get(0);
            dpsHost = objectContent.getPropSet();
            for (DynamicProperty dp : dpsHost)
            {
                if (dp.getName().equals("name"))
                {
                    hostName = dp.getVal().toString();
                    HostServiceTicket ticket = vimPort.acquireCimServicesTicket(objectContent.getObj());
                    URL cimBaseURL = cim.cimBaseUrl(hostName);
                    cimHost = new CIMHostSession(cimBaseURL.toString(), ticket.getSessionId());
                    break;
                }
            }

        }
        return cimHost;

    }

    public List<Adapter> getAdapters(UserSessionService userSession, String hostId) throws Exception
    {
        logger.info("Getting Adapters for Host by Host Id : " + hostId);
        List<Adapter> adapters = null;
        Map<String, HostPciDevice> hwData = null;
        List<VMNIC> nicList = null;
        try
        {
            ServiceContent serviceContent = VCenterHelper.getServiceContent(userSession, vimPort);
            ManagedObjectReference rootFolder = serviceContent.getRootFolder();
            rootFolder.setType("HostSystem");
            rootFolder.setValue(hostId);
            // -------------Set property specs --------------
            PropertySpec hostPropSpec = new PropertySpec();
            hostPropSpec.setType("HostSystem");
            hostPropSpec.setAll(false);
            hostPropSpec.getPathSet().add("name");
            hostPropSpec.getPathSet().add("config");
            hostPropSpec.getPathSet().add("hardware");
            // -----------------------------------------------
            List<ObjectContent> objectList = getHostObjs(hostPropSpec, vimPort, serviceContent, rootFolder);
            String hostName = null;
            if (objectList != null)
            {
                adapters = new ArrayList<>();
                for (ObjectContent oc : objectList)
                {

                    List<DynamicProperty> dpsHost = oc.getPropSet();
                    for (DynamicProperty dp : dpsHost)
                    {
                        if (dp.getName().equals("name"))
                        {
                            hostName = dp.getVal().toString();
                        }
                        if (dp.getName().equals("config"))
                        {
                            nicList = getPNicInfo(dp);
                        }
                        else if (dp.getName().equals("hardware"))
                        {
                            hwData = getHardwareInfo(dp);
                        }
                    }
                    Map<String, List<VMNIC>> nicGrp = mergPNicAndHWData(nicList, hwData);
                    for (String key : nicGrp.keySet())
                    {
                        Adapter adapter = new Adapter();
                        adapter.setName(key);
                        adapter.setId(key);
                        List<VMNIC> listNic = nicGrp.get(key);
                        adapter.setChildren(listNic);
                        if (listNic != null && listNic.size() > 0)
                        {
                            String deviceId = listNic.get(0).getName();
                            HostServiceTicket ticket = vimPort.acquireCimServicesTicket(oc.getObj());
                            URL cimBaseURL = cim.cimBaseUrl(hostName);
                            CIMHost cimHost = new CIMHostSession(cimBaseURL.toString(), ticket.getSessionId());
                            Map<String, String> versions = cim.getAdapterVersions(cimHost, deviceId);

                            String controllerVersion = versions.get(CIMConstants.CONTROLLER_VERSION);
                            String bootROMVersion = versions.get(CIMConstants.BOOT_ROM_VERSION);
                            String firmwareVersion = versions.get(CIMConstants.FIRMARE_VERSION);
                            String UEFIROMVersion = versions.get(CIMConstants.UEFI_ROM_VERSION);
                            adapter.setVersionController(controllerVersion);
                            adapter.setVersionBootROM(bootROMVersion);
                            adapter.setVersionFirmware(firmwareVersion);
                            adapter.setVersionUEFIROM(UEFIROMVersion);

                            // Get version from image binary for controller
                            CIMInstance fwInstance = cim.getFirmwareSoftwareInstallationInstance(cimHost);
                            CIMInstance niCimInstance = cim.getNICCardInstance(cimHost, deviceId);
                            String latestControllerVersion = cim.getLatestControllerFWImageVersion(serviceContent, vimPort,cimHost,fwInstance,niCimInstance);;

                            // Get latest version otherwise blank value if both are equal
                            String latestVersion = VCenterHelper.getLatestVersion(controllerVersion, latestControllerVersion);
                            logger.debug("Getting latest version of controller is :" + latestVersion);
                            // Check for latest version available
                            if (latestVersion.equals(latestControllerVersion))
                            {
                                adapter.setLaterVersionAvailable(true);
                            }
                            else
                            {
                                // Get version from image binary for BootRom
                            	CIMInstance bootROMInstance = cim.getBootROMSoftwareInstallationInstance(cimHost);
                            	String latestBootRomVersion = cim.getLatestBootROMFWImageVersion(serviceContent, vimPort,cimHost,bootROMInstance,niCimInstance);
                                logger.debug("Getting latest version of BootRom is :" + latestBootRomVersion);
                                String finalLatestBootVersion = VCenterHelper.getLatestVersion(bootROMVersion,
                                        latestBootRomVersion);
                                if (latestBootRomVersion.equals(finalLatestBootVersion))
                                {
                                    adapter.setLaterVersionAvailable(true);
                                }
                            }

                        }
                        adapters.add(adapter);
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error getting Adapters for Host " + e.getMessage());
            throw e;
        }
        return adapters;

    }
}
