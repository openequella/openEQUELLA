package com.tle.core.services.impl;

import java.util.Collection;

import com.tle.core.services.TaskService;
import com.tle.core.services.TaskStatusChange;

public interface PrivateTaskService extends TaskService
{
	void updateTaskStatus(Task task, Collection<TaskStatusChange<?>> statusChanges, String appliesTo, String becomes);

	void messageResponse(Task task, SimpleMessage message);
}
