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

package com.tle.core.migration.initial;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ItemsWithNullDateCreatedMigration extends AbstractHibernateSchemaMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(ItemsWithNullDateCreatedMigration.class) + ".";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "item.datecreatednull.title", keyPrefix
			+ "item.datecreatednull.description");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{Item.class};
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return false;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return Collections.emptyList();
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return helper.getAddNotNullSQL("item", "date_created", "date_modified");
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 3;
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// If no creation or modification dates, set both to now.
		final Date now = new Date();
		session
			.createQuery(
				"UPDATE Item SET date_created = ?, date_modified = ?"
					+ " WHERE date_created IS NULL AND date_modified IS NULL").setParameter(0, now)
			.setParameter(1, now).executeUpdate();
		result.incrementStatus();

		// If only creation is missing, then set to modification
		session.createQuery("UPDATE Item SET date_created = date_modified WHERE date_created IS NULL").executeUpdate();
		result.incrementStatus();

		// If only modification is missing, then set to creation
		session.createQuery("UPDATE Item SET date_modified = date_created WHERE date_modified IS NULL").executeUpdate();
		result.incrementStatus();
	}

	@AccessType("field")
	@Entity(name = "Item")
	public static class Item
	{
		@Id
		long id;

		@Column(nullable = false)
		Date dateModified;

		@Column(nullable = false)
		Date dateCreated;
	}
}
