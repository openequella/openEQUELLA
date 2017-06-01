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

package com.tle.core.notification.migrate;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.google.common.collect.Sets;
import com.tle.common.i18n.KeyString;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ConstraintToAvoidDuplicateNotificationsMigration extends AbstractHibernateSchemaMigration
{
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(ConstraintToAvoidDuplicateNotificationsMigration.class) + ".";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(new KeyString(KEY_PREFIX + "migrate.duplicateconstraint"));
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// Find dupes and kill them (keep the latest one)
		final ScrollableResults dupes = session.createQuery(getDupesFrom() + " ORDER BY n.date DESC").scroll(
			ScrollMode.FORWARD_ONLY);
		final Set<String> visited = Sets.newHashSet();
		while( dupes.next() )
		{
			final FakeNotification dupe = (FakeNotification) dupes.get(0);
			final String key = dupe.itemid + dupe.reason + dupe.userTo + dupe.institution.id;
			// Ignore the most recent notification, we'll keep this one
			if( !visited.contains(key) )
			{
				visited.add(key);
			}
			else
			{
				session.delete(dupe);
				session.flush();
				session.clear();
			}
			result.incrementStatus();
		}
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session.createQuery("SELECT COUNT(*) " + getDupesFrom()));
	}

	private String getDupesFrom()
	{
		return "FROM Notification n WHERE 1 < (SELECT COUNT(*) FROM Notification n2 WHERE n2.institution = n.institution"
			+ " AND n2.itemid = n.itemid AND n2.reason = n.reason AND n2.userTo = n.userTo)";
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		// false - we specify that we DON'T want to create the foreign key
		// constraint(s). In this case, a foreign key for Institution already
		// exists, and Oracle will object to any attempt to make a superfluous
		// extra constraint (although Postgres for example will happily allow
		// such)
		return helper.getAddIndexesAndConstraintsForColumns("notification", false, "institution_id", "itemid",
			"reason", "user_to");
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return Collections.emptyList();
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeNotification.class, FakeInstitution.class,};
	}

	@Entity(name = "Notification")
	@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"institution_id", "itemid", "reason", "userTo"}))
	public static class FakeNotification
	{
		@Id
		long id;

		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(nullable = false)
		FakeInstitution institution;

		String itemid;
		String userTo;
		String reason;
		Date date;
	}

	@Entity(name = "Institution")
	@AccessType("field")
	public static class FakeInstitution
	{
		@Id
		long id;
	}
}
