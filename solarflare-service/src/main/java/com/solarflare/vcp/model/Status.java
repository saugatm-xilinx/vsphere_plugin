package com.solarflare.vcp.model;

public class Status {

	 private TaskState state;
	 private String errorMessage;
	 private Long timeStamp;
	 private FwType firmwareType; 
	
	
	public Status(TaskState state, String errorMessage, FwType firmwareType) {
		this.state = state;
		this.errorMessage = errorMessage;
		this.timeStamp = System.currentTimeMillis();
		this.firmwareType = firmwareType;
	}

	public Status(String state, String errorMessage, String firmwareType) {
		//this.state = state;
		this.errorMessage = errorMessage;
		this.timeStamp = System.currentTimeMillis();
	}

	public TaskState getState() {
		return state;
	}


	public void setState(TaskState state) {
		this.state = state;
	}


	public String getErrorMessage() {
		return errorMessage;
	}


	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}


	public Long getTimeStamp() {
		return timeStamp;
	}


	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}


	public FwType getFirmwareType() {
		return firmwareType;
	}


	public void setFirmwareType(FwType firmwareType) {
		this.firmwareType = firmwareType;
	}


	@Override
	public String toString() {
		return "Status [state=" + state + ", errorMessage=" + errorMessage + ", timeStamp=" + timeStamp
				+ ", firmwareType=" + firmwareType + "]";
	}
	
	
}
