package com.solarflare.vcp.services;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

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

public class FirmwareUpdateThread implements Callable<Void> {
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
	public Void call() {
		logger.info("Updating firmware for adapter : " + adapterId);
		try {

			logger.info("Processing .....");
			process();
			logger.info("Processing done.");
		} catch (Exception e) {
			logger.error("Exception while uploading binary file for adapter " + adapterId + ":" + e.getMessage());

		}
		return null;
	}

	public void process() throws Exception {
		Status controllerStatus = null;
		Status bootROMStatus = null;

		if (controller) {
			controllerStatus = updateController();
		}
		if (bootrom) {
			bootROMStatus = updateBootROM();
		}

		AdapterTask aTask = new AdapterTask();
		aTask.setAdapterId(adapterId);
		aTask.setController(controllerStatus);
		aTask.setBootROM(bootROMStatus);

		taskInfo.add(aTask);
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
		try {

			controllerStatus = new Status(TaskState.Queued, null, FwType.CONTROLLER);

			logger.info("Start updating controller for adapterId : " + adapterId);
			CIMInstance svc_mcfw_inst = cim.getFirmwareSoftwareInstallationInstance(cimHost);
			if (!isCustom) {
				fwImagePath = getImageURL(svc_mcfw_inst, true);

				byte[] bytes = null;
				boolean readComplete = false;
				bytes = cim.readData(fwImagePath, readComplete);
				header = cim.getFileHeader(bytes);
			}

			controllerStatus.setState(TaskState.Running);

			boolean isCompatable = cim.isCustomFWImageCompatible(cimHost, svc_mcfw_inst, this.nicInstance, header);
			logger.info("Is firmware image compatable : " + isCompatable);
			if (isCompatable) {

				boolean res = cim.updateFirmwareFromURL(svc_mcfw_inst.getObjectPath(), cimHost, this.nicInstance,
						fwImagePath);
				if (res) {
					logger.info("Contoller firmware update for adapter '" + adapterId + "' is Successfull.");
					controllerStatus.setState(TaskState.Success);
				} else {
					logger.info("Contoller firmware update for adapter '" + adapterId + "' is failed");
					controllerStatus.setState(TaskState.Error);
					controllerStatus.setErrorMessage("Fail to update Controller firmware image");
				}

			} else {
				logger.info("Firmware Image is not compatable");
				controllerStatus.setState(TaskState.Error);
				controllerStatus.setErrorMessage("Controller firmware image is not compatable");
			}
		} catch (Exception e) {
			logger.error("Error in updating controller firmware, error " + e.getMessage());
			controllerStatus.setState(TaskState.Error);
			controllerStatus.setErrorMessage(e.getMessage());
		}
		return controllerStatus;
	}

	private synchronized Status updateBootROM() throws Exception {
		logger.info("Start updating BootROM for adapterId : " + adapterId);
		Status bootROMStatus = null;
		
		try {
			bootROMStatus = new Status(TaskState.Queued, null, FwType.BOOTROM);
			CIMInstance svc_bootrom_inst = cim.getBootROMSoftwareInstallationInstance(cimHost);
			if (!isCustom) {
				fwImagePath = getImageURL(svc_bootrom_inst, false);

				byte[] bytes = null;
				boolean readComplete = false;
				bytes = cim.readData(fwImagePath, readComplete);
				header = cim.getFileHeader(bytes);
			}

			bootROMStatus.setState(TaskState.Running);
			boolean isCompatable = cim.isCustomFWImageCompatible(cimHost, svc_bootrom_inst, this.nicInstance, header);
			logger.info("Is firmware image compatable : "+isCompatable);
			if (isCompatable) {
				boolean res = cim.updateFirmwareFromURL(svc_bootrom_inst.getObjectPath(), cimHost, nicInstance,
						fwImagePath);
				if (res) {
					logger.info("BootROM firmware update for adapter '" + adapterId + "' is Successfull.");
					bootROMStatus.setState(TaskState.Success);
					
				} else {
					logger.info("BootROM firmware update for adapter '" + adapterId + "' is failed");
					bootROMStatus.setState(TaskState.Error);
					bootROMStatus.setErrorMessage("Fail to update BootROM firmware image");
				}

			} else {
				logger.info("Firmware Image is not compatable");
				bootROMStatus.setState(TaskState.Error);
				bootROMStatus.setErrorMessage("BootROM firmware image is not compatable");
			}
		} catch (Exception e) {
			logger.error("Error in updating Boot ROM firmware, error " + e.getMessage());
			bootROMStatus.setState(TaskState.Error);
			bootROMStatus.setErrorMessage(e.getMessage());
		}
		return bootROMStatus;
	}

}
