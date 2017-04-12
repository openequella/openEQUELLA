package com.tle.core.services;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.core.services.impl.ClusteredTask;

/**
 * The service responsible for managing tasks which are to be run once through
 * out the cluster.
 * 
 * @author jolz
 */
@NonNullByDefault
public interface TaskService
{
	/**
	 * Starts a new clustered task.
	 * 
	 * @param clusteredTask
	 * @return The UUID of the newly started task.
	 */
	String startTask(ClusteredTask clusteredTask);

	/**
	 * Return the UUID of a global clustered task, which may have already been
	 * started. If the task has already started (or is suspended), it will
	 * return the UUID of that task. If the task hasn't been started, it starts
	 * it and waits to find out what UUID it has.
	 * 
	 * @param globalTask The global clustered task.
	 * @param millis The maximum time to wait for the task to start. NOTE: ClusteredTaskService does not use this.
	 * @return The UUID of the global clustered task.
	 */
	GlobalTaskStartInfo getGlobalTask(ClusteredTask globalTask, long millis);

	void addTaskStatusListener(String taskId, TaskStatusListener listener);

	Map<String, TaskStatus> getAllStatuses();

	@Nullable
	TaskStatus getTaskStatus(String taskId);

	boolean haveTaskStatus(String taskId);

	boolean isTaskActive(String taskId);

	void askTaskChanges(Collection<String> taskId);

	String getRunningGlobalTask(String globalId);

	TaskStatus waitForTaskStatus(String taskId, long millis);

	<T> T waitForTaskSubStatus(String taskId, String subStatus, long millis);

	void postMessage(String taskId, Serializable message);

	<T> T postSynchronousMessage(String taskId, Serializable message, long millis);

}
