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

package com.tle.core.migration.impl;

import java.io.Serializable;
import java.util.Collection;

import com.tle.core.migration.InstallSettings;
import com.tle.core.migration.MigrationStatus;

public class MigrationMessage implements Serializable
{
	enum Type
	{
		MIGRATE, REFRESH, SETONLINE, ADD, DELETE, INSTALLSETTINGS, ERRORREPORT, CHECKED
	}

	private static final long serialVersionUID = 1L;
	private final Type type;

	public MigrationMessage(Type type)
	{
		this.type = type;
	}

	public Type getType()
	{
		return type;
	}

	public static class MigrateMessage extends MigrationMessage
	{
		private static final long serialVersionUID = 1L;

		private final Collection<Long> migrateSchemaIds;

		public MigrateMessage(Collection<Long> migrationSchemaIds)
		{
			super(Type.MIGRATE);
			this.migrateSchemaIds = migrationSchemaIds;
		}

		public Collection<Long> getMigrateSchemaIds()
		{
			return migrateSchemaIds;
		}
	}

	public static class SchemaMessage extends MigrationMessage
	{
		private static final long serialVersionUID = 1L;
		private long schemaId;

		public SchemaMessage(Type type, long schemaId)
		{
			super(type);
			this.schemaId = schemaId;
		}

		public long getSchemaId()
		{
			return schemaId;
		}
	}

	public static class AddMessage extends SchemaMessage
	{
		private static final long serialVersionUID = 1L;
		private final boolean initialise;

		public AddMessage(long schemaId, boolean initialise)
		{
			super(Type.ADD, schemaId);
			this.initialise = initialise;
		}

		public boolean isInitialise()
		{
			return initialise;
		}
	}

	public static class SetOnlineMessage extends MigrationMessage
	{
		private static final long serialVersionUID = 1L;
		private final Collection<Long> schemaIds;
		private final boolean online;

		public SetOnlineMessage(Collection<Long> schemaIds, boolean online)
		{
			super(Type.SETONLINE);
			this.schemaIds = schemaIds;
			this.online = online;
		}

		public Collection<Long> getSchemaIds()
		{
			return schemaIds;
		}

		public boolean isOnline()
		{
			return online;
		}
	}

	public static class InstallMessage extends MigrationMessage
	{
		private static final long serialVersionUID = 1L;
		private final InstallSettings installSettings;

		public InstallMessage(InstallSettings settings)
		{
			super(Type.INSTALLSETTINGS);
			this.installSettings = settings;
		}

		public InstallMessage()
		{
			this(null);
		}

		public InstallSettings getInstallSettings()
		{
			return installSettings;
		}
	}

	public static class MigrationResponse implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private MigrationStatus status;
		private Throwable error;
		private Object contents;

		public MigrationStatus getStatus()
		{
			return status;
		}

		public void setStatus(MigrationStatus status)
		{
			this.status = status;
		}

		public Throwable getError()
		{
			return error;
		}

		public void setError(Throwable error)
		{
			this.error = error;
		}

		@SuppressWarnings("unchecked")
		public <T> T getContents()
		{
			return (T) contents;
		}

		public void setContents(Object contents)
		{
			this.contents = contents;
		}
	}
}
