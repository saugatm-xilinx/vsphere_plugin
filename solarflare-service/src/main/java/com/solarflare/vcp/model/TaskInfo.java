package com.solarflare.vcp.model;

import java.util.ArrayList;
import java.util.List;

public class TaskInfo {

	private String taskid;
	private String hostId;
	private Long requestSubmitted;
	private List<AdapterTask> adapterTasks;

	public TaskInfo() {
		this.requestSubmitted = System.currentTimeMillis();
	}
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

	public void add(AdapterTask aTask) {
		if (this.adapterTasks == null) {
			this.adapterTasks = new ArrayList<AdapterTask>();
		}
		this.adapterTasks.add(aTask);
	}

	public Long getRequestSubmitted() {
		return requestSubmitted;
	}
	@Override
	public String toString() {
		return "TaskInfo [taskid=" + taskid + ", hostId=" + hostId + ", requestSubmitted=" + requestSubmitted + ", adapterTasks=" + adapterTasks + "]";
	}

}
