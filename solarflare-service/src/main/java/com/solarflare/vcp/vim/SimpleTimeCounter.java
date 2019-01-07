
package com.solarflare.vcp.vim;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simplistic time counter for code profiling
 */
public class SimpleTimeCounter {
	private static Log log = LogFactory.getLog(SimpleTimeCounter.class);
	private String name;
	private long startTime;
	public static final boolean debugFlag = false;

	/**
	 * Creates and starts the counter Side affect: the start time is printed to
	 * the log
	 * 
	 * @param name:
	 *            name of code segment to be profiled, which will be printed in
	 *            the log
	 */
	public SimpleTimeCounter(String name) {
		start(name);
	}

	/**
	 * Stops and starts the counter for a different code segment. This allows
	 * the counter to be re-used
	 * 
	 * @param name:
	 *            name of code segment to be profiled, which will be printed in
	 *            the log
	 */
	public long reset(String name) {
		stop();
		return start(name);
	}

	/**
	 * Starts the counter. Side affect: the start time is printed to the log
	 * 
	 * @param name:
	 *            name of code segment to be profiled, which will be printed in
	 *            the log
	 */
	public long start(String name) {
		if (debugFlag) {
		    this.name = name;
		    startTime = System.currentTimeMillis();
		    log.info("TIMER STARTED: " + name);
		    return startTime;
		}
		return 0;
	}

	/**
	 * Stops the counter. Side affect: the stop time and time difference since
	 * start is printed to the log
	 */
	public long stop() {
		if (debugFlag) {
		    long stopTime = System.currentTimeMillis();
		    long timeTaken = stopTime - startTime;
		    log.info("TIMER STOPPED: " + name);
		    log.info("TIME TAKEN: " + name + ": " + (timeTaken / 1000.0));
		    return timeTaken;
		}
		return 0;
	}
}
