package com.solarflare.vcp.services;

import java.util.List;

import com.solarflare.vcp.model.Adapter;
import com.solarflare.vcp.model.AdapterNicStatistics;
import com.solarflare.vcp.model.AdapterOverview;
import com.solarflare.vcp.model.Host;
import com.solarflare.vcp.model.HostConfiguration;
import com.solarflare.vcp.model.NicBootParamInfo;
import com.solarflare.vcp.model.VMNICResponse;

public interface HostAdapterService {

	/**
	 * List all Adapters for all hosts
	 * 
	 * @return Host list with adapters.
	 */
	List<Host> getHostList() throws Exception;

	/**
	 * List all Adapters for given hosts
	 * 
	 * @return
	 * @throws Exception
	 */

	List<Adapter> getHostAdapters(String hostId) throws Exception;

	/**
	 * Get host details by id
	 * 
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

	/**
	 * upload a binary file for updating cim version
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */

	String updateFirmwareToLatest(List<Adapter> adapterList, String hostId) throws Exception;

	String customUpdateFirmwareFromLocal(List<Adapter> adapterList, String hostId, String base64Data) throws Exception;

	String customUpdateFirmwareFromURL(List<Adapter> adapterList, String hostId, String fwImagePath) throws Exception;

	/**
	 * Validate firmware binary file
	 * 
	 * @param file
	 * @param isLocal
	 * @return
	 * @throws Exception
	 */

	boolean validateTypeAndSubTupe(String file, boolean isLocal) throws Exception;

	HostConfiguration getHostConfigurations(String hostId) throws Exception;

	void updateHostConfigurations(String hostId, HostConfiguration hostConfigurationRequest) throws Exception;
	
	AdapterOverview getAdapterOverview(String hostId, String nicId) throws Exception;
	
	VMNICResponse getAdapterForNIC(String hostId, String nicId) throws Exception ;

	AdapterNicStatistics getAdapterNicStatistics(String hostId, String nicId) throws Exception;
}
