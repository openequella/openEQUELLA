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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
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
public class RemoveNavigationGroupsAndLinks extends AbstractHibernateSchemaMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(RemoveNavigationGroupsAndLinks.class)
		+ ".removenavigationgroupsandlinks.";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "title", keyPrefix + "description");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeBaseEntity.class, FakeBaseEntity.Attribute.class, FakeLanguageBundle.class,
				FakeLanguageString.class, FakeNavigationGroup.class, FakeNavigationLink.class, FakeInstitution.class,
				FakeEntityLock.class,};
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return false;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return Collections.emptyList();
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		List<String> sql = helper.getDropTableSql("navigation_link", "navigation_group");
		sql.addAll(helper.getDropColumnSQL("institution", "badge_url"));
		return sql;
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 1 + count(session, "FROM NavigationLink") + count(session, "FROM NavigationGroup");
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// Remove any security privileges for Navigation links/groups
		session.createSQLQuery(
			"DELETE FROM access_entry WHERE privilege LIKE '%_NAVIGATION_GROUP'"
				+ " OR privilege LIKE '%_NAVIGATION_LINK'").executeUpdate();
		result.incrementStatus();

		final List<FakeNavigationLink> fnls = session.createQuery("FROM NavigationLink").list();
		final List<FakeNavigationGroup> fngs = session.createQuery("FROM NavigationGroup").list();

		List<FakeBaseEntity> entities = new ArrayList<FakeBaseEntity>(fnls);
		entities.addAll(fngs);
		if( !entities.isEmpty() )
		{
			session.createQuery("DELETE FROM EntityLock WHERE entity IN (:entities)")
				.setParameterList("entities", entities).executeUpdate();

			for( FakeNavigationLink fnl : fnls )
			{
				session.delete(fnl);
				result.incrementStatus();
			}

			for( FakeNavigationGroup fng : fngs )
			{
				session.delete(fng);
				result.incrementStatus();
			}
		}
	}

	@Entity(name = "BaseEntity")
	@AccessType("field")
	@Inheritance(strategy = InheritanceType.JOINED)
	public static class FakeBaseEntity
	{
		@Id
		long id;

		@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
		FakeLanguageBundle name;
		@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
		FakeLanguageBundle description;

		@JoinColumn
		@ElementCollection(fetch = FetchType.EAGER)
		@CollectionTable(name = "base_entity_attributes", joinColumns = @JoinColumn(name = "base_entity_id"))
		@Fetch(value = FetchMode.SUBSELECT)
		List<Attribute> attributes;

		@Embeddable
		@AccessType("field")
		public static class Attribute implements Serializable
		{
			private static final long serialVersionUID = 1L;

			@Column(length = 64, nullable = false)
			String key;
			@Column(name = "value", length = 1024)
			String value;
		}
	}

	@Entity(name = "LanguageBundle")
	@AccessType("field")
	public static class FakeLanguageBundle
	{
		@Id
		long id;

		@OneToMany(cascade = CascadeType.ALL, mappedBy = "bundle")
		@Fetch(value = FetchMode.SELECT)
		@MapKey(name = "locale")
		Map<String, FakeLanguageString> strings;
	}

	@Entity(name = "LanguageString")
	@AccessType("field")
	public static class FakeLanguageString
	{
		@Id
		long id;

		@Column(length = 20, nullable = false)
		String locale;

		@JoinColumn(nullable = false)
		@ManyToOne(fetch = FetchType.LAZY)
		FakeLanguageBundle bundle;
	}

	@Entity(name = "NavigationGroup")
	@AccessType("field")
	public static class FakeNavigationGroup extends FakeBaseEntity
	{
		// Nothing to declare
	}

	@Entity(name = "NavigationLink")
	@AccessType("field")
	public static class FakeNavigationLink extends FakeBaseEntity
	{
		// Nothing to declare
	}

	@Entity(name = "Institution")
	@AccessType("field")
	public static class FakeInstitution
	{
		@Id
		long id;

		@Column(nullable = false)
		String badgeUrl;
	}

	@Entity(name = "EntityLock")
	@AccessType("field")
	public class FakeEntityLock
	{
		@Id
		long id;

		@OneToOne(fetch = FetchType.LAZY)
		FakeBaseEntity entity;
	}
}
