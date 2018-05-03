package com.msys.vcp.mvc;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.msys.vcp.model.ActionRequest;
import com.msys.vcp.model.ActionResponse;
import com.msys.vcp.model.ExtensionData;
import com.msys.vcp.utils.ExtensionDataHelper;
import com.msys.vcp.vmware.ExtensionService;

@RestController
public class PluginController {
	private static Logger logger = LoggerFactory.getLogger(PluginController.class);

	public PluginController() {
		logger.info("PluginController created.");
	}

	@Autowired
	private ExtensionService service;

	@RequestMapping("/status")
	public String welcome() throws UnsupportedEncodingException {// Welcome
																	// page,
																	// non-rest
		String welcome = "Welcome to Plugin Registration App.";
		logger.info("This is an Welcome message: " + welcome);
		logger.info("appPath property: " + System.getProperty("appPath"));
		return welcome + "  log file Path: " + System.getProperty("appPath");
	}

	@GetMapping("/defaultData")
	public ExtensionData defaultData() throws Exception {
		logger.info("defaultData requested.");
		ExtensionData data = ExtensionDataHelper.getDefaultData();
		logger.info("defaultData response: {}.", data);
		if (data != null)
			return data;
		throw new Exception("Error while reading properties file!");
	}

	@PostMapping("/plugin-service")
	public ActionResponse executeAction(@RequestBody ActionRequest req) throws Exception {
		logger.debug("executeAction called with request {}.", req);
		ActionResponse response = null;
		ExtensionData data = ExtensionDataHelper.getDefaultData();
		String action = req.getAction().toLowerCase();
		switch (action) {
		case "register":
			logger.info("Registering plugin.");
			logger.debug("Registering plugin with data {}", data);
			response = service.registerPlugin(req.getConnection(), data);
			break;
		case "unregister":
			logger.info("Unregistering plugin : {}", data.getKey());
			response = service.unRegisterPlugin(req.getConnection(), data.getKey());
			break;
		case "isregistered":
			response = service.isPluginRegistered(req.getConnection(), data.getKey());
			break;
		default:
			new ActionResponse(false, req.getAction() + ": Action is not supported.");

		}
		logger.debug("executeAction response: {}", response);
		return response;

	}

	/**
	 * Generic handling of internal exceptions. Sends a 500 server error
	 * response along with a json body with messages
	 *
	 * @param ex
	 *            The exception that was thrown.
	 * @param response
	 * @return a map containing the exception message, the cause, and a
	 *         stackTrace
	 */
	@ExceptionHandler(Exception.class)
	@ResponseBody
	public Map<String, String> handleException(Exception ex, HttpServletResponse response) {
		logger.error("Exception in request processing!", ex);
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

	public String getPath() throws UnsupportedEncodingException {
		String path = this.getClass().getClassLoader().getResource("").getPath();
		String fullPath = URLDecoder.decode(path, "UTF-8");
		System.out.println(fullPath);
		String pathArr[] = fullPath.split("/WEB-INF/classes/");
		System.out.println(fullPath);
		System.out.println(pathArr[0]);
		fullPath = pathArr[0];
		String reponsePath = "";
		// to read a file from webcontent
		reponsePath = new File(fullPath).getPath() + File.separatorChar + "registration.log";
		System.out.println(reponsePath);
		return reponsePath;
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		new PluginController().getPath();
	}
}
