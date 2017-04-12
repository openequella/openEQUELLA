package com.tle.core.services;

public class GlobalTaskStartInfo
{
	private final String taskId;
	private final boolean alreadyRunning;

	public GlobalTaskStartInfo(String taskId, boolean alreadyRunning)
	{
		this.taskId = taskId;
		this.alreadyRunning = alreadyRunning;
	}

	public String getTaskId()
	{
		return taskId;
	}

	public boolean isAlreadyRunning()
	{
		return alreadyRunning;
	}
}