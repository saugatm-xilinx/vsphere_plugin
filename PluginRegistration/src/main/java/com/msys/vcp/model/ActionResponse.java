package com.msys.vcp.model;

import org.springframework.stereotype.Component;

@Component
public class ActionResponse {
	private Boolean status;
	private String message;

	public ActionResponse() {
	}

	public ActionResponse(Boolean status, String message) {
		this.status = status;
		this.message = message;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "ActionStatus [status=" + status + ", message=" + message + "]";
	}
}
