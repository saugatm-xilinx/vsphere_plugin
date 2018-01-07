package com.solarflare.vcp.services;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.cim.CIMInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import com.solarflare.vcp.model.TaskStatus;
import com.solarflare.vcp.model.UpdateRequest;
import com.solarflare.vcp.vim.SfVimService;
import com.solarflare.vcp.vim.SfVimServiceImpl;
import com.solarflare.vcp.vim.connection.Connection;
import com.solarflare.vcp.vim25.VCenterService;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.VimPortType;
import com.vmware.vise.security.ClientSessionEndListener;
import com.vmware.vise.usersession.UserSessionService;

public class HostAdapterServiceImpl implements HostAdapterService, ClientSessionEndListener {
	private static final Log logger = LogFactory.getLog(HostAdapterServiceImpl.class);

	private UserSessionService userSessionService;
	// private static ExecutorService executor =
	// Executors.newFixedThreadPool(1);

	// VCenterService service = new VCenterService();
	// CIMService cim = new CIMService();

	@Autowired
	SfVimService sfVimService;

	@Autowired
	public HostAdapterServiceImpl(UserSessionService session) {
		userSessionService = session;
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

			// CIMHost cimHost = new SfVimService().getCIMHost(hostId,
			// "TestingOnly");
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

			taskManager.addTaskInfo(taskInfo);
		} catch (Exception e) {
			throw e;
		}
		return taskID;
		// String taskID = null;
		// logger.info("Start updating firmware adapter");
		// try {
		// TaskManager taskManager = TaskManager.getInstance();
		// taskID = taskManager.getTaskId();
		// TaskInfo taskInfo = new TaskInfo();
		// taskInfo.setTaskid(taskID);
		// String data = null; // as this is Update from URL
		// ServiceContent serviceContent =
		// VCenterHelper.getServiceContent(userSessionService,
		// VCenterService.vimPort);
		// CIMHost cimHost = service.getCIMHost(serviceContent, hostId, cim);
		// CIMInstance nicInstance = null;
		// for (Adapter adapter : adapterList) {
		// String adapterId = adapter.getId();
		// nicInstance = cim.getNICCardInstance(cimHost,
		// adapter.getChildren().get(0).getName());
		// FirmwareVersion fwVersion = adapter.getLatestVersion();
		// boolean contronller = false;
		// boolean bootRom = false;
		// // Check for controller version
		// String currentVersion = adapter.getVersionController();
		// String latestVersion = fwVersion.getController();
		// String latest = VCenterHelper.getLatestVersion(currentVersion,
		// latestVersion);
		// if (latest.equals(latestVersion)) {
		// contronller = true;
		// }
		// // Check for Boot ROM version
		// currentVersion = adapter.getVersionBootROM();
		// latestVersion = fwVersion.getBootROM();
		// latest = VCenterHelper.getLatestVersion(currentVersion,
		// latestVersion);
		// if (latest.equals(latestVersion)) {
		// bootRom = true;
		// }
		// Callable<Void> workerForController = new
		// FirmwareUpdateThread(serviceContent, cim, cimHost, null, null,
		// nicInstance, adapterId, hostId, false, contronller, bootRom,
		// taskInfo);
		// Future<Void> futureTask = executor.submit(workerForController);
		//
		// }
		//
		// taskManager.addTaskInfo(taskInfo);
		// } catch (Exception e) {
		// throw e;
		// }
		// return taskID;
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

				fwImageURL = new URL("file:/" + tempFile);

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

					requestProcessor.addUpdateRequest(updateRequest);

				}
				/*
				 * if (finshed) { // Delete temp file after updating firmware
				 * boolean isRemoved = cim.removeFwImage(cimHost, fwInstance,
				 * tempFile); logger.info("File " + tempFile +
				 * " Removed status: " + isRemoved); }
				 */
			} else {
				logger.error("Fail to get CIM Firmware Instance");
			}
			taskManager.addTaskInfo(taskInfo);
		} catch (Exception e) {
			throw e;
		}
		return taskID;

		// logger.info("Start updating firmware adapter");
		// String taskID = null;
		// try {
		//
		// TaskManager taskManager = TaskManager.getInstance();
		// taskID = taskManager.getTaskId();
		// TaskInfo taskInfo = new TaskInfo();
		// taskInfo.setTaskid(taskID);
		// URL fwImageURL = null;
		// ServiceContent serviceContent =
		// VCenterHelper.getServiceContent(userSessionService,
		// VCenterService.vimPort);
		// CIMHost cimHost = service.getCIMHost(serviceContent, hostId, cim);
		//
		// byte[] dataBytes = base64Data.getBytes();
		// // Decode this data using java's decoder
		// Base64.Decoder decoder = Base64.getDecoder();
		// byte[] decodedDataBytes = decoder.decode(dataBytes);
		//
		// // Get Header info from data
		// byte[] headerData = Arrays.copyOf(decodedDataBytes, 40); // header
		// // size
		// // is 40
		// // bytes
		// FileHeader header = cim.getFileHeader(headerData);
		//
		// boolean controller = false;
		// boolean bootrom = false;
		// CIMInstance fwInstance = null;
		// if (FirmwareType.FIRMWARE_TYPE_MCFW.ordinal() == header.getType()) {
		// logger.info("Updating Controller");
		// fwInstance = cim.getFirmwareSoftwareInstallationInstance(cimHost);
		// controller = true;
		// } else if (FirmwareType.FIRMWARE_TYPE_BOOTROM.ordinal() ==
		// header.getType()) {
		// // Get BootROM SF_SoftwareInstallationService instance
		// logger.info("Updating BootROM");
		// fwInstance = cim.getBootROMSoftwareInstallationInstance(cimHost);
		// bootrom = true;
		// }
		//
		// if (fwInstance != null) {
		// String tempFile = cim.startFwImageSend(cimHost, fwInstance);
		//
		// sendDataInChunks(cimHost, fwInstance, tempFile, decodedDataBytes);
		//
		// fwImageURL = new URL("file:/" + tempFile);
		//
		// CIMInstance nicInstance = null;
		//
		// for (Adapter adapter : adapterList) {
		// nicInstance = cim.getNICCardInstance(cimHost,
		// adapter.getChildren().get(0).getName());
		//
		// Callable<Void> workerForFW = new FirmwareUpdateThread(serviceContent,
		// cim, cimHost, fwImageURL,
		// header, nicInstance, adapter.getId(), hostId, true, controller,
		// bootrom, taskInfo);
		// Future<Void> futureTask = executor.submit(workerForFW);
		//
		// }
		// /*
		// * if (finshed) { // Delete temp file after updating firmware
		// * boolean isRemoved = cim.removeFwImage(cimHost, fwInstance,
		// * tempFile); logger.info("File " + tempFile +
		// * " Removed status: " + isRemoved); }
		// */
		// } else {
		// logger.error("Fail to get CIM Firmware Instance");
		// }
		// taskManager.addTaskInfo(taskInfo);
		// } catch (Exception e) {
		// throw e;
		// }
		// return taskID;
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

			URL fwImageURL = new URL(fwImagePath);
			// ServiceContent serviceContent =
			// VCenterHelper.getServiceContent(userSessionService,
			// VCenterService.vimPort);
			// CIMHost cimHost = new SfVimService().getCIMHost(hostId,
			// "TestingOnly");
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
			taskManager.addTaskInfo(taskInfo);
		} catch (Exception e) {
			throw e;
		}
		return taskID;

		// logger.info("Start updating firmware adapter");
		// String taskID = null;
		// try {
		// TaskManager taskManager = TaskManager.getInstance();
		// taskID = taskManager.getTaskId();
		// TaskInfo taskInfo = new TaskInfo();
		// taskInfo.setTaskid(taskID);
		// taskInfo.setHostId(hostId);
		// String data = null; // As this is update from URL
		// URL fwImageURL = new URL(fwImagePath);
		// ServiceContent serviceContent =
		// VCenterHelper.getServiceContent(userSessionService,
		// VCenterService.vimPort);
		// CIMHost cimHost = service.getCIMHost(serviceContent, hostId, cim);
		//
		// boolean readComplete = false;
		// byte[] headerData = cim.readData(fwImageURL, readComplete);
		// FileHeader header = cim.getFileHeader(headerData);
		// data = new String(headerData);
		// boolean controller = false;
		// boolean bootrom = false;
		// if (FirmwareType.FIRMWARE_TYPE_MCFW.ordinal() == header.getType()) {
		// controller = true;
		// } else if (FirmwareType.FIRMWARE_TYPE_BOOTROM.ordinal() ==
		// header.getType()) {
		// bootrom = true;
		// }
		//
		// CIMInstance nicInstance = null;
		//
		// for (Adapter adapter : adapterList) {
		// nicInstance = cim.getNICCardInstance(cimHost,
		// adapter.getChildren().get(0).getName());
		//
		// Callable<Void> workerForFW = new FirmwareUpdateThread(serviceContent,
		// cim, cimHost, fwImageURL, header,
		// nicInstance, adapter.getId(), hostId, true, controller, bootrom,
		// taskInfo);
		// Future<Void> futureTask = executor.submit(workerForFW);
		//
		// }
		// taskManager.addTaskInfo(taskInfo);
		// } catch (Exception e) {
		// throw e;
		// }
		// return taskID;
	}

	@Override
	public void sessionEnded(String clientId) {
		logger.info("Logging out client session - " + clientId);

		// Clean up all session specific resources.
		// Logout from any session specific services.

	}

	@Override
	public List<Adapter> getHostAdapters(String hostId) throws Exception {

		List<Adapter> adapters = new ArrayList<>();
		adapters = sfVimService.getHostAdapters(hostId);
		// Set Firmware Versions for adapters
		// CIMHost cimHost = new SfVimService().getCIMHost(hostId, "testing
		// only");
		CIMHost cimHost = sfVimService.getCIMHost(hostId);
		SfCIMService cimService = new SfCIMService(new SfCIMClientService(cimHost));
		Connection con = new SfVimServiceImpl().getConnection();
		ServiceContent serviceContent = con.getServiceContent();
		VimPortType vimPort = con.getVimPort();
		for (Adapter adapter : adapters) {
			setFirmwareVersions(adapter, cimService, serviceContent, vimPort);
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

	/*
	 * private void sendDataInChunks(CIMHost cimHost, CIMInstance fwInstance,
	 * String tempFile, byte[] decodedDataBytes) { // Send/write this data
	 * totemp file on host logger.info("Sending data in chunks"); SFBase64
	 * sfBase64 = new SFBase64(); int chunkSize = 100000; int index; for (index
	 * = 0; index < decodedDataBytes.length; index += chunkSize) { if (index +
	 * chunkSize > decodedDataBytes.length) { chunkSize =
	 * decodedDataBytes.length - index; } byte[] temp =
	 * Arrays.copyOfRange(decodedDataBytes, index, index + chunkSize); int
	 * encodeSize = sfBase64.base64_enc_size(chunkSize); byte[] encoded = new
	 * byte[encodeSize]; // using SFBase64 to encode data encoded =
	 * sfBase64.base64_encode(temp, chunkSize); cim.sendFWImageData(cimHost,
	 * fwInstance, new String(encoded), tempFile); }
	 * 
	 * logger.info("Sending data in chunks is complete"); }
	 */

	@Override
	public List<Status> getStatus(String hostId, String adapterId) throws Exception {

		String cntStatusId = VCenterHelper.generateId(hostId, adapterId, MessageConstant.CONTROLLER);
		String bootStatusId = VCenterHelper.generateId(hostId, adapterId, MessageConstant.BOOTROM);

		List<Status> statusList = new ArrayList<>();
		List<Status> cntStatus = TaskStatus.getTaskStatus(cntStatusId);
		List<Status> boootStatus = TaskStatus.getTaskStatus(bootStatusId);

		statusList.addAll(cntStatus == null ? new ArrayList<Status>() : cntStatus);
		statusList.addAll(boootStatus == null ? new ArrayList<Status>() : boootStatus);

		return statusList;

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

	/* Temp implementation for update firmware */

	public String updateLatest(List<Adapter> adapterList, String hostId) throws Exception {
		String taskID = null;
		logger.info("Start updating firmware adapter");
		try {
			// TODO :: validate all adapter before update
			TaskManager taskManager = TaskManager.getInstance();
			taskID = taskManager.getTaskId();
			TaskInfo taskInfo = new TaskInfo();
			taskInfo.setTaskid(taskID);
			taskInfo.setHostId(hostId);

			// CIMHost cimHost = new SfVimService().getCIMHost(hostId,
			// "TestingOnly");
			CIMHost cimHost = new SfVimServiceImpl().getCIMHost(hostId);
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

			taskManager.addTaskInfo(taskInfo);
		} catch (Exception e) {
			throw e;
		}
		return taskID;
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

	public String customUpdateFromURL(List<Adapter> adapterList, String hostId, String fwImagePath) throws Exception {
		logger.info("Start updating firmware adapter");
		String taskID = null;
		try {

			TaskManager taskManager = TaskManager.getInstance();
			taskID = taskManager.getTaskId();
			TaskInfo taskInfo = new TaskInfo();
			taskInfo.setTaskid(taskID);
			taskInfo.setHostId(hostId);

			URL fwImageURL = new URL(fwImagePath);
			// ServiceContent serviceContent =
			// VCenterHelper.getServiceContent(userSessionService,
			// VCenterService.vimPort);
			// CIMHost cimHost = new SfVimService().getCIMHost(hostId,
			// "TestingOnly");
			CIMHost cimHost = new SfVimServiceImpl().getCIMHost(hostId, "TestingOnly");
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
			taskManager.addTaskInfo(taskInfo);
		} catch (Exception e) {
			throw e;
		}
		return taskID;
	}

	public String customUpdateFromLocal(List<Adapter> adapterList, String hostId, String base64Data) throws Exception {
		logger.info("Start updating firmware adapter");
		String taskID = null;
		try {

			TaskManager taskManager = TaskManager.getInstance();
			taskID = taskManager.getTaskId();
			TaskInfo taskInfo = new TaskInfo();
			taskInfo.setTaskid(taskID);
			URL fwImageURL = null;

			// CIMHost cimHost = new SfVimService().getCIMHost(hostId,
			// "TestingOnly");
			CIMHost cimHost = new SfVimServiceImpl().getCIMHost(hostId);
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

				fwImageURL = new URL("file:/" + tempFile);

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

					requestProcessor.addUpdateRequest(updateRequest);

				}
				/*
				 * if (finshed) { // Delete temp file after updating firmware
				 * boolean isRemoved = cim.removeFwImage(cimHost, fwInstance,
				 * tempFile); logger.info("File " + tempFile +
				 * " Removed status: " + isRemoved); }
				 */
			} else {
				logger.error("Fail to get CIM Firmware Instance");
			}
			taskManager.addTaskInfo(taskInfo);
		} catch (Exception e) {
			throw e;
		}
		return taskID;
	}

	private URL getImageURL(CIMInstance fw_inst, CIMInstance nicInstance, SfCIMService cimService, FwType fwType) throws Exception {
		ServiceContent serviceContent = VCenterHelper.getServiceContent(userSessionService, VCenterService.vimPort);
		String urlPath = cimService.getPluginURL(serviceContent, VCenterService.vimPort, CIMConstants.PLUGIN_KEY);
		URL pluginURL = new URL(urlPath);
		String filePath = null;
		SfFirmware file = new MetadataHelper().getMetaDataForAdapter(pluginURL, cimService, fw_inst, nicInstance, fwType);
		if (file != null) {
			filePath = file.getPath();
		}
		// TODO : check for https certificate warning
		// TODO check version current version and version from file for
		// both controller and BootRom
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

	private List<Adapter> getAdaptersFromFile() throws Exception {
		// FileInputStream fin = new
		// FileInputStream("D:\\Projects\\SolarFlare\\adapterList.txt");
		FileReader fReader = new FileReader(new File("D:\\Projects\\SolarFlare\\adapterList.txt"));
		FileWriter fW = new FileWriter(new File("D:\\Projects\\SolarFlare\\adapterList2.txt"));
		char[] data = new char[100000];
		int noRead = fReader.read(data);
		System.out.println("Data read : " + noRead);
		String adapterList = new String(data, 0, noRead);
		fW.write(adapterList);
		System.out.println(adapterList);

		Gson gson = new Gson();
		Type listType = new TypeToken<List<Adapter>>() {
		}.getType();

		List<Adapter> adapter = gson.fromJson(adapterList, listType);
		System.out.println(adapter.get(0).getId());
		return adapter;
	}

	private void setFirmwareVersions(Adapter adapter, SfCIMService cimService, ServiceContent serviceContent, VimPortType vimPort) throws Exception {

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
		String latestControllerVersion = cimService.getLatestControllerFWImageVersion(serviceContent, vimPort, cimService, fwInstance, niCimInstance);

		// Get latest version otherwise blank value if both are equal
		String latestVersion = VCenterHelper.getLatestVersion(controllerVersion, latestControllerVersion);
		logger.debug("Getting latest version of controller is :" + latestVersion);
		// Check for latest version available
		FirmwareVersion frmVesion = new FirmwareVersion();

		frmVesion.setController(latestControllerVersion);

		// Get version from image binary for BootRom
		CIMInstance bootROMInstance = cimService.getBootROMSoftwareInstallationInstance();
		String latestBootRomVersion = cimService.getLatestBootROMFWImageVersion(serviceContent, vimPort, cimService, bootROMInstance, niCimInstance);
		logger.debug("Getting latest version of BootRom is :" + latestBootRomVersion);
		String finalLatestBootVersion = VCenterHelper.getLatestVersion(bootROMVersion, latestBootRomVersion);
		frmVesion.setBootROM(finalLatestBootVersion);

		// Put dummy latest versions for UEFI and Firmware family
		frmVesion.setUefi(UEFIROMVersion); // setting current as latest for now
		frmVesion.setFirmewareFamily(firmwareVersion); // setting current as
														// latest for now

		adapter.setLatestVersion(frmVesion);
	}

	// TODO : Cleanup main method

	// public static void main(String[] args) throws Exception {
	//
	// HostAdapterServiceImpl adapterServiceImpl = new HostAdapterServiceImpl();
	// List<Adapter> adapterList = adapterServiceImpl.getAdaptersFromFile();
	// String imgPath = "http://10.101.10.132/customFw/v6.2.5.1000/mcfw.dat";
	// //adapterServiceImpl.customUpdateFromURL(adapterList, "host-14",
	// imgPath);
	// adapterServiceImpl.updateLatest(adapterList, "host-14");
	// }

}
