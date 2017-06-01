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

import java.io.Serializable;
import java.util.List;

import com.tle.beans.DatabaseSchema;
import com.tle.common.Check;

public class SchemaInfoImpl implements Serializable
{
	private static final long serialVersionUID = 1L;

	private DatabaseSchema databaseSchema;
	private long updateTime;
	private String uniqueId;
	private boolean autoMigrate;
	private boolean checking;
	private boolean up;
	private List<MigrationInfo> migrations;
	private String taskId;
	private String finishedTaskId;
	private String errorMessage;
	private boolean hasErrors;
	private boolean canRetry;
	private boolean initial;
	private boolean triedUpdatingId;

	private DatabaseSchema duplicateWith;

	public SchemaInfoImpl(DatabaseSchema databaseSchema)
	{
		this.databaseSchema = databaseSchema;
		this.checking = true;
	}

	public DatabaseSchema getDatabaseSchema()
	{
		return databaseSchema;
	}

	public List<MigrationInfo> getMigrations()
	{
		return migrations;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public String getTaskId()
	{
		return taskId;
	}

	public void setTaskId(String taskId)
	{
		this.taskId = taskId;
	}

	public boolean isSystem()
	{
		return databaseSchema.isSystem();
	}

	public boolean isUp()
	{
		return up;
	}

	public boolean isMigrationRequired()
	{
		return !Check.isEmpty(migrations);
	}

	public boolean isInitial()
	{
		return initial;
	}

	public void setInitial(boolean initial)
	{
		this.initial = initial;
	}

	public boolean isHasErrors()
	{
		return hasErrors;
	}

	public boolean isCanRetry()
	{
		return canRetry;
	}

	public String getFinishedTaskId()
	{
		return finishedTaskId;
	}

	public void setFinishedTaskId(String finishedTaskId)
	{
		this.finishedTaskId = finishedTaskId;
	}

	public void setAvailable(boolean up)
	{
		this.up = up;
		duplicateWith = null;
		errorMessage = null;
		hasErrors = false;
		migrations = null;
	}

	public void setDatabaseSchema(DatabaseSchema databaseSchema)
	{
		this.databaseSchema = databaseSchema;
	}

	public void setMigrations(List<MigrationInfo> migrations)
	{
		this.migrations = migrations;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	public void setHasErrors(boolean hasErrors)
	{
		this.hasErrors = hasErrors;
	}

	public void setCanRetry(boolean canRetry)
	{
		this.canRetry = canRetry;
	}

	public void setDuplicateWith(DatabaseSchema duplicateWith)
	{
		this.duplicateWith = duplicateWith;
	}

	public DatabaseSchema getDuplicateWith()
	{
		return duplicateWith;
	}

	public boolean isChecking()
	{
		return checking;
	}

	public void setChecking(boolean checking)
	{
		this.checking = checking;
	}

	public String getUniqueId()
	{
		return uniqueId;
	}

	public void setUniqueId(String uniqueId)
	{
		this.uniqueId = uniqueId;
	}

	public boolean isTriedUpdatingId()
	{
		return triedUpdatingId;
	}

	public void setTriedUpdatingId(boolean triedUpdatingId)
	{
		this.triedUpdatingId = triedUpdatingId;
	}

	public long getUpdateTime()
	{
		return updateTime;
	}

	public void setUpdateTime(long updateTime)
	{
		this.updateTime = updateTime;
	}

	public void updateTime()
	{
		setUpdateTime(System.currentTimeMillis());
	}

	public boolean isAutoMigrate()
	{
		return autoMigrate;
	}

	public void setAutoMigrate(boolean autoMigrate)
	{
		this.autoMigrate = autoMigrate;
	}

	public SchemaInfo immutable()
	{
		return new SchemaInfoImmutable(databaseSchema, taskId, duplicateWith, errorMessage, finishedTaskId, checking,
			initial, updateTime, canRetry, hasErrors, isMigrationRequired(), isUp());
	}

	public static final class SchemaInfoImmutable implements SchemaInfo, Serializable
	{
		private final DatabaseSchema databaseSchema;
		private final String taskId;
		private final DatabaseSchema duplicateWith;
		private final String errorMessage;
		private final String finishedTaskId;
		private final boolean checking;
		private final boolean initial;
		private final long updateTime;
		private final boolean canRetry;
		private final boolean hasErrors;
		private final boolean up;
		private final boolean migrationRequired;

		public SchemaInfoImmutable(DatabaseSchema db, String taskId, DatabaseSchema duplicateWith, String errorMessage,
			String finishedTaskId, boolean checking, boolean initial, long updateTime, boolean canRetry,
			boolean hasErrors, boolean migrationRequired, boolean up)
		{
			this.databaseSchema = db;
			this.taskId = taskId;
			this.duplicateWith = duplicateWith;
			this.errorMessage = errorMessage;
			this.finishedTaskId = finishedTaskId;
			this.checking = checking;
			this.initial = initial;
			this.updateTime = updateTime;
			this.canRetry = canRetry;
			this.hasErrors = hasErrors;
			this.migrationRequired = migrationRequired;
			this.up = up;
		}

		@Override
		public DatabaseSchema getDatabaseSchema()
		{
			return databaseSchema;
		}

		@Override
		public String getTaskId()
		{
			return taskId;
		}

		@Override
		public DatabaseSchema getDuplicateWith()
		{
			return duplicateWith;
		}

		@Override
		public String getErrorMessage()
		{
			return errorMessage;
		}

		@Override
		public String getFinishedTaskId()
		{
			return finishedTaskId;
		}

		@Override
		public boolean isChecking()
		{
			return checking;
		}

		@Override
		public boolean isInitial()
		{
			return initial;
		}

		@Override
		public long getUpdateTime()
		{
			return updateTime;
		}

		@Override
		public boolean isCanRetry()
		{
			return canRetry;
		}

		@Override
		public boolean isHasErrors()
		{
			return hasErrors;
		}

		@Override
		public boolean isMigrationRequired()
		{
			return migrationRequired;
		}

		@Override
		public boolean isSystem()
		{
			return databaseSchema.isSystem();
		}

		@Override
		public boolean isUp()
		{
			return up;
		}

	}
}
