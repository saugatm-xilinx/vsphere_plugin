package com.msys.vcp.vmware;

import com.msys.vcp.model.ActionResponse;
import com.msys.vcp.model.ConnectionDAO;
import com.msys.vcp.model.ExtensionData;

public interface ExtensionService {

	ActionResponse registerPlugin(ConnectionDAO conn, ExtensionData data) throws Exception;

	ActionResponse isPluginRegistered(ConnectionDAO conn, String key) throws Exception;

	ActionResponse unRegisterPlugin(ConnectionDAO conn, String key) throws Exception;

	ActionResponse updatePlugin(ConnectionDAO conn, String key, ExtensionData data);

}
