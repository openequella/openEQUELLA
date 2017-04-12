package com.tle.core.services;

import java.io.Serializable;

import com.tle.core.services.impl.TaskStatusImpl;

public interface TaskStatusChange<T extends TaskStatusChange<T>> extends Serializable
{
	void modifyStatus(TaskStatusImpl taskStatus);

	void merge(T newChanges);
}
