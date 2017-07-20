package com.msys.solarflare.services;

import java.util.List;

import com.msys.solarflare.model.Host;

public interface HostAdapterService {
	/**
	 * List all Adapters for all hosts
	 * 
	 * @return Host list with adapters.
	 */
	List<Host> getHostAdapters();

	/**
	 * List all Adapters for given hosts
	 * 
	 * @return Host with adapters
	 */
	Host getHostAdapters(String hostId);
}
