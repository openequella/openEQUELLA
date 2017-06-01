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

import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.ScrollableResults;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.google.common.collect.Lists;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.ExtendedDialect;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@SuppressWarnings("nls")
@Bind
@Singleton
public class MigrateNotifications2 extends AbstractHibernateSchemaMigration
{
	private static final String TABLE = "notification";
	private static final String TABLE_BLOBS = "itemdef_blobs";
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(MigrateNotifications2.class) + '.';

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PREFIX + "migratenotifications2.title");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		ScrollableResults scroll = session.createQuery("from Notification").scroll();
		while( scroll.next() )
		{
			FakeNotification note = (FakeNotification) scroll.get(0);
			if( note.reason.equals("review") )
			{
				session.delete(note);
			}
			else
			{
				note.itemidOnly = note.itemid;
				note.processed = true;
				note.batched = false;
				session.save(note);
			}
			session.flush();
			session.clear();
			result.incrementStatus();
		}
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "from Notification");
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		List<String> sql = Lists.newArrayList();
		sql.addAll(helper.getDropColumnSQL(TABLE, "user_from"));
		sql.addAll(helper.getDropColumnSQL(TABLE_BLOBS, "escalations"));
		ExtendedDialect extDialect = helper.getExtDialect();
		boolean drop = extDialect.requiresNoConstraintsForModify();
		if( drop )
		{
			sql.addAll(helper.getDropConstraintsSQL(TABLE, "itemid"));
		}
		sql.addAll(helper.getAddNotNullSQL(TABLE, "processed", "batched", "itemid_only", "itemid"));
		if( drop )
		{
			sql.addAll(helper.getAddIndexesAndConstraintsForColumns(TABLE, "processed", "batched", "itemid_only"));
		}
		else
		{
			sql.addAll(helper.getAddIndexesAndConstraintsForColumns(TABLE, "processed", "batched", "itemid_only",
				"itemid"));
		}
		return sql;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return helper.getAddColumnsSQL(TABLE, "processed", "batched", "itemid_only");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeNotification.class, FakeBlobs.class};
	}

	@Entity(name = "Notification")
	@AccessType("field")
	public static class FakeNotification
	{
		@Id
		long id;

		@Column
		String itemid;

		@Column
		String reason;

		@Column(length = 50)
		@Index(name = "itemidonly_idx")
		String itemidOnly;

		String userFrom;

		@Index(name = "processed_idx")
		Boolean processed;
		@Index(name = "batched_idx")
		Boolean batched;
	}

	@Entity(name = "ItemdefBlobs")
	@AccessType("field")
	public static class FakeBlobs
	{
		@Id
		long id;
		String escalations;
	}

}
