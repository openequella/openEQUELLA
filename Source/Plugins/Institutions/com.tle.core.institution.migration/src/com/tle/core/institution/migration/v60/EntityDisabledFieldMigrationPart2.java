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

package com.tle.core.institution.migration.v60;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;
import org.hibernate.jdbc.Work;

import com.google.common.collect.Lists;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;

@Bind
@Singleton
@SuppressWarnings("nls")
public class EntityDisabledFieldMigrationPart2 extends AbstractHibernateSchemaMigration
{
	private static final String TABLE_FED = "federated_search";
	private static final String COL_DISABLED = "disabled";

	@Override
	protected boolean migrationIsRequired(HibernateMigrationHelper helper)
	{
		Session session = helper.getFactory().openSession();
		try
		{
			CheckColumnWork work = new CheckColumnWork(helper);
			session.doWork(work);
			return work.isColumnExists();
		}
		finally
		{
			session.close();
		}
	}

	public static class CheckColumnWork implements Work
	{
		private final HibernateMigrationHelper helper;
		private boolean columnExists;

		public CheckColumnWork(HibernateMigrationHelper helper)
		{
			this.helper = helper;
		}

		/**
		 * When running on Oracle, we actually need to uppercase the table name.
		 * Specifying the column name as the fourth parameter to getColumns is
		 * optional but again, if on Oracle, the column name if present must
		 * also be uppercased (we could otherwise pass null as the column name
		 * filter, and then search through all the returned columns, being sure
		 * to do a case-insensitive match on the column name as String)
		 */
		@Override
		public void execute(Connection connection) throws SQLException
		{
			final DatabaseMetaData metaData = connection.getMetaData();
			final String defaultCatalog = helper.getDefaultCatalog();
			final String defaultSchema = helper.getDefaultSchema();

			final String metadataFriendlyTableName = helper.getExtDialect().getNameForMetadataQuery(TABLE_FED, false);
			final String metadataFriendlyColumnName = helper.getExtDialect().getNameForMetadataQuery(COL_DISABLED,
				false);
			final ResultSet columnSet = metaData.getColumns(defaultCatalog, defaultSchema, metadataFriendlyTableName,
				metadataFriendlyColumnName);

			try
			{
				// If there's a next(), then the column we're looking for is it.
				columnExists = columnSet.next();
			}
			finally
			{
				columnSet.close();
			}
		}

		public boolean isColumnExists()
		{
			return columnExists;
		}
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.entity.services.migration.v60.disabledentity2.title");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		session
			.createSQLQuery(
				"update base_entity set disabled = :true where id in (select fs.id from federated_search fs where fs.disabled = :true)")
			.setParameter("true", true).executeUpdate();
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 1;
	}

	@Override
	protected List<String> getDropModifySql(final HibernateMigrationHelper helper)
	{
		final List<String> dropModify = Lists.newArrayList();
		dropModify.addAll(helper.getDropColumnSQL(TABLE_FED, COL_DISABLED));
		return dropModify;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return null;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeFedSearch.class};
	}

	@Entity(name = "FederatedSearch")
	@AccessType("field")
	public static class FakeFedSearch
	{
		@Id
		long id;

		boolean disabled;
	}
}
