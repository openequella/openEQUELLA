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

package com.tle.core.institution.migration.v41;

import java.util.List;

import javax.inject.Singleton;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.Query;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;

@Bind
@Singleton
public class ItemNavigationIndexMigration extends AbstractHibernateSchemaMigration
{
	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM ItemNavigationNode");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		Query query = session.createQuery("FROM ItemNavigationNode");
		List<FakeItemNavigationNode> nodes = query.list();

		int index = 0;

		for( FakeItemNavigationNode node : nodes )
		{
			index = 0;
			for( FakeItemNavigationTab tab : node.tabs )
			{
				tab.tabindex = index;
				session.update(tab);
				index++;
			}
			result.incrementStatus();
		}
		session.flush();
		session.clear();
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return helper.getAddColumnsSQL("item_navigation_tab", "tabindex");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeItemNavigationNode.class, FakeItemNavigationTab.class};
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return helper.getAddNotNullSQL("item_navigation_tab", "tabindex");
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.entity.services.itemnavigationindex.title");
	}

	@Entity(name = "ItemNavigationNode")
	@AccessType("field")
	public static class FakeItemNavigationNode
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		public long id;

		@OneToMany(fetch = FetchType.LAZY, mappedBy = "node", cascade = CascadeType.ALL, orphanRemoval = true)
		@Fetch(value = FetchMode.SUBSELECT)
		List<FakeItemNavigationTab> tabs;
	}

	@Entity(name = "ItemNavigationTab")
	@AccessType("field")
	public static class FakeItemNavigationTab
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		public long id;

		@ManyToOne
		@JoinColumn(nullable = false)
		@Index(name = "itemNavTabNode")
		public FakeItemNavigationNode node;

		@Column(nullable = false)
		Integer tabindex;
	}
}
