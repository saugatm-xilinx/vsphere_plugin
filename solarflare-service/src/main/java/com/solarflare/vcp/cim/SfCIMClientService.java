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

public class SfCIMClientService {

	private static final Log logger = LogFactory.getLog(SfCIMClientService.class);

	public WBEMClient getClient(CIMHost cimHost, String cimObjectName) {
		return getClient(cimHost, cimObjectName, null);
	}

	public WBEMClient getClient(CIMHost cimHost, String cimObjectName, CIMProperty<?>[] properties) {
		javax.security.auth.Subject subject = createSubjectFromHost(cimHost);

		try {
			WBEMClient client = WBEMClientFactory.getClient(CIMConstants.WBEMCLIENT_FORMAT);
			client.initialize(getHostObjectPath(cimHost, properties, cimObjectName), subject,
					Locale.getAvailableLocales());
			return client;
		} catch (WBEMException e) {
			logger.error("Failed to initialize WBEMClient!" + e.getMessage());
		}
		return null;
	}

	private CIMObjectPath getHostObjectPath(CIMHost cimHost, CIMProperty<?>[] properties, String cimObjectName) {
		try {
			URL UrlHost = new URL(cimHost.getUrl());
			return new CIMObjectPath(UrlHost.getProtocol(), UrlHost.getHost(), String.valueOf(UrlHost.getPort()),
					CIMConstants.CIM_NAMESPACE, cimObjectName, properties);
		} catch (MalformedURLException e) {
			logger.error("Invalid URL '" + cimHost.getUrl() + "'." + e.getMessage());
		}
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

}
