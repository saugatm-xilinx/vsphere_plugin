package com.solarflare.vcp.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.solarflare.vcp.exception.SfNotFoundException;
import com.solarflare.vcp.model.AdapterTask;
import com.solarflare.vcp.model.FwType;
import com.solarflare.vcp.model.Status;
import com.solarflare.vcp.model.TaskInfo;
import com.solarflare.vcp.model.TaskState;

public class TaskManager {
	private static final Log log = LogFactory.getLog(TaskManager.class);
	Map<String, TaskInfo> taskMap = new ConcurrentHashMap<>();

	private TaskManager() {
		// Singleton instance
	}

	public TaskManager init() {
		taskMap = new ConcurrentHashMap<>();
		return this;
	}

	private static class TaskManagerHolder {
		private static final TaskManager instance = new TaskManager();
	}

	public static final TaskManager getInstance() {
		return TaskManagerHolder.instance;
	}

	/**
	 * Used to update the state of task
	 * 
	 * @param taskId
	 * @param adapterId
	 * @param status
	 */
	public synchronized void updateTaskState(String taskId, String adapterId, Status status) {
		log.info("TaskManager updateTaskState " + taskId + " status " + status);
		AdapterTask adapterTask = geAdapterTask(taskId, adapterId);
		if (adapterTask != null) {
			if (FwType.CONTROLLER.equals(status.getFirmwareType()) && adapterTask.getController() != null) {
				log.info("updating controller " + adapterTask.getController());
				adapterTask.getController().setState(status.getState());
				adapterTask.getController().setErrorMessage(status.getErrorMessage());
				adapterTask.getController().setTimeStamp(status.getTimeStamp());
			}

			if (FwType.BOOTROM.equals(status.getFirmwareType()) && adapterTask.getBootROM() != null) {
				adapterTask.getBootROM().setState(status.getState());
				adapterTask.getBootROM().setErrorMessage(status.getErrorMessage());
				adapterTask.getBootROM().setTimeStamp(status.getTimeStamp());
			}

			if (FwType.UEFIROM.equals(status.getFirmwareType())) {
				adapterTask.setUefiROM(status);
			}

			if (FwType.SUCFW.equals(status.getFirmwareType())) {
				adapterTask.setSucfw(status);
			}
		}

		log.debug("AdapterTask after update: " + adapterTask);
		log.debug("Taskinfo after update: " + getTask(taskId));
	}
	private AdapterTask geAdapterTask(String taskId, String adapterId) {
		log.info("TaskManager geAdapterTask " + taskId + " adapterId " + adapterId);
		TaskInfo taskInfo = getTask(taskId);
		log.info("found taskinfo: " + taskInfo);
		if (taskInfo != null && !taskInfo.getAdapterTasks().isEmpty())
			for (AdapterTask aTask : taskInfo.getAdapterTasks()) {
				if (aTask.getAdapterId().equals(adapterId)) {
					log.info("found adapter task: " + aTask);
					return aTask;
				}
			}
		return null;
	}

	public void updateTask(TaskInfo task) {
		addTaskInfo(task);
	}

	public void addTaskInfo(TaskInfo taskInfo) {
		log.debug("addTaskInfo start.");
		if (taskInfo == null) {
			log.error("addTaskInfo : Task is null.");
			return;
		}
		String taskId = taskInfo.getTaskid();
		if (taskId == null) {
			log.error("addTaskInfo : taskId is null.");
			return;
		}
		try {
			addUpdateTask(taskId, taskInfo);
		} catch (Exception e) {
			log.error("Error in addTaskInfo ", e);
		}

		log.info("addTaskInfo : Task( " + taskId + " ) has been stored.");
	}

	public TaskInfo getTaskInfo(String taskId) throws Exception {
		validateTaskId(taskId);

		TaskInfo task = getTask(taskId);

		if (task == null) {
			log.error("getTaskInfo : This taskInfo is null, maybe it is invalid or have bean deleted");
			throw new SfNotFoundException("getTaskInfo : This taskInfo is null, maybe it is invalid or have bean deleted.");
		}
		return task;
	}

	public TaskInfo getTaskUpdate(String taskId) throws Exception {
		log.debug("getTaskUpdate start.");
		// task id is validated in the method getTaskInfo
		TaskInfo task = getTaskInfo(taskId);
		// null object is checked in getTaskInfo() method already.
		List<AdapterTask> adapterTasks = task.getAdapterTasks();
		boolean isTaskDone = true;
		for (AdapterTask aTask : adapterTasks) {
			Status controllerStatus = aTask.getController();
			isTaskDone = isTaskDone(controllerStatus);
			if (!isTaskDone) {
				break;
			}

			Status bootROMStatus = aTask.getBootROM();
			isTaskDone = isTaskDone(bootROMStatus);
			if (!isTaskDone) {
				break;
			}

			Status UEFIStatus = aTask.getUefiROM();
			isTaskDone = isTaskDone(UEFIStatus);
			if (!isTaskDone) {
				break;
			}

			Status sucfwStatus = aTask.getSucfw();
			isTaskDone = isTaskDone(sucfwStatus);
			if (!isTaskDone) {
				break;
			}
		}
		if (isTaskDone) {
			removeTaskInfo(taskId);
		}
		log.debug("getTaskUpdate end.");
		return task;
	}

	public void removeTaskInfo(String taskId) {
		deleteTask(taskId);
	}

	public String getTaskId() {
		String id = UUID.randomUUID().toString();
		while (getTask(id) != null) {
			log.warn("Task id: " + id + " is already present. Generating another unique id.");
			id = UUID.randomUUID().toString();
		}
		log.info("getTaskId: Unique TaskId generated- " + id);
		return id;
	}

	private void validateTaskId(String taskId) throws Exception {
		if (taskId == null) {
			log.error("validateTask : taskId is null.");
			throw new SfNotFoundException("validateTask : Task ID is null.");
		}
	}

	private void addUpdateTask(String taskId, TaskInfo info) {

		taskMap.put(taskId, info);
	}

	private void deleteTask(String taskId) {
		taskMap.remove(taskId);
	}

	private TaskInfo getTask(String taskId) {
		log.info("Getting task with id: " + taskId);
		return taskMap.get(taskId);
	}

	public List<TaskInfo> getTasks() {
		List<TaskInfo> tasks = new ArrayList<>();
		for (String id : taskMap.keySet()) {
			TaskInfo task = taskMap.get(id);
			tasks.add(task);
		}
		return tasks;
	}

	private boolean isTaskDone(Status status) {
		TaskState taskState = null;
		if (status != null) {
			taskState = status.getState();
		}
		if (taskState != null && !(TaskState.Success.equals(taskState) || TaskState.Error.equals(taskState))) {
			return false;

		}
		return true;
	}
}
