package com.solarflare.vcp.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Debugging {

	private static final Log logger = LogFactory.getLog(Debugging.class);
	/**
	 * While adding new variable then add entry in DebugMask Enum for this
	 * variable.
	 */
	private boolean driver;
	private boolean utils;
	private boolean mgmt;
	private boolean uplink;
	private boolean transmit;
	private boolean receive;
	private boolean hardware;
	private boolean eventQueue;
	private boolean rss;
	private boolean port;
	private boolean interrupt;
	private boolean commonCode;
	private boolean filter;
	private boolean mcdi;

	/**
	 * Set the debug mask bits and return the final value as integer
	 * 
	 */
	public int getDebugMask() {
		logger.info("Solarflare:: getDebugMask");
		int mask = 0;
		Field[] fields = this.getClass().getDeclaredFields();
		// Get field value, if it true then set it's corresponding bit in
		// bitMask
		for (Field field : fields) {
			try {
				int distance = DebugMask.getDistance(field.getName());
				logger.debug("distance : " + distance + " Field Name : " + field.getName());
				if (distance != -1 && Boolean.compare(field.getBoolean(this), true) == 0) {
					mask = mask | Integer.rotateLeft(1, distance);
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				logger.error(e.getMessage());
			}
		}
		logger.info("Solarflare:: getDebugMask returned : " + mask);
		return mask;
	}

	public Debugging decodeDebugMask(int mask) {
		logger.info("Solarflare:: decodeDebugMask for mask : " + mask);
		Debugging debug = new Debugging();
		for (DebugMask value : DebugMask.values()) {
			logger.debug(value.getName() + " = " + value.getDistance());
			if ((mask & Integer.rotateLeft(1, value.getDistance())) != 0) {
				Method method;
				String methodName = getMethodName(value.getName());
				// Boolean parameter type
				Class<?>[] boolParam = new Class[1];
				boolParam[0] = Boolean.TYPE;
				try {
					method = debug.getClass().getDeclaredMethod(methodName, boolParam);
					method.invoke(debug, true);
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		}
		return debug;
	}

	private String getMethodName(String fieldName) {
		StringBuilder sb = new StringBuilder("set");
		sb.append(Character.toUpperCase(fieldName.charAt(0)));
		sb.append(fieldName.substring(1));
		return sb.toString();
	}

	/**
	 * Debug Mask Enum
	 * 
	 * @author Praveen
	 *
	 */
	enum DebugMask {
		SFVMK_DEBUG_DRIVER(0, "driver"), 
		SFVMK_DEBUG_UTILS(1, "utils"), 
		SFVMK_DEBUG_MGMT(2, "mgmt"), 
		SFVMK_DEBUG_UPLINK(3, "uplink"), 
		SFVMK_DEBUG_RSS(4, "rss"), 
		SFVMK_DEBUG_INTR(5, "interrupt"), 
		SFVMK_DEBUG_HW(6, "hardware"), 
		SFVMK_DEBUG_TX(7, "transmit"), 
		SFVMK_DEBUG_RX(8, "receive"), 
		SFVMK_DEBUG_EVQ(9, "eventQueue"), 
		SFVMK_DEBUG_PORT(10, "port"), 
		SFVMK_DEBUG_MCDI(11, "mcdi"), 
		SFVMK_DEBUG_FILTER(12, "filter"), 
		SFVMK_DEBUG_COMMON_CODE(13, "commonCode");

		private int distance;
		private String name;
		private static Map<String, DebugMask> lookupMap = new ConcurrentHashMap<String, DebugMask>();

		static {
			for (DebugMask debugMask : DebugMask.values()) {
				lookupMap.put(debugMask.getName(), debugMask);
			}
			lookupMap = Collections.unmodifiableMap(lookupMap);
		}

		DebugMask(int value, String name) {
			this.distance = value;
			this.name = name;
		}

		public static DebugMask lookupByName(String name) {
			return lookupMap.get(name);
		}

		/**
		 * Returns -1 if not found, otherwise return the distance
		 * 
		 * @param name
		 * @return
		 */
		public static int getDistance(String name) {
			DebugMask mask = lookupByName(name);
			return mask == null ? -1 : mask.getDistance();
		}

		public int getDistance() {
			return distance;
		}

		public String getName() {
			return name;
		}

	}

	public boolean isFilter() {
		return filter;
	}

	public void setFilter(boolean filter) {
		this.filter = filter;
	}

	public boolean isDriver() {
		return driver;
	}

	public void setDriver(boolean driver) {
		this.driver = driver;
	}

	public boolean isUtils() {
		return utils;
	}

	public void setUtils(boolean utils) {
		this.utils = utils;
	}

	public boolean isMgmt() {
		return mgmt;
	}

	public void setMgmt(boolean mgmt) {
		this.mgmt = mgmt;
	}

	public boolean isUplink() {
		return uplink;
	}

	public void setUplink(boolean uplink) {
		this.uplink = uplink;
	}

	public boolean isTransmit() {
		return transmit;
	}

	public void setTransmit(boolean transmit) {
		this.transmit = transmit;
	}

	public boolean isReceive() {
		return receive;
	}

	public void setReceive(boolean receive) {
		this.receive = receive;
	}

	public boolean isHardware() {
		return hardware;
	}

	public void setHardware(boolean hardware) {
		this.hardware = hardware;
	}

	public boolean isEventQueue() {
		return eventQueue;
	}

	public void setEventQueue(boolean eventQueue) {
		this.eventQueue = eventQueue;
	}

	public boolean isRss() {
		return rss;
	}

	public void setRss(boolean rss) {
		this.rss = rss;
	}

	public boolean isPort() {
		return port;
	}

	public void setPort(boolean port) {
		this.port = port;
	}

	public boolean isInterrupt() {
		return interrupt;
	}

	public void setInterrupt(boolean interrupt) {
		this.interrupt = interrupt;
	}

	public boolean isCommonCode() {
		return commonCode;
	}

	public void setCommonCode(boolean commonCode) {
		this.commonCode = commonCode;
	}

	public boolean isMcdi() {
		return mcdi;
	}

	public void setMcdi(boolean mcdi) {
		this.mcdi = mcdi;
	}

	@Override
	public String toString() {
		return "Debugging [driver=" + driver + ", utils=" + utils + ", mgmt=" + mgmt + ", uplink=" + uplink
				+ ", transmit=" + transmit + ", receive=" + receive + ", hardware=" + hardware + ", eventQueue="
				+ eventQueue + ", rss=" + rss + ", port=" + port + ", interrupt=" + interrupt + ", commonCode="
				+ commonCode + ", filter=" + filter + ", mcdi=" + mcdi + "]";
	}

}
