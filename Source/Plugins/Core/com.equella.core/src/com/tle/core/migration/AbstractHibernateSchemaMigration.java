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

import java.util.List;

import org.hibernate.Query;
import org.hibernate.classic.Session;

import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.plugins.impl.PluginServiceImpl;

public abstract class AbstractHibernateSchemaMigration extends AbstractHibernateMigration
{
	private static String prefix = PluginServiceImpl.getMyPluginId(AbstractCombinedSchemaMigration.class) + "."; //$NON-NLS-1$

	@Override
	public void migrate(final MigrationResult result) throws Exception
	{
		result.setupSubTaskStatus(getCalcKey(), 100);
		result.setCanRetry(true);
		final HibernateMigrationHelper helper = createMigrationHelper();
		result.setCanRetry(helper.getExtDialect().canRollbackSchemaChanges());

		if( migrationIsRequired(helper) )
		{
			final List<String> addSql = getAddSql(helper);
			final List<String> dropModifySql = getDropModifySql(helper);
			final int dropModifySize = (dropModifySql != null ? dropModifySql.size() : 0);
			final int addSize = (addSql != null ? addSql.size() : 0);

			runInTransaction(helper.getFactory(), new HibernateCall()
			{
				@Override
				public void run(Session session) throws Exception
				{
					int dataCount = countDataMigrations(helper, session);
					int totalQueries = addSize + dropModifySize + dataCount;

					result.setupSubTaskStatus(getCalcKey(), totalQueries);
					if( addSize > 0 )
					{
						result.setupSubSubTask(getStatusKey(), addSize);
						executeSqlStatements(result, session, addSql);
						session.flush();
					}

					result.setupSubSubTask(getDataKey(), dataCount);
					executeDataMigration(helper, result, session);
					session.flush();

					if( dropModifySize > 0 )
					{
						result.setupSubSubTask(getStatusKey(), dropModifySize);
						executeSqlStatements(result, session, dropModifySql);
					}
				}
			});
		}
	}

	protected boolean migrationIsRequired(HibernateMigrationHelper helper)
	{
		return true;
	}

	protected abstract void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result,
		Session session) throws Exception;

	protected abstract int countDataMigrations(HibernateMigrationHelper helper, Session session);

	protected abstract List<String> getDropModifySql(HibernateMigrationHelper helper);

	protected abstract List<String> getAddSql(HibernateMigrationHelper helper);

	@Override
	protected abstract Class<?>[] getDomainClasses();

	protected String getStatusKey()
	{
		return prefix + "migration.sqlstatus"; //$NON-NLS-1$
	}

	protected String getDataKey()
	{
		return prefix + "migration.datastatus"; //$NON-NLS-1$
	}

	protected String getCalcKey()
	{
		return prefix + "migration.datacalc"; //$NON-NLS-1$
	}

	/**
	 * @param session
	 * @param fromAndClause you need to include the FROM
	 * @return
	 */
	protected int count(Session session, String fromAndClause)
	{
		return count(session.createQuery("SELECT COUNT(*) " + fromAndClause)); //$NON-NLS-1$
	}

	protected int count(Query query)
	{
		return ((Number) query.uniqueResult()).intValue();
	}
}
