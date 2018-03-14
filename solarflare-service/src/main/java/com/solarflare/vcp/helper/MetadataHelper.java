package com.solarflare.vcp.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.cim.CIMInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.solarflare.vcp.cim.CIMConstants;
import com.solarflare.vcp.cim.SfCIMService;
import com.solarflare.vcp.model.Adapter;
import com.solarflare.vcp.model.BinaryFiles;
import com.solarflare.vcp.model.FwType;
import com.solarflare.vcp.model.SfFirmware;
import com.vmware.vim25.RuntimeFaultFaultMsg;

public class MetadataHelper {

	private static final Log logger = LogFactory.getLog(MetadataHelper.class);
	private static BinaryFiles metadata;
	private static Map<String, SfFirmware> files;

	public static BinaryFiles getMetadata(URL filePath) throws MalformedURLException, Exception {
		logger.info("getMetadata called with URL : " + filePath);
		validateURL(filePath);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		byte[] jsonData = readData(filePath, true);
		String jsonString = new String(jsonData);
		JsonReader reader = new JsonReader(new StringReader(jsonString));
		BinaryFiles fwMetaData = gson.fromJson(reader, BinaryFiles.class);

		return fwMetaData;
	}

	public static SfFirmware getMetaDataForAdapter(URL pluginURL, SfCIMService cimService, CIMInstance sfFWInstance,
			CIMInstance nicInstance, FwType fwType, Adapter adapter)
			throws MalformedURLException, RuntimeFaultFaultMsg, Exception {
		logger.info("getMetaDataForAdapter called for input params pluginURL : " + pluginURL + " FwType : " + fwType);
 
		setTypeNSubType(cimService,sfFWInstance,nicInstance,fwType,adapter);
		
		int currentType = 0;
		int currentSubType = 0;

		// Check if adapter has type and subType cached
		if (fwType.equals(FwType.CONTROLLER)) {
			currentType = adapter.getControllerType();
			currentSubType = adapter.getControllerSubType();
		} else if (fwType.equals(FwType.BOOTROM)) {
			currentType = adapter.getBootROMType();
			currentSubType = adapter.getBootROMSubType();
		} else if (fwType.equals(FwType.UEFIROM)) {
			currentType = adapter.getUefiROMType();
			currentSubType = adapter.getUefiROMSubType();
		}
		
		if (metadata == null) {
			// TODO : check for https certificate warning
			URL metaDataFilePath = new URL("http", pluginURL.getHost(), CIMConstants.METADATA_PATH);
			logger.info("Solarfalre:: getting metadata from " + metaDataFilePath);
			metadata = getMetadata(metaDataFilePath);
		}
		if (files == null || files.isEmpty()) {
			logger.info("Solarflare:: creating map from metedata file...");
			prepareFileMap(metadata);
		}
		/*
		 * List<SfFirmware> files = null; if (FwType.CONTROLLER.equals(fwType))
		 * { Controller controller = metadata.getController(); files =
		 * controller.getFiles(); } if (FwType.BOOTROM.equals(fwType)) { BootROM
		 * bootROM = metadata.getBootROM(); files = bootROM.getFiles(); } if
		 * (FwType.UEFIROM.equals(fwType)) { UEFIROM uefiROM =
		 * metadata.getUefiROM(); files = uefiROM.getFiles(); }
		 * 
		 * for (SfFirmware file : files) { int type =
		 * Integer.parseInt(file.getType()); int subType =
		 * Integer.parseInt(file.getSubtype()); if (type == currentType &&
		 * subType == currentSubType) { metaDatafile = file; } }
		 */

		return files.get(currentType + "," + currentSubType);
	}

	private static void setTypeNSubType(SfCIMService cimService, CIMInstance sfFWInstance, CIMInstance nicInstance,
			FwType fwType, Adapter adapter) {
		
		int currentType = 0;
		int currentSubType = 0;

		// Check if adapter has type and subType cached
		if (fwType.equals(FwType.CONTROLLER)) {
			currentType = adapter.getControllerType();
			currentSubType = adapter.getControllerSubType();
		} else if (fwType.equals(FwType.BOOTROM)) {
			currentType = adapter.getBootROMType();
			currentSubType = adapter.getBootROMSubType();
		} else if (fwType.equals(FwType.UEFIROM)) {
			currentType = adapter.getUefiROMType();
			currentSubType = adapter.getUefiROMSubType();
		}

		if (currentType == 0 || currentSubType == 0) {
			logger.info("Solarflare:: CIM call to get supported type and sub type");
			Map<String, String> params = cimService.getRequiredFwImageName(sfFWInstance, nicInstance);

			currentType = Integer.parseInt(params.isEmpty() ? "0" : params.get(CIMConstants.TYPE));
			currentSubType = Integer.parseInt(params.isEmpty() ? "0" : params.get(CIMConstants.SUB_TYPE));
			
			// Store type and subType in adapter
			if (fwType.equals(FwType.CONTROLLER)) {
				adapter.setControllerType(currentType);
				adapter.setControllerSubType(currentSubType);
			} else if (fwType.equals(FwType.BOOTROM)) {
				adapter.setBootROMType(currentType);
				adapter.setBootROMSubType(currentSubType);
			} else if (fwType.equals(FwType.UEFIROM)) {
				adapter.setUefiROMType(currentType);
				adapter.setUefiROMSubType(currentSubType);
			}
			
		} else {
			logger.info("Solarflare:: Read supported type and sub type from adapter object");
		}
	}

	private static byte[] readData(URL toDownload, boolean readComplete) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try {
			byte[] chunk = new byte[1000];
			int bytesRead;
			if (toDownload != null) {
				InputStream stream = toDownload.openStream();
				if (readComplete) {
					while ((bytesRead = stream.read(chunk)) > 0) {
						outputStream.write(chunk, 0, bytesRead);
					}
				} else {
					bytesRead = stream.read(chunk);
					outputStream.write(chunk, 0, bytesRead);
				}
			}
		} catch (IOException e) {
			logger.error("Error in reading file : " + toDownload);
		}
		return outputStream.toByteArray();
	}

	public static boolean validateURL(URL url) throws Exception {
		logger.info("Solaflare :: validating url...");
		// validate url
		// check for multicast or loopback
		java.net.InetAddress address = java.net.InetAddress.getByName(url.getHost());
		logger.info("Solaflare :: Validating address for metadata file : " + address);

		if (address.isMulticastAddress()) {
			logger.error("Solaflare :: Invalid host. {} is a multicast address." + url.getHost());
			throw new Exception("Multicast address can not be used.");
		}
		if (address.isLoopbackAddress()) {
			logger.error("Solaflare :: Invalid host. {} is a loopback address." + url.getHost());
			throw new Exception("Loopback address can not be used.");
		}
		// ping host
		if (!address.isReachable(5000)) {
			logger.error("Solaflare ::  unreachable " + url.getHost());
			throw new Exception(url.getHost() + " is NOT reachable! Please verify if the server is accessible.");
		}

		// Validate if metadata file is present at server
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.connect();

		int code = connection.getResponseCode();
		if (code == 404) {
			logger.error("Solaflare ::  FirmwareMetadata.json file not found on server " + url.getHost());
			throw new Exception("Unable to access latest binary files metadata on server " + url.getHost());
		}

		return true;
	}

	private static void prepareFileMap(BinaryFiles metadata) {
		files = new HashMap<String, SfFirmware>();
		for (SfFirmware file : metadata.getController().getFiles()) {
			int type = Integer.parseInt(file.getType());
			int subType = Integer.parseInt(file.getSubtype());
			files.put(type + "," + subType, file);
		}
		for (SfFirmware file : metadata.getBootROM().getFiles()) {
			int type = Integer.parseInt(file.getType());
			int subType = Integer.parseInt(file.getSubtype());
			files.put(type + "," + subType, file);
		}
		for (SfFirmware file : metadata.getUefiROM().getFiles()) {
			int type = Integer.parseInt(file.getType());
			int subType = Integer.parseInt(file.getSubtype());
			files.put(type + "," + subType, file);
		}

	}
}
