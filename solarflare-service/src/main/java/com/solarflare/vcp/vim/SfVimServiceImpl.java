
package com.solarflare.vcp.vim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import com.solarflare.vcp.cim.CIMHost;
import com.solarflare.vcp.cim.CIMHostSession;
import com.solarflare.vcp.cim.SfCIMClientService;
import com.solarflare.vcp.cim.SfCIMService;
import com.solarflare.vcp.model.Adapter;
import com.solarflare.vcp.model.Host;
import com.solarflare.vcp.model.VMNIC;
import com.solarflare.vcp.vim.connection.Connection;
import com.solarflare.vcp.vim.helpers.GetMOREF;
import com.solarflare.vcp.vim.helpers.SfVimServiceHelper;
import com.vmware.vim25.ArrayOfHostPciDevice;
import com.vmware.vim25.ArrayOfPerfCounterInfo;
import com.vmware.vim25.ArrayOfPhysicalNic;
import com.vmware.vim25.Extension;
import com.vmware.vim25.ExtensionClientInfo;
import com.vmware.vim25.HostPciDevice;
import com.vmware.vim25.HostServiceTicket;
import com.vmware.vim25.HostSystemConnectionState;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfEntityMetricCSV;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfMetricSeriesCSV;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.PhysicalNic;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.SoftwarePackage;
import com.vmware.vise.security.ClientSessionEndListener;
import com.vmware.vise.usersession.ServerInfo;
import com.vmware.vise.usersession.UserSessionService;

public class SfVimServiceImpl implements SfVimService, InitializingBean, ClientSessionEndListener {
	private static final Log logger = LogFactory.getLog(SfVimServiceImpl.class);
	private static final String LOG_KEY = "Solarflare:: ";
	private static final String CIM_SCHEME = "https://";
	private static final String SOLARFLARE_MODULE_NAME = "sfvmk";
	private String extensionURL;
	private Connection connection;
	private UserSessionService userSessionService;
	private static final Map<String, CIMHost> cimHostCache = new HashMap<>();

	public UserSessionService getUserSessionService() {
		return userSessionService;
	}

	public void setUserSessionService(UserSessionService userSessionService) {
		this.userSessionService = userSessionService;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connect) {
		this.connection = connect;
	}

	public SfVimServiceImpl() {

	}

	public SfVimServiceImpl(Connection connection, UserSessionService userSessionService) {
		this.connection = connection;
		this.userSessionService = userSessionService;
	}

	/**
	 * Get a connection that is connected to server.
	 * 
	 * @return
	 */
	@Override
	public Connection getSession() {
		// Implementation of Junit test connection can be done here.
		return connection.connect(userSessionService, true);
	}

	@Override
	public String getPluginURL(String pluginKey) throws Exception {
		logger.info("getPluginURL() called with pluginKey : " + pluginKey);
		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: Get - getPluginURL");
		if (extensionURL == null || extensionURL.isEmpty()) {
			Connection _conn = getSession();
			ManagedObjectReference extensionManager = _conn.getServiceContent().getExtensionManager();
			Extension ext = _conn.getVimPort().findExtension(extensionManager, pluginKey);
			List<ExtensionClientInfo> extClientInfo = ext.getClient();
			for (ExtensionClientInfo clientInfo : extClientInfo) {
				extensionURL = clientInfo.getUrl();
			}
		}
		timer.stop();
		return extensionURL;
	}

	/**
	 * Returns host summary for given host MoRef. Summary includes CIM Provider
	 * and CIM driver versions, number of adapters and number of ports.
	 * 
	 * @throws Exception
	 */
	@Override
	public Host getHostSummary(String hostId) throws Exception {
		logger.info("getHostSummary() hostId : " + hostId);
		Host host = new Host();
		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: Get - getHostSummary");
		List<String> props = new ArrayList<>();
		props.add("configManager.imageConfigManager");
		props.add("hardware.pciDevice");
		props.add("config.network.pnic");
		props.add("name");

		Connection _conn = getSession();

		GetMOREF _moRefService = new GetMOREF(_conn.getVimPort(), _conn.getServiceContent());

		ManagedObjectReference hostMoRef = getManagedObjectReference("HostSystem", hostId);

		Map<String, Object> hostprops = _moRefService.entityProps(hostMoRef, props.toArray(new String[] {}));

		// Get name
		String name = (String) hostprops.get("name");
		host.setName(name);
		host.setId(hostId);

		List<Adapter> adapters = getAdapters(hostprops, hostId);
		host.setAdapterCount(adapters.size());
		host.setChildren(adapters);
		host.setPortCount(SfVimServiceHelper.getPortCount(adapters));

		// Get Version of CIM provider and Driver
		ManagedObjectReference imageConfigManager = (ManagedObjectReference) hostprops
				.get("configManager.imageConfigManager");

		List<SoftwarePackage> softwarePackage = _conn.getVimPort().fetchSoftwarePackages(imageConfigManager);
		host.setCimProviderVersion(SfVimServiceHelper.getCimProviderVersion(softwarePackage));
		host.setDriverVersion(SfVimServiceHelper.getDriverVersion(softwarePackage));

		timer.stop();
		logger.debug(LOG_KEY + "getHostSummary() returned: " + host);
		return host;
	}

	public String getDriverVersion(String hostId) throws Exception {
		logger.info("getDriverVersion() for hostId : " + hostId);

		List<String> props = new ArrayList<>();
		props.add("configManager.imageConfigManager");

		Connection _conn = getSession();

		GetMOREF _moRefService = new GetMOREF(_conn.getVimPort(), _conn.getServiceContent());
		ManagedObjectReference hostMoRef = getManagedObjectReference("HostSystem", hostId);

		Map<String, Object> hostprops = _moRefService.entityProps(hostMoRef, props.toArray(new String[] {}));
		// Get Version of CIM provider and Driver
		ManagedObjectReference imageConfigManager = (ManagedObjectReference) hostprops
				.get("configManager.imageConfigManager");

		List<SoftwarePackage> softwarePackage = _conn.getVimPort().fetchSoftwarePackages(imageConfigManager);

		String driverVersion = SfVimServiceHelper.getDriverVersion(softwarePackage);

		return driverVersion;
	}

	/**
	 * Returns all hosts of vCenter.
	 */
	@Override
	public List<Host> getAllHosts() throws Exception {
		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: Get - getAllHosts");
		List<Host> hostList = new ArrayList<>();

		List<String> props = new ArrayList<>();
		props.add("name");
		props.add("runtime.connectionState");

		Connection _conn = getSession();

		GetMOREF _moRefService = new GetMOREF(_conn.getVimPort(), _conn.getServiceContent());

		try {
			Map<ManagedObjectReference, Map<String, Object>> hosts = _moRefService.inContainerByType(
					_conn.getServiceContent().getRootFolder(), "HostSystem", props.toArray(new String[] {}));
			if (hosts == null || hosts.isEmpty())
				return hostList;

			for (ManagedObjectReference hostMoRef : hosts.keySet()) {
				Map<String, Object> hostprops = hosts.get(hostMoRef);
				HostSystemConnectionState state = (HostSystemConnectionState) hostprops.get("runtime.connectionState");

				if (!state.equals(HostSystemConnectionState.DISCONNECTED)) {
					Host host = new Host();
					host.setId(hostMoRef.getValue());
					// Get name
					String name = (String) hostprops.get("name");
					host.setName(name);
					logger.debug("Adding '" + name + "' host to hostList");
					hostList.add(host);
				} else {
					String name = (String) hostprops.get("name");
					logger.error("Host : '" + name + "' is in Disconnected state");
				}
			}
		} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
			// TODO Auto-generated catch block
			logger.error("Solarflare:: Error getting All hosts : " + e.getMessage());
			throw e;
		}
		timer.stop();
		logger.trace("Solarflare::Get - getAllHosts returned total hosts: " + hostList.size());
		return hostList;

	}

	/**
	 * returns Adapters for given host Id.
	 * 
	 * @param hostId
	 * @return
	 * @throws Exception
	 * @throws RuntimeFaultFaultMsg
	 */
	@Override
	public List<Adapter> getHostAdapters(String hostId) throws Exception, RuntimeFaultFaultMsg {

		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: Get - getHostAdapters");
		List<String> props = new ArrayList<>();
		// props.add("configManager.imageConfigManager");
		props.add("hardware.pciDevice");
		props.add("config.network.pnic");
		// props.add("name");

		Connection _conn = getSession();

		GetMOREF _moRefService = new GetMOREF(_conn.getVimPort(), _conn.getServiceContent());

		ManagedObjectReference hostMoRef = getManagedObjectReference("HostSystem", hostId);

		Map<String, Object> hostprops = _moRefService.entityProps(hostMoRef, props.toArray(new String[] {}));

		List<Adapter> adapters = getAdapters(hostprops, hostId);

		timer.stop();
		return adapters;
	}

	private List<Adapter> getAdapters(Map<String, Object> hostprops, String hostId) {

		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: Get - getAdapters");
		logger.info("Solarflare:: Get adapters for hostId : " + hostId);
		List<Adapter> adapters = new ArrayList<>();
		try {

			// Get PCI devices (key for this is hardware.pciDevice and return
			// type is ArrayOfHostPciDevice)
			ArrayOfHostPciDevice arrayOfHostPciDevice = (ArrayOfHostPciDevice) hostprops.get("hardware.pciDevice");
			List<HostPciDevice> sfDevices = SfVimServiceHelper.getSfPciDevice(arrayOfHostPciDevice.getHostPciDevice());
			List<String> sfDeviceIds = SfVimServiceHelper.getSfPciDeviceIds(sfDevices);
			// Get Physical Nics
			ArrayOfPhysicalNic arrayOfPhysicalNic = (ArrayOfPhysicalNic) hostprops.get("config.network.pnic");

			Map<String, PhysicalNic> sfPhysicalNics = SfVimServiceHelper
					.getSfPhysicalNic(arrayOfPhysicalNic.getPhysicalNic(), sfDeviceIds);
			Map<String, List<VMNIC>> vmNICMap = SfVimServiceHelper.mergeToVMNICObject(sfDevices, sfPhysicalNics,
					hostId);
			Map<String, Adapter> adapterMap = new HashMap<>();
			for (HostPciDevice pciDevice : sfDevices) {
				String id = SfVimServiceHelper.getAdapterId(hostId, pciDevice.getId());
				List<VMNIC> vmNICs = vmNICMap.get(id);
				if (vmNICs != null) {
					Adapter adapter = adapterMap.get(id);
					if (adapter == null) {
						adapter = new Adapter();
					}
					adapter.setName(pciDevice.getDeviceName());
					adapter.setId(id);
					adapter.setDeviceId(Short.toString(pciDevice.getDeviceId()));
					adapter.setSubSystemDeviceId(Short.toString(pciDevice.getSubDeviceId()));
					adapter.setVendorId(Short.toString(pciDevice.getVendorId()));
					adapter.setSubSystemVendorId(Short.toString(pciDevice.getSubVendorId()));
					// adapter.setDriverName(sfPhysicalNics.get(0).getDriver());
					adapter.setChildren(vmNICs);

					adapterMap.put(id, adapter);
				}
			}
			// Set adapter name including device name and mac address
			for (Adapter adapter : adapterMap.values()) {
				String minMacAddress = SfVimServiceHelper.getMinMacAddress(adapter.getChildren());
				// VSPPLUG-154 - Using "Part Number" Field for adapter name
				String deviceId = adapter.getChildren().get(0).getName();
				String partNumber = getPartNumber(hostId, deviceId);
				String adapterName = partNumber + "-" + minMacAddress;
				adapter.setName(adapterName);
				adapters.add(adapter);
			}
		} catch (Exception e) {
			logger.error("Solarflare:: Error getting adapters : " + e.getMessage());
			throw e;
		}
		timer.stop();
		logger.trace("Solarflare:: returning adapters for hostId : " + hostId + " Total adapters: " + adapters.size());
		return adapters;
	}

	private String getPartNumber(String hostId, String deviceId) {
		CIMHost cimHost;
		String partNumber = null;
		try {
			cimHost = getCIMHost(hostId);
			SfCIMService cimService = new SfCIMService(new SfCIMClientService(cimHost));
			partNumber = cimService.getPartNumber(deviceId);
		} catch (Exception e) {
			logger.error("Solarflare:: Error gettinng part number for hostId - " + hostId + " deviceId - " + deviceId);
			e.printStackTrace();
		}

		return partNumber;
	}

	@Override
	public CIMHost getCIMHost(String hostId) throws Exception {
		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: Get - getCIMHost");
		CIMHost cimHost = null;

		cimHost = cimHostCache.get(hostId);
		/*
		 * if(cimHost!=null){ logger.info(LOG_KEY +
		 * "Returning CIM Host object from cache for host : " + hostId); return
		 * cimHost; }
		 */

		logger.info(LOG_KEY + "Getting CIM ticket object for host : " + hostId);
		Connection _conn = getSession();
		HostServiceTicket ticket = _conn.getVimPort()
				.acquireCimServicesTicket(getManagedObjectReference("HostSystem", hostId));
		String url = CIM_SCHEME + ticket.getHost() + ":" + ticket.getPort() + "/";
		timer.stop();
		cimHost = new CIMHostSession(url, ticket.getSessionId());
		// Add cimHost to cache
		cimHostCache.put(hostId, cimHost);
		return cimHost;
	}

	private ManagedObjectReference getManagedObjectReference(String type, String value) {
		ManagedObjectReference managedObjectReference = new ManagedObjectReference();
		managedObjectReference.setType(type);
		managedObjectReference.setValue(value);
		return managedObjectReference;
	}

	@Override
	public void sessionEnded(String clientId) {
		try {
			connection.disconnect(clientId);
		} catch (Throwable t) {
			logger.error(LOG_KEY + "Error in session ending ", t);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// here session info may be null.
		if (userSessionService == null) {
			logger.error(LOG_KEY + "afterPropertiesSet: userSessionService= Null");
			return;
		}
		logger.info(LOG_KEY + "afterPropertiesSet: UserSession= " + userSessionService.getUserSession());
		if (userSessionService.getUserSession() != null) {
			logger.info(LOG_KEY + "afterPropertiesSet: clientId= " + userSessionService.getUserSession().clientId);
			logger.info(LOG_KEY + "afterPropertiesSet: userName= " + userSessionService.getUserSession().userName);
			ServerInfo[] servers = userSessionService.getUserSession().serversInfo;
			if (servers != null) {
				logger.info(LOG_KEY + "afterPropertiesSet: servers length = " + servers.length);
				logger.info(LOG_KEY + "afterPropertiesSet: server name = " + servers[0].name);
				logger.info(LOG_KEY + "afterPropertiesSet: server serviceGuid = " + servers[0].serviceGuid);
				logger.info(LOG_KEY + "afterPropertiesSet: server serviceUrl = " + servers[0].serviceUrl);
				logger.info(LOG_KEY + "afterPropertiesSet: server sessionCookie = " + servers[0].sessionCookie);
				logger.info(LOG_KEY + "afterPropertiesSet: server sessionKey = " + servers[0].sessionKey);
			} else {
				logger.error(LOG_KEY + "afterPropertiesSet: servers= Null");
			}

		} else {
			logger.error(LOG_KEY + "afterPropertiesSet: UserSession= null");
		}

		try {
			// Initialize the connection as it is time taking process.
			connection.connect(userSessionService, true);
		} catch (Exception e) {
			logger.error(LOG_KEY, e);
		}
	}

	public ManagedObjectReference getHostKernelModuleSystem(String hostId) throws Exception {
		logger.info("getHostKernelModuleSystem hostId : " + hostId);
		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: getHostKernelModuleSystem");
		List<String> props = new ArrayList<>();
		props.add("configManager.kernelModuleSystem");

		Connection _conn = getSession();

		GetMOREF _moRefService = new GetMOREF(_conn.getVimPort(), _conn.getServiceContent());

		ManagedObjectReference hostMoRef = getManagedObjectReference("HostSystem", hostId);

		Map<String, Object> hostprops = _moRefService.entityProps(hostMoRef, props.toArray(new String[] {}));

		ManagedObjectReference kernelModuleSystem = (ManagedObjectReference) hostprops
				.get("configManager.kernelModuleSystem");
		timer.stop();
		return kernelModuleSystem;
	}

	/**
	 * Returns Options string value for given host id
	 */
	public String getOptionString(String hostId) throws Exception {
		logger.info("getOptionString for hostId : " + hostId);
		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: getOptionString");
		Connection _conn = getSession();
		ManagedObjectReference kernelModuleSystem = getHostKernelModuleSystem(hostId);
		String optionString = _conn.getVimPort().queryConfiguredModuleOptionString(kernelModuleSystem,
				SOLARFLARE_MODULE_NAME);
		timer.stop();
		return optionString;
	}

	/**
	 * Updates the option string for given host id
	 */
	public void updateOptionString(String hostId, String value) throws Exception {
		logger.info("updateOptionString for hostId : " + hostId + " optionString : " + value);
		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: updateOptionString");
		Connection _conn = getSession();
		ManagedObjectReference kernelModuleSystem = getHostKernelModuleSystem(hostId);
		_conn.getVimPort().updateModuleOptionString(kernelModuleSystem, SOLARFLARE_MODULE_NAME, value);
		timer.stop();
	}
	
	@Override
	public Map<String,PerfCounterInfo> getNicStatPerfCounters(ManagedObjectReference perfManager, List<String> nicStatPerfCounter)
			throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {

		logger.info("Solarflare:: getNicStatPerfCounters");
		Map<String,PerfCounterInfo> nicStatPerfIdMap = new HashMap<String, PerfCounterInfo>();
		Connection _conn = getSession();

		GetMOREF _moRefService = new GetMOREF(_conn.getVimPort(), _conn.getServiceContent());
		Object property = _moRefService.entityProps(perfManager, new String[] { "perfCounter" }).get("perfCounter");
		ArrayOfPerfCounterInfo arrayCounter = (ArrayOfPerfCounterInfo) property;
		List<PerfCounterInfo> counters = arrayCounter.getPerfCounterInfo();
		
		for (PerfCounterInfo counter : counters) {
			// Only look for network related counters
			if ("net".equalsIgnoreCase(counter.getGroupInfo().getKey())){
				String counterGroup = counter.getGroupInfo().getKey();
				String counterName = counter.getNameInfo().getKey();
				String counterRollupType = counter.getRollupType().toString();
				String fullCounterName = counterGroup + "." + counterName + "." + counterRollupType;
				
				if(nicStatPerfCounter.contains(fullCounterName)){
					nicStatPerfIdMap.put(fullCounterName,counter);
				}
			}
		}
		return nicStatPerfIdMap;
	}

	@Override
	public List<PerfEntityMetricBase> retriveStats(ManagedObjectReference perfManager, Map<String,PerfCounterInfo> nicStatPerfIdMap, String hostId, String nicId) throws RuntimeFaultFaultMsg,Exception{
		
		logger.info("Solarflare:: retriveStats for hostId: " + hostId + " and nicId: "+nicId);
		/*
		 * Create the list of PerfMetricIds, one for each counter.
		 */
		List<PerfMetricId> perfMetricIds = new ArrayList<PerfMetricId>();
		
		/* Get the ID for this counter. */
		for (String perfConter : nicStatPerfIdMap.keySet()) {
			/*
			 * Create the PerfMetricId object for the counterName. Use an asterisk
			 * to select all metrics associated with counterId (instances and
			 * rollup).
			 */
			PerfMetricId metricId = new PerfMetricId();
			metricId.setCounterId(nicStatPerfIdMap.get(perfConter).getKey());
			metricId.setInstance(nicId);
			perfMetricIds.add(metricId);
		}
		ManagedObjectReference mor = getManagedObjectReference("HostSystem", hostId);
		/*
		 * Create the query specification for queryPerf(). Specify 5 minute
		 * rollup interval and CSV output format.
		 */
		int intervalId = 20; // for real time data
		PerfQuerySpec querySpecification = new PerfQuerySpec();
		querySpecification.setEntity(mor);
		querySpecification.setIntervalId(intervalId);
		querySpecification.setFormat("csv");
		querySpecification.getMetricId().addAll(perfMetricIds);

		List<PerfQuerySpec> pqsList = new ArrayList<PerfQuerySpec>();
		pqsList.add(querySpecification);

		/*
		 * Call queryPerf()
		 *
		 * QueryPerf() returns the statistics specified by the provided
		 * PerfQuerySpec objects. When specified statistics are unavailable -
		 * for example, when the counter doesn't exist on the target
		 * ManagedEntity - QueryPerf() returns null for that counter.
		 */
		Connection _conn = getSession();
		List<PerfEntityMetricBase> retrievedStats = _conn.getVimPort().queryPerf(perfManager, pqsList);

		return retrievedStats;
				
	}
	
	@Override
	public  Map<Integer,Integer> processNicStats(List<PerfEntityMetricBase> retrievedStats, Map<String,PerfCounterInfo> nicStatPerfIdMap) throws Exception{
		
		logger.info("Solarflare:: processNicStats");
		Map<Integer,Integer> nicStatPerfValue = new HashMap<>();
		/*
		 * Cycle through the PerfEntityMetricBase objects. Each object contains
		 * a set of statistics for a single ManagedEntity.
		 */
		for (PerfEntityMetricBase singleEntityPerfStats : retrievedStats) {

			/*
			 * Cast the base type (PerfEntityMetricBase) to the csv-specific
			 * sub-class.
			 */
			PerfEntityMetricCSV entityStatsCsv = (PerfEntityMetricCSV) singleEntityPerfStats;

			/* Retrieve the list of sampled values. */
			List<PerfMetricSeriesCSV> metricsValues = entityStatsCsv.getValue();

			if (metricsValues.isEmpty()) {
				System.out.println("No stats retrieved. " + "Check whether the NIC is properly configured.");
				throw new Exception();
			}

			/*
			 * Cycle through the PerfMetricSeriesCSV objects. Each object
			 * contains statistics for a single counter on the ManagedEntity.
			 */
			for (PerfMetricSeriesCSV csv : metricsValues) {
	
				String[] values = csv.getValue().split(",");
				int val = getSum(values);
				nicStatPerfValue.put(csv.getId().getCounterId(),val);
			}
		}

		return nicStatPerfValue;
	}
	
	int getSum(String[] values){
		int sum = 0;
		for(String strVal : values){
			int value = Integer.parseInt(strVal);
			sum += value;
		}
		return sum;
	}	
}
