package com.solarflare.vcp.services;

import java.util.List;

import com.solarflare.vcp.model.Adapter;
import com.solarflare.vcp.model.Host;
import com.solarflare.vcp.model.HostConfiguration;
import com.solarflare.vcp.model.NicBootParamInfo;
import com.solarflare.vcp.model.Status;

public class DummayService implements HostAdapterService{

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
	public boolean updateFirmwareToLatest(List<Adapter> adapterList, String hostId) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean customUpdateFirmwareFromLocal(List<Adapter> adapterList, String hostId, String base64Data)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean customUpdateFirmwareFromURL(List<Adapter> adapterList, String hostId, String fwImagePath)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean validateTypeAndSubTupe(String file, boolean isLocal) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Status> getStatus(String hostId, String adapterId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HostConfiguration getHostConfigurations(String hostId) throws Exception {
		
		HostConfiguration hostConfiguration = new HostConfiguration();
		
		hostConfiguration.setNetQueueCount(8);
		hostConfiguration.setNetQueueRSS(4);
		hostConfiguration.setDebudggingMaskUtils(true);
		hostConfiguration.setDebudggingMaskReceive(true);
		hostConfiguration.setOverlayVxlanOffloadEnable(true);
		hostConfiguration.setOverlayGeneveOffloadEnable(true);
		return hostConfiguration;
	}

	@Override
	public void updateHostConfigurations(HostConfiguration hostConfigurationRequest)
			throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	

}
