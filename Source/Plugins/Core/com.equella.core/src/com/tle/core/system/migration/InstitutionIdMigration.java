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

package com.tle.core.system.migration;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.google.inject.Singleton;
import com.tle.beans.ConfigurationProperty;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@SuppressWarnings("nls")
@Bind
@Singleton
public class InstitutionIdMigration extends AbstractHibernateSchemaMigration
{
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(InstitutionIdMigration.class) + '.';

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PREFIX + "instid.title");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		@SuppressWarnings("unchecked")
		List<FakeInstitution> insts = session.createQuery("FROM Institution").list();
		for( FakeInstitution inst : insts )
		{
			inst.uniqueId = inst.id;
			session.update(inst);
			result.incrementStatus();
		}
		session.createQuery("DELETE FROM ConfigurationProperty WHERE key.institutionId = 0").executeUpdate();
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "from Institution");
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return helper.getAddNotNullSQL("institution", "unique_id");
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return helper.getAddColumnsSQL("institution", "unique_id");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeInstitution.class, ConfigurationProperty.class,
				ConfigurationProperty.PropertyKey.class,};
	}

	@AccessType("field")
	@Entity(name = "Institution")
	public static class FakeInstitution
	{
		@Id
		@GeneratedValue
		long id;

		@Column(nullable = false)
		Long uniqueId;
	}

}
