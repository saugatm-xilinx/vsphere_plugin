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
import com.solarflare.vcp.exception.SfInvalidLoginException;
import com.solarflare.vcp.exception.SfInvalidRequestException;
import com.solarflare.vcp.exception.SfNotFoundException;
import com.solarflare.vcp.model.Adapter;
import com.solarflare.vcp.model.AdapterNicStatistics;
import com.solarflare.vcp.model.AdapterOverview;
import com.solarflare.vcp.model.CustomUpdateRequest;
import com.solarflare.vcp.model.Host;
import com.solarflare.vcp.model.HostConfiguration;
import com.solarflare.vcp.model.TaskInfo;
import com.solarflare.vcp.model.VMNICResponse;
import com.solarflare.vcp.services.HostAdapterService;
import com.solarflare.vcp.services.TaskManager;
import com.solarflare.vcp.vim.SimpleTimeCounter;
import com.solarflare.vcp.vim.connection.ConnectionException;

@Controller
@RequestMapping(value = "/services")
public class HostAdapterController {
	private static final Log logger = LogFactory.getLog(HostAdapterController.class);

	@Autowired
	HostAdapterService hostAdapterService;

	@RequestMapping(value = "/hosts/{hostId}", method = RequestMethod.GET)
	@ResponseBody
	public Host getHostAdapterInfo(@PathVariable String hostId) throws Exception {
		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: Get - getHostAdapterInfo");
		logger.info("Start getting host overview by host id :" + hostId);

		if (hostId == null || hostId.isEmpty()) {
			throw new SfInvalidRequestException("Host id is invalid.");
		}
		Host host = hostAdapterService.getHostById(hostId);
		logger.debug("End getting host overview by host id :" + hostId);
		timer.stop();
		return host;
	}

	@RequestMapping(value = "/hosts", method = RequestMethod.GET)
	@ResponseBody
	public List<Host> getHostList() throws Exception {
		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: Get - getHostList");
		logger.info("Start getting host list");
		List<Host> hostList = hostAdapterService.getHostList();
		logger.debug("End getting host list");
		timer.stop();
		return hostList;
	}

	@RequestMapping(value = "/hosts/{hostId}/adapters", method = RequestMethod.GET)
	@ResponseBody
	public List<Adapter> listAdapter(@PathVariable String hostId) throws Exception {
		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: Get - listAdapter");
		logger.info("Getting list of host adapters for host :" + hostId);
		if (hostId == null || hostId.isEmpty()) {
			throw new SfInvalidRequestException("Host id is invalid");
		}

		List<Adapter> adapters = hostAdapterService.getHostAdapters(hostId);
		logger.debug("End getting list of host adapters for host :" + hostId);
		timer.stop();
		return adapters;
	}

	@RequestMapping(value = "/hosts/{hostId}/adapters/latest", method = RequestMethod.POST)
	@ResponseBody
	public String updateFirmwareToLatest(@RequestBody String adapterList, @PathVariable String hostId)
			throws Exception {
		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: Update - updateFirmwareToLatest");
		if (adapterList == null || adapterList.isEmpty()) {
			throw new SfInvalidRequestException("Adapter is not selected to update.");
		}
		if (hostId == null || hostId.isEmpty()) {
			throw new SfInvalidRequestException("Host id is invalid");
		}

		Gson gson = new Gson();
		Type listType = new TypeToken<List<Adapter>>() {
		}.getType();

		List<Adapter> adapter = gson.fromJson(adapterList, listType);
		String taskId = hostAdapterService.updateFirmwareToLatest(adapter, hostId);
		String response = createTaskRequestId(taskId);
		logger.debug("Solarflare:: updateCustomWithBinary response: " + response);
		timer.stop();
		return response;

	}

	@RequestMapping(value = "/hosts/tasks/{taskId}", method = RequestMethod.GET)
	@ResponseBody
	public TaskInfo getTaskInfo(@PathVariable String taskId) throws Exception {
		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: getTaskInfo");
		logger.info("Getting task info for task id : " + taskId);
		TaskInfo taskInfo = TaskManager.getInstance().getTaskInfo(taskId);
		timer.stop();
		return taskInfo;
	}

	@RequestMapping(value = "/hosts/tasks", method = RequestMethod.GET)
	@ResponseBody
	public List<TaskInfo> getTasks() throws Exception {
		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: getTasks");
		logger.info("Getting List of Tasks ");
		List<TaskInfo> tasks = TaskManager.getInstance().getTasks();
		timer.stop();
		return tasks;
	}

	@RequestMapping(value = "/hosts/{hostId}/adapters/updateCustomWithUrl", method = RequestMethod.POST)
	@ResponseBody
	public String updateCustomWithUrl(@RequestBody String adapters, @PathVariable String hostId) throws Exception {
		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: Update - updateCustomWithUrl");
		if (hostId == null || hostId.isEmpty()) {
			throw new SfInvalidRequestException("Host id is invalid");
		}
		Gson gson = new Gson();
		CustomUpdateRequest customUpdateRequest = gson.fromJson(adapters, CustomUpdateRequest.class);
		String url = customUpdateRequest.getUrl();
		logger.info("url : " + url);
		String taskId = hostAdapterService.customUpdateFirmwareFromURL(customUpdateRequest.getAdapters(), hostId, url);
		String response = createTaskRequestId(taskId);
		logger.debug("Solarflare:: updateCustomWithBinary response: " + response);
		timer.stop();
		return response;
	}

	@RequestMapping(value = "/hosts/{hostId}/adapters/updateCustomWithBinary", method = RequestMethod.POST)
	@ResponseBody
	public String updateCustomWithBinary(@RequestBody String adapters, @PathVariable String hostId) throws Exception {
		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: Update - updateCustomWithBinary");
		logger.info("start getting file as string content");
		Gson gson = new Gson();
		CustomUpdateRequest customUpdateRequest = gson.fromJson(adapters, CustomUpdateRequest.class);
		String data = customUpdateRequest.getBase64Data();
		String taskId = hostAdapterService.customUpdateFirmwareFromLocal(customUpdateRequest.getAdapters(), hostId,
				data);
		String response = createTaskRequestId(taskId);
		logger.debug("Solarflare:: updateCustomWithBinary response: " + response);
		timer.stop();
		return response;
	}

	// Get configuration
	@RequestMapping(value = "/hosts/{hostId}/configuration", method = RequestMethod.GET)
	@ResponseBody
	public HostConfiguration getConfiguration(@PathVariable String hostId) throws Exception {
		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: getConfiguration");
		logger.info("Start configuring host, hostId:" + hostId);
		HostConfiguration hostConfigurations = null;
		hostConfigurations = hostAdapterService.getHostConfigurations(hostId);
		timer.stop();
		return hostConfigurations;

	}

	// Get configuration
	@RequestMapping(value = "/hosts/{hostId}/configuration", method = RequestMethod.POST)
	@ResponseBody
	public void updateConfiguration(@RequestBody String hostConfiguration, @PathVariable String hostId)
			throws Exception {
		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: updateConfiguration");
		logger.info("Solarflare:: updateConfiguration");
		if (hostId == null || hostId.isEmpty()) {
			logger.error("hostId is null");
			throw new Exception("hostId should not be is null or empty");
		}
		if (hostConfiguration == null || hostConfiguration.isEmpty()) {
			logger.error("Host configuration is null");
			throw new Exception("Host configuration should not be is null or empty");
		}
		Gson gson = new Gson();
		HostConfiguration hostConfigurationRequest = gson.fromJson(hostConfiguration, HostConfiguration.class);
		hostAdapterService.updateHostConfigurations(hostId, hostConfigurationRequest);
		timer.stop();
	}

	// Get Adapter Overview
	@RequestMapping(value = "/hosts/{hostId}/adapters/{nicId}/overview", method = RequestMethod.GET)
	@ResponseBody
	public AdapterOverview getAdapterOverview(@PathVariable String hostId, @PathVariable String nicId)
			throws Exception {
		if (hostId == null || hostId.isEmpty()) {
			throw new Exception("hostId should not be null or empty");
		} else if (nicId == null || nicId.isEmpty()) {
			throw new Exception("nicId should not be null or empty");
		}
		AdapterOverview adapterOverview = null;
		try {
			adapterOverview = hostAdapterService.getAdapterOverview(hostId, nicId);
		} catch (Exception e) {
			throw e;
		}
		return adapterOverview;
	}

	@RequestMapping(value = "/hosts/{hostId}/adapters/{nicId}/nics", method = RequestMethod.GET)
	@ResponseBody
	public VMNICResponse getAdapterNics(@PathVariable String hostId, @PathVariable String nicId) throws Exception {
		if (hostId == null || hostId.isEmpty()) {
			throw new Exception("hostId should not be null or empty");
		} else if (nicId == null || nicId.isEmpty()) {
			throw new Exception("nicId should not be null or empty");
		}
		VMNICResponse nicResponse = null;
		try {
			nicResponse = hostAdapterService.getAdapterForNIC(hostId, nicId);
		} catch (Exception e) {
			throw e;
		}
		return nicResponse;
	}

	@ExceptionHandler(SfNotFoundException.class)
	@ResponseBody
	public Map<String, String> handleException(SfNotFoundException ex, HttpServletResponse response) {
		response.setStatus(HttpStatus.NOT_FOUND.value());
		return getError(ex, false);
	}

	@ExceptionHandler(SfInvalidRequestException.class)
	@ResponseBody
	public Map<String, String> handleException(SfInvalidRequestException ex, HttpServletResponse response) {
		response.setStatus(HttpStatus.BAD_REQUEST.value());
		return getError(ex, false);
	}

	@ExceptionHandler(SfInvalidLoginException.class)
	@ResponseBody
	public Map<String, String> handleException(SfInvalidLoginException ex, HttpServletResponse response) {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		return getError(ex, false);
	}

	@ExceptionHandler(ConnectionException.class)
	@ResponseBody
	public Map<String, String> handleException(ConnectionException ex, HttpServletResponse response) {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());

		return getError(ex, false);
	}

	@ExceptionHandler(Exception.class)
	@ResponseBody
	public Map<String, String> handleException(Exception ex, HttpServletResponse response) {
		response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

		return getError(ex, false);
	}

	private Map<String, String> getError(Exception ex, boolean includeStacktrace) {
		Map<String, String> errorMap = new HashMap<String, String>();
		errorMap.put("message", ex.getMessage());
		if (ex.getCause() != null) {
			errorMap.put("cause", ex.getCause().getMessage());
		}
		if (includeStacktrace) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			errorMap.put("stackTrace", sw.toString());
		}

		return errorMap;
	}

	private String createTaskRequestId(String taskId) {
		Map<String, String> response = new HashMap<>();
		response.put("taskId", taskId);
		Gson gson = new Gson();
		return gson.toJson(response);
	}

	@RequestMapping(value = "/hosts/{hostId}/adapters/{nicId}/statistics", method = RequestMethod.GET)
	@ResponseBody
	public AdapterNicStatistics getAdapterNicStatistics(@PathVariable String hostId, @PathVariable String nicId)
			throws Exception {
		logger.info("Solarflare:: getAdapterNicStatistics called for hostId: " + hostId + " and nicId: "+nicId);
		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: getAdapterNicStatistics");
		if (hostId == null || hostId.isEmpty()) {
			timer.stop();
			throw new Exception("hostId should not be null or empty");
		} else if (nicId == null || nicId.isEmpty()) {
			timer.stop();
			throw new Exception("nicId should not be null or empty");
		}
		AdapterNicStatistics adapterNicStatistics = null;
		try {
			adapterNicStatistics = hostAdapterService.getAdapterNicStatistics(hostId, nicId);
		} catch (Exception e) {
			timer.stop();
			throw e;
		}
		timer.stop();
		return adapterNicStatistics;
	}
}
