package com.msys.solarflare.cim;

public class CIMHostUser extends CIMHost {
	private String	_username	= null;
	private String	_password	= null;

	public CIMHostUser() {
		this(null, null, null);
	}

	public CIMHostUser(String url, String username, String password) {
		super(url);
		setUsername(username);
		setPassword(password);
	}

	public String getUsername() {
		return _username;
	}

	public void setUsername(String username) {
		this._username = username;
	}

	public String getPassword() {
		return _password;
	}

	public void setPassword(String password) {
		this._password = password;
	}

	@Override
	public boolean isValid() {
		return getUrl() != null && getUsername() != null && getPassword() != null;
	}
}