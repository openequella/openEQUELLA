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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.migration.MigrationStatusLog;
import com.tle.core.migration.MigrationStatusLog.LogType;
import com.tle.core.plugins.impl.PluginServiceImpl;
import com.tle.core.workflow.migrate.AddNotificationSchemaOrig.FakeCollection;
import com.tle.core.workflow.migrate.AddNotificationSchemaOrig.FakeHistoryEvent;
import com.tle.core.workflow.migrate.AddNotificationSchemaOrig.FakeItem;
import com.tle.core.workflow.migrate.AddNotificationSchemaOrig.FakeModerationStatus;
import com.tle.core.workflow.migrate.AddNotificationSchemaOrig.FakeWorkflowItem;
import com.tle.core.workflow.migrate.AddNotificationSchemaOrig.FakeWorkflowItemStatus;

@Bind
@Singleton
@SuppressWarnings("nls")
public class CreateTaskHistoryTable extends AbstractHibernateSchemaMigration
{
	private static String keyPrefix = PluginServiceImpl.getMyPluginId(CreateTaskHistoryTable.class) + ".";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("FIXME.YOU.SHOULD.NEVER.SEE.THIS.IN.PRODUCTION");
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		Query query = session
			.createQuery("update Item set moderating = true, dateForIndex = ? where status = 'MODERATING' and moderating = false");
		query.setParameter(0, new Date());
		int fixes = query.executeUpdate();
		if( fixes > 0 )
		{
			result.addLogEntry(new MigrationStatusLog(LogType.WARNING, keyPrefix + "wrongmod", fixes));
		}
		ScrollableResults results = session.createQuery(
			"SELECT ws.started, i.id, ws.wnode.id FROM Item i JOIN i.moderation AS ms JOIN ms.statuses AS ws "
				+ "WHERE ws.status = 'i' AND ws.acttype = 'task' and i.moderating = true").scroll();

		while( results.next() )
		{
			Object[] resultArray = results.get();
			Date started = (Date) resultArray[0];
			FakeItem i = new FakeItem();
			i.id = ((Number) resultArray[1]).longValue();
			FakeWorkflowItem wi = new FakeWorkflowItem();
			wi.id = ((Number) resultArray[2]).longValue();

			FakeTaskHistory th = new FakeTaskHistory();
			th.item = i;
			th.task = wi;
			th.entryDate = started;
			th.exitDate = null;

			session.save(th);
			session.flush();
			session.clear();
		}
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM WorkflowNodeStatus");
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return Collections.emptyList();
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return helper.getCreationSql(new TablesOnlyFilter("task_history"));
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeTaskHistory.class, FakeWorkflowItem.class, FakeWorkflowItemStatus.class,
				FakeModerationStatus.class, FakeItem.class, FakeCollection.class, FakeHistoryEvent.class};
	}

	@Entity(name = "TaskHistory")
	class FakeTaskHistory
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@Index(name = "th_item")
		@ManyToOne(fetch = FetchType.LAZY)
		FakeItem item;

		@Index(name = "th_task")
		@ManyToOne(fetch = FetchType.LAZY)
		FakeWorkflowItem task;

		@Index(name = "th_entry")
		Date entryDate;

		@Index(name = "th_exit")
		Date exitDate;
	}
}
