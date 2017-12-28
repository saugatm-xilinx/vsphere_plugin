package com.solarflare.vcp.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.solarflare.vcp.model.AdapterTask;
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
			throw new Exception("getTaskInfo : This taskInfo is null, maybe it is invalid or have bean deleted.");
		}
		return task;
	}

	public TaskInfo getTaskUpdate(String taskId) throws Exception{
		log.debug("getTaskUpdate start.");
		// task id is validated in the method getTaskInfo
		TaskInfo task = getTaskInfo(taskId);
		// null object is checked in getTaskInfo() method already.
		List<AdapterTask> adapterTasks = task.getAdapterTasks();
		boolean isTaskDone = true;
		for (AdapterTask aTask : adapterTasks) {
			Status controllerStatus = aTask.getController();
			TaskState taskStateController = null;
			if (controllerStatus != null) {
				taskStateController = controllerStatus.getState();
			}
			if (taskStateController != null && !(TaskState.Success.equals(taskStateController)
					|| TaskState.Error.equals(taskStateController))) {
				isTaskDone = false;
				break;
			}

			Status bootROMStatus = aTask.getBootROM();
			TaskState taskStateBootROM = null;
			if (bootROMStatus != null) {
				taskStateBootROM = bootROMStatus.getState();
			}
			if (taskStateBootROM != null
					&& !(TaskState.Success.equals(taskStateBootROM) || TaskState.Error.equals(taskStateBootROM))) {
				isTaskDone = false;
				break;
			}

			Status UEFIStatus = aTask.getUefiROM();
			TaskState taskStateUEFI = null;
			if (UEFIStatus != null) {
				taskStateUEFI = UEFIStatus.getState();
			}
			if (taskStateUEFI != null
					&& !(TaskState.Success.equals(taskStateUEFI) || TaskState.Error.equals(taskStateUEFI))) {
				isTaskDone = false;
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
			throw new Exception("validateTask : Task ID is null.");
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
		for(String id : taskMap.keySet()){
			TaskInfo task = taskMap.get(id);
			tasks.add(task);
		}
		return tasks;
	}
}
