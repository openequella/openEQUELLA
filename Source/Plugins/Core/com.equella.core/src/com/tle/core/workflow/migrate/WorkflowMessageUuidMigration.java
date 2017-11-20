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

package com.tle.core.workflow.migrate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
public class WorkflowMessageUuidMigration extends AbstractHibernateSchemaMigration
{
	private static final int BATCH_SIZE = 1000;
	private static final String TABLE = "workflow_message";
	private static final String COLUMN = "uuid";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		String keyPrefix = PluginServiceImpl.getMyPluginId(WorkflowMessageUuidMigration.class) + ".";
		return new MigrationInfo(keyPrefix + "migration.workflowmessageuuid.title");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeWorkflowMessage.class};
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
		sql.addAll(helper.getAddColumnsSQL(TABLE, COLUMN));
		return sql;
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		List<String> dropModify = new ArrayList<String>();
		dropModify.addAll(helper.getAddNotNullSQL(TABLE, COLUMN));
		dropModify.addAll(helper.getAddIndexesAndConstraintsForColumns(TABLE, COLUMN));
		return dropModify;
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM WorkflowMessage");
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		List<FakeWorkflowMessage> messages = session.createQuery("FROM WorkflowMessage").list();
		session.clear();

		int i = 0;
		for( FakeWorkflowMessage m : messages )
		{
			m.uuid = UUID.randomUUID().toString();
			session.update(m);
			i++;
			if( i % BATCH_SIZE == 0 )
			{
				session.flush();
				session.clear();
			}
			result.incrementStatus();
		}
		session.flush();
		session.clear();
	}

	@Entity(name = "WorkflowMessage")
	@Table(name = "workflow_message")
	@AccessType("field")
	public static class FakeWorkflowMessage
	{
		@Id
		long id;
		@Column(length = 40, nullable = false)
		@Index(name = "messageUuidIndex")
		String uuid;
	}
}
