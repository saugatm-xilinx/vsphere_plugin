package com.solarflare.vcp.vim;

import java.util.List;

import com.solarflare.vcp.cim.CIMHost;
import com.solarflare.vcp.model.Adapter;
import com.solarflare.vcp.model.Host;
import com.solarflare.vcp.vim.connection.Connection;
import com.vmware.vim25.RuntimeFaultFaultMsg;

public interface SfVimService {

	public Host getHostSummary(String hostId) throws Exception;

	public List<Host> getAllHosts() throws Exception;

	public List<Adapter> getHostAdapters(String hostId) throws Exception, RuntimeFaultFaultMsg;

	public CIMHost getCIMHost(String hostId) throws Exception;

	Connection getSession();

	String getPluginURL(String pluginKey) throws Exception;
}
