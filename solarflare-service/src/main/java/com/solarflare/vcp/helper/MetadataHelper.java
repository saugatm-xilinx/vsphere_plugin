package com.solarflare.vcp.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.cim.CIMInstance;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.solarflare.vcp.cim.CIMConstants;
import com.solarflare.vcp.cim.CIMHost;
import com.solarflare.vcp.cim.CIMService;
import com.solarflare.vcp.model.BinaryFiles;
import com.solarflare.vcp.model.BootROM;
import com.solarflare.vcp.model.Controller;
import com.solarflare.vcp.model.SfFirmware;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.VimPortType;

public class MetadataHelper {

	public BinaryFiles getMetadata(URL filePath) throws MalformedURLException {

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		byte[] jsonData = readData(filePath, true);
		String jsonString = new String(jsonData);
		JsonReader reader = new JsonReader(new StringReader(jsonString));
		BinaryFiles fwMetaData = gson.fromJson(reader, BinaryFiles.class);

		return fwMetaData;
	}

	public SfFirmware getMetaDataForAdapter(ServiceContent serviceContent, VimPortType vimPort, CIMHost cimHost,
			CIMInstance sfFWInstance, CIMInstance nicInstance, boolean isController)
			throws MalformedURLException, RuntimeFaultFaultMsg {

		SfFirmware metaDatafile = null;
		CIMService cim = new CIMService();

		Map<String, String> params = cim.getRequiredFwImageName(cimHost, sfFWInstance, nicInstance);

		int currentType = Integer.parseInt(params.get(CIMConstants.TYPE));
		int currentSubType = Integer.parseInt(params.get(CIMConstants.SUB_TYPE));

		// get version for given Controller from metadata file
		String urlPath = cim.getPluginURL(serviceContent, vimPort, CIMConstants.PLUGIN_KEY);
		URL pluginURL = new URL(urlPath);

		// TODO : check for https certificate warning
		// URL controllerFWImagePath = new
		// URL(pluginURL.getProtocol(),pluginURL.getHost(),pluginURL.getPort(),CONTROLLER_FW_IMAGE_PATH);
		URL metaDataFilePath = new URL("http", pluginURL.getHost(), CIMConstants.METADATA_PATH);

		MetadataHelper metadataHelper = new MetadataHelper();
		BinaryFiles metadata = metadataHelper.getMetadata(metaDataFilePath);
		List<SfFirmware> files = null;
		if (isController) {
			Controller controller = metadata.getController();
			files = controller.getFiles();
		} else {
			BootROM bootROM = metadata.getBootROM();
			files = bootROM.getFiles();
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

	// TODO : Cleanup
	/*
	 * public static void main(String[] args) throws MalformedURLException {
	 * MetadataHelper obj = new MetadataHelper();
	 * 
	 * URL filePath = new
	 * URL("http","10.101.10.132",CIMConstants.METADATA_PATH); BinaryFiles
	 * fwMetaData = obj.getMetadata(filePath); // String json =
	 * gson.toJson(createfile()); System.out.println(fwMetaData); }
	 */
	private byte[] readData(URL toDownload, boolean readComplete) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try {
			byte[] chunk = new byte[1000];
			int bytesRead;
			InputStream stream = toDownload.openStream();
			if (readComplete) {
				while ((bytesRead = stream.read(chunk)) > 0) {
					outputStream.write(chunk, 0, bytesRead);
				}
			} else {
				bytesRead = stream.read(chunk);
				outputStream.write(chunk, 0, bytesRead);
			}
		} catch (IOException e) {
			return null;
		}

		return outputStream.toByteArray();
	}

}
