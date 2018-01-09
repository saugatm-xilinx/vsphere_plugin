package com.solarflare.vcp.services;

import java.util.ArrayList;
import java.util.List;

import com.solarflare.vcp.model.Adapter;
import com.solarflare.vcp.model.AdapterOverview;
import com.solarflare.vcp.model.Debugging;
import com.solarflare.vcp.model.Host;
import com.solarflare.vcp.model.HostConfiguration;
import com.solarflare.vcp.model.NetQueue;
import com.solarflare.vcp.model.NicBootParamInfo;
import com.solarflare.vcp.model.Overlay;
import com.solarflare.vcp.model.VMNIC;

public class DummayService implements HostAdapterService {

	@Override
	public List<Host> getHostList() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Adapter> getHostAdapters(String hostId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Host getHostById(String hostId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NicBootParamInfo getNicParamInfo(String hostId, String nicId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String updateFirmwareToLatest(List<Adapter> adapterList, String hostId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String customUpdateFirmwareFromLocal(List<Adapter> adapterList, String hostId, String base64Data) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String customUpdateFirmwareFromURL(List<Adapter> adapterList, String hostId, String fwImagePath) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean validateTypeAndSubTupe(String file, boolean isLocal) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public HostConfiguration getHostConfigurations(String hostId) throws Exception {

		HostConfiguration hostConfiguration = new HostConfiguration();

		NetQueue netQueue = new NetQueue();
		netQueue.setNetQueueCount(8);
		netQueue.setRss(4);

		Debugging debugging = new Debugging();
		debugging.setMaskUtils(true);
		debugging.setMaskReceive(true);

		Overlay overlay = new Overlay();
		overlay.setGeneveOffloadEnable(true);
		overlay.setVxlanOffloadEnable(true);

		hostConfiguration.setNetQueue(netQueue);
		hostConfiguration.setDebugging(debugging);
		hostConfiguration.setOverlay(overlay);
		return hostConfiguration;
	}

	@Override
	public void updateHostConfigurations(HostConfiguration hostConfigurationRequest) throws Exception {
		// TODO Auto-generated method stub

	}
	
	@Override
	public AdapterOverview getAdapterOverview(String hostId, String nicId) throws Exception {
		
		AdapterOverview overview = new AdapterOverview();
		overview.setName("solarflare adapter-1");
		overview.setSerialNumber("231231231231");
		overview.setPortNumber("SFNsdsdssd");
		overview.setPciExpressLinkSpeed("8.0 Gt/s");
		overview.setPciExpressBusWidth("x8");
		return overview;
	}
	@Override
	public Adapter getAdapters(String hostId, String nicId)  throws Exception{
		
		Adapter adp = new Adapter();
		adp.setId("adapter-id");
		adp.setName("solarflare adapter-id");
		adp.setDeviceId("device id");
		adp.setSubSystemDeviceId("subsystem device id");
		adp.setSubSystemVendorId("subsystem vender id");
		adp.setVendorId("venderId");
		VMNIC vmNic = new VMNIC();
		vmNic.setName("nic name");
		vmNic.setId("nic-Id");
		vmNic.setDriverName("driver name");
		vmNic.setDriverVersion("10.1.1.1");
		vmNic.setMacAddress("aa.bb.cc.ee");
		vmNic.setStatus("link status");
		vmNic.setInterfaceName("Interface name");
		vmNic.setPortSpeed("port speed");
		vmNic.setCurrentMTU("current MTU");
		vmNic.setMaxMTU("max mtu");
		vmNic.setPciBusNumber("pci bus number");
		vmNic.setPciFunction("pci function");
		List<VMNIC> list = new ArrayList<>();
		list.add(vmNic);
		adp.setChildren(list);
		return adp;
	}

}
