package com.solarflare.vcp.services;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.cim.CIMInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.solarflare.vcp.cim.CIMHost;
import com.solarflare.vcp.cim.CIMService;
import com.solarflare.vcp.helper.SFBase64;
import com.solarflare.vcp.helper.VCenterHelper;
import com.solarflare.vcp.model.Adapter;
import com.solarflare.vcp.model.FileHeader;
import com.solarflare.vcp.model.FirmewareVersion;
import com.solarflare.vcp.model.FirmwareType;
import com.solarflare.vcp.model.Host;
import com.solarflare.vcp.model.NicBootParamInfo;
import com.solarflare.vcp.model.Status;
import com.solarflare.vcp.model.TaskStatus;
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
			CIMInstance nicInstance = null;
			for (Adapter adapter : adapterList) {
				String adapterId = adapter.getId();
				nicInstance = cim.getNICCardInstance(cimHost, adapter.getChildren().get(0).getName());
				FirmewareVersion fwVersion = adapter.getLatestVersion();
				boolean contronller = false;
				boolean bootRom = false;
				if(fwVersion.getControlerVersion() !=null)
				{
					contronller = true;
				}
				if(fwVersion.getBootROMVersion() != null)
				{
					bootRom = true;
				}
				Runnable workerForController = new FirmwareUpdateThread(serviceContent, cim, cimHost, null, data,
						nicInstance, adapterId, hostId, false, contronller, bootRom);
				executor.execute(workerForController);
				return true;
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

			boolean controller = false;
			boolean bootrom = false;
			CIMInstance fwInstance = null;
			if (FirmwareType.FIRMWARE_TYPE_MCFW.ordinal() == header.getType()) {
				fwInstance = cim.getFirmwareSoftwareInstallationInstance(cimHost);
				controller = true;
			} else if (FirmwareType.FIRMWARE_TYPE_BOOTROM.ordinal() == header.getType()) {
				// Get BootROM SF_SoftwareInstallationService instance
				fwInstance = cim.getBootROMSoftwareInstallationInstance(cimHost);
				bootrom = true;
			}

			if (fwInstance != null) {
				String tempFile = cim.startFwImageSend(cimHost, fwInstance);

				sendDataInChunks(cimHost, fwInstance, tempFile, decodedDataBytes);

				fwImageURL = new URL("file:\\" + tempFile);

				CIMInstance nicInstance = null;

				for (Adapter adapter : adapterList) {
					nicInstance = cim.getNICCardInstance(cimHost, adapter.getChildren().get(0).getName());

					Runnable workerForFW = new FirmwareUpdateThread(serviceContent, cim, cimHost, fwImageURL,
							new String(decodedDataBytes), nicInstance, adapter.getId(), hostId, true, controller, bootrom);
					executor.execute(workerForFW);
				}

				// Delete temp file after updating firmware
				boolean isRemoved = cim.removeFwImage(cimHost, fwInstance, tempFile);
				logger.info("File " + tempFile + " Removed status: " + isRemoved);
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
			logger.info("fwImagePath : "+fwImagePath);
			String data = null; // As this is update from URL
			URL fwImageURL = new URL(fwImagePath);
			ServiceContent serviceContent = VCenterHelper.getServiceContent(userSessionService, VCenterService.vimPort);
			CIMHost cimHost = service.getCIMHost(serviceContent, hostId);

			boolean readComplete = false;
			byte[] headerData = cim.readData(fwImageURL, readComplete);
			FileHeader header = cim.getFileHeader(headerData);
			logger.info("header : " + header);
			data = new String(headerData);
			boolean controller = false;
			boolean bootrom = false;
			if (FirmwareType.FIRMWARE_TYPE_MCFW.ordinal() == header.getType()) {
				controller = true;
			} else if (FirmwareType.FIRMWARE_TYPE_BOOTROM.ordinal() == header.getType()) {
				bootrom = true;
			}
			logger.info("is controller " + controller) ;
			logger.info("is BootROM " + bootrom) ;
			
			CIMInstance nicInstance = null;

			for (Adapter adapter : adapterList) {
				nicInstance = cim.getNICCardInstance(cimHost, adapter.getChildren().get(0).getName());

				Runnable workerForFW = new FirmwareUpdateThread(serviceContent, cim, cimHost, fwImageURL, data,
						nicInstance, adapter.getId(), hostId, true, controller, bootrom);
				executor.execute(workerForFW);
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

	@Override
	public List<Status> getStatus(String hostId, String adapterId) throws Exception {
		
		String cntStatusId = VCenterHelper.generateId(hostId, adapterId, MessageConstant.CONTROLLER);
		String bootStatusId = VCenterHelper.generateId(hostId, adapterId, MessageConstant.BOOTROM);
		
		List<Status> statusList = new ArrayList<>();
		List<Status> cntStatus = TaskStatus.getTaskStatus(cntStatusId);
		List<Status> boootStatus = TaskStatus.getTaskStatus(bootStatusId);
		
		statusList.addAll(cntStatus == null ? new ArrayList<Status>(): cntStatus);
		statusList.addAll(boootStatus == null ? new ArrayList<Status>(): boootStatus);
		
		return statusList;
		
	}

}
