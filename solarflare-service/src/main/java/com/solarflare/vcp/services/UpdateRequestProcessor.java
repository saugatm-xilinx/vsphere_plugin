package com.solarflare.vcp.services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.solarflare.vcp.model.UpdateRequest;

public class UpdateRequestProcessor {
	private static final Log logger = LogFactory.getLog(UpdateRequestProcessor.class);
	private ExecutorService executor;
	private static final int DEFAULT_POOL_SIZE = 8;

	private UpdateRequestProcessor() {
		
	}

	private static class UpdateRequestProcessorHolder {
		private static final UpdateRequestProcessor instance = new UpdateRequestProcessor();
	}

	public static final UpdateRequestProcessor getInstance() {
		return UpdateRequestProcessorHolder.instance;
	}

	public void init(int poolSize) {
		
		if (poolSize <= 0) {
			poolSize = 1;
		}
		if (poolSize > DEFAULT_POOL_SIZE) {
			poolSize = DEFAULT_POOL_SIZE;
		}
		
		if (executor == null || executor.isShutdown() || executor.isTerminated()) {
			logger.info("Creating Thread pool created with size : "+poolSize);
			executor = Executors.newFixedThreadPool(poolSize);
		}
	}

	public void addUpdateRequest(UpdateRequest updateRequest) {
		logger.info("submitting update request for " + updateRequest.getAdapterId() + " and firmware "
				+ updateRequest.getFwType());
		if (executor == null || executor.isShutdown() || executor.isTerminated()) {
			logger.info("Thread pool is not active! Creating a new one..");
			executor = Executors.newFixedThreadPool(DEFAULT_POOL_SIZE); 
		}
		UpdateRequestThread updateThread = new UpdateRequestThread();
		updateThread.setUpdateRequest(updateRequest);
		executor.execute(updateThread);
	}

	public void shutdown() {
		if(executor != null){
			logger.info("Shutdown Thread Pool");
			executor.shutdown();
			
			try {
				executor.awaitTermination(10,TimeUnit.MINUTES);
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
		}
				
	}
}
