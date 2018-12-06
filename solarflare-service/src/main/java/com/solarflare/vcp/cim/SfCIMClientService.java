package com.solarflare.vcp.cim;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import javax.cim.CIMObjectPath;
import javax.cim.CIMProperty;
import javax.wbem.WBEMException;
import javax.wbem.client.UserPrincipal;
import javax.wbem.client.WBEMClient;
import javax.wbem.client.WBEMClientFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.solarflare.vcp.vim.SimpleTimeCounter;
import com.solarflare.vcp.vim.connection.Connection;
import com.solarflare.vcp.vim.connection.ConnectionImpl;
import com.vmware.vim25.HostServiceTicket;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;

public class SfCIMClientService {

	private CIMHost cimHost;

	public WBEMClient getCimClient() {
		return getClient(null);
	}

	public CIMHost getCimHost() {
		return cimHost;
	}

	public void setCimHost(CIMHost cimHost) {
		this.cimHost = cimHost;
	}

	public SfCIMClientService(CIMHost cimHost) {
		this.cimHost = cimHost;
	}
	
	private static final Log logger = LogFactory.getLog(SfCIMClientService.class);

	public WBEMClient getClient(String cimObjectName) {
		return getClient(cimObjectName, null);
	}

	
	public WBEMClient getClient(String cimObjectName, CIMProperty<?>[] properties) {
		javax.security.auth.Subject subject = createSubjectFromHost(cimHost);
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: getClient");
		try {
			WBEMClient client = WBEMClientFactory.getClient(CIMConstants.WBEMCLIENT_FORMAT);
			client.initialize(getHostObjectPath(properties, cimObjectName), subject,
					Locale.getAvailableLocales());
			timer.stop();
			return client;
		} catch (WBEMException e) {
			logger.error("Failed to initialize WBEMClient!" + e.getMessage());
		}
		timer.stop();
		return null;
	}
	
	public CIMObjectPath getHostObjectPath(CIMProperty<?>[] properties, String cimObjectName) {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: getHostObjectPath");
		try {
			URL UrlHost = new URL(cimHost.getUrl());
			CIMObjectPath cimObjPath = new CIMObjectPath(UrlHost.getProtocol(), UrlHost.getHost(), String.valueOf(UrlHost.getPort()),
					CIMConstants.CIM_NAMESPACE, cimObjectName, properties); 
			timer.stop();
			return cimObjPath;
		} catch (MalformedURLException e) {
			logger.error("Invalid URL '" + cimHost.getUrl() + "'." + e.getMessage());
		}
		timer.stop();
		return null;
	}
	
	private javax.security.auth.Subject createSubjectFromHost(CIMHost cimHost) {
		String user = "";
		String password = "";
		javax.security.auth.Subject subject = new javax.security.auth.Subject();

		if (cimHost instanceof CIMHostUser) {
			user = ((CIMHostUser) cimHost).getUsername();
			password = ((CIMHostUser) cimHost).getPassword();
		} else if (cimHost instanceof CIMHostSession) {
			user = ((CIMHostSession) cimHost).getSessionId();
			password = user;
		}

		subject.getPrincipals().add(new UserPrincipal(user));
		subject.getPrivateCredentials().add(new PasswordCredential(password));

		return subject;
	}

	/**
	 * The default PasswordCredential will prevent us from using sessionId's
	 * that can be over 16 characters in length. Instead use inheritance to
	 * force the PasswordCredential class to hold values longer than 16 chars.
	 * <p>
	 * 
	 * @see javax.wbem.client.PasswordCredential
	 */
	public static class PasswordCredential extends javax.wbem.client.PasswordCredential {
		private final String longPassword;

		public PasswordCredential(String userPassword) {
			super("fake password"); // the parent class' password is ignored
			longPassword = userPassword;
		}

		@Override
		public char[] getUserPassword() {
			return longPassword.toCharArray(); // use our long password instead
		}
	}

	public void renewSessionId() throws Exception {
		String clientId = cimHost.getClientId();
		Connection conn = ConnectionImpl.getConnectionByClientId(clientId);
		HostServiceTicket ticket = conn.getVimPort()
				.acquireCimServicesTicket(getManagedObjectReference("HostSystem", cimHost.getHostId()));
		((CIMHostSession)cimHost).setSessionId(ticket.getSessionId());
	}

	private ManagedObjectReference getManagedObjectReference(String type, String value) {
		ManagedObjectReference managedObjectReference = new ManagedObjectReference();
		managedObjectReference.setType(type);
		managedObjectReference.setValue(value);
		return managedObjectReference;
	}

}
