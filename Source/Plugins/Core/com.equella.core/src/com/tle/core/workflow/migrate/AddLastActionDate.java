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

import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.Query;
import org.hibernate.ScrollableResults;
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
public class AddLastActionDate extends AbstractHibernateSchemaMigration
{
	private static String keyPrefix = PluginServiceImpl.getMyPluginId(AddLastActionDate.class) + "."; //$NON-NLS-1$

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session.createQuery("select count(*) from Item where moderating = true or status = 'rejected'"));
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		Query query = session
			.createQuery("select i.id, i.moderation.id from Item i where moderating = true or status = 'rejected'");
		ScrollableResults results = query.scroll();
		while( results.next() )
		{
			Query upQuery = session
				.createQuery("update ModerationStatus m set lastAction = "
					+ "(select max(h.date) from Item i join i.history h where i.id = ? and h.type in ('approved','rejected', 'comment') group by i.id) "
					+ "where m.id = ?");
			upQuery.setParameter(0, results.get(0));
			upQuery.setParameter(1, results.get(1));
			upQuery.executeUpdate();
		}
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return helper.getAddColumnsSQL("moderation_status", "last_action");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{NewWorkflowStatus.class, FakeItem.class, FakeHistoryEvent.class};
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return null;
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "migrateaddlast.title"); //$NON-NLS-1$
	}

	@Entity(name = "ModerationStatus")
	@AccessType("field")
	public static class NewWorkflowStatus
	{
		@Id
		long id;
		Date lastAction;
	}

	@Entity(name = "Item")
	@AccessType("field")
	public static class FakeItem
	{
		@Id
		long id;

		boolean moderating;
		String status;
		@ManyToOne
		NewWorkflowStatus moderation;
		@OneToMany
		List<FakeHistoryEvent> history;
	}

	@Entity(name = "HistoryEvent")
	@AccessType("field")
	public static class FakeHistoryEvent
	{
		@Id
		long id;
		Date date;
		String type;
	}

}
