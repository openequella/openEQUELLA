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

package com.tle.core.security.convert.migration.v52;

import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class RemoveUnusedSystemSettingsPrivilegesDatabaseMigration extends AbstractHibernateDataMigration
{
	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.entity.services.migration.unusedsystemsettingsprivs");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		session
			.createQuery(
				"DELETE FROM AccessEntry a WHERE a.targetObject IN ('C:attachmentFileTypes', 'C:proxy', 'C:sif')")
			.executeUpdate();
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		int ct = count(session.createQuery(
			"SELECT COUNT(*) FROM AccessEntry a WHERE a.targetObject IN ('C:attachmentFileTypes', 'C:proxy', 'C:sif')"));
		return ct;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeAccessEntry.class};
	}

	@Entity(name = "AccessEntry")
	@AccessType("field")
	public static class FakeAccessEntry
	{
		@Id
		long id;

		String targetObject;
	}
}
