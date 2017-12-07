package com.solarflare.vcp.cim;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.cim.CIMArgument;
import javax.cim.CIMDataType;
import javax.cim.CIMInstance;
import javax.cim.CIMObjectPath;
import javax.cim.CIMProperty;
import javax.wbem.CloseableIterator;
import javax.wbem.WBEMException;
import javax.wbem.client.UserPrincipal;
import javax.wbem.client.WBEMClient;
import javax.wbem.client.WBEMClientFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sblim.cimclient.internal.util.MOF;

import com.solarflare.vcp.helper.MetadataHelper;
import com.solarflare.vcp.model.FileHeader;
import com.solarflare.vcp.model.SfFirmware;
import com.vmware.vim25.Extension;
import com.vmware.vim25.ExtensionClientInfo;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.VimPortType;

public class CIMService {

	private static final Log logger = LogFactory.getLog(CIMService.class);

	public Collection<CIMInstance> getAllInstances(CIMHost cimHost, final String namespace, final String classname)
			throws WBEMException {
		WBEMClient client = getClient(cimHost, classname);
		CIMObjectPath objectPath = getHostObjectPath(cimHost, null, classname);

		final List<CIMInstance> results = new LinkedList<CIMInstance>();
		CloseableIterator<CIMInstance> enumeration = client.enumerateInstances(objectPath, true, true, true, null);
		while (enumeration.hasNext()) {
			results.add(enumeration.next());
		}

		return results;
	}

	public WBEMClient getClient(CIMHost cimHost, String cimObjectName) {
		return getClient(cimHost, cimObjectName, null);
	}

	public WBEMClient getClient(CIMHost cimHost, String cimObjectName, CIMProperty<?>[] properties) {
		javax.security.auth.Subject subject = createSubjectFromHost(cimHost);

		try {
			WBEMClient client = WBEMClientFactory.getClient(CIMConstants.WBEMCLIENT_FORMAT);
			client.initialize(getHostObjectPath(cimHost, properties, cimObjectName), subject,
					Locale.getAvailableLocales());
			return client;
		} catch (WBEMException e) {
			logger.error("Failed to initialize WBEMClient!" + e.getMessage());
		}
		return null;
	}

	private CIMObjectPath getHostObjectPath(CIMHost cimHost, CIMProperty<?>[] properties, String cimObjectName) {
		try {
			URL UrlHost = new URL(cimHost.getUrl());
			return new CIMObjectPath(UrlHost.getProtocol(), UrlHost.getHost(), String.valueOf(UrlHost.getPort()),
					CIMConstants.CIM_NAMESPACE, cimObjectName, properties);
		} catch (MalformedURLException e) {
			logger.error("Invalid URL '" + cimHost.getUrl() + "'." + e.getMessage());
		}
		return null;
	}

	private javax.security.auth.Subject createSubjectFromHost(CIMHost cimHost) {
		String user = "";
		String password = "";
		javax.security.auth.Subject subject = new javax.security.auth.Subject();

		if (cimHost instanceof CIMHostUser) {
			user = ((CIMHostUser) cimHost).getUsername();
			password = ((CIMHostUser) cimHost).getPassword();
		} else if (cimHost instanceof CIMHostSession) {
			user = ((CIMHostSession) cimHost).getSessionId();
			password = user;
		}

		subject.getPrincipals().add(new UserPrincipal(user));
		subject.getPrivateCredentials().add(new PasswordCredential(password));

		return subject;
	}

	public CIMInstance getFirmwareSoftwareInstallationInstance(CIMHost cimHost) throws WBEMException {
		Collection<CIMInstance> instances = getAllInstances(cimHost, CIMConstants.CIM_NAMESPACE,
				CIMConstants.SF_SOFTWARE_INSTALLATION_SERVICE);
		logger.info("Getting Firmware Software Installation Instance");
		CIMInstance svc_mcfw_inst = null;

		for (CIMInstance inst : instances) {

			if (inst.getProperty("Name").getValue().equals(CIMConstants.SVC_MCFW_NAME)) {
				svc_mcfw_inst = inst;
			}
		}

		return svc_mcfw_inst;
	}

	public CIMInstance getBootROMSoftwareInstallationInstance(CIMHost cimHost) throws WBEMException {
		logger.info("Getting BootROM Software Installation Instance");
		Collection<CIMInstance> instances = getAllInstances(cimHost, CIMConstants.CIM_NAMESPACE,
				CIMConstants.SF_SOFTWARE_INSTALLATION_SERVICE);
		CIMInstance svc_bootrom_inst = null;

		for (CIMInstance inst : instances) {

			if (inst.getProperty("Name").getValue().equals(CIMConstants.SVC_BOOTROM_NAME)) {
				svc_bootrom_inst = inst;
			}
		}

		return svc_bootrom_inst;
	}

	private CIMInstance getEthernatePortInstance(CIMHost cimHost, String deviceId) throws WBEMException {
		CIMInstance ethernateInstance = null;
		CIMService cimUtil = new CIMService();
		String cimClass = "SF_EthernetPort";

		// Get SF_EthernetPort instance
		Collection<CIMInstance> instances = cimUtil.getAllInstances(cimHost, CIMConstants.CIM_NAMESPACE, cimClass);
		for (CIMInstance inst : instances) {
			String devId = (String) inst.getProperty("DeviceID").getValue();
			String macAddress = (String) inst.getProperty("PermanentAddress").getValue();
			if (devId != null && devId.equals(deviceId)) {
				ethernateInstance = inst;
			}
		}
		return ethernateInstance;
	}

	@SuppressWarnings("unchecked")
	private CloseableIterator<CIMInstance> getAssociators(WBEMClient client, CIMObjectPath objectPath,
			String associationClass, String resultClass, String role) throws WBEMException {

		return client.associators(objectPath, associationClass, resultClass, role, null, true, true, null);

	}

	public CIMInstance getNICCardInstance(CIMHost cimHost, String deviceId) throws WBEMException {
		String cimClass = "SF_SoftwareInstallationService";
		WBEMClient client = getClient(cimHost, cimClass);
		// Get EthernatePort Instance
		CIMInstance ethernateInstance = getEthernatePortInstance(cimHost, deviceId);

		// Get SF_ControlledBy instance through association
		CloseableIterator<CIMInstance> inst = getAssociators(client, ethernateInstance.getObjectPath(),
				"SF_ControlledBy", null, "Dependent");
		CIMInstance controlledByInstance = inst.next();

		// Get SF_ControllerSoftwareIdentity instance through association
		inst = getAssociators(client, controlledByInstance.getObjectPath(), "SF_ControllerSoftwareIdentity", null,
				"Dependent");
		CIMInstance controllerSoftwareIdentityInstance = inst.next();

		// Get SF_CardRealizesController and NICCard instance through
		// association
		inst = getAssociators(client, controlledByInstance.getObjectPath(), "SF_CardRealizesController", "SF_NICCard",
				"Dependent");
		CIMInstance nicCardIntance = inst.next();

		System.out.println(nicCardIntance);

		return nicCardIntance;
	}

	// public static void main(String[] args) throws WBEMException
	// {
	// String url = "https://10.101.10.3:5989/";
	// String password = "Ibmx#3750c";
	// String user = "root";
	//
	// CIMService cimUtil = new CIMService();
	// String cimClass = "SF_SoftwareInstallationService";
	//
	// CIMHost cimHost = new CIMHostUser(url, user, password);
	//
	// // Get SF_SoftwareInstallationService instance
	// Collection<CIMInstance> instances = cimUtil.getAllInstances(cimHost,
	// CIMConstants.CIM_NAMESPACE, cimClass);
	// for (CIMInstance inst : instances)
	// {
	// System.out.println(inst);
	// }
	//
	// // Get Firmware SF_SoftwareInstallationService instance
	// CIMInstance svc_mcfw_inst =
	// cimUtil.getFirmwareSoftwareInstallationInstance(instances);
	//
	// // Get BootROM SF_SoftwareInstallationService instance
	// CIMInstance svc_bootrom_inst =
	// cimUtil.getBootROMSoftwareInstallationInstance(instances);
	//
	// // Get EthernatePort Instance for 'vmnic6'
	// System.out.println(cimUtil.getEthernatePortInstance(cimHost, "vmnic6"));
	//
	// cimUtil.getNICCardInstance(cimHost, "vmnic6");
	// }

	public Map<String, String> getAdapterVersions(CIMHost cimHost, String deviceId) throws WBEMException {
		logger.info("Getting Adapter Versions for Device Id : " + deviceId);
		String cimClass = CIMConstants.SF_SOFTWARE_INSTALLATION_SERVICE;
		Map<String, String> versions = new HashMap<String, String>();
		WBEMClient client = getClient(cimHost, cimClass);
		// Get EthernatePort Instance
		CIMInstance ethernateInstance = getEthernatePortInstance(cimHost, deviceId);

		// Get SF_ControlledBy instance through association
		CloseableIterator<CIMInstance> inst = getAssociators(client, ethernateInstance.getObjectPath(),
				CIMConstants.SF_CONTROLLED_BY, null, "Dependent");
		CIMInstance controlledByInstance = inst.next();

		// Get SF_ControllerSoftwareIdentity instance through association
		inst = getAssociators(client, controlledByInstance.getObjectPath(),
				CIMConstants.SF_CONTROLLER_SOFTWARE_IDENTITY, null, "Dependent");

		while (inst.hasNext()) {
			CIMInstance instance = inst.next();
			String versionString = instance.getProperty("VersionString").getValue().toString();
			if (versionString == null) {
				versionString = "0.0.0.0";
			}
			String description = instance.getProperty("Description").getValue().toString();
			if (CIMConstants.DESC_CONTROLLER.equals(description)) {
				versions.put(CIMConstants.CONTROLLER_VERSION, versionString);
			} else if (CIMConstants.DESC_BOOT_ROM.equals(description)) {
				versions.put(CIMConstants.BOOT_ROM_VERSION, versionString);
			}

			// Adding dummy values for below
			versions.put(CIMConstants.FIRMARE_VERSION, "1.1.1.0");
			versions.put(CIMConstants.UEFI_ROM_VERSION, "1.1.1.0");
		}
		return versions;
	}

	/**
	 * The default PasswordCredential will prevent us from using sessionId's
	 * that can be over 16 characters in length. Instead use inheritance to
	 * force the PasswordCredential class to hold values longer than 16 chars.
	 * <p>
	 * 
	 * @see javax.wbem.client.PasswordCredential
	 */
	public static class PasswordCredential extends javax.wbem.client.PasswordCredential {
		private final String longPassword;

		public PasswordCredential(String userPassword) {
			super("fake password"); // the parent class' password is ignored
			longPassword = userPassword;
		}

		@Override
		public char[] getUserPassword() {
			return longPassword.toCharArray(); // use our long password instead
		}
	}

	/**
	 * builds a base URL to use for CIMObjectPath objects based on the host and
	 * connection objects already present in this object on initialization
	 *
	 * @return a URL to talk to the CIM server on
	 */
	public URL cimBaseUrl(String hostname) {
		URL url = null;
		try {
			url = new URL("https", hostname, CIMConstants.CIM_PORT, "/");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return check(url) ? url : null;
	}

	/**
	 * checks a URL to see if we can open a connection to it
	 *
	 * @param url
	 *            - to examine
	 * @return true if we can talk to the host
	 */
	public boolean check(final URL url) {
		boolean valid = false;

		try {
			url.openConnection();
			valid = true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return valid;
	}

	public String getLatestControllerFWImageVersion(ServiceContent serviceContent, VimPortType vimPort, CIMHost cimHost,
			CIMInstance fwInstance, CIMInstance nicInstance)
			throws MalformedURLException, RuntimeFaultFaultMsg, URISyntaxException, WBEMException {
		String versionString = "0.0.0.0";

		MetadataHelper metadataHelper = new MetadataHelper();
		boolean isController = true;
		SfFirmware file = metadataHelper.getMetaDataForAdapter(serviceContent, vimPort, cimHost, fwInstance,
				nicInstance, isController);
		if (file != null) {
			versionString = file.getVersionString();
		}

		// versionString = getVersionFromBinaryFile(metaDataFilePath);
		return versionString;
	}

	public String getLatestBootROMFWImageVersion(ServiceContent serviceContent, VimPortType vimPort, CIMHost cimHost,
			CIMInstance bootROMInstance, CIMInstance nicInstance)
			throws MalformedURLException, RuntimeFaultFaultMsg, URISyntaxException, WBEMException {
		String versionString = "0.0.0.0";
		MetadataHelper metadataHelper = new MetadataHelper();
		boolean isController = false; // This is for BootROM
		SfFirmware file = metadataHelper.getMetaDataForAdapter(serviceContent, vimPort, cimHost, bootROMInstance,
				nicInstance, isController);
		if (file != null) {
			versionString = file.getVersionString();
		}

		// versionString = getVersionFromBinaryFile(metaDataFilePath);
		return versionString;
	}

	public String getVersionFromBinaryFile(URL filePath) throws URISyntaxException {
		Path path = null;
		byte[] bytes = null;
		String versionString = "";

		boolean readComplete = false;
		bytes = readData(filePath, readComplete);
		if (bytes == null) {
			versionString = CIMConstants.DEFAULT_VERSION;
		} else {
			ByteBuffer buffer = ByteBuffer.wrap(bytes);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			int ih_magic = buffer.getInt();
			int ih_version = buffer.getInt();
			int ih_type = buffer.getInt();
			int ih_subtype = buffer.getInt();
			int ih_code_size = buffer.getInt();
			int ih_size = buffer.getInt();

			int ih_controller_version_min = buffer.getInt();

			int ih_controller_version_max = buffer.getInt();

			short ih_code_version_a = buffer.getShort();
			short ih_code_version_b = buffer.getShort();
			short ih_code_version_c = buffer.getShort();
			short ih_code_version_d = buffer.getShort();
			StringBuffer version = new StringBuffer();
			version.append(ih_code_version_a);
			version.append(".");
			version.append(ih_code_version_b);
			version.append(".");
			version.append(ih_code_version_c);
			version.append(".");
			version.append(ih_code_version_d);

			versionString = version.toString();
		}
		return versionString;
	}

	private byte[] readData(URL toDownload, boolean readComplete) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try {
			byte[] chunk = new byte[1000];
			int bytesRead;
			InputStream stream = toDownload.openStream();
			if (readComplete) {
				while ((bytesRead = stream.read(chunk)) > 0) {
					outputStream.write(chunk, 0, bytesRead);
				}
			} else {
				bytesRead = stream.read(chunk);
				outputStream.write(chunk, 0, bytesRead);
			}
		} catch (IOException e) {
			return null;
		}

		return outputStream.toByteArray();
	}

	public String getPluginURL(ServiceContent serviceContent, VimPortType vimPort, String pluginKey)
			throws RuntimeFaultFaultMsg, MalformedURLException {
		String urlPath = "";
		ManagedObjectReference extensionManager = serviceContent.getExtensionManager();
		Extension ext = vimPort.findExtension(extensionManager, pluginKey);
		List<ExtensionClientInfo> extClientInfo = ext.getClient();
		for (ExtensionClientInfo clientInfo : extClientInfo) {
			urlPath = clientInfo.getUrl();
		}

		return urlPath;

	}

	public boolean isCustomFWImageCompatible(ServiceContent serviceContent, VimPortType vimPort, CIMHost cimHost,
			CIMInstance fwInst, CIMInstance nicInstance, String deviceID, String path) throws Exception {
		boolean isCompatible = false;

		Map<String, String> params = getRequiredFwImageName(cimHost, fwInst, nicInstance);

		int currentType = Integer.parseInt(params.get(CIMConstants.TYPE));
		int currentSubType = Integer.parseInt(params.get(CIMConstants.SUB_TYPE));
		logger.debug("Current firmware type : " + currentType);
		logger.debug("Current firmware subtype : " + currentSubType);

		// Get Type and SubType of given firmware image;
		// TODO : get path from input param
		// String urlPath = getPluginURL(serviceContent, vimPort,
		// CIMConstants.PLUGIN_KEY);
		// URL pluginURL = new URL(urlPath);
		URL firmwareURL = new URL(path);

		// TODO : check crtificate for https
		// URL controllerFWImagePath = new URL("http", firmwareURL.getHost(),
		// firmwareURL.getFile());

		FileHeader header = getFileHeader(firmwareURL);
		int newType = header.getType();
		int newSubType = header.getSubtype();

		// TODO : add logs
		logger.debug("Type from firmware file :" + newType);
		logger.debug("Subtype from firmware file :" + newSubType);
		if (currentType == newType && currentSubType == newSubType) {
			isCompatible = true;
		}

		return isCompatible;
	}

	public Map<String, String> getRequiredFwImageName(CIMHost cimHost, CIMInstance fw_inst, CIMInstance nicInstance) {
		logger.info("Getting Required Firmware Image Version");
		Map<String, String> params = new HashMap<>();

		try {
			String cimClass = "SF_SoftwareInstallationService";
			WBEMClient client = getClient(cimHost, cimClass);

			// Create type for nicInstance and set input parameter
			CIMDataType instanceType = new CIMDataType(nicInstance.getClassName());
			CIMArgument<?> cimTarget = new CIMArgument<CIMInstance>(CIMConstants.TARGET, instanceType, nicInstance);

			CIMArgument<?>[] cimArguments = { cimTarget }; // input parameters
			CIMArgument<?>[] cimArgumentsOut = new CIMArgument<?>[5]; // output
																		// parameters

			Object status = client.invokeMethod(fw_inst.getObjectPath(), CIMConstants.GET_FW_IMAGE_NAME, cimArguments,
					cimArgumentsOut);
			int statusCode = Integer.parseInt(status.toString());
			if (statusCode == 0) {

				logger.info("'GetRequiredFwImageName' method invoked successfully!");

				for (int i = 0; i < cimArgumentsOut.length; i++) {
					if (cimArgumentsOut[i] != null) {
						params.put(cimArgumentsOut[i].getName(), cimArgumentsOut[i].getValue().toString());
					}
				}
			} else {
				String errMsg = getLatestLogErrorMessage(cimHost);
				throw new Exception(errMsg);
			}

		} catch (Exception e) {
			logger.error("Failed to get required Firmware Image Name for given NIC instance! " + e.getMessage());
		}

		return params;
	}

	private FileHeader getFileHeader(URL filePath) {
		FileHeader header = new FileHeader();
		Path path = null;
		byte[] bytes = null;

		boolean readComplete = false;
		bytes = readData(filePath, readComplete);
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		int ih_magic = buffer.getInt();
		header.setMagic(ih_magic);

		int ih_version = buffer.getInt();
		header.setVersion(ih_version);

		int ih_type = buffer.getInt();
		header.setType(ih_type);

		int ih_subtype = buffer.getInt();
		header.setSubtype(ih_subtype);

		int ih_code_size = buffer.getInt();
		header.setCodeSize(ih_code_size);

		int ih_size = buffer.getInt();
		header.setSize(ih_size);

		int ih_controller_version_min = buffer.getInt();
		header.setControllerVersionMin(ih_controller_version_min);

		int ih_controller_version_max = buffer.getInt();
		header.setControllerVersionMax(ih_controller_version_max);

		short ih_code_version_a = buffer.getShort();
		header.setCodeVersion_a(ih_code_version_a);

		short ih_code_version_b = buffer.getShort();
		header.setCodeVersion_b(ih_code_version_b);

		short ih_code_version_c = buffer.getShort();
		header.setCodeVersion_c(ih_code_version_c);

		short ih_code_version_d = buffer.getShort();
		header.setCodeVersion_d(ih_code_version_d);
		return header;
	}

	private String getLatestLogErrorMessage(CIMHost cimHost) {
		String errMsg = null;
		String cimClass = CIMConstants.SF_SOFTWARE_INSTALLATION_SERVICE;
		WBEMClient client = getClient(cimHost, cimClass);
		CIMInstance logErrorInstance = null;
		try {
			CIMInstance pLogInstance = getProviderLogInstance(cimHost);

			CloseableIterator<CIMInstance> inst = getAssociators(client, pLogInstance.getObjectPath(),
					CIMConstants.SF_LOG_MANAGES_RECORD, null, "Log");

			int maxRecordID = -1;
			while (inst.hasNext()) {
				CIMInstance tempInst = inst.next();

				int recordID = Integer.parseInt(tempInst.getPropertyValue("RecordID").toString(), 16); // record
																										// ID
																										// is
																										// in
																										// Hex
																										// format
				if (recordID > maxRecordID) {
					maxRecordID = recordID;
					logErrorInstance = tempInst;
				}
			}

			errMsg = logErrorInstance.getPropertyValue("RecordData").toString();

		} catch (WBEMException e) {
			e.printStackTrace();
		}
		return errMsg;
	}

	private CIMInstance getProviderLogInstance(CIMHost cimHost) throws WBEMException {
		CIMInstance plInstance = null;
		CIMService cimUtil = new CIMService();
		String cimClass = "SF_ProviderLog";
		// Get SF_EthernetPort instance
		Collection<CIMInstance> instances = cimUtil.getAllInstances(cimHost, CIMConstants.CIM_NAMESPACE, cimClass);
		for (CIMInstance inst : instances) {
			String desc = (String) inst.getProperty("Description").getValue();
			if (desc != null && desc.equals("Error log")) {
				plInstance = inst;
			}
		}
		return plInstance;
	}

	public boolean updateFirmwareFromURL(CIMObjectPath objectPath, CIMHost cimHost, CIMInstance nic, URL fwImagePath)
			throws Exception {

		String cimClass = CIMConstants.SF_SOFTWARE_INSTALLATION_SERVICE;
		WBEMClient client = getClient(cimHost, cimClass);
		String pMethodName = CIMConstants.INSTALL_FROM_URI;

		logger.debug("Updating nic object: " + nic.getObjectPath());
		CIMArgument<CIMObjectPath> target = new CIMArgument<CIMObjectPath>("Target",
				new CIMDataType(nic.getClassName()),
				new CIMObjectPath(MOF.objectHandle(nic.getObjectPath(), false, true)));
		CIMArgument<String> uri = new CIMArgument<String>(CIMConstants.URI, CIMDataType.STRING_T,
				fwImagePath.toString());

		Integer[] options = new Integer[] { 3 };
		CIMArgument<?> installOptions = new CIMArgument<Integer[]>(CIMConstants.INSTALL_OPTIONS,
				CIMDataType.UINT16_ARRAY_T, options);

		String[] optionValues = new String[] { "x" };
		CIMArgument<?> installOptionsValues = new CIMArgument<String[]>(CIMConstants.INSTALL_OPTIONS_VALUES,
				CIMDataType.STRING_ARRAY_T, optionValues);

		CIMArgument<?>[] pInputArguments = { target, uri, installOptions, installOptionsValues };
		CIMArgument<?>[] pOutputArguments = new CIMArgument<?>[10];

		try {
			Object status = client.invokeMethod(objectPath, pMethodName, pInputArguments, pOutputArguments);
			logger.debug("status: " + status);

			int statusCode = Integer.parseInt(status.toString());
			if (statusCode == 0) {
				logger.info("'InstallFromURI' method invoked successfully!");
				return true;

			} else {
				String errMsg = getLatestLogErrorMessage(cimHost);
				logger.error(errMsg);
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
		return false;
	}

}
