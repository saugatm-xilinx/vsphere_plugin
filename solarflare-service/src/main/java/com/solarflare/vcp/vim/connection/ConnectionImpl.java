package com.solarflare.vcp.vim.connection;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vmware.vim25.InvalidLocaleFaultMsg;
import com.vmware.vim25.InvalidLoginFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.UserSession;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VimService;
import com.vmware.vise.usersession.ServerInfo;
import com.vmware.vise.usersession.UserSessionService;

public class ConnectionImpl implements Connection {
	private static final Log logger = LogFactory.getLog(ConnectionImpl.class);
	private static final String LOG_KEY = "Solarflare:: ";

	public static final String SVC_INST_NAME = "ServiceInstance";
	private static ManagedObjectReference svcInstRef;

	private VimPortType vimPort;
	private VimService vimService;
	private ServiceContent serviceContent;
	private UserSession userSession;
	private static LinkedList<ConnectionImpl> sessionList = new LinkedList<>();
	// sessionListLock must be held while sessionList is
	// being manipulated.
	private static final ReentrantLock sessionListLock = new ReentrantLock();

	private URL url;
	private String username;
	private String password;
	private boolean ignoreCert;
	private String sessionCookie;
	private String sessionKey;
	private volatile String clientId;

	// initializeVimPort
	static {
		svcInstRef = new ManagedObjectReference();
		svcInstRef.setType(SVC_INST_NAME);
		svcInstRef.setValue(SVC_INST_NAME);
	}

	public ConnectionImpl() {
		logger.info(LOG_KEY + "Default ConnectionImpl created. " + this);
	}

	private ConnectionImpl(String clientId, String sessionCookie) {
		// This is taking some time so create it this time.
		this.vimService = new VimService();
		this.vimPort = vimService.getVimPort();
		this.sessionCookie = sessionCookie;
		this.clientId = clientId;
		sessionListLock.lock();
		try {
			sessionList.add(this);
			logger.info(LOG_KEY + " Number of connections in list: " + sessionList.size());
		} finally {
			sessionListLock.unlock();
		}

		logger.info(LOG_KEY + "Session created using clientId: " + clientId + " and session id " + sessionCookie);
	}

	public ConnectionImpl(String url, String username, String password, boolean ignoreCert) {
		setUrl(url);
		this.username = username;
		this.password = password;
		this.ignoreCert = ignoreCert;
		this.vimService = new VimService();
		this.vimPort = vimService.getVimPort();
		logger.info(LOG_KEY + "ConnectionImpl using credencials created. " + this);
	}

	private static ConnectionImpl lookupSessionByClientId(String clientId) {
		if ((sessionList != null) && (clientId != null)) {

			sessionListLock.lock();
			try {
				for (int i = 0; i < sessionList.size(); i++) {
					ConnectionImpl conn = sessionList.get(i);
					if (conn != null && clientId.equals(conn.getClientId())) {
						return conn;
					}
				}
			} finally {
				sessionListLock.unlock();
			}
		}

		return null;
	}

	private static void removeFromList(ConnectionImpl _connection) {

		if ((sessionList != null) && (_connection != null)) {
			boolean removed = false;
			String id = _connection.getClientId();

			sessionListLock.lock();
			try {
				removed = sessionList.remove(_connection);
			} finally {
				sessionListLock.unlock();
			}

			if (removed) {
				logger.debug(LOG_KEY + "Session " + id + " removed.");
			} else {
				logger.debug(LOG_KEY + "Session " + id + " could not be removed. Not found in list.");
			}
		}
	}

	private void removeFromList() {
		logger.debug("Session " + this.getClientId() + " ended.");
		removeFromList(this);
	}

	@Override
	public Connection connect(UserSessionService usersessionService, boolean ignoreCert) {
		this.ignoreCert = ignoreCert;
		if (usersessionService != null && usersessionService.getUserSession() != null) {

			com.vmware.vise.usersession.UserSession session = usersessionService.getUserSession();

			if (session != null && session.serversInfo.length > 0 && session.serversInfo[0] != null) {
				logger.info(LOG_KEY + "usersessionService have Client Id: " + session.clientId + " and userName: "
						+ session.userName);
				logger.info(LOG_KEY + "looking session for client id: " + session.clientId);

				ConnectionImpl conn = lookupSessionByClientId(session.clientId);
				if (conn == null) {
					logger.info(LOG_KEY + "session not found for client id: " + session.clientId);
					ServerInfo server = session.serversInfo[0];
					logger.info(LOG_KEY + "session key: " + server.sessionKey);
					// Create a new Session
					conn = newSession(session.clientId, session.userName, server);
				} else {
					logger.info(LOG_KEY + "session found for client id: " + session.clientId);
				}

				try {
					if (!conn.isConnected()) {
						logger.info(LOG_KEY + "connecting to " + conn.getURL().toString()
								+ " using user session's cookie: " + conn.getSessionCookie());

						conn._connect();
						logger.info(LOG_KEY + "Connected using session cookie: " + sessionCookie);
					}
					return conn;
				} catch (Exception e) {
					Throwable cause = (e.getCause() != null) ? e.getCause() : e;
					logger.error(LOG_KEY + "connect() failed. " + e.getMessage());
					throw new ConnectionException("failed to connect: " + e.getMessage() + " : " + cause.getMessage(),
							cause);
				}
			} else {
				// if logoin() method is not used to connect for unit test then
				// throw error
				if (serviceContent == null)
					throw new ConnectionException("User Session is not valid. Can not make a connection to vCenter.");
			}
		} else {
			// if logoin() method is not used to connect for unit test then
			// throw error
			if (serviceContent == null)
				throw new ConnectionException("UserSessionService is null. Can not connect to Center.");
		}
		return this;

	}

	private ConnectionImpl newSession(String _clientId, String _userName, ServerInfo server) {
		ConnectionImpl conn = new ConnectionImpl(_clientId, server.sessionCookie);
		conn.setUrl(server.serviceUrl);
		conn.setUsername(_userName);
		conn.setSessionKey(server.sessionKey);
		return conn;
	}

	/**
	 * Uses session cookie to connect to server
	 * 
	 * @throws RuntimeFaultFaultMsg
	 * @throws Exception
	 */
	private void _connect() throws RuntimeFaultFaultMsg, Exception {

		Map<String, Object> reqContext = ((BindingProvider) this.vimPort).getRequestContext();
		reqContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.url.toString());
		reqContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
		this.addSessionCookie(reqContext, sessionCookie);

		if (ignoreCert) {
			DisableSecurity.trustEveryone();
		}
		this.serviceContent = this.vimPort.retrieveServiceContent(svcInstRef);
	}

	private void addSessionCookie(Map<String, Object> reqContext, String cookie) {
		List<String> values = new ArrayList<String>();
		values.add("vmware_soap_session=" + sessionCookie);
		Map<String, List<String>> reqHeadrs = new HashMap<String, List<String>>();
		reqHeadrs.put("Cookie", values);
		reqContext.put(MessageContext.HTTP_REQUEST_HEADERS, reqHeadrs);
	}

	/**
	 * This is only for Unit testing.
	 * 
	 * @throws RuntimeFaultFaultMsg
	 * @throws InvalidLocaleFaultMsg
	 * @throws InvalidLoginFaultMsg
	 * @throws Exception
	 */
	public void _login() throws RuntimeFaultFaultMsg, InvalidLocaleFaultMsg, InvalidLoginFaultMsg, Exception {
		Map<String, Object> ctxt = ((BindingProvider) vimPort).getRequestContext();

		ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url.toString());
		ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
		if (ignoreCert) {
			DisableSecurity.trustEveryone();
		}

		serviceContent = vimPort.retrieveServiceContent(svcInstRef);
		userSession = vimPort.login(serviceContent.getSessionManager(), username, password, null);
		logger.info(LOG_KEY + "_login() sucess at " + vimPort.currentTime(svcInstRef));

	}

	/**
	 * Checks connection time out and try to get current time on this session.
	 */
	@Override
	public boolean isConnected() {
		if (this.serviceContent == null) {
			return false;
		}
		try {
			this.vimPort.currentTime(svcInstRef);
			// TODO: change with debug. as dev vCenter is running in info level
			// that why it is used with info level.
			logger.info(LOG_KEY + "Session with clientId= " + this.getClientId() + " and userName: "
					+ this.getUsername() + " is connected.");
			return true;
		} catch (Throwable t) {
			logger.error(LOG_KEY + t);
			return false;
		}
	}

	@Override
	public void disconnect(String clientId) {
		ConnectionImpl _connection = lookupSessionByClientId(clientId);
		if (_connection.isConnected()) {
			try {
				_connection.getVimPort().logout(_connection.getServiceContent().getSessionManager());
			} catch (Exception e) {
				Throwable cause = e.getCause();
				throw new ConnectionException(
						"failed to disconnect properly: " + e.getMessage() + " : " + cause.getMessage(), cause);
			} finally {
				// A connection is very memory intensive, I'm helping the
				// garbage collector here
				_connection.removeFromList();
				_connection = null;
			}
		}
	}

	public void setUrl(String url) {
		try {
			this.url = new URL(url);
		} catch (MalformedURLException e) {
			throw new ConnectionMalformedUrlException("malformed URL argument: '" + url + "'", e);
		}
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public VimService getVimService() {
		return vimService;
	}

	public VimPortType getVimPort() {
		return vimPort;
	}

	public ServiceContent getServiceContent() {
		return serviceContent;
	}

	public UserSession getUserSession() {
		return userSession;
	}

	@Override
	public URL getURL() {
		return this.url;
	}

	public String getSessionCookie() {
		return sessionCookie;
	}

	public String getClientId() {
		return clientId;
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}

	@Override
	public String toString() {
		return "ConnectionImpl [url=" + url + ", username=" + username + ", ignoreCert=" + ignoreCert
				+ ", sessionCookie=" + sessionCookie + ", sessionKey=" + sessionKey + ", clientId=" + clientId + "]";
	}

}
