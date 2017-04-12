package com.tle.core.migration;

import com.tle.core.migration.impl.MigrateTask;

public class MigrationResult
{
	private boolean canRetry;
	private String message;
	private MigrateTask task;
	private MigrationSubTaskStatus subtask;

	public MigrationResult(MigrateTask task)
	{
		this.task = task;
	}

	public boolean isCanRetry()
	{
		return canRetry;
	}

	public void setCanRetry(boolean canRetry)
	{
		this.canRetry = canRetry;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public void addLogEntry(MigrationStatusLog entry)
	{
		task.addLogEntry(entry);
	}

	public void setupSubSubTask(String statusKey, int size)
	{
		subtask.setCurrentMax(size);
		subtask.setCurrentDone(0);
		subtask.setStatusKey(statusKey);
		task.updateSubtask(subtask);
		task.publishStatus();
	}

	public void setupSubTaskStatus(String statusKey, int size)
	{
		subtask = new MigrationSubTaskStatus(statusKey, size);
		task.updateSubtask(subtask);
		task.publishStatus();
	}

	public void incrementStatus()
	{
		subtask.increment();
		task.updateSubtask(subtask);
	}
}
