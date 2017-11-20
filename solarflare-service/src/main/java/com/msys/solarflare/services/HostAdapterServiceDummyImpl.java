package com.msys.solarflare.services;

import java.util.List;

import com.msys.solarflare.model.Adapter;
import com.msys.solarflare.model.DummyData;
import com.msys.solarflare.model.Host;
import com.msys.solarflare.model.NicBootParamInfo;

public class HostAdapterServiceDummyImpl implements HostAdapterService {
	
	private DummyData dataService = new DummyData();

	@Override
	public List<Host> getHostList() {
		return dataService.getHostAdapters();
	}

	@Override
	public Host getHostById(String hostId) {
		return dataService.getHostAdapters(hostId);
	}

	@Override
	public NicBootParamInfo getNicParamInfo(String hostId, String nicId) throws Exception {
		// TODO Auto-generated method stub
		throw new Exception("Method not implemented yet in HostAdapterServiceDummyImpl class");
	}

    @Override
    public List<Adapter> getHostAdapters(String Id) throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

}
