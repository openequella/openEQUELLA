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

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.hibernate.ScrollableResults;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.workflow.migrate.AddNotificationSchemaOrig.FakeModerationStatus;
import com.tle.core.workflow.migrate.AddNotificationSchemaOrig.FakeWorkflowItem;
import com.tle.core.workflow.migrate.AddNotificationSchemaOrig.FakeWorkflowItemStatus;

@Bind
@SuppressWarnings("nls")
public class AddTaskStartDate extends AbstractHibernateSchemaMigration
{
	private static final String QUERY = "from WorkflowNodeStatus where acttype = 'task' and status = 'i'";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("COMBINED:SHOULD NEVER SEE IN RELEASE");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		ScrollableResults scroll = session.createQuery(QUERY).scroll();
		while( scroll.next() )
		{
			FakeWorkflowItemStatus item = (FakeWorkflowItemStatus) scroll.get(0);
			if( item.dateDue != null )
			{
				item.started = new Date(item.dateDue.getTime() - TimeUnit.DAYS.toMillis(item.wnode.escalationdays));
			}
			else
			{
				item.started = new Date();
			}
			session.save(item);
			session.flush();
			session.clear();
			result.incrementStatus();
		}
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, QUERY);
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return null;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return helper.getAddColumnsSQL("workflow_node_status", "started");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeWorkflowItemStatus.class, FakeWorkflowItem.class, FakeModerationStatus.class};
	}

}
