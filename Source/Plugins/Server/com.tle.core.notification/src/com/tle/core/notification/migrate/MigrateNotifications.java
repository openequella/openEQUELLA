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

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.beans.Institution;
import com.tle.beans.item.ItemId;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@SuppressWarnings("nls")
public class MigrateNotifications extends AbstractHibernateSchemaMigration
{
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(MigrateNotifications.class) + ".";

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "from ItemUsersNotified");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		Date convertDate = new Date();
		Query query = session.createSQLQuery("SELECT i.uuid, i.version, i.institution_id,"
			+ " un.element FROM item_users_notified un INNER JOIN item i ON i.id = un.item_id");
		ScrollableResults results = query.scroll();
		while( results.next() )
		{
			Object[] oldnote = results.get();

			ItemId itemId = new ItemId((String) oldnote[0], ((Number) oldnote[1]).intValue());

			Institution inst = new Institution();
			inst.setDatabaseId(((Number) oldnote[2]).longValue());

			FakeNotification notification = new FakeNotification();
			notification.reason = FakeNotification.REASON_WENTLIVE;
			notification.date = convertDate;
			notification.itemid = itemId.toString();
			notification.institution = inst;
			notification.userTo = (String) oldnote[3];

			session.save(notification);
			session.flush();
			session.clear();
		}
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return helper.getCreationSql(new TablesOnlyFilter("notification"));
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeNotification.class, Institution.class, FakeItemUsersNotified.class};
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return helper.getDropTableSql("item_users_notified");
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PREFIX + "migratenotifications.title");
	}

	@Entity(name = "ItemUsersNotified")
	public static class FakeItemUsersNotified
	{
		@Id
		long id;
	}

	@Entity(name = "Notification")
	@AccessType("field")
	public static class FakeNotification
	{
		public static final String REASON_WENTLIVE = "wentlive";

		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(nullable = false)
		@Index(name = "inst_idx")
		@XStreamOmitField
		Institution institution;

		@Column(length = 255, nullable = true)
		@Index(name = "itemid_idx")
		String itemid;
		@Column(length = 8, nullable = false)
		String reason;

		@Column(length = 255, nullable = false)
		@Index(name = "userto_idx")
		String userTo;
		@Column(nullable = false)
		Date date;

		@Column(length = 255)
		String userFrom;
	}

}
