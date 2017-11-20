package com.msys.solarflare.vim25;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.msys.solarflare.model.Adapter;
import com.msys.solarflare.model.Host;
import com.msys.solarflare.model.NicBootParamInfo;
import com.msys.solarflare.model.VMNIC;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.HostConfigInfo;
import com.vmware.vim25.HostConfigManager;
import com.vmware.vim25.HostHardwareInfo;
import com.vmware.vim25.HostNetworkInfo;
import com.vmware.vim25.HostPciDevice;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PhysicalNic;
import com.vmware.vim25.PhysicalNicLinkInfo;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.Relation;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.SelectionSpec;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.SoftwarePackage;
import com.vmware.vim25.TraversalSpec;
import com.vmware.vim25.VimPortType;

public class NetworkAPI {
	ServiceContent serviceContent;
	private VimPortType vimPort;

	public NetworkAPI() {

	}

	public static void main(String[] args) {
		System.out.println(new NetworkAPI().getHostWithAdapter());
	}

	private void connect() {
		Connection con = new Connection();
		serviceContent = con.getServiceContent();
		vimPort = con.getVimPort();
	}

	private void disconnect() {
		try {
			vimPort.logout(serviceContent.getSessionManager());
		} catch (RuntimeFaultFaultMsg e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public List<Host> getHostWithAdapter() {
		connect();
		List<Host> hostList = null;
		try {
			hostList = getHosts();
		} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
			// TODO Auto-generated catch block
			hostList = Collections.emptyList();
		}
		disconnect();
		//Collections.sort(hostList);
		return hostList;
	}
	
	public Host getHostInfo(String hostId)
	{
		connect();
		Host host = null;
		 List<Host> hostList = null;
		try {
			 hostList = getHosts();
			 
			 for(Host hostresp :hostList)
			 {
				 if(hostresp.getId().equals(hostId))
				 {
					 host =  hostresp;
					 break;
				 }
			 }
		} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
			// TODO Auto-generated catch block
			hostList = Collections.emptyList();
		}
		disconnect();
		//Collections.sort(hostList);
		return host;
	}

	private List<Host> getHosts() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
		List<Host> hosts = new ArrayList<>();
		List<ObjectContent> listObjects = getHostObjs(vimPort, serviceContent, serviceContent.getRootFolder());
		//int portcount = 0;
		if (listObjects != null)
			for (ObjectContent oc : listObjects) {
				Host host = new Host();
				host.setId(oc.getObj().getValue());
				// host.setName("10.101.10.3");

				List<DynamicProperty> dpsHost = oc.getPropSet();
				for (DynamicProperty dp : dpsHost) {
					if (dp.getName().equals("name")) {
						host.setName(dp.getVal().toString());
					}
					if(dp.getName().equals("configManager"))
					{
						host.setCimProviderVersion(getCimProviderVersion(dp));
						host.setDriverVersion(getDriverVersion(dp));
					}
				}

				List<VMNIC> nics = getPNic(oc);
				// group by adapter type
				Map<String, List<VMNIC>> nicGrp = new HashMap<>();
				//portcount = portcount + nics.size();
				for (VMNIC nic : nics) {
					String adapterName = nic.getVendorName() + " " + nic.getDeviceName();
					if (nicGrp.get(adapterName) == null) {
						List<VMNIC> niclist = new ArrayList<>();
						niclist.add(nic);
						nicGrp.put(adapterName, niclist);
					} else {
						nicGrp.get(adapterName).add(nic);
					}
				}
				List<Adapter> adapters = new ArrayList<>();
				for (String key : nicGrp.keySet()) {
					Adapter adapter = new Adapter();
					adapter.setName(key);
					adapter.setId(key);
					adapter.setChildren(nicGrp.get(key));
					adapters.add(adapter);
				}
				host.setChildren(adapters);
				host.setAdapterCount(adapters.size());
				host.setPortCount(nics.size());
				hosts.add(host);
			}

		return hosts;
	}

	private List<VMNIC> getPNic(ObjectContent oc) {
		List<DynamicProperty> dpsHost = oc.getPropSet();
		List<VMNIC> pnicData = null;
		Map<String, HostPciDevice> hwData = null;
		if (dpsHost != null)
			for (DynamicProperty dp : dpsHost) {
				switch (dp.getName()) {
				case "config":
					pnicData = getPNicInfo(dp);
					break;
				case "hardware":
					hwData = getHardwareInfo(dp);
					break;
				case "configManager":
					getconfigManagerInfo(dp);
					break;
				}
			}
		mergeHwNicData(pnicData, hwData);
		return pnicData;
	}

	private String getCimProviderVersion(DynamicProperty dp)
	{
		String cimProvider = null;
		System.out.println("Getting configManager info" + dp);
		HostConfigManager hcm = (HostConfigManager) dp.getVal();
		ManagedObjectReference icmMor = hcm.getImageConfigManager();
		System.out.println(icmMor.getType() + " and value= " + icmMor.getValue());

		try {
			List<SoftwarePackage> sfs = vimPort.fetchSoftwarePackages(icmMor);
			System.out.println(sfs.size());
			for (SoftwarePackage sf : sfs) {
				List<String> vendors = Arrays.asList(new String[] { "SLF", "Solarflare" });
				if (vendors.contains(sf.getVendor()) && sf.getName().contains("cim")) {
					cimProvider = sf.getVersion();
				}

			}
		} catch (RuntimeFaultFaultMsg e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return cimProvider;
	}
	private String getDriverVersion(DynamicProperty dp)
	{
		String cimProvider = null;
		System.out.println("Getting configManager info" + dp);
		HostConfigManager hcm = (HostConfigManager) dp.getVal();
		ManagedObjectReference icmMor = hcm.getImageConfigManager();
		System.out.println(icmMor.getType() + " and value= " + icmMor.getValue());

		try {
			List<SoftwarePackage> sfs = vimPort.fetchSoftwarePackages(icmMor);
			System.out.println(sfs.size());
			for (SoftwarePackage sf : sfs) {
				List<String> vendors = Arrays.asList(new String[] { "SLF", "Solarflare" });
				if (vendors.contains(sf.getVendor()) && sf.getName().contains("net")) {
					cimProvider = sf.getVersion();
				}

			}
		} catch (RuntimeFaultFaultMsg e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return cimProvider;
	}
	private void getconfigManagerInfo(DynamicProperty dp) {
		System.out.println("Getting configManager info" + dp);
		HostConfigManager hcm = (HostConfigManager) dp.getVal();
		ManagedObjectReference icmMor = hcm.getImageConfigManager();
		System.out.println(icmMor.getType() + " and value= " + icmMor.getValue());

		try {
			List<SoftwarePackage> sfs = vimPort.fetchSoftwarePackages(icmMor);
			System.out.println(sfs.size());
			for (SoftwarePackage sf : sfs) {
				List<String> vendors = Arrays.asList(new String[] { "SLF", "Solarflare" });
				if (vendors.contains(sf.getVendor())) {
					System.out.println("Package Name: " + sf.getName());
					System.out.println("Package Description: " + sf.getDescription());
					System.out.println("Package Summary: " + sf.getSummary());
					System.out.println("Package Version: " + sf.getVersion());
					System.out.println("Package Vendor: " + sf.getVendor());
					System.out.println("Package tag: " + sf.getTag());
					System.out.println("Package Provides: " + sf.getProvides());
					System.out.println("Package payload: " + sf.getPayload());
					System.out.print("Package depends: ");
					for (Relation relation : sf.getDepends())
						System.out.print("\t" + relation.getName());
					System.out.println("\n -------------- \n");
				}

			}
		} catch (RuntimeFaultFaultMsg e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	private void mergeHwNicData(List<VMNIC> pnicData, Map<String, HostPciDevice> hwData) {
		if (pnicData == null || hwData == null)
			return;

		for (VMNIC nic : pnicData) {
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

	private List<VMNIC> getPNicInfo(DynamicProperty dp) {
		List<VMNIC> nics = new ArrayList<>();

		HostConfigInfo configInfo = (HostConfigInfo) dp.getVal();
		if (configInfo != null) {
			HostNetworkInfo networkInfo = configInfo.getNetwork();
			if (networkInfo != null) {
				List<PhysicalNic> pnic = networkInfo.getPnic();
				if (pnic != null && pnic.size() > 0) {
					for (PhysicalNic physicalNic : pnic) {
						if ("sfc".equalsIgnoreCase(physicalNic.getDriver())) {
							VMNIC pnicInfo = new VMNIC();

							String pnicKey = physicalNic.getKey();
							pnicInfo.setId(pnicKey);

							String pnicDriver = physicalNic.getDriver();
							pnicInfo.setDriverName(pnicDriver);

							String pnicDevice = physicalNic.getDevice();
							pnicInfo.setName(pnicDevice);

							String pnicMac = physicalNic.getMac();
							pnicInfo.setMacAddress(pnicMac);

							PhysicalNicLinkInfo linkSpeed = physicalNic.getLinkSpeed();
							if (linkSpeed != null) {
								pnicInfo.setStatus("UP");
								pnicInfo.setPortSpeed(linkSpeed.getSpeedMb() + " Mbps");
							} else {
								pnicInfo.setStatus("Down");
								pnicInfo.setPortSpeed(0 + " Mbps");
							}

							pnicInfo.setPciId(physicalNic.getPci());
							nics.add(pnicInfo);
						}
					}
				}
			}
		}
		return nics;
	}

	private Map<String, HostPciDevice> getHardwareInfo(DynamicProperty dp) {
		Map<String, HostPciDevice> map = new HashMap<>();
		HostHardwareInfo hwInfo = (HostHardwareInfo) dp.getVal();
		if (hwInfo != null) {
			List<HostPciDevice> pciDecices = hwInfo.getPciDevice();
			if (pciDecices != null && pciDecices.size() > 0) {
				for (HostPciDevice pciDecice : pciDecices) {
					if ("Solarflare".equalsIgnoreCase(pciDecice.getVendorName()))
						map.put(pciDecice.getId(), pciDecice);
				}
			}
		}
		return map;
	}

	private List<ObjectContent> getHostObjs(VimPortType vimPort, ServiceContent serviceContent,
			ManagedObjectReference rootFolderRef) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {

		PropertySpec hostPropSpec = new PropertySpec();
		// hostPropSpec.setType(rootFolderRef.getType());
		hostPropSpec.setType("HostSystem");
		hostPropSpec.setAll(false);
		// hostPropSpec.getPathSet().addAll(Collections.<String>emptyList());
		hostPropSpec.getPathSet().add("name");
		hostPropSpec.getPathSet().add("config");
		hostPropSpec.getPathSet().add("hardware");
		hostPropSpec.getPathSet().add("configManager");

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
		// propertyFilterSpec.getPropSet().addAll(Arrays.asList(hostPropSpec));
		propertyFilterSpec.getPropSet().add(hostPropSpec);
		// propertyFilterSpec.getObjectSet().addAll(ospecList);
		propertyFilterSpec.getObjectSet().add(oSpec);

		List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>();
		listpfs.add(propertyFilterSpec);
		List<ObjectContent> listObjContent = retrievePropertiesAllObjects(vimPort,
				serviceContent.getPropertyCollector(), listpfs);

		return listObjContent;
	}

	public static List<ObjectContent> retrievePropertiesAllObjects(VimPortType vimPort,
			ManagedObjectReference propCollectorRef, List<PropertyFilterSpec> listpfs)
			throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
		RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();
		List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();
		RetrieveResult rslts = vimPort.retrievePropertiesEx(propCollectorRef, listpfs, propObjectRetrieveOpts);
		if (rslts != null && rslts.getObjects() != null && !rslts.getObjects().isEmpty()) {
			listobjcontent.addAll(rslts.getObjects());
		}
		String token = null;
		if (rslts != null && rslts.getToken() != null) {
			token = rslts.getToken();
		}
		while (token != null && !token.isEmpty()) {
			rslts = vimPort.continueRetrievePropertiesEx(propCollectorRef, token);
			token = null;
			if (rslts != null) {
				token = rslts.getToken();
				if (rslts.getObjects() != null && !rslts.getObjects().isEmpty()) {
					listobjcontent.addAll(rslts.getObjects());
				}
			}
		}

		return listobjcontent;
	}

	public NicBootParamInfo getNicParamInfo(String hostId, String nicId) {

		return sampleInfo(hostId, nicId);

	}

	private NicBootParamInfo sampleInfo(String hostid, String nicId) {
		NicBootParamInfo info = new NicBootParamInfo();
		info.setHostId(hostid);
		info.setNicId(nicId);
		info.setBannerDelayTime("2 sec");
		info.setBootImage("Option ROM");
		info.setBootSkipDelayTime("5 sec");
		info.setBootType("Disabled");

		info.setFirmwareVariant("full features/ virtualization");
		info.setInsecureFilters("Disabled");
		info.setLinkSpeed("negotiated auto");
		info.setLinkUpDelayTime("5 sec");

		info.setMsiXinterruptLimit("32");
		info.setVfMsiXinterruptLimit("8");

		info.setNumOfVirtualFunctions("0");
		info.setPhysicalFunPerPort("1");
		info.setSwitchMode("Default");
		info.setvLANTags("None");

		return info;

	}

}