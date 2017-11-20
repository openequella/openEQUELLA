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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.google.inject.Singleton;
import com.tle.beans.ConfigurationProperty;
import com.tle.beans.ConfigurationProperty.PropertyKey;
import com.tle.beans.Institution;
import com.tle.common.Check;
import com.tle.common.hash.Hash;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.CurrentDataSource;
import com.tle.core.hibernate.DataSourceHolder;
import com.tle.core.hibernate.DataSourceService;
import com.tle.core.hibernate.HibernateFactory;
import com.tle.core.hibernate.HibernateFactoryService;
import com.tle.core.hibernate.impl.DynamicDataSource;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.MigrationService;
import com.tle.core.migration.beans.SchemaId;
import com.tle.core.migration.beans.SystemConfig;
import com.tle.core.migration.impl.MigrationServiceImpl.MigrationExt;
import com.tle.core.migration.impl.MigrationServiceImpl.MigrationState;
import com.tle.core.migration.log.MigrationLog;
import com.tle.core.migration.log.MigrationLog.LogStatus;
import com.tle.exceptions.BadCredentialsException;

@Bind
@Singleton
@SuppressWarnings("nls")
public class HibernateMigrationService
{
	private HibernateFactory configuration;
	private SessionFactory sessionFactory;

	@Inject
	private MigrationService migrationService;
	@Inject
	private HibernateFactoryService hibernateFactoryService;
	@Inject
	private DataSourceService datasourceService;

	private HibernateTemplate getHibernateTemplate()
	{
		return new HibernateTemplate(getSessionFactory());
	}

	synchronized SessionFactory getSessionFactory()
	{
		if( sessionFactory == null )
		{
			sessionFactory = getHibernateFactory().getSessionFactory();
		}
		return sessionFactory;
	}

	public synchronized HibernateFactory getHibernateFactory()
	{
		if( configuration == null )
		{
			configuration = hibernateFactoryService.createConfiguration(
				new DataSourceHolder(new DynamicDataSource(), datasourceService.getSystemDataSource().getDialect()),
				MigrationLog.class, MigrationLog.LogStatus.class, ConfigurationProperty.class,
				ConfigurationProperty.PropertyKey.class, SystemConfig.class, SchemaId.class);
		}
		return configuration;
	}

	public MigrationsToRun checkSchemaForMigrations(DataSourceHolder dataSource, boolean system)
	{
		CurrentDataSource.set(dataSource);
		SessionFactory factory = getSessionFactory();
		Session session = factory.openSession();
		HibernateMigrationHelper helper = new HibernateMigrationHelper(getHibernateFactory(),
			dataSource.getDefaultSchema());
		if( !helper.tableExists(session, "migration_log") )
		{
			Transaction tx = session.beginTransaction();
			List<String> tableScripts = helper.getCreationSql(new TablesOnlyFilter("migration_log"));
			for( String sql : tableScripts )
			{
				session.createSQLQuery(sql).executeUpdate();
			}
			if( helper.tableExists(session, "item") ) //$NON-NLS-1$
			{
				MigrationLog log = new MigrationLog();
				log.setStatus(LogStatus.SKIPPED);
				log.setExecuted(new Date());
				log.setMigrationId("com.tle.core.migration.initial.InitialSchema");
				session.save(log);
				session.flush();
			}
			tx.commit();
		}
		if( !helper.tableExists(session, "sys_schema_id") ) //$NON-NLS-1$
		{
			Transaction tx = session.beginTransaction();
			List<String> tableScripts = helper.getCreationSql(new TablesOnlyFilter("sys_schema_id")); //$NON-NLS-1$
			for( String sql : tableScripts )
			{
				session.createSQLQuery(sql).executeUpdate();
			}
			SchemaId schemaId = new SchemaId();
			schemaId.setUuid(UUID.randomUUID().toString());
			session.save(schemaId);
			tx.commit();
		}
		MigrationsToRun migrations = findMigrationsToRun(system);
		if( !migrations.isExecutions() )
		{
			List<MigrationState> migList = migrations.getMigrations();
			if( !migList.isEmpty() )
			{
				Transaction tx = session.beginTransaction();
				for( MigrationState ext : migList )
				{
					if( !ext.isExecute() )
					{
						MigrationLog log = ensureLogEntry(ext, session);
						log.setStatus(ext.isSkip() ? LogStatus.SKIPPED : LogStatus.OBSOLETE);
						session.save(log);
					}
				}
				session.flush();
				session.clear();
				tx.commit();
			}
		}
		session.close();
		return migrations;
	}

	@SuppressWarnings("unchecked")
	public MigrationsToRun findMigrationsToRun(boolean system)
	{
		List<MigrationLog> logEntries = getHibernateTemplate().find("from MigrationLog");
		Map<String, MigrationLog> statuses = new HashMap<String, MigrationLog>();
		for( MigrationLog logEntry : logEntries )
		{
			statuses.put(logEntry.getMigrationId(), logEntry);
		}

		boolean initial = false;
		Set<MigrationExt> migrations = migrationService.getOrderedMigrations();
		for( MigrationExt mig : migrations )
		{
			if( mig.isInitial() && mig.isSystem() == system )
			{
				String migId = mig.getId();
				MigrationLog log = statuses.get(migId);
				if( log == null || log.getStatus() == LogStatus.ERRORED )
				{
					initial = true;
					break;
				}
			}
		}
		boolean executions = false;
		List<MigrationState> toRun = new ArrayList<MigrationServiceImpl.MigrationState>();
		Map<String, MigrationState> stateMap = new HashMap<String, MigrationServiceImpl.MigrationState>();
		for( MigrationExt mig : migrations )
		{
			String migId = mig.getId();
			MigrationState state = new MigrationState(mig, statuses.remove(migId));
			stateMap.put(migId, state);

			if( state.needsProcessing() && system == mig.isSystem() )
			{
				if( initial )
				{
					if( !mig.isInitial() )
					{
						state.setObsoleted(true);
					}
				}
				else
				{
					if( mig.isInitial() )
					{
						state.setSkip(true);
					}
				}
				Set<String> obsoletedBy = mig.getObsoletedBy();
				for( String obsoleteId : obsoletedBy )
				{
					MigrationState otherState = stateMap.get(obsoleteId);
					if( otherState.isExecute() )
					{
						state.setObsoleted(true);
						break;
					}
				}
				Set<String> fixes = mig.getFixes();
				for( String fixId : fixes )
				{
					MigrationState otherState = stateMap.get(fixId);
					if( !otherState.wasExecutedAlready() )
					{
						state.setSkip(true);
						break;
					}
				}
				Set<String> ifSkipped = mig.getIfSkipped();
				for( String ifSkipId : ifSkipped )
				{
					MigrationState otherState = stateMap.get(ifSkipId);
					if( !otherState.wasSkippedAlready() )
					{
						state.setSkip(true);
						break;
					}
				}
				if( !state.isObsoleted() && !state.isSkip() )
				{
					state.setExecute(true);
					executions = true;
				}
				toRun.add(state);
			}
		}
		for( MigrationLog log : statuses.values() )
		{
			if( log.isMustExist() )
			{
				throw new RuntimeException(
					"Can not upgrade to this version, missing required backwards incompatible migration: "
						+ log.getMigrationId());
			}
		}
		return new MigrationsToRun(toRun, initial, executions);
	}

	public MigrationLog ensureLogEntry(MigrationState ext, Session session)
	{
		MigrationLog log = ext.getLogEntry();
		if( log == null )
		{
			log = new MigrationLog();
			log.setMigrationId(ext.getId());
		}
		else
		{
			if( !log.isCanRetry() )
			{
				throw new Error("Trying to execute a non-retry migration:" //$NON-NLS-1$
					+ log.getMigrationId());
			}
			log = (MigrationLog) session.merge(log);
		}
		log.setExecuted(new Date());
		return log;
	}

	public Session openSession()
	{
		return getSessionFactory().openSession();
	}

	public String getSystemPassword()
	{
		CurrentDataSource.set(datasourceService.getSystemDataSource());
		Session session = openSession();
		try
		{
			HibernateMigrationHelper helper = new HibernateMigrationHelper(getHibernateFactory(),
				datasourceService.getSystemDataSource().getDefaultSchema());
			if( helper.tableExists(session, SystemConfig.TABLE_NAME) )
			{
				SystemConfig configRow = (SystemConfig) session.get(SystemConfig.class, SystemConfig.ADMIN_PASSWORD);
				if( configRow != null )
				{
					return configRow.getValue();
				}
			}
			else if( helper.tableExists(session, ConfigurationProperty.TABLE_NAME) )
			{
				ConfigurationProperty configRow = (ConfigurationProperty) session.get(ConfigurationProperty.class,
					new PropertyKey(new Institution(), SystemConfig.ADMIN_PASSWORD));
				if( configRow != null )
				{
					return configRow.getValue();
				}

			}
		}
		finally
		{
			session.close();
		}
		return null;
	}

	public MigrationLog getLogEntry(DataSourceHolder dataSource, String migrateId)
	{
		CurrentDataSource.set(dataSource);
		Session session = openSession();
		try
		{
			return (MigrationLog) session.get(MigrationLog.class, migrateId);
		}
		finally
		{
			if( session != null )
			{
				session.close();
			}
		}
	}

	private boolean hasLogEntry(DataSourceHolder dataSource, String migrateId)
	{
		CurrentDataSource.set(dataSource);
		HibernateMigrationHelper helper = new HibernateMigrationHelper(getHibernateFactory(),
			dataSource.getDefaultSchema());
		Session session = openSession();

		if( helper.tableExists(session, "migration_log") )
		{
			Query query = session.createQuery("FROM MigrationLog WHERE migrationId = :migrateId");
			query.setParameter("migrateId", migrateId);
			return !Check.isEmpty(query.list());
		}
		return false;
	}

	private boolean hasRunMigration(DataSourceHolder dataSource, String migrationId)
	{
		return hasLogEntry(dataSource, migrationId);
	}

	public boolean hasRunSystemMigration(String migrationId)
	{
		return hasRunMigration(datasourceService.getSystemDataSource(), migrationId);
	}

	public void checkSystemPassword(String passwordText)
	{
		if( !Hash.checkPasswordMatch(getSystemPassword(), passwordText) )
		{
			throw new BadCredentialsException("Wrong password");
		}
	}

	public String getUniqueId(DataSourceHolder dataSource)
	{
		CurrentDataSource.set(dataSource);
		Session session = openSession();
		try
		{
			return ((SchemaId) session.createQuery("from SchemaId").uniqueResult()).getUuid();
		}
		catch( Exception e )
		{
			return null;
		}
		finally
		{
			session.close();
		}
	}

	public String updateUniqueId(DataSourceHolder dataSource)
	{
		CurrentDataSource.set(dataSource);
		Session session = openSession();
		try
		{
			String uuid = UUID.randomUUID().toString();
			session.createQuery("update SchemaId set uuid = ?").setParameter(0, uuid).executeUpdate();
			return uuid;
		}
		catch( Exception e )
		{
			return null;
		}
		finally
		{
			session.close();
		}
	}
}
