package com.solarflare.vcp.services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.solarflare.vcp.model.UpdateRequest;

public class UpdateRequestProcessor {
	
	private static ExecutorService executor = Executors.newFixedThreadPool(1);
	
	private UpdateRequestProcessor(){
		
	}
	private static class UpdateRequestProcessorHolder {
		private static final UpdateRequestProcessor instance = new UpdateRequestProcessor();
	}

	public static final UpdateRequestProcessor getInstance() {
		return UpdateRequestProcessorHolder.instance;
	}

	
	public void addUpdateRequest(UpdateRequest updateRequest){
		if(executor.isShutdown() || executor.isTerminated()){
			executor = Executors.newFixedThreadPool(1);
		}
		UpdateRequestThread updateThread = new UpdateRequestThread();
		updateThread.setUpdateRequest(updateRequest);
		executor.execute(updateThread);
	}

}
