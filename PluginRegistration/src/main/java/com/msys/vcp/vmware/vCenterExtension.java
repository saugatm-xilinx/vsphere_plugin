package com.msys.vcp.vmware;

import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.msys.vcp.model.ActionResponse;
import com.msys.vcp.model.ConnectionDAO;
import com.msys.vcp.model.ExtensionData;
import com.msys.vcp.utils.ExtensionDataHelper;
import com.vmware.vim25.Description;
import com.vmware.vim25.Extension;
import com.vmware.vim25.ExtensionClientInfo;
import com.vmware.vim25.ExtensionResourceInfo;
import com.vmware.vim25.ExtensionServerInfo;
import com.vmware.vim25.KeyValue;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.ServiceContent;

@Service
public class vCenterExtension implements ExtensionService {
	@Autowired
	private ConnectionMgr connectionService;

	public vCenterExtension() {
	}

	@Override
	public ActionResponse isPluginRegistered(ConnectionDAO conn, String key) throws Exception {
		ManagedObjectReference _extensionManager = getExtensionManager(conn);
		// check if plugin registered
		Boolean status = isPluginRegistered(key, _extensionManager);
		return new ActionResponse(status, "Status of plugin. True if plugin registered otherwise false.)");
	}

	private boolean isPluginRegistered(String key, ManagedObjectReference _extensionManager) {
		try {
			return null != connectionService.vimPort().findExtension(_extensionManager, key);
		} catch (Exception e) {
			//plugin not found exception
			return false;
		}

	}

	@Override
	public ActionResponse registerPlugin(ConnectionDAO conn, ExtensionData data) throws Exception {
		ManagedObjectReference _extensionManager = getExtensionManager(conn);

		if (!vCenter())
			throw new Exception("Given host is not a vCenter Server. You can register plugin on vCenter server only.");

		if (data == null) {
			// read default data from ExtensionDataHelper
			data = ExtensionDataHelper.getDefaultData();
		}
		// check if plugin registration
		if (isPluginRegistered(data.getKey(), _extensionManager))
			throw new Exception("Plugin with key: " + data.getKey() + " is already registered!");

		// if not registered, register it
		String msg = registerPlugin(data, _extensionManager);
		disconnect();
		return new ActionResponse(true, msg);

	}

	@Override
	public ActionResponse unRegisterPlugin(ConnectionDAO conn, String key) throws Exception {
		ManagedObjectReference _extensionManager = getExtensionManager(conn);
		if (!vCenter())
			throw new Exception(
					"Given host is not a vCenter Server. You can unregister plugin from vCenter server only.");
		if (key == null)
			throw new Exception("To unregister a plugin you must provide the key for the plugin.");

		if (!isPluginRegistered(key, _extensionManager))
			throw new Exception("Plugin with key: " + key + " is not registered!");

		ActionResponse response = unRegisterPlugin(key, _extensionManager);
		disconnect();
		return response;
	}

	private void disconnect() {
		try {
			connectionService.disconnect();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	@Override
	public ActionResponse updatePlugin(ConnectionDAO conn, String key, ExtensionData data) {
		return new ActionResponse(false, "Update plugin not implemented yet!");
	}

	private ManagedObjectReference getExtensionManager(ConnectionDAO conn) throws Exception {
		ServiceContent _serviceContent = connectionService.getServiceContent(conn);
		return _serviceContent.getExtensionManager();
	}

	/**
	 * Unregisters a single extension based on the set member fields.
	 * 
	 * @throws Exception
	 */
	private synchronized ActionResponse unRegisterPlugin(String key, ManagedObjectReference _extensionManager)
			throws Exception {
		connectionService.vimPort().unregisterExtension(_extensionManager, key);

		return new ActionResponse(true, "Plugin: " + key + " has been successfully un-registered.");
	}

	private synchronized String registerPlugin(ExtensionData data, ManagedObjectReference _extensionManager)
			throws Exception {
		try {
			Extension extension = createExtensionObject(data);
			connectionService.vimPort().registerExtension(_extensionManager, extension);
			return "Plugin: " + data.getKey() + " has been successfully registered in vCenter.";
		} catch (RuntimeFaultFaultMsg e) {
			throw new Exception("Exception while registering plugin " + data.getKey() + ". Error: " + e.getMessage());
		} catch (DatatypeConfigurationException e) {
			throw new Exception("Exception while registering plugin " + data.getKey() + ". Error: " + e.getMessage());
		}

	}

	private Extension createExtensionObject(ExtensionData data) throws Exception {
		Extension extension = new Extension();

		Description description = new Description();
		description.setLabel(data.getName());
		description.setSummary(data.getSummary());

		extension.setKey(data.getKey());
		extension.setVersion(data.getVersion());
		extension.setCompany(data.getCompany());
		extension.setDescription(description);

		ExtensionClientInfo extClientInfo = new ExtensionClientInfo();
		extClientInfo.setVersion(data.getVersion());
		extClientInfo.setCompany(data.getCompany());
		extClientInfo.setDescription(description);
		extClientInfo.setType(ExtensionData.getDefaultPluginType());
		extClientInfo.setUrl(data.getPluginUrl());
		extension.getClient().add(extClientInfo);

		ExtensionResourceInfo extResourceInfo_en = new ExtensionResourceInfo();
		extResourceInfo_en.setLocale("en_US");
		extResourceInfo_en.setModule("name");
		KeyValue kv1 = new KeyValue();
		kv1.setKey("name");
		kv1.setValue(data.getName());
		extResourceInfo_en.getData().add(kv1);
		extension.getResourceList().add(extResourceInfo_en);

		if (data.getPluginUrl().startsWith(ExtensionData.getHttpsProtocol().toLowerCase())) {
			// HTTPS requests require server info
			if (data.getServerThumbprint() == null) {
				throw new Exception(
						"Missing required serverThumbprint info when -pluginUrl is https");
			}
			ExtensionServerInfo extServerInfo = new ExtensionServerInfo();
			extServerInfo.getAdminEmail().add(data.getEmail());
			extServerInfo.setCompany(data.getCompany());
			extServerInfo.setDescription(description);
			extServerInfo.setType(ExtensionData.getHttpsProtocol());
			extServerInfo.setServerThumbprint(data.getServerThumbprint());
			extServerInfo.setUrl(data.getPluginUrl());
			extension.getServer().add(extServerInfo);
		} else {
			System.out.println(
					"INFO: Not using https for your plugin URL is OK for testing but not recommended for production."
							+ "\nUsers will have to include the flag allowHttp=true in their vSphere Client webclient.properties otherwise the http URL will be ignored");
		}
		extension.setShownInSolutionManager(data.isShowInSolutionManager());
		GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

		DatatypeFactory dtFactory = DatatypeFactory.newInstance();
		XMLGregorianCalendar xmlCalendar = dtFactory.newXMLGregorianCalendar(cal);
		extension.setLastHeartbeatTime(xmlCalendar);
		return extension;
	}

	public boolean vCenter() {
		final String type = connectionService.getApiType();
		if (type.equals("VirtualCenter"))
			return true;
		//Disconnect session. It allows to create a new connection with VC.
		disconnect();
		return false;
	}

}
