package com.solarflare.vcp.mvc;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.solarflare.vcp.model.Adapter;
import com.solarflare.vcp.model.CustomUpdateRequest;
import com.solarflare.vcp.model.Host;
import com.solarflare.vcp.model.Status;
import com.solarflare.vcp.services.HostAdapterService;

@Controller
@RequestMapping(value = "/services/hosts")
public class HostAdapterController {
	private static final Log logger = LogFactory.getLog(HostAdapterController.class);

	@Autowired
	HostAdapterService hostAdapterService;

	@RequestMapping(value = "/{hostId}", method = RequestMethod.GET)
	@ResponseBody
	public Host getHostAdapterInfo(@PathVariable String hostId) throws Exception {
		logger.info("Start getting host info by host id :" + hostId);
		Host host = null;
		try {
			host = hostAdapterService.getHostById(hostId);
		} catch (Exception e) {
			logger.error("Exception while getting host by host id, error: " + e.getMessage());
			throw e;
		}
		logger.info("End getting host info by host id :" + hostId);
		return host;
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	@ResponseBody
	public List<Host> getHostList() throws Exception {
		logger.info("Start getting host list");
		List<Host> hostList = null;
		try {
			hostList = hostAdapterService.getHostList();
		} catch (Exception e) {
			logger.error("Exception while getting list of hosts, error: " + e.getMessage());
			throw e;
		}
		logger.info("End getting host list");
		return hostList;
	}

	@RequestMapping(value = "/{hostId}/adapters", method = RequestMethod.GET)
	@ResponseBody
	public List<Adapter> listAdapter(@PathVariable String hostId) throws Exception {
		logger.info("Start getting list of host adapters for host :" + hostId);
		List<Adapter> adapters = null;
		try {
			adapters = hostAdapterService.getHostAdapters(hostId);
		} catch (Exception e) {
			logger.error("Exception while getting list of host adapters, error: " + e.getMessage());
			throw e;
		}
		logger.info("End getting list of host adapters for host :" + hostId);
		return adapters;
	}

	@RequestMapping(value = "/{hostId}/adapters/latest", method = RequestMethod.POST)
    @ResponseBody
    public void updateFirmwareToLatest(@RequestBody String adapterList, @PathVariable String hostId)
    {
        logger.info("start getting file as string content");
        if (adapterList != null && hostId != null)
        {
        	Gson gson = new Gson();
        	Type listType = new TypeToken<List<Adapter>>()
            {
            }.getType();

			List<Adapter> adapter = gson.fromJson(adapterList, listType);
        	
            try
            {
                hostAdapterService.updateFirmwareToLatest(adapter, hostId);
            }
            catch (Exception e)
            {
                logger.error("Exception while updating firmware to latest, error :" + e.getMessage());
            }
        }
        logger.info("End getting file as string content");
    }

	@RequestMapping(value = "/{hostId}/adapters/{adapterId}/status", method = RequestMethod.GET)
	@ResponseBody
	public  List<Status> getStatus(@PathVariable String hostId, @PathVariable String adapterId) throws Exception {
		logger.info("Getting status for hostId :" + hostId +", AdapterId :"+adapterId);
		List<Status> status = hostAdapterService.getStatus(hostId, adapterId);
		return status;
	}
	
	@RequestMapping(value = "/{hostId}/adapters/updateCustomWithUrl", method = RequestMethod.POST)
	@ResponseBody
	public void updateCustomWithUrl(@RequestBody String adapters, @PathVariable String hostId) {
		logger.info("start getting file as string content " );
		try {
			Gson gson = new Gson();
			CustomUpdateRequest customUpdateRequest = gson.fromJson(adapters, CustomUpdateRequest.class);
			String url = customUpdateRequest.getUrl();
			logger.info("url : " +url);
			hostAdapterService.customUpdateFirmwareFromURL(customUpdateRequest.getAdapters(), hostId, url);
			
		} catch (Exception e) {
			logger.error("Exception while updating firmware, error :" + e.getMessage());
		}
		logger.info("End getting file as string content");
	}
	
	@RequestMapping(value = "/{hostId}/adapters/updateCustomWithBinary", method = RequestMethod.POST)
	@ResponseBody
	public void updateCustomWithBinary(@RequestBody String adapters, @PathVariable String hostId) {
		logger.info("start getting file as string content");
		try {
			Gson gson = new Gson();
			CustomUpdateRequest customUpdateRequest = gson.fromJson(adapters, CustomUpdateRequest.class);
			String data = customUpdateRequest.getBase64Data();
			hostAdapterService.customUpdateFirmwareFromLocal(customUpdateRequest.getAdapters(), hostId, data);
		} catch (Exception e) {
			logger.error("Exception while updating firmware, error :" + e.getMessage());
		}
		logger.info("End getting file as string content");
	}

	@ExceptionHandler(Exception.class)
	@ResponseBody
	public Map<String, String> handleException(Exception ex, HttpServletResponse response) {
		response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

		Map<String, String> errorMap = new HashMap<String, String>();
		errorMap.put("message", ex.getMessage());
		if (ex.getCause() != null) {
			errorMap.put("cause", ex.getCause().getMessage());
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		errorMap.put("stackTrace", sw.toString());

		return errorMap;
	}
}
