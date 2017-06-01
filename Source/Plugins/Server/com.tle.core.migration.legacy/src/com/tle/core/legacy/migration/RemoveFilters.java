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
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

/**
 * @author Andrew Gibb
 */

@SuppressWarnings("nls")
@Bind
@Singleton
public class RemoveFilters extends AbstractHibernateSchemaMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(RemoveFilters.class) + ".removefilters.";

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM FilterGroup") + count(session, "FROM ItemFilter");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		List<FakeFilterGroup> fgs = session.createQuery("FROM FilterGroup").list();

		if( !fgs.isEmpty() )
		{
			session.createQuery("DELETE FROM EntityLock WHERE entity IN (:filters)").setParameterList("filters", fgs)
				.executeUpdate();
		}

		for( FakeFilterGroup fg : fgs )
		{
			session.delete(fg);
			result.incrementStatus();
		}
		session.flush();
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return helper.getDropTableSql("filter_control_item_definition");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeFilterGroup.class, FakeBaseEntity.class, FakeEntityLock.class, FakeItemFilter.class,
				FakeLanguageBundle.class, FakeLanguageString.class, FakeFilterControl.class, FakeItemdefControl.class,
				FakeItemDefinition.class, FakeBulkControl.class, FakeCourseControl.class, FakeDateControl.class,
				FakeFilterChangeControl.class, FakeFlagsControl.class, FakePerPageControl.class,
				FakeQueryControl.class, FakeRefineControl.class, FakeSortControl.class, FakeStatusControl.class,
				FakeSynonymControl.class, FakeUserControl.class, FakeWorkflowStepControl.class, FakeXPathControl.class,
				FakeBaseEntity.Attribute.class};
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		List<String> sql = helper.getDropTableSql("filter_group_filters", "item_filter_controls",
			"filter_group_default_controls", "filter_control", "filter_group", "item_filter");
		return sql;
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "title", keyPrefix + "description");
	}

	@Entity(name = "FilterGroup")
	@AccessType("field")
	public static class FakeFilterGroup extends FakeBaseEntity
	{
		@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
		@Fetch(value = FetchMode.SUBSELECT)
		List<FakeFilterControl> defaultControls = new ArrayList<FakeFilterControl>();

		@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
		@Fetch(value = FetchMode.SUBSELECT)
		@IndexColumn(name = "list_position", base = 0)
		List<FakeItemFilter> filters;
	}

	@Entity(name = "BaseEntity")
	@AccessType("field")
	@Inheritance(strategy = InheritanceType.JOINED)
	public static class FakeBaseEntity
	{
		@Id
		public long id;

		@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
		public FakeLanguageBundle name;
		@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
		public FakeLanguageBundle description;

		@JoinColumn
		@ElementCollection(fetch = FetchType.EAGER)
		@CollectionTable(name = "base_entity_attributes", joinColumns = @JoinColumn(name = "base_entity_id"))
		@Fetch(value = FetchMode.SUBSELECT)
		public List<Attribute> attributes;

		@Embeddable
		@AccessType("field")
		public static class Attribute implements Serializable
		{
			private static final long serialVersionUID = 1L;

			@Column(length = 64, nullable = false)
			public String key;
			@Column(name = "value", length = 1024)
			public String value;
		}
	}

	@Entity(name = "LanguageBundle")
	@AccessType("field")
	public static class FakeLanguageBundle
	{
		@Id
		public long id;

		@OneToMany(cascade = CascadeType.ALL, mappedBy = "bundle")
		@Fetch(value = FetchMode.SELECT)
		@MapKey(name = "locale")
		public Map<String, FakeLanguageString> strings;
	}

	@Entity(name = "LanguageString")
	@AccessType("field")
	public static class FakeLanguageString
	{
		@Id
		public long id;

		@Column(length = 20, nullable = false)
		public String locale;

		@JoinColumn(nullable = false)
		@ManyToOne(fetch = FetchType.LAZY)
		public FakeLanguageBundle bundle;
	}

	@Entity(name = "EntityLock")
	@AccessType("field")
	public static class FakeEntityLock
	{
		@Id
		public long id;

		@OneToOne(fetch = FetchType.LAZY)
		public FakeBaseEntity entity;
	}

	@Entity(name = "ItemFilter")
	@AccessType("field")
	public static class FakeItemFilter
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
		@Index(name = "itemFilterDescription")
		FakeLanguageBundle description;

		@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
		@Index(name = "itemFilterName")
		FakeLanguageBundle name;

		@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
		@Fetch(value = FetchMode.SUBSELECT)
		List<FakeFilterControl> controls = new ArrayList<FakeFilterControl>();
	}

	@Entity(name = "ItemdefControl")
	@AccessType("field")
	@DiscriminatorValue("itemdef")
	public class FakeItemdefControl extends FakeFilterControl
	{
		@ManyToMany(fetch = FetchType.LAZY)
		List<FakeItemDefinition> itemDefinitions;
	}

	@Entity(name = "ItemDefinition")
	@AccessType("field")
	public static class FakeItemDefinition extends FakeBaseEntity
	{
		// Nothing to declare
	}

	@Entity(name = "FilterControl")
	@AccessType("field")
	@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
	@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "type")
	public static class FakeFilterControl
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;
	}

	/* Below are all necessary for table joins grrrr */

	@Entity(name = "XPathControl")
	@AccessType("field")
	@DiscriminatorValue("xpath")
	public static class FakeXPathControl extends FakeFilterControl
	{
		// Nothing to declare
	}

	@Entity(name = "WorkflowStepControl")
	@AccessType("field")
	@DiscriminatorValue("step")
	public static class FakeWorkflowStepControl extends FakeFilterControl
	{
		// Nothing to declare
	}

	@Entity(name = "UserControl")
	@AccessType("field")
	@DiscriminatorValue("user")
	public static class FakeUserControl extends FakeFilterControl
	{
		// Nothing to declare
	}

	@Entity(name = "SynonymControl")
	@AccessType("field")
	@DiscriminatorValue("synonym")
	public static class FakeSynonymControl extends FakeFilterControl
	{
		// Nothing to declare
	}

	@Entity(name = "StatusControl")
	@AccessType("field")
	@DiscriminatorValue("status")
	public static class FakeStatusControl extends FakeFilterControl
	{
		// Nothing to declare
	}

	@Entity(name = "SortControl")
	@AccessType("field")
	@DiscriminatorValue("sort")
	public static class FakeSortControl extends FakeFilterControl
	{
		// Nothing to declare
	}

	@Entity(name = "RefineControl")
	@AccessType("field")
	@DiscriminatorValue("refine")
	public static class FakeRefineControl extends FakeFilterControl
	{
		// Nothing to declare
	}

	@Entity(name = "QueryControl")
	@AccessType("field")
	@DiscriminatorValue("query")
	public static class FakeQueryControl extends FakeFilterControl
	{
		// Nothing to declare
	}

	@Entity(name = "PerPageControl")
	@AccessType("field")
	@DiscriminatorValue("perpage")
	public static class FakePerPageControl extends FakeFilterControl
	{
		// Nothing to declare
	}

	@Entity(name = "FlagsControl")
	@AccessType("field")
	@DiscriminatorValue("flags")
	public static class FakeFlagsControl extends FakeFilterControl
	{
		// Nothing to declare
	}

	@Entity(name = "FilterChangeControl")
	@AccessType("field")
	@DiscriminatorValue("change")
	public static class FakeFilterChangeControl extends FakeFilterControl
	{
		// Nothing to declare
	}

	@Entity(name = "FakeDateControl")
	@AccessType("field")
	@DiscriminatorValue("date")
	public static class FakeDateControl extends FakeFilterControl
	{
		// Nothing to declare
	}

	@Entity(name = "CourseControl")
	@AccessType("field")
	@DiscriminatorValue("itemdef")
	public static class FakeCourseControl extends FakeFilterControl
	{
		// Nothing to declare
	}

	@Entity(name = "FakeBulkControl")
	@AccessType("field")
	@DiscriminatorValue("bulk")
	public static class FakeBulkControl extends FakeFilterControl
	{
		// Nothing to declare
	}
}
