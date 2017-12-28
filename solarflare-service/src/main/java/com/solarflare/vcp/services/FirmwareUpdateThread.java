package com.solarflare.vcp.services;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.cim.CIMInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.solarflare.vcp.cim.CIMConstants;
import com.solarflare.vcp.cim.CIMHost;
import com.solarflare.vcp.cim.CIMService;
import com.solarflare.vcp.helper.MetadataHelper;
import com.solarflare.vcp.helper.VCenterHelper;
import com.solarflare.vcp.model.AdapterTask;
import com.solarflare.vcp.model.FileHeader;
import com.solarflare.vcp.model.FwType;
import com.solarflare.vcp.model.SfFirmware;
import com.solarflare.vcp.model.Status;
import com.solarflare.vcp.model.TaskInfo;
import com.solarflare.vcp.model.TaskState;
import com.solarflare.vcp.model.TaskStatus;
import com.solarflare.vcp.vim25.VCenterService;
import com.vmware.vim25.ServiceContent;

public class FirmwareUpdateThread implements Runnable {
	private static final Log logger = LogFactory.getLog(HostAdapterServiceImpl.class);
	private boolean isCustom;
	private boolean controller;
	private boolean bootrom;
	private String hostId;
	String adapterId;
	CIMHost cimHost;
	URL fwImagePath;
	FileHeader header;
	CIMService cim;
	CIMInstance nicInstance;
	ServiceContent serviceContent;
	private String currentProcess;
	private TaskInfo taskInfo;

	MetadataHelper metadataHelper = new MetadataHelper();

	FirmwareUpdateThread(ServiceContent serviceContent, CIMService cim, CIMHost cimHost, URL fwImageURL,
			FileHeader header, CIMInstance nicInstance, String adapterId, String hostId, boolean isCustom,
			boolean controller, boolean bootrom, TaskInfo taskInfo) {
		this.adapterId = adapterId;
		this.cim = cim;
		this.cimHost = cimHost;
		this.header = header;
		this.nicInstance = nicInstance;
		this.serviceContent = serviceContent;
		this.fwImagePath = fwImageURL;
		this.isCustom = isCustom;
		this.controller = controller;
		this.bootrom = bootrom;
		this.hostId = hostId;
		this.taskInfo = taskInfo;
	}

	@Override
	public void run() {
		logger.debug("Updating firmware for adapter : " + adapterId);
		try {

			logger.info("Processing .....");
			process();
			logger.info("Processing done.");
			// if (TaskStatus.getTaskStatus(adapterId) == null ||
			// TaskStatus.getTaskStatus(adapterId).getStatus().equals(UploadStatusEnum.DONE.toString()))
			// {
			// TaskStatus.updateTaskStatus(adapterId,
			// UploadStatusEnum.VALIDATING.toString(), null);
			//
			// //process();
			// } else {
			//// logger.info("Processing .....");
			// }
		} catch (Exception e) {
			logger.error("Exception while uploading binary file for adapter " + adapterId + ":" + e.getMessage());
			if (MessageConstant.CONTROLLER.equals(currentProcess)) {
				TaskStatus.updateTaskStatus(adapterId, UploadStatusEnum.UPLOADING_FAIL.toString(), e.getMessage(),
						MessageConstant.CONTROLLER);
			} else if (MessageConstant.BOOTROM.equals(currentProcess)) {
				TaskStatus.updateTaskStatus(adapterId, UploadStatusEnum.UPLOADING_FAIL.toString(), e.getMessage(),
						MessageConstant.BOOTROM);
			}

		}
	}

	public void process() throws Exception {
		Status controllerStatus = null;
		Status bootROMStatus = null;
		if (isCustom) {
			if (controller) {
				controllerStatus = updateController();
			}
			if (bootrom) {
				bootROMStatus = updateBootROM();
			}

		} else {
			if (controller) {
				controllerStatus =  updateController();
			}
			if (bootrom) {
				bootROMStatus = updateBootROM();
			}
		}
		
		List<AdapterTask> adapterTasks = taskInfo.getAdapterTasks();
		if(adapterTasks == null){
			adapterTasks = new ArrayList<>();
		}
		AdapterTask aTask = new AdapterTask();
		aTask.setAdapterId(adapterId);
		aTask.setController(controllerStatus);
		aTask.setBootROM(bootROMStatus);
		
		adapterTasks.add(aTask);
		taskInfo.setAdapterTasks(adapterTasks);
	}

	private URL getImageURL(CIMInstance fw_inst, boolean isController) throws Exception {
		String urlPath = cim.getPluginURL(serviceContent, VCenterService.vimPort, CIMConstants.PLUGIN_KEY);
		URL pluginURL = new URL(urlPath);
		String filePath = null;
		SfFirmware file = metadataHelper.getMetaDataForAdapter(serviceContent, VCenterService.vimPort, cimHost, fw_inst,
				this.nicInstance, isController);
		if (file != null) {
			filePath = file.getPath();
		}
		// TODO : check for https certificate warning
		// TODO check version current version and version from file for
		// both controller and BootRom
		URL fwImageURL = new URL("http", pluginURL.getHost(), filePath);

		return fwImageURL;
	}

	private synchronized Status updateController() throws Exception {
		Status controllerStatus = null;
		try{
			
			
			controllerStatus= new Status(TaskState.Queued, null, FwType.CONTROLLER);
			
			
		logger.info("Start updating controller for adapterId : " + adapterId);
		currentProcess = MessageConstant.CONTROLLER;
		String statusId = VCenterHelper.generateId(hostId, adapterId, MessageConstant.CONTROLLER);
		CIMInstance svc_mcfw_inst = cim.getFirmwareSoftwareInstallationInstance(cimHost);
		if (!isCustom) {
			fwImagePath = getImageURL(svc_mcfw_inst, true);
		
			byte[] bytes = null;
			boolean readComplete = false;
			bytes = cim.readData(fwImagePath, readComplete);
			header = cim.getFileHeader(bytes);
		}
		logger.info("FW Image URL : "+fwImagePath);
		TaskStatus.updateTaskStatus(statusId, UploadStatusEnum.VALIDATING.toString(),
				"Controller firmware image validation in-progress", MessageConstant.CONTROLLER);
		logger.info("Adapter Id :" + statusId + " , controller " + UploadStatusEnum.VALIDATING.toString());
		boolean isCompatable = cim.isCustomFWImageCompatible(cimHost, svc_mcfw_inst, this.nicInstance, header);
		if (isCompatable) {
			logger.info("Adapter Id :" + statusId + " , controller " + UploadStatusEnum.VALIDATED.toString());
			TaskStatus.updateTaskStatus(statusId, UploadStatusEnum.VALIDATED.toString(),
					"Controller firmware image validated successfully", MessageConstant.CONTROLLER);
			logger.info("Adapter Id :" + statusId + " , controller " + UploadStatusEnum.UPLOADING.toString());
			TaskStatus.updateTaskStatus(statusId, UploadStatusEnum.UPLOADING.toString(),
					"Controller firmware image file uploading in-progress", MessageConstant.CONTROLLER);

			controllerStatus.setState(TaskState.Running); 
			boolean res = cim.updateFirmwareFromURL(svc_mcfw_inst.getObjectPath(), cimHost, this.nicInstance, fwImagePath);
			logger.info("Result after upload is : " + res);
			if (res) {
				controllerStatus.setState(TaskState.Success);
				TaskStatus.updateTaskStatus(statusId, UploadStatusEnum.DONE.toString(),
						"Controller firmware image update is done", MessageConstant.CONTROLLER);
				logger.info("Contoller firmware update for adapter '" + statusId + "' is done.");
			} else {
				controllerStatus.setState(TaskState.Error);
				controllerStatus.setErrorMessage("Fail to update Controller firmware image");
				TaskStatus.updateTaskStatus(statusId, UploadStatusEnum.UPLOADING_FAIL.toString(),
						"Fail to update Controller firmware image", MessageConstant.CONTROLLER);
				logger.info("Contoller firmware update for adapter '" + statusId + "' is failed");
			}

		} else {
			controllerStatus.setState(TaskState.Error);
			controllerStatus.setErrorMessage("Controller firmware image is not compatable");
			TaskStatus.updateTaskStatus(statusId, UploadStatusEnum.VALIDATION_FAIL.toString(),
					"Controller firmware image is not compatable", MessageConstant.CONTROLLER);
			logger.info("Image not compatable");
		}
		currentProcess = "";
		}catch(Exception e){
			controllerStatus.setState(TaskState.Error);
			controllerStatus.setErrorMessage("Controller firmware image is not compatable");
			logger.error("Error in updating controller firmware, error "+e.getMessage());
		}
		return controllerStatus;
	}

	private synchronized Status updateBootROM() throws Exception {
		logger.info("Starting bootrom update ");
		Status bootROMStatus = null; 
		bootROMStatus= new Status(TaskState.Queued, null, FwType.BOOTROM);
		currentProcess = MessageConstant.BOOTROM;
		String statusId = VCenterHelper.generateId(hostId, adapterId, MessageConstant.BOOTROM);
		CIMInstance svc_bootrom_inst = cim.getBootROMSoftwareInstallationInstance(cimHost);
		if (!isCustom) {
			fwImagePath = getImageURL(svc_bootrom_inst, false);
			
			byte[] bytes = null;
			boolean readComplete = false;
			bytes = cim.readData(fwImagePath, readComplete);
			header = cim.getFileHeader(bytes);
		}
		
			

		TaskStatus.updateTaskStatus(statusId, UploadStatusEnum.VALIDATING.toString(),
				"BootROM firmware image validation in-progress", MessageConstant.BOOTROM);
		logger.info("Adapter Id :" + statusId + " , bootrom " + UploadStatusEnum.VALIDATING.toString());
		boolean isCompatable = cim.isCustomFWImageCompatible(cimHost, svc_bootrom_inst, this.nicInstance, header);

		if (isCompatable) {
			logger.info("Adapter Id :" + statusId + " , bootrom " + UploadStatusEnum.VALIDATED.toString());
			TaskStatus.updateTaskStatus(statusId, UploadStatusEnum.VALIDATED.toString(),
					"BootROM firmware image validated successfully", MessageConstant.BOOTROM);
			logger.info("Adapter Id :" + statusId + " , bootrom " + UploadStatusEnum.UPLOADING.toString());
			TaskStatus.updateTaskStatus(statusId, UploadStatusEnum.UPLOADING.toString(),
					"BootROM firmware image file uploading in-progress", MessageConstant.BOOTROM);

			bootROMStatus.setState(TaskState.Running);
			boolean res = cim.updateFirmwareFromURL(svc_bootrom_inst.getObjectPath(), cimHost, nicInstance,
					fwImagePath);
			if (res) {
				bootROMStatus.setState(TaskState.Success);
				TaskStatus.updateTaskStatus(statusId, UploadStatusEnum.DONE.toString(),
						"BootROM firmware image update is done", MessageConstant.BOOTROM);
				logger.debug("BootROM firmware update for adapter '" + statusId + "' is done.");
			} else {
				bootROMStatus.setState(TaskState.Error);
				bootROMStatus.setErrorMessage("Fail to update BootROM firmware image");
				TaskStatus.updateTaskStatus(statusId, UploadStatusEnum.UPLOADING_FAIL.toString(),
						"Fail to update BootROM firmware image", MessageConstant.BOOTROM);
				logger.debug("BootROM firmware update for adapter '" + statusId + "' is failed");
			}

		} else {
			bootROMStatus.setState(TaskState.Error);
			bootROMStatus.setErrorMessage("BootROM firmware image is not compatable");
			TaskStatus.updateTaskStatus(statusId, UploadStatusEnum.VALIDATION_FAIL.toString(),
					"BootROM firmware image is not compatable", MessageConstant.BOOTROM);
		}
		return bootROMStatus;
	}

}
