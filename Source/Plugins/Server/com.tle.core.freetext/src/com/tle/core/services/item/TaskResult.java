package com.tle.core.services.item;

import com.tle.beans.item.ItemIdKey;

public class TaskResult extends FreetextResult
{
	private static final long serialVersionUID = 1L;

	private final String taskId;

	public TaskResult(ItemIdKey key, String taskId, float relevance, boolean sortByRelevance)
	{
		super(key, relevance, sortByRelevance);
		this.taskId = taskId;
	}

	public String getTaskId()
	{
		return taskId;
	}
}
