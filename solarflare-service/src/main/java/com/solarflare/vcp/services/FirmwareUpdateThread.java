package com.solarflare.vcp.services;

import java.net.URL;

import javax.cim.CIMInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.solarflare.vcp.cim.CIMConstants;
import com.solarflare.vcp.cim.CIMHost;
import com.solarflare.vcp.cim.CIMService;
import com.solarflare.vcp.helper.MetadataHelper;
import com.solarflare.vcp.helper.VCenterHelper;
import com.solarflare.vcp.model.SfFirmware;
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
	String data;
	CIMService cim;
	CIMInstance nicInstance;
	ServiceContent serviceContent;
	private String currentProcess ;

	MetadataHelper metadataHelper = new MetadataHelper();

	FirmwareUpdateThread(ServiceContent serviceContent, CIMService cim, CIMHost cimHost, URL fwImageURL, String data,
			CIMInstance nicInstance, String adapterId, String hostId, boolean isCustom, boolean controller, boolean bootrom) {
		this.adapterId = adapterId;
		this.cim = cim;
		this.cimHost = cimHost;
		this.data = data;
		this.nicInstance = nicInstance;
		this.serviceContent = serviceContent;
		this.fwImagePath = fwImageURL;
		this.isCustom = isCustom;
		this.controller = controller;
		this.bootrom = bootrom;
		this.hostId = hostId;
	}

	@Override
	public void run() {
		logger.debug("Updating firmware for adapter : " + adapterId);
		try {

			logger.info("Processing .....");
			process();
			logger.info("Processing done.");
//			if (TaskStatus.getTaskStatus(adapterId) == null || TaskStatus.getTaskStatus(adapterId).getStatus().equals(UploadStatusEnum.DONE.toString())) {
//				TaskStatus.updateTaskStatus(adapterId, UploadStatusEnum.VALIDATING.toString(), null);
//				
//				//process();
//			} else {
////				logger.info("Processing .....");
//			}
		} catch (Exception e) {
			logger.error("Exception while uploading binary file for adapter " + adapterId + ":" + e.getMessage());
			if(MessageConstant.CONTROLLER.equals(currentProcess))
			{
				TaskStatus.updateTaskStatus(adapterId, UploadStatusEnum.UPLOADING_FAIL.toString(), e.getMessage(), MessageConstant.CONTROLLER);
			}
			else if(MessageConstant.BOOTROM.equals(currentProcess))
			{
				TaskStatus.updateTaskStatus(adapterId, UploadStatusEnum.UPLOADING_FAIL.toString(), e.getMessage(), MessageConstant.BOOTROM);
			}
			
		}
	}

	private void process() throws Exception {
		if (isCustom) {
			if (controller) {
				updateController();
			}
			if (bootrom) {
				updateBootROM();
			}

		} else {
			if (controller) {
				updateController();
			}
			if (bootrom) {
				updateBootROM();
			}
		}
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

	private void updateController() throws Exception {
		logger.info("Start updating controller for adapterId : " + adapterId);
		currentProcess = MessageConstant.CONTROLLER;
		String statusId = VCenterHelper.generateId(hostId,adapterId, MessageConstant.CONTROLLER);
		CIMInstance svc_mcfw_inst = cim.getFirmwareSoftwareInstallationInstance(cimHost);
		if (!isCustom) {
			fwImagePath = getImageURL(svc_mcfw_inst, true);
		}

		byte[] bytes = null;

		boolean readComplete = false;
		bytes = cim.readData(fwImagePath, readComplete);
		logger.info("bytes length :" + bytes.length);

		TaskStatus.updateTaskStatus(statusId, UploadStatusEnum.VALIDATING.toString(), "Controller firmware image validation in-progress" , MessageConstant.CONTROLLER);
		logger.info("Adapter Id :"+statusId + " , controller "+UploadStatusEnum.VALIDATING.toString());
		boolean isCompatable = cim.isCustomFWImageCompatible(cimHost, svc_mcfw_inst, this.nicInstance, bytes);
		if (isCompatable) {
			logger.info("Adapter Id :"+statusId + " , controller "+UploadStatusEnum.VALIDATED.toString());
			TaskStatus.updateTaskStatus(statusId, UploadStatusEnum.VALIDATED.toString(), "Controller firmware image validated successfully", MessageConstant.CONTROLLER);
			logger.info("Adapter Id :"+statusId + " , controller "+UploadStatusEnum.UPLOADING.toString());
			TaskStatus.updateTaskStatus(statusId, UploadStatusEnum.UPLOADING.toString(), "Controller firmware image file uploading in-progress", MessageConstant.CONTROLLER);
			
			boolean res = cim.updateFirmwareFromURL(svc_mcfw_inst.getObjectPath(), cimHost, nicInstance, fwImagePath);
			logger.info("Result after upload is : "+res);
			if (res) {
				TaskStatus.updateTaskStatus(statusId, UploadStatusEnum.DONE.toString(), "Controller firmware image update is done", MessageConstant.CONTROLLER);
				logger.info("Contoller firmware update for adapter '" + statusId + "' is done.");
			} else {
				TaskStatus.updateTaskStatus(statusId, UploadStatusEnum.UPLOADING_FAIL.toString(), "Fail to update Controller firmware image", MessageConstant.CONTROLLER);
				logger.info("Contoller firmware update for adapter '" + statusId + "' is failed");
			}

		} else {
			TaskStatus.updateTaskStatus(statusId, UploadStatusEnum.VALIDATION_FAIL.toString(), "Controller firmware image is not compatable", MessageConstant.CONTROLLER);
			logger.info("Image not compatable");
		}
		currentProcess = "";
	}

	private void updateBootROM() throws Exception {
		logger.info("Starting bootrom update ");
		currentProcess = MessageConstant.BOOTROM;
		String statusId = VCenterHelper.generateId(hostId,adapterId, MessageConstant.BOOTROM);
		CIMInstance svc_bootrom_inst = cim.getBootROMSoftwareInstallationInstance(cimHost);
		if (!isCustom) {
			fwImagePath = getImageURL(svc_bootrom_inst, false);
		}
		byte[] bytes = null;

		boolean readComplete = false;
		bytes = cim.readData(fwImagePath, readComplete);
		logger.info("bytes length :" + bytes.length);

		TaskStatus.updateTaskStatus(statusId, UploadStatusEnum.VALIDATING.toString(), "BootROM firmware image validation in-progress", MessageConstant.BOOTROM);
		logger.info("Adapter Id :"+statusId + " , bootrom "+UploadStatusEnum.VALIDATING.toString());
		boolean isCompatable = cim.isCustomFWImageCompatible(cimHost, svc_bootrom_inst, this.nicInstance, bytes);
		
		if (isCompatable) {
			logger.info("Adapter Id :"+statusId + " , bootrom "+UploadStatusEnum.VALIDATED.toString());
			TaskStatus.updateTaskStatus(statusId, UploadStatusEnum.VALIDATED.toString(), "BootROM firmware image validated successfully", MessageConstant.BOOTROM);
			logger.info("Adapter Id :"+statusId + " , bootrom "+UploadStatusEnum.UPLOADING.toString());
			TaskStatus.updateTaskStatus(statusId, UploadStatusEnum.UPLOADING.toString(), "BootROM firmware image file uploading in-progress", MessageConstant.BOOTROM);
			
			boolean res = cim.updateFirmwareFromURL(svc_bootrom_inst.getObjectPath(), cimHost, nicInstance,
					fwImagePath);
			if (res) {
				TaskStatus.updateTaskStatus(statusId, UploadStatusEnum.DONE.toString(), "BootROM firmware image update is done", MessageConstant.BOOTROM);
				logger.debug("BootROM firmware update for adapter '" + statusId + "' is done.");
			} else {
				TaskStatus.updateTaskStatus(statusId, UploadStatusEnum.UPLOADING_FAIL.toString(), "Fail to update BootROM firmware image", MessageConstant.BOOTROM);
				logger.debug("BootROM firmware update for adapter '" + statusId + "' is failed");
			}

		} else {
			TaskStatus.updateTaskStatus(statusId, UploadStatusEnum.VALIDATION_FAIL.toString(),	"BootROM firmware image is not compatable", MessageConstant.BOOTROM);
		}
	}

}
