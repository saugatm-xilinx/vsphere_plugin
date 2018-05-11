package com.msys.vcp.model;

import org.springframework.stereotype.Component;

@Component
public class ActionRequest {
	private ConnectionDAO connection;
	private String action;

	public ConnectionDAO getConnection() {
		return connection;
	}

	public void setConnection(ConnectionDAO connection) {
		this.connection = connection;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	@Override
	public String toString() {
		return "ActionRequest [connection=" + connection + ", action=" + action + "]";
	}
}
