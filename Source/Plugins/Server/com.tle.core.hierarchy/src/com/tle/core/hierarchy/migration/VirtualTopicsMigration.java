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

package com.tle.core.hierarchy.migration;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class VirtualTopicsMigration extends AbstractHibernateSchemaMigration
{
	private static final String TITLE_KEY = PluginServiceImpl.getMyPluginId(VirtualTopicsMigration.class)
		+ ".migration.virtualtopics.title";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(TITLE_KEY);
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeTopic.class, FakeTopic.FakeAttribute.class,};
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 0;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		List<String> rv = helper.getCreationSql(new TablesOnlyFilter("hierarchy_topic_attributes"));
		rv.addAll(helper.getAddColumnsSQL("hierarchy_topic", "virtualisation_id", "virtualisation_path"));
		rv.addAll(helper.getAddIndexesRaw("hierarchy_topic_attributes", "hta_topic", "hierarchy_topic_id"));
		return rv;
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return Collections.emptyList();
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// Nothing to do!
	}

	@Entity(name = "HierarchyTopic")
	@AccessType("field")
	public static class FakeTopic
	{
		@Id
		long id;

		@Column(length = 100)
		String virtualisationId;

		@Column(length = 255)
		String virtualisationPath;

		@JoinColumn
		@ElementCollection(fetch = FetchType.EAGER)
		@CollectionTable(name = "hierarchy_topic_attributes", joinColumns = @JoinColumn(name = "hierarchy_topic_id"))
		@Fetch(value = FetchMode.SUBSELECT)
		List<FakeAttribute> attributes;

		@Embeddable
		@AccessType("field")
		public static class FakeAttribute implements Serializable
		{
			private static final long serialVersionUID = 1L;

			@Column(length = 64, nullable = false)
			String key;
			@Column(name = "value", length = 1024)
			String value;
		}
	}
}
