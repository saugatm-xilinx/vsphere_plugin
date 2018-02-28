package com.solarflare.vcp.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.cim.CIMInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.solarflare.vcp.cim.CIMConstants;
import com.solarflare.vcp.cim.SfCIMService;
import com.solarflare.vcp.model.BinaryFiles;
import com.solarflare.vcp.model.BootROM;
import com.solarflare.vcp.model.Controller;
import com.solarflare.vcp.model.FwType;
import com.solarflare.vcp.model.SfFirmware;
import com.solarflare.vcp.model.UEFIROM;
import com.vmware.vim25.RuntimeFaultFaultMsg;

public class MetadataHelper {

	private static final Log logger = LogFactory.getLog(MetadataHelper.class);

	public static BinaryFiles getMetadata(URL filePath) throws MalformedURLException,Exception {
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
			CIMInstance nicInstance, FwType fwType) throws MalformedURLException, RuntimeFaultFaultMsg, Exception {
		logger.info("getMetaDataForAdapter called for input params pluginURL : " + pluginURL + " FwType : " + fwType);

		BinaryFiles metadata = null;
		SfFirmware metaDatafile = null;

		Map<String, String> params = cimService.getRequiredFwImageName(sfFWInstance, nicInstance);

		int currentType = Integer.parseInt(params.isEmpty() ? "0" : params.get(CIMConstants.TYPE));
		int currentSubType = Integer.parseInt(params.isEmpty() ? "0" : params.get(CIMConstants.SUB_TYPE));

		if (metadata == null) {
			// TODO : check for https certificate warning
			URL metaDataFilePath = new URL("http", pluginURL.getHost(), CIMConstants.METADATA_PATH);
			metadata = getMetadata(metaDataFilePath);
		}

		List<SfFirmware> files = null;
		if (FwType.CONTROLLER.equals(fwType)) {
			Controller controller = metadata.getController();
			files = controller.getFiles();
		}
		if (FwType.BOOTROM.equals(fwType)) {
			BootROM bootROM = metadata.getBootROM();
			files = bootROM.getFiles();
		}
		if (FwType.UEFIROM.equals(fwType)) {
			UEFIROM uefiROM = metadata.getUefiROM();
			files = uefiROM.getFiles();
		}

		for (SfFirmware file : files) {
			int type = Integer.parseInt(file.getType());
			int subType = Integer.parseInt(file.getSubtype());
			if (type == currentType && subType == currentSubType) {
				metaDatafile = file;
			}
		}
		return metaDatafile;
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
			logger.error("Error in reading file : "+toDownload);
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
			logger.error("Solaflare :: Invalid host. {} is a multicast address."+ url.getHost());
			throw new Exception("Multicast address can not be used.");
		}
		if (address.isLoopbackAddress()) {
			logger.error("Solaflare :: Invalid host. {} is a loopback address."+ url.getHost());
			throw new Exception("Loopback address can not be used.");
		}
		// ping host
		if (!address.isReachable(5000)) {
			logger.error("Solaflare ::  unreachable "+ url.getHost());
			throw new Exception(url.getHost() + " is NOT reachable! Please verify if the server is accessible.");
		}

		// Validate if metadata file is present at server
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        int code = connection.getResponseCode();
        if (code == 404) {
        	logger.error("Solaflare ::  FirmwareMetadata.json file not found on server "+url.getHost());
			throw new Exception("Unable to access latest binary files metadata on server "+url.getHost());
        }

		return true;
	}
}
