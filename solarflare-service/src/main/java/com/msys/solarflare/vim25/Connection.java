package com.msys.solarflare.vim25;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.xml.ws.BindingProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.vmware.vim25.InvalidLocaleFaultMsg;
import com.vmware.vim25.InvalidLoginFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VimService;
import com.vmware.vise.usersession.ServerInfo;
import com.vmware.vise.usersession.UserSession;
import com.vmware.vise.usersession.UserSessionService;

public class Connection {
	private static final Log _logger = LogFactory.getLog(Connection.class);

	private static final String SERVICE_INSTANCE = "ServiceInstance";
	String url = "https://10.101.10.7/sdk";
	String user = "msys@vsphere.local";
	 // String password = "Ibmx#3750c";
	 String password = "Msys@123";

	/** object for access to all of the methods defined in the vSphere API */
	private static VimPortType _vimPort = initializeVimPort();
	
	//@Autowired
//	private UserSessionService userSessionService;
//	public Connection() {}
//	public Connection(UserSessionService userSessionService)
//	{
//		this.userSessionService = userSessionService;
//		UserSession usersession = userSessionService.getUserSession();
//		//usersession.serversInfo = new  ServerInfo[10];;
//	}

	private static VimPortType initializeVimPort() {
		// Static initialization is preferred because it takes a few seconds.
		VimService vimService = new VimService();
		return vimService.getVimPort();
	}

	public VimPortType getVimPort() {
		return _vimPort;
	}

	/**
	 * Get the ServiceContent for a given VC server.
	 */
	public ServiceContent getServiceContent() {
		/*
		 * ServerInfo serverInfoObject = getServerInfoObject(serverGuid); String
		 * sessionCookie = serverInfoObject.sessionCookie; String serviceUrl =
		 * serverInfoObject.serviceUrl;
		 * 
		 * if (_logger.isDebugEnabled()) {
		 * _logger.debug("getServiceContent: sessionCookie = " + sessionCookie +
		 * ", serviceUrl= " + serviceUrl); }
		 * 
		 * List<String> values = new ArrayList<String>();
		 * values.add("vmware_soap_session=" + sessionCookie); Map<String,
		 * List<String>> reqHeadrs = new HashMap<String, List<String>>();
		 * reqHeadrs.put("Cookie", values);
		 */
		Map<String, Object> reqContext = ((BindingProvider) _vimPort).getRequestContext();
		reqContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
		reqContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
		// reqContext.put(MessageContext.HTTP_REQUEST_HEADERS, reqHeadrs);

		final ManagedObjectReference svcInstanceRef = new ManagedObjectReference();
		svcInstanceRef.setType(SERVICE_INSTANCE);
		svcInstanceRef.setValue(SERVICE_INSTANCE);

		ServiceContent serviceContent = null;
		try {
			// TODO: Used for development only
			DisableSecurity.trustEveryone();
			//UserSession user2 = userSessionService.getUserSession();
			serviceContent = _vimPort.retrieveServiceContent(svcInstanceRef);
			_vimPort.login(serviceContent.getSessionManager(), user, password, null);
		} catch (RuntimeFaultFaultMsg | KeyManagementException | NoSuchAlgorithmException | InvalidLocaleFaultMsg | InvalidLoginFaultMsg e) {
			_logger.error("getServiceContent error: " + e);
		}

		return serviceContent;
	}

	public void disconnect(ServiceContent serviceContent) {
		try {
			_vimPort.logout(serviceContent.getSessionManager());
		} catch (RuntimeFaultFaultMsg e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
