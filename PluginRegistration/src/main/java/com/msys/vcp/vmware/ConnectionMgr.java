package com.msys.vcp.vmware;

import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.msys.vcp.model.ConnectionDAO;
import com.msys.vcp.utils.TrustAllTrustManager;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VimService;

@Service
public class ConnectionMgr {
	private static Logger logger = LoggerFactory.getLogger(ConnectionDAO.class);
	private static final ManagedObjectReference SVC_INST_REF = new ManagedObjectReference();
	/**
	 * Name and Type of Service instance
	 */
	private static final String SVC_INST_NAME = "ServiceInstance";

	VimPortType _vimPort;
	private VimService _vimService;
	private ServiceContent _serviceContent;
	private boolean _isConnected = false;
	private String apiType;
	private String apiVersion;

	public ConnectionMgr() {

	}

	public VimPortType vimPort() {
		return _vimPort;
	}

	public String getApiType() {
		return apiType;
	}

	public void setApiType(String apiType) {
		this.apiType = apiType;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public ServiceContent getServiceContent(ConnectionDAO conn) throws Exception {
		// connect it to vCenter.
		connect(conn);
		return _serviceContent;
	}

	public void connect(String url, String username, String password) throws Exception {
		ConnectionDAO conn = new ConnectionDAO(url, username, password);
		connect(conn);
	}

	/**
	 * Establishes user session with the vCenter server.
	 *
	 * @throws Exception
	 */
	public void connect(ConnectionDAO conn) throws Exception {
		logger.info("Connecting with connection info:  {}", conn);
		if (!_isConnected) {

			HostnameVerifier hv = new HostnameVerifier() {
				public boolean verify(String urlHostName, SSLSession session) {
					return true;
				}
			};
			TrustAllTrustManager.trustAllHttpsCertificates();
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
			// validate connection
			conn.validateURL();

			SVC_INST_REF.setType(SVC_INST_NAME);
			SVC_INST_REF.setValue(SVC_INST_NAME);

			_vimService = new VimService();
			_vimPort = _vimService.getVimPort();
			Map<String, Object> ctxt = ((BindingProvider) _vimPort).getRequestContext();

			ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, conn.getUrl());
			ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

			_serviceContent = _vimPort.retrieveServiceContent(SVC_INST_REF);
			_vimPort.login(_serviceContent.getSessionManager(), conn.getUsername(), conn.getPassword(), null);
			apiType = _serviceContent.getAbout().getApiType();
			apiVersion = _serviceContent.getAbout().getApiVersion();
			_isConnected = true;
		}
	}

	/**
	 * Disconnects the user session.
	 *
	 * @throws Exception
	 */
	public void disconnect() throws Exception {
		if (_isConnected) {
			_vimPort.logout(_serviceContent.getSessionManager());
		}
		_isConnected = false;
	}

	public static void main(String[] args) throws Exception {
		new ConnectionMgr().connect("https://192.168.102.198", "root", "Msys@123");
	}
}
