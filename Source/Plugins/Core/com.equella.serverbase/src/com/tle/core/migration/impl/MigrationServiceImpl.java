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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;
import org.springframework.transaction.annotation.Transactional;

import com.tle.common.beans.exception.ValidationError;
import com.tle.common.beans.exception.InvalidDataException;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Provider;
import com.tle.beans.DatabaseSchema;
import com.tle.common.Check;
import com.tle.core.application.StartupBean;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.DataSourceService;
import com.tle.core.hibernate.SystemDatabase;
import com.tle.core.hibernate.event.SchemaEvent;
import com.tle.core.migration.InstallSettings;
import com.tle.core.migration.Migration;
import com.tle.core.migration.MigrationErrorReport;
import com.tle.core.migration.MigrationService;
import com.tle.core.migration.MigrationStatus;
import com.tle.core.migration.MigrationStatusLog;
import com.tle.core.migration.MigrationStatusLog.LogType;
import com.tle.core.migration.SchemaInfo;
import com.tle.core.migration.SchemaInfoImpl;
import com.tle.core.migration.impl.MigrationMessage.AddMessage;
import com.tle.core.migration.impl.MigrationMessage.InstallMessage;
import com.tle.core.migration.impl.MigrationMessage.MigrateMessage;
import com.tle.core.migration.impl.MigrationMessage.MigrationResponse;
import com.tle.core.migration.impl.MigrationMessage.SchemaMessage;
import com.tle.core.migration.impl.MigrationMessage.SetOnlineMessage;
import com.tle.core.migration.impl.MigrationMessage.Type;
import com.tle.core.migration.log.MigrationLog;
import com.tle.core.migration.log.MigrationLog.LogStatus;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.security.impl.SecureOnCallSystem;
import com.tle.core.events.services.EventService;
import com.tle.core.services.TaskService;
import com.tle.core.services.TaskStatus;
import com.tle.core.services.TaskStatusListener;
import com.tle.core.services.impl.BeanClusteredTask;
import com.tle.core.services.impl.Task;
import com.tle.core.system.dao.DatabaseSchemaDao;

@Bind(MigrationService.class)
@Singleton
@SuppressWarnings("nls")
@SystemDatabase
public class MigrationServiceImpl implements MigrationService, StartupBean, TaskStatusListener
{
	private static final long TIMEOUT = TimeUnit.MINUTES.toMillis(1);

	private static final BeanClusteredTask CHECK_TASK = new BeanClusteredTask("CheckSystemSchema", true,
		MigrationService.class, "checkSystemSchema");

	private static Log LOGGER = LogFactory.getLog(MigrationServiceImpl.class);

	@Inject
	private TaskService taskService;
	@Inject
	private EventService eventService;
	@Inject
	private Provider<MigrationGlobalTask> globalProvider;
	@Inject
	private DatabaseSchemaDao databaseSchemaDao;
	@Inject
	private DataSourceService dataSourceService;

	private PluginTracker<Migration> migrateTracker;
	private Set<MigrationExt> orderedMigrations;

	private String checkSystemTask;
	private boolean systemUp;
	private final Set<Long> upSchemas = Sets.newHashSet();
	private MigrationStatus migrationStatus;

	@Override
	public void startup()
	{
		checkSystemTask = taskService.getGlobalTask(CHECK_TASK, TIMEOUT).getTaskId();
		taskService.addTaskStatusListener(checkSystemTask, this);
		TaskStatus status = taskService.getTaskStatus(checkSystemTask);
		if( status != null )
		{
			taskStatusChanged(null, status);
		}
	}

	@Override
	public Collection<Long> getAvailableSchemaIds()
	{
		synchronized( upSchemas )
		{
			return ImmutableSet.copyOf(upSchemas);
		}
	}

	@Override
	public void taskStatusChanged(String taskId, TaskStatus taskStatus)
	{
		migrationStatus = taskStatus.getTaskSubStatus(MigrationStatus.KEY_STATUS);
		if( migrationStatus == null )
		{
			return;
		}
		Set<Long> newUp;
		Set<Long> newDown;
		synchronized( upSchemas )
		{
			newUp = Sets.newHashSet();
			newDown = Sets.newHashSet(upSchemas);
			for( SchemaInfo schemaInfo : migrationStatus.getSchemas().values() )
			{
				long schemaId = schemaInfo.getDatabaseSchema().getId();
				if( schemaInfo.isSystem() )
				{
					if( !systemUp && schemaInfo.isUp() )
					{
						systemUp = true;
						eventService.publishApplicationEvent(new SchemaEvent(true));
					}
				}
				else if( !schemaInfo.isChecking() )
				{
					if( schemaInfo.isUp() )
					{
						if( !newDown.remove(schemaId) )
						{
							newUp.add(schemaId);
						}
					}
				}
				else
				{
					newDown.remove(schemaId);
				}
			}
			upSchemas.removeAll(newDown);
			upSchemas.addAll(newUp);
		}
		if( !newUp.isEmpty() || !newDown.isEmpty() )
		{
			SchemaEvent event = new SchemaEvent(newUp, newDown);
			eventService.publishApplicationEvent(event);
		}
	}

	public Task checkSystemSchema()
	{
		return globalProvider.get();
	}

	public static Log getLogger()
	{
		return LOGGER;
	}

	@Override
	public synchronized Set<MigrationExt> getOrderedMigrations()
	{
		if( orderedMigrations == null || migrateTracker.needsUpdate() )
		{
			List<MigrationExt> migrationList = new ArrayList<MigrationServiceImpl.MigrationExt>();
			for( Extension extension : migrateTracker.getExtensions() )
			{
				String id = extension.getParameter("id").valueAsString();
				Parameter initialParam = extension.getParameter("initial");
				boolean initial = initialParam != null && initialParam.valueAsBoolean();
				Parameter systemParam = extension.getParameter("system");
				boolean system = systemParam != null && systemParam.valueAsBoolean();
				Date date = new Date(0L);
				Parameter dateParam = extension.getParameter("date");
				if( dateParam != null )
				{
					date = dateParam.valueAsDate();
				}
				MigrationExt ext = new MigrationExt(id, extension, date, initial, system);
				ext.setDepends(idSet(extension, "depends"));
				ext.setFixes(idSet(extension, "fixes"));
				ext.setObsoletedBy(idSet(extension, "obsoletedby"));
				ext.setIfSkipped(idSet(extension, "ifskipped"));
				migrationList.add(ext);
			}
			Collections.sort(migrationList, new Comparator<MigrationExt>()
			{
				@Override
				public int compare(MigrationExt o1, MigrationExt o2)
				{
					int comparison = o1.getDate().compareTo(o2.getDate());
					if( comparison == 0 )
					{
						return o1.getId().compareTo(o2.getId());
					}
					return comparison;
				}
			});
			Map<String, MigrationExt> mappedMigrations = new HashMap<String, MigrationServiceImpl.MigrationExt>();
			for( MigrationExt mext : migrationList )
			{
				mappedMigrations.put(mext.getId(), mext);
			}
			Set<MigrationExt> processing = new HashSet<MigrationExt>();
			orderedMigrations = new LinkedHashSet<MigrationServiceImpl.MigrationExt>();
			for( MigrationExt mig : migrationList )
			{
				processDependencies(mig, processing, orderedMigrations, mappedMigrations);
			}
		}
		return orderedMigrations;
	}

	private void processDependencies(MigrationExt mig, Set<MigrationExt> processing, Set<MigrationExt> outSet,
		Map<String, MigrationExt> orderedMigrations)
	{
		if( outSet.contains(mig) )
		{
			return;
		}
		String migId = mig.getId();
		if( processing.contains(mig) )
		{
			throw new RuntimeException("Cyclic dependency found:" + migId);
		}
		processing.add(mig);
		processDependSet(migId, mig.getDepends(), processing, outSet, orderedMigrations);
		processDependSet(migId, mig.getFixes(), processing, outSet, orderedMigrations);
		processDependSet(migId, mig.getObsoletedBy(), processing, outSet, orderedMigrations);
		processDependSet(migId, mig.getIfSkipped(), processing, outSet, orderedMigrations);
		processing.remove(mig);
		outSet.add(mig);
	}

	private void processDependSet(String sourceId, Set<String> depends, Set<MigrationExt> processing,
		Set<MigrationExt> outSet, Map<String, MigrationExt> orderedMigrations)
	{
		for( String depend : depends )
		{
			MigrationExt mig = orderedMigrations.get(depend);
			if( mig == null )
			{
				throw new RuntimeException("Missing dependency '" + depend + "' for '" + sourceId + "'");
			}
			processDependencies(mig, processing, outSet, orderedMigrations);
		}
	}

	private Set<String> idSet(Extension extension, String param)
	{
		Set<String> idSet = new HashSet<String>();
		Collection<Parameter> ids = extension.getParameters(param);
		for( Parameter idParam : ids )
		{
			idSet.add(idParam.valueAsString());
		}
		return idSet;
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		migrateTracker = new PluginTracker<Migration>(pluginService, "com.tle.core.migration", "migration", "id").setBeanKey("bean");
	}

	@Override
	public MigrationStatus getMigrationsStatus()
	{
		return migrationStatus;
	}

	@Override
	public List<MigrationStatusLog> getWarnings(String taskId)
	{
		TaskStatus taskStatus = taskService.getTaskStatus(taskId);
		List<MigrationStatusLog> warnings = new ArrayList<MigrationStatusLog>();
		List<MigrationStatusLog> taskLog = taskStatus.getTaskLog();
		for( MigrationStatusLog logEntry : taskLog )
		{
			if( logEntry.getType() == LogType.WARNING )
			{
				warnings.add(logEntry);
			}
		}
		return warnings;
	}

	@Override
	public Migration getMigration(MigrationState ext)
	{
		return migrateTracker.getBeanByExtension(ext.getExtension());
	}

	public static class MigrationExt
	{
		private final String id;
		private final Extension extension;
		private final boolean initial;
		private final boolean system;
		private final Date date;
		private Set<String> depends;
		private Set<String> obsoletedBy;
		private Set<String> fixes;
		private Set<String> ifSkipped;

		public MigrationExt(String id, Extension ext, Date date, boolean initial, boolean system)
		{
			this.id = id;
			this.extension = ext;
			this.initial = initial;
			this.date = date;
			this.system = system;
		}

		public boolean isSystem()
		{
			return system;
		}

		public String getId()
		{
			return id;
		}

		public Extension getExtension()
		{
			return extension;
		}

		public boolean isInitial()
		{
			return initial;
		}

		public Set<String> getDepends()
		{
			return depends;
		}

		public void setDepends(Set<String> depends)
		{
			this.depends = depends;
		}

		public Set<String> getObsoletedBy()
		{
			return obsoletedBy;
		}

		public void setObsoletedBy(Set<String> obsoletedBy)
		{
			this.obsoletedBy = obsoletedBy;
		}

		public Set<String> getFixes()
		{
			return fixes;
		}

		public void setFixes(Set<String> fixes)
		{
			this.fixes = fixes;
		}

		public Set<String> getIfSkipped()
		{
			return ifSkipped;
		}

		public void setIfSkipped(Set<String> ifSkipped)
		{
			this.ifSkipped = ifSkipped;
		}

		public Date getDate()
		{
			return date;
		}

		@Override
		public String toString()
		{
			return id;
		}
	}

	public static class MigrationState
	{
		private final MigrationExt extension;
		private final MigrationLog logEntry;
		private boolean skip;
		private boolean execute;
		private boolean obsoleted;

		public MigrationState(MigrationExt extension, MigrationLog logEntry)
		{
			this.extension = extension;
			this.logEntry = logEntry;
		}

		public boolean isPlaceHolder()
		{
			return extension.getExtension().getParameter("bean") == null; //$NON-NLS-1$
		}

		public boolean needsProcessing()
		{
			return logEntry == null || logEntry.getStatus() == LogStatus.ERRORED;
		}

		public boolean wasSkippedAlready()
		{
			return logEntry != null && logEntry.getStatus() == LogStatus.SKIPPED;
		}

		public boolean wasExecutedAlready()
		{
			return logEntry != null && logEntry.getStatus() == LogStatus.EXECUTED;
		}

		public Extension getExtension()
		{
			return extension.getExtension();
		}

		public String getId()
		{
			return extension.getId();
		}

		public boolean isCanRetry()
		{
			return logEntry != null && logEntry.isCanRetry();
		}

		public LogStatus getStatus()
		{
			if( logEntry == null )
			{
				return null;
			}
			return logEntry.getStatus();
		}

		public MigrationLog getLogEntry()
		{
			return logEntry;
		}

		public boolean isSkip()
		{
			return skip;
		}

		public void setSkip(boolean skip)
		{
			this.skip = skip;
		}

		public boolean isObsoleted()
		{
			return obsoleted;
		}

		public void setObsoleted(boolean obsoleted)
		{
			this.obsoleted = obsoleted;
		}

		public boolean isExecute()
		{
			return execute;
		}

		public void setExecute(boolean execute)
		{
			this.execute = execute;
		}
	}

	@Override
	@SecureOnCallSystem
	public void executeMigrationsForSchemas(Collection<Long> schemaIds)
	{
		postMessage(new MigrateMessage(Lists.newArrayList(schemaIds)));
	}

	@Override
	@SecureOnCallSystem
	public void refreshSchema(long schemaId)
	{
		postMessage(new SchemaMessage(Type.REFRESH, schemaId));
	}

	@Override
	@SecureOnCallSystem
	public void migrateSystemSchema()
	{
		if( !systemUp )
		{
			postMessage(new MigrateMessage(Collections.singleton(DatabaseSchema.SYSTEM_SCHEMA.getId())));
		}
	}

	@Override
	public void refreshSystemSchema()
	{
		if( !systemUp )
		{
			postMessage(new SchemaMessage(Type.REFRESH, DatabaseSchema.SYSTEM_SCHEMA.getId()));
		}
	}

	@Override
	@SecureOnCallSystem
	public void setSchemasOnline(Collection<Long> schemaIds, boolean online)
	{
		postMessage(new SetOnlineMessage(schemaIds, online));
	}

	private <T> T postMessage(MigrationMessage msg)
	{
		MigrationResponse response = taskService.postSynchronousMessage(checkSystemTask, msg, TIMEOUT);
		if( response.getStatus() != null )
		{
			migrationStatus = response.getStatus();
		}
		if( response.getError() != null )
		{
			Throwables.propagate(response.getError());
		}
		return response.getContents();
	}

	@Override
	public boolean isSomeSchemasUp()
	{
		Collection<SchemaInfo> schemas = migrationStatus.getSchemas().values();
		for( SchemaInfo schemaInfo : schemas )
		{
			if( schemaInfo.isUp() && !schemaInfo.isSystem() )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public DatabaseSchema getSchema(long schemaId)
	{
		return databaseSchemaDao.get(schemaId);
	}

	@Override
	public SchemaInfo getSystemSchemaInfo()
	{
		return new SchemaInfoImpl(DatabaseSchema.SYSTEM_SCHEMA).immutable();
	}

	@Override
	@SecureOnCallSystem
	public long addSchema(DatabaseSchema ds, boolean initialise)
	{
		validateSchema(ds, true);
		long schemaId = databaseSchemaDao.add(ds);
		postMessage(new AddMessage(schemaId, initialise));
		return schemaId;
	}

	@Override
	@SecureOnCallSystem
	public void editSchema(DatabaseSchema ds)
	{
		editSchemaTransactional(ds);
		postMessage(new SchemaMessage(Type.REFRESH, ds.getId()));
	}

	@Transactional
	protected void editSchemaTransactional(DatabaseSchema ds)
	{
		DatabaseSchema existing = databaseSchemaDao.get(ds.getId());
		if( Check.isEmpty(ds.getPassword()) )
		{
			ds.setPassword(existing.getPassword());
		}
		existing.setOnline(existing.isOnline());
		validateSchema(ds, false);
		databaseSchemaDao.edit(ds);
	}

	@Transactional
	protected void validateSchema(DatabaseSchema ds, boolean create) throws InvalidDataException
	{
		List<ValidationError> errors = Lists.newArrayList();
		if( !ds.isUseSystem() )
		{
			addIfEmpty(errors, ds.getUrl(), "url");
			addIfEmpty(errors, ds.getUsername(), "username");
			addIfEmpty(errors, ds.getPassword(), "password");
		}

		// Compare to existing schemas
		Collection<DatabaseSchema> others = databaseSchemaDao.enumerate();
		for( DatabaseSchema otherSchema : others )
		{
			otherSchema = otherSchema.isUseSystem() ? getSystemDatabaseSchema(otherSchema.getId()) : otherSchema;

			if( !create && otherSchema.getId() == ds.getId() )
			{
				// Editing the current system schema
				continue;
			}

			if( ds.isUseSystem() )
			{
				ds = getSystemDatabaseSchema(0);
				if( otherSchema.isUseSystem() || isDuplicate(otherSchema, ds) )
				{
					errors.add(new ValidationError("usesystem", "duplicate"));
				}
			}
			else
			{
				if( isDuplicate(otherSchema, ds) )
				{
					errors.add(new ValidationError("url", "duplicate"));
				}
			}

		}
		if( !errors.isEmpty() )
		{
			throw new InvalidDataException(errors);
		}
	}

	private DatabaseSchema getSystemDatabaseSchema(long id)
	{
		DatabaseSchema dbs = new DatabaseSchema();
		dbs.setId(id);
		dbs.setUrl(dataSourceService.getSystemUrl());
		dbs.setUsername(dataSourceService.getSystemUsername());
		dbs.setPassword(dataSourceService.getSystemPassword());
		dbs.setUseSystem(true);
		return dbs;
	}

	private boolean isDuplicate(DatabaseSchema ds1, DatabaseSchema ds2)
	{
		return Objects.equals(ds1.getUrl(), ds2.getUrl()) && Objects.equals(ds1.getUsername(), ds2.getUsername());
	}

	private void addIfEmpty(List<ValidationError> errors, String value, String field)
	{
		if( Check.isEmpty(value) )
		{
			errors.add(new ValidationError(field, "mandatory"));
		}
	}

	@Override
	@SecureOnCallSystem
	public void deleteSchema(long schemaId)
	{
		postMessage(new SchemaMessage(Type.DELETE, schemaId));
	}

	@Override
	public boolean isSystemSchemaUp()
	{
		return systemUp;
	}

	@Override
	public InstallSettings getInstallSettings()
	{
		return postMessage(new InstallMessage());
	}

	@Override
	public void setInstallSettings(InstallSettings installSettings)
	{
		postMessage(new InstallMessage(installSettings));
	}

	@Override
	public MigrationErrorReport getErrorReport(long schemaId)
	{
		return postMessage(new SchemaMessage(Type.ERRORREPORT, schemaId));
	}
}
