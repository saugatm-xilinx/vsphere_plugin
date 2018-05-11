package com.solarflare.vcp.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class TaskStatus {

	public static Map<String, List<Status>> statusMap = new ConcurrentHashMap<>();

	private static final long DEFAULT_CLEANUP_TIMEOUT = 60 * 1000; // timeout in
																	// milliseconds
	private Timer timer;

	private TaskStatus() {
		restartTimer();
	}
	class StatusCleanupTask extends TimerTask {
		public void run() {
			if (!statusMap.isEmpty())
				removeFromMap();
		}
	}

	private void removeFromMap() {
		// first find cleanup candidate ids
		List<String> ids = getCleanupIds();
		for (String key : ids)
			statusMap.remove(key);
	}

	private List<String> getCleanupIds() {

		List<String> cleanupIds = new ArrayList<>();
		Long currentTS = System.currentTimeMillis();

		for (String key : statusMap.keySet()) {
			List<Status> statusList = statusMap.get(key);
			for(Status status: statusList)
			{
				if (status != null && status.getTimeStamp() != null && status.getTimeStamp() > 0) {
					if (currentTS - status.getTimeStamp() >= DEFAULT_CLEANUP_TIMEOUT) {
						cleanupIds.add(key);
					}
				}
			}
		}
		return cleanupIds;
	}

	private void stopTimer() {
		if (timer != null) {
			timer.cancel();
		}
	}
	/*
	 * Cancel any existing timer task and start a new one.
	 */
	private boolean restartTimer() {
		stopTimer();
		timer = new Timer(true);

		try {
			timer.schedule(new StatusCleanupTask(), DEFAULT_CLEANUP_TIMEOUT, DEFAULT_CLEANUP_TIMEOUT);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static List<Status> getTaskStatus(String id) {
		return statusMap.get(id);
	}

	public static void updateTaskStatus(String id, String uploadStatus, String message , String type) {
		Status status = new Status(uploadStatus, message, type);
		List<Status> statusList = new ArrayList<>();
		statusList.add(status);
		statusMap.put(id, statusList);
	}
}
