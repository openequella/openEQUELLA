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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.hibernate.classic.Session;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;

public abstract class AbstractCombinedSchemaMigration extends AbstractHibernateSchemaMigration
{
	protected abstract List<AbstractHibernateSchemaMigration> getMigrations();

	private AbstractHibernateSchemaMigration getFirstMigration()
	{
		return getMigrations().get(0);
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		List<AbstractHibernateSchemaMigration> migrations = getMigrations();
		int count = 0;
		for( AbstractHibernateSchemaMigration migration : migrations )
		{
			count += migration.countDataMigrations(helper, session);
		}
		return count;
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		for( AbstractHibernateSchemaMigration migration : getMigrations() )
		{
			migration.executeDataMigration(helper, result, session);
		}
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		List<String> addSql = Lists.newArrayList();
		for( AbstractHibernateSchemaMigration migration : getMigrations() )
		{
			List<String> anAdd = migration.getAddSql(helper);
			if( anAdd != null )
			{
				addSql.addAll(anAdd);
			}
		}
		return addSql;
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		List<String> dropModifySql = Lists.newArrayList();
		for( AbstractHibernateSchemaMigration migration : getMigrations() )
		{
			List<String> aDrop = migration.getDropModifySql(helper);
			if( aDrop != null )
			{
				dropModifySql.addAll(aDrop);
			}
		}
		return dropModifySql;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		Set<Class<?>> clazzes = Sets.newHashSet();
		for( AbstractHibernateSchemaMigration migration : getMigrations() )
		{
			clazzes.addAll(Arrays.asList(migration.getDomainClasses()));
		}
		return clazzes.toArray(new Class<?>[clazzes.size()]);
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return getFirstMigration().createMigrationInfo();
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return getFirstMigration().isBackwardsCompatible();
	}
}
