package com.solarflare.vcp.services;

import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.cim.CIMInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.solarflare.vcp.cim.CIMConstants;
import com.solarflare.vcp.cim.CIMHost;
import com.solarflare.vcp.cim.CIMService;
import com.solarflare.vcp.helper.MetadataHelper;
import com.solarflare.vcp.helper.SFBase64;
import com.solarflare.vcp.helper.VCenterHelper;
import com.solarflare.vcp.model.Adapter;
import com.solarflare.vcp.model.FileHeader;
import com.solarflare.vcp.model.FirmwareType;
import com.solarflare.vcp.model.Host;
import com.solarflare.vcp.model.NicBootParamInfo;
import com.solarflare.vcp.model.SfFirmware;
import com.solarflare.vcp.vim25.VCenterService;
import com.vmware.vim25.ServiceContent;
import com.vmware.vise.security.ClientSessionEndListener;
import com.vmware.vise.usersession.UserSessionService;

public class HostAdapterServiceImpl implements HostAdapterService, ClientSessionEndListener {
	private static final Log logger = LogFactory.getLog(HostAdapterServiceImpl.class);

	private UserSessionService userSessionService;
	private static ExecutorService executor = Executors.newFixedThreadPool(10);

	VCenterService service = new VCenterService();
	CIMService cim = new CIMService();

	@Autowired
	public HostAdapterServiceImpl(UserSessionService session) {
		userSessionService = session;
	}

	@Override
	public List<Host> getHostList() throws Exception {
		List<Host> hostList = null;
		try {
			hostList = service.getHostsList(userSessionService);
		} catch (Exception e) {
			throw e;
		}
		return hostList;
	}

	@Override
	public Host getHostById(String hostId) throws Exception {
		Host host = null;
		try {
			host = service.getHostById(userSessionService, hostId);
		} catch (Exception e) {
			throw e;
		}
		return host;

	}

	@Override
	public boolean updateFirmwareToLatest(List<Adapter> adapterList, String hostId) throws Exception {
		logger.info("Start updating firmware adapter");
		try {
			String data = null; // as this is Update from URL
			ServiceContent serviceContent = VCenterHelper.getServiceContent(userSessionService, VCenterService.vimPort);
			CIMHost cimHost = service.getCIMHost(serviceContent, hostId);
			// Get Controller SF_SoftwareInstallationService instance
			CIMInstance svc_mcfw_inst = cim.getFirmwareSoftwareInstallationInstance(cimHost);
			// Get BootROM SF_SoftwareInstallationService instance
			CIMInstance svc_bootrom_inst = cim.getBootROMSoftwareInstallationInstance(cimHost);
			// TODO taken for testing
			// URL fwImagePath = new URL("http://10.101.10.132" +
			// CIMConstants.CONTROLLER_FW_IMAGE_PATH);

			CIMInstance nicInstance = null;
			MetadataHelper metadataHelper = new MetadataHelper();
			for (Adapter adapter : adapterList) {
				nicInstance = cim.getNICCardInstance(cimHost, adapter.getChildren().get(0).getName());
				// get the URL of latest firmware file
				String urlPath = cim.getPluginURL(serviceContent, VCenterService.vimPort, CIMConstants.PLUGIN_KEY);
				URL pluginURL = new URL(urlPath);
				String filePath = null;
				boolean isController = true;
				SfFirmware file = metadataHelper.getMetaDataForAdapter(serviceContent, VCenterService.vimPort, cimHost,
						svc_mcfw_inst, nicInstance, isController);
				if (file != null) {
					filePath = file.getPath();
				}
				// TODO : check for https certificate warning
				// URL fwImagePath = new
				// URL(pluginURL.getProtocol(),pluginURL.getHost(),pluginURL.getPort(),filePath);

				// TODO check version current version and version from file for
				// both controller and BootRom
				URL fwImageURL = new URL("http", pluginURL.getHost(), filePath);
				// FirmwareUpdateThread updateFirmware = new
				// FirmwareUpdateThread(cim, cimHost, fwImagePath,
				// svc_mcfw_inst,
				// nicInstance, "cnt-"+adapter.getId());
				// Thread thread = new Thread(updateFirmware);
				// thread.start();

				Runnable workerForController = new FirmwareUpdateThread(cim, cimHost, fwImageURL, data, svc_mcfw_inst,
						nicInstance, "cnt-" + adapter.getId());
				executor.execute(workerForController);

				// Update BootROM
				isController = false;
				file = metadataHelper.getMetaDataForAdapter(serviceContent, VCenterService.vimPort, cimHost,
						svc_bootrom_inst, nicInstance, isController);
				if (file != null) {
					filePath = file.getPath();
				}
				// TODO : check for https certificate warning
				// URL fwImagePath = new
				// URL(pluginURL.getProtocol(),pluginURL.getHost(),pluginURL.getPort(),filePath);
				fwImageURL = new URL("http", pluginURL.getHost(), filePath);

				Runnable workerForBoot = new FirmwareUpdateThread(cim, cimHost, fwImageURL, data, svc_bootrom_inst,
						nicInstance, "boot-" + adapter.getId());
				executor.execute(workerForBoot);

			}
		} catch (Exception e) {
			throw e;
		}
		return false;
	}

	@Override
	public boolean customUpdateFirmwareFromLocal(List<Adapter> adapterList, String hostId, String base64Data)
			throws Exception {
		logger.info("Start updating firmware adapter");
		try {
			URL fwImageURL = null;
			ServiceContent serviceContent = VCenterHelper.getServiceContent(userSessionService, VCenterService.vimPort);
			CIMHost cimHost = service.getCIMHost(serviceContent, hostId);
			CIMInstance fwInstance = null;

			byte[] dataBytes = base64Data.getBytes();
			// Decode this data using java's decoder
			Base64.Decoder decoder = Base64.getDecoder();
			byte[] decodedDataBytes = decoder.decode(dataBytes);

			// Get Header info from data
			byte[] headerData = Arrays.copyOf(decodedDataBytes, 40); // header
																		// size
																		// is 40
																		// bytes
			FileHeader header = cim.getFileHeader(headerData);

			if (FirmwareType.FIRMWARE_TYPE_MCFW.ordinal() == header.getType()) {
				// Get Controller SF_SoftwareInstallationService instance
				fwInstance = cim.getFirmwareSoftwareInstallationInstance(cimHost);
			} else if (FirmwareType.FIRMWARE_TYPE_BOOTROM.ordinal() == header.getType()) {
				// Get BootROM SF_SoftwareInstallationService instance
				fwInstance = cim.getBootROMSoftwareInstallationInstance(cimHost);
			}

			if (fwInstance != null) {
				String tempFile = cim.startFwImageSend(cimHost, fwInstance);

				sendDataInChunks(cimHost, fwInstance, tempFile, decodedDataBytes);

				fwImageURL = new URL("file:\\" + tempFile);

				CIMInstance nicInstance = null;

				for (Adapter adapter : adapterList) {
					nicInstance = cim.getNICCardInstance(cimHost, adapter.getChildren().get(0).getName());

					Runnable workerForFW = new FirmwareUpdateThread(cim, cimHost, fwImageURL,
							new String(decodedDataBytes), fwInstance, nicInstance, "fw-" + adapter.getId());
					executor.execute(workerForFW);
				}

				// Delete temp file after updating firmware
				boolean isRemoved = cim.removeFwImage(cimHost, fwInstance, tempFile);
			} else {
				// TODO : log error for invalid firmware file
			}

		} catch (Exception e) {
			throw e;
		}
		return false;
	}

	@Override
	public boolean customUpdateFirmwareFromURL(List<Adapter> adapterList, String hostId, String fwImagePath)
			throws Exception {
		logger.info("Start updating firmware adapter");
		try {
			String data = null; // As this is update from URL
			URL fwImageURL = new URL(fwImagePath);
			ServiceContent serviceContent = VCenterHelper.getServiceContent(userSessionService, VCenterService.vimPort);
			CIMHost cimHost = service.getCIMHost(serviceContent, hostId);
			CIMInstance fwInstance = null;

			boolean readComplete = false;
			byte[] headerData = cim.readData(fwImageURL, readComplete);
			FileHeader header = cim.getFileHeader(headerData);
			data = new String(headerData);
			
			if (FirmwareType.FIRMWARE_TYPE_MCFW.ordinal() == header.getType()) {
				// Get Controller SF_SoftwareInstallationService instance
				fwInstance = cim.getFirmwareSoftwareInstallationInstance(cimHost);
			} else if (FirmwareType.FIRMWARE_TYPE_BOOTROM.ordinal() == header.getType()) {
				// Get BootROM SF_SoftwareInstallationService instance
				fwInstance = cim.getBootROMSoftwareInstallationInstance(cimHost);
			}

			if (fwInstance != null) {
				CIMInstance nicInstance = null;

				for (Adapter adapter : adapterList) {
					nicInstance = cim.getNICCardInstance(cimHost, adapter.getChildren().get(0).getName());

					Runnable workerForFW = new FirmwareUpdateThread(cim, cimHost, fwImageURL, data, fwInstance,
							nicInstance, "fw-" + adapter.getId());
					executor.execute(workerForFW);
				}
			}else{
				//TODO log error msg for invalid firmware URL
			}
		} catch (Exception e) {
			throw e;
		}
		return false;
	}

	@Override
	public void sessionEnded(String clientId) {
		logger.info("Logging out client session - " + clientId);

		// Clean up all session specific resources.
		// Logout from any session specific services.

	}

	@Override
	public List<Adapter> getHostAdapters(String hostId) throws Exception {

		List<Adapter> adapters = service.getAdapters(userSessionService, hostId);
		return adapters;

	}

	@Override
	public NicBootParamInfo getNicParamInfo(String hostId, String nicId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean validateTypeAndSubTupe(String file, boolean isLocal) throws Exception {
		return false;
	}

	private void sendDataInChunks(CIMHost cimHost, CIMInstance fwInstance, String tempFile, byte[] decodedDataBytes) {
		// Send/write this data to temp file on host
		SFBase64 sfBase64 = new SFBase64();
		int chunkSize = 100000;
		int index;
		for (index = chunkSize; index < decodedDataBytes.length; index += chunkSize) {
			byte[] temp = Arrays.copyOf(decodedDataBytes, index);
			int encodeSize = sfBase64.base64_enc_size(index);
			byte[] encoded = new byte[encodeSize];
			// using SFBase64 to encode data
			encoded = sfBase64.base64_encode(temp, index);
			cim.sendFWImageData(cimHost, fwInstance, new String(encoded), tempFile);
		}

		// send remaining last chunk
		index -= chunkSize;
		byte[] temp = Arrays.copyOfRange(decodedDataBytes, index, decodedDataBytes.length);
		int encodeSize = sfBase64.base64_enc_size(index);
		byte[] encoded = new byte[encodeSize];
		encoded = sfBase64.base64_encode(temp, index);
		cim.sendFWImageData(cimHost, fwInstance, new String(encoded), tempFile);
	}

}
