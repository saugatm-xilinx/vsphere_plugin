package com.solarflare.vcp.services;

import java.net.URL;
import java.util.List;

import javax.cim.CIMInstance;
import javax.cim.CIMObjectPath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sblim.cimclient.internal.util.MOF;

import com.solarflare.vcp.cim.CIMConstants;
import com.solarflare.vcp.cim.SfCIMService;
import com.solarflare.vcp.model.Adapter;
import com.solarflare.vcp.model.AdapterTask;
import com.solarflare.vcp.model.FwType;
import com.solarflare.vcp.model.Status;
import com.solarflare.vcp.model.TaskInfo;
import com.solarflare.vcp.model.TaskState;
import com.solarflare.vcp.model.UpdateRequest;

public class CustomUpdateRequestThread implements Runnable {

	private static final Log logger = LogFactory.getLog(CustomUpdateRequestThread.class);
	private UpdateRequest updateRequest;
	private List<Adapter> adapterList;

	public List<Adapter> getAdapterList() {
		return adapterList;
	}

	public void setAdapterList(List<Adapter> adapterList) {
		this.adapterList = adapterList;
	}

	public UpdateRequest getUpdateRequest() {
		return updateRequest;
	}

	public void setUpdateRequest(UpdateRequest updateRequest) {
		this.updateRequest = updateRequest;
	}

	public void run() {
		SfCIMService cimService = updateRequest.getCimService();
		try {
                        // sleep added so that the calling HTTP thread can return
                        // task Id to user
			Thread.sleep(1000);
                        for (Adapter adapter : adapterList) {
			    updateRequestForAdapter(adapter);
			    CIMObjectPath fwInstance = updateRequest.getFwInstance();
			    setTaskState(TaskState.Running, null);

			    CIMObjectPath nicInstance = updateRequest.getNicInstance();
			    URL fwImagePath = updateRequest.getFwImagePath();
			    cimService.renewCimSession();
			    cimService.updateFirmwareFromURL(fwInstance, nicInstance, fwImagePath);

			    setTaskState(TaskState.Success, null);
		        }
		} catch (Exception e) {
	                String errorMsg = "Update request failed! Error : " + e.getMessage();
			logger.error(errorMsg);
			setTaskState(TaskState.Error, errorMsg);
		}

		// if temp file is created then remove it.
		boolean status = cimService.removeFwImage(updateRequest.getFwInstance(), updateRequest.getTempFilePath());
		logger.debug("Temp file : " + updateRequest.getTempFilePath() + " removed: " + status);
	}

	private void setTaskState(TaskState taskState, String error) {
		logger.info("Solarflare:: setTaskState TaskId " + updateRequest.getTaskId() + " taskState: " + taskState);
		// update task state as running
		Status status = new Status(taskState, error, updateRequest.getFwType());
		TaskManager taskManager = TaskManager.getInstance();
		taskManager.updateTaskState(updateRequest.getTaskId(), updateRequest.getAdapterId(), status);

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

	private void updateRequestForAdapter(Adapter adapter) throws Exception {
		String adapterId = adapter.getId();
		String taskID = updateRequest.getTaskId();
		AdapterTask aTask = getAdapterTask(TaskManager.getInstance().getTaskInfo(taskID), adapterId);

		CIMInstance fwInstance = null;
		SfCIMService cimService = updateRequest.getCimService();
		CIMInstance nicInstance = cimService.getNICCardInstance(adapter.getChildren().get(0).getName());
		// Check for controller version
		FwType fwType = updateRequest.getFwType();
		Status status = new Status(TaskState.Queued, null, fwType);
		if (FwType.CONTROLLER.equals(fwType)) {
			aTask.setController(status);
			fwInstance = cimService.getSoftwareInstallationInstance(CIMConstants.SVC_MCFW_NAME);
		} else if (FwType.BOOTROM.equals(fwType)) {
			aTask.setBootROM(status);
			fwInstance = cimService.getSoftwareInstallationInstance(CIMConstants.SVC_BOOTROM_NAME);
		} else if (FwType.UEFIROM.equals(fwType)) {
			aTask.setUefiROM(status);
			fwInstance = cimService.getSoftwareInstallationInstance(CIMConstants.SVC_UEFI_NAME);
		} else if (FwType.SUCFW.equals(fwType)) {
				aTask.setSucfw(status);
				fwInstance = cimService.getSoftwareInstallationInstance(CIMConstants.SVC_SUCFW_NAME);
			}
		updateRequest.setAdapterId(adapterId);
		updateRequest.setFwInstance(fwInstance.getObjectPath());
		updateRequest.setNicInstance(new CIMObjectPath(MOF.objectHandle(nicInstance.getObjectPath(), false, true)));

	}
}
