package com.solarflare.vcp.services;

import java.net.URL;

import javax.cim.CIMObjectPath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.solarflare.vcp.cim.SfCIMService;
import com.solarflare.vcp.model.Status;
import com.solarflare.vcp.model.TaskState;
import com.solarflare.vcp.model.UpdateRequest;

public class UpdateRequestThread implements Runnable {

	private static final Log logger = LogFactory.getLog(UpdateRequestThread.class);
	private UpdateRequest updateRequest;

	public UpdateRequest getUpdateRequest() {
		return updateRequest;
	}

	public void setUpdateRequest(UpdateRequest updateRequest) {
		this.updateRequest = updateRequest;
	}

	public void run() {
		logger.info("Solarflare:: UpdateRequestThread run method started.");
		logger.debug("Solarflare:: updateRequest in thread : "+updateRequest);
		
		//TODO : Review Comment : check for null
		SfCIMService cimService = updateRequest.getCimService();
		CIMObjectPath fwInstance = updateRequest.getFwInstance();

		try {
			setTaskState(TaskState.Running, null);

			CIMObjectPath nicInstance = updateRequest.getNicInstance();
			URL fwImagePath = updateRequest.getFwImagePath();
			cimService.updateFirmwareFromURL(fwInstance, nicInstance, fwImagePath);

			setTaskState(TaskState.Success, null);

		} catch (Exception e) {
			String errorMsg = "Update request failed! Error : " + e.getMessage();
			logger.error(errorMsg);
			setTaskState(TaskState.Error, errorMsg);
		} finally {
			// if temp file is created then remove it.
			if (updateRequest.isCustom() && updateRequest.getTempFilePath() != null && !updateRequest.getTempFilePath().isEmpty()) { // Delete
				boolean status = cimService.removeFwImage(fwInstance, updateRequest.getTempFilePath());
				logger.debug("Temp file : " + updateRequest.getTempFilePath() + " removed: " + status);
			}
		}
	}

	private void setTaskState(TaskState taskState, String error) {
		logger.info("Solarflare:: setTaskState TaskId " + updateRequest.getTaskId() + " taskState: " + taskState);
		// update task state as running
		Status status = new Status(taskState, error, updateRequest.getFwType());
		TaskManager taskManager = TaskManager.getInstance();
		taskManager.updateTaskState(updateRequest.getTaskId(), updateRequest.getAdapterId(), status);

	}
}
