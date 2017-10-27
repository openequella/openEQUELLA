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

package com.tle.core.freetext.migration;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.CurrentDataSource;
import com.tle.core.hibernate.HibernateFactoryService;
import com.tle.core.migration.AbstractHibernateMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
public class ReIndexMigration extends AbstractHibernateMigration
{
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(ReIndexMigration.class) + "."; //$NON-NLS-1$

	@Inject
	private HibernateFactoryService hibernateService;

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{};
	}

	@SuppressWarnings("nls")
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PREFIX + "reindex.title", KEY_PREFIX + "reindex.desc");
	}

	@Override
	public void migrate(MigrationResult status) throws Exception
	{
		runInTransaction(hibernateService.createConfiguration(CurrentDataSource.get(), getDomainClasses())
			.getSessionFactory(), new HibernateCall()
		{
			@SuppressWarnings("nls")
			@Override
			public void run(Session session) throws Exception
			{
				session.createSQLQuery("update item set date_for_index = ?").setParameter(0, new Date())
					.executeUpdate();

			}
		});
	}
}
