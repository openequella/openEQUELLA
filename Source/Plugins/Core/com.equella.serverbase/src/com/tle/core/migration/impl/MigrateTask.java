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
import java.util.List;

import javax.inject.Inject;

import org.hibernate.Transaction;
import org.hibernate.classic.Session;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.core.hibernate.CurrentDataSource;
import com.tle.core.hibernate.DataSourceHolder;
import com.tle.core.hibernate.DataSourceService;
import com.tle.core.migration.Migration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.migration.MigrationService;
import com.tle.core.migration.MigrationSubTaskStatus;
import com.tle.core.migration.impl.MigrationServiceImpl.MigrationState;
import com.tle.core.migration.log.MigrationLog;
import com.tle.core.migration.log.MigrationLog.LogStatus;
import com.tle.core.services.impl.SingleShotTask;
import com.tle.core.system.service.SchemaDataSourceService;

public final class MigrateTask extends SingleShotTask
{
	@Inject
	private MigrationService migrationService;
	@Inject
	private HibernateMigrationService hibernateMigrationService;
	@Inject
	private DataSourceService dataSourceService;
	@Inject
	private SchemaDataSourceService databaseSchemaService;

	private final long schemaId;
	private final boolean system;

	@AssistedInject
	public MigrateTask(@Assisted long schemaId, @Assisted boolean system)
	{
		this.schemaId = schemaId;
		this.system = system;
	}

	@Override
	public void runTask() throws Exception
	{
		DataSourceHolder dataSource;
		if( system )
		{
			dataSource = dataSourceService.getSystemDataSource();
		}
		else
		{
			dataSource = databaseSchemaService.getDataSourceForId(schemaId);
		}
		CurrentDataSource.set(dataSource);
		MigrationsToRun toRun = hibernateMigrationService.findMigrationsToRun(system);
		Session session = hibernateMigrationService.openSession();
		List<MigrationInfo> infos = new ArrayList<MigrationInfo>();
		List<MigrationState> migrationsToExecute = toRun.getMigrations();
		Transaction tx = session.beginTransaction();
		for( MigrationState ext : migrationsToExecute )
		{
			if( ext.isExecute() )
			{
				if( !ext.isPlaceHolder() )
				{
					Migration migration = migrationService.getMigration(ext);
					MigrationInfo info = migration.createMigrationInfo();
					info.setMigrateId(ext.getId());
					info.setCanRetry(ext.isCanRetry());
					infos.add(info);
				}
			}
			else
			{
				MigrationLog log = hibernateMigrationService.ensureLogEntry(ext, session);
				log.setStatus(ext.isSkip() ? LogStatus.SKIPPED : LogStatus.OBSOLETE);
				session.save(log);
			}
		}
		session.flush();
		session.clear();
		tx.commit();
		updateMigrationInfos(infos);
		setupStatus("com.tle.core.migration.task.status", infos.size()); //$NON-NLS-1$
		int i = 0;
		for( MigrationState ext : migrationsToExecute )
		{
			if( ext.isExecute() )
			{
				if( ext.isPlaceHolder() )
				{
					tx = session.beginTransaction();
					MigrationLog log = hibernateMigrationService.ensureLogEntry(ext, session);
					log.setStatus(LogStatus.EXECUTED);
					session.save(log);
					session.flush();
					session.clear();
					tx.commit();
				}
				else
				{
					executeMigration(ext, session, infos.get(i), infos);
					incrementWork();
					i++;
				}
			}
		}
		session.close();
	}

	private void executeMigration(MigrationState ext, Session session, MigrationInfo info, List<MigrationInfo> infos)
		throws Exception
	{
		Exception error = null;
		Migration migration = migrationService.getMigration(ext);
		MigrationLog log = hibernateMigrationService.ensureLogEntry(ext, session);
		log.setMustExist(!migration.isBackwardsCompatible());
		MigrationResult status = new MigrationResult(this);
		try
		{
			info.setExecuting(true);
			updateMigrationInfos(infos);
			migration.migrate(status);
			log.setStatus(LogStatus.EXECUTED);
			log.setMessage(status.getMessage());
			log.setErrorMessage(null);
		}
		catch( Exception t )
		{
			error = t;
			MigrationServiceImpl.getLogger().error("Error migrating:" + ext.getId(), t); //$NON-NLS-1$
			info.setCanRetry(status.isCanRetry());
			log.setCanRetry(status.isCanRetry());
			StringWriter strWriter = new StringWriter();
			t.printStackTrace(new PrintWriter(strWriter));
			log.setMessage(status.getMessage());
			String errorMsg = strWriter.toString();
			log.setErrorMessage(errorMsg);
			info.setFailed(true);
			info.setError(errorMsg);
			log.setStatus(LogStatus.ERRORED);
		}
		updateSubtask(null);
		info.setProcessed(true);
		info.setExecuting(false);
		Transaction tx = session.beginTransaction();
		session.save(log);
		session.flush();
		session.clear();
		tx.commit();
		updateMigrationInfos(infos);
		if( error != null )
		{
			throw error;
		}
	}

	private void updateMigrationInfos(List<MigrationInfo> infos)
	{
		setSubTaskStatus("migrations", (Serializable) infos); //$NON-NLS-1$
	}

	@Override
	protected String getTitleKey()
	{
		return "com.tle.core.migration.task.title"; //$NON-NLS-1$
	}

	public void updateSubtask(MigrationSubTaskStatus subtask)
	{
		setSubTaskStatus("subtask", subtask); //$NON-NLS-1$
	}
}