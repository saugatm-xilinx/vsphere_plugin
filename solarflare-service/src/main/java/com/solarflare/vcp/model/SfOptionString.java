package com.solarflare.vcp.model;

import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.solarflare.vcp.vim.SimpleTimeCounter;

public class SfOptionString {

	private static final Log logger = LogFactory.getLog(SfOptionString.class);
	String netQCount;
	String rssQCount;
	String vxlanOffload;
	String geneveOffload;
	String debugMask;

	private static final String DEFAULT_NET_Q_COUNT = "8";
	// VSPPLUG-282 : changed default value of RSS to 1
	private static final String DEFAULT_RSS_Q_COUNT = "1";
	private static final String DEFAULT_VX_LAN_OFFLOAD = "1";
	private static final String DEFAULT_GENEVE_OFFLOAD = "1";
	private static final String DEFAULT_DEBUG_MASK = "0x00";

	public SfOptionString() {
		this.netQCount = DEFAULT_NET_Q_COUNT;
		this.rssQCount = DEFAULT_RSS_Q_COUNT;
		this.vxlanOffload = DEFAULT_VX_LAN_OFFLOAD;
		this.debugMask = DEFAULT_DEBUG_MASK;
		this.geneveOffload = DEFAULT_GENEVE_OFFLOAD;
	}

	public String getNetQCount() {
		return netQCount;
	}

	public void setNetQCount(String netQCount) {
		this.netQCount = netQCount;
	}

	public String getRssQCount() {
		return rssQCount;
	}

	public void setRssQCount(String rssQCount) {
		this.rssQCount = rssQCount;
	}

	public String getVxlanOffload() {
		return vxlanOffload;
	}

	public void setVxlanOffload(String vxlanOffload) {
		this.vxlanOffload = vxlanOffload;
	}


	public String getGeneveOffload() {
		return geneveOffload;
	}

	public void setGeneveload(String geneveOffload) {
		this.geneveOffload = geneveOffload;
	}

	public String getDebugMask() {
		return debugMask;
	}

	public void setDebugMask(String debugMask) {
		this.debugMask = debugMask;
	}

	@Override
	public String toString() {
		return "netQCount=" + netQCount + " rssQCount=" + rssQCount + " vxlanOffload=" + vxlanOffload
			    +" geneveOffload=" + geneveOffload + " debugMask=" + debugMask;
	}

	/**
	 * Returns SfOptionString for given Host Configuration object
	 * 
	 * @param hostConfigObj
	 * @return
	 */
	public SfOptionString getOptionString(HostConfiguration hostConfigObj) {
		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: getOptionString");
		logger.info("Solarflare:: getOptionString");
		SfOptionString optionString = new SfOptionString();
		if (hostConfigObj != null) {
			// Validate Host Configuration object
			HostConfiguration validHostConfigObj = getValidHostConfig(hostConfigObj);

			int netQCount = validHostConfigObj.getNetQueue().getNetQueueCount();
			int rssQCount = validHostConfigObj.getNetQueue().getRss();
			boolean vxLANOffload = validHostConfigObj.getOverlay().isVxlanOffloadEnable();
			boolean geneveOffloadv_val = validHostConfigObj.getOverlay().isGeneveOffloadEnable();
			int debug = validHostConfigObj.getDebuggingMask().getDebugMask();

			optionString.netQCount = Integer.toString(netQCount);
			optionString.rssQCount = Integer.toString(rssQCount);
			optionString.vxlanOffload = vxLANOffload ? "1" : "0";
			optionString.geneveOffload = geneveOffloadv_val ? "1" : "0";
			optionString.debugMask = "0x" + Integer.toHexString(debug);
		} else {
			logger.error("Input param - Host Configuration object is null");
		}
		timer.stop();
		return optionString;
	}

	/**
	 * Validate and return valid HostConfig object
	 * 
	 * @return
	 */
	private HostConfiguration getValidHostConfig(HostConfiguration hostConfigObj) {
		logger.info("Solarflare:: getValidHostConfig");
		HostConfiguration validHostConfig = hostConfigObj;

		// Get valid NetQueue
		NetQueue netQueue = validHostConfig.getNetQueue();
		validHostConfig.setNetQueue(getValidNetQueue(netQueue));

		// Get valid Overlay
		Overlay overlay = validHostConfig.getOverlay();
		validHostConfig.setOverlay(getValidOverlay(overlay));

		// Get valid Debugging
		Debugging debug = validHostConfig.getDebuggingMask();
		validHostConfig.setDebuggingMask(getValidDebugging(debug));

		return validHostConfig;
	}

	private NetQueue getValidNetQueue(NetQueue netQueue) {
		logger.info("Solarflare:: getValidNetQueue");
		NetQueue validNetQueue = netQueue;
		// Set valid Net Q Count
		if (validNetQueue == null) {
			validNetQueue = new NetQueue();
		}
		int netQCount = validNetQueue.getNetQueueCount();
		if (1 > netQCount || netQCount > 15) {
			netQCount = Integer.parseInt(DEFAULT_NET_Q_COUNT);
			validNetQueue.setNetQueueCount(netQCount);
		}

		// Set valid rss Q Count
		int rssQCount = validNetQueue.getRss();
		if (1 > rssQCount || rssQCount > 4) {
			rssQCount = Integer.parseInt(DEFAULT_RSS_Q_COUNT);
			validNetQueue.setRss(rssQCount);
		}
		return validNetQueue;
	}

	private Overlay getValidOverlay(Overlay overlay) {
		logger.info("Solarflare:: getValidOverlay");
		Overlay validOverlay = overlay;
		if (validOverlay == null) {
			validOverlay = new Overlay();
			validOverlay.setVxlanOffloadEnable(true); // Default is true;
			validOverlay.setGeneveOffloadEnable(true);
		}
		return validOverlay;
	}

	private Debugging getValidDebugging(Debugging debug) {
		logger.info("Solarflare:: getValidDebugging");
		Debugging validDebug = debug;
		if (validDebug == null) {
			validDebug = new Debugging();
			validDebug = validDebug.decodeDebugMask(0); // Default
		}
		return validDebug;
	}

	/**
	 * Returns HostConfiguration object corresponding to given option string
	 * 
	 * @param optionString
	 * @return
	 * @throws Exception
	 */
	public HostConfiguration getHostConfiguration(String optionString) throws Exception {
		SimpleTimeCounter timer = new SimpleTimeCounter("Solarflare:: getHostConfiguration");
		logger.info("Solarflare:: getHostConfiguration");

		HostConfiguration hostConfigObj = new HostConfiguration();
		SfOptionString sfOptionString = getSfOptionString(optionString);

		NetQueue netQueue = new NetQueue();
		netQueue.setNetQueueCount(Integer.parseInt(sfOptionString.getNetQCount()));
		netQueue.setRss(Integer.parseInt(sfOptionString.getRssQCount()));

		Overlay overlay = new Overlay();
		overlay.setVxlanOffloadEnable(sfOptionString.getVxlanOffload().equals("1") ? true : false);
		overlay.setGeneveOffloadEnable(sfOptionString.getGeneveOffload().equals("1") ? true : false);

		Debugging debug = new Debugging();
		debug = debug.decodeDebugMask(Integer.decode(sfOptionString.getDebugMask()));

		hostConfigObj.setNetQueue(netQueue);
		hostConfigObj.setOverlay(overlay);
		hostConfigObj.setDebuggingMask(debug);

		timer.stop();
		return hostConfigObj;
	}

	private SfOptionString getSfOptionString(String optionString) throws Exception {
		logger.info("Solarflare:: getSfOptionString");
		SfOptionString sfOptionString = new SfOptionString();
		Field[] fields = this.getClass().getDeclaredFields();
		//VCPPLUG-336 - Added null check
		if (optionString != null && !optionString.isEmpty()) {
			String options[] = optionString.split("\\s+");
			for (String opt : options) {
				String optNValue[] = opt.split("=");
				String fieldName = optNValue[0];
				String fieldValue = optNValue[1];
				for (Field field : fields) {
					if (field.getName().equals(fieldName)) {
						field.set(sfOptionString, fieldValue);
					}
				}
			}
		}

		return sfOptionString;
	}

}
