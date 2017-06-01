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
public class RemoveCourseMigration extends AbstractHibernateSchemaMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(RemoveCourseMigration.class)
		+ ".removecourse.";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "title", keyPrefix + "description");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeBaseEntity.class, FakeBaseEntity.Attribute.class, FakeLanguageBundle.class,
				FakeLanguageString.class, FakeCourse.class, FakeItemReference.class, FakeEntityLock.class,};
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
		return helper.getDropTableSql("course_items", "course_collaborators", "course_student_users",
			"course_student_groups", "course");
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 1 + count(session, "FROM Course");
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// Remove redundant security privileges
		session.createSQLQuery("DELETE FROM access_entry WHERE privilege = 'LAUNCH_ACTIVITY_MANAGER'").executeUpdate();
		result.incrementStatus();

		final List<FakeCourse> cs = session.createQuery("FROM Course").list();
		if( !cs.isEmpty() )
		{
			session.createQuery("DELETE FROM EntityLock WHERE entity IN (:entities)").setParameterList("entities", cs)
				.executeUpdate();

			for( FakeCourse c : cs )
			{
				session.delete(c);
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

	@Entity(name = "Course")
	@AccessType("field")
	public static class FakeCourse extends FakeBaseEntity
	{
		@ElementCollection
		@CollectionTable(name = "course_collaborators", joinColumns = @JoinColumn(name = "course_id"))
		@Column(name = "element")
		List<String> collaborators;
		@ElementCollection
		@CollectionTable(name = "course_student_users", joinColumns = @JoinColumn(name = "course_id"))
		@Column(name = "element")
		List<String> studentUsers;
		@ElementCollection
		@CollectionTable(name = "course_student_groups", joinColumns = @JoinColumn(name = "course_id"))
		@Column(name = "element")
		List<String> studentGroups;
		@ElementCollection
		@CollectionTable(name = "course_items", joinColumns = @JoinColumn(name = "course_id"))
		List<FakeItemReference> items;
	}

	@Embeddable
	@AccessType("field")
	public static class FakeItemReference
	{
		// Not used, but we need to declare at least one field to avoid
		// Hibernate crashing.
		String type;
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
