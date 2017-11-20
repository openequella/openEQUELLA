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

package com.tle.core.institution.migration.v50;

import java.io.Serializable;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;

/**
 * @author Aaron
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class AddManualNavigationFlag extends AbstractHibernateSchemaMigration
{
	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 0;
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		session.createSQLQuery("UPDATE item SET manual_navigation = ? WHERE manual_navigation IS NULL")
			.setBoolean(0, false).executeUpdate();
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return helper.getAddColumnsSQL("item", "manual_navigation");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeItem.class, FakeNavigationSettings.class};
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return helper.getAddNotNullSQL("item", "manual_navigation");
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.entity.services.migration.manualnavigationflag");
	}

	@Entity(name = "Item")
	@AccessType("field")
	public static class FakeItem
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@Embedded
		FakeNavigationSettings navigationSettings = new FakeNavigationSettings();
	}

	@Embeddable
	@AccessType("field")
	public static class FakeNavigationSettings implements Serializable
	{
		private static final long serialVersionUID = 1L;

		@Column(name = "manualNavigation")
		boolean manualNavigation;
	}
}
