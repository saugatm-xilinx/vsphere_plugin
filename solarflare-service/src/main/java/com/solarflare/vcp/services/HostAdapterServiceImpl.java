package com.solarflare.vcp.services;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.cim.CIMInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.solarflare.vcp.cim.CIMHost;
import com.solarflare.vcp.cim.CIMService;
import com.solarflare.vcp.helper.SFBase64;
import com.solarflare.vcp.helper.VCenterHelper;
import com.solarflare.vcp.model.Adapter;
import com.solarflare.vcp.model.FileHeader;
import com.solarflare.vcp.model.FirmwareVersion;
import com.solarflare.vcp.model.FirmwareType;
import com.solarflare.vcp.model.Host;
import com.solarflare.vcp.model.HostConfiguration;
import com.solarflare.vcp.model.NicBootParamInfo;
import com.solarflare.vcp.model.Status;
import com.solarflare.vcp.model.TaskInfo;
import com.solarflare.vcp.model.TaskStatus;
import com.solarflare.vcp.vim25.VCenterService;
import com.vmware.vim25.ServiceContent;
import com.vmware.vise.security.ClientSessionEndListener;
import com.vmware.vise.usersession.UserSessionService;

public class HostAdapterServiceImpl implements HostAdapterService, ClientSessionEndListener {
	private static final Log logger = LogFactory.getLog(HostAdapterServiceImpl.class);

	private UserSessionService userSessionService;
	private static ExecutorService executor = Executors.newFixedThreadPool(1);

	VCenterService service = new VCenterService();
	CIMService cim = new CIMService();

	@Autowired
	public HostAdapterServiceImpl(UserSessionService session) {
		userSessionService = session;
	}

	@Override
	public List<Host> getHostList() throws Exception {
		List<Host> hostList = null;
		try {
			hostList = service.getHostsList(userSessionService);
		} catch (Exception e) {
			throw e;
		}
		return hostList;
	}

	@Override
	public Host getHostById(String hostId) throws Exception {
		Host host = null;
		try {
			host = service.getHostById(userSessionService, hostId);
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
			TaskManager taskManager = TaskManager.getInstance();
			taskID = taskManager.getTaskId();
			TaskInfo taskInfo = new TaskInfo();
			taskInfo.setTaskid(taskID);
			String data = null; // as this is Update from URL
			ServiceContent serviceContent = VCenterHelper.getServiceContent(userSessionService, VCenterService.vimPort);
			CIMHost cimHost = service.getCIMHost(serviceContent, hostId, cim);
			CIMInstance nicInstance = null;
			for (Adapter adapter : adapterList) {
				String adapterId = adapter.getId();
				nicInstance = cim.getNICCardInstance(cimHost, adapter.getChildren().get(0).getName());
				FirmwareVersion fwVersion = adapter.getLatestVersion();
				boolean contronller = false;
				boolean bootRom = false;
				// Check for controller version
				String currentVersion = adapter.getVersionController();
				String latestVersion = fwVersion.getControlerVersion();
				String latest = VCenterHelper.getLatestVersion(currentVersion, latestVersion);
				if (latest.equals(latestVersion)) {
					contronller = true;
				}
				// Check for Boot ROM version
				currentVersion = adapter.getVersionBootROM();
				latestVersion = fwVersion.getBootROMVersion();
				latest = VCenterHelper.getLatestVersion(currentVersion, latestVersion);
				if (latest.equals(latestVersion)) {
					bootRom = true;
				}
				Callable<Void> workerForController = new FirmwareUpdateThread(serviceContent, cim, cimHost, null, null,
						nicInstance, adapterId, hostId, false, contronller, bootRom, taskInfo);
				Future<Void> futureTask = executor.submit(workerForController);

			}

			taskManager.addTaskInfo(taskInfo);
		} catch (Exception e) {
			throw e;
		}
		return taskID;
	}

	@Override
	public String customUpdateFirmwareFromLocal(List<Adapter> adapterList, String hostId, String base64Data)
			throws Exception {
		logger.info("Start updating firmware adapter");
		String taskID = null;
		try {

			TaskManager taskManager = TaskManager.getInstance();
			taskID = taskManager.getTaskId();
			TaskInfo taskInfo = new TaskInfo();
			taskInfo.setTaskid(taskID);
			URL fwImageURL = null;
			ServiceContent serviceContent = VCenterHelper.getServiceContent(userSessionService, VCenterService.vimPort);
			CIMHost cimHost = service.getCIMHost(serviceContent, hostId, cim);

			byte[] dataBytes = base64Data.getBytes();
			// Decode this data using java's decoder
			Base64.Decoder decoder = Base64.getDecoder();
			byte[] decodedDataBytes = decoder.decode(dataBytes);

			// Get Header info from data
			byte[] headerData = Arrays.copyOf(decodedDataBytes, 40); // header
																		// size
																		// is 40
																		// bytes
			FileHeader header = cim.getFileHeader(headerData);

			boolean controller = false;
			boolean bootrom = false;
			CIMInstance fwInstance = null;
			if (FirmwareType.FIRMWARE_TYPE_MCFW.ordinal() == header.getType()) {
				logger.info("Updating Controller");
				fwInstance = cim.getFirmwareSoftwareInstallationInstance(cimHost);
				controller = true;
			} else if (FirmwareType.FIRMWARE_TYPE_BOOTROM.ordinal() == header.getType()) {
				// Get BootROM SF_SoftwareInstallationService instance
				logger.info("Updating BootROM");
				fwInstance = cim.getBootROMSoftwareInstallationInstance(cimHost);
				bootrom = true;
			}

			if (fwInstance != null) {
				String tempFile = cim.startFwImageSend(cimHost, fwInstance);

				sendDataInChunks(cimHost, fwInstance, tempFile, decodedDataBytes);

				fwImageURL = new URL("file:/" + tempFile);

				CIMInstance nicInstance = null;

				for (Adapter adapter : adapterList) {
					nicInstance = cim.getNICCardInstance(cimHost, adapter.getChildren().get(0).getName());

					Callable<Void> workerForFW = new FirmwareUpdateThread(serviceContent, cim, cimHost, fwImageURL,
							header, nicInstance, adapter.getId(), hostId, true, controller, bootrom, taskInfo);
					Future<Void> futureTask = executor.submit(workerForFW);

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

	@Override
	public String customUpdateFirmwareFromURL(List<Adapter> adapterList, String hostId, String fwImagePath)
			throws Exception {
		logger.info("Start updating firmware adapter");
		String taskID = null;
		try {
			TaskManager taskManager = TaskManager.getInstance();
			taskID = taskManager.getTaskId();
			TaskInfo taskInfo = new TaskInfo();
			taskInfo.setTaskid(taskID);
			taskInfo.setHostId(hostId);
			String data = null; // As this is update from URL
			URL fwImageURL = new URL(fwImagePath);
			ServiceContent serviceContent = VCenterHelper.getServiceContent(userSessionService, VCenterService.vimPort);
			CIMHost cimHost = service.getCIMHost(serviceContent, hostId, cim);

			boolean readComplete = false;
			byte[] headerData = cim.readData(fwImageURL, readComplete);
			FileHeader header = cim.getFileHeader(headerData);
			data = new String(headerData);
			boolean controller = false;
			boolean bootrom = false;
			if (FirmwareType.FIRMWARE_TYPE_MCFW.ordinal() == header.getType()) {
				controller = true;
			} else if (FirmwareType.FIRMWARE_TYPE_BOOTROM.ordinal() == header.getType()) {
				bootrom = true;
			}

			CIMInstance nicInstance = null;

			for (Adapter adapter : adapterList) {
				nicInstance = cim.getNICCardInstance(cimHost, adapter.getChildren().get(0).getName());

				Callable<Void> workerForFW = new FirmwareUpdateThread(serviceContent, cim, cimHost, fwImageURL, header,
						nicInstance, adapter.getId(), hostId, true, controller, bootrom, taskInfo);
				Future<Void> futureTask = executor.submit(workerForFW);

			}
			taskManager.addTaskInfo(taskInfo);
		} catch (Exception e) {
			throw e;
		}
		return taskID;
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
		adapters = service.getAdapters(userSessionService, hostId, cim);

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

	private void sendDataInChunks(CIMHost cimHost, CIMInstance fwInstance, String tempFile, byte[] decodedDataBytes) {
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
			cim.sendFWImageData(cimHost, fwInstance, new String(encoded), tempFile);
		}

		logger.info("Sending data in chunks is complete");
	}

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

}
