package com.solarflare.vcp.services;

import java.util.List;

import com.solarflare.vcp.model.Adapter;
import com.solarflare.vcp.model.Host;
import com.solarflare.vcp.model.NicBootParamInfo;

public interface HostAdapterService {
    
   
    
	/**
	 * List all Adapters for all hosts
	 * 
	 * @return Host list with adapters.
	 */
	List<Host> getHostList() throws Exception;

	/**
	 * List all Adapters for given hosts 
	 * @return
	 * @throws Exception
	 */
	 
	List<Adapter> getHostAdapters(String hostId) throws Exception;
	/**
	 * Get host details by id
	 * @param hostId
	 * @return
	 * @throws Exception
	 */
	Host getHostById(String hostId) throws Exception;

	/**
	 * List nic boot param info.
	 * 
	 * @param id
	 *            : key of vm nic
	 * @param name
	 *            : name of vm nic
	 * @return : NicBootParamInfo
	 * @throws Exception 
	 */
	NicBootParamInfo getNicParamInfo(String hostId, String nicId) throws Exception;

}
