package com.solarflare.vcp.model;

import java.util.List;

public class TaskInfo {

	private String taskid;
	private String hostId;
	private List<AdapterTask> adapterTasks;

	public String getTaskid() {
		return taskid;
	}

	public void setTaskid(String taskid) {
		this.taskid = taskid;
	}

	
	public String getHostId() {
		return hostId;
	}

	public void setHostId(String hostId) {
		this.hostId = hostId;
	}

	public List<AdapterTask> getAdapterTasks() {
		return adapterTasks;
	}

	public void setAdapterTasks(List<AdapterTask> adapterTasks) {
		this.adapterTasks = adapterTasks;
	}

	
}
