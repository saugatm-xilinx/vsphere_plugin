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
import com.solarflare.vcp.model.CustomUpdateRequest;
import com.solarflare.vcp.model.Host;
import com.solarflare.vcp.model.HostConfiguration;
import com.solarflare.vcp.model.Status;
import com.solarflare.vcp.model.TaskInfo;
import com.solarflare.vcp.services.DummayService;
import com.solarflare.vcp.services.HostAdapterService;
import com.solarflare.vcp.services.TaskManager;
import com.solarflare.vcp.vim.SimpleTimeCounter;

@Controller
@RequestMapping(value = "/services/hosts")
public class HostAdapterController {
	private static final Log logger = LogFactory.getLog(HostAdapterController.class);

	@Autowired
	HostAdapterService hostAdapterService;

	@RequestMapping(value = "/{hostId}", method = RequestMethod.GET)
	@ResponseBody
	public Host getHostAdapterInfo(@PathVariable String hostId) throws Exception {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: getHostAdapterInfo");
		logger.info("Start getting host info by host id :" + hostId);

		Host host = null;
		try {
			host = hostAdapterService.getHostById(hostId);
		} catch (Exception e) {
			logger.error("Exception while getting host by host id, error: " + e.getMessage());
			throw e;
		} finally {
		}
		logger.info("End getting host info by host id :" + hostId);
		timer.stop();
		return host;
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	@ResponseBody
	public List<Host> getHostList() throws Exception {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: getHostList");
		logger.info("Start getting host list");
		List<Host> hostList = null;
		try {
			hostList = hostAdapterService.getHostList();
		} catch (Exception e) {
			logger.error("Exception while getting list of hosts, error: " + e.getMessage());
			throw e;
		} finally {
		}
		logger.info("End getting host list");
		timer.stop();
		return hostList;
	}

	@RequestMapping(value = "/{hostId}/adapters", method = RequestMethod.GET)
	@ResponseBody
	public List<Adapter> listAdapter(@PathVariable String hostId) throws Exception {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: listAdapter");
		logger.info("Start getting list of host adapters for host :" + hostId);
		List<Adapter> adapters = null;
		try {
			adapters = hostAdapterService.getHostAdapters(hostId);
		} catch (Exception e) {
			logger.error("Exception while getting list of host adapters, error: " + e.getMessage());
			throw e;
		} finally {
		}
		logger.info("End getting list of host adapters for host :" + hostId);
		timer.stop();
		return adapters;
	}

	@RequestMapping(value = "/{hostId}/adapters/latest", method = RequestMethod.POST)
	@ResponseBody
	public String updateFirmwareToLatest(@RequestBody String adapterList, @PathVariable String hostId) {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: updateFirmwareToLatest");
		logger.info("start getting file as string content");
		String taskIDResponse = null;
		try {
			if (adapterList != null && hostId != null) {
				Gson gson = new Gson();
				Type listType = new TypeToken<List<Adapter>>() {
				}.getType();

				List<Adapter> adapter = gson.fromJson(adapterList, listType);
				try {
					String taskID = hostAdapterService.updateFirmwareToLatest(adapter, hostId);
					Map<String,String> response = new HashMap<>();
					response.put("taskId", taskID);
					taskIDResponse = gson.toJson(response);
				} catch (Exception e) {
					logger.error("Exception while updating firmware to latest, error :" + e.getMessage());
				}
			}
		} finally {
		}
		logger.info("End getting file as string content");
		timer.stop();
		return taskIDResponse;
	}

	@RequestMapping(value = "/{hostId}/adapters/{adapterId}/status", method = RequestMethod.GET)
	@ResponseBody
	public List<Status> getStatus(@PathVariable String hostId, @PathVariable String adapterId) throws Exception {
		logger.info("Getting status for hostId :" + hostId + ", AdapterId :" + adapterId);
		List<Status> status = hostAdapterService.getStatus(hostId, adapterId);
		return status;
	}

	@RequestMapping(value = "/tasks/{taskId}", method = RequestMethod.GET)
	@ResponseBody
	public TaskInfo getTaskInfo(@PathVariable String taskId) throws Exception {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: getTaskInfo");
		logger.info("Getting task info for Host ID : " + taskId);
		TaskInfo taskInfo = null;
		List<TaskInfo> tasks = TaskManager.getInstance().getTasks();
		for(TaskInfo task : tasks){
			if(task.getTaskid().equals(taskId)){
				taskInfo = task;
			}
		}
		timer.stop();
		return taskInfo;
	}
	
	@RequestMapping(value = "/tasks", method = RequestMethod.GET)
	@ResponseBody
	public List<TaskInfo> getTasks() throws Exception {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: getTasks");
		logger.info("Getting List of Tasks " );
		List<TaskInfo> tasks = TaskManager.getInstance().getTasks();
		timer.stop();
		return tasks;
	}
	
	@RequestMapping(value = "/{hostId}/adapters/updateCustomWithUrl", method = RequestMethod.POST)
	@ResponseBody
	public String updateCustomWithUrl(@RequestBody String adapters, @PathVariable String hostId) {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: updateCustomWithUrl");
		logger.info("start getting file as string content ");
		String taskIDResponse = null;
		try {
			Gson gson = new Gson();
			CustomUpdateRequest customUpdateRequest = gson.fromJson(adapters, CustomUpdateRequest.class);
			String url = customUpdateRequest.getUrl();
			logger.info("url : " + url);
			String taskID = hostAdapterService.customUpdateFirmwareFromURL(customUpdateRequest.getAdapters(), hostId, url);
			Map<String,String> response = new HashMap<>();
			response.put("taskId", taskID);
			taskIDResponse = gson.toJson(response);

		} catch (Exception e) {
			logger.error("Exception while updating firmware, error :" + e.getMessage());
		} finally {
		}
		logger.info("End getting file as string content");
		timer.stop();
		return taskIDResponse;
	}

	@RequestMapping(value = "/{hostId}/adapters/updateCustomWithBinary", method = RequestMethod.POST)
	@ResponseBody
	public String updateCustomWithBinary(@RequestBody String adapters, @PathVariable String hostId) {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: updateCustomWithBinary");
		logger.info("start getting file as string content");
		String taskIDResponse = null;
		try {
			Gson gson = new Gson();
			CustomUpdateRequest customUpdateRequest = gson.fromJson(adapters, CustomUpdateRequest.class);
			String data = customUpdateRequest.getBase64Data();
			String taskID = hostAdapterService.customUpdateFirmwareFromLocal(customUpdateRequest.getAdapters(), hostId, data);
			Map<String,String> response = new HashMap<>();
			response.put("taskId", taskID);
			taskIDResponse = gson.toJson(response);
		} catch (Exception e) {
			logger.error("Exception while updating firmware, error :" + e.getMessage());
		} finally {
		}
		logger.info("End getting file as string content");
		timer.stop();
		return taskIDResponse;
	}
	// Get configuration
	@RequestMapping(value = "/{hostId}/configuration", method = RequestMethod.GET)
	@ResponseBody
	public HostConfiguration getConfiguration(@PathVariable String hostId)
	{
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: getConfiguration");
		logger.info("Start configuring host, hostId:"+hostId);
		HostConfiguration hostConfigurations = null;
		try {
			DummayService service = new DummayService();
			//hostConfigurations = hostAdapterService.getHostConfigurations(hostId);
			hostConfigurations = service.getHostConfigurations(hostId);
		} catch (Exception e) {
			logger.error("Exception while getting host configurations, hostId:" + hostId);
		}
		timer.stop();
		return hostConfigurations;

	}

	// Get configuration
	@RequestMapping(value = "/{hostId}/configuration", method = RequestMethod.POST)
	@ResponseBody
	public void updateConfiguration(@RequestBody String hostConfiguration, @PathVariable String hostId) {
		SimpleTimeCounter timer = new SimpleTimeCounter("SolarFlare :: updateConfiguration");
		try {
			if (hostId == null || hostId.isEmpty()) {
				throw new Exception("hostId should not be is null or empty");
			}
			if (hostConfiguration == null || hostConfiguration.isEmpty()) {
				throw new Exception("Host configuration should not be is null or empty");
			}
			Gson gson = new Gson();
			HostConfiguration hostConfigurationRequest = gson.fromJson(hostConfiguration, HostConfiguration.class);

			hostAdapterService.updateHostConfigurations(hostConfigurationRequest);

		} catch (Exception e) {
			logger.error("Exception while getting host configurations, hostId:" + hostId);
		}
		timer.stop();
	}
	
	
	@ExceptionHandler(SfNotFoundException.class)
	@ResponseBody
	public Map<String, String> handleException(SfNotFoundException ex, HttpServletResponse response) {
		response.setStatus(HttpStatus.NOT_FOUND.value());

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

	@ExceptionHandler(SfInvalidRequestException.class)
	@ResponseBody
	public Map<String, String> handleException(SfInvalidRequestException ex, HttpServletResponse response) {
		response.setStatus(HttpStatus.BAD_REQUEST.value());

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

	@ExceptionHandler(SfInvalidLoginException.class)
	@ResponseBody
	public Map<String, String> handleException(SfInvalidLoginException ex, HttpServletResponse response) {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());

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
