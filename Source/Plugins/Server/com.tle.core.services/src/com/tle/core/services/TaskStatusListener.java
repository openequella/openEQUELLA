package com.tle.core.services;


public interface TaskStatusListener
{
	void taskStatusChanged(String taskId, TaskStatus taskStatus);
}
