package com.solarflare.vcp.services;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.cim.CIMInstance;
import javax.cim.CIMObjectPath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sblim.cimclient.internal.util.MOF;

import com.solarflare.vcp.cim.CIMConstants;
import com.solarflare.vcp.cim.CIMHost;
import com.solarflare.vcp.cim.SfCIMClientService;
import com.solarflare.vcp.cim.SfCIMService;
import com.solarflare.vcp.exception.SfInvalidRequestException;
import com.solarflare.vcp.helper.FtpUrlProcessor;
import com.solarflare.vcp.helper.MetadataHelper;
import com.solarflare.vcp.helper.SFBase64;
import com.solarflare.vcp.model.Adapter;
import com.solarflare.vcp.model.AdapterOverview;
import com.solarflare.vcp.model.AdapterTask;
import com.solarflare.vcp.model.FileHeader;
import com.solarflare.vcp.model.FirmwareType;
import com.solarflare.vcp.model.FirmwareVersion;
import com.solarflare.vcp.model.FwType;
import com.solarflare.vcp.model.Host;
import com.solarflare.vcp.model.HostConfiguration;
import com.solarflare.vcp.model.NicBootParamInfo;
import com.solarflare.vcp.model.SfFirmware;
import com.solarflare.vcp.model.SfOptionString;
import com.solarflare.vcp.model.Status;
import com.solarflare.vcp.model.TaskInfo;
import com.solarflare.vcp.model.TaskState;
import com.solarflare.vcp.model.UpdateRequest;
import com.solarflare.vcp.security.ASN1Parser;
import com.solarflare.vcp.vim.SfVimService;
import com.solarflare.vcp.vim.SimpleTimeCounter;

public class HostAdapterServiceImpl implements HostAdapterService {
	private static final Log logger = LogFactory.getLog(HostAdapterServiceImpl.class);

	private SfVimService sfVimService;
	private String SFC9220_deviceID = "2563";
	private int threadPoolSize;

	public int getThreadPoolSize() {
		return threadPoolSize;
	}

	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

	public HostAdapterServiceImpl(SfVimService sfVimService) {
		logger.info("Solarflare:: HostAdapterServiceImpl created.");
		this.sfVimService = sfVimService;
	}

	public void init() {
		logger.info("init method called ");
		UpdateRequestProcessor updateRequestProcessor = UpdateRequestProcessor.getInstance();
		updateRequestProcessor.init(threadPoolSize);
	}

	public void shutdown() {
		logger.info("shutdown method called ");
		UpdateRequestProcessor updateRequestProcessor = UpdateRequestProcessor.getInstance();
		updateRequestProcessor.shutdown();
	}

	@Override
	public List<Host> getHostList() throws Exception {
		List<Host> hostList = null;
		try {
			hostList = sfVimService.getAllHosts();
		} catch (Exception e) {
			throw e;
		}
		return hostList;
	}

	@Override
	public Host getHostById(String hostId) throws Exception {
		Host host = null;
		try {
			if (hostId != null && !hostId.isEmpty()) {
				host = sfVimService.getHostSummary(hostId);
			}
		} catch (Exception e) {
			throw e;
		}
		return host;
	}

	@Override
	public String updateFirmwareToLatest(List<Adapter> adapterList, String hostId) throws Exception {

		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: Update - updateFirmwareToLatest");
		String taskID = null;
		logger.info("Solarflare:: updateFirmwareToLatest");
		try {
			TaskInfo taskInfo = createTask(hostId);
			taskID = taskInfo.getTaskid();

			// TODO : Review Comment : Try Caching cimHost to check performance
			SfCIMService cimService = getCIMService(hostId);

			UpdateRequestProcessor requestProcessor = UpdateRequestProcessor.getInstance();

			for (Adapter adapter : adapterList) {
				if (isValidated(adapter, taskInfo)) {

					// update controller
					logger.debug("Updating Controller of adapter " + adapter.getName());
					URL fWImageURL = null; // for latest update this is null
					CIMInstance fwInstance = cimService.getSoftwareInstallationInstance(CIMConstants.SVC_MCFW_NAME);
					UpdateRequest updateRequest = createUpdateRequest(adapter, cimService, taskInfo, FwType.CONTROLLER,
							fWImageURL, fwInstance);
					requestProcessor.addUpdateRequest(updateRequest);

					logger.debug("Updating BootROM of adapter " + adapter.getName());
					fWImageURL = null; // for latest update this is null
					fwInstance = cimService.getSoftwareInstallationInstance(CIMConstants.SVC_BOOTROM_NAME);
					updateRequest = createUpdateRequest(adapter, cimService, taskInfo, FwType.BOOTROM, fWImageURL,
							fwInstance);
					requestProcessor.addUpdateRequest(updateRequest);

					logger.debug("Updating UEFI ROM of adapter " + adapter.getName());
					fWImageURL = null; // for latest update this is null
					fwInstance = cimService.getSoftwareInstallationInstance(CIMConstants.SVC_UEFI_NAME);
					updateRequest = createUpdateRequest(adapter, cimService, taskInfo, FwType.UEFIROM, fWImageURL,
							fwInstance);
					requestProcessor.addUpdateRequest(updateRequest);

				}
			}
		} catch (Exception e) {
			timer.stop();
			throw e;
		}
		timer.stop();
		return taskID;
	}

	@Override
	public String customUpdateFirmwareFromLocal(List<Adapter> adapterList, String hostId, String base64Data)
			throws Exception {
		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: Update - customUpdateFirmwareFromLocal");
		logger.info("Solarflare:: customUpdateFirmwareFromLocal");
		String taskID = null;
		try {

			TaskInfo taskInfo = createTask(hostId);
			taskID = taskInfo.getTaskid();

			URL fwImageURL = null;

			SfCIMService cimService = getCIMService(hostId);

			byte[] dataBytes = base64Data.getBytes();
			// Decode this data using java's decoder
			Base64.Decoder decoder = Base64.getDecoder();
			byte[] decodedDataBytes = decoder.decode(dataBytes);

			// Get Header info from data
			byte[] headerData = new ASN1Parser().getFileHeaderBytes(decodedDataBytes);
			FileHeader header = cimService.getFileHeader(headerData);
			logger.debug("Solarflare:: Header : " + header);

			boolean controller = false;
			boolean bootrom = false;
			boolean uefirom = false;
			CIMInstance fwInstance = null;
			if (FirmwareType.FIRMWARE_TYPE_MCFW.ordinal() == header.getType()) {
				logger.debug("Solarflare:: Updating Controller");
				fwInstance = cimService.getSoftwareInstallationInstance(CIMConstants.SVC_MCFW_NAME);
				controller = true;
			} else if (FirmwareType.FIRMWARE_TYPE_BOOTROM.ordinal() == header.getType()) {
				// Get BootROM SF_SoftwareInstallationService instance
				logger.debug("Solarflare:: Updating BootROM");
				fwInstance = cimService.getSoftwareInstallationInstance(CIMConstants.SVC_BOOTROM_NAME);
				bootrom = true;
			} else if (FirmwareType.FIRMWARE_TYPE_UEFIROM.ordinal() == header.getType()) {
				// Get UEFI ROM SF_SoftwareInstallationService instance
				logger.debug("Solarflare:: Updating UEFI ROM");
				fwInstance = cimService.getSoftwareInstallationInstance(CIMConstants.SVC_UEFI_NAME);
				uefirom = true;
			}

			boolean isValid = isFwFileValid(adapterList, header, fwInstance, cimService);
			if (isValid) {
				if (fwInstance != null) {

					String tempFile = cimService.startFwImageSend(fwInstance);

					sendDataInChunks(cimService, fwInstance, tempFile, decodedDataBytes);

					fwImageURL = new URL("file:/" + tempFile);

					UpdateRequestProcessor requestProcessor = UpdateRequestProcessor.getInstance();
					UpdateRequest updateRequest = null;
					if (controller) {
						updateRequest = createUpdateRequestForCustom(cimService, taskInfo, FwType.CONTROLLER,
								fwImageURL);
					}
					if (bootrom) {
						updateRequest = createUpdateRequestForCustom(cimService, taskInfo, FwType.BOOTROM, fwImageURL);
					}
					if (uefirom) {
						updateRequest = createUpdateRequestForCustom(cimService, taskInfo, FwType.UEFIROM, fwImageURL);
					}

					updateRequest.setCustom(true); // temp file is created
					updateRequest.setTempFilePath(tempFile);
					requestProcessor.addUpdateRequest(updateRequest, adapterList);

				} else {
					String errMsg = "Fail to get CIM Firmware Instance";
					logger.error(errMsg);
					throw new SfInvalidRequestException(errMsg);
				}

			}

		} catch (Exception e) {
			timer.stop();
			throw e;
		}
		timer.stop();
		return taskID;
	}

	@Override
	public String customUpdateFirmwareFromURL(List<Adapter> adapterList, String hostId, String fwImagePath)
			throws Exception {
		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: Update - customUpdateFirmwareFromURL");
		logger.info("Solarflare:: customUpdateFirmwareFromURL");
		String taskID = null;
		try {
			//encode special characters in user name and password for ftp and sftp url
			if(fwImagePath != null && !fwImagePath.isEmpty()){
				if(fwImagePath.indexOf("ftp") == 0 || 
				   fwImagePath.indexOf("FTP") == 0 ||
				   fwImagePath.indexOf("sftp") == 0 ||
				   fwImagePath.indexOf("SFTP") == 0){
					fwImagePath = FtpUrlProcessor.getEncodedURL(fwImagePath);
				}
			}
			URL fwImageURL = new URL(fwImagePath);
			SfCIMService cimService = getCIMService(hostId);
			boolean readComplete = true;
			byte[] fileData = cimService.readData(fwImageURL, readComplete);
			Base64.Encoder encoder = Base64.getEncoder();
			byte[] encodedData = encoder.encode(fileData);
			String encodedFileData = new String(encodedData);
			taskID = customUpdateFirmwareFromLocal(adapterList, hostId, encodedFileData);
		} catch (Exception e) {
			timer.stop();
			throw e;
		}
		timer.stop();
		return taskID;


	}

	@Override
	public List<Adapter> getHostAdapters(String hostId) throws Exception {

		List<Adapter> adapters = new ArrayList<>();
		if (hostId != null && !hostId.isEmpty()) {
			adapters = sfVimService.getHostAdapters(hostId);
			SfCIMService cimService = getCIMService(hostId);
			Map<String, CIMInstance> nics = cimService.getEthernatePortInstanceMap();
			
			for (Adapter adapter : adapters) {
				setFirmwareVersions(adapter, cimService, nics);
			}
		}
		return adapters;

	}

	private boolean isFwFileValid(List<Adapter> adapterList, FileHeader header, CIMInstance fwInstance,
			SfCIMService cimService) throws Exception {

		boolean isValid = true;
		for (Adapter adapter : adapterList) {
			CIMInstance nicInstance = cimService.getNICCardInstance(adapter.getChildren().get(0).getName());
			if (FirmwareType.FIRMWARE_TYPE_MCFW.ordinal() == header.getType()) {
				isValid = cimService.isCustomFWImageCompatible(fwInstance, nicInstance, header, FwType.CONTROLLER,
						adapter);
			} else if (FirmwareType.FIRMWARE_TYPE_BOOTROM.ordinal() == header.getType()) {
				isValid = cimService.isCustomFWImageCompatible(fwInstance, nicInstance, header, FwType.BOOTROM,
						adapter);
			} else if (FirmwareType.FIRMWARE_TYPE_UEFIROM.ordinal() == header.getType()) {
				isValid = cimService.isCustomFWImageCompatible(fwInstance, nicInstance, header, FwType.UEFIROM,
						adapter);
			} else {
				throw new SfInvalidRequestException(
						"Invalid firmware file. Not valid for either Controler, BootROM or UEFI ROM.");
			}
		}
		return isValid;
	}

	@Override
	public NicBootParamInfo getNicParamInfo(String hostId, String nicId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean validateTypeAndSubTupe(String file, boolean isLocal) throws Exception {
		return false;
	}

	@Override
	public HostConfiguration getHostConfigurations(String hostId) throws Exception {
		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: getHostConfigurations");
		logger.info("Solarflare:: getHostConfigurations for hostId : " + hostId);
		SfOptionString sfOptionString = new SfOptionString();
		String optionString = sfVimService.getOptionString(hostId);
		HostConfiguration hostConfiguration = sfOptionString.getHostConfiguration(optionString);
		timer.stop();
		return hostConfiguration;
	}

	@Override
	public void updateHostConfigurations(String hostId, HostConfiguration hostConfigurationRequest) throws Exception {
		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: updateHostConfigurations");
		logger.info("Solarflare:: updateHostConfigurations ");
		SfOptionString sfOptionString = new SfOptionString();
		sfOptionString = sfOptionString.getOptionString(hostConfigurationRequest);
		sfVimService.updateOptionString(hostId, sfOptionString.toString());
		timer.stop();
	}

	private TaskInfo createTask(String hostId) {
		TaskManager taskManager = TaskManager.getInstance();
		String taskID = taskManager.getTaskId();
		TaskInfo taskInfo = new TaskInfo();
		taskInfo.setTaskid(taskID);
		taskInfo.setHostId(hostId);
		taskManager.addTaskInfo(taskInfo);
		return taskInfo;
	}

	private SfCIMService getCIMService(String hostId) throws Exception {
		CIMHost cimHost = sfVimService.getCIMHost(hostId);
		SfCIMService cimService = new SfCIMService(new SfCIMClientService(cimHost));
		// Call some CIM method for increasing session validity to 15 min
		cimService.getProperty();
		return cimService;
	}

	private UpdateRequest createUpdateRequest(Adapter adapter, SfCIMService cimService, TaskInfo taskInfo,
			FwType fwType, URL fwImageURL, CIMInstance fwInstance) throws Exception {
		String adapterId = adapter.getId();
		AdapterTask aTask = getAdapterTask(taskInfo, adapterId);

		CIMInstance nicInstance = cimService.getNICCardInstance(adapter.getChildren().get(0).getName());
		// Check for controller version
		Status status = new Status(TaskState.Queued, null, fwType);
		if (FwType.CONTROLLER.equals(fwType)) {
			aTask.setController(status);
		} else if (FwType.BOOTROM.equals(fwType)) {
			aTask.setBootROM(status);
		} else if (FwType.UEFIROM.equals(fwType)) {
			aTask.setUefiROM(status);
		}
		if (fwImageURL == null) {
			fwImageURL = getImageURL(fwInstance, nicInstance, cimService, fwType, adapter);
		}
		UpdateRequest updateRequest = new UpdateRequest();
		updateRequest.setAdapterId(adapterId);
		updateRequest.setFwInstance(fwInstance.getObjectPath());
		updateRequest.setNicInstance(new CIMObjectPath(MOF.objectHandle(nicInstance.getObjectPath(), false, true)));
		updateRequest.setFwImagePath(fwImageURL);
		updateRequest.setCimService(cimService);
		updateRequest.setTaskId(taskInfo.getTaskid());
		updateRequest.setFwType(fwType);

		return updateRequest;
	}

	private UpdateRequest createUpdateRequestForCustom(SfCIMService cimService, TaskInfo taskInfo, FwType fwType,
			URL fwImageURL) throws Exception {

		UpdateRequest updateRequest = new UpdateRequest();
		updateRequest.setFwImagePath(fwImageURL);
		updateRequest.setCimService(cimService);
		updateRequest.setTaskId(taskInfo.getTaskid());
		updateRequest.setFwType(fwType);

		return updateRequest;
	}

	private AdapterTask getAdapterTask(TaskInfo taskInfo, String adapterId) {
		List<AdapterTask> tasks = taskInfo.getAdapterTasks();
		// At this point, this can be null
		if (tasks != null && !tasks.isEmpty())
			for (AdapterTask aTask : tasks)
				if (aTask.getAdapterId().equals(adapterId)) {
					return aTask;
				}
		// task is not created yet, create a new task
		AdapterTask aTask = new AdapterTask();
		aTask.setAdapterId(adapterId);
		taskInfo.add(aTask);
		return aTask;
	}

	private URL getImageURL(CIMInstance fw_inst, CIMInstance nicInstance, SfCIMService cimService, FwType fwType, Adapter adapter)
			throws Exception {

		URL pluginURL = new URL(sfVimService.getPluginURL(CIMConstants.PLUGIN_KEY));
		String filePath = null;
		SfFirmware file = MetadataHelper.getMetaDataForAdapter(pluginURL, cimService, fw_inst, nicInstance, fwType,adapter);
		if (file != null) {
			filePath = file.getPath();
		}
		URL fwImageURL = new URL("http", pluginURL.getHost(), filePath);

		return fwImageURL;
	}

	private void sendDataInChunks(SfCIMService cimService, CIMInstance fwInstance, String tempFile,
			byte[] decodedDataBytes) {
		// Send/write this data totemp file on host
		logger.info("Solarflare::Sending data in chunks");
		SFBase64 sfBase64 = new SFBase64();
		int chunkSize = 100000;
		int index;
		for (index = 0; index < decodedDataBytes.length; index += chunkSize) {
			if (index + chunkSize > decodedDataBytes.length) {
				chunkSize = decodedDataBytes.length - index;
			}
			byte[] temp = Arrays.copyOfRange(decodedDataBytes, index, index + chunkSize);
			int encodeSize = sfBase64.base64_enc_size(chunkSize);
			byte[] encoded = new byte[encodeSize];
			// using SFBase64 to encode data
			encoded = sfBase64.base64_encode(temp, chunkSize);
			cimService.sendFWImageData(fwInstance, new String(encoded), tempFile);
		}

		logger.info("Solarflare::Sending data in chunks is complete");
	}

	private void setFirmwareVersions(Adapter adapter, SfCIMService cimService, Map<String, CIMInstance> nics) throws Exception {
		logger.info("Solarflare:: setFirmwareVersions for adapter : " + adapter.getName());
		String deviceId = adapter.getChildren().get(0).getName();
		Map<String, String> versions = cimService.getAdapterVersions(deviceId,nics);

		String controllerVersion = versions.get(CIMConstants.CONTROLLER_VERSION);
		String bootROMVersion = versions.get(CIMConstants.BOOT_ROM_VERSION);
		String firmwareVersion = versions.get(CIMConstants.FIRMARE_VERSION);
		String UEFIROMVersion = versions.get(CIMConstants.UEFI_ROM_VERSION);
		adapter.setVersionController(controllerVersion);
		adapter.setVersionBootROM(bootROMVersion);
		adapter.setVersionFirmware(firmwareVersion);
		adapter.setVersionUEFIROM(UEFIROMVersion);

		// Get version from image binary for controller
		CIMInstance fwInstance = cimService.getSoftwareInstallationInstance(CIMConstants.SVC_MCFW_NAME);
		CIMInstance niCimInstance = cimService.getNICCardInstance(deviceId);
		URL pluginURL = new URL(sfVimService.getPluginURL(CIMConstants.PLUGIN_KEY));

		String latestControllerVersion = cimService.getLatestFWImageVersion(pluginURL, cimService, fwInstance,
				niCimInstance, FwType.CONTROLLER,adapter);

		FirmwareVersion frmVesion = new FirmwareVersion();

		frmVesion.setController(latestControllerVersion);

		// Get version from image binary for BootRom
		CIMInstance bootROMInstance = cimService.getSoftwareInstallationInstance(CIMConstants.SVC_BOOTROM_NAME);
		String latestBootROMVersion = cimService.getLatestFWImageVersion(pluginURL, cimService, bootROMInstance,
				niCimInstance, FwType.BOOTROM,adapter);

		frmVesion.setBootROM(latestBootROMVersion);

		// Get version from image binary for BootRom
		CIMInstance uefiROMInstance = cimService.getSoftwareInstallationInstance(CIMConstants.SVC_UEFI_NAME);
		String latestUefiROMVersion = cimService.getLatestFWImageVersion(pluginURL, cimService, uefiROMInstance,
				niCimInstance, FwType.UEFIROM,adapter);
		frmVesion.setUefi(latestUefiROMVersion);

		// Put dummy latest versions for Firmware family
		frmVesion.setFirmewareFamily(firmwareVersion); // setting current as
														// latest for now

		adapter.setLatestVersion(frmVesion);
	}

	private boolean isValidated(Adapter adapter, TaskInfo taskInfo) {
		// TODO : Need to refactor/cleanup
		if (SFC9220_deviceID.equals(adapter.getDeviceId())) {
			String adapterId = adapter.getId();
			AdapterTask aTask = getAdapterTask(taskInfo, adapterId);

			Status status = new Status(TaskState.Error, "Adapter Type Not Supported", FwType.CONTROLLER);
			// aTask.setController(status);

			status = new Status(TaskState.Error, "Adapter Type Not Supported", FwType.BOOTROM);
			// aTask.setBootROM(status);
			return true;
		}
		return true;
	}

	@Override
	public AdapterOverview getAdapterOverview(String hostId, String nicId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Adapter getAdapters(String hostId, String nicId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
