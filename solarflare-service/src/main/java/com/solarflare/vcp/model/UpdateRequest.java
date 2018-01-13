package com.solarflare.vcp.model;

import java.net.URL;

import javax.cim.CIMObjectPath;

import com.solarflare.vcp.cim.SfCIMService;
import com.vmware.vim25.ServiceContent;

public class UpdateRequest {

	private boolean isCustom;
	private String hostId;
	private String adapterId;
	private URL fwImagePath;
	private FileHeader header;
	private CIMObjectPath nicInstance;
	private CIMObjectPath fwInstance;
	private ServiceContent serviceContent;
	private SfCIMService cimService;
	private String taskId;
	private FwType fwType;
	private String tempFilePath;
	
	public boolean isCustom() {
		return isCustom;
	}
	public void setCustom(boolean isCustom) {
		this.isCustom = isCustom;
	}

	public FwType getFwType() {
		return fwType;
	}
	public void setFwType(FwType fwType) {
		this.fwType = fwType;
	}
	public String getHostId() {
		return hostId;
	}
	public void setHostId(String hostId) {
		this.hostId = hostId;
	}
	public String getAdapterId() {
		return adapterId;
	}
	public void setAdapterId(String adapterId) {
		this.adapterId = adapterId;
	}
	public URL getFwImagePath() {
		return fwImagePath;
	}
	public void setFwImagePath(URL fwImagePath) {
		this.fwImagePath = fwImagePath;
	}
	public FileHeader getHeader() {
		return header;
	}
	public void setHeader(FileHeader header) {
		this.header = header;
	}
	public CIMObjectPath getNicInstance() {
		return nicInstance;
	}
	public void setNicInstance(CIMObjectPath nicInstance) {
		this.nicInstance = nicInstance;
	}
	public CIMObjectPath getFwInstance() {
		return fwInstance;
	}
	public void setFwInstance(CIMObjectPath fwInstance) {
		this.fwInstance = fwInstance;
	}
	public ServiceContent getServiceContent() {
		return serviceContent;
	}
	public void setServiceContent(ServiceContent serviceContent) {
		this.serviceContent = serviceContent;
	}

	public SfCIMService getCimService() {
		return cimService;
	}
	public void setCimService(SfCIMService cimService) {
		this.cimService = cimService;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	@Override
	public String toString() {
		return "UpdateRequest [isCustom=" + isCustom + ", hostId=" + hostId + ", adapterId=" + adapterId + ", fwImagePath=" + fwImagePath + ", header=" + header + ", nicInstance=" + nicInstance + ", fwInstance=" + fwInstance + ", serviceContent=" + serviceContent
				+ ", cimService=" + cimService + ", taskId=" + taskId + ", fwType=" + fwType + "]";
	}
	public String getTempFilePath() {
		return tempFilePath;
	}
	public void setTempFilePath(String tempFilePath) {
		this.tempFilePath = tempFilePath;
	}

}
