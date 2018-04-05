package com.solarflare.vcp.vim;

import java.util.List;
import java.util.Map;

import com.solarflare.vcp.cim.CIMHost;
import com.solarflare.vcp.model.Adapter;
import com.solarflare.vcp.model.Host;
import com.solarflare.vcp.vim.connection.Connection;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.RuntimeFaultFaultMsg;

public interface SfVimService {

	public Host getHostSummary(String hostId) throws Exception;

	public List<Host> getAllHosts() throws Exception;

	public List<Adapter> getHostAdapters(String hostId) throws Exception, RuntimeFaultFaultMsg;

	public CIMHost getCIMHost(String hostId) throws Exception;

	Connection getSession();

	String getPluginURL(String pluginKey) throws Exception;
	
	public String getOptionString(String hostId) throws Exception;
	
	public void updateOptionString(String hostId, String value)throws Exception;
	
	String getDriverVersion(String hostId) throws Exception;
	
	Map<String,PerfCounterInfo> getNicStatPerfCounters(ManagedObjectReference perfManager, List<String> nicStatPerfCounter)
			throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg;
	
	List<PerfEntityMetricBase> retriveStats(ManagedObjectReference perfManager, Map<String,PerfCounterInfo> nicStatPerfIdMap, String hostId, String nicId) throws RuntimeFaultFaultMsg,Exception;
	
	Map<Integer,Integer> processNicStats(List<PerfEntityMetricBase> retrievedStats, Map<String,PerfCounterInfo> nicStatPerfIdMap) throws Exception;
}
