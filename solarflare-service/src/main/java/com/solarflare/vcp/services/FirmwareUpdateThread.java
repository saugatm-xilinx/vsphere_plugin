package com.solarflare.vcp.services;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.cim.CIMInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.solarflare.vcp.cim.CIMHost;
import com.solarflare.vcp.cim.CIMService;
import com.solarflare.vcp.model.Status;

public class FirmwareUpdateThread implements Runnable {
	private static final Log logger = LogFactory.getLog(HostAdapterServiceImpl.class);
	String adapterName;
	CIMHost cimHost;
	URL fwImagePath;
	String data;
	CIMService cim;
	CIMInstance fw_inst;
	CIMInstance nicInstance;
	public static Map<String, Status> statusMap = new ConcurrentHashMap<>();

	FirmwareUpdateThread(CIMService cim, CIMHost cimHost, URL fwImagePath, String data, CIMInstance fw_inst,
			CIMInstance nicInstance, String adapterName) {
		this.adapterName = adapterName;
		this.cim = cim;
		this.cimHost = cimHost;
		this.fwImagePath = fwImagePath;
		this.data = data;
		this.fw_inst = fw_inst;
		this.nicInstance = nicInstance;
	}

	@Override
	public void run() {
		logger.debug("Updating firmware for adapter : " + adapterName);
		// CIMInstance nicInstance = null;
		try {

			// Validate before update
			setStatus(UploadStatusEnum.VALIDATING.toString(), null);
			byte[] bytes = null;
			if(data != null && !data.isEmpty()) {
				bytes = data.getBytes();
			}else{
				boolean readComplete = false;
				bytes = cim.readData(fwImagePath, readComplete);
			}
			boolean isCompatable = cim.isCustomFWImageCompatible(cimHost, fw_inst, nicInstance, bytes);
			if (isCompatable) {
				setStatus(UploadStatusEnum.VALIDATED.toString(), null);
				setStatus(UploadStatusEnum.UPLOADING.toString(), null);
				boolean res = cim.updateFirmwareFromURL(fw_inst.getObjectPath(), cimHost, nicInstance,
						fwImagePath);
				if (res) {
					setStatus(UploadStatusEnum.UPLOADED.toString(), null);
					logger.debug("Contoller firmware update for adapter '" + adapterName + "' is done.");
				} else {
					setStatus(UploadStatusEnum.UPLOADING_FAIL.toString(), MessageConstant.UPLOAD_FAIL);
					logger.debug("Contoller firmware update for adapter '" + adapterName + "' is falied");
				}

			} else {
				setStatus(UploadStatusEnum.VALIDATION_FAIL.toString(), MessageConstant.VALIDATION_FAIL);
			}
		} catch (Exception e) {
			logger.error("Exception while uploading binary file for adapter " + adapterName + ":" + e.getMessage());
			setStatus(UploadStatusEnum.UPLOADING_FAIL.toString(), e.getMessage());
		}
	}

	private void setStatus(String uploadStatus, String message) {
		Status status = new Status();
		status.setAdapter(adapterName);
		status.setMessage(message);
		status.setStatus(uploadStatus);
		if (statusMap.get(adapterName) != null) {
			statusMap.replace(adapterName, status);
		} else {
			statusMap.put(adapterName, status);
		}
	}

}
