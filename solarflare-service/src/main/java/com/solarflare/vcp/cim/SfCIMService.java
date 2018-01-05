package com.solarflare.vcp.cim;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.cim.CIMArgument;
import javax.cim.CIMDataType;
import javax.cim.CIMInstance;
import javax.cim.CIMObjectPath;
import javax.wbem.CloseableIterator;
import javax.wbem.WBEMException;
import javax.wbem.client.WBEMClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sblim.cimclient.internal.util.MOF;

import com.solarflare.vcp.helper.MetadataHelper;
import com.solarflare.vcp.model.FileHeader;
import com.solarflare.vcp.model.FwType;
import com.solarflare.vcp.model.SfFirmware;
import com.solarflare.vcp.vim.SimpleTimeCounter;
import com.vmware.vim25.Extension;
import com.vmware.vim25.ExtensionClientInfo;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.VimPortType;

public class SfCIMService {

	private static final Log logger = LogFactory.getLog(SfCIMService.class);

	SfCIMClientService cimClientService;

	public SfCIMService(SfCIMClientService cimClientService) {
		this.cimClientService = cimClientService;
	}

	// TODO Cleanup : Written for testing
	public static void main(String[] args) throws WBEMException {
		String url = "https://10.101.10.3:5989/";
		String password = "Ibmx#3750c";
		String user = "root";

		CIMHost cimHost = new CIMHostUser(url, user, password);

		SfCIMClientService cimClientService = new SfCIMClientService(cimHost);

		SfCIMService cimService = new SfCIMService(cimClientService);
		// cimService.setCIMClient(cimClient);
		// cimService.setCimHost(cimHost);

		System.out.println(cimService.getAdapterVersions("vmnic6"));
		System.out.println(cimService.getNICCardInstance("vmnic6"));
	}

	
	public WBEMClient getCIMClient() {
		return this.cimClientService.getCimClient();
	}

	
	/**
	 * 
	 * @param namespace
	 * @param classname
	 * @return All the instances of classname
	 * @throws WBEMException
	 */
	public Collection<CIMInstance> getAllInstances(final String namespace, final String classname)
			throws WBEMException {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: getAllInstances");
		WBEMClient client = getCIMClient();
		CIMObjectPath objectPath = this.cimClientService.getHostObjectPath(null, classname);

		final List<CIMInstance> results = new LinkedList<CIMInstance>();
		CloseableIterator<CIMInstance> enumeration = client.enumerateInstances(objectPath, true, true, true, null);
		while (enumeration.hasNext()) {
			results.add(enumeration.next());
		}
		timer.stop();
		return results;
	}

	/**
	 * 
	 * @return Instance of Controller Software Installation Service
	 * @throws WBEMException
	 */
	public CIMInstance getFirmwareSoftwareInstallationInstance() throws WBEMException {
		Collection<CIMInstance> instances = getAllInstances(CIMConstants.CIM_NAMESPACE,
				CIMConstants.SF_SOFTWARE_INSTALLATION_SERVICE);
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: getFirmwareSoftwareInstallationInstance");
		logger.info("Getting Firmware Software Installation Instance");
		CIMInstance svc_mcfw_inst = null;

		for (CIMInstance inst : instances) {

			if (inst.getProperty("Name").getValue().equals(CIMConstants.SVC_MCFW_NAME)) {
				svc_mcfw_inst = inst;
			}
		}
		timer.stop();
		return svc_mcfw_inst;
	}

	/**
	 * 
	 * @return Instance of BootROM Software Installation Service
	 * @throws WBEMException
	 */
	public CIMInstance getBootROMSoftwareInstallationInstance() throws WBEMException {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: getBootROMSoftwareInstallationInstance");
		logger.info("Getting BootROM Software Installation Instance");
		Collection<CIMInstance> instances = getAllInstances(CIMConstants.CIM_NAMESPACE,
				CIMConstants.SF_SOFTWARE_INSTALLATION_SERVICE);
		CIMInstance svc_bootrom_inst = null;

		for (CIMInstance inst : instances) {

			if (inst.getProperty("Name").getValue().equals(CIMConstants.SVC_BOOTROM_NAME)) {
				svc_bootrom_inst = inst;
			}
		}
		timer.stop();
		return svc_bootrom_inst;
	}

	/**
	 * 
	 * @param deviceId
	 * @return
	 * @throws WBEMException
	 */
	private CIMInstance getEthernatePortInstance(String deviceId) throws WBEMException {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: getEthernatePortInstance");
		CIMInstance ethernateInstance = null;
		String cimClass = "SF_EthernetPort";

		// Get SF_EthernetPort instance
		Collection<CIMInstance> instances = getAllInstances(CIMConstants.CIM_NAMESPACE, cimClass);
		for (CIMInstance inst : instances) {
			String devId = (String) inst.getProperty("DeviceID").getValue();
			// String macAddress = (String)
			// inst.getProperty("PermanentAddress").getValue();
			if (devId != null && devId.equals(deviceId)) {
				ethernateInstance = inst;
			}
		}
		timer.stop();
		return ethernateInstance;
	}

	@SuppressWarnings("unchecked")
	private CloseableIterator<CIMInstance> getAssociators(CIMObjectPath objectPath, String associationClass,
			String resultClass, String role) throws WBEMException {

		
		return getCIMClient().associators(objectPath, associationClass, resultClass, role, null, true, true, null);

	}

	/**
	 * 
	 * @param deviceId
	 * @return NIC Card instance for given deviceId
	 * @throws WBEMException
	 */
	public CIMInstance getNICCardInstance(String deviceId) throws WBEMException {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: getNICCardInstance");
		// Get EthernatePort Instance
		CIMInstance ethernateInstance = getEthernatePortInstance(deviceId);

		// Get SF_ControlledBy instance through association
		CloseableIterator<CIMInstance> inst = getAssociators(ethernateInstance.getObjectPath(), "SF_ControlledBy", null,
				"Dependent");
		CIMInstance controlledByInstance = inst.next();

		// Get SF_ControllerSoftwareIdentity instance through association
		inst = getAssociators(controlledByInstance.getObjectPath(), "SF_ControllerSoftwareIdentity", null, "Dependent");
		// CIMInstance controllerSoftwareIdentityInstance = inst.next();

		// Get SF_CardRealizesController and NICCard instance through
		// association
		inst = getAssociators(controlledByInstance.getObjectPath(), "SF_CardRealizesController", "SF_NICCard",
				"Dependent");
		CIMInstance nicCardIntance = inst.next();
		timer.stop();
		return nicCardIntance;
	}

	/**
	 * 
	 * @param deviceId
	 * @return Adapter Versions for given deviceId
	 * @throws WBEMException
	 */
	public Map<String, String> getAdapterVersions(String deviceId) throws WBEMException {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: getAdapterVersions");
		logger.info("Getting Adapter Versions for Device Id : " + deviceId);
		Map<String, String> versions = new HashMap<String, String>();
		// Get EthernatePort Instance
		CIMInstance ethernateInstance = getEthernatePortInstance(deviceId);

		// Get SF_ControlledBy instance through association
		CloseableIterator<CIMInstance> inst = getAssociators(ethernateInstance.getObjectPath(),
				CIMConstants.SF_CONTROLLED_BY, null, "Dependent");
		CIMInstance controlledByInstance = inst.next();

		// Get SF_ControllerSoftwareIdentity instance through association
		inst = getAssociators(controlledByInstance.getObjectPath(), CIMConstants.SF_CONTROLLER_SOFTWARE_IDENTITY, null,
				"Dependent");

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
		timer.stop();
		return versions;
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

	/**
	 * 
	 * @param serviceContent
	 * @param vimPort
	 * @param cimHost
	 * @param fwInstance
	 * @param nicInstance
	 * @return
	 * @throws MalformedURLException
	 * @throws RuntimeFaultFaultMsg
	 * @throws URISyntaxException
	 * @throws WBEMException
	 */
	public String getLatestControllerFWImageVersion(ServiceContent serviceContent, VimPortType vimPort, SfCIMService cimService,
			CIMInstance fwInstance, CIMInstance nicInstance)
			throws MalformedURLException, RuntimeFaultFaultMsg, URISyntaxException, WBEMException {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: getLatestControllerFWImageVersion");
		String versionString = "0.0.0.0";

		MetadataHelper metadataHelper = new MetadataHelper();
		URL pluginURL = new URL(getPluginURL(serviceContent, vimPort, CIMConstants.PLUGIN_KEY));
		SfFirmware file = metadataHelper.getMetaDataForAdapter(pluginURL, cimService, fwInstance, nicInstance, FwType.CONTROLLER);
		if (file != null) {
			versionString = file.getVersionString();
		}
		timer.stop();
		return versionString;
	}

	/**
	 * 
	 * @param serviceContent
	 * @param vimPort
	 * @param cimHost
	 * @param bootROMInstance
	 * @param nicInstance
	 * @return
	 * @throws MalformedURLException
	 * @throws RuntimeFaultFaultMsg
	 * @throws URISyntaxException
	 * @throws WBEMException
	 */
	public synchronized String getLatestBootROMFWImageVersion(ServiceContent serviceContent, VimPortType vimPort,
			SfCIMService cimService, CIMInstance bootROMInstance, CIMInstance nicInstance)
			throws MalformedURLException, RuntimeFaultFaultMsg, URISyntaxException, WBEMException {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: getLatestBootROMFWImageVersion");
		String versionString = "0.0.0.0";
		MetadataHelper metadataHelper = new MetadataHelper();
		URL pluginURL = new URL(getPluginURL(serviceContent, vimPort, CIMConstants.PLUGIN_KEY));
		SfFirmware file = metadataHelper.getMetaDataForAdapter(pluginURL, cimService, bootROMInstance, nicInstance, FwType.BOOTROM);
		if (file != null) {
			versionString = file.getVersionString();
		}
		timer.stop();
		return versionString;
	}

	/**
	 * 
	 * @param filePath
	 * @return Get version from file header
	 * @throws URISyntaxException
	 */
	public String getVersionFromBinaryFile(URL filePath) throws URISyntaxException {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: getVersionFromBinaryFile");
		byte[] bytes = null;
		String versionString = "";

		boolean readComplete = false;
		bytes = readData(filePath, readComplete);
		if (bytes == null) {
			versionString = CIMConstants.DEFAULT_VERSION;
		} else {
			getFileHeader(bytes);
		}
		timer.stop();
		return versionString;
	}

	public byte[] readData(URL toDownload, boolean readComplete) {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: readData");
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
		timer.stop();
		return outputStream.toByteArray();
	}

	/**
	 * 
	 * @param serviceContent
	 * @param vimPort
	 * @param pluginKey
	 * @return Get the URL where plugin is installed
	 * @throws RuntimeFaultFaultMsg
	 * @throws MalformedURLException
	 */
	public String getPluginURL(ServiceContent serviceContent, VimPortType vimPort, String pluginKey)
			throws RuntimeFaultFaultMsg, MalformedURLException {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: getPluginURL");
		String urlPath = "";
		ManagedObjectReference extensionManager = serviceContent.getExtensionManager();
		Extension ext = vimPort.findExtension(extensionManager, pluginKey);
		List<ExtensionClientInfo> extClientInfo = ext.getClient();
		for (ExtensionClientInfo clientInfo : extClientInfo) {
			urlPath = clientInfo.getUrl();
		}
		timer.stop();
		return urlPath;

	}

	/**
	 * 
	 * @param fwInst
	 * @param nicInstance
	 * @param header
	 * @return True is given Firmware image file is compatible with nicInstance
	 * @throws Exception
	 */
	public boolean isCustomFWImageCompatible(CIMInstance fwInst, CIMInstance nicInstance, FileHeader header)
			throws Exception {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: isCustomFWImageCompatible");
		boolean isCompatible = false;

		Map<String, String> params = getRequiredFwImageName(fwInst, nicInstance);
		int currentType = Integer.parseInt(params.get(CIMConstants.TYPE));
		int currentSubType = Integer.parseInt(params.get(CIMConstants.SUB_TYPE));
		logger.info("Current firmware type : " + currentType);
		logger.info("Current firmware subtype : " + currentSubType);

		// FileHeader header = getFileHeader(bytes);
		logger.info("Headers: " + header);
		int newType = header.getType();
		int newSubType = header.getSubtype();
		logger.info("New Type:" + newType);
		logger.info("New Subtype:" + newSubType);

		if (currentType == newType && currentSubType == newSubType) {
			isCompatible = true;
			logger.info("Custom Firmeware Image compatable");
		} else {
			logger.info("Custom Firmeware Image not compatable");
		}
		timer.stop();
		return isCompatible;
	}

	/**
	 * 
	 * @param fw_inst
	 * @param nicInstance
	 * @return
	 */
	public Map<String, String> getRequiredFwImageName(CIMInstance fw_inst, CIMInstance nicInstance) {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: getRequiredFwImageName");
		logger.info("Getting Required Firmware Image Version");
		Map<String, String> params = new HashMap<>();

		try {
			WBEMClient client = getCIMClient();

			CIMArgument<CIMObjectPath> cimTarget = new CIMArgument<CIMObjectPath>(CIMConstants.TARGET,
					new CIMDataType(nicInstance.getClassName()),
					new CIMObjectPath(MOF.objectHandle(nicInstance.getObjectPath(), false, true)));

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
						logger.info("getRequiredFwImageName:=> " + cimArgumentsOut[i].getName() + " = "
								+ cimArgumentsOut[i].getValue().toString());
					}
				}
			} else {
				String errMsg = getLatestLogErrorMessage();
				logger.error(errMsg);
				throw new Exception(errMsg);
			}

		} catch (Exception e) {
			logger.error("Failed to get required Firmware Image Name for given NIC instance! " + e.getMessage());
		}
		timer.stop();
		return params;
	}

	public FileHeader getFileHeader(byte[] bytes) {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: getFileHeader");
		FileHeader header = new FileHeader();

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
		timer.stop();
		return header;
	}

	private synchronized String getLatestLogErrorMessage() {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: getLatestLogErrorMessage");
		String errMsg = null;
		CIMInstance logErrorInstance = null;
		try {
			CIMInstance pLogInstance = getProviderLogInstance();

			CloseableIterator<CIMInstance> inst = getAssociators(pLogInstance.getObjectPath(),
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
		timer.stop();
		return errMsg;
	}

	private CIMInstance getProviderLogInstance() throws WBEMException {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: getProviderLogInstance");
		CIMInstance plInstance = null;
		String cimClass = CIMConstants.SF_PROVIDER_LOG;
		// Get SF_EthernetPort instance
		Collection<CIMInstance> instances = getAllInstances(CIMConstants.CIM_NAMESPACE, cimClass);
		for (CIMInstance inst : instances) {
			String desc = (String) inst.getProperty("Description").getValue();
			if (desc != null && desc.equals("Error log")) {
				plInstance = inst;
			}
		}
		timer.stop();
		return plInstance;
	}

	/**
	 * Updates the firmware for given NIC card with URL specified
	 * 
	 * @param objectPath
	 * @param nic
	 * @param fwImagePath
	 * @return true if Firmware is updated successfully
	 * @throws Exception
	 */
	public boolean updateFirmwareFromURL(CIMInstance softwareIntsallationInstance, CIMInstance nic, URL fwImagePath)
			throws Exception {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: updateFirmwareFromURL");
		WBEMClient client = getCIMClient();
		String pMethodName = CIMConstants.INSTALL_FROM_URI;

		logger.debug("Updating nic object: " + nic.getObjectPath());
		CIMArgument<CIMObjectPath> target = new CIMArgument<CIMObjectPath>(CIMConstants.TARGET,
				new CIMDataType(nic.getClassName()),
				new CIMObjectPath(MOF.objectHandle(nic.getObjectPath(), false, true)));
		CIMArgument<String> uri = new CIMArgument<String>(CIMConstants.URI, CIMDataType.STRING_T,
				fwImagePath.toString());

		Integer[] options = new Integer[] { 3 };
		CIMArgument<?> installOptions = new CIMArgument<Integer[]>(CIMConstants.INSTALL_OPTIONS,
				CIMDataType.UINT16_ARRAY_T, options);

		String[] optionValues = new String[] { "x" };
		// TODO will use SFBase64 to encode and generate checksum
		CIMArgument<?> installOptionsValues = new CIMArgument<String[]>(CIMConstants.INSTALL_OPTIONS_VALUES,
				CIMDataType.STRING_ARRAY_T, optionValues);

		CIMArgument<?>[] pInputArguments = { target, uri, installOptions, installOptionsValues };
		CIMArgument<?>[] pOutputArguments = new CIMArgument<?>[10];

		try {
			Object status = client.invokeMethod(softwareIntsallationInstance.getObjectPath(), pMethodName,
					pInputArguments, pOutputArguments);
			logger.debug("status: " + status);

			int statusCode = Integer.parseInt(status.toString());
			if (statusCode == 0) {
				logger.info("'InstallFromURI' method invoked successfully!");
				return true;

			} else {
				String errMsg = getLatestLogErrorMessage();
				logger.error(errMsg);
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
		timer.stop();
		return false;
	}

	/**
	 * 
	 * @param fw_inst
	 * @param data
	 * @param filePath
	 */
	public void sendFWImageData(CIMInstance fw_inst, String data, String filePath) {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: sendFWImageData");
		logger.info("Send Firmware Image Send");

		try {
			WBEMClient client = getCIMClient();
			CIMArgument<?> cimFilePath = new CIMArgument<String>(CIMConstants.FILE_NAME, CIMDataType.STRING_T,
					filePath);
			CIMArgument<?> cimFileData = new CIMArgument<String>(CIMConstants.BASE64STR, CIMDataType.STRING_T, data);
			CIMArgument<?>[] cimArguments = { cimFilePath, cimFileData };
			CIMArgument<?>[] cimArgumentsOut = new CIMArgument<?>[5]; // output
																		// parameters

			Object status = client.invokeMethod(fw_inst.getObjectPath(), CIMConstants.SEND_FW_IMAGE_DATA, cimArguments,
					cimArgumentsOut);
			int statusCode = Integer.parseInt(status.toString());
			logger.info("Status Code : " + statusCode);
			if (statusCode == 0) {
				logger.info("'SendFwImageData' method invoked successfully!");
			} else {
				String errMsg = getLatestLogErrorMessage();
				throw new Exception(errMsg);
			}
		} catch (Exception e) {
			logger.error("Failed to Send Firmware Image Data! " + e.getMessage());
		}
		timer.stop();
	}

	/**
	 * 
	 * @param fw_inst
	 * @return temp file name created at Host
	 */
	public String startFwImageSend(CIMInstance fw_inst) {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: startFwImageSend");
		String filePath = null;
		logger.info("Start Firmware Image Send");

		try {
			WBEMClient client = getCIMClient();
			CIMArgument<?>[] cimArgumentsOut = new CIMArgument<?>[5]; // output
																		// parameters

			Object status = client.invokeMethod(fw_inst.getObjectPath(), CIMConstants.START_FW_IMAGE_SEND, null,
					cimArgumentsOut);
			int statusCode = Integer.parseInt(status.toString());
			if (statusCode == 0) {

				logger.info("'StartFwImageSend' method invoked successfully!");

				if (cimArgumentsOut[0] != null) {
					filePath = cimArgumentsOut[0].getValue().toString();
				}
			} else {
				String errMsg = getLatestLogErrorMessage();
				throw new Exception(errMsg);
			}

		} catch (Exception e) {
			logger.error("Failed to Start Firmware Image Send! " + e.getMessage());
		}
		timer.stop();
		return filePath;
	}

	/**
	 * Removes the Temp file created at host after update is done.
	 * 
	 * @param fw_inst
	 * @param tempFilePath
	 * @return
	 */
	public boolean removeFwImage(CIMInstance fw_inst, String tempFilePath) {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: removeFwImage");
		boolean isRemoved = false;
		logger.info("Remove FW Image file");

		try {
			WBEMClient client = getCIMClient();
			CIMArgument<?> cimFilePath = new CIMArgument<String>(CIMConstants.FILE_NAME, CIMDataType.STRING_T,
					tempFilePath);
			CIMArgument<?>[] cimArguments = { cimFilePath };
			CIMArgument<?>[] cimArgumentsOut = new CIMArgument<?>[5]; // output
																		// parameters

			Object status = client.invokeMethod(fw_inst.getObjectPath(), CIMConstants.REMOVE_FW_IMAGE, cimArguments,
					cimArgumentsOut);
			int statusCode = Integer.parseInt(status.toString());
			if (statusCode == 0) {
				isRemoved = true;
				logger.info("'RemoveFwImage' method invoked successfully!");

			} else {
				String errMsg = getLatestLogErrorMessage();
				throw new Exception(errMsg);
			}

		} catch (Exception e) {
			logger.error("Failed to Remove Firmware Image! " + e.getMessage());
		}
		timer.stop();
		return isRemoved;
	}

	/**
	 * 
	 * @param fw_inst
	 * @param nicInstance
	 * @param filePath
	 */
	public void getLocalFwImageVersion(CIMInstance fw_inst, CIMInstance nicInstance, String filePath) {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: getLocalFwImageVersion");
		logger.info("Getting Local Firmware Image Version");

		try {
			WBEMClient client = getCIMClient();

			// Create type for nicInstance and set input parameter
			CIMArgument<CIMObjectPath> cimTarget = new CIMArgument<CIMObjectPath>(CIMConstants.TARGET,
					new CIMDataType(nicInstance.getClassName()),
					new CIMObjectPath(MOF.objectHandle(nicInstance.getObjectPath(), false, true)));
			CIMArgument<?> cimFilePath = new CIMArgument<String>(CIMConstants.FILE_NAME, CIMDataType.STRING_T,
					filePath);
			CIMArgument<?>[] cimArguments = { cimTarget, cimFilePath }; // input
																		// parameters
			CIMArgument<?>[] cimArgumentsOut = new CIMArgument<?>[5]; // output
																		// parameters

			Object status = client.invokeMethod(fw_inst.getObjectPath(), CIMConstants.GET_LOCAL_FW_IMAGE_VERSION,
					cimArguments, cimArgumentsOut);
			int statusCode = Integer.parseInt(status.toString());
			if (statusCode == 0) {

				logger.info("'GetLocalFwImageVersion' method invoked successfully!");

				for (int i = 0; i < cimArgumentsOut.length; i++) {
					if (cimArgumentsOut[i] != null) {
						System.out.println(
								cimArgumentsOut[i].getName() + " : " + cimArgumentsOut[i].getValue().toString());
					}
				}
			} else {
				String errMsg = getLatestLogErrorMessage();
				throw new Exception(errMsg);
			}

		} catch (Exception e) {
			logger.error("Failed to get required Firmware Image Name for given NIC instance! " + e.getMessage());
		}
		timer.stop();
	}

}
