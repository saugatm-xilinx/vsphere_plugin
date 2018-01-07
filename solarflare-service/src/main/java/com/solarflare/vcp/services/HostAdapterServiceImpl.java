package com.solarflare.vcp.services;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.cim.CIMInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.solarflare.vcp.cim.CIMConstants;
import com.solarflare.vcp.cim.CIMHost;
import com.solarflare.vcp.cim.SfCIMClientService;
import com.solarflare.vcp.cim.SfCIMService;
import com.solarflare.vcp.helper.MetadataHelper;
import com.solarflare.vcp.helper.SFBase64;
import com.solarflare.vcp.helper.VCenterHelper;
import com.solarflare.vcp.model.Adapter;
import com.solarflare.vcp.model.AdapterTask;
import com.solarflare.vcp.model.FileHeader;
import com.solarflare.vcp.model.FirmwareType;
import com.solarflare.vcp.model.FirmwareVersion;
import com.solarflare.vcp.model.FwType;
import com.solarflare.vcp.model.Host;
import com.solarflare.vcp.model.HostConfiguration;
import com.solarflare.vcp.model.NicBootParamInfo;
import com.solarflare.vcp.model.SfFirmware;
import com.solarflare.vcp.model.Status;
import com.solarflare.vcp.model.TaskInfo;
import com.solarflare.vcp.model.TaskState;
import com.solarflare.vcp.model.UpdateRequest;
import com.solarflare.vcp.vim.SfVimService;

public class HostAdapterServiceImpl implements HostAdapterService {
	private static final Log logger = LogFactory.getLog(HostAdapterServiceImpl.class);

	private SfVimService sfVimService;

	public HostAdapterServiceImpl(SfVimService sfVimService) {
		logger.info("Solarflare:: HostAdapterServiceImpl created.");
		this.sfVimService = sfVimService;
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
			host = sfVimService.getHostSummary(hostId);
		} catch (Exception e) {
			throw e;
		}
		return host;

	}

	@Override
	public String updateFirmwareToLatest(List<Adapter> adapterList, String hostId) throws Exception {

		String taskID = null;
		logger.info("Start updating firmware adapter");
		try {
			// TODO :: validate all adapter before update
			TaskManager taskManager = TaskManager.getInstance();
			taskID = taskManager.getTaskId();
			TaskInfo taskInfo = new TaskInfo();
			taskInfo.setTaskid(taskID);
			taskInfo.setHostId(hostId);
			taskManager.addTaskInfo(taskInfo);
			CIMHost cimHost = sfVimService.getCIMHost(hostId);
			SfCIMService cimService = new SfCIMService(new SfCIMClientService(cimHost));

			// Call some CIM method for increasing session validity to 15 min
			cimService.getAllInstances(CIMConstants.CIM_NAMESPACE, CIMConstants.SF_SOFTWARE_INSTALLATION_SERVICE);

			UpdateRequestProcessor requestProcessor = UpdateRequestProcessor.getInstance();

			for (Adapter adapter : adapterList) {

				FirmwareVersion fwVersion = adapter.getLatestVersion();

				String currentVersion = adapter.getVersionController();
				String latestVersion = fwVersion.getController();
				String latest = VCenterHelper.getLatestVersion(currentVersion, latestVersion);
				if (latest.equals(latestVersion)) {
					// update controller
					URL fWImageURL = null; // for latest update this is null
					UpdateRequest updateRequest = createUpdateRequest(adapter, cimService, taskInfo, FwType.CONTROLLER, fWImageURL);
					requestProcessor.addUpdateRequest(updateRequest);
				}

				currentVersion = adapter.getVersionBootROM();
				latestVersion = fwVersion.getBootROM();
				latest = VCenterHelper.getLatestVersion(currentVersion, latestVersion);
				if (latest.equals(latestVersion)) {
					URL fWImageURL = null; // for latest update this is null
					UpdateRequest updateRequest = createUpdateRequest(adapter, cimService, taskInfo, FwType.BOOTROM, fWImageURL);
					requestProcessor.addUpdateRequest(updateRequest);
				}
			}

		} catch (Exception e) {
			throw e;
		}
		return taskID;
	}

	@Override
	public String customUpdateFirmwareFromLocal(List<Adapter> adapterList, String hostId, String base64Data) throws Exception {

		logger.info("Start updating firmware adapter");
		String taskID = null;
		try {

			TaskManager taskManager = TaskManager.getInstance();
			taskID = taskManager.getTaskId();
			TaskInfo taskInfo = new TaskInfo();
			taskInfo.setTaskid(taskID);
			taskInfo.setHostId(hostId);
			taskManager.addTaskInfo(taskInfo);
			URL fwImageURL = null;

			// CIMHost cimHost = new SfVimService().getCIMHost(hostId,
			// "TestingOnly");
			CIMHost cimHost = sfVimService.getCIMHost(hostId);
			SfCIMService cimService = new SfCIMService(new SfCIMClientService(cimHost));

			byte[] dataBytes = base64Data.getBytes();
			// Decode this data using java's decoder
			Base64.Decoder decoder = Base64.getDecoder();
			byte[] decodedDataBytes = decoder.decode(dataBytes);

			// Get Header info from data
			byte[] headerData = Arrays.copyOf(decodedDataBytes, 40); // header
																		// size
																		// is 40
																		// bytes
			FileHeader header = cimService.getFileHeader(headerData);

			boolean controller = false;
			boolean bootrom = false;
			CIMInstance fwInstance = null;
			if (FirmwareType.FIRMWARE_TYPE_MCFW.ordinal() == header.getType()) {
				logger.info("Updating Controller");
				fwInstance = cimService.getFirmwareSoftwareInstallationInstance();
				controller = true;
			} else if (FirmwareType.FIRMWARE_TYPE_BOOTROM.ordinal() == header.getType()) {
				// Get BootROM SF_SoftwareInstallationService instance
				logger.info("Updating BootROM");
				fwInstance = cimService.getBootROMSoftwareInstallationInstance();
				bootrom = true;
			}

			if (fwInstance != null) {
				String tempFile = cimService.startFwImageSend(fwInstance);

				sendDataInChunks(cimService, fwInstance, tempFile, decodedDataBytes);

				fwImageURL = new URL("file://" + tempFile);

				// Call some CIM method for increasing session validity to 15
				// min
				cimService.getAllInstances(CIMConstants.CIM_NAMESPACE, CIMConstants.SF_SOFTWARE_INSTALLATION_SERVICE);

				UpdateRequestProcessor requestProcessor = UpdateRequestProcessor.getInstance();
				for (Adapter adapter : adapterList) {
					UpdateRequest updateRequest = null;
					if (controller) {
						updateRequest = createUpdateRequest(adapter, cimService, taskInfo, FwType.CONTROLLER, fwImageURL);
					}
					if (bootrom) {
						updateRequest = createUpdateRequest(adapter, cimService, taskInfo, FwType.BOOTROM, fwImageURL);
					}

					updateRequest.setCustom(true); // temp file is created
					updateRequest.setTempFilePath(tempFile);
					requestProcessor.addUpdateRequest(updateRequest);
				}

			} else {
				logger.error("Fail to get CIM Firmware Instance");
			}

		} catch (Exception e) {
			throw e;
		}
		return taskID;

	}

	@Override
	public String customUpdateFirmwareFromURL(List<Adapter> adapterList, String hostId, String fwImagePath) throws Exception {

		logger.info("Start updating firmware adapter");
		String taskID = null;
		try {

			TaskManager taskManager = TaskManager.getInstance();
			taskID = taskManager.getTaskId();
			TaskInfo taskInfo = new TaskInfo();
			taskInfo.setTaskid(taskID);
			taskInfo.setHostId(hostId);
			taskManager.addTaskInfo(taskInfo);

			URL fwImageURL = new URL(fwImagePath);
			CIMHost cimHost = sfVimService.getCIMHost(hostId);
			SfCIMService cimService = new SfCIMService(new SfCIMClientService(cimHost));

			boolean controller = false;
			boolean bootrom = false;
			boolean readComplete = false;
			byte[] headerData = cimService.readData(fwImageURL, readComplete);
			FileHeader header = cimService.getFileHeader(headerData);
			if (FirmwareType.FIRMWARE_TYPE_MCFW.ordinal() == header.getType()) {
				controller = true;
			} else if (FirmwareType.FIRMWARE_TYPE_BOOTROM.ordinal() == header.getType()) {
				bootrom = true;
			}

			// Call some CIM method for increasing session validity to 15 min
			cimService.getAllInstances(CIMConstants.CIM_NAMESPACE, CIMConstants.SF_SOFTWARE_INSTALLATION_SERVICE);

			UpdateRequestProcessor requestProcessor = UpdateRequestProcessor.getInstance();
			for (Adapter adapter : adapterList) {
				UpdateRequest updateRequest = null;
				if (controller) {
					updateRequest = createUpdateRequest(adapter, cimService, taskInfo, FwType.CONTROLLER, fwImageURL);
				}
				if (bootrom) {
					updateRequest = createUpdateRequest(adapter, cimService, taskInfo, FwType.BOOTROM, fwImageURL);
				}

				requestProcessor.addUpdateRequest(updateRequest);
			}
		} catch (Exception e) {
			throw e;
		}
		return taskID;

	}

	@Override
	public List<Adapter> getHostAdapters(String hostId) throws Exception {

		List<Adapter> adapters = new ArrayList<>();
		adapters = sfVimService.getHostAdapters(hostId);
		CIMHost cimHost = sfVimService.getCIMHost(hostId);

		SfCIMService cimService = new SfCIMService(new SfCIMClientService(cimHost));
		for (Adapter adapter : adapters) {
			setFirmwareVersions(adapter, cimService);
		}
		return adapters;

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateHostConfigurations(HostConfiguration hostConfigurationRequest) throws Exception {
		// TODO Auto-generated method stub

	}

	private UpdateRequest createUpdateRequest(Adapter adapter, SfCIMService cimService, TaskInfo taskInfo, FwType fwType, URL fwImageURL) throws Exception {
		String adapterId = adapter.getId();
		AdapterTask aTask = getAdapterTask(taskInfo, adapterId);

		CIMInstance fwInstance = null;
		CIMInstance nicInstance = cimService.getNICCardInstance(adapter.getChildren().get(0).getName());
		// Check for controller version
		Status status = new Status(TaskState.Queued, null, fwType);
		if (FwType.CONTROLLER.equals(fwType)) {
			aTask.setController(status);
			fwInstance = cimService.getFirmwareSoftwareInstallationInstance();
		} else if (FwType.BOOTROM.equals(fwType)) {
			aTask.setBootROM(status);
			fwInstance = cimService.getBootROMSoftwareInstallationInstance();
		} else if (FwType.UEFIROM.equals(fwType)) {
			aTask.setUefiROM(status);
		}
		if (fwImageURL == null) {
			fwImageURL = getImageURL(fwInstance, nicInstance, cimService, fwType);
		}
		UpdateRequest updateRequest = new UpdateRequest();
		updateRequest.setAdapterId(adapterId);
		updateRequest.setFwInstance(fwInstance);
		updateRequest.setNicInstance(nicInstance);
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

	private URL getImageURL(CIMInstance fw_inst, CIMInstance nicInstance, SfCIMService cimService, FwType fwType) throws Exception {

		URL pluginURL = new URL(sfVimService.getPluginURL(CIMConstants.PLUGIN_KEY));
		String filePath = null;
		SfFirmware file = MetadataHelper.getMetaDataForAdapter(pluginURL, cimService, fw_inst, nicInstance, fwType);
		if (file != null) {
			filePath = file.getPath();
		}
		URL fwImageURL = new URL("http", pluginURL.getHost(), filePath);

		return fwImageURL;
	}

	private void sendDataInChunks(SfCIMService cimService, CIMInstance fwInstance, String tempFile, byte[] decodedDataBytes) {
		// Send/write this data totemp file on host
		logger.info("Sending data in chunks");
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

		logger.info("Sending data in chunks is complete");
	}

	private void setFirmwareVersions(Adapter adapter, SfCIMService cimService) throws Exception {

		String deviceId = adapter.getChildren().get(0).getName();
		Map<String, String> versions = cimService.getAdapterVersions(deviceId);

		String controllerVersion = versions.get(CIMConstants.CONTROLLER_VERSION);
		String bootROMVersion = versions.get(CIMConstants.BOOT_ROM_VERSION);
		String firmwareVersion = versions.get(CIMConstants.FIRMARE_VERSION);
		String UEFIROMVersion = versions.get(CIMConstants.UEFI_ROM_VERSION);
		adapter.setVersionController(controllerVersion);
		adapter.setVersionBootROM(bootROMVersion);
		adapter.setVersionFirmware(firmwareVersion);
		adapter.setVersionUEFIROM(UEFIROMVersion);

		// Get version from image binary for controller
		CIMInstance fwInstance = cimService.getFirmwareSoftwareInstallationInstance();
		CIMInstance niCimInstance = cimService.getNICCardInstance(deviceId);
		URL pluginURL = new URL(sfVimService.getPluginURL(CIMConstants.PLUGIN_KEY));

		String latestControllerVersion = cimService.getLatestControllerFWImageVersion(pluginURL, cimService, fwInstance, niCimInstance);

		// Get latest version otherwise blank value if both are equal
		String latestVersion = VCenterHelper.getLatestVersion(controllerVersion, latestControllerVersion);
		logger.debug("Getting latest version of controller is :" + latestVersion);
		// Check for latest version available
		FirmwareVersion frmVesion = new FirmwareVersion();

		frmVesion.setController(latestControllerVersion);

		// Get version from image binary for BootRom
		CIMInstance bootROMInstance = cimService.getBootROMSoftwareInstallationInstance();
		String latestBootRomVersion = cimService.getLatestBootROMFWImageVersion(pluginURL, cimService, bootROMInstance, niCimInstance);
		logger.debug("Getting latest version of BootRom is :" + latestBootRomVersion);
		String finalLatestBootVersion = VCenterHelper.getLatestVersion(bootROMVersion, latestBootRomVersion);
		frmVesion.setBootROM(finalLatestBootVersion);

		// Put dummy latest versions for UEFI and Firmware family
		frmVesion.setUefi(UEFIROMVersion); // setting current as latest for now
		frmVesion.setFirmewareFamily(firmwareVersion); // setting current as
														// latest for now

		adapter.setLatestVersion(frmVesion);
	}

}
