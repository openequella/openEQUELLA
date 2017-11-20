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

package com.tle.core.legacy.migration;

import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.hibernate.Query;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
public class RemoveHierarchyColumnsMigration extends AbstractHibernateSchemaMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(RemoveHierarchyColumnsMigration.class) + ".removehierarchycolumns."; //$NON-NLS-1$

	private static final String HIERARCHY_TABLE = "hierarchy_topic"; //$NON-NLS-1$

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 3 + count(session, "FROM HierarchyTopic WHERE key_resources_section_name_id IS NOT NULL"); //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		Query query = session.createQuery("FROM HierarchyTopic WHERE key_resources_section_name_id IS NOT NULL"); //$NON-NLS-1$
		List<FakeHierarchyTopic> hts = query.list();

		for( FakeHierarchyTopic ht : hts )
		{
			session.delete(ht.keyResourcesSectionName);
			ht.keyResourcesSectionName = null;
		}

		session.flush();
		session.clear();
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return Collections.emptyList();
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeHierarchyTopic.class, LanguageBundle.class, LanguageString.class};
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return helper.getDropColumnSQL(HIERARCHY_TABLE, "key_resources_section_name_id", //$NON-NLS-1$
			"subtopic_column_ordering", "show_subtopic_result_count"); //$NON-NLS-1$//$NON-NLS-2$
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "title", keyPrefix + "description"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Entity(name = "HierarchyTopic")
	@AccessType("field")
	public static class FakeHierarchyTopic
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		boolean subtopicColumnOrdering;

		@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
		@Index(name = "hierarchyKRName")
		LanguageBundle keyResourcesSectionName;

		Boolean showSubtopicResultCount;
	}
}
