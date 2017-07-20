package com.msys.solarflare.services;

import java.util.List;

import com.msys.solarflare.model.DummyData;
import com.msys.solarflare.model.Host;

public class HostAdapterServiceDummyImpl implements HostAdapterService {
	
	private DummyData dataService = new DummyData();

	@Override
	public List<Host> getHostAdapters() {
		return dataService.getHostAdapters();
	}

	@Override
	public Host getHostAdapters(String hostId) {
		return dataService.getHostAdapters(hostId);
	}

}
