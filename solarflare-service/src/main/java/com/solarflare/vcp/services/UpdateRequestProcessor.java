package com.solarflare.vcp.services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.solarflare.vcp.model.UpdateRequest;

public class UpdateRequestProcessor {
	private static final Log logger = LogFactory.getLog(UpdateRequestProcessor.class);
	private ExecutorService executor;

	private UpdateRequestProcessor() {
		logger.info("Thread pool created with size of 8 threads");
		executor = Executors.newFixedThreadPool(4);
	}
	private static class UpdateRequestProcessorHolder {
		private static final UpdateRequestProcessor instance = new UpdateRequestProcessor();
	}

	public static final UpdateRequestProcessor getInstance() {
		return UpdateRequestProcessorHolder.instance;
	}

	public void addUpdateRequest(UpdateRequest updateRequest) {
		logger.info("submitting update request for " + updateRequest.getAdapterId() + " and firmware " + updateRequest.getFwType());
		if (executor.isShutdown() || executor.isTerminated()) {
			logger.info("Thread pool is not active! Creating a new one..");
			executor = Executors.newFixedThreadPool(4);
		}
		UpdateRequestThread updateThread = new UpdateRequestThread();
		updateThread.setUpdateRequest(updateRequest);
		executor.execute(updateThread);
	}

}
