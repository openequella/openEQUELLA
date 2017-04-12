package com.tle.core.hierarchy.migration;

import java.io.Serializable;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.classic.Session;

import com.google.common.collect.Lists;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ForeignKeyIndexesHT extends AbstractHibernateSchemaMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(ForeignKeyIndexesHT.class) + ".migration.";

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeHierarchyTopic.class, FakeHierarchyTopic.FakeAttribute.class};
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "addfkeys");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// nothing
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 0;
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return null;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		List<String> rv = Lists.newArrayList();
		rv.addAll(helper.getAddIndexesRaw("hierarchy_topic_attributes", "hta_topic", "hierarchy_topic_id"));
		return rv;
	}

	@Entity(name = "HierarchyTopic")
	public static class FakeHierarchyTopic
	{
		@Id
		long id;

		@JoinColumn
		@ElementCollection(fetch = FetchType.EAGER)
		@Fetch(value = FetchMode.SUBSELECT)
		@JoinTable(name = "HierarchyTopic_attributes")
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
