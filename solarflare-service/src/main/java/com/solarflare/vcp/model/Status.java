package com.solarflare.vcp.model;

public class Status {
	String status;
	String message;
	Long timeStamp;
	String type;
	public Status() {
	}

	public Status(String status, String message, String type) {
		this.status = status;
		this.message = message;
		this.timeStamp = System.currentTimeMillis();
		this.type = type;
	}

	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	@Override
	public String toString() {
		return "Status [status=" + status + ", message=" + message + ", timeStamp=" + timeStamp + ", type=" + type
				+ "]";
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
