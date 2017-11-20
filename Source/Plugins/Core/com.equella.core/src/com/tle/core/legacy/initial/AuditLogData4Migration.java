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

package com.tle.core.legacy.initial;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

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
public class AuditLogData4Migration extends AbstractHibernateSchemaMigration
{
	private static final String TABLE_NAME = "audit_log_entry";
	private static final String NEW_SETTING_COLUMN = "data4";
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(AuditLogData4Migration.class) + ".";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "auditlog.data4migration.title", keyPrefix
			+ "auditlog.data4migration.description");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{AuditLogEntry.class};
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return false;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		List<String> sql = new ArrayList<String>();
		sql.addAll(helper.getAddColumnsSQL(TABLE_NAME, NEW_SETTING_COLUMN));
		sql.addAll(helper.getAddIndexesAndConstraintsForColumns(TABLE_NAME, NEW_SETTING_COLUMN));
		return sql;
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return Collections.emptyList();
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 3;
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// Copy path for CONTENT_VIEWED from data3 to data4
		session.createQuery("UPDATE AuditLogEntry SET data4 = data3 WHERE eventType = 'CONTENT_VIEWED'")
			.executeUpdate();
		result.incrementStatus();

		// Remove old data3 entries
		session.createQuery("UPDATE AuditLogEntry SET data3 = NULL WHERE eventType = 'CONTENT_VIEWED'").executeUpdate();
		result.incrementStatus();

		// Change all Item summary and content view to the ITEM category (was
		// incorrectly ENTITY before).
		session.createQuery(
			"UPDATE AuditLogEntry SET eventCategory = 'ITEM' WHERE eventType IN ('CONTENT_VIEWED', 'SUMMARY_VIEWED')")
			.executeUpdate();
		result.incrementStatus();
	}

	@AccessType("field")
	@Entity(name = "AuditLogEntry")
	public static class AuditLogEntry implements Serializable
	{
		private static final long serialVersionUID = 1L;

		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		private long id;

		@Column(length = 20)
		private String eventCategory;

		@Column(length = 20)
		private String eventType;

		@Column(length = 255)
		private String data3;

		@Lob
		private String data4;

		public AuditLogEntry()
		{
			super();
		}

		public long getId()
		{
			return id;
		}

		public void setId(long id)
		{
			this.id = id;
		}

		public String getEventCategory()
		{
			return eventCategory;
		}

		public void setEventCategory(String eventCategory)
		{
			this.eventCategory = eventCategory;
		}

		public String getEventType()
		{
			return eventType;
		}

		public void setEventType(String eventType)
		{
			this.eventType = eventType;
		}

		public String getData3()
		{
			return data3;
		}

		public void setData3(String data3)
		{
			this.data3 = data3;
		}

		public String getData4()
		{
			return data4;
		}

		public void setData4(String data4)
		{
			this.data4 = data4;
		}
	}
}
