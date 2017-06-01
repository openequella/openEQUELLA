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

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.beans.DatabaseSchema;
import com.tle.common.i18n.KeyString;
import com.tle.core.guice.Bind;
import com.tle.core.guice.BindFactory;
import com.tle.core.hibernate.DataSourceHolder;
import com.tle.core.hibernate.DataSourceService;
import com.tle.core.hibernate.ExtendedDialect;
import com.tle.core.migration.InstallSettings;
import com.tle.core.migration.Migration;
import com.tle.core.migration.MigrationErrorReport;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationService;
import com.tle.core.migration.MigrationStatus;
import com.tle.core.migration.MigrationStatusLog;
import com.tle.core.migration.SchemaInfo;
import com.tle.core.migration.SchemaInfoImpl;
import com.tle.core.migration.impl.MigrationMessage.AddMessage;
import com.tle.core.migration.impl.MigrationMessage.InstallMessage;
import com.tle.core.migration.impl.MigrationMessage.MigrateMessage;
import com.tle.core.migration.impl.MigrationMessage.MigrationResponse;
import com.tle.core.migration.impl.MigrationMessage.SchemaMessage;
import com.tle.core.migration.impl.MigrationMessage.SetOnlineMessage;
import com.tle.core.migration.impl.MigrationMessage.Type;
import com.tle.core.migration.impl.MigrationServiceImpl.MigrationState;
import com.tle.core.migration.log.MigrationLog;
import com.tle.core.migration.log.MigrationLog.LogStatus;
import com.tle.core.plugins.impl.PluginServiceImpl;
import com.tle.core.services.TaskStatus;
import com.tle.core.services.TaskStatusListener;
import com.tle.core.services.impl.AlwaysRunningTask;
import com.tle.core.services.impl.BeanClusteredTask;
import com.tle.core.services.impl.SimpleMessage;
import com.tle.core.system.dao.DatabaseSchemaDao;
import com.tle.core.system.service.SchemaDataSourceService;

@Bind
@SuppressWarnings("nls")
public class MigrationGlobalTask extends AlwaysRunningTask<SimpleMessage>
{
	private static final String KEY_INSTALL_SETTINGS = "InstallSettings";
	private static final String TASK_ID = "MigrationTask";
	private static final Log LOGGER = LogFactory.getLog(MigrationGlobalTask.class);
	private static final String KEY_PFX = PluginServiceImpl.getMyPluginId(MigrationGlobalTask.class) + '.';

	@Inject
	private DataSourceService dataSourceService;
	@Inject
	private DatabaseSchemaDao dao;
	@Inject
	private SchemaDataSourceService schemaDataSourceService;
	@Inject
	private MigrationService migrationService;
	@Inject
	private HibernateMigrationService hibernateMigrationService;

	private final Map<Long, CheckCallable> checkingSchemas = Maps.newHashMap();
	private final MigrationStatus migStatus = new MigrationStatus();
	private final Map<Long, SchemaInfoImpl> schemaInfos = Maps.newHashMap();
	private final ExecutorService checkExecutor = Executors.newCachedThreadPool();

	private InstallSettings installSettings;
	private ExtendedDialect dialect;

	@Override
	protected String getTitleKey()
	{
		return "com.tle.core.migration.migrationglobaltask.title";
	}

	private String getStackTrace(Throwable t)
	{
		StringWriter swriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(swriter);
		t.printStackTrace(printWriter);
		return swriter.toString();
	}

	@Override
	public void init()
	{
		dialect = dataSourceService.getDialect();
		checkSystemSchema();
		publishMigrationStatus(migStatus);
	}

	@Override
	protected SimpleMessage waitFor() throws Exception
	{
		return waitForMessage();
	}

	@Override
	public void runTask(SimpleMessage msg) throws Exception
	{
		MigrationResponse response = new MigrationResponse();
		boolean publishStatus = true;
		try
		{
			MigrationMessage migMessage = msg.getContents();
			switch( migMessage.getType() )
			{
				case CHECKED:
					processChecked((SchemaMessage) migMessage);
					break;
				case ERRORREPORT:
					response.setContents(processErrorReport((SchemaMessage) migMessage));
					publishStatus = false;
					break;
				case INSTALLSETTINGS:
					response.setContents(processInstallSettings((InstallMessage) migMessage));
					publishStatus = false;
					break;
				case MIGRATE:
					processMigrations((MigrateMessage) migMessage);
					break;
				case REFRESH:
					processRefresh((SchemaMessage) migMessage);
					break;
				case SETONLINE:
					processSetOnline((SetOnlineMessage) migMessage);
					break;
				case ADD:
					processAdd((AddMessage) migMessage);
					break;
				case DELETE:
					processDelete((SchemaMessage) migMessage);
					break;
			}
		}
		catch( Exception t )
		{
			response.setError(t);
		}

		if( publishStatus )
		{
			response.setStatus(migStatus);
			publishMigrationStatus(migStatus);
		}

		if( msg.getMessageId() != null )
		{
			sendResponse(msg.getMessageId(), response);
		}
	}

	private void processChecked(SchemaMessage migMessage)
	{
		long schemaId = migMessage.getSchemaId();
		SchemaInfoImpl schemaInfo = schemaInfos.get(schemaId);
		schemaInfo.updateTime();
		CheckCallable callable = checkingSchemas.get(schemaId);
		if( callable != null )
		{
			Future<CheckResult> future = callable.getFuture();
			if( future == null )
			{
				callable.submit(checkExecutor);
				return;
			}
			schemaInfo.setChecking(false);
			checkingSchemas.remove(schemaId);
			DatabaseSchema dbSchema = schemaInfo.getDatabaseSchema();
			try
			{
				CheckResult checkResult = future.get();
				MigrationsToRun migrationsToRun = checkResult.getMigrationsToRun();
				schemaInfo.setUniqueId(checkResult.getUniqueId());
				Collection<SchemaInfoImpl> duplicates = getDuplicates(schemaInfo, checkResult.getUniqueId());
				if( duplicates.isEmpty() )
				{
					boolean autoMigrate = schemaInfo.isAutoMigrate();
					schemaInfo.setAutoMigrate(false);
					if( !migrationsToRun.isExecutions() )
					{
						schemaInfo.setAvailable(dbSchema.isOnline());
					}
					else
					{
						setupMigrationInfo(schemaInfo, migrationsToRun);
						if( autoMigrate )
						{
							migrateSchema(schemaId, schemaInfo);
						}
					}
				}
				else
				{
					schemaInfo.setAvailable(false);
					if( !schemaInfo.isTriedUpdatingId() )
					{
						schemaInfo.setTriedUpdatingId(true);
						LOGGER.warn("Found duplicate schema id, attempting to update on: " + dbSchema);
						schemaInfo.setUniqueId(hibernateMigrationService.updateUniqueId(schemaDataSourceService
							.getDataSourceForId(dbSchema.getId())));
						for( SchemaInfoImpl dupe : duplicates )
						{
							dupe.setTriedUpdatingId(true);
							checkSchema(dupe);
						}
					}
					else
					{
						schemaInfo.setDuplicateWith(duplicates.iterator().next().getDatabaseSchema());
					}
				}
			}
			catch( InterruptedException e )
			{
				throw new RuntimeException(e);
			}
			catch( ExecutionException e )
			{
				schemaInfo.setAvailable(false);
				schemaInfo.setUniqueId(null);
				Throwable cause = e.getCause();
				schemaInfo.setErrorMessage(getStackTrace(cause));
				LOGGER.error("Error checking for migrations on schema " + dbSchema, cause);
			}
		}
	}

	private MigrationErrorReport processErrorReport(SchemaMessage msg)
	{
		long schemaId = msg.getSchemaId();
		SchemaInfoImpl schemaInfo = schemaInfos.get(schemaId);
		if( schemaInfo.getErrorMessage() != null )
		{
			MigrationErrorReport errorReport = new MigrationErrorReport();
			errorReport.setError(schemaInfo.getErrorMessage());
			errorReport.setName(new KeyString(KEY_PFX + "checktask"));
			errorReport.setCanRetry(true);
			return errorReport;
		}
		List<MigrationInfo> migrations = schemaInfo.getMigrations();
		for( MigrationInfo minfo : migrations )
		{
			if( minfo.isFailed() )
			{
				DataSourceHolder dataSource = schemaId == DatabaseSchema.SYSTEM_SCHEMA.getId() ? dataSourceService
					.getSystemDataSource() : schemaDataSourceService.getDataSourceForId(schemaId);
				MigrationLog log = hibernateMigrationService.getLogEntry(dataSource, minfo.getMigrateId());
				if( log == null )
				{
					return null;
				}
				MigrationErrorReport report = new MigrationErrorReport();
				report.setError(log.getErrorMessage());
				report.setMessage(log.getMessage());
				report.setName(minfo.getName());
				report.setCanRetry(log.isCanRetry());
				String finishedTaskId = schemaInfo.getFinishedTaskId();
				if( finishedTaskId != null )
				{
					TaskStatus taskStatus = taskService.getTaskStatus(finishedTaskId);
					if( taskStatus != null )
					{
						List<MigrationStatusLog> taskLog = taskStatus.getTaskLog();
						report.setLog(taskLog);
					}
				}
				return report;
			}
		}
		return null;
	}

	private Serializable processInstallSettings(InstallMessage msg)
	{
		InstallSettings settings = msg.getInstallSettings();
		if( settings != null )
		{
			this.installSettings = settings;
			setSubTaskStatus(KEY_INSTALL_SETTINGS, settings);
			publishStatus();
			return true;
		}
		return this.installSettings;
	}

	private void processDelete(SchemaMessage migMessage)
	{
		long schemaId = migMessage.getSchemaId();
		schemaDataSourceService.removeSchemaDataSource(schemaId);
		dao.deleteSchema(schemaId);
		Map<Long, SchemaInfoImpl> allSchemas = schemaInfos;
		SchemaInfoImpl removed = allSchemas.remove(schemaId);
		DatabaseSchema removedSchema = removed.getDatabaseSchema();
		for( SchemaInfoImpl schemaInfo : allSchemas.values() )
		{
			if( removedSchema.equals(schemaInfo.getDuplicateWith()) )
			{
				schemaInfo.setDuplicateWith(null);
				checkSchema(schemaInfo);
			}
		}
	}

	private void processAdd(AddMessage migMessage)
	{
		long schemaId = migMessage.getSchemaId();
		SchemaInfoImpl schemaInfo = ensureSchemaInfoImpl(dao.get(schemaId));
		schemaInfo.setAutoMigrate(migMessage.isInitialise());
		checkSchema(schemaInfo);
	}

	private void processSetOnline(SetOnlineMessage migMessage)
	{
		Collection<Long> schemaIds = migMessage.getSchemaIds();
		boolean online = migMessage.isOnline();
		for( Long schemaId : schemaIds )
		{
			SchemaInfoImpl schemaInfo = schemaInfos.get(schemaId);
			if( schemaInfo != null )
			{
				DatabaseSchema ds = schemaInfo.getDatabaseSchema();
				if( ds.isOnline() != online && schemaInfo.getTaskId() == null )
				{
					checkSchema(ensureSchemaInfoImpl(dao.setOnline(schemaId, online)));
				}
			}
		}
	}

	private void checkSystemSchema()
	{
		SchemaInfoImpl schemaInfo = ensureSchemaInfoImpl(DatabaseSchema.SYSTEM_SCHEMA);
		if( !schemaInfo.isUp() )
		{
			try
			{
				migStatus.setException(null);
				DataSourceHolder systemDataSource = dataSourceService.getSystemDataSource();
				MigrationsToRun systemMigrations = hibernateMigrationService.checkSchemaForMigrations(systemDataSource,
					true);
				boolean needsInstallation = systemMigrations.isFirstTime();
				if( !needsInstallation || systemMigrations.isExecutions() )
				{
					needsInstallation = hibernateMigrationService.getSystemPassword() == null;
				}
				migStatus.setNeedsInstallation(needsInstallation);
				if( !systemMigrations.isExecutions() )
				{
					Collection<DatabaseSchema> schemas = dao.enumerate();
					for( DatabaseSchema databaseSchema : schemas )
					{
						checkSchema(ensureSchemaInfoImpl(databaseSchema));
					}
					schemaInfo.setAvailable(true);
				}
				else
				{
					setupMigrationInfo(schemaInfo, systemMigrations);
				}
			}
			catch( Exception t )
			{
				migStatus.setException(getStackTrace(t));
				schemaInfo.setAvailable(false);
				schemaInfo.setErrorMessage(getStackTrace(t));
				LOGGER.error("Error checking for migrations on system schema", t);
			}
		}
	}

	private SchemaInfoImpl ensureSchemaInfoImpl(DatabaseSchema databaseSchema)
	{
		SchemaInfoImpl schemaInfo = schemaInfos.get(databaseSchema.getId());
		if( schemaInfo != null )
		{
			schemaInfo.setDatabaseSchema(databaseSchema);
		}
		else
		{
			schemaInfo = new SchemaInfoImpl(databaseSchema);
			schemaInfo.setChecking(true);
			schemaInfos.put(databaseSchema.getId(), schemaInfo);
		}
		return schemaInfo;
	}

	private void processRefresh(SchemaMessage migMessage)
	{
		long schemaId = migMessage.getSchemaId();
		SchemaInfoImpl oldSchema = schemaInfos.get(schemaId);
		DatabaseSchema dupeWith = null;

		// We assume oldSchema is never null here: if it is, let the stack trace
		// do the talking ...
		if( oldSchema.isSystem() )
		{
			checkSystemSchema();
		}
		else
		{
			dupeWith = oldSchema.getDuplicateWith();
			SchemaInfoImpl schemaInfo = ensureSchemaInfoImpl(dao.get(schemaId));
			schemaInfo.setTriedUpdatingId(false);
			checkSchema(schemaInfo);
			if( dupeWith != null )
			{
				checkSchema(ensureSchemaInfoImpl(dupeWith));
			}
		}
	}

	private void processMigrations(MigrateMessage migMessage)
	{
		Collection<Long> schemaIds = migMessage.getMigrateSchemaIds();
		for( final Long schemaId : schemaIds )
		{
			final SchemaInfoImpl schemaInfo = schemaInfos.get(schemaId);
			if( schemaInfo != null )
			{
				migrateSchema(schemaId, schemaInfo);
			}
		}
	}

	private void migrateSchema(final long schemaId, final SchemaInfoImpl schemaInfo)
	{
		boolean system = schemaInfo.isSystem();
		BeanClusteredTask task = new BeanClusteredTask(TASK_ID + "-" + schemaId, true, MigrateTaskFactory.class,
			"create", schemaId, system);
		String taskId = taskService.getGlobalTask(task, TimeUnit.MINUTES.toMillis(1)).getTaskId();
		taskService.addTaskStatusListener(taskId, new TaskStatusListener()
		{
			@Override
			public void taskStatusChanged(String taskId, TaskStatus taskStatus)
			{
				if( taskStatus.isFinished() )
				{
					schemaInfo.setFinishedTaskId(taskId);
					schemaInfo.setTaskId(null);
					postMessage(new SimpleMessage(null, new SchemaMessage(Type.REFRESH, schemaId)));
				}
			}
		});
		if( system )
		{
			migStatus.setNeedsInstallation(false);
		}
		schemaInfo.setTaskId(taskId);
	}

	private void checkSchema(SchemaInfoImpl schemaInfo)
	{
		schemaInfo.updateTime();
		long schemaId = schemaInfo.getDatabaseSchema().getId();
		boolean submitNow = true;
		CheckCallable callable = checkingSchemas.get(schemaId);
		if( callable != null )
		{
			submitNow = false;
			Future<CheckResult> future = callable.getFuture();
			if( future != null )
			{
				future.cancel(true);
			}
		}
		schemaInfo.setChecking(true);
		callable = new CheckCallable(schemaInfo.getDatabaseSchema());
		checkingSchemas.put(schemaId, callable);
		if( submitNow )
		{
			callable.submit(checkExecutor);
		}
	}

	public class CheckCallable implements Callable<CheckResult>
	{
		private final DatabaseSchema databaseSchema;
		private Future<CheckResult> future;

		public CheckCallable(DatabaseSchema databaseSchema)
		{
			this.databaseSchema = databaseSchema;
		}

		public void submit(ExecutorService executorService)
		{
			this.future = executorService.submit(this);
		}

		@Override
		public CheckResult call() throws Exception
		{
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			long schemaId = databaseSchema.getId();
			String[] connectionDetails = schemaDataSourceService.getConnectionDetails(databaseSchema);
			MigrationDataSource migDataSource = new MigrationDataSource(connectionDetails[0], connectionDetails[1],
				connectionDetails[2]);
			try
			{
				schemaDataSourceService.removeFromCache(schemaId);
				DataSourceHolder dataSource = new DataSourceHolder(migDataSource, dialect);
				MigrationsToRun toRun = hibernateMigrationService.checkSchemaForMigrations(dataSource, false);
				String uniqueId = hibernateMigrationService.getUniqueId(dataSource);
				return new CheckResult(toRun, uniqueId);
			}
			finally
			{
				migDataSource.close();
				postMessage(new SimpleMessage(null, new SchemaMessage(Type.CHECKED, schemaId)));
			}
		}

		public long getSchemaId()
		{
			return databaseSchema.getId();
		}

		public Future<CheckResult> getFuture()
		{
			return future;
		}

	}

	private Collection<SchemaInfoImpl> getDuplicates(SchemaInfoImpl schemaInfo, String uniqueId)
	{
		List<SchemaInfoImpl> schemas = Lists.newArrayList();
		DatabaseSchema databaseSchema = schemaInfo.getDatabaseSchema();
		Map<Long, SchemaInfoImpl> allSchemas = schemaInfos;
		for( SchemaInfoImpl otherInfo : allSchemas.values() )
		{
			DatabaseSchema otherSchema = otherInfo.getDatabaseSchema();
			if( !otherInfo.isChecking() && otherSchema.getId() != databaseSchema.getId() )
			{
				String otherUniqueId = otherInfo.getUniqueId();
				if( otherUniqueId != null && otherUniqueId.equals(uniqueId) )
				{
					schemas.add(otherInfo);
				}
			}
		}
		return schemas;
	}

	private static class CheckResult
	{
		private final MigrationsToRun migrationsToRun;
		private final String uniqueId;

		public CheckResult(MigrationsToRun toRun, String uniqueId)
		{
			this.migrationsToRun = toRun;
			this.uniqueId = uniqueId;
		}

		public MigrationsToRun getMigrationsToRun()
		{
			return migrationsToRun;
		}

		public String getUniqueId()
		{
			return uniqueId;
		}
	}

	protected void publishMigrationStatus(MigrationStatus status)
	{
		Map<Long, SchemaInfo> newInfos = Maps.newHashMap();
		for( Entry<Long, SchemaInfoImpl> info : schemaInfos.entrySet() )
		{
			newInfos.put(info.getKey(), info.getValue().immutable());
		}
		migStatus.setSchemas(newInfos);
		setSubTaskStatus(MigrationStatus.KEY_STATUS, status);
		publishStatus();
	}

	private void setupMigrationInfo(SchemaInfoImpl schemaInfo, MigrationsToRun toRun)
	{
		boolean hasErrors = false;
		boolean canRetry = true;
		List<MigrationInfo> infos = new ArrayList<MigrationInfo>();
		for( MigrationState ext : toRun.getMigrations() )
		{
			if( ext.isExecute() && !ext.isPlaceHolder() )
			{
				Migration migration = migrationService.getMigration(ext);
				MigrationInfo info = migration.createMigrationInfo();
				info.setMigrateId(ext.getId());
				boolean failed = ext.getStatus() == LogStatus.ERRORED;
				if( failed )
				{
					hasErrors = true;
					canRetry &= ext.isCanRetry();
				}
				info.setFailed(failed);
				infos.add(info);
			}
		}
		schemaInfo.setAvailable(false);
		schemaInfo.setMigrations(infos);
		schemaInfo.setCanRetry(canRetry);
		schemaInfo.setHasErrors(hasErrors);
		schemaInfo.setInitial(toRun.isFirstTime());
	}

	@BindFactory
	public interface MigrateTaskFactory
	{
		MigrateTask create(long schemaId, boolean system);
	}
}
