/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
