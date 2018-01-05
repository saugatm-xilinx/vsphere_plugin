package com.solarflare.vcp.services;

import java.net.URL;
import java.util.List;

import javax.cim.CIMInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.solarflare.vcp.cim.SfCIMService;
import com.solarflare.vcp.model.AdapterTask;
import com.solarflare.vcp.model.FwType;
import com.solarflare.vcp.model.Status;
import com.solarflare.vcp.model.TaskInfo;
import com.solarflare.vcp.model.TaskState;
import com.solarflare.vcp.model.UpdateRequest;
import com.sun.media.jfxmedia.logging.Logger;

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
		AdapterTask aTask = null;
		try {

			TaskInfo taskInfo = updateRequest.getTaskInfo();
			aTask = getRunningAdapterTask(taskInfo.getAdapterTasks(),updateRequest.getAdapterId());
			setTaskState(aTask,TaskState.Running,null,updateRequest.getFwType());
			
			SfCIMService cimService = updateRequest.getCimService();
			CIMInstance fwInstance = updateRequest.getFwInstance();
			CIMInstance nicInstance = updateRequest.getNicInstance();
			URL fwImagePath = updateRequest.getFwImagePath();

			cimService.updateFirmwareFromURL(fwInstance, nicInstance, fwImagePath);
				
			setTaskState(aTask,TaskState.Success,null,updateRequest.getFwType());
			
			
		} catch (Exception e) {
			String errorMsg = "Error updating firmware , error : "+e.getMessage(); 
			logger.error(errorMsg);
			setTaskState(aTask,TaskState.Error,errorMsg,updateRequest.getFwType());
		}
	}
	
	private void setTaskState(AdapterTask aTask, TaskState taskState, String error, FwType fwType){
		Status status = new Status(taskState,error,fwType);
		
		if(FwType.CONTROLLER.equals(fwType)){
			aTask.setController(status);
		}
		
		if(FwType.BOOTROM.equals(fwType)){
			aTask.setBootROM(status);
		}
		
		if(FwType.UEFIROM.equals(fwType)){
			aTask.setUefiROM(status);
		}
		
	}
	private AdapterTask getRunningAdapterTask(List<AdapterTask> adapterTasks, String adapterId){
		for(AdapterTask aTask : adapterTasks){
			if(aTask.getAdapterId().equals(adapterId)){
				return aTask;
			}
		}
		return null;
	}
}
