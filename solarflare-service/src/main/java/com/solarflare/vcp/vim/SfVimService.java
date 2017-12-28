
package com.solarflare.vcp.vim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.solarflare.vcp.cim.CIMHost;
import com.solarflare.vcp.cim.CIMHostSession;
import com.solarflare.vcp.model.Adapter;
import com.solarflare.vcp.model.Host;
import com.solarflare.vcp.vim.connection.Connection;
import com.solarflare.vcp.vim.connection.ConnectionImpl;
import com.solarflare.vcp.vim.helpers.GetMOREF;
import com.solarflare.vcp.vim.helpers.SfVimServiceHelper;
import com.vmware.vim25.ArrayOfHostPciDevice;
import com.vmware.vim25.ArrayOfPhysicalNic;
import com.vmware.vim25.HostPciDevice;
import com.vmware.vim25.HostServiceTicket;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.PhysicalNic;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.SoftwarePackage;
import com.vmware.vise.security.ClientSessionEndListener;
import com.vmware.vise.usersession.ServerInfo;
import com.vmware.vise.usersession.UserSessionService;

public class SfVimService implements InitializingBean, ClientSessionEndListener {
	private static final Log logger = LogFactory.getLog(SfVimService.class);
	private static final String LOG_KEY = "Solarflare:: ";
	private static final String CIM_SCHEME = "https://";
	@Autowired
	private Connection connection;
	@Autowired
	private UserSessionService userSessionService;

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

	public static void main(String[] args) throws Exception {
		ConnectionImpl connection = new ConnectionImpl("https://10.101.10.7/sdk", "msys@vsphere.local", "Msys@123", true);
		connection._login();
		System.out.println(connection.isConnected());
		SfVimService vimsevice = new SfVimService();
		vimsevice.setConnection(connection);
		ManagedObjectReference h = new ManagedObjectReference();
		h.setType("HostSystem");
		h.setValue("host-9");
		vimsevice.getAllHosts();
	}

	/**
	 * Get a connection that is connected to server.
	 * 
	 * @return
	 */
	private Connection getSession() {
		// Implementation of Junit test connection can be done here.
		return connection.connect(userSessionService, true);
	}

	/**
	 * Returns host summary for given host MoRef. Summary includes CIM Provider
	 * and CIM driver versions, number of adapters and number of ports.
	 * 
	 * @throws Exception
	 */
	public Host getHostSummary(String hostId) throws Exception {
		Host host = new Host();
		SimpleTimeCounter timer = new SimpleTimeCounter("getHostSummary");
		List<String> props = new ArrayList<>();
		props.add("configManager.imageConfigManager");
		props.add("hardware.pciDevice");
		props.add("config.network.pnic");
		props.add("name");

		Connection _conn = getSession();

		GetMOREF _moRefService = new GetMOREF(_conn.getVimPort(), _conn.getServiceContent());

		ManagedObjectReference hostMoRef = getManagedObjectReference("HostSystem", hostId);

		Map<String, Object> hostprops = _moRefService.entityProps(hostMoRef, props.toArray(new String[]{}));

		// Get name
		String name = (String) hostprops.get("name");
		host.setName(name);
		host.setId(hostId);

		// Get PCI devices (key for this is hardware.pciDevice and return
		// type is ArrayOfHostPciDevice)
		ArrayOfHostPciDevice arrayOfHostPciDevice = (ArrayOfHostPciDevice) hostprops.get("hardware.pciDevice");
		List<HostPciDevice> sfDevices = SfVimServiceHelper.getSfPciDevice(arrayOfHostPciDevice.getHostPciDevice());

		int count = SfVimServiceHelper.getAdapterCount(sfDevices);
		host.setAdapterCount(count);

		// Get Physical Nics
		ArrayOfPhysicalNic arrayOfPhysicalNic = (ArrayOfPhysicalNic) hostprops.get("config.network.pnic");
		List<PhysicalNic> sfPhysicalNics = SfVimServiceHelper.getSfPhysicalNic(arrayOfPhysicalNic.getPhysicalNic());
		host.setPortCount(sfPhysicalNics.size());

		// Get Version of CIM provider and Driver
		ManagedObjectReference imageConfigManager = (ManagedObjectReference) hostprops.get("configManager.imageConfigManager");

		List<SoftwarePackage> softwarePackage = _conn.getVimPort().fetchSoftwarePackages(imageConfigManager);
		host.setCimProviderVersion(SfVimServiceHelper.getCimProviderVersion(softwarePackage));
		host.setDriverVersion(SfVimServiceHelper.getDriverVersion(softwarePackage));

		timer.stop();
		logger.debug(LOG_KEY + "getHostSummary() returned: " + host);
		return host;
	}

	/**
	 * Returns all hosts of vCenter.
	 */
	public List<Host> getAllHosts() {
		List<Host> hostList = new ArrayList<>();

		List<String> props = new ArrayList<>();
		props.add("hardware.pciDevice");
		props.add("config.network.pnic");
		props.add("name");

		Connection _conn = getSession();

		GetMOREF _moRefService = new GetMOREF(_conn.getVimPort(), _conn.getServiceContent());

		try {
			Map<ManagedObjectReference, Map<String, Object>> hosts = _moRefService.inContainerByType(_conn.getServiceContent().getRootFolder(), "HostSystem", props.toArray(new String[]{}));

			for (ManagedObjectReference hostMoRef : hosts.keySet()) {
				Map<String, Object> hostprops = hosts.get(hostMoRef);
				Host host = new Host();
				host.setId(hostMoRef.getValue());
				// Get name
				String name = (String) hostprops.get("name");
				host.setName(name);

				ArrayOfHostPciDevice arrayOfHostPciDevice = (ArrayOfHostPciDevice) hostprops.get("hardware.pciDevice");
				List<HostPciDevice> sfDevices = SfVimServiceHelper.getSfPciDevice(arrayOfHostPciDevice.getHostPciDevice());

				// Get Physical Nics
				ArrayOfPhysicalNic arrayOfPhysicalNic = (ArrayOfPhysicalNic) hostprops.get("config.network.pnic");
				List<PhysicalNic> sfPhysicalNics = SfVimServiceHelper.getSfPhysicalNic(arrayOfPhysicalNic.getPhysicalNic());
				List<Adapter> adapters = getAdapters(sfDevices, sfPhysicalNics, false);
				host.setChildren(adapters);
			}

		} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg e) {
			// TODO Auto-generated catch block
		}
		return hostList;

	}

	private List<Adapter> getAdapters(List<HostPciDevice> sfDevices, List<PhysicalNic> sfPhysicalNics, boolean fullResponse) {
		List<Adapter> adapters = new ArrayList<>();
		// TODO: implement here
		Adapter adapter = new Adapter();

		adapter.setName("");
		adapter.setId("");
		return adapters;
	}

	public CIMHost getCIMHost(String hostId) throws Exception {
		logger.info(LOG_KEY + "Getting CIM ticket object for host : " + hostId);
		Connection _conn = getSession();
		HostServiceTicket ticket = _conn.getVimPort().acquireCimServicesTicket(getManagedObjectReference("HostSystem", hostId));
		String url = CIM_SCHEME + ticket.getHost() + ":" + ticket.getPort() + "/";
		return new CIMHostSession(url, ticket.getSessionId());
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
			// init. the connection as it is time taking process.
			connection.connect(userSessionService, true);
		} catch (Exception e) {
			logger.error(LOG_KEY, e);
		}
	}

}
