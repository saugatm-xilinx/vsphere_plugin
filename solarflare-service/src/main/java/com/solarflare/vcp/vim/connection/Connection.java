package com.solarflare.vcp.vim.connection;

import java.net.URL;

import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.UserSession;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VimService;
import com.vmware.vise.usersession.UserSessionService;

public interface Connection {

	void setUsername(String username);

	String getUsername();

	void setPassword(String password);

	VimService getVimService();

	VimPortType getVimPort();

	ServiceContent getServiceContent();

	UserSession getUserSession();

	Connection connect(UserSessionService usersessionService, boolean ignoreCert);

	boolean isConnected();

	void disconnect(String clientId);

	URL getURL();

}
