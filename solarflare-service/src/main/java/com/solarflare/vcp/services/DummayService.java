package com.solarflare.vcp.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.solarflare.vcp.model.Adapter;
import com.solarflare.vcp.model.AdapterNicStatistics;
import com.solarflare.vcp.model.AdapterOverview;
import com.solarflare.vcp.model.Debugging;
import com.solarflare.vcp.model.Host;
import com.solarflare.vcp.model.HostConfiguration;
import com.solarflare.vcp.model.NetQueue;
import com.solarflare.vcp.model.NicBootParamInfo;
import com.solarflare.vcp.model.Overlay;
import com.solarflare.vcp.model.VMNIC;
import com.solarflare.vcp.model.VMNICResponse;

public class DummayService implements HostAdapterService {

	static final Map<String,HostConfiguration> hostconfig = new HashMap<>();
	
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

		HostConfiguration hostConfiguration = null;
		if(hostconfig.size() == 0 )
		{
			hostConfiguration = new HostConfiguration();
			NetQueue netQueue = new NetQueue();
			netQueue.setNetQueueCount(8);
			netQueue.setRss(4);

			Debugging debugging = new Debugging();
			debugging.setUtils(true);
			debugging.setReceive(true);

			Overlay overlay = new Overlay();
			overlay.setGeneveOffloadEnable(true);
			overlay.setVxlanOffloadEnable(true);

			hostConfiguration.setNetQueue(netQueue);
			hostConfiguration.setDebuggingMask(debugging);
			hostConfiguration.setOverlay(overlay);
		}
		else
		{
			return hostconfig.get("1");
		}
		return hostConfiguration;
	}

	@Override
	public void updateHostConfigurations(String hostId, HostConfiguration hostConfigurationRequest) throws Exception {
		hostconfig.put("1",hostConfigurationRequest);
	}
	
	@Override
	public AdapterOverview getAdapterOverview(String hostId, String nicId) throws Exception {
		
		AdapterOverview overview = new AdapterOverview();
		overview.setSerialNumber("231231231231");
		overview.setPortNumber("SFNsdsdssd");
		return overview;
	}
	
	
	public VMNICResponse getAdapterForNIC(String hostId, String nicId)  throws Exception{
		
		VMNICResponse nicResponse = new VMNICResponse();
		nicResponse.setDeviceId("device id");
		nicResponse.setSubSystemDeviceId("subsystem device id");
		nicResponse.setSubSystemVendorId("subsystem vender id");
		nicResponse.setVendorId("venderId");
		nicResponse.setDriverVersion("10.1.1.1");
		nicResponse.setMacAddress("aa.bb.cc.ee");
		nicResponse.setLinkStatus("link status");
		nicResponse.setInterfaceName("Interface name");
		nicResponse.setPortSpeed("port speed");
		nicResponse.setPciBusNumber("pci bus number");
		nicResponse.setPciFunction("pci function");
		nicResponse.setDriverName("driver name");
		return nicResponse;
	}

	public AdapterNicStatistics getAdapterNicStatistics(String hostId, String nicId) {
		  AdapterNicStatistics stat = new AdapterNicStatistics();
		  stat.setTimePeriod("Last 1 Hour");
		  stat.setPacketsReceived("57");
		  stat.setPacketsSent("69");
		  stat.setBytesReceived("4502");
		  stat.setBytesSent("5770");
		  stat.setReceivePacketsDropped("0");
		  stat.setTransmitPacketsDropped("0");
		  stat.setMulticastPacketsReceived("0");
		  stat.setBroadcastPacketsSent("0");  
		  stat.setTotalReceiveError("0");
		  stat.setReceiveLengthErrors("0");
		  stat.setReceiveOverErrors("0");
		  stat.setReceiveCRCErrors("0");
		  stat.setReveiveFrameErrors("0");
		  stat.setReveiveFIFOErrors("0");
		  stat.setReceiveMissedErrors("0");
		  stat.setTotalReceiveError("0");
		  stat.setTotalTransmitErrors("0");
		  stat.setTransmitAbortedErrors("0");
		  stat.setTransmitCarrierErrors("0");
		  stat.setTransmitFIFOErrors("0");
		  stat.setMulticastPacketsReceived("0");
		  stat.setBroadcastPacketsSent("0");
		  return stat;
		 }
}
